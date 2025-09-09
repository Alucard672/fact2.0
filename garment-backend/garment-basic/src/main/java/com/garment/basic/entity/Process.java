package com.garment.basic.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 工序实体
 *
 * @author garment
 */
@Data
@TableName("processes")
public class Process {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 工序编码
     */
    private String processCode;

    /**
     * 工序名称
     */
    private String processName;

    /**
     * 工序分类
     */
    private String category;

    /**
     * 计量单位
     */
    private String unit;

    /**
     * 默认工价
     */
    private BigDecimal defaultPrice;

    /**
     * 难度等级
     */
    private String difficultyLevel;

    /**
     * 质量标准
     */
    private String qualityStandard;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态
     */
    private String status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Boolean deleted;
}