package com.garment.basic.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.garment.basic.entity.Workshop;
import com.garment.basic.dto.WorkshopDTO;
import com.garment.basic.dto.WorkshopQueryDTO;

/**
 * 车间服务接口
 *
 * @author garment
 */
public interface WorkshopService extends IService<Workshop> {

    /**
     * 分页查询车间列表
     *
     * @param queryDTO 查询参数
     * @return 车间分页列表
     */
    IPage<Workshop> getWorkshopPage(WorkshopQueryDTO queryDTO);

    /**
     * 根据ID获取车间详情
     *
     * @param id 车间ID
     * @return 车间详情
     */
    WorkshopDTO getWorkshopById(Long id);

    /**
     * 创建车间
     *
     * @param workshopDTO 车间信息
     * @return 车间详情
     */
    WorkshopDTO createWorkshop(WorkshopDTO workshopDTO);

    /**
     * 更新车间
     *
     * @param id          车间ID
     * @param workshopDTO 车间信息
     * @return 车间详情
     */
    WorkshopDTO updateWorkshop(Long id, WorkshopDTO workshopDTO);

    /**
     * 删除车间
     *
     * @param id 车间ID
     * @return 是否删除成功
     */
    boolean deleteWorkshop(Long id);

    /**
     * 检查车间编码是否已存在
     *
     * @param tenantId     租户ID
     * @param workshopCode 车间编码
     * @param excludeId    排除的ID（更新时使用）
     * @return 是否存在
     */
    boolean checkWorkshopCodeExists(Long tenantId, String workshopCode, Long excludeId);
}