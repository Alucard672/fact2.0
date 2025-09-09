package com.garment.print.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码工具类
 *
 * @author system
 */
@Slf4j
public class QrCodeUtil {

    private static final int DEFAULT_WIDTH = 200;
    private static final int DEFAULT_HEIGHT = 200;
    private static final String DEFAULT_FORMAT = "PNG";

    /**
     * 生成二维码图片（Base64格式）
     *
     * @param content 二维码内容
     * @return Base64编码的图片
     */
    public static String generateQrCodeBase64(String content) {
        return generateQrCodeBase64(content, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 生成二维码图片（Base64格式）
     *
     * @param content 二维码内容
     * @param width   宽度
     * @param height  高度
     * @return Base64编码的图片
     */
    public static String generateQrCodeBase64(String content, int width, int height) {
        try {
            BufferedImage image = generateQrCodeImage(content, width, height);
            return imageToBase64(image);
        } catch (Exception e) {
            log.error("生成二维码失败：{}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 生成二维码图片
     *
     * @param content 二维码内容
     * @param width   宽度
     * @param height  高度
     * @return 二维码图片
     */
    public static BufferedImage generateQrCodeImage(String content, int width, int height) throws WriterException {
        // 设置二维码参数
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 1);

        // 生成二维码矩阵
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

        // 创建图片
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        
        // 设置背景色为白色
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        
        // 设置前景色为黑色
        graphics.setColor(Color.BLACK);
        
        // 绘制二维码
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (bitMatrix.get(x, y)) {
                    graphics.fillRect(x, y, 1, 1);
                }
            }
        }
        
        graphics.dispose();
        return image;
    }

    /**
     * 图片转Base64
     *
     * @param image 图片
     * @return Base64字符串
     */
    public static String imageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, DEFAULT_FORMAT, baos);
        byte[] bytes = baos.toByteArray();
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 生成带Logo的二维码
     *
     * @param content    二维码内容
     * @param logoBase64 Logo图片Base64
     * @param width      宽度
     * @param height     高度
     * @return Base64编码的图片
     */
    public static String generateQrCodeWithLogo(String content, String logoBase64, int width, int height) {
        try {
            BufferedImage qrImage = generateQrCodeImage(content, width, height);
            
            if (logoBase64 != null && !logoBase64.isEmpty()) {
                // 解码Logo图片
                byte[] logoBytes = Base64.getDecoder().decode(logoBase64.replace("data:image/png;base64,", ""));
                BufferedImage logoImage = ImageIO.read(new java.io.ByteArrayInputStream(logoBytes));
                
                // 计算Logo大小（二维码的1/5）
                int logoSize = Math.min(width, height) / 5;
                Image scaledLogo = logoImage.getScaledInstance(logoSize, logoSize, Image.SCALE_SMOOTH);
                
                // 在二维码中央绘制Logo
                Graphics2D graphics = qrImage.createGraphics();
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int logoX = (width - logoSize) / 2;
                int logoY = (height - logoSize) / 2;
                
                // 绘制白色背景
                graphics.setColor(Color.WHITE);
                graphics.fillRoundRect(logoX - 2, logoY - 2, logoSize + 4, logoSize + 4, 8, 8);
                
                // 绘制Logo
                graphics.drawImage(scaledLogo, logoX, logoY, null);
                graphics.dispose();
            }
            
            return imageToBase64(qrImage);
        } catch (Exception e) {
            log.error("生成带Logo的二维码失败：{}", e.getMessage(), e);
            return generateQrCodeBase64(content, width, height);
        }
    }
}




