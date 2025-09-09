package com.garment.basic.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.garment.basic.entity.Style;
import com.garment.basic.dto.StyleDTO;
import com.garment.basic.dto.StyleQueryDTO;

/**
 * 款式服务接口
 *
 * @author garment
 */
public interface StyleService extends IService<Style> {

    /**
     * 分页查询款式列表
     *
     * @param queryDTO 查询参数
     * @return 款式分页列表
     */
    IPage<Style> getStylePage(StyleQueryDTO queryDTO);

    /**
     * 根据ID获取款式详情
     *
     * @param id 款式ID
     * @return 款式详情
     */
    StyleDTO getStyleById(Long id);

    /**
     * 创建款式
     *
     * @param styleDTO 款式信息
     * @return 款式详情
     */
    StyleDTO createStyle(StyleDTO styleDTO);

    /**
     * 更新款式
     *
     * @param id       款式ID
     * @param styleDTO 款式信息
     * @return 款式详情
     */
    StyleDTO updateStyle(Long id, StyleDTO styleDTO);

    /**
     * 删除款式
     *
     * @param id 款式ID
     * @return 是否删除成功
     */
    boolean deleteStyle(Long id);

    /**
     * 检查款号是否已存在
     *
     * @param tenantId  租户ID
     * @param styleCode 款号
     * @param excludeId 排除的ID（更新时使用）
     * @return 是否存在
     */
    boolean checkStyleCodeExists(Long tenantId, String styleCode, Long excludeId);
}