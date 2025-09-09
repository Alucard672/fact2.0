package com.garment.basic.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.garment.basic.entity.ProcessPrice;
import com.garment.basic.dto.ProcessPriceDTO;
import com.garment.basic.dto.ProcessPriceQueryDTO;

/**
 * 工价模板服务接口
 *
 * @author garment
 */
public interface ProcessPriceService extends IService<ProcessPrice> {

    /**
     * 分页查询工价模板列表
     *
     * @param queryDTO 查询参数
     * @return 工价模板分页列表
     */
    IPage<ProcessPrice> getProcessPricePage(ProcessPriceQueryDTO queryDTO);

    /**
     * 根据ID获取工价模板详情
     *
     * @param id 工价模板ID
     * @return 工价模板详情
     */
    ProcessPriceDTO getProcessPriceById(Long id);

    /**
     * 创建工价模板
     *
     * @param processPriceDTO 工价模板信息
     * @return 工价模板详情
     */
    ProcessPriceDTO createProcessPrice(ProcessPriceDTO processPriceDTO);

    /**
     * 更新工价模板
     *
     * @param id              工价模板ID
     * @param processPriceDTO 工价模板信息
     * @return 工价模板详情
     */
    ProcessPriceDTO updateProcessPrice(Long id, ProcessPriceDTO processPriceDTO);

    /**
     * 删除工价模板
     *
     * @param id 工价模板ID
     * @return 是否删除成功
     */
    boolean deleteProcessPrice(Long id);

    /**
     * 根据条件获取工价
     *
     * @param tenantId   租户ID
     * @param styleId    款式ID
     * @param processId  工序ID
     * @param workshopId 车间ID
     * @return 工价
     */
    ProcessPriceDTO getPriceByCondition(Long tenantId, Long styleId, Long processId, Long workshopId);
}