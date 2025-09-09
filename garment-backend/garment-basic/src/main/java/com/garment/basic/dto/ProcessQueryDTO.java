package com.garment.basic.dto;

import lombok.Data;

/**
 * 工序查询DTO
 *
 * @author garment
 */
@Data
public class ProcessQueryDTO {

    /**
     * 页码
     */
    private int page = 1;

    /**
     * 每页大小
     */
    private int size = 10;

    /**
     * 关键词搜索
     */
    private String keyword;

    /**
     * 分类
     */
    private String category;

    /**
     * 状态
     */
    private String status;
}