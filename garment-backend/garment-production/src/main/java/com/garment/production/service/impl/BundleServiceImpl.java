package com.garment.production.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.garment.common.context.TenantContext;
import com.garment.common.exception.BusinessException;
import com.garment.production.dto.CreateCutOrderRequest;
import com.garment.production.dto.GenerateBundlesRequest;
import com.garment.production.entity.Bundle;
import com.garment.production.entity.CutOrder;
import com.garment.production.mapper.BundleMapper;
import com.garment.production.service.BundleService;
import com.garment.production.service.CutOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 包服务实现
 *
 * @author system
 */
@Slf4j
@Service
public class BundleServiceImpl extends ServiceImpl<BundleMapper, Bundle> implements BundleService {

    private final CutOrderService cutOrderService;

    // Add explicit constructor with @Lazy to break circular dependency
    public BundleServiceImpl(@Lazy CutOrderService cutOrderService) {
        this.cutOrderService = cutOrderService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Bundle> generateBundles(GenerateBundlesRequest request) {
        // 获取裁床订单
        CutOrder cutOrder = cutOrderService.getById(request.getCutOrderId());
        if (cutOrder == null) {
            throw new BusinessException("裁床订单不存在");
        }
        
        if (!"confirmed".equals(cutOrder.getStatus())) {
            throw new BusinessException("只有已确认的订单才能生成包");
        }
        
        // 检查是否已生成包
        if (!request.getRegenerate() && cutOrder.getBundleCount() != null && cutOrder.getBundleCount() > 0) {
            throw new BusinessException("该订单已生成包，如需重新生成请设置regenerate=true");
        }
        
        // 删除已有的包（如果重新生成）
        if (request.getRegenerate()) {
            LambdaQueryWrapper<Bundle> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Bundle::getCutOrderId, request.getCutOrderId());
            remove(wrapper);
        }
        
        // 解析尺码比例
        Map<String, Integer> sizeRatio = JSON.parseObject(cutOrder.getSizeRatio(), 
            new TypeReference<Map<String, Integer>>() {});
        
        List<Bundle> bundles = new ArrayList<>();
        
        if ("average".equals(cutOrder.getCuttingType())) {
            // 平均裁
            bundles = generateAverageBundles(cutOrder, sizeRatio, request.getBundleSize());
        } else if ("segment".equals(cutOrder.getCuttingType())) {
            // 分层段裁
            bundles = generateSegmentBundles(cutOrder, request.getBundleSize());
        }
        
        // 生成二维码
        for (Bundle bundle : bundles) {
            bundle.setQrCode(generateQrCode(bundle));
        }
        
        // 批量保存
        saveBatch(bundles);
        
        // 更新裁床订单的包数量
        cutOrder.setBundleCount(bundles.size());
        cutOrder.setUpdatedBy(1L); // TODO: 从当前用户上下文获取
        cutOrder.setUpdatedAt(LocalDateTime.now());
        cutOrderService.updateById(cutOrder);
        
        log.info("生成包成功，订单号：{}，包数量：{}", cutOrder.getOrderNo(), bundles.size());
        return bundles;
    }

    @Override
    public List<Bundle> getBundlesByCutOrderId(Long cutOrderId) {
        LambdaQueryWrapper<Bundle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bundle::getCutOrderId, cutOrderId)
               .orderByAsc(Bundle::getBundleNo);
        return list(wrapper);
    }

    @Override
    public Bundle getBundleByQrCode(String qrCode) {
        Long bundleId = parseQrCode(qrCode);
        if (bundleId == null) {
            return null;
        }
        
        LambdaQueryWrapper<Bundle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bundle::getId, bundleId)
               .eq(Bundle::getTenantId, TenantContext.getCurrentTenantId());
        return getOne(wrapper);
    }

    @Override
    public String generateQrCode(Bundle bundle) {
        // 二维码格式：BDL-{tenantId}-{cutOrderId}-{bundleId}-{segmentTag}-{checksum}
        String segmentTag = StrUtil.isNotBlank(bundle.getSegmentTag()) ? bundle.getSegmentTag() : "check";
        String baseCode = String.format("BDL-%d-%d-%d-%s", 
            bundle.getTenantId(), bundle.getCutOrderId(), bundle.getId(), segmentTag);
        
        // 简单的校验和（实际生产中应使用更安全的算法）
        int checksum = baseCode.hashCode() & 0xFFFF;
        return baseCode + "-" + Integer.toHexString(checksum).toUpperCase();
    }

    @Override
    public Long parseQrCode(String qrCode) {
        if (StrUtil.isBlank(qrCode) || !qrCode.startsWith("BDL-")) {
            return null;
        }
        
        String[] parts = qrCode.split("-");
        if (parts.length < 5) {
            return null;
        }
        
        try {
            Long tenantId = Long.parseLong(parts[1]);
            Long bundleId = Long.parseLong(parts[3]);
            
            // 验证租户ID
            if (!tenantId.equals(TenantContext.getCurrentTenantId())) {
                throw new BusinessException("二维码不属于当前租户");
            }
            
            return bundleId;
        } catch (NumberFormatException e) {
            log.warn("解析二维码失败：{}", qrCode);
            return null;
        }
    }

    /**
     * 生成平均裁的包
     */
    private List<Bundle> generateAverageBundles(CutOrder cutOrder, Map<String, Integer> sizeRatio, Integer bundleSize) {
        List<Bundle> bundles = new ArrayList<>();
        
        // 默认每包数量
        if (bundleSize == null) {
            bundleSize = calculateDefaultBundleSize(cutOrder.getTotalQuantity());
        }
        
        int bundleIndex = 1;
        
        // 按尺码生成包
        for (Map.Entry<String, Integer> entry : sizeRatio.entrySet()) {
            String size = entry.getKey();
            Integer ratio = entry.getValue();
            
            // 计算该尺码的总数量
            int sizeQuantity = (int) Math.round(cutOrder.getTotalQuantity() * ratio / 100.0);
            
            // 按包大小分包
            int remainingQuantity = sizeQuantity;
            while (remainingQuantity > 0) {
                int currentBundleSize = Math.min(remainingQuantity, bundleSize);
                
                Bundle bundle = createBundle(cutOrder, bundleIndex++, size, currentBundleSize);
                bundles.add(bundle);
                remainingQuantity -= currentBundleSize;
            }
        }
        
        return bundles;
    }

    /**
     * 生成分层段裁的包
     */
    private List<Bundle> generateSegmentBundles(CutOrder cutOrder, Integer bundleSize) {
        List<Bundle> bundles = new ArrayList<>();
        
        // 解析分层段方案
        CreateCutOrderRequest.SegmentPlan segmentPlan = JSON.parseObject(cutOrder.getSegmentPlan(), 
            CreateCutOrderRequest.SegmentPlan.class);
        
        // 默认每包数量
        if (bundleSize == null) {
            bundleSize = calculateDefaultBundleSize(cutOrder.getTotalQuantity());
        }
        
        int bundleIndex = 1;
        
        // 按分层段生成包
        for (CreateCutOrderRequest.SegmentPlan.Segment segment : segmentPlan.getSegments()) {
            int layerCount = segment.getLayerTo() - segment.getLayerFrom() + 1;
            
            // 按尺码生成包
            for (Map.Entry<String, Integer> entry : segment.getSizeRatio().entrySet()) {
                String size = entry.getKey();
                Integer ratio = entry.getValue();
                
                // 计算该尺码在该层段的数量
                int sizeQuantity = (int) Math.round(layerCount * ratio / 100.0);
                
                // 按包大小分包
                int remainingQuantity = sizeQuantity;
                while (remainingQuantity > 0) {
                    int currentBundleSize = Math.min(remainingQuantity, bundleSize);
                    
                    Bundle bundle = createBundle(cutOrder, bundleIndex++, size, currentBundleSize);
                    bundle.setLayerFrom(segment.getLayerFrom());
                    bundle.setLayerTo(segment.getLayerTo());
                    bundle.setSegmentTag(segment.getSegmentTag());
                    
                    bundles.add(bundle);
                    remainingQuantity -= currentBundleSize;
                }
            }
        }
        
        return bundles;
    }

    /**
     * 创建包对象
     */
    private Bundle createBundle(CutOrder cutOrder, int bundleIndex, String size, int quantity) {
        Bundle bundle = new Bundle();
        bundle.setId(IdUtil.getSnowflakeNextId());
        bundle.setTenantId(cutOrder.getTenantId());
        bundle.setCutOrderId(cutOrder.getId());
        bundle.setBundleNo(generateBundleNo(cutOrder.getOrderNo(), bundleIndex));
        bundle.setSize(size);
        bundle.setColor(cutOrder.getColor());
        bundle.setQuantity(quantity);
        bundle.setStatus("pending");
        bundle.setCreatedBy(1L); // TODO: 从当前用户上下文获取
        
        return bundle;
    }

    /**
     * 生成包号
     */
    private String generateBundleNo(String orderNo, int index) {
        return orderNo.replace("CUT", "BDL") + String.format("%03d", index);
    }

    /**
     * 计算默认包大小
     */
    private int calculateDefaultBundleSize(Integer totalQuantity) {
        // 根据总数量动态计算包大小
        if (totalQuantity <= 500) {
            return 50;
        } else if (totalQuantity <= 2000) {
            return 100;
        } else {
            return 200;
        }
    }
}