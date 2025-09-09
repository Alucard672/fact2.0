package com.garment.basic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.garment.basic.entity.ProcessPrice;
import com.garment.basic.mapper.ProcessPriceMapper;
import com.garment.basic.service.ProcessPriceService;
import com.garment.basic.dto.ProcessPriceDTO;
import com.garment.basic.dto.ProcessPriceQueryDTO;
import com.garment.common.context.TenantContext;
import com.garment.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 工价模板服务实现类
 *
 * @author garment
 */
@Service
@RequiredArgsConstructor
public class ProcessPriceServiceImpl extends ServiceImpl<ProcessPriceMapper, ProcessPrice> implements ProcessPriceService {

    private final ProcessPriceMapper processPriceMapper;

    @Override
    public IPage<ProcessPrice> getProcessPricePage(ProcessPriceQueryDTO queryDTO) {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        Page<ProcessPrice> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        return processPriceMapper.selectProcessPricePage(page, tenantId, queryDTO.getKeyword(), queryDTO.getStatus());
    }

    @Override
    public ProcessPriceDTO getProcessPriceById(Long id) {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        ProcessPrice processPrice = this.getOne(new QueryWrapper<ProcessPrice>()
                .eq("id", id)
                .eq("tenant_id", tenantId)
                .eq("deleted", 0));

        if (processPrice == null) {
            throw new BusinessException("工价模板不存在");
        }

        ProcessPriceDTO processPriceDTO = new ProcessPriceDTO();
        BeanUtils.copyProperties(processPrice, processPriceDTO);
        return processPriceDTO;
    }

    @Override
    public ProcessPriceDTO createProcessPrice(ProcessPriceDTO processPriceDTO) {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        ProcessPrice processPrice = new ProcessPrice();
        BeanUtils.copyProperties(processPriceDTO, processPrice);
        processPrice.setTenantId(tenantId);
        processPrice.setCreatedAt(LocalDateTime.now());
        processPrice.setUpdatedAt(LocalDateTime.now());
        processPrice.setDeleted(false);
        processPrice.setVersion(1);
        processPrice.setCreatedBy(TenantContext.getCurrentUserId());

        this.save(processPrice);

        ProcessPriceDTO result = new ProcessPriceDTO();
        BeanUtils.copyProperties(processPrice, result);
        return result;
    }

    @Override
    public ProcessPriceDTO updateProcessPrice(Long id, ProcessPriceDTO processPriceDTO) {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        // 检查工价模板是否存在
        ProcessPrice existingProcessPrice = this.getOne(new QueryWrapper<ProcessPrice>()
                .eq("id", id)
                .eq("tenant_id", tenantId)
                .eq("deleted", 0));

        if (existingProcessPrice == null) {
            throw new BusinessException("工价模板不存在");
        }

        ProcessPrice processPrice = new ProcessPrice();
        BeanUtils.copyProperties(processPriceDTO, processPrice);
        processPrice.setId(id);
        processPrice.setTenantId(tenantId);
        processPrice.setUpdatedAt(LocalDateTime.now());
        processPrice.setVersion(existingProcessPrice.getVersion() + 1);

        this.updateById(processPrice);

        ProcessPriceDTO result = new ProcessPriceDTO();
        BeanUtils.copyProperties(processPrice, result);
        return result;
    }

    @Override
    public boolean deleteProcessPrice(Long id) {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        // 检查工价模板是否存在
        ProcessPrice existingProcessPrice = this.getOne(new QueryWrapper<ProcessPrice>()
                .eq("id", id)
                .eq("tenant_id", tenantId)
                .eq("deleted", 0));

        if (existingProcessPrice == null) {
            throw new BusinessException("工价模板不存在");
        }

        // 逻辑删除
        existingProcessPrice.setDeleted(true);
        return this.updateById(existingProcessPrice);
    }

    @Override
    public ProcessPriceDTO getPriceByCondition(Long tenantId, Long styleId, Long processId, Long workshopId) {
        QueryWrapper<ProcessPrice> queryWrapper = new QueryWrapper<ProcessPrice>()
                .eq("tenant_id", tenantId)
                .eq("process_id", processId)
                .eq("deleted", 0)
                .orderByDesc("version");

        // 优先匹配款式+车间
        if (styleId != null && workshopId != null) {
            queryWrapper.eq("style_id", styleId).eq("workshop_id", workshopId);
            ProcessPrice price = this.getOne(queryWrapper);
            if (price != null) {
                ProcessPriceDTO result = new ProcessPriceDTO();
                BeanUtils.copyProperties(price, result);
                return result;
            }
        }

        // 匹配款式
        if (styleId != null) {
            queryWrapper.clear();
            queryWrapper.eq("tenant_id", tenantId)
                    .eq("process_id", processId)
                    .eq("style_id", styleId)
                    .isNull("workshop_id")
                    .eq("deleted", 0)
                    .orderByDesc("version");
            ProcessPrice price = this.getOne(queryWrapper);
            if (price != null) {
                ProcessPriceDTO result = new ProcessPriceDTO();
                BeanUtils.copyProperties(price, result);
                return result;
            }
        }

        // 匹配车间
        if (workshopId != null) {
            queryWrapper.clear();
            queryWrapper.eq("tenant_id", tenantId)
                    .eq("process_id", processId)
                    .eq("workshop_id", workshopId)
                    .isNull("style_id")
                    .eq("deleted", 0)
                    .orderByDesc("version");
            ProcessPrice price = this.getOne(queryWrapper);
            if (price != null) {
                ProcessPriceDTO result = new ProcessPriceDTO();
                BeanUtils.copyProperties(price, result);
                return result;
            }
        }

        // 通用工价
        queryWrapper.clear();
        queryWrapper.eq("tenant_id", tenantId)
                .eq("process_id", processId)
                .isNull("style_id")
                .isNull("workshop_id")
                .eq("deleted", 0)
                .orderByDesc("version");
        ProcessPrice price = this.getOne(queryWrapper);
        if (price != null) {
            ProcessPriceDTO result = new ProcessPriceDTO();
            BeanUtils.copyProperties(price, result);
            return result;
        }

        return null;
    }
}