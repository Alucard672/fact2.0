package com.garment.print.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 打印任务实体
 *
 * @author system
 */
@Data
@TableName("print_tasks")
public class PrintTask {

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
     * 任务编号
     */
    private String taskNo;

    /**
     * 裁床订单ID
     */
    private Long cutOrderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 打印类型：bundle-包菲票，qr-二维码，label-标签
     */
    private String printType;

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 打印数量
     */
    private Integer printCount;

    /**
     * 打印数据，JSON格式
     */
    private String printData;

    /**
     * 打印机IP
     */
    private String printerIp;

    /**
     * 打印机名称
     */
    private String printerName;

    /**
     * 状态：pending-待打印，printing-打印中，completed-已完成，failed-失败
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 打印开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime printStartTime;

    /**
     * 打印完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime printEndTime;

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




