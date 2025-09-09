package com.garment.auth.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 令牌信息
 *
 * @author garment
 */
@Data
@ApiModel("令牌信息")
public class TokenInfo {

    @ApiModelProperty("访问令牌")
    private String accessToken;

    @ApiModelProperty("刷新令牌")
    private String refreshToken;

    @ApiModelProperty("令牌类型")
    private String tokenType = "Bearer";

    @ApiModelProperty("过期时间(秒)")
    private Long expiresIn;

    public TokenInfo(String accessToken, String refreshToken, Long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }
}