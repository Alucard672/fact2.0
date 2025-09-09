package com.garment.basic.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.garment.basic.entity.Employee;
import com.garment.basic.dto.EmployeeDTO;
import com.garment.basic.dto.EmployeeQueryDTO;

/**
 * 员工服务接口
 *
 * @author garment
 */
public interface EmployeeService extends IService<Employee> {

    /**
     * 分页查询员工列表
     *
     * @param queryDTO 查询参数
     * @return 员工分页列表
     */
    IPage<Employee> getEmployeePage(EmployeeQueryDTO queryDTO);

    /**
     * 根据ID获取员工详情
     *
     * @param id 员工ID
     * @return 员工详情
     */
    EmployeeDTO getEmployeeById(Long id);

    /**
     * 创建员工
     *
     * @param employeeDTO 员工信息
     * @return 员工详情
     */
    EmployeeDTO createEmployee(EmployeeDTO employeeDTO);

    /**
     * 更新员工
     *
     * @param id          员工ID
     * @param employeeDTO 员工信息
     * @return 员工详情
     */
    EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO);

    /**
     * 删除员工
     *
     * @param id 员工ID
     * @return 是否删除成功
     */
    boolean deleteEmployee(Long id);

    /**
     * 检查员工工号是否已存在
     *
     * @param tenantId   租户ID
     * @param employeeNo 员工工号
     * @param excludeId  排除的ID（更新时使用）
     * @return 是否存在
     */
    boolean checkEmployeeNoExists(Long tenantId, String employeeNo, Long excludeId);
}