<template>
  <div class="main-layout">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside :width="collapsed ? '64px' : '240px'" class="sidebar">
        <div class="logo">
          <img src="/logo.png" alt="Logo" />
          <span v-if="!collapsed" class="title">服装生产管理</span>
        </div>
        
        <el-menu
          :default-active="activeMenu"
          :collapse="collapsed"
          :unique-opened="true"
          router
          class="sidebar-menu"
          background-color="#001529"
          text-color="#cfd8dc"
          active-text-color="#ffffff"
        >
          <template v-for="route in menuRoutes" :key="route.path">
            <el-sub-menu v-if="route.children && route.children.length > 0" :index="route.path">
              <template #title>
                <el-icon><component :is="getIcon(route.meta?.icon as string)" /></el-icon>
                <span>{{ route.meta?.title }}</span>
              </template>
              <el-menu-item
                v-for="child in route.children"
                :key="child.path"
                :index="child.path"
              >
                {{ child.meta?.title }}
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else :index="route.path">
              <el-icon><component :is="getIcon(route.meta?.icon as string)" /></el-icon>
              <template #title>{{ route.meta?.title }}</template>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>

      <el-container>
        <!-- 顶部导航 -->
        <el-header class="header">
          <div class="header-left">
            <el-button text @click="toggleCollapse">
              <el-icon><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
            </el-button>
          </div>
          
          <div class="header-right">
            <el-dropdown>
              <div class="user-info">
                <el-avatar :size="32" :src="displayUser.avatar" />
                <span class="username">{{ displayUser.username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleProfile">个人中心</el-dropdown-item>
                  <el-dropdown-item @click="handleSettings">系统设置</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 主内容区 -->
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Fold, Expand, ArrowDown, DataAnalysis, Grid, Setting, Money, DataLine, Tools, Menu as MenuIcon } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()

const collapsed = ref(false)
const userStore = useUserStore()

// 显示用用户信息（从 Pinia 获取）
const displayUser = computed(() => ({
  username: (userStore.username as unknown as string) || '管理员',
  avatar: (userStore.avatar as unknown as string) || ''
}))

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 菜单路由
const menuRoutes = [
  {
    path: '/dashboard',
    meta: { title: '工作台', icon: 'DataAnalysis' }
  },
  {
    path: '/basic',
    meta: { title: '基础资料', icon: 'Grid' },
    children: [
      { path: '/basic/styles', meta: { title: '款式管理' } },
      { path: '/basic/processes', meta: { title: '工序管理' } },
      { path: '/basic/workshops', meta: { title: '车间管理' } },
      { path: '/basic/employees', meta: { title: '员工管理' } },
      { path: '/basic/process-prices', meta: { title: '工价管理' } }
    ]
  },
  {
    path: '/production',
    meta: { title: '生产管理', icon: 'Setting' },
    children: [
      { path: '/production/cut-orders', meta: { title: '裁床订单' } },
      { path: '/production/bundles', meta: { title: '包管理' } },
      { path: '/production/print-tasks', meta: { title: '打印任务' } },
      { path: '/production/scan-work', meta: { title: '扫码作业' } },
      { path: '/production/production-flow', meta: { title: '生产流水' } },
      { path: '/production/repair-records', meta: { title: '返修记录' } }
    ]
  },
  {
    path: '/salary',
    meta: { title: '计件工资', icon: 'Money' },
    children: [
      { path: '/salary/piecework-records', meta: { title: '计件记录' } },
      { path: '/salary/payroll-periods', meta: { title: '结算管理' } }
    ]
  },
  {
    path: '/statistics',
    meta: { title: '统计分析', icon: 'DataLine' },
    children: [
      { path: '/statistics/production-stats', meta: { title: '生产统计' } },
      { path: '/statistics/workshop-ranking', meta: { title: '车间排名' } },
      { path: '/statistics/quality-analysis', meta: { title: '质量分析' } },
      { path: '/statistics/efficiency-analysis', meta: { title: '效率分析' } }
    ]
  },
  {
    path: '/system',
    meta: { title: '系统管理', icon: 'Tools' },
    children: [
      { path: '/system/tenant', meta: { title: '租户管理' } },
      { path: '/system/user-invitations', meta: { title: '用户邀请' } },
      { path: '/system/subscription', meta: { title: '订阅管理' } },
      { path: '/system/permissions', meta: { title: '权限管理' } },
      { path: '/system/settings', meta: { title: '系统设置' } }
    ]
  }
]

// 图标映射（将字符串名映射到实际图标组件）
const iconsMap: Record<string, any> = { DataAnalysis, Grid, Setting, Money, DataLine, Tools }
const getIcon = (name?: string) => (name && iconsMap[name]) || MenuIcon

// 切换侧边栏折叠
const toggleCollapse = () => {
  collapsed.value = !collapsed.value
}

// 个人中心
const handleProfile = () => {
  router.push('/profile')
}

// 系统设置
const handleSettings = () => {
  router.push('/system/settings')
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await userStore.logout()
    router.push('/login')
  } catch (error) {
    // 用户取消
  }
}

onMounted(() => {
  const hasToken = localStorage.getItem('token')
  if (hasToken) {
    userStore.getUserInfo()
  }
})
</script>

<style scoped>
.main-layout {
  height: 100vh;
}

.sidebar {
  background: #001529;
  transition: width 0.3s ease;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 0 16px;
  border-bottom: 1px solid #1f1f1f;
}

.logo img {
  width: 32px;
  height: 32px;
}

.logo .title {
  color: white;
  font-size: 16px;
  font-weight: 500;
}

.sidebar-menu {
  border-right: none;
  background: transparent;
}

/* 调高暗色侧边栏的文字可读性 */
.sidebar-menu :deep(.el-menu-item),
.sidebar-menu :deep(.el-sub-menu__title) {
  color: #cfd8dc;
  font-weight: 500;
}

.sidebar-menu :deep(.el-menu-item:hover),
.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.08);
  color: #ffffff;
}

.sidebar-menu :deep(.el-menu-item.is-active),
.sidebar-menu :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: #ffffff;
  background: rgba(64, 158, 255, 0.18);
}

/* 图标颜色继承文字颜色，保证对比度一致 */
.sidebar-menu :deep(.el-icon) {
  color: inherit;
}
.header {
  background: white;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.user-info:hover {
  background: #f5f5f5;
}

.username {
  font-size: 14px;
  color: #333;
}

.main-content {
  background: #f5f5f5;
  padding: 20px;
  overflow-y: auto;
}

/* 展开子菜单的内层菜单块底色与留白 */
.sidebar-menu :deep(.el-sub-menu.is-opened > .el-menu),
.sidebar-menu :deep(.el-menu--inline) {
  background-color: #2f343a; /* 改为中性灰，弱化蓝色调 */
  padding: 6px 0;
}

/* 悬停与激活在内层菜单依旧清晰可见 */
.sidebar-menu :deep(.el-sub-menu.is-opened > .el-menu .el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.12); /* 略调高以适配灰底 */
  color: #ffffff;
}
.sidebar-menu :deep(.el-sub-menu.is-opened > .el-menu .el-menu-item) {
  margin: 4px 8px; /* 左右留白，形成块状分组感 */
  border-radius: 6px;
}
</style>