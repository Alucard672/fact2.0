package com.garment.basic.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 工价模板DTO
 *
 * @author garment
 */
@Data
public class ProcessPriceDTO {

    /**
     * ID
     */
    private Long id;

    /**
     * 款式ID（为空表示通用）
     */
    private Long styleId;

    /**
     * 工序ID
     */
    @NotNull(message = "工序ID不能为空")
    private Long processId;

    /**
     * 车间ID（为空表示通用）
     */
    private Long workshopId;

    /**
     * 工价
     */
    @NotNull(message = "工价不能为空")
    private BigDecimal price;

    /**
     * 计价方式
     */
    private String priceType = "per_piece";

    /**
     * 生效日期
     */
    @NotBlank(message = "生效日期不能为空")
    private String effectiveFrom;

    /**
     * 失效日期
     */
    private String effectiveTo;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 备注
     */
    private String remark;

    /**
     * 状态
     */
    private String status = "active";

    /**
     * 创建人ID
     */
    private Long createdBy;
}