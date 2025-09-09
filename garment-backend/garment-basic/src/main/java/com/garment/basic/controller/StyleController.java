package com.garment.basic.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.garment.basic.dto.StyleDTO;
import com.garment.basic.dto.StyleQueryDTO;
import com.garment.basic.entity.Style;
import com.garment.basic.service.StyleService;
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
 * 款式管理控制器
 *
 * @author garment
 */
@Slf4j
@RestController
@RequestMapping("/api/styles")
@Api(tags = "款式管理")
@RequiredArgsConstructor
@Validated
public class StyleController {

    private final StyleService styleService;

    /**
     * 分页查询款式列表
     */
    @GetMapping
    @ApiOperation("分页查询款式列表")
    public ApiResponse<IPage<Style>> getStylePage(@Valid StyleQueryDTO queryDTO) {
        try {
            IPage<Style> page = styleService.getStylePage(queryDTO);
            return ApiResponse.success(page);
        } catch (Exception e) {
            log.error("分页查询款式列表失败: {}", e.getMessage());
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取款式详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID获取款式详情")
    public ApiResponse<StyleDTO> getStyleById(
            @ApiParam("款式ID") @PathVariable Long id) {
        try {
            StyleDTO styleDTO = styleService.getStyleById(id);
            return ApiResponse.success(styleDTO);
        } catch (Exception e) {
            log.error("获取款式详情失败: {}", e.getMessage());
            return ApiResponse.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 创建款式
     */
    @PostMapping
    @ApiOperation("创建款式")
    @RequireRole({"owner", "admin", "manager"})
    public ApiResponse<StyleDTO> createStyle(@RequestBody @Valid StyleDTO styleDTO) {
        try {
            StyleDTO result = styleService.createStyle(styleDTO);
            return ApiResponse.success("创建成功", result);
        } catch (Exception e) {
            log.error("创建款式失败: {}", e.getMessage());
            return ApiResponse.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新款式
     */
    @PutMapping("/{id}")
    @ApiOperation("更新款式")
    @RequireRole({"owner", "admin", "manager"})
    public ApiResponse<StyleDTO> updateStyle(
            @ApiParam("款式ID") @PathVariable Long id,
            @RequestBody @Valid StyleDTO styleDTO) {
        try {
            StyleDTO result = styleService.updateStyle(id, styleDTO);
            return ApiResponse.success("更新成功", result);
        } catch (Exception e) {
            log.error("更新款式失败: {}", e.getMessage());
            return ApiResponse.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除款式
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除款式")
    @RequireRole({"owner", "admin"})
    public ApiResponse<Void> deleteStyle(
            @ApiParam("款式ID") @PathVariable Long id) {
        try {
            boolean result = styleService.deleteStyle(id);
            if (result) {
                return ApiResponse.success("删除成功");
            } else {
                return ApiResponse.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除款式失败: {}", e.getMessage());
            return ApiResponse.error("删除失败: " + e.getMessage());
        }
    }
}