package com.garment.production.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.garment.production.dto.GenerateBundlesRequest;
import com.garment.production.entity.Bundle;

import java.util.List;

/**
 * 包服务接口
 *
 * @author system
 */
public interface BundleService extends IService<Bundle> {

    /**
     * 生成包
     *
     * @param request 生成请求
     * @return 生成的包列表
     */
    List<Bundle> generateBundles(GenerateBundlesRequest request);

    /**
     * 根据裁床订单ID查询包列表
     *
     * @param cutOrderId 裁床订单ID
     * @return 包列表
     */
    List<Bundle> getBundlesByCutOrderId(Long cutOrderId);

    /**
     * 根据二维码查询包信息
     *
     * @param qrCode 二维码内容
     * @return 包信息
     */
    Bundle getBundleByQrCode(String qrCode);

    /**
     * 生成二维码内容
     *
     * @param bundle 包信息
     * @return 二维码内容
     */
    String generateQrCode(Bundle bundle);

    /**
     * 解析二维码内容
     *
     * @param qrCode 二维码内容
     * @return 包ID
     */
    Long parseQrCode(String qrCode);
}




