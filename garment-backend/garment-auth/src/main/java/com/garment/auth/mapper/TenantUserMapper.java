package com.garment.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.garment.auth.entity.TenantUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 租户用户关联Mapper
 *
 * @author garment
 */
@Mapper
public interface TenantUserMapper extends BaseMapper<TenantUser> {

    /**
     * 根据用户ID和租户ID查询关联关系
     */
    @Select("SELECT * FROM tenant_users WHERE user_id = #{userId} AND tenant_id = #{tenantId} AND deleted = 0")
    TenantUser findByUserIdAndTenantId(@Param("userId") Long userId, @Param("tenantId") Long tenantId);

    /**
     * 根据用户ID查询所有租户关联
     */
    @Select("SELECT tu.*, t.company_name, t.tenant_code FROM tenant_users tu " +
            "LEFT JOIN tenants t ON tu.tenant_id = t.id " +
            "WHERE tu.user_id = #{userId} AND tu.status = 'active' AND tu.deleted = 0")
    List<TenantUser> findTenantsByUserId(@Param("userId") Long userId);

    /**
     * 查询用户在指定租户的权限列表
     */
    @Select("SELECT permissions FROM tenant_users WHERE user_id = #{userId} AND tenant_id = #{tenantId} AND deleted = 0")
    String findPermissionsByUserIdAndTenantId(@Param("userId") Long userId, @Param("tenantId") Long tenantId);
}