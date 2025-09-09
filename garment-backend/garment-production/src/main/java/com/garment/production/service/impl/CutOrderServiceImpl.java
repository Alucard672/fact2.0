package com.garment.production.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.garment.common.context.TenantContext;
import com.garment.common.exception.BusinessException;
import com.garment.production.dto.CreateCutOrderRequest;
import com.garment.production.entity.Bundle;
import com.garment.production.entity.CutOrder;
import com.garment.production.mapper.CutOrderMapper;
import com.garment.production.service.BundleService;
import com.garment.production.service.CutOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 裁床订单服务实现
 *
 * @author system
 */
@Slf4j
@Service
public class CutOrderServiceImpl extends ServiceImpl<CutOrderMapper, CutOrder> implements CutOrderService {

    private final BundleService bundleService;

    public CutOrderServiceImpl(@Lazy BundleService bundleService) {
        this.bundleService = bundleService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CutOrder createCutOrder(CreateCutOrderRequest request) {
        // 验证数据
        validateCreateRequest(request);
        
        // 创建裁床订单
        CutOrder cutOrder = new CutOrder();
        cutOrder.setTenantId(TenantContext.getCurrentTenantId());
        cutOrder.setOrderNo(generateOrderNo());
        cutOrder.setStyleId(request.getStyleId());
        // TODO: 从基础资料服务获取款式信息
        cutOrder.setStyleCode("ST001"); // 临时数据
        cutOrder.setStyleName("经典T恤"); // 临时数据
        cutOrder.setColor(request.getColor());
        cutOrder.setBedNo(request.getBedNo());
        cutOrder.setTotalLayers(request.getTotalLayers());
        cutOrder.setCuttingType(request.getCuttingType());
        cutOrder.setSizeRatio(JSON.toJSONString(request.getSizeRatio()));
        
        // 如果是分层段裁，设置分层段方案
        if ("segment".equals(request.getCuttingType()) && request.getSegmentPlan() != null) {
            cutOrder.setSegmentPlan(JSON.toJSONString(request.getSegmentPlan()));
        }
        
        // 计算总数量
        int totalQuantity = calculateTotalQuantity(request.getSizeRatio(), request.getTotalLayers());
        cutOrder.setTotalQuantity(totalQuantity);
        
        cutOrder.setCuttingDate(request.getCuttingDate());
        cutOrder.setDeliveryDate(request.getDeliveryDate());
        cutOrder.setPriority(request.getPriority());
        cutOrder.setStatus("draft");
        cutOrder.setRemark(request.getRemark());
        // TODO: 从当前用户上下文获取
        cutOrder.setCreatedBy(1L);
        
        // 保存到数据库
        save(cutOrder);
        
        log.info("创建裁床订单成功，订单号：{}", cutOrder.getOrderNo());
        return cutOrder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmCutOrder(Long id) {
        CutOrder cutOrder = getById(id);
        if (cutOrder == null) {
            throw new BusinessException("裁床订单不存在");
        }
        
        if (!"draft".equals(cutOrder.getStatus())) {
            throw new BusinessException("只有草稿状态的订单才能确认");
        }
        
        cutOrder.setStatus("confirmed");
        cutOrder.setUpdatedBy(1L); // TODO: 从当前用户上下文获取
        cutOrder.setUpdatedAt(LocalDateTime.now());
        
        boolean result = updateById(cutOrder);
        if (result) {
            log.info("确认裁床订单成功，订单号：{}", cutOrder.getOrderNo());
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelCutOrder(Long id) {
        CutOrder cutOrder = getById(id);
        if (cutOrder == null) {
            throw new BusinessException("裁床订单不存在");
        }
        
        if ("completed".equals(cutOrder.getStatus()) || "cancelled".equals(cutOrder.getStatus())) {
            throw new BusinessException("已完成或已取消的订单不能取消");
        }
        
        cutOrder.setStatus("cancelled");
        cutOrder.setUpdatedBy(1L); // TODO: 从当前用户上下文获取
        cutOrder.setUpdatedAt(LocalDateTime.now());
        
        boolean result = updateById(cutOrder);
        if (result) {
            log.info("取消裁床订单成功，订单号：{}", cutOrder.getOrderNo());
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean restoreCutOrder(Long id, boolean removeBundles) {
        CutOrder cutOrder = getById(id);
        if (cutOrder == null) {
            throw new BusinessException("裁床订单不存在");
        }
        if (!"confirmed".equals(cutOrder.getStatus()) && !"cutting".equals(cutOrder.getStatus())) {
            throw new BusinessException("只有已确认或裁剪中的订单才能还原");
        }

        if (removeBundles) {
            LambdaQueryWrapper<Bundle> bw = new LambdaQueryWrapper<>();
            bw.eq(Bundle::getCutOrderId, id)
              .eq(Bundle::getTenantId, TenantContext.getCurrentTenantId());
            bundleService.remove(bw);
        }

        cutOrder.setStatus("draft");
        if (removeBundles) {
            cutOrder.setBundleCount(0);
        }
        cutOrder.setUpdatedBy(1L);
        cutOrder.setUpdatedAt(LocalDateTime.now());

        boolean ok = updateById(cutOrder);
        if (ok) {
            log.info("还原裁床订单成功，订单号：{}，removeBundles={}", cutOrder.getOrderNo(), removeBundles);
        }
        return ok;
    }

    @Override
    public Page<CutOrder> pageCutOrders(Page<CutOrder> page, String orderNo, Long styleId, String status, String priority) {
        LambdaQueryWrapper<CutOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CutOrder::getTenantId, TenantContext.getCurrentTenantId())
               .like(StrUtil.isNotBlank(orderNo), CutOrder::getOrderNo, orderNo)
               .eq(styleId != null, CutOrder::getStyleId, styleId)
               .eq(StrUtil.isNotBlank(status), CutOrder::getStatus, status)
               .eq(StrUtil.isNotBlank(priority), CutOrder::getPriority, priority)
               .orderByDesc(CutOrder::getCreatedAt);
        
        return page(page, wrapper);
    }

    @Override
    public String generateOrderNo() {
        String dateStr = DateUtil.format(LocalDateTime.now(), "yyyyMMdd");
        String prefix = "CUT" + dateStr;
        
        // 查询当天最大序号
        LambdaQueryWrapper<CutOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CutOrder::getTenantId, TenantContext.getCurrentTenantId())
               .likeRight(CutOrder::getOrderNo, prefix)
               .orderByDesc(CutOrder::getOrderNo)
               .last("LIMIT 1");
        
        CutOrder lastOrder = getOne(wrapper);
        int sequence = 1;
        
        if (lastOrder != null && lastOrder.getOrderNo().startsWith(prefix)) {
            String lastSequence = lastOrder.getOrderNo().substring(prefix.length());
            try {
                sequence = Integer.parseInt(lastSequence) + 1;
            } catch (NumberFormatException e) {
                log.warn("解析订单号序列失败：{}", lastOrder.getOrderNo());
            }
        }
        
        return prefix + String.format("%03d", sequence);
    }

    /**
     * 验证创建请求
     */
    private void validateCreateRequest(CreateCutOrderRequest request) {
        // 验证床次号唯一性
        LambdaQueryWrapper<CutOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CutOrder::getTenantId, TenantContext.getCurrentTenantId())
               .eq(CutOrder::getBedNo, request.getBedNo())
               .ne(CutOrder::getStatus, "cancelled");
        
        if (count(wrapper) > 0) {
            throw new BusinessException("床次号已存在");
        }
        
        // 验证尺码比例总和为100
        int totalRatio = request.getSizeRatio().values().stream().mapToInt(Integer::intValue).sum();
        if (totalRatio != 100) {
            throw new BusinessException("尺码比例总和必须为100%");
        }
        
        // 如果是分层段裁，校验方案
        if ("segment".equals(request.getCuttingType())) {
            validateSegmentPlan(request.getSegmentPlan(), request.getTotalLayers());
        }
    }

    private void validateSegmentPlan(CreateCutOrderRequest.SegmentPlan segmentPlan, Integer totalLayers) {
        if (segmentPlan == null || segmentPlan.getSegments() == null || segmentPlan.getSegments().isEmpty()) {
            throw new BusinessException("分层段方案不能为空");
        }
        
        int sumLayers = 0;
        for (CreateCutOrderRequest.SegmentPlan.Segment segment : segmentPlan.getSegments()) {
            if (segment.getLayerFrom() <= 0 || segment.getLayerTo() <= 0 || segment.getLayerTo() < segment.getLayerFrom()) {
                throw new BusinessException("层段定义不合法");
            }
            sumLayers += (segment.getLayerTo() - segment.getLayerFrom() + 1);
        }
        if (!sumLayersEqualsTotal(sumLayers, totalLayers)) {
            throw new BusinessException("层段总层数与总层数不一致");
        }
    }

    private boolean sumLayersEqualsTotal(int sumLayers, Integer totalLayers) {
        return totalLayers != null && sumLayers == totalLayers;
    }

    private int calculateTotalQuantity(Map<String, Integer> sizeRatio, Integer totalLayers) {
        int perLayer = sizeRatio.values().stream().mapToInt(Integer::intValue).sum();
        return perLayer * (totalLayers != null ? totalLayers : 1);
    }
}




