package com.garment.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.garment.auth.entity.Invitation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 邀请Mapper
 *
 * @author garment
 */
@Mapper
public interface InvitationMapper extends BaseMapper<Invitation> {

    /**
     * 根据邀请码查询邀请记录
     */
    @Select("SELECT * FROM invitations WHERE invitation_code = #{invitationCode}")
    Invitation findByInvitationCode(@Param("invitationCode") String invitationCode);

    /**
     * 根据租户ID和手机号查询邀请记录
     */
    @Select("SELECT * FROM invitations WHERE tenant_id = #{tenantId} AND phone = #{phone} AND status IN ('pending', 'accepted') ORDER BY created_at DESC LIMIT 1")
    Invitation findByTenantIdAndPhone(@Param("tenantId") Long tenantId, @Param("phone") String phone);

    /**
     * 查询租户的邀请列表
     */
    @Select("SELECT * FROM invitations WHERE tenant_id = #{tenantId} ORDER BY created_at DESC")
    List<Invitation> findByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 查询用户发出的邀请
     */
    @Select("SELECT * FROM invitations WHERE inviter_id = #{inviterId} ORDER BY created_at DESC")
    List<Invitation> findByInviterId(@Param("inviterId") Long inviterId);

    /**
     * 查询过期邀请
     */
    @Select("SELECT * FROM invitations WHERE status = 'pending' AND expires_at < NOW()")
    List<Invitation> findExpiredInvitations();
}