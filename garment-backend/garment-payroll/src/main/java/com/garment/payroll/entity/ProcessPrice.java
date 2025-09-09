package com.garment.payroll.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 工序单价实体
 *
 * @author system
 */
@Data
@TableName("process_prices")
public class ProcessPrice {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 款式ID
     */
    private Long styleId;

    /**
     * 款式编码
     */
    private String styleCode;

    /**
     * 款式名称
     */
    private String styleName;

    /**
     * 工序ID
     */
    private Long processId;

    /**
     * 工序名称
     */
    private String processName;

    /**
     * 基础单价
     */
    private BigDecimal basePrice;

    /**
     * 难度系数
     */
    private BigDecimal difficultyFactor;

    /**
     * 质量要求等级：A-高，B-中，C-低
     */
    private String qualityLevel;

    /**
     * 质量系数
     */
    private BigDecimal qualityFactor;

    /**
     * 最终单价（基础单价 * 难度系数 * 质量系数）
     */
    private BigDecimal finalPrice;

    /**
     * 标准用时（分钟/件）
     */
    private BigDecimal standardTime;

    /**
     * 生效日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveDate;

    /**
     * 失效日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;

    /**
     * 状态：active-生效，inactive-失效
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新人
     */
    private Long updatedBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 删除标记
     */
    @TableLogic
    private Boolean deleted;
}




