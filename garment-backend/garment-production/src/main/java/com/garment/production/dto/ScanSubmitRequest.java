package com.garment.production.dto;

import lombok.Data;

import javax.validation.constraints.*;

/**
 * 扫码交工请求
 *
 * @author system
 */
@Data
public class ScanSubmitRequest {

    /**
     * 二维码内容
     */
    @NotBlank(message = "二维码内容不能为空")
    private String qrCode;

    /**
     * 合格数量
     */
    @NotNull(message = "合格数量不能为空")
    @Min(value = 0, message = "合格数量不能小于0")
    private Integer quantityOk;

    /**
     * 次品数量
     */
    @Min(value = 0, message = "次品数量不能小于0")
    private Integer quantityNg = 0;

    /**
     * 质量评分（1-5分）
     */
    @Min(value = 1, message = "质量评分不能小于1")
    @Max(value = 5, message = "质量评分不能大于5")
    private Integer qualityScore = 5;

    /**
     * 是否需要返修
     */
    private Boolean needRepair = false;

    /**
     * 工人ID（从JWT中获取，也可以传递）
     */
    private Long workerId;

    /**
     * 备注
     */
    private String remark;
}




