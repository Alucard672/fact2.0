package com.garment.payroll.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.garment.payroll.entity.ProcessPrice;

/**
 * 工序单价服务接口
 *
 * @author system
 */
public interface ProcessPriceService extends IService<ProcessPrice> {

    /**
     * 获取有效的工序单价
     *
     * @param styleId 款式ID
     * @param processId 工序ID
     * @return 工序单价
     */
    ProcessPrice getActivePrice(Long styleId, Long processId);

    /**
     * 创建或更新工序单价
     *
     * @param processPrice 工序单价
     * @return 是否成功
     */
    boolean createOrUpdatePrice(ProcessPrice processPrice);
}




