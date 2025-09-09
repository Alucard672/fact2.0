package com.garment.basic.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.garment.basic.dto.WorkshopDTO;
import com.garment.basic.dto.WorkshopQueryDTO;
import com.garment.basic.entity.Workshop;
import com.garment.basic.service.WorkshopService;
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
 * 车间管理控制器
 *
 * @author garment
 */
@Slf4j
@RestController
@RequestMapping("/api/workshops")
@Api(tags = "车间管理")
@RequiredArgsConstructor
@Validated
public class WorkshopController {

    private final WorkshopService workshopService;

    /**
     * 分页查询车间列表
     */
    @GetMapping
    @ApiOperation("分页查询车间列表")
    public ApiResponse<IPage<Workshop>> getWorkshopPage(@Valid WorkshopQueryDTO queryDTO) {
        try {
            IPage<Workshop> page = workshopService.getWorkshopPage(queryDTO);
            return ApiResponse.success(page);
        } catch (Exception e) {
            log.error("分页查询车间列表失败: {}", e.getMessage());
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取车间详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID获取车间详情")
    public ApiResponse<WorkshopDTO> getWorkshopById(
            @ApiParam("车间ID") @PathVariable Long id) {
        try {
            WorkshopDTO workshopDTO = workshopService.getWorkshopById(id);
            return ApiResponse.success(workshopDTO);
        } catch (Exception e) {
            log.error("获取车间详情失败: {}", e.getMessage());
            return ApiResponse.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 创建车间
     */
    @PostMapping
    @ApiOperation("创建车间")
    @RequireRole({"owner", "admin", "manager"})
    public ApiResponse<WorkshopDTO> createWorkshop(@RequestBody @Valid WorkshopDTO workshopDTO) {
        try {
            WorkshopDTO result = workshopService.createWorkshop(workshopDTO);
            return ApiResponse.success("创建成功", result);
        } catch (Exception e) {
            log.error("创建车间失败: {}", e.getMessage());
            return ApiResponse.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新车间
     */
    @PutMapping("/{id}")
    @ApiOperation("更新车间")
    @RequireRole({"owner", "admin", "manager"})
    public ApiResponse<WorkshopDTO> updateWorkshop(
            @ApiParam("车间ID") @PathVariable Long id,
            @RequestBody @Valid WorkshopDTO workshopDTO) {
        try {
            WorkshopDTO result = workshopService.updateWorkshop(id, workshopDTO);
            return ApiResponse.success("更新成功", result);
        } catch (Exception e) {
            log.error("更新车间失败: {}", e.getMessage());
            return ApiResponse.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除车间
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除车间")
    @RequireRole({"owner", "admin"})
    public ApiResponse<Void> deleteWorkshop(
            @ApiParam("车间ID") @PathVariable Long id) {
        try {
            boolean result = workshopService.deleteWorkshop(id);
            if (result) {
                return ApiResponse.success("删除成功");
            } else {
                return ApiResponse.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除车间失败: {}", e.getMessage());
            return ApiResponse.error("删除失败: " + e.getMessage());
        }
    }
}