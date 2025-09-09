package com.garment.basic.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 车间DTO
 *
 * @author garment
 */
@Data
public class WorkshopDTO {

    /**
     * ID
     */
    private Long id;

    /**
     * 车间编码
     */
    @NotBlank(message = "车间编码不能为空")
    private String workshopCode;

    /**
     * 车间名称
     */
    @NotBlank(message = "车间名称不能为空")
    private String workshopName;

    /**
     * 车间主管ID
     */
    private Long managerId;

    /**
     * 车间描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sortOrder = 0;

    /**
     * 状态
     */
    private String status = "active";
}