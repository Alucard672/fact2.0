package com.garment.payroll.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.garment.common.context.TenantContext;
import com.garment.payroll.entity.ProcessPrice;
import com.garment.payroll.mapper.ProcessPriceMapper;
import com.garment.payroll.service.ProcessPriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 工序单价服务实现
 *
 * @author system
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessPriceServiceImpl extends ServiceImpl<ProcessPriceMapper, ProcessPrice> 
        implements ProcessPriceService {

    @Override
    public ProcessPrice getActivePrice(Long styleId, Long processId) {
        LambdaQueryWrapper<ProcessPrice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessPrice::getTenantId, TenantContext.getCurrentTenantId())
               .eq(ProcessPrice::getStyleId, styleId)
               .eq(ProcessPrice::getProcessId, processId)
               .eq(ProcessPrice::getStatus, "active")
               .le(ProcessPrice::getEffectiveDate, LocalDate.now())
               .and(w -> w.isNull(ProcessPrice::getExpiryDate)
                       .or()
                       .ge(ProcessPrice::getExpiryDate, LocalDate.now()))
               .orderByDesc(ProcessPrice::getEffectiveDate)
               .last("LIMIT 1");
        
        return getOne(wrapper);
    }

    @Override
    public boolean createOrUpdatePrice(ProcessPrice processPrice) {
        // 设置租户ID
        processPrice.setTenantId(TenantContext.getCurrentTenantId());
        
        // 计算最终单价
        processPrice.setFinalPrice(
            processPrice.getBasePrice()
                .multiply(processPrice.getDifficultyFactor())
                .multiply(processPrice.getQualityFactor())
        );
        
        // 如果没有设置生效日期，默认为当前日期
        if (processPrice.getEffectiveDate() == null) {
            processPrice.setEffectiveDate(LocalDate.now());
        }
        
        return saveOrUpdate(processPrice);
    }
}




