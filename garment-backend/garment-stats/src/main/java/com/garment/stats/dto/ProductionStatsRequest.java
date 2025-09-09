package com.garment.stats.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.List;

/**
 * 生产统计请求
 *
 * @author system
 */
@Data
public class ProductionStatsRequest {

    /**
     * 统计维度：day-按日，week-按周，month-按月，quarter-按季度，year-按年
     */
    @NotBlank(message = "统计维度不能为空")
    @Pattern(regexp = "^(day|week|month|quarter|year)$", message = "统计维度只能是day、week、month、quarter或year")
    private String dimension;

    /**
     * 开始日期
     */
    @NotNull(message = "开始日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @NotNull(message = "结束日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * 车间ID列表（可选，不传则统计所有车间）
     */
    private List<Long> workshopIds;

    /**
     * 款式ID列表（可选，不传则统计所有款式）
     */
    private List<Long> styleIds;

    /**
     * 工序ID列表（可选，不传则统计所有工序）
     */
    private List<Long> processIds;

    /**
     * 是否包含返修数据
     */
    private Boolean includeRepair = false;
}




