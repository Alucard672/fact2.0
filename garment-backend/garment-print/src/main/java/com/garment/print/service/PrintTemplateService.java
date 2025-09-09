package com.garment.print.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.garment.print.entity.PrintTemplate;

/**
 * 打印模板服务接口
 *
 * @author system
 */
public interface PrintTemplateService extends IService<PrintTemplate> {

    /**
     * 获取默认模板
     *
     * @param templateType 模板类型
     * @return 默认模板
     */
    PrintTemplate getDefaultTemplate(String templateType);

    /**
     * 创建默认模板
     *
     * @param templateType 模板类型
     * @return 默认模板
     */
    PrintTemplate createDefaultTemplate(String templateType);
}




