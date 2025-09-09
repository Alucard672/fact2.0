package com.garment.payroll.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 计件记录实体
 *
 * @author system
 */
@Data
@TableName("piecework_records")
public class PieceworkRecord {

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
     * 工人ID
     */
    private Long workerId;

    /**
     * 工人姓名
     */
    private String workerName;

    /**
     * 车间ID
     */
    private Long workshopId;

    /**
     * 车间名称
     */
    private String workshopName;

    /**
     * 包ID
     */
    private Long bundleId;

    /**
     * 包号
     */
    private String bundleNo;

    /**
     * 工序ID
     */
    private Long processId;

    /**
     * 工序名称
     */
    private String processName;

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
     * 颜色
     */
    private String color;

    /**
     * 尺码
     */
    private String size;

    /**
     * 完成数量
     */
    private Integer quantity;

    /**
     * 合格数量
     */
    private Integer qualifiedQuantity;

    /**
     * 次品数量
     */
    private Integer defectQuantity;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 计件金额
     */
    private BigDecimal amount;

    /**
     * 质量系数（影响最终金额）
     */
    private BigDecimal qualityFactor;

    /**
     * 实际金额（计件金额 * 质量系数）
     */
    private BigDecimal actualAmount;

    /**
     * 难度系数
     */
    private BigDecimal difficultyFactor;

    /**
     * 紧急系数
     */
    private BigDecimal urgencyFactor;

    /**
     * 工作日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String workDate;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 用时（分钟）
     */
    private Integer duration;

    /**
     * 状态：pending-待结算，settled-已结算，paid-已发放
     */
    private String status;

    /**
     * 结算周期ID
     */
    private Long payrollPeriodId;

    /**
     * 结算时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime settledAt;

    /**
     * 发放时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paidAt;

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




