package com.garment.production.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.garment.common.result.Result;
import com.garment.production.dto.CreateCutOrderRequest;
import com.garment.production.dto.RestoreCutOrderRequest;
import com.garment.production.entity.CutOrder;
import com.garment.production.service.CutOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 裁床订单控制器
 *
 * @author system
 */
@RestController
@RequestMapping("/api/production/cut-orders")
@RequiredArgsConstructor
public class CutOrderController {

    private final CutOrderService cutOrderService;

    /**
     * 创建裁床订单
     */
    @PostMapping
    public Result<CutOrder> createCutOrder(@RequestBody @Validated CreateCutOrderRequest request) {
        CutOrder cutOrder = cutOrderService.createCutOrder(request);
        return Result.success(cutOrder);
    }

    /**
     * 分页查询裁床订单
     */
    @GetMapping
    public Result<Page<CutOrder>> pageCutOrders(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long styleId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority) {
        
        Page<CutOrder> page = new Page<>(current, size);
        Page<CutOrder> result = cutOrderService.pageCutOrders(page, orderNo, styleId, status, priority);
        return Result.success(result);
    }

    /**
     * 获取裁床订单详情
     */
    @GetMapping("/{id}")
    public Result<CutOrder> getCutOrder(@PathVariable Long id) {
        CutOrder cutOrder = cutOrderService.getById(id);
        return Result.success(cutOrder);
    }

    /**
     * 确认裁床订单
     */
    @PostMapping("/{id}/confirm")
    public Result<Void> confirmCutOrder(@PathVariable Long id) {
        cutOrderService.confirmCutOrder(id);
        return Result.success();
    }

    /**
     * 取消裁床订单
     */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelCutOrder(@PathVariable Long id) {
        cutOrderService.cancelCutOrder(id);
        return Result.success();
    }

    /**
     * 还原裁床订单为草稿状态（可选清除包）
     */
    @PostMapping("/{id}/restore")
    public Result<Void> restoreCutOrder(@PathVariable Long id, @RequestBody @Validated RestoreCutOrderRequest request) {
        cutOrderService.restoreCutOrder(id, Boolean.TRUE.equals(request.getRemoveBundles()));
        return Result.success();
    }

    /**
     * 删除裁床订单
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCutOrder(@PathVariable Long id) {
        cutOrderService.removeById(id);
        return Result.success();
    }
}



