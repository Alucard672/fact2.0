package com.garment.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.garment.auth.entity.Tenant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 租户Mapper
 *
 * @author garment
 */
@Mapper
public interface TenantMapper extends BaseMapper<Tenant> {

    /**
     * 根据租户编码查询租户
     */
    @Select("SELECT * FROM tenants WHERE tenant_code = #{tenantCode} AND deleted = 0")
    Tenant findByTenantCode(@Param("tenantCode") String tenantCode);
}