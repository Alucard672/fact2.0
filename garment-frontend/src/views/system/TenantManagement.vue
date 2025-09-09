<template>
  <div class="page-container">
    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="公司名称">
          <el-input v-model="searchForm.companyName" placeholder="请输入公司名称" clearable />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="searchForm.contactName" placeholder="请输入联系人" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.contactPhone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="套餐类型">
          <el-select v-model="searchForm.planType" placeholder="请选择套餐" clearable>
            <el-option label="免费版" value="free" />
            <el-option label="基础版" value="basic" />
            <el-option label="专业版" value="professional" />
            <el-option label="企业版" value="enterprise" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="正常" value="active" />
            <el-option label="已过期" value="expired" />
            <el-option label="已禁用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格工具栏 -->
    <div class="table-toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增租户
        </el-button>
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-button :icon="Refresh" circle @click="fetchData" />
      </div>
    </div>

    <!-- 数据表格 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      stripe
      border
    >
      <el-table-column prop="companyName" label="公司名称" min-width="200">
        <template #default="{ row }">
          <el-link type="primary" @click="handleView(row)">{{ row.companyName }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="contactName" label="联系人" width="120" />
      <el-table-column prop="contactPhone" label="手机号" width="120" />
      <el-table-column prop="email" label="邮箱" width="180" />
      <el-table-column label="套餐信息" width="150">
        <template #default="{ row }">
          <el-tag :type="getPlanType(row.planType)">{{ getPlanText(row.planType) }}</el-tag>
          <div class="expire-info" v-if="row.expireDate">
            <span :class="{ 'text-danger': isExpiringSoon(row.expireDate) }">
              {{ row.expireDate }}到期
            </span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="使用情况" width="200">
        <template #default="{ row }">
          <div class="usage-info">
            <div class="usage-item">
              <span class="label">用户数:</span>
              <span class="value">{{ row.userCount }}/{{ row.maxUsers }}</span>
            </div>
            <div class="usage-item">
              <span class="label">存储:</span>
              <span class="value">{{ row.storageUsed }}GB/{{ row.maxStorage }}GB</span>
            </div>
            <div class="usage-item">
              <span class="label">月产量:</span>
              <span class="value">{{ row.monthlyProduction }}/{{ row.maxProduction || '不限' }}</span>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="160" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看</el-button>
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="handleUpgrade(row)">升级套餐</el-button>
          <el-button link type="primary" @click="handleRenewal(row)">续费</el-button>
          <el-button link type="warning" @click="handleDisable(row)" v-if="row.status === 'active'">禁用</el-button>
          <el-button link type="success" @click="handleEnable(row)" v-if="row.status === 'disabled'">启用</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pageParams.current"
        v-model:page-size="pageParams.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="公司名称" prop="companyName">
          <el-input v-model="formData.companyName" placeholder="请输入公司名称" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="formData.contactName" placeholder="请输入联系人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="contactPhone">
          <el-input v-model="formData.contactPhone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="formData.address" placeholder="请输入公司地址" />
        </el-form-item>
        <el-form-item label="套餐类型" prop="planType">
          <el-select v-model="formData.planType" placeholder="请选择套餐" style="width: 100%" @change="onPlanChange">
            <el-option label="免费版" value="free" />
            <el-option label="基础版" value="basic" />
            <el-option label="专业版" value="professional" />
            <el-option label="企业版" value="enterprise" />
          </el-select>
        </el-form-item>
        <el-form-item label="有效期" prop="expireDate">
          <el-date-picker
            v-model="formData.expireDate"
            type="date"
            placeholder="选择到期日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
            :disabled="formData.planType === 'free'"
          />
        </el-form-item>
        <el-form-item label="最大用户数" prop="maxUsers">
          <el-input-number
            v-model="formData.maxUsers"
            :min="1"
            :max="9999"
            :disabled="formData.planType === 'free'"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="最大存储(GB)" prop="maxStorage">
          <el-input-number
            v-model="formData.maxStorage"
            :min="1"
            :max="9999"
            :disabled="formData.planType === 'free'"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="月产量限制" prop="maxProduction">
          <el-input-number
            v-model="formData.maxProduction"
            :min="0"
            placeholder="0表示不限制"
            :disabled="formData.planType === 'enterprise'"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio label="active">正常</el-radio>
            <el-radio label="disabled">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 升级套餐对话框 -->
    <el-dialog
      v-model="upgradeDialogVisible"
      title="升级套餐"
      width="600px"
    >
      <div class="upgrade-content">
        <div class="current-plan">
          <h4>当前套餐</h4>
          <el-tag :type="getPlanType(currentTenant?.planType)">
            {{ getPlanText(currentTenant?.planType) }}
          </el-tag>
        </div>
        
        <el-divider />
        
        <div class="plan-options">
          <h4>选择新套餐</h4>
          <el-radio-group v-model="upgradePlan" class="plan-list">
            <div class="plan-item" v-for="plan in availablePlans" :key="plan.value">
              <el-radio :label="plan.value">
                <div class="plan-info">
                  <div class="plan-header">
                    <span class="plan-name">{{ plan.name }}</span>
                    <span class="plan-price">¥{{ plan.price }}/月</span>
                  </div>
                  <div class="plan-features">
                    <div class="feature-item" v-for="feature in plan.features" :key="feature">
                      <el-icon><CircleCheck /></el-icon>
                      {{ feature }}
                    </div>
                  </div>
                </div>
              </el-radio>
            </div>
          </el-radio-group>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="upgradeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitUpgrade">确认升级</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { Plus, Download, Refresh, CircleCheck } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

// 搜索表单
const searchForm = reactive({
  companyName: '',
  contactName: '',
  contactPhone: '',
  planType: '',
  status: ''
})

// 分页参数
const pageParams = reactive({
  current: 1,
  size: 20
})

// 表单数据
const formData = ref<any>({
  companyName: '',
  contactName: '',
  contactPhone: '',
  email: '',
  address: '',
  planType: 'free',
  expireDate: '',
  maxUsers: 10,
  maxStorage: 10,
  maxProduction: 0,
  status: 'active'
})

const formRules: FormRules = {
  companyName: [
    { required: true, message: '请输入公司名称', trigger: 'blur' }
  ],
  contactName: [
    { required: true, message: '请输入联系人', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  planType: [
    { required: true, message: '请选择套餐类型', trigger: 'change' }
  ]
}

// 状态变量
const loading = ref(false)
const dialogVisible = ref(false)
const upgradeDialogVisible = ref(false)
const dialogTitle = computed(() => formData.value.id ? '编辑租户' : '新增租户')
const tableData = ref<any[]>([])
const total = ref(0)
const formRef = ref<FormInstance>()
const currentTenant = ref<any>()
const upgradePlan = ref('')

// 可用套餐
const availablePlans = ref([
  {
    value: 'basic',
    name: '基础版',
    price: 299,
    features: ['50个用户', '100GB存储', '月产量5万件', '基础统计报表']
  },
  {
    value: 'professional',
    name: '专业版',
    price: 899,
    features: ['200个用户', '500GB存储', '月产量20万件', '高级统计分析', '数据导出']
  },
  {
    value: 'enterprise',
    name: '企业版',
    price: 2999,
    features: ['不限用户', '2TB存储', '不限产量', '全部功能', '专属客服']
  }
])

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    // 模拟API调用
    setTimeout(() => {
      tableData.value = [
        {
          id: 1,
          companyName: '优衣服装厂',
          contactName: '张总',
          contactPhone: '13800138001',
          email: 'zhang@youyi.com',
          planType: 'professional',
          expireDate: '2024-06-30',
          maxUsers: 200,
          userCount: 156,
          maxStorage: 500,
          storageUsed: 238,
          maxProduction: 200000,
          monthlyProduction: 185600,
          createdAt: '2023-01-15 10:00:00',
          status: 'active'
        },
        {
          id: 2,
          companyName: '时尚制衣',
          contactName: '李经理',
          contactPhone: '13800138002',
          email: 'li@fashion.com',
          planType: 'basic',
          expireDate: '2024-03-15',
          maxUsers: 50,
          userCount: 35,
          maxStorage: 100,
          storageUsed: 45,
          maxProduction: 50000,
          monthlyProduction: 42300,
          createdAt: '2023-06-20 14:30:00',
          status: 'active'
        },
        {
          id: 3,
          companyName: '快服工厂',
          contactName: '王老板',
          contactPhone: '13800138003',
          email: 'wang@kuaifu.com',
          planType: 'free',
          expireDate: '',
          maxUsers: 10,
          userCount: 8,
          maxStorage: 10,
          storageUsed: 6,
          maxProduction: 10000,
          monthlyProduction: 8500,
          createdAt: '2023-09-10 09:15:00',
          status: 'active'
        }
      ]
      total.value = 3
      loading.value = false
    }, 1000)
  } catch (error) {
    console.error('获取数据失败:', error)
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pageParams.current = 1
  fetchData()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    companyName: '',
    contactName: '',
    contactPhone: '',
    planType: '',
    status: ''
  })
  handleSearch()
}

// 新增
const handleAdd = () => {
  formData.value = {
    companyName: '',
    contactName: '',
    contactPhone: '',
    email: '',
    address: '',
    planType: 'free',
    expireDate: '',
    maxUsers: 10,
    maxStorage: 10,
    maxProduction: 0,
    status: 'active'
  }
  dialogVisible.value = true
}

// 查看
const handleView = (row: any) => {
  ElMessage.info('查看详情功能开发中')
}

// 编辑
const handleEdit = (row: any) => {
  formData.value = { ...row }
  dialogVisible.value = true
}

// 升级套餐
const handleUpgrade = (row: any) => {
  currentTenant.value = row
  upgradePlan.value = ''
  upgradeDialogVisible.value = true
}

// 提交升级
const handleSubmitUpgrade = () => {
  if (!upgradePlan.value) {
    ElMessage.warning('请选择新套餐')
    return
  }
  
  ElMessageBox.confirm(
    `确认将 ${currentTenant.value.companyName} 升级到 ${getPlanText(upgradePlan.value)} 吗？`,
    '升级套餐',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 调用升级API
    ElMessage.success('套餐升级成功')
    upgradeDialogVisible.value = false
    fetchData()
  })
}

// 续费
const handleRenewal = (row: any) => {
  ElMessageBox.prompt(
    '请选择续费时长',
    '续费',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /^[1-9]\d*$/,
      inputErrorMessage: '请输入有效的月数',
      inputPlaceholder: '请输入续费月数'
    }
  ).then(({ value }) => {
    // 调用续费API
    ElMessage.success(`续费 ${value} 个月成功`)
    fetchData()
  })
}

// 禁用
const handleDisable = (row: any) => {
  ElMessageBox.confirm(
    `确定要禁用租户 ${row.companyName} 吗？禁用后该租户将无法登录系统`,
    '禁用租户',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 调用禁用API
    ElMessage.success('禁用成功')
    fetchData()
  })
}

// 启用
const handleEnable = (row: any) => {
  // 调用启用API
  ElMessage.success('启用成功')
  fetchData()
}

// 删除
const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除租户 ${row.companyName} 吗？删除后数据将无法恢复`,
    '删除租户',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 调用删除API
    ElMessage.success('删除成功')
    fetchData()
  })
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

// 套餐变化
const onPlanChange = () => {
  // 根据套餐设置默认值
  const planDefaults: Record<string, any> = {
    free: { maxUsers: 10, maxStorage: 10, maxProduction: 10000 },
    basic: { maxUsers: 50, maxStorage: 100, maxProduction: 50000 },
    professional: { maxUsers: 200, maxStorage: 500, maxProduction: 200000 },
    enterprise: { maxUsers: 9999, maxStorage: 2000, maxProduction: 0 }
  }
  
  const defaults = planDefaults[formData.value.planType]
  if (defaults) {
    Object.assign(formData.value, defaults)
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate((valid) => {
    if (valid) {
      // 调用保存API
      ElMessage.success('保存成功')
      dialogVisible.value = false
      fetchData()
    }
  })
}

// 判断是否即将过期
const isExpiringSoon = (date: string) => {
  const days = dayjs(date).diff(dayjs(), 'day')
  return days >= 0 && days <= 30
}

// 获取套餐类型
const getPlanType = (type: string) => {
  const types: Record<string, string> = {
    free: 'info',
    basic: 'warning',
    professional: 'primary',
    enterprise: 'danger'
  }
  return types[type] || 'info'
}

// 获取套餐文本
const getPlanText = (type: string) => {
  const texts: Record<string, string> = {
    free: '免费版',
    basic: '基础版',
    professional: '专业版',
    enterprise: '企业版'
  }
  return texts[type] || type
}

// 获取状态类型
const getStatusType = (status: string) => {
  const types: Record<string, string> = {
    active: 'success',
    expired: 'warning',
    disabled: 'danger'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    active: '正常',
    expired: '已过期',
    disabled: '已禁用'
  }
  return texts[status] || status
}

// 分页大小变化
const handleSizeChange = () => {
  fetchData()
}

// 页码变化
const handleCurrentChange = () => {
  fetchData()
}

// 初始化
onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
.usage-info {
  .usage-item {
    font-size: 12px;
    line-height: 20px;
    
    .label {
      color: #909399;
      margin-right: 4px;
    }
    
    .value {
      color: #303133;
    }
  }
}

.expire-info {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
  
  .text-danger {
    color: #f56c6c;
  }
}

.upgrade-content {
  .current-plan {
    h4 {
      margin: 0 0 10px;
      font-size: 14px;
      color: #303133;
    }
  }
  
  .plan-options {
    h4 {
      margin: 0 0 20px;
      font-size: 14px;
      color: #303133;
    }
    
    .plan-list {
      width: 100%;
      
      .plan-item {
        margin-bottom: 20px;
        
        :deep(.el-radio) {
          width: 100%;
          height: auto;
          
          .el-radio__input {
            margin-top: 8px;
          }
          
          .el-radio__label {
            width: calc(100% - 20px);
          }
        }
        
        .plan-info {
          width: 100%;
          padding: 12px;
          border: 1px solid #e4e7ed;
          border-radius: 4px;
          
          .plan-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 12px;
            
            .plan-name {
              font-size: 16px;
              font-weight: 600;
              color: #303133;
            }
            
            .plan-price {
              font-size: 18px;
              font-weight: 700;
              color: #f56c6c;
            }
          }
          
          .plan-features {
            .feature-item {
              display: flex;
              align-items: center;
              gap: 8px;
              font-size: 14px;
              color: #606266;
              line-height: 24px;
              
              .el-icon {
                color: #67c23a;
              }
            }
          }
        }
      }
    }
  }
}
</style>




