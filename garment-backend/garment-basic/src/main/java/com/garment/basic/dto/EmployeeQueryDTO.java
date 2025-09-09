package com.garment.basic.dto;

import lombok.Data;

/**
 * 员工查询DTO
 *
 * @author garment
 */
@Data
public class EmployeeQueryDTO {

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
     * 角色
     */
    private String role;

    /**
     * 状态
     */
    private String status;
}