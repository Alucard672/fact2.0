package com.garment.basic.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.garment.basic.dto.EmployeeDTO;
import com.garment.basic.dto.EmployeeQueryDTO;
import com.garment.basic.entity.Employee;
import com.garment.basic.service.EmployeeService;
import com.garment.common.annotation.RequireRole;
import com.garment.common.vo.ApiResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 员工管理控制器
 *
 * @author garment
 */
@Slf4j
@RestController
@RequestMapping("/api/employees")
@Api(tags = "员工管理")
@RequiredArgsConstructor
@Validated
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * 分页查询员工列表
     */
    @GetMapping
    @ApiOperation("分页查询员工列表")
    public ApiResponse<IPage<Employee>> getEmployeePage(@Valid EmployeeQueryDTO queryDTO) {
        try {
            IPage<Employee> page = employeeService.getEmployeePage(queryDTO);
            return ApiResponse.success(page);
        } catch (Exception e) {
            log.error("分页查询员工列表失败: {}", e.getMessage());
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取员工详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID获取员工详情")
    public ApiResponse<EmployeeDTO> getEmployeeById(
            @ApiParam("员工ID") @PathVariable Long id) {
        try {
            EmployeeDTO employeeDTO = employeeService.getEmployeeById(id);
            return ApiResponse.success(employeeDTO);
        } catch (Exception e) {
            log.error("获取员工详情失败: {}", e.getMessage());
            return ApiResponse.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 创建员工
     */
    @PostMapping
    @ApiOperation("创建员工")
    @RequireRole({"owner", "admin", "manager"})
    public ApiResponse<EmployeeDTO> createEmployee(@RequestBody @Valid EmployeeDTO employeeDTO) {
        try {
            EmployeeDTO result = employeeService.createEmployee(employeeDTO);
            return ApiResponse.success("创建成功", result);
        } catch (Exception e) {
            log.error("创建员工失败: {}", e.getMessage());
            return ApiResponse.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新员工
     */
    @PutMapping("/{id}")
    @ApiOperation("更新员工")
    @RequireRole({"owner", "admin", "manager"})
    public ApiResponse<EmployeeDTO> updateEmployee(
            @ApiParam("员工ID") @PathVariable Long id,
            @RequestBody @Valid EmployeeDTO employeeDTO) {
        try {
            EmployeeDTO result = employeeService.updateEmployee(id, employeeDTO);
            return ApiResponse.success("更新成功", result);
        } catch (Exception e) {
            log.error("更新员工失败: {}", e.getMessage());
            return ApiResponse.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除员工
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除员工")
    @RequireRole({"owner", "admin"})
    public ApiResponse<Void> deleteEmployee(
            @ApiParam("员工ID") @PathVariable Long id) {
        try {
            boolean result = employeeService.deleteEmployee(id);
            if (result) {
                return ApiResponse.success("删除成功");
            } else {
                return ApiResponse.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除员工失败: {}", e.getMessage());
            return ApiResponse.error("删除失败: " + e.getMessage());
        }
    }
}