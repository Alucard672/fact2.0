package com.garment.production.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 包实体
 *
 * @author system
 */
@Data
@TableName("bundles")
public class Bundle {

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
     * 裁床订单ID
     */
    private Long cutOrderId;

    /**
     * 包号
     */
    private String bundleNo;

    /**
     * 尺码
     */
    private String size;

    /**
     * 颜色
     */
    private String color;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 起始层（分层段裁使用）
     */
    private Integer layerFrom;

    /**
     * 结束层（分层段裁使用）
     */
    private Integer layerTo;

    /**
     * 层段标签（分层段裁使用）
     */
    private String segmentTag;

    /**
     * 二维码内容
     */
    private String qrCode;

    /**
     * 状态：pending-待领工，in_work-生产中，completed-已完成，returned-已返修
     */
    private String status;

    /**
     * 当前工序ID
     */
    private Long currentProcessId;

    /**
     * 当前工人ID
     */
    private Long currentWorkerId;

    /**
     * 进度信息（JSON格式）
     */
    private String progress;

    /**
     * 完成率
     */
    private java.math.BigDecimal completionRate;

    /**
     * 质量等级：A-优，B-良，C-合格，D-次品
     */
    private String qualityGrade;

    /**
     * 次品数量
     */
    private Integer defectCount;

    /**
     * 打印时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime printedAt;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startedAt;

    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;

    /**
     * 创建人
     */
    @TableField(exist = false)
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
    @TableField(exist = false)
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




