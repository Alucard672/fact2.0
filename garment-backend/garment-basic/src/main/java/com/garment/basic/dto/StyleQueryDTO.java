package com.garment.basic.dto;

import lombok.Data;

/**
 * 款式查询DTO
 *
 * @author garment
 */
@Data
public class StyleQueryDTO {

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
     * 状态
     */
    private String status;
}