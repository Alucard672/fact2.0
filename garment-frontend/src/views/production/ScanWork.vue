<template>
  <div class="scan-work-container">
    <!-- 扫码模式切换 -->
    <el-card class="mode-card">
      <div class="mode-header">
        <h3>扫码作业模式</h3>
        <el-radio-group v-model="scanMode" size="large" @change="handleModeChange">
          <el-radio-button label="take">
            <el-icon><Promotion /></el-icon>
            领工扫码
          </el-radio-button>
          <el-radio-button label="submit">
            <el-icon><SuccessFilled /></el-icon>
            收包扫码
          </el-radio-button>
          <el-radio-button label="return">
            <el-icon><CircleClose /></el-icon>
            返修扫码
          </el-radio-button>
          <el-radio-button label="query">
            <el-icon><Search /></el-icon>
            查询扫码
          </el-radio-button>
        </el-radio-group>
      </div>
    </el-card>

    <!-- 扫码输入区 -->
    <el-card class="scan-card">
      <div class="scan-input-area">
        <el-input
          v-model="scanCode"
          size="large"
          placeholder="请扫描或输入二维码/条形码"
          @keyup.enter="handleScan"
          ref="scanInputRef"
          clearable
        >
          <template #prefix>
            <el-icon><Scan /></el-icon>
          </template>
          <template #append>
            <el-button @click="handleScan">确认</el-button>
          </template>
        </el-input>
        
        <div class="scan-tips">
          <el-alert
            :title="scanTips[scanMode]"
            type="info"
            :closable="false"
          />
        </div>
      </div>

      <!-- 扫码历史 -->
      <div class="scan-history">
        <div class="history-header">
          <span>最近扫码记录</span>
          <el-button link @click="clearHistory">清空</el-button>
        </div>
        <el-timeline v-if="scanHistory.length > 0">
          <el-timeline-item
            v-for="item in scanHistory"
            :key="item.id"
            :timestamp="item.time"
            :type="item.success ? 'success' : 'danger'"
            placement="top"
          >
            <div class="history-item">
              <div class="item-code">{{ item.code }}</div>
              <div class="item-result" :class="{ success: item.success, error: !item.success }">
                {{ item.message }}
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无扫码记录" />
      </div>
    </el-card>

    <!-- 领工扫码界面 -->
    <el-card v-if="scanMode === 'take' && currentBundle" class="operation-card">
      <template #header>
        <div class="card-header">
          <h3>领工确认</h3>
          <el-tag>包号: {{ currentBundle.bundleNo }}</el-tag>
        </div>
      </template>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="款式">
          {{ currentBundle.styleName }}
        </el-descriptions-item>
        <el-descriptions-item label="颜色">
          {{ currentBundle.color }}
        </el-descriptions-item>
        <el-descriptions-item label="尺码">
          {{ currentBundle.size }}
        </el-descriptions-item>
        <el-descriptions-item label="数量">
          {{ currentBundle.quantity }}件
        </el-descriptions-item>
        <el-descriptions-item label="当前工序">
          {{ currentProcess?.processName || '裁剪' }}
        </el-descriptions-item>
        <el-descriptions-item label="工价">
          ¥{{ currentProcess?.price || '0.00' }}/件
        </el-descriptions-item>
      </el-descriptions>

      <el-form ref="takeFormRef" :model="takeForm" label-width="100px" style="margin-top: 20px;">
        <el-form-item label="领工人" prop="workerId" required>
          <el-select v-model="takeForm.workerId" placeholder="请选择领工人" filterable>
            <el-option
              v-for="worker in workerList"
              :key="worker.id"
              :label="`${worker.name} (${worker.employeeNo})`"
              :value="worker.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="工序" prop="processId" required>
          <el-select v-model="takeForm.processId" placeholder="请选择工序">
            <el-option
              v-for="process in processList"
              :key="process.id"
              :label="process.processName"
              :value="process.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="takeForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>

      <div class="operation-buttons">
        <el-button @click="cancelOperation">取消</el-button>
        <el-button type="primary" @click="confirmTake" :loading="loading">确认领工</el-button>
      </div>
    </el-card>

    <!-- 收包扫码界面 -->
    <el-card v-if="scanMode === 'submit' && currentBundle" class="operation-card">
      <template #header>
        <div class="card-header">
          <h3>收包确认</h3>
          <el-tag>包号: {{ currentBundle.bundleNo }}</el-tag>
        </div>
      </template>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="款式">
          {{ currentBundle.styleName }}
        </el-descriptions-item>
        <el-descriptions-item label="颜色">
          {{ currentBundle.color }}
        </el-descriptions-item>
        <el-descriptions-item label="尺码">
          {{ currentBundle.size }}
        </el-descriptions-item>
        <el-descriptions-item label="数量">
          {{ currentBundle.quantity }}件
        </el-descriptions-item>
        <el-descriptions-item label="领工人">
          {{ currentBundle.workerName }}
        </el-descriptions-item>
        <el-descriptions-item label="领工时间">
          {{ currentBundle.takeTime }}
        </el-descriptions-item>
        <el-descriptions-item label="当前工序">
          {{ currentBundle.processName }}
        </el-descriptions-item>
        <el-descriptions-item label="工价">
          ¥{{ currentBundle.price }}/件
        </el-descriptions-item>
      </el-descriptions>

      <el-form ref="submitFormRef" :model="submitForm" label-width="100px" style="margin-top: 20px;">
        <el-form-item label="合格数量" prop="quantityOk" required>
          <el-input-number
            v-model="submitForm.quantityOk"
            :min="0"
            :max="currentBundle.quantity"
            :step="1"
            style="width: 200px"
          />
          <span style="margin-left: 10px; color: #909399;">件</span>
        </el-form-item>
        <el-form-item label="次品数量" prop="quantityNg">
          <el-input-number
            v-model="submitForm.quantityNg"
            :min="0"
            :max="currentBundle.quantity - submitForm.quantityOk"
            :step="1"
            style="width: 200px"
          />
          <span style="margin-left: 10px; color: #909399;">件</span>
        </el-form-item>
        <el-form-item label="质量评分" prop="qualityScore">
          <el-rate v-model="submitForm.qualityScore" :max="5" show-text />
        </el-form-item>
        <el-form-item label="是否返修" prop="needRepair">
          <el-switch v-model="submitForm.needRepair" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="submitForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>

      <div class="operation-buttons">
        <el-button @click="cancelOperation">取消</el-button>
        <el-button type="primary" @click="confirmSubmit" :loading="loading">确认收包</el-button>
      </div>
    </el-card>

    <!-- 查询结果界面 -->
    <el-card v-if="scanMode === 'query' && queryResult" class="operation-card">
      <template #header>
        <div class="card-header">
          <h3>查询结果</h3>
          <el-tag>包号: {{ queryResult.bundleNo }}</el-tag>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="basic">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="款式">{{ queryResult.styleName }}</el-descriptions-item>
            <el-descriptions-item label="颜色">{{ queryResult.color }}</el-descriptions-item>
            <el-descriptions-item label="尺码">{{ queryResult.size }}</el-descriptions-item>
            <el-descriptions-item label="数量">{{ queryResult.quantity }}件</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getBundleStatusType(queryResult.status)">
                {{ getBundleStatusText(queryResult.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="完成率">
              <el-progress :percentage="queryResult.completionRate || 0" />
            </el-descriptions-item>
            <el-descriptions-item label="当前工序">{{ queryResult.currentProcess }}</el-descriptions-item>
            <el-descriptions-item label="当前工人">{{ queryResult.currentWorker }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ queryResult.createdAt }}</el-descriptions-item>
            <el-descriptions-item label="打印时间">{{ queryResult.printedAt }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
        
        <el-tab-pane label="流转记录" name="flow">
          <el-table :data="queryResult.flowRecords" style="width: 100%">
            <el-table-column prop="time" label="时间" width="160" />
            <el-table-column prop="action" label="操作" width="100">
              <template #default="{ row }">
                <el-tag :type="getActionType(row.action)" size="small">
                  {{ getActionText(row.action) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="workerName" label="操作人" width="100" />
            <el-table-column prop="processName" label="工序" width="100" />
            <el-table-column prop="quantityOk" label="合格数" width="80" />
            <el-table-column prop="quantityNg" label="次品数" width="80" />
            <el-table-column prop="remark" label="备注" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Promotion, SuccessFilled, CircleClose, Search
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'

// 扫码模式
const scanMode = ref<'take' | 'submit' | 'return' | 'query'>('take')
const scanCode = ref('')
const scanInputRef = ref()
const loading = ref(false)
const activeTab = ref('basic')

// 扫码提示
const scanTips = {
  take: '请扫描待领工的包二维码，系统将记录领工信息',
  submit: '请扫描要收包的包二维码，系统将记录完成信息',
  return: '请扫描需要返修的包二维码，系统将创建返修单',
  query: '请扫描包二维码查询详细信息和流转记录'
}

// 扫码历史
const scanHistory = ref<any[]>([])

// 当前包信息
const currentBundle = ref<any>(null)
const currentProcess = ref<any>(null)
const queryResult = ref<any>(null)

// 领工表单
const takeForm = reactive({
  workerId: null,
  processId: null,
  remark: ''
})

// 收包表单
const submitForm = reactive({
  quantityOk: 0,
  quantityNg: 0,
  qualityScore: 5,
  needRepair: false,
  remark: ''
})

// 工人列表（实际从API获取）
const workerList = ref([
  { id: 1, name: '张三', employeeNo: 'EMP001' },
  { id: 2, name: '李四', employeeNo: 'EMP002' },
  { id: 3, name: '王五', employeeNo: 'EMP003' }
])

// 工序列表（实际从API获取）
const processList = ref([
  { id: 1, processName: '裁剪', price: 0.50 },
  { id: 2, processName: '缝合', price: 1.20 },
  { id: 3, processName: '锁边', price: 0.80 },
  { id: 4, processName: '熨烫', price: 0.60 }
])

// 切换模式
const handleModeChange = () => {
  scanCode.value = ''
  currentBundle.value = null
  queryResult.value = null
  nextTick(() => {
    scanInputRef.value?.focus()
  })
}

// 扫码处理
const handleScan = async () => {
  if (!scanCode.value) {
    ElMessage.warning('请输入或扫描二维码')
    return
  }

  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 解析二维码（示例格式: BDL-{tenantId}-{cutId}-{bundleId}-{segTag}-{checksum}）
    const bundleInfo = {
      id: 1,
      bundleNo: 'BDL001',
      styleName: '经典T恤',
      color: '白色',
      size: 'M',
      quantity: 100,
      status: scanMode.value === 'take' ? 'pending' : 'in_work',
      workerName: '张三',
      takeTime: '2024-01-15 10:30:00',
      processName: '缝合',
      price: 1.20,
      currentProcess: '缝合',
      currentWorker: '张三',
      completionRate: 60,
      createdAt: '2024-01-15 08:00:00',
      printedAt: '2024-01-15 09:00:00',
      flowRecords: [
        {
          time: '2024-01-15 10:30:00',
          action: 'take',
          workerName: '张三',
          processName: '缝合',
          quantityOk: 0,
          quantityNg: 0,
          remark: ''
        },
        {
          time: '2024-01-15 09:00:00',
          action: 'take',
          workerName: '李四',
          processName: '裁剪',
          quantityOk: 100,
          quantityNg: 0,
          remark: ''
        }
      ]
    }

    // 根据扫码模式处理
    switch (scanMode.value) {
      case 'take':
        if (bundleInfo.status !== 'pending') {
          throw new Error('该包已被领工或不在待领工状态')
        }
        currentBundle.value = bundleInfo
        currentProcess.value = processList.value[0]
        break
        
      case 'submit':
        if (bundleInfo.status !== 'in_work') {
          throw new Error('该包不在生产中状态')
        }
        currentBundle.value = bundleInfo
        submitForm.quantityOk = bundleInfo.quantity
        submitForm.quantityNg = 0
        break
        
      case 'return':
        ElMessage.info('返修功能开发中')
        break
        
      case 'query':
        queryResult.value = bundleInfo
        break
    }

    // 添加到扫码历史
    scanHistory.value.unshift({
      id: Date.now(),
      code: scanCode.value,
      time: dayjs().format('HH:mm:ss'),
      success: true,
      message: `扫码成功: ${bundleInfo.bundleNo}`
    })
    
    // 清空输入
    scanCode.value = ''
  } catch (error: any) {
    ElMessage.error(error.message || '扫码失败')
    
    // 添加到扫码历史
    scanHistory.value.unshift({
      id: Date.now(),
      code: scanCode.value,
      time: dayjs().format('HH:mm:ss'),
      success: false,
      message: error.message || '扫码失败'
    })
  } finally {
    loading.value = false
    nextTick(() => {
      scanInputRef.value?.focus()
    })
  }
}

// 确认领工
const confirmTake = async () => {
  if (!takeForm.workerId) {
    ElMessage.warning('请选择领工人')
    return
  }
  if (!takeForm.processId) {
    ElMessage.warning('请选择工序')
    return
  }

  loading.value = true
  try {
    // 调用领工API
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('领工成功')
    currentBundle.value = null
    Object.assign(takeForm, {
      workerId: null,
      processId: null,
      remark: ''
    })
  } catch (error) {
    ElMessage.error('领工失败')
  } finally {
    loading.value = false
  }
}

// 确认收包
const confirmSubmit = async () => {
  if (submitForm.quantityOk + submitForm.quantityNg > currentBundle.value.quantity) {
    ElMessage.warning('合格数量与次品数量之和不能大于总数量')
    return
  }

  loading.value = true
  try {
    // 调用收包API
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('收包成功')
    
    if (submitForm.needRepair && submitForm.quantityNg > 0) {
      ElMessage.info('已自动创建返修单')
    }
    
    currentBundle.value = null
    Object.assign(submitForm, {
      quantityOk: 0,
      quantityNg: 0,
      qualityScore: 5,
      needRepair: false,
      remark: ''
    })
  } catch (error) {
    ElMessage.error('收包失败')
  } finally {
    loading.value = false
  }
}

// 取消操作
const cancelOperation = () => {
  currentBundle.value = null
  queryResult.value = null
}

// 清空历史
const clearHistory = () => {
  ElMessageBox.confirm('确定要清空扫码历史吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    scanHistory.value = []
    ElMessage.success('已清空')
  })
}

// 获取包状态类型
const getBundleStatusType = (status: string) => {
  const types: Record<string, string> = {
    pending: 'info',
    in_work: 'warning',
    completed: 'success',
    returned: 'danger'
  }
  return types[status] || 'info'
}

// 获取包状态文本
const getBundleStatusText = (status: string) => {
  const texts: Record<string, string> = {
    pending: '待领工',
    in_work: '生产中',
    completed: '已完成',
    returned: '已返修'
  }
  return texts[status] || status
}

// 获取操作类型
const getActionType = (action: string) => {
  const types: Record<string, string> = {
    take: 'primary',
    submit: 'success',
    return: 'warning',
    repair: 'danger'
  }
  return types[action] || 'info'
}

// 获取操作文本
const getActionText = (action: string) => {
  const texts: Record<string, string> = {
    take: '领工',
    submit: '收包',
    return: '退回',
    repair: '返修'
  }
  return texts[action] || action
}

// 初始化
onMounted(() => {
  nextTick(() => {
    scanInputRef.value?.focus()
  })
})
</script>

<style lang="scss" scoped>
.scan-work-container {
  .mode-card {
    margin-bottom: 20px;
    
    .mode-header {
      h3 {
        margin: 0 0 20px;
        font-size: 18px;
      }
      
      :deep(.el-radio-button) {
        .el-radio-button__inner {
          padding: 12px 20px;
          font-size: 14px;
          
          .el-icon {
            margin-right: 4px;
            vertical-align: -2px;
          }
        }
      }
    }
  }
  
  .scan-card {
    margin-bottom: 20px;
    
    .scan-input-area {
      .el-input {
        margin-bottom: 20px;
        
        :deep(.el-input__inner) {
          font-size: 16px;
          height: 50px;
          line-height: 50px;
        }
      }
      
      .scan-tips {
        margin-bottom: 20px;
      }
    }
    
    .scan-history {
      .history-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
        padding-bottom: 10px;
        border-bottom: 1px solid #ebeef5;
        
        span {
          font-size: 16px;
          font-weight: 500;
        }
      }
      
      .history-item {
        .item-code {
          font-size: 14px;
          font-weight: 500;
          margin-bottom: 4px;
        }
        
        .item-result {
          font-size: 12px;
          
          &.success {
            color: #67c23a;
          }
          
          &.error {
            color: #f56c6c;
          }
        }
      }
      
      :deep(.el-timeline) {
        max-height: 300px;
        overflow-y: auto;
        padding-left: 0;
      }
    }
  }
  
  .operation-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      h3 {
        margin: 0;
        font-size: 18px;
      }
    }
    
    .operation-buttons {
      display: flex;
      justify-content: flex-end;
      margin-top: 20px;
      padding-top: 20px;
      border-top: 1px solid #ebeef5;
      
      .el-button + .el-button {
        margin-left: 10px;
      }
    }
  }
}
</style>
