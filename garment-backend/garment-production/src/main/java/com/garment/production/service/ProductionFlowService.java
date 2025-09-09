package com.garment.production.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.garment.production.dto.ScanSubmitRequest;
import com.garment.production.dto.ScanTakeRequest;
import com.garment.production.entity.ProductionFlow;

import java.util.List;

/**
 * 生产流转服务接口
 *
 * @author system
 */
public interface ProductionFlowService extends IService<ProductionFlow> {

    /**
     * 扫码领工
     *
     * @param request 领工请求
     * @return 流转记录
     */
    ProductionFlow scanTake(ScanTakeRequest request);

    /**
     * 扫码交工
     *
     * @param request 交工请求
     * @return 流转记录
     */
    ProductionFlow scanSubmit(ScanSubmitRequest request);

    /**
     * 根据包ID查询流转记录
     *
     * @param bundleId 包ID
     * @return 流转记录列表
     */
    List<ProductionFlow> getFlowsByBundleId(Long bundleId);

    /**
     * 根据工人ID查询当前在制包
     *
     * @param workerId 工人ID
     * @return 在制包的流转记录
     */
    List<ProductionFlow> getCurrentWorkByWorkerId(Long workerId);
}




