package com.garment.auth.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 刷新令牌请求
 *
 * @author garment
 */
@Data
@ApiModel("刷新令牌请求")
public class RefreshTokenRequest {

    @ApiModelProperty(value = "刷新令牌", required = true)
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}