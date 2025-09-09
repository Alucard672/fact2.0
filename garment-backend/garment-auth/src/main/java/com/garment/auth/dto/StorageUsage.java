package com.garment.auth.dto;

import lombok.Data;

/**
 * 存储使用情况DTO
 *
 * @author garment
 */
@Data
public class StorageUsage {

    /**
     * 总存储空间（字节）
     */
    private Long totalStorage;

    /**
     * 已使用存储空间（字节）
     */
    private Long usedStorage;

    /**
     * 剩余存储空间（字节）
     */
    private Long availableStorage;

    /**
     * 使用百分比
     */
    private Double usagePercentage;

    /**
     * 存储类型分布
     */
    private StorageTypeDistribution distribution;

    @Data
    public static class StorageTypeDistribution {
        /**
         * 图片存储（字节）
         */
        private Long images;

        /**
         * 文档存储（字节）
         */
        private Long documents;

        /**
         * 日志存储（字节）
         */
        private Long logs;

        /**
         * 其他存储（字节）
         */
        private Long others;
    }
}