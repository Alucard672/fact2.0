package com.garment.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 分页响应结果
 *
 * @author garment
 */
@Data
@ApiModel("分页响应结果")
public class PageResponse<T> {

    @ApiModelProperty("数据列表")
    private List<T> list;

    @ApiModelProperty("总记录数")
    private long total;

    @ApiModelProperty("当前页码")
    private long current;

    @ApiModelProperty("每页条数")
    private long size;

    @ApiModelProperty("总页数")
    private long pages;

    @ApiModelProperty("是否有上一页")
    private boolean hasPrevious;

    @ApiModelProperty("是否有下一页")
    private boolean hasNext;

    public PageResponse() {
    }

    public PageResponse(List<T> list, long total, long current, long size) {
        this.list = list;
        this.total = total;
        this.current = current;
        this.size = size;
        this.pages = (total + size - 1) / size;
        this.hasPrevious = current > 1;
        this.hasNext = current < pages;
    }

    /**
     * 构建分页响应
     */
    public static <T> PageResponse<T> of(List<T> list, long total, long current, long size) {
        return new PageResponse<>(list, total, current, size);
    }

    /**
     * 空分页响应
     */
    public static <T> PageResponse<T> empty() {
        return new PageResponse<>(List.of(), 0, 1, 10);
    }
}