package com.garment.basic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.garment.basic.entity.Employee;
import com.garment.basic.mapper.EmployeeMapper;
import com.garment.basic.service.EmployeeService;
import com.garment.basic.dto.EmployeeDTO;
import com.garment.basic.dto.EmployeeQueryDTO;
import com.garment.common.context.TenantContext;
import com.garment.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 员工服务实现类
 *
 * @author garment
 */
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    private final EmployeeMapper employeeMapper;

    @Override
    public IPage<Employee> getEmployeePage(EmployeeQueryDTO queryDTO) {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        Page<Employee> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        return employeeMapper.selectEmployeePage(page, tenantId, queryDTO.getKeyword(), 
                                                queryDTO.getRole(), queryDTO.getStatus());
    }

    @Override
    public EmployeeDTO getEmployeeById(Long id) {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        Employee employee = this.getOne(new QueryWrapper<Employee>()
                .eq("id", id)
                .eq("tenant_id", tenantId)
                .eq("deleted", 0));

        if (employee == null) {
            throw new BusinessException("员工不存在");
        }

        EmployeeDTO employeeDTO = new EmployeeDTO();
        BeanUtils.copyProperties(employee, employeeDTO);
        return employeeDTO;
    }

    @Override
    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO) {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        // 检查员工工号是否已存在
        if (checkEmployeeNoExists(tenantId, employeeDTO.getEmployeeNo(), null)) {
            throw new BusinessException("员工工号已存在");
        }

        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        employee.setTenantId(tenantId);
        employee.setCreatedAt(LocalDateTime.now());
        employee.setUpdatedAt(LocalDateTime.now());
        employee.setDeleted(false);

        this.save(employee);

        EmployeeDTO result = new EmployeeDTO();
        BeanUtils.copyProperties(employee, result);
        return result;
    }

    @Override
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO) {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        // 检查员工是否存在
        Employee existingEmployee = this.getOne(new QueryWrapper<Employee>()
                .eq("id", id)
                .eq("tenant_id", tenantId)
                .eq("deleted", 0));

        if (existingEmployee == null) {
            throw new BusinessException("员工不存在");
        }

        // 检查员工工号是否已存在
        if (checkEmployeeNoExists(tenantId, employeeDTO.getEmployeeNo(), id)) {
            throw new BusinessException("员工工号已存在");
        }

        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        employee.setId(id);
        employee.setTenantId(tenantId);
        employee.setUpdatedAt(LocalDateTime.now());

        this.updateById(employee);

        EmployeeDTO result = new EmployeeDTO();
        BeanUtils.copyProperties(employee, result);
        return result;
    }

    @Override
    public boolean deleteEmployee(Long id) {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        // 检查员工是否存在
        Employee existingEmployee = this.getOne(new QueryWrapper<Employee>()
                .eq("id", id)
                .eq("tenant_id", tenantId)
                .eq("deleted", 0));

        if (existingEmployee == null) {
            throw new BusinessException("员工不存在");
        }

        // 逻辑删除
        existingEmployee.setDeleted(true);
        return this.updateById(existingEmployee);
    }

    @Override
    public boolean checkEmployeeNoExists(Long tenantId, String employeeNo, Long excludeId) {
        QueryWrapper<Employee> queryWrapper = new QueryWrapper<Employee>()
                .eq("tenant_id", tenantId)
                .eq("employee_no", employeeNo)
                .eq("deleted", 0);

        if (excludeId != null) {
            queryWrapper.ne("id", excludeId);
        }

        return this.count(queryWrapper) > 0;
    }
}