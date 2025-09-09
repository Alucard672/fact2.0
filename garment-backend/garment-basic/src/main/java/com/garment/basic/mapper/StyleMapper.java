package com.garment.basic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.garment.basic.entity.Style;
import org.apache.ibatis.annotations.Param;

/**
 * 款式Mapper接口
 *
 * @author garment
 */
public interface StyleMapper extends BaseMapper<Style> {

    /**
     * 分页查询款式列表
     *
     * @param page      分页对象
     * @param tenantId  租户ID
     * @param keyword   关键词搜索
     * @param status    状态
     * @return 款式分页列表
     */
    IPage<Style> selectStylePage(Page<Style> page, @Param("tenantId") Long tenantId,
                                 @Param("keyword") String keyword, @Param("status") String status);
}