<template>
  <div class="page-container">
    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="工号">
          <el-input v-model="searchForm.employeeNo" placeholder="请输入工号" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="车间">
          <el-select v-model="searchForm.workshopId" placeholder="请选择车间" clearable>
            <el-option
              v-for="item in workshopList"
              :key="item.id"
              :label="item.workshopName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="请选择角色" clearable>
            <el-option label="管理员" value="admin" />
            <el-option label="主管" value="manager" />
            <el-option label="组长" value="supervisor" />
            <el-option label="工人" value="worker" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="在职" value="active" />
            <el-option label="离职" value="suspended" />
            <el-option label="待入职" value="pending" />
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
          新增员工
        </el-button>
        <el-button type="success" @click="handleInvite">
          <el-icon><Message /></el-icon>
          邀请员工
        </el-button>
        <el-button type="warning" @click="handleImport">
          <el-icon><Upload /></el-icon>
          批量导入
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
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="employeeNo" label="工号" width="100" fixed="left" />
      <el-table-column prop="name" label="姓名" width="100" fixed="left">
        <template #default="{ row }">
          <div class="user-info">
            <el-avatar :size="32" :src="row.avatar">
              {{ row.name.substring(0, 1) }}
            </el-avatar>
            <span>{{ row.name }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" width="120" />
      <el-table-column prop="gender" label="性别" width="80">
        <template #default="{ row }">
          {{ row.gender === 'male' ? '男' : row.gender === 'female' ? '女' : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="workshopName" label="车间" width="100" />
      <el-table-column prop="department" label="部门" width="100" />
      <el-table-column prop="position" label="职位" width="100" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="getRoleType(row.role)">{{ getRoleText(row.role) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="今日产量" width="100" align="center">
        <template #default="{ row }">
          {{ row.todayProduction || 0 }}
        </template>
      </el-table-column>
      <el-table-column label="本月工资" width="120">
        <template #default="{ row }">
          <span style="color: #f56c6c; font-weight: 600;">
            ¥{{ row.monthSalary?.toFixed(2) || '0.00' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="joinedAt" label="入职时间" width="110" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看</el-button>
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="handleResetPwd(row)">重置密码</el-button>
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
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工号" prop="employeeNo">
              <el-input v-model="formData.employeeNo" placeholder="请输入工号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="formData.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="formData.gender">
                <el-radio label="male">男</el-radio>
                <el-radio label="female">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="车间" prop="workshopId">
              <el-select v-model="formData.workshopId" placeholder="请选择车间" style="width: 100%">
                <el-option
                  v-for="item in workshopList"
                  :key="item.id"
                  :label="item.workshopName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门" prop="department">
              <el-input v-model="formData.department" placeholder="请输入部门" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="职位" prop="position">
              <el-input v-model="formData.position" placeholder="请输入职位" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色" prop="role">
              <el-select v-model="formData.role" placeholder="请选择角色" style="width: 100%">
                <el-option label="管理员" value="admin" />
                <el-option label="主管" value="manager" />
                <el-option label="组长" value="supervisor" />
                <el-option label="工人" value="worker" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="formData.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入职时间" prop="joinedAt">
              <el-date-picker
                v-model="formData.joinedAt"
                type="date"
                placeholder="选择入职时间"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="家庭地址" prop="address">
          <el-input v-model="formData.address" placeholder="请输入家庭地址" />
        </el-form-item>
        
        <el-form-item label="紧急联系人" prop="emergencyContact">
          <el-input v-model="formData.emergencyContact" placeholder="姓名 - 电话" />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio label="pending">待入职</el-radio>
            <el-radio label="active">在职</el-radio>
            <el-radio label="suspended">离职</el-radio>
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

    <!-- 邀请员工对话框 -->
    <el-dialog
      v-model="inviteDialogVisible"
      title="邀请员工"
      width="500px"
    >
      <el-form ref="inviteFormRef" :model="inviteForm" :rules="inviteRules" label-width="100px">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="inviteForm.phone" placeholder="请输入员工手机号" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="inviteForm.name" placeholder="请输入员工姓名" />
        </el-form-item>
        <el-form-item label="工号" prop="employeeNo">
          <el-input v-model="inviteForm.employeeNo" placeholder="请输入工号（选填）" />
        </el-form-item>
        <el-form-item label="车间" prop="workshopId">
          <el-select v-model="inviteForm.workshopId" placeholder="请选择车间" style="width: 100%">
            <el-option
              v-for="item in workshopList"
              :key="item.id"
              :label="item.workshopName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="inviteForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" value="admin" />
            <el-option label="主管" value="manager" />
            <el-option label="组长" value="supervisor" />
            <el-option label="工人" value="worker" />
          </el-select>
        </el-form-item>
        <el-form-item label="邀请消息" prop="message">
          <el-input
            v-model="inviteForm.message"
            type="textarea"
            :rows="3"
            placeholder="请输入邀请消息（选填）"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="inviteDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSendInvite">发送邀请</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { Plus, Message, Upload, Download, Refresh } from '@element-plus/icons-vue'
import type { Employee } from '@/types/api'

// 搜索表单
const searchForm = reactive({
  employeeNo: '',
  name: '',
  phone: '',
  workshopId: null,
  role: '',
  status: ''
})

// 分页参数
const pageParams = reactive({
  current: 1,
  size: 20
})

// 表单数据
const formData = ref<any>({
  employeeNo: '',
  name: '',
  phone: '',
  gender: 'male',
  workshopId: null,
  department: '',
  position: '',
  role: 'worker',
  idCard: '',
  joinedAt: '',
  address: '',
  emergencyContact: '',
  status: 'active'
})

// 邀请表单
const inviteForm = ref({
  phone: '',
  name: '',
  employeeNo: '',
  workshopId: null,
  role: 'worker',
  message: ''
})

// 表单验证规则
const formRules: FormRules = {
  employeeNo: [
    { required: true, message: '请输入工号', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

const inviteRules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 状态变量
const loading = ref(false)
const dialogVisible = ref(false)
const inviteDialogVisible = ref(false)
const dialogTitle = computed(() => formData.value.id ? '编辑员工' : '新增员工')
const tableData = ref<any[]>([])
const total = ref(0)
const selectedRows = ref<any[]>([])
const formRef = ref<FormInstance>()
const inviteFormRef = ref<FormInstance>()

// 车间列表（实际从API获取）
const workshopList = ref([
  { id: 1, workshopName: '一车间' },
  { id: 2, workshopName: '二车间' },
  { id: 3, workshopName: '三车间' }
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
          employeeNo: 'EMP001',
          name: '张三',
          phone: '13800138001',
          gender: 'male',
          workshopId: 1,
          workshopName: '一车间',
          department: '生产部',
          position: '缝纫工',
          role: 'worker',
          todayProduction: 85,
          monthSalary: 5280.50,
          joinedAt: '2023-06-15',
          status: 'active'
        },
        {
          id: 2,
          employeeNo: 'EMP002',
          name: '李四',
          phone: '13800138002',
          gender: 'female',
          workshopId: 1,
          workshopName: '一车间',
          department: '生产部',
          position: '组长',
          role: 'supervisor',
          todayProduction: 120,
          monthSalary: 6850.00,
          joinedAt: '2022-03-20',
          status: 'active'
        },
        {
          id: 3,
          employeeNo: 'MGR001',
          name: '王主管',
          phone: '13800138003',
          gender: 'male',
          workshopId: 2,
          workshopName: '二车间',
          department: '管理部',
          position: '车间主管',
          role: 'manager',
          todayProduction: 0,
          monthSalary: 8500.00,
          joinedAt: '2021-01-10',
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
    employeeNo: '',
    name: '',
    phone: '',
    workshopId: null,
    role: '',
    status: ''
  })
  handleSearch()
}

// 新增
const handleAdd = () => {
  formData.value = {
    employeeNo: '',
    name: '',
    phone: '',
    gender: 'male',
    workshopId: null,
    department: '',
    position: '',
    role: 'worker',
    idCard: '',
    joinedAt: '',
    address: '',
    emergencyContact: '',
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

// 删除
const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除员工 ${row.name} 吗？`,
    '提示',
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

// 重置密码
const handleResetPwd = (row: any) => {
  ElMessageBox.confirm(
    `确定要重置员工 ${row.name} 的密码吗？重置后密码为：123456`,
    '重置密码',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 调用重置密码API
    ElMessage.success('密码重置成功')
  })
}

// 邀请员工
const handleInvite = () => {
  inviteForm.value = {
    phone: '',
    name: '',
    employeeNo: '',
    workshopId: null,
    role: 'worker',
    message: ''
  }
  inviteDialogVisible.value = true
}

// 发送邀请
const handleSendInvite = async () => {
  if (!inviteFormRef.value) return
  
  await inviteFormRef.value.validate((valid) => {
    if (valid) {
      // 调用发送邀请API
      ElMessage.success('邀请已发送')
      inviteDialogVisible.value = false
    }
  })
}

// 批量导入
const handleImport = () => {
  ElMessage.info('批量导入功能开发中')
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

// 选择变化
const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
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

// 获取角色类型
const getRoleType = (role: string) => {
  const types: Record<string, string> = {
    admin: 'danger',
    manager: 'warning',
    supervisor: 'primary',
    worker: 'info'
  }
  return types[role] || 'info'
}

// 获取角色文本
const getRoleText = (role: string) => {
  const texts: Record<string, string> = {
    admin: '管理员',
    manager: '主管',
    supervisor: '组长',
    worker: '工人'
  }
  return texts[role] || role
}

// 获取状态类型
const getStatusType = (status: string) => {
  const types: Record<string, string> = {
    pending: 'warning',
    active: 'success',
    suspended: 'info'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    pending: '待入职',
    active: '在职',
    suspended: '离职'
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
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>




