package com.garment.production.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 创建裁床订单请求
 *
 * @author system
 */
@Data
public class CreateCutOrderRequest {

    /**
     * 款式ID
     */
    @NotNull(message = "款式ID不能为空")
    private Long styleId;

    /**
     * 颜色
     */
    @NotBlank(message = "颜色不能为空")
    private String color;

    /**
     * 床次号
     */
    @NotBlank(message = "床次号不能为空")
    private String bedNo;

    /**
     * 总层数
     */
    @NotNull(message = "总层数不能为空")
    @Min(value = 1, message = "总层数不能小于1")
    private Integer totalLayers;

    /**
     * 裁切方式：average-平均裁，segment-分层段裁
     */
    @NotBlank(message = "裁切方式不能为空")
    @Pattern(regexp = "^(average|segment)$", message = "裁切方式只能是average或segment")
    private String cuttingType;

    /**
     * 尺码比例
     */
    @NotNull(message = "尺码比例不能为空")
    private Map<String, Integer> sizeRatio;

    /**
     * 分层段方案（仅分层段裁使用）
     */
    private SegmentPlan segmentPlan;

    /**
     * 裁剪日期
     */
    @NotNull(message = "裁剪日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate cuttingDate;

    /**
     * 交货日期
     */
    @NotNull(message = "交货日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deliveryDate;

    /**
     * 优先级：low-低，medium-中，high-高，urgent-紧急
     */
    @NotBlank(message = "优先级不能为空")
    @Pattern(regexp = "^(low|medium|high|urgent)$", message = "优先级只能是low、medium、high或urgent")
    private String priority;

    /**
     * 备注
     */
    private String remark;

    /**
     * 分层段方案
     */
    @Data
    public static class SegmentPlan {
        
        /**
         * 分层段列表
         */
        @NotNull(message = "分层段列表不能为空")
        private List<Segment> segments;

        /**
         * 分层段
         */
        @Data
        public static class Segment {
            
            /**
             * 层段标签
             */
            @NotBlank(message = "层段标签不能为空")
            private String segmentTag;

            /**
             * 起始层
             */
            @NotNull(message = "起始层不能为空")
            @Min(value = 1, message = "起始层不能小于1")
            private Integer layerFrom;

            /**
             * 结束层
             */
            @NotNull(message = "结束层不能为空")
            @Min(value = 1, message = "结束层不能小于1")
            private Integer layerTo;

            /**
             * 尺码比例
             */
            @NotNull(message = "尺码比例不能为空")
            private Map<String, Integer> sizeRatio;
        }
    }
}




