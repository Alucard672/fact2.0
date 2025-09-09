package com.garment.production.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 裁床订单实体
 *
 * @author system
 */
@Data
@TableName("cut_orders")
public class CutOrder {

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
     * 订单号
     */
    private String orderNo;

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
     * 床次号
     */
    private String bedNo;

    /**
     * 总层数
     */
    private Integer totalLayers;

    /**
     * 裁切方式：average-平均裁，segment-分层段裁
     */
    private String cuttingType;

    /**
     * 尺码比例，JSON格式
     */
    private String sizeRatio;

    /**
     * 分层段方案，JSON格式（仅分层段裁使用）
     */
    private String segmentPlan;

    /**
     * 总数量
     */
    private Integer totalQuantity;

    /**
     * 包数量
     */
    private Integer bundleCount;

    /**
     * 裁剪日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate cuttingDate;

    /**
     * 交货日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deliveryDate;

    /**
     * 优先级：low-低，medium-中，high-高，urgent-紧急
     */
    private String priority;

    /**
     * 状态：draft-草稿，confirmed-已确认，cutting-裁剪中，completed-已完成，cancelled-已取消
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




