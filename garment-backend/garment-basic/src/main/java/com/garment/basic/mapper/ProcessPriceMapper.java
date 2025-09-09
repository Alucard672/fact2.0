package com.garment.basic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.garment.basic.entity.ProcessPrice;
import org.apache.ibatis.annotations.Param;

/**
 * 工价模板Mapper接口
 *
 * @author garment
 */
public interface ProcessPriceMapper extends BaseMapper<ProcessPrice> {

    /**
     * 分页查询工价模板列表
     *
     * @param page      分页对象
     * @param tenantId  租户ID
     * @param keyword   关键词搜索
     * @param status    状态
     * @return 工价模板分页列表
     */
    IPage<ProcessPrice> selectProcessPricePage(Page<ProcessPrice> page, @Param("tenantId") Long tenantId,
                                              @Param("keyword") String keyword, @Param("status") String status);
}