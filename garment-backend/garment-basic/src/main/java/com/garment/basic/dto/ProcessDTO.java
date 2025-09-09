package com.garment.basic.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 工序DTO
 *
 * @author garment
 */
@Data
public class ProcessDTO {

    /**
     * ID
     */
    private Long id;

    /**
     * 工序编码
     */
    @NotBlank(message = "工序编码不能为空")
    private String processCode;

    /**
     * 工序名称
     */
    @NotBlank(message = "工序名称不能为空")
    private String processName;

    /**
     * 工序分类
     */
    private String category;

    /**
     * 计量单位
     */
    private String unit = "件";

    /**
     * 默认工价
     */
    private BigDecimal defaultPrice;

    /**
     * 难度等级
     */
    private String difficultyLevel = "medium";

    /**
     * 质量标准
     */
    private String qualityStandard;

    /**
     * 排序
     */
    private Integer sortOrder = 0;

    /**
     * 状态
     */
    private String status = "active";
}