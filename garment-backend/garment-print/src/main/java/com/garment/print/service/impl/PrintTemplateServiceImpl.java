package com.garment.print.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.garment.common.context.TenantContext;
import com.garment.print.entity.PrintTemplate;
import com.garment.print.mapper.PrintTemplateMapper;
import com.garment.print.service.PrintTemplateService;
import com.garment.print.util.PrintUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 打印模板服务实现
 *
 * @author system
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrintTemplateServiceImpl extends ServiceImpl<PrintTemplateMapper, PrintTemplate> 
        implements PrintTemplateService {

    @Override
    public PrintTemplate getDefaultTemplate(String templateType) {
        LambdaQueryWrapper<PrintTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrintTemplate::getTenantId, TenantContext.getCurrentTenantId())
               .eq(PrintTemplate::getTemplateType, templateType)
               .eq(PrintTemplate::getIsDefault, true)
               .eq(PrintTemplate::getStatus, "active")
               .orderByDesc(PrintTemplate::getCreatedAt)
               .last("LIMIT 1");
        
        return getOne(wrapper);
    }

    @Override
    public PrintTemplate createDefaultTemplate(String templateType) {
        PrintTemplate template = new PrintTemplate();
        template.setTenantId(TenantContext.getCurrentTenantId());
        template.setTemplateName("默认" + getTemplateTypeName(templateType) + "模板");
        template.setTemplateType(templateType);
        template.setTemplateContent(getDefaultTemplateContent(templateType));
        template.setTemplateStyle(getDefaultTemplateStyle(templateType));
        template.setPaperSize("80mm");
        template.setOrientation("portrait");
        template.setMargins("{\"top\":10,\"right\":10,\"bottom\":10,\"left\":10}");
        template.setIsDefault(true);
        template.setStatus("active");
        template.setRemark("系统自动创建的默认模板");
        template.setCreatedBy(1L); // TODO: 从当前用户上下文获取
        
        save(template);
        
        log.info("创建默认模板成功，类型：{}", templateType);
        return template;
    }

    /**
     * 获取模板类型名称
     */
    private String getTemplateTypeName(String templateType) {
        switch (templateType) {
            case "bundle":
                return "包菲票";
            case "qr":
                return "二维码";
            case "label":
                return "标签";
            default:
                return "未知";
        }
    }

    /**
     * 获取默认模板内容
     */
    private String getDefaultTemplateContent(String templateType) {
        switch (templateType) {
            case "bundle":
                return PrintUtil.getDefaultBundleTicketTemplate();
            case "qr":
                return getDefaultQrCodeTemplate();
            case "label":
                return getDefaultLabelTemplate();
            default:
                return "<html><body><p>默认模板</p></body></html>";
        }
    }

    /**
     * 获取默认模板样式
     */
    private String getDefaultTemplateStyle(String templateType) {
        return "body { font-family: Arial, sans-serif; font-size: 12px; margin: 0; padding: 10px; }";
    }

    /**
     * 获取默认二维码模板
     */
    private String getDefaultQrCodeTemplate() {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>二维码</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; text-align: center; margin: 0; padding: 20px; }\n" +
                "        .qr-code { margin: 20px 0; }\n" +
                "        .qr-code img { width: 120px; height: 120px; }\n" +
                "        .qr-text { font-size: 12px; margin-top: 10px; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"qr-code\">\n" +
                "        <img th:src=\"${qrCodeImage}\" alt=\"二维码\" />\n" +
                "        <div class=\"qr-text\">[[${qrCode}]]</div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * 获取默认标签模板
     */
    private String getDefaultLabelTemplate() {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>标签</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; font-size: 10px; margin: 0; padding: 5px; }\n" +
                "        .label { border: 1px solid #000; padding: 5px; }\n" +
                "        .label-text { margin: 2px 0; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"label\">\n" +
                "        <div class=\"label-text\">包号: [[${bundleNo}]]</div>\n" +
                "        <div class=\"label-text\">款式: [[${styleName}]]</div>\n" +
                "        <div class=\"label-text\">尺码: [[${size}]]</div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}




