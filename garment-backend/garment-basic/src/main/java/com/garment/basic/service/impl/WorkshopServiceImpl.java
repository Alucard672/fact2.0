package com.garment.basic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.garment.basic.entity.Workshop;
import com.garment.basic.mapper.WorkshopMapper;
import com.garment.basic.service.WorkshopService;
import com.garment.basic.dto.WorkshopDTO;
import com.garment.basic.dto.WorkshopQueryDTO;
import com.garment.common.context.TenantContext;
import com.garment.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 车间服务实现类
 *
 * @author garment
 */
@Service
@RequiredArgsConstructor
public class WorkshopServiceImpl extends ServiceImpl<WorkshopMapper, Workshop> implements WorkshopService {

    private final WorkshopMapper workshopMapper;

    @Override
    public IPage<Workshop> getWorkshopPage(WorkshopQueryDTO queryDTO) {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        Page<Workshop> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        return workshopMapper.selectWorkshopPage(page, tenantId, queryDTO.getKeyword(), queryDTO.getStatus());
    }

    @Override
    public WorkshopDTO getWorkshopById(Long id) {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        Workshop workshop = this.getOne(new QueryWrapper<Workshop>()
                .eq("id", id)
                .eq("tenant_id", tenantId)
                .eq("deleted", 0));

        if (workshop == null) {
            throw new BusinessException("车间不存在");
        }

        WorkshopDTO workshopDTO = new WorkshopDTO();
        BeanUtils.copyProperties(workshop, workshopDTO);
        return workshopDTO;
    }

    @Override
    public WorkshopDTO createWorkshop(WorkshopDTO workshopDTO) {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        // 检查车间编码是否已存在
        if (checkWorkshopCodeExists(tenantId, workshopDTO.getWorkshopCode(), null)) {
            throw new BusinessException("车间编码已存在");
        }

        Workshop workshop = new Workshop();
        BeanUtils.copyProperties(workshopDTO, workshop);
        workshop.setTenantId(tenantId);
        workshop.setCreatedAt(LocalDateTime.now());
        workshop.setUpdatedAt(LocalDateTime.now());
        workshop.setDeleted(false);

        this.save(workshop);

        WorkshopDTO result = new WorkshopDTO();
        BeanUtils.copyProperties(workshop, result);
        return result;
    }

    @Override
    public WorkshopDTO updateWorkshop(Long id, WorkshopDTO workshopDTO) {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        // 检查车间是否存在
        Workshop existingWorkshop = this.getOne(new QueryWrapper<Workshop>()
                .eq("id", id)
                .eq("tenant_id", tenantId)
                .eq("deleted", 0));

        if (existingWorkshop == null) {
            throw new BusinessException("车间不存在");
        }

        // 检查车间编码是否已存在
        if (checkWorkshopCodeExists(tenantId, workshopDTO.getWorkshopCode(), id)) {
            throw new BusinessException("车间编码已存在");
        }

        Workshop workshop = new Workshop();
        BeanUtils.copyProperties(workshopDTO, workshop);
        workshop.setId(id);
        workshop.setTenantId(tenantId);
        workshop.setUpdatedAt(LocalDateTime.now());

        this.updateById(workshop);

        WorkshopDTO result = new WorkshopDTO();
        BeanUtils.copyProperties(workshop, result);
        return result;
    }

    @Override
    public boolean deleteWorkshop(Long id) {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        // 检查车间是否存在
        Workshop existingWorkshop = this.getOne(new QueryWrapper<Workshop>()
                .eq("id", id)
                .eq("tenant_id", tenantId)
                .eq("deleted", 0));

        if (existingWorkshop == null) {
            throw new BusinessException("车间不存在");
        }

        // 检查是否有员工关联此车间
        // 这里简化处理，实际项目中需要检查关联数据
        // ...

        // 逻辑删除
        existingWorkshop.setDeleted(true);
        return this.updateById(existingWorkshop);
    }

    @Override
    public boolean checkWorkshopCodeExists(Long tenantId, String workshopCode, Long excludeId) {
        QueryWrapper<Workshop> queryWrapper = new QueryWrapper<Workshop>()
                .eq("tenant_id", tenantId)
                .eq("workshop_code", workshopCode)
                .eq("deleted", 0);

        if (excludeId != null) {
            queryWrapper.ne("id", excludeId);
        }

        return this.count(queryWrapper) > 0;
    }
}