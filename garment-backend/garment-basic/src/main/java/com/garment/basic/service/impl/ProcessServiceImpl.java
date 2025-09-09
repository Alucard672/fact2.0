package com.garment.basic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.garment.basic.entity.Process;
import com.garment.basic.mapper.ProcessMapper;
import com.garment.basic.service.ProcessService;
import com.garment.basic.dto.ProcessDTO;
import com.garment.basic.dto.ProcessQueryDTO;
import com.garment.common.context.TenantContext;
import com.garment.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 工序服务实现类
 *
 * @author garment
 */
@Service
@RequiredArgsConstructor
public class ProcessServiceImpl extends ServiceImpl<ProcessMapper, Process> implements ProcessService {

    private final ProcessMapper processMapper;

    @Override
    public IPage<Process> getProcessPage(ProcessQueryDTO queryDTO) {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        Page<Process> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        return processMapper.selectProcessPage(page, tenantId, queryDTO.getKeyword(), 
                                              queryDTO.getCategory(), queryDTO.getStatus());
    }

    @Override
    public ProcessDTO getProcessById(Long id) {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        Process process = this.getOne(new QueryWrapper<Process>()
                .eq("id", id)
                .eq("tenant_id", tenantId)
                .eq("deleted", 0));

        if (process == null) {
            throw new BusinessException("工序不存在");
        }

        ProcessDTO processDTO = new ProcessDTO();
        BeanUtils.copyProperties(process, processDTO);
        return processDTO;
    }

    @Override
    public ProcessDTO createProcess(ProcessDTO processDTO) {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        // 检查工序编码是否已存在
        if (checkProcessCodeExists(tenantId, processDTO.getProcessCode(), null)) {
            throw new BusinessException("工序编码已存在");
        }

        Process process = new Process();
        BeanUtils.copyProperties(processDTO, process);
        process.setTenantId(tenantId);
        process.setCreatedAt(LocalDateTime.now());
        process.setUpdatedAt(LocalDateTime.now());
        process.setDeleted(false);

        this.save(process);

        ProcessDTO result = new ProcessDTO();
        BeanUtils.copyProperties(process, result);
        return result;
    }

    @Override
    public ProcessDTO updateProcess(Long id, ProcessDTO processDTO) {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        // 检查工序是否存在
        Process existingProcess = this.getOne(new QueryWrapper<Process>()
                .eq("id", id)
                .eq("tenant_id", tenantId)
                .eq("deleted", 0));

        if (existingProcess == null) {
            throw new BusinessException("工序不存在");
        }

        // 检查工序编码是否已存在
        if (checkProcessCodeExists(tenantId, processDTO.getProcessCode(), id)) {
            throw new BusinessException("工序编码已存在");
        }

        Process process = new Process();
        BeanUtils.copyProperties(processDTO, process);
        process.setId(id);
        process.setTenantId(tenantId);
        process.setUpdatedAt(LocalDateTime.now());

        this.updateById(process);

        ProcessDTO result = new ProcessDTO();
        BeanUtils.copyProperties(process, result);
        return result;
    }

    @Override
    public boolean deleteProcess(Long id) {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        // 检查工序是否存在
        Process existingProcess = this.getOne(new QueryWrapper<Process>()
                .eq("id", id)
                .eq("tenant_id", tenantId)
                .eq("deleted", 0));

        if (existingProcess == null) {
            throw new BusinessException("工序不存在");
        }

        // 检查是否有工价模板引用此工序
        // 这里简化处理，实际项目中需要检查关联数据
        // ...

        // 逻辑删除
        existingProcess.setDeleted(true);
        return this.updateById(existingProcess);
    }

    @Override
    public boolean checkProcessCodeExists(Long tenantId, String processCode, Long excludeId) {
        QueryWrapper<Process> queryWrapper = new QueryWrapper<Process>()
                .eq("tenant_id", tenantId)
                .eq("process_code", processCode)
                .eq("deleted", 0);

        if (excludeId != null) {
            queryWrapper.ne("id", excludeId);
        }

        return this.count(queryWrapper) > 0;
    }
}