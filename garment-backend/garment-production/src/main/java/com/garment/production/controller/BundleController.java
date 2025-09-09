package com.garment.production.controller;

import com.garment.common.result.Result;
import com.garment.production.dto.GenerateBundlesRequest;
import com.garment.production.entity.Bundle;
import com.garment.production.service.BundleService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 包控制器
 *
 * @author system
 */
@RestController
@RequestMapping("/api/production/bundles")
@RequiredArgsConstructor
public class BundleController {

    private final BundleService bundleService;

    /**
     * 生成包
     */
    @PostMapping("/generate")
    public Result<List<Bundle>> generateBundles(@RequestBody @Validated GenerateBundlesRequest request) {
        List<Bundle> bundles = bundleService.generateBundles(request);
        return Result.success(bundles);
    }

    /**
     * 根据裁床订单ID查询包列表
     */
    @GetMapping("/cut-order/{cutOrderId}")
    public Result<List<Bundle>> getBundlesByCutOrderId(@PathVariable Long cutOrderId) {
        List<Bundle> bundles = bundleService.getBundlesByCutOrderId(cutOrderId);
        return Result.success(bundles);
    }

    /**
     * 根据二维码查询包信息
     */
    @GetMapping("/qr-code/{qrCode}")
    public Result<Bundle> getBundleByQrCode(@PathVariable String qrCode) {
        Bundle bundle = bundleService.getBundleByQrCode(qrCode);
        return Result.success(bundle);
    }

    /**
     * 获取包详情
     */
    @GetMapping("/{id}")
    public Result<Bundle> getBundle(@PathVariable Long id) {
        Bundle bundle = bundleService.getById(id);
        return Result.success(bundle);
    }
}



