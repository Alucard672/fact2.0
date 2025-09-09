package com.garment.production.controller;

import com.garment.common.result.Result;
import com.garment.production.dto.ScanSubmitRequest;
import com.garment.production.dto.ScanTakeRequest;
import com.garment.production.entity.ProductionFlow;
import com.garment.production.service.ProductionFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 生产流转控制器
 *
 * @author system
 */
@RestController
@RequestMapping("/api/production/flows")
@RequiredArgsConstructor
public class ProductionFlowController {

    private final ProductionFlowService productionFlowService;

    /**
     * 扫码领工
     */
    @PostMapping("/scan-take")
    public Result<ProductionFlow> scanTake(@RequestBody @Validated ScanTakeRequest request) {
        ProductionFlow flow = productionFlowService.scanTake(request);
        return Result.success(flow);
    }

    /**
     * 扫码交工
     */
    @PostMapping("/scan-submit")
    public Result<ProductionFlow> scanSubmit(@RequestBody @Validated ScanSubmitRequest request) {
        ProductionFlow flow = productionFlowService.scanSubmit(request);
        return Result.success(flow);
    }

    /**
     * 根据包ID查询流转记录
     */
    @GetMapping("/bundle/{bundleId}")
    public Result<List<ProductionFlow>> getFlowsByBundleId(@PathVariable Long bundleId) {
        List<ProductionFlow> flows = productionFlowService.getFlowsByBundleId(bundleId);
        return Result.success(flows);
    }

    /**
     * 查询工人当前在制包
     */
    @GetMapping("/worker/{workerId}/current")
    public Result<List<ProductionFlow>> getCurrentWorkByWorkerId(@PathVariable Long workerId) {
        List<ProductionFlow> flows = productionFlowService.getCurrentWorkByWorkerId(workerId);
        return Result.success(flows);
    }
}



