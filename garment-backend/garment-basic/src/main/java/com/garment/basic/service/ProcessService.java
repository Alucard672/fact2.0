package com.garment.basic.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.garment.basic.entity.Process;
import com.garment.basic.dto.ProcessDTO;
import com.garment.basic.dto.ProcessQueryDTO;

/**
 * 工序服务接口
 *
 * @author garment
 */
public interface ProcessService extends IService<Process> {

    /**
     * 分页查询工序列表
     *
     * @param queryDTO 查询参数
     * @return 工序分页列表
     */
    IPage<Process> getProcessPage(ProcessQueryDTO queryDTO);

    /**
     * 根据ID获取工序详情
     *
     * @param id 工序ID
     * @return 工序详情
     */
    ProcessDTO getProcessById(Long id);

    /**
     * 创建工序
     *
     * @param processDTO 工序信息
     * @return 工序详情
     */
    ProcessDTO createProcess(ProcessDTO processDTO);

    /**
     * 更新工序
     *
     * @param id         工序ID
     * @param processDTO 工序信息
     * @return 工序详情
     */
    ProcessDTO updateProcess(Long id, ProcessDTO processDTO);

    /**
     * 删除工序
     *
     * @param id 工序ID
     * @return 是否删除成功
     */
    boolean deleteProcess(Long id);

    /**
     * 检查工序编码是否已存在
     *
     * @param tenantId    租户ID
     * @param processCode 工序编码
     * @param excludeId   排除的ID（更新时使用）
     * @return 是否存在
     */
    boolean checkProcessCodeExists(Long tenantId, String processCode, Long excludeId);
}