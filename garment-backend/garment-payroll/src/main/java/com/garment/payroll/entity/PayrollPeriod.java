package com.garment.payroll.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 工资周期实体
 *
 * @author system
 */
@Data
@TableName("payroll_periods")
public class PayrollPeriod {

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
     * 周期名称
     */
    private String periodName;

    /**
     * 周期类型：weekly-周结，monthly-月结，custom-自定义
     */
    private String periodType;

    /**
     * 开始日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * 状态：open-开放，calculating-计算中，calculated-已计算，locked-已锁定，paid-已发放
     */
    private String status;

    /**
     * 总工人数
     */
    private Integer totalWorkers;

    /**
     * 总记录数
     */
    private Integer totalRecords;

    /**
     * 总件数
     */
    private Integer totalPieces;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 平均单价
     */
    private BigDecimal avgUnitPrice;

    /**
     * 平均质量率
     */
    private BigDecimal avgQualityRate;

    /**
     * 计算开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime calculateStartTime;

    /**
     * 计算完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime calculateEndTime;

    /**
     * 锁定时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lockedAt;

    /**
     * 锁定人
     */
    private Long lockedBy;

    /**
     * 发放时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paidAt;

    /**
     * 发放人
     */
    private Long paidBy;

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




