package com.garment.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.garment.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户Mapper
 *
 * @author garment
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM users WHERE username = #{username} AND deleted = 0")
    User findByUsername(@Param("username") String username);

    /**
     * 根据手机号查询用户
     */
    @Select("SELECT * FROM users WHERE phone = #{phone} AND deleted = 0")
    User findByPhone(@Param("phone") String phone);

    /**
     * 根据微信OpenID查询用户
     */
    @Select("SELECT * FROM users WHERE wechat_openid = #{openid} AND deleted = 0")
    User findByWechatOpenid(@Param("openid") String openid);

    /**
     * 更新最后登录信息
     */
    @Update("UPDATE users SET last_login_time = NOW(), last_login_ip = #{ip}, current_tenant_id = #{tenantId} WHERE id = #{userId}")
    void updateLastLoginInfo(@Param("userId") Long userId, @Param("ip") String ip, @Param("tenantId") Long tenantId);

    /**
     * 更新用户手机号
     */
    @Update("UPDATE users SET phone = #{phone} WHERE id = #{userId}")
    void updatePhone(@Param("userId") Long userId, @Param("phone") String phone);

    /**
     * 更新用户当前租户
     */
    @Update("UPDATE users SET current_tenant_id = #{tenantId} WHERE id = #{userId}")
    void updateCurrentTenant(@Param("userId") Long userId, @Param("tenantId") Long tenantId);
}