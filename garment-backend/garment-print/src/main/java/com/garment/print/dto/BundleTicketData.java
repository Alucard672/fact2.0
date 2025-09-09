package com.garment.print.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 包菲票数据
 *
 * @author system
 */
@Data
public class BundleTicketData {

    /**
     * 包号
     */
    private String bundleNo;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 款式编码
     */
    private String styleCode;

    /**
     * 款式名称
     */
    private String styleName;

    /**
     * 颜色
     */
    private String color;

    /**
     * 尺码
     */
    private String size;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 床次号
     */
    private String bedNo;

    /**
     * 层段标签（分层段裁使用）
     */
    private String segmentTag;

    /**
     * 起始层（分层段裁使用）
     */
    private Integer layerFrom;

    /**
     * 结束层（分层段裁使用）
     */
    private Integer layerTo;

    /**
     * 二维码内容
     */
    private String qrCode;

    /**
     * 二维码图片Base64
     */
    private String qrCodeImage;

    /**
     * 裁剪日期
     */
    private LocalDate cuttingDate;

    /**
     * 交货日期
     */
    private LocalDate deliveryDate;

    /**
     * 优先级
     */
    private String priority;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 车间名称
     */
    private String workshopName;

    /**
     * 打印时间
     */
    private String printTime;
}




