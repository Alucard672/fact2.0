package com.garment.basic.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.garment.basic.dto.ProcessDTO;
import com.garment.basic.dto.ProcessQueryDTO;
import com.garment.basic.entity.Process;
import com.garment.basic.service.ProcessService;
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
 * 工序管理控制器
 *
 * @author garment
 */
@Slf4j
@RestController
@RequestMapping("/api/processes")
@Api(tags = "工序管理")
@RequiredArgsConstructor
@Validated
public class ProcessController {

    private final ProcessService processService;

    /**
     * 分页查询工序列表
     */
    @GetMapping
    @ApiOperation("分页查询工序列表")
    public ApiResponse<IPage<Process>> getProcessPage(@Valid ProcessQueryDTO queryDTO) {
        try {
            IPage<Process> page = processService.getProcessPage(queryDTO);
            return ApiResponse.success(page);
        } catch (Exception e) {
            log.error("分页查询工序列表失败: {}", e.getMessage());
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取工序详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID获取工序详情")
    public ApiResponse<ProcessDTO> getProcessById(
            @ApiParam("工序ID") @PathVariable Long id) {
        try {
            ProcessDTO processDTO = processService.getProcessById(id);
            return ApiResponse.success(processDTO);
        } catch (Exception e) {
            log.error("获取工序详情失败: {}", e.getMessage());
            return ApiResponse.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 创建工序
     */
    @PostMapping
    @ApiOperation("创建工序")
    @RequireRole({"owner", "admin", "manager"})
    public ApiResponse<ProcessDTO> createProcess(@RequestBody @Valid ProcessDTO processDTO) {
        try {
            ProcessDTO result = processService.createProcess(processDTO);
            return ApiResponse.success("创建成功", result);
        } catch (Exception e) {
            log.error("创建工序失败: {}", e.getMessage());
            return ApiResponse.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新工序
     */
    @PutMapping("/{id}")
    @ApiOperation("更新工序")
    @RequireRole({"owner", "admin", "manager"})
    public ApiResponse<ProcessDTO> updateProcess(
            @ApiParam("工序ID") @PathVariable Long id,
            @RequestBody @Valid ProcessDTO processDTO) {
        try {
            ProcessDTO result = processService.updateProcess(id, processDTO);
            return ApiResponse.success("更新成功", result);
        } catch (Exception e) {
            log.error("更新工序失败: {}", e.getMessage());
            return ApiResponse.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除工序
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除工序")
    @RequireRole({"owner", "admin"})
    public ApiResponse<Void> deleteProcess(
            @ApiParam("工序ID") @PathVariable Long id) {
        try {
            boolean result = processService.deleteProcess(id);
            if (result) {
                return ApiResponse.success("删除成功");
            } else {
                return ApiResponse.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除工序失败: {}", e.getMessage());
            return ApiResponse.error("删除失败: " + e.getMessage());
        }
    }
}