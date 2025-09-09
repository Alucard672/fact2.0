package com.garment.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 业务配置
 *
 * @author system
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "garment.business")
public class BusinessConfig {

    /**
     * 包配置
     */
    private BundleConfig bundle = new BundleConfig();

    /**
     * 质量配置
     */
    private QualityConfig quality = new QualityConfig();

    /**
     * 打印配置
     */
    private PrintConfig print = new PrintConfig();

    @Data
    public static class BundleConfig {
        /**
         * 默认包大小
         */
        private Integer defaultSize = 100;

        /**
         * 最小包大小
         */
        private Integer minSize = 10;

        /**
         * 最大包大小
         */
        private Integer maxSize = 500;
    }

    @Data
    public static class QualityConfig {
        /**
         * 优秀阈值
         */
        private Double excellentThreshold = 0.98;

        /**
         * 优秀系数
         */
        private Double excellentFactor = 1.05;

        /**
         * 正常阈值
         */
        private Double normalThreshold = 0.95;

        /**
         * 正常系数
         */
        private Double normalFactor = 1.00;

        /**
         * 较差阈值
         */
        private Double poorThreshold = 0.90;

        /**
         * 较差系数
         */
        private Double poorFactor = 0.95;

        /**
         * 差系数
         */
        private Double badFactor = 0.85;
    }

    @Data
    public static class PrintConfig {
        /**
         * 默认打印机端口
         */
        private Integer defaultPrinterPort = 9100;

        /**
         * 模板路径
         */
        private String templatePath = "/data/garment/templates/";

        /**
         * 输出路径
         */
        private String outputPath = "/data/garment/prints/";
    }
}
