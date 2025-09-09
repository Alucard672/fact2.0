package com.garment.basic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.garment.basic.entity.Style;
import com.garment.basic.mapper.StyleMapper;
import com.garment.basic.service.StyleService;
import com.garment.basic.dto.StyleDTO;
import com.garment.basic.dto.StyleQueryDTO;
import com.garment.common.context.TenantContext;
import com.garment.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 款式服务实现类
 *
 * @author garment
 */
@Service
@RequiredArgsConstructor
public class StyleServiceImpl extends ServiceImpl<StyleMapper, Style> implements StyleService {

    private final StyleMapper styleMapper;

    @Override
    public IPage<Style> getStylePage(StyleQueryDTO queryDTO) {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        Page<Style> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        return styleMapper.selectStylePage(page, tenantId, queryDTO.getKeyword(), queryDTO.getStatus());
    }

    @Override
    public StyleDTO getStyleById(Long id) {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        Style style = this.getOne(new QueryWrapper<Style>()
                .eq("id", id)
                .eq("tenant_id", tenantId)
                .eq("deleted", 0));

        if (style == null) {
            throw new BusinessException("款式不存在");
        }

        StyleDTO styleDTO = new StyleDTO();
        BeanUtils.copyProperties(style, styleDTO);
        return styleDTO;
    }

    @Override
    public StyleDTO createStyle(StyleDTO styleDTO) {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        // 检查款号是否已存在
        if (checkStyleCodeExists(tenantId, styleDTO.getStyleCode(), null)) {
            throw new BusinessException("款号已存在");
        }

        Style style = new Style();
        BeanUtils.copyProperties(styleDTO, style);
        style.setTenantId(tenantId);
        style.setCreatedAt(LocalDateTime.now());
        style.setUpdatedAt(LocalDateTime.now());
        style.setDeleted(false);

        this.save(style);

        StyleDTO result = new StyleDTO();
        BeanUtils.copyProperties(style, result);
        return result;
    }

    @Override
    public StyleDTO updateStyle(Long id, StyleDTO styleDTO) {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        // 检查款式是否存在
        Style existingStyle = this.getOne(new QueryWrapper<Style>()
                .eq("id", id)
                .eq("tenant_id", tenantId)
                .eq("deleted", 0));

        if (existingStyle == null) {
            throw new BusinessException("款式不存在");
        }

        // 检查款号是否已存在
        if (checkStyleCodeExists(tenantId, styleDTO.getStyleCode(), id)) {
            throw new BusinessException("款号已存在");
        }

        Style style = new Style();
        BeanUtils.copyProperties(styleDTO, style);
        style.setId(id);
        style.setTenantId(tenantId);
        style.setUpdatedAt(LocalDateTime.now());

        this.updateById(style);

        StyleDTO result = new StyleDTO();
        BeanUtils.copyProperties(style, result);
        return result;
    }

    @Override
    public boolean deleteStyle(Long id) {
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            throw new BusinessException("未获取到租户信息");
        }

        // 检查款式是否存在
        Style existingStyle = this.getOne(new QueryWrapper<Style>()
                .eq("id", id)
                .eq("tenant_id", tenantId)
                .eq("deleted", 0));

        if (existingStyle == null) {
            throw new BusinessException("款式不存在");
        }

        // 逻辑删除
        existingStyle.setDeleted(true);
        return this.updateById(existingStyle);
    }

    @Override
    public boolean checkStyleCodeExists(Long tenantId, String styleCode, Long excludeId) {
        QueryWrapper<Style> queryWrapper = new QueryWrapper<Style>()
                .eq("tenant_id", tenantId)
                .eq("style_code", styleCode)
                .eq("deleted", 0);

        if (excludeId != null) {
            queryWrapper.ne("id", excludeId);
        }

        return this.count(queryWrapper) > 0;
    }
}