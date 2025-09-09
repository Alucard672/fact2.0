package com.garment.production.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 生成包请求
 *
 * @author system
 */
@Data
public class GenerateBundlesRequest {

    /**
     * 裁床订单ID
     */
    @NotNull(message = "裁床订单ID不能为空")
    private Long cutOrderId;

    /**
     * 每包数量（可选，不传则使用默认算法）
     */
    private Integer bundleSize;

    /**
     * 是否重新生成（如果已有包则覆盖）
     */
    private Boolean regenerate = false;
}




