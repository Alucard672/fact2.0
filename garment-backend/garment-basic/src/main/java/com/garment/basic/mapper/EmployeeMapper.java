package com.garment.basic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.garment.basic.entity.Employee;
import org.apache.ibatis.annotations.Param;

/**
 * 员工Mapper接口
 *
 * @author garment
 */
public interface EmployeeMapper extends BaseMapper<Employee> {

    /**
     * 分页查询员工列表
     *
     * @param page      分页对象
     * @param tenantId  租户ID
     * @param keyword   关键词搜索
     * @param role      角色
     * @param status    状态
     * @return 员工分页列表
     */
    IPage<Employee> selectEmployeePage(Page<Employee> page, @Param("tenantId") Long tenantId,
                                      @Param("keyword") String keyword, @Param("role") String role,
                                      @Param("status") String status);
}