package com.garment.basic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.garment.basic.entity.Workshop;
import org.apache.ibatis.annotations.Param;

/**
 * 车间Mapper接口
 *
 * @author garment
 */
public interface WorkshopMapper extends BaseMapper<Workshop> {

    /**
     * 分页查询车间列表
     *
     * @param page      分页对象
     * @param tenantId  租户ID
     * @param keyword   关键词搜索
     * @param status    状态
     * @return 车间分页列表
     */
    IPage<Workshop> selectWorkshopPage(Page<Workshop> page, @Param("tenantId") Long tenantId,
                                      @Param("keyword") String keyword, @Param("status") String status);
}