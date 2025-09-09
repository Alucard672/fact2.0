package com.garment.production.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 还原裁床订单请求
 */
@Data
public class RestoreCutOrderRequest {

    /**
     * 是否删除已生成包
     */
    @NotNull(message = "是否删除已生成包不能为空")
    private Boolean removeBundles;
}