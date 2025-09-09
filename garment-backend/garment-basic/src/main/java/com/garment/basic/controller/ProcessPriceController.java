package com.garment.basic.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.garment.basic.dto.ProcessPriceDTO;
import com.garment.basic.dto.ProcessPriceQueryDTO;
import com.garment.basic.entity.ProcessPrice;
import com.garment.basic.service.ProcessPriceService;
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
 * 工价模板管理控制器
 *
 * @author garment
 */
@Slf4j
@RestController
@RequestMapping("/api/process-prices")
@Api(tags = "工价模板管理")
@RequiredArgsConstructor
@Validated
public class ProcessPriceController {

    private final ProcessPriceService processPriceService;

    /**
     * 分页查询工价模板列表
     */
    @GetMapping
    @ApiOperation("分页查询工价模板列表")
    public ApiResponse<IPage<ProcessPrice>> getProcessPricePage(@Valid ProcessPriceQueryDTO queryDTO) {
        try {
            IPage<ProcessPrice> page = processPriceService.getProcessPricePage(queryDTO);
            return ApiResponse.success(page);
        } catch (Exception e) {
            log.error("分页查询工价模板列表失败: {}", e.getMessage());
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取工价模板详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID获取工价模板详情")
    public ApiResponse<ProcessPriceDTO> getProcessPriceById(
            @ApiParam("工价模板ID") @PathVariable Long id) {
        try {
            ProcessPriceDTO processPriceDTO = processPriceService.getProcessPriceById(id);
            return ApiResponse.success(processPriceDTO);
        } catch (Exception e) {
            log.error("获取工价模板详情失败: {}", e.getMessage());
            return ApiResponse.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 创建工价模板
     */
    @PostMapping
    @ApiOperation("创建工价模板")
    @RequireRole({"owner", "admin", "manager"})
    public ApiResponse<ProcessPriceDTO> createProcessPrice(@RequestBody @Valid ProcessPriceDTO processPriceDTO) {
        try {
            ProcessPriceDTO result = processPriceService.createProcessPrice(processPriceDTO);
            return ApiResponse.success("创建成功", result);
        } catch (Exception e) {
            log.error("创建工价模板失败: {}", e.getMessage());
            return ApiResponse.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新工价模板
     */
    @PutMapping("/{id}")
    @ApiOperation("更新工价模板")
    @RequireRole({"owner", "admin", "manager"})
    public ApiResponse<ProcessPriceDTO> updateProcessPrice(
            @ApiParam("工价模板ID") @PathVariable Long id,
            @RequestBody @Valid ProcessPriceDTO processPriceDTO) {
        try {
            ProcessPriceDTO result = processPriceService.updateProcessPrice(id, processPriceDTO);
            return ApiResponse.success("更新成功", result);
        } catch (Exception e) {
            log.error("更新工价模板失败: {}", e.getMessage());
            return ApiResponse.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除工价模板
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除工价模板")
    @RequireRole({"owner", "admin"})
    public ApiResponse<Void> deleteProcessPrice(
            @ApiParam("工价模板ID") @PathVariable Long id) {
        try {
            boolean result = processPriceService.deleteProcessPrice(id);
            if (result) {
                return ApiResponse.success("删除成功");
            } else {
                return ApiResponse.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除工价模板失败: {}", e.getMessage());
            return ApiResponse.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 根据条件获取工价
     */
    @GetMapping("/price")
    @ApiOperation("根据条件获取工价")
    public ApiResponse<ProcessPriceDTO> getPriceByCondition(
            @ApiParam("款式ID") @RequestParam(required = false) Long styleId,
            @ApiParam("工序ID") @RequestParam Long processId,
            @ApiParam("车间ID") @RequestParam(required = false) Long workshopId) {
        try {
            Long tenantId = com.garment.common.context.TenantContext.getCurrentTenantId();
            ProcessPriceDTO result = processPriceService.getPriceByCondition(tenantId, styleId, processId, workshopId);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("获取工价失败: {}", e.getMessage());
            return ApiResponse.error("获取失败: " + e.getMessage());
        }
    }
}