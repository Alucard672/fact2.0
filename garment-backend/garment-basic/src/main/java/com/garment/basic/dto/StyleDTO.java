package com.garment.basic.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 款式DTO
 *
 * @author garment
 */
@Data
public class StyleDTO {

    /**
     * ID
     */
    private Long id;

    /**
     * 款号
     */
    @NotBlank(message = "款号不能为空")
    private String styleCode;

    /**
     * 款式名称
     */
    @NotBlank(message = "款式名称不能为空")
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
    private String status = "active";
}