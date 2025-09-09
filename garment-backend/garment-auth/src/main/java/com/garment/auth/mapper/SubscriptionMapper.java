package com.garment.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.garment.auth.entity.Subscription;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订阅Mapper
 *
 * @author garment
 */
@Mapper
public interface SubscriptionMapper extends BaseMapper<Subscription> {

    /**
     * 根据租户ID查询当前有效订阅
     */
    @Select("SELECT * FROM subscriptions WHERE tenant_id = #{tenantId} AND status = 'active' AND deleted = 0 ORDER BY end_date DESC LIMIT 1")
    Subscription findCurrentSubscription(@Param("tenantId") Long tenantId);

    /**
     * 根据租户ID查询所有订阅记录
     */
    @Select("SELECT * FROM subscriptions WHERE tenant_id = #{tenantId} AND deleted = 0 ORDER BY created_at DESC")
    List<Subscription> findByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据支付订单号查询订阅
     */
    @Select("SELECT * FROM subscriptions WHERE payment_order_no = #{orderNo} AND deleted = 0")
    Subscription findByPaymentOrderNo(@Param("orderNo") String orderNo);

    /**
     * 查询即将过期的订阅（7天内过期）
     */
    @Select("SELECT * FROM subscriptions WHERE status = 'active' AND end_date BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 7 DAY) AND deleted = 0")
    List<Subscription> findExpiringSubscriptions();

    /**
     * 查询已过期的订阅
     */
    @Select("SELECT * FROM subscriptions WHERE status = 'active' AND end_date < NOW() AND deleted = 0")
    List<Subscription> findExpiredSubscriptions();
}