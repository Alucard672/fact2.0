package com.garment.basic.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 款式实体
 *
 * @author garment
 */
@Data
@TableName("styles")
public class Style {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 款号
     */
    private String styleCode;

    /**
     * 款式名称
     */
    private String styleName;

    /**
     * 客户ID
     */
    private Long customerId;

    /**
     * 季节
     */
    private String season;

    /**
     * 分类
     */
    private String category;

    /**
     * 颜色列表 (JSON格式)
     */
    private String colors;

    /**
     * 尺码列表 (JSON格式)
     */
    private String sizes;

    /**
     * 默认尺码比例 (JSON格式)
     */
    private String sizeRatio;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 款式描述
     */
    private String description;

    /**
     * 款式图片 (JSON格式)
     */
    private String imageUrls;

    /**
     * 工艺说明
     */
    private String techSpec;

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