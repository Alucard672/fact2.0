package com.garment.production.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.garment.production.dto.CreateCutOrderRequest;
import com.garment.production.entity.CutOrder;

/**
 * 裁床订单服务接口
 *
 * @author system
 */
public interface CutOrderService extends IService<CutOrder> {

    /**
     * 创建裁床订单
     *
     * @param request 创建请求
     * @return 裁床订单
     */
    CutOrder createCutOrder(CreateCutOrderRequest request);

    /**
     * 确认裁床订单
     *
     * @param id 订单ID
     * @return 是否成功
     */
    boolean confirmCutOrder(Long id);

    /**
     * 取消裁床订单
     *
     * @param id 订单ID
     * @return 是否成功
     */
    boolean cancelCutOrder(Long id);

    /**
     * 还原裁床订单为草稿状态
     * 可选是否删除已生成的包
     *
     * @param id 裁床订单ID
     * @param removeBundles 是否删除已生成包
     * @return 是否成功
     */
    boolean restoreCutOrder(Long id, boolean removeBundles);

    /**
     * 分页查询裁床订单
     *
     * @param page 分页参数
     * @param orderNo 订单号
     * @param styleId 款式ID
     * @param status 状态
     * @param priority 优先级
     * @return 分页结果
     */
    Page<CutOrder> pageCutOrders(Page<CutOrder> page, String orderNo, Long styleId, String status, String priority);

    /**
     * 生成订单号
     *
     * @return 订单号
     */
    String generateOrderNo();
}




