package com.garment.production.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 生产流转记录实体
 *
 * @author system
 */
@Data
@TableName("production_flows")
public class ProductionFlow {

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
     * 操作类型：take-领工，submit-交工，return-退回，repair-返修
     */
    private String actionType;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 合格数量
     */
    private Integer quantityOk;

    /**
     * 次品数量
     */
    private Integer quantityNg;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 质量评分
     */
    private Integer qualityScore;

    /**
     * 是否需要返修
     */
    private Boolean needRepair;

    /**
     * 备注
     */
    private String remark;

    /**
     * 操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operatedAt;

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




