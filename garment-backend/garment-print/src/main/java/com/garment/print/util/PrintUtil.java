package com.garment.print.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

/**
 * 打印工具类
 *
 * @author system
 */
@Slf4j
public class PrintUtil {

    private static final TemplateEngine templateEngine;

    static {
        // 初始化模板引擎
        templateEngine = new SpringTemplateEngine();
        StringTemplateResolver templateResolver = new StringTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateEngine.setTemplateResolver(templateResolver);
    }

    /**
     * 渲染模板
     *
     * @param template 模板内容
     * @param data     数据
     * @return 渲染后的HTML
     */
    public static String renderTemplate(String template, Map<String, Object> data) {
        try {
            Context context = new Context();
            context.setVariables(data);
            return templateEngine.process(template, context);
        } catch (Exception e) {
            log.error("模板渲染失败：{}", e.getMessage(), e);
            return template;
        }
    }

    /**
     * 检查打印机是否在线
     *
     * @param printerIp 打印机IP
     * @param port      端口（默认9100）
     * @return 是否在线
     */
    public static boolean isPrinterOnline(String printerIp, int port) {
        if (StrUtil.isBlank(printerIp)) {
            return false;
        }

        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress(printerIp, port), 3000);
            return true;
        } catch (IOException e) {
            log.debug("打印机{}:{}不在线：{}", printerIp, port, e.getMessage());
            return false;
        }
    }

    /**
     * 检查打印机是否在线（默认端口9100）
     *
     * @param printerIp 打印机IP
     * @return 是否在线
     */
    public static boolean isPrinterOnline(String printerIp) {
        return isPrinterOnline(printerIp, 9100);
    }

    /**
     * 发送打印命令到网络打印机
     *
     * @param printerIp   打印机IP
     * @param port        端口
     * @param printData   打印数据
     * @param timeout     超时时间（毫秒）
     * @return 是否成功
     */
    public static boolean sendToPrinter(String printerIp, int port, byte[] printData, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress(printerIp, port), timeout);
            socket.getOutputStream().write(printData);
            socket.getOutputStream().flush();
            return true;
        } catch (IOException e) {
            log.error("发送打印数据失败：{}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 发送打印命令到网络打印机（默认参数）
     *
     * @param printerIp 打印机IP
     * @param printData 打印数据
     * @return 是否成功
     */
    public static boolean sendToPrinter(String printerIp, byte[] printData) {
        return sendToPrinter(printerIp, 9100, printData, 5000);
    }

    /**
     * 生成ESC/POS打印命令
     *
     * @param text 打印文本
     * @return 打印命令
     */
    public static byte[] generateEscPosCommand(String text) {
        StringBuilder sb = new StringBuilder();
        
        // ESC/POS命令
        sb.append("\u001B@"); // 初始化打印机
        sb.append("\u001Ba\u0001"); // 居中对齐
        sb.append(text);
        sb.append("\n\n\n"); // 换行
        sb.append("\u001Bm"); // 切纸
        
        return sb.toString().getBytes();
    }

    /**
     * 生成默认的包菲票模板
     *
     * @return 模板HTML
     */
    public static String getDefaultBundleTicketTemplate() {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>包菲票</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; font-size: 12px; margin: 0; padding: 10px; }\n" +
                "        .ticket { border: 2px solid #000; padding: 10px; width: 80mm; }\n" +
                "        .header { text-align: center; font-weight: bold; font-size: 16px; margin-bottom: 10px; }\n" +
                "        .row { display: flex; justify-content: space-between; margin-bottom: 5px; }\n" +
                "        .label { font-weight: bold; }\n" +
                "        .qr-code { text-align: center; margin: 10px 0; }\n" +
                "        .qr-code img { width: 60px; height: 60px; }\n" +
                "        .footer { text-align: center; font-size: 10px; margin-top: 10px; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"ticket\">\n" +
                "        <div class=\"header\">[[${companyName}]]</div>\n" +
                "        <div class=\"row\">\n" +
                "            <span class=\"label\">包号:</span>\n" +
                "            <span>[[${bundleNo}]]</span>\n" +
                "        </div>\n" +
                "        <div class=\"row\">\n" +
                "            <span class=\"label\">订单:</span>\n" +
                "            <span>[[${orderNo}]]</span>\n" +
                "        </div>\n" +
                "        <div class=\"row\">\n" +
                "            <span class=\"label\">款式:</span>\n" +
                "            <span>[[${styleCode}]] - [[${styleName}]]</span>\n" +
                "        </div>\n" +
                "        <div class=\"row\">\n" +
                "            <span class=\"label\">颜色:</span>\n" +
                "            <span>[[${color}]]</span>\n" +
                "        </div>\n" +
                "        <div class=\"row\">\n" +
                "            <span class=\"label\">尺码:</span>\n" +
                "            <span>[[${size}]]</span>\n" +
                "        </div>\n" +
                "        <div class=\"row\">\n" +
                "            <span class=\"label\">数量:</span>\n" +
                "            <span>[[${quantity}]]件</span>\n" +
                "        </div>\n" +
                "        <div class=\"row\" th:if=\"${segmentTag}\">\n" +
                "            <span class=\"label\">层段:</span>\n" +
                "            <span>[[${segmentTag}]] ([[${layerFrom}]]-[[${layerTo}]]层)</span>\n" +
                "        </div>\n" +
                "        <div class=\"qr-code\">\n" +
                "            <img th:src=\"${qrCodeImage}\" alt=\"二维码\" />\n" +
                "            <div>[[${qrCode}]]</div>\n" +
                "        </div>\n" +
                "        <div class=\"footer\">\n" +
                "            <div>打印时间: [[${printTime}]]</div>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * 格式化打印时间
     *
     * @return 格式化后的时间字符串
     */
    public static String formatPrintTime() {
        return DateUtil.now();
    }
}




