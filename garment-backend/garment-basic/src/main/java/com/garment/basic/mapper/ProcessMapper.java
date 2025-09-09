package com.garment.basic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.garment.basic.entity.Process;
import org.apache.ibatis.annotations.Param;

/**
 * 工序Mapper接口
 *
 * @author garment
 */
public interface ProcessMapper extends BaseMapper<Process> {

    /**
     * 分页查询工序列表
     *
     * @param page      分页对象
     * @param tenantId  租户ID
     * @param keyword   关键词搜索
     * @param category  分类
     * @param status    状态
     * @return 工序分页列表
     */
    IPage<Process> selectProcessPage(Page<Process> page, @Param("tenantId") Long tenantId,
                                     @Param("keyword") String keyword, @Param("category") String category,
                                     @Param("status") String status);
}