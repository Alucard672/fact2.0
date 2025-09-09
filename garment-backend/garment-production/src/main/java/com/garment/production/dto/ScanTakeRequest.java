package com.garment.production.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 扫码领工请求
 *
 * @author system
 */
@Data
public class ScanTakeRequest {

    /**
     * 二维码内容
     */
    @NotBlank(message = "二维码内容不能为空")
    private String qrCode;

    /**
     * 工序ID
     */
    @NotNull(message = "工序ID不能为空")
    private Long processId;

    /**
     * 工人ID（从JWT中获取，也可以传递）
     */
    private Long workerId;

    /**
     * 备注
     */
    private String remark;
}




