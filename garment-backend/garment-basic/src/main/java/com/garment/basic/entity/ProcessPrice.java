package com.garment.basic.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 工价模板实体
 *
 * @author garment
 */
@Data
@TableName("process_prices")
public class ProcessPrice {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 款式ID（为空表示通用）
     */
    private Long styleId;

    /**
     * 工序ID
     */
    private Long processId;

    /**
     * 车间ID（为空表示通用）
     */
    private Long workshopId;

    /**
     * 工价
     */
    private BigDecimal price;

    /**
     * 计价方式
     */
    private String priceType;

    /**
     * 生效日期
     */
    private LocalDate effectiveFrom;

    /**
     * 失效日期
     */
    private LocalDate effectiveTo;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 备注
     */
    private String remark;

    /**
     * 状态
     */
    private String status;

    /**
     * 创建人ID
     */
    private Long createdBy;

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