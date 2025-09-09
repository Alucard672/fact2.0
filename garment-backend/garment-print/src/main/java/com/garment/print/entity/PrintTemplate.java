package com.garment.print.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 打印模板实体
 *
 * @author system
 */
@Data
@TableName("print_templates")
public class PrintTemplate {

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
     * 模板名称
     */
    private String templateName;

    /**
     * 模板类型：bundle-包菲票，qr-二维码，label-标签
     */
    private String templateType;

    /**
     * 模板内容，HTML格式
     */
    private String templateContent;

    /**
     * 模板样式，CSS格式
     */
    private String templateStyle;

    /**
     * 纸张尺寸：A4、A5、58mm、80mm等
     */
    private String paperSize;

    /**
     * 打印方向：portrait-纵向，landscape-横向
     */
    private String orientation;

    /**
     * 页边距设置，JSON格式
     */
    private String margins;

    /**
     * 是否默认模板
     */
    private Boolean isDefault;

    /**
     * 状态：active-启用，inactive-禁用
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




