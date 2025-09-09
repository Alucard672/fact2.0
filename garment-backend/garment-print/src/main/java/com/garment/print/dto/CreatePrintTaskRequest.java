package com.garment.print.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;

/**
 * 创建打印任务请求
 *
 * @author system
 */
@Data
public class CreatePrintTaskRequest {

    /**
     * 裁床订单ID
     */
    @NotNull(message = "裁床订单ID不能为空")
    private Long cutOrderId;

    /**
     * 打印类型：bundle-包菲票，qr-二维码，label-标签
     */
    @NotBlank(message = "打印类型不能为空")
    @Pattern(regexp = "^(bundle|qr|label)$", message = "打印类型只能是bundle、qr或label")
    private String printType;

    /**
     * 模板ID（可选，不传则使用默认模板）
     */
    private Long templateId;

    /**
     * 包ID列表（如果不传则打印所有包）
     */
    private List<Long> bundleIds;

    /**
     * 打印机IP
     */
    private String printerIp;

    /**
     * 打印机名称
     */
    private String printerName;

    /**
     * 额外打印参数
     */
    private Map<String, Object> printParams;
}




