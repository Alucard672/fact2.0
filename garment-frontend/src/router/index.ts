/**
 * 路由配置
 */
import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import NProgress from 'nprogress'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

// 路由配置
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录', noAuth: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Index.vue'),
        meta: { title: '工作台', icon: 'DataAnalysis' }
      },
      // 基础资料管理
      {
        path: 'basic',
        name: 'Basic',
        meta: { title: '基础资料', icon: 'Grid' },
        children: [
          {
            path: 'styles',
            name: 'StyleManagement',
            component: () => import('@/views/basic/StyleManagement.vue'),
            meta: { title: '款式管理' }
          },
          {
            path: 'processes',
            name: 'ProcessManagement',
            component: () => import('@/views/basic/ProcessManagement.vue'),
            meta: { title: '工序管理' }
          },
          {
            path: 'workshops',
            name: 'WorkshopManagement',
            component: () => import('@/views/basic/WorkshopManagement.vue'),
            meta: { title: '车间管理' }
          },
          {
            path: 'employees',
            name: 'EmployeeManagement',
            component: () => import('@/views/basic/EmployeeManagement.vue'),
            meta: { title: '员工管理' }
          },
          {
            path: 'process-prices',
            name: 'ProcessPriceManagement',
            component: () => import('@/views/basic/ProcessPriceManagement.vue'),
            meta: { title: '工价管理' }
          }
        ]
      },
      // 生产管理
      {
        path: 'production',
        name: 'Production',
        meta: { title: '生产管理', icon: 'Setting' },
        children: [
          {
            path: 'cut-orders',
            name: 'CutOrderManagement',
            component: () => import('@/views/production/CutOrderManagement.vue'),
            meta: { title: '裁床订单' }
          },
          {
            path: 'cut-orders/create',
            name: 'CreateCutOrder',
            component: () => import('@/views/production/CreateCutOrder.vue'),
            meta: { title: '新建裁床订单', hidden: true }
          },
          {
            path: 'bundles',
            name: 'BundleManagement',
            component: () => import('@/views/production/BundleManagement.vue'),
            meta: { title: '包管理' }
          },
          {
            path: 'print-tasks',
            name: 'PrintTaskManagement',
            component: () => import('@/views/production/PrintTaskManagement.vue'),
            meta: { title: '打印任务' }
          },
          {
            path: 'scan-work',
            name: 'ScanWork',
            component: () => import('@/views/production/ScanWork.vue'),
            meta: { title: '扫码作业' }
          },
          {
            path: 'production-flow',
            name: 'ProductionFlow',
            component: () => import('@/views/production/ProductionFlow.vue'),
            meta: { title: '生产流水' }
          },
          {
            path: 'repair-records',
            name: 'RepairRecords',
            component: () => import('@/views/production/RepairRecords.vue'),
            meta: { title: '返修记录' }
          }
        ]
      },
      // 计件工资
      {
        path: 'salary',
        name: 'Salary',
        meta: { title: '计件工资', icon: 'Money' },
        children: [
          {
            path: 'piecework-records',
            name: 'PieceworkRecords',
            component: () => import('@/views/salary/PieceworkRecords.vue'),
            meta: { title: '计件记录' }
          },
          {
            path: 'payroll-periods',
            name: 'PayrollPeriods',
            component: () => import('@/views/salary/PayrollPeriods.vue'),
            meta: { title: '结算管理' }
          },
          {
            path: 'payroll-details/:id',
            name: 'PayrollDetails',
            component: () => import('@/views/salary/PayrollDetails.vue'),
            meta: { title: '结算详情', hidden: true }
          }
        ]
      },
      // 统计分析
      {
        path: 'statistics',
        name: 'Statistics',
        meta: { title: '统计分析', icon: 'DataLine' },
        children: [
          {
            path: 'production-stats',
            name: 'ProductionStatistics',
            component: () => import('@/views/statistics/ProductionStatistics.vue'),
            meta: { title: '生产统计' }
          },
          {
            path: 'workshop-ranking',
            name: 'WorkshopRanking',
            component: () => import('@/views/statistics/WorkshopRanking.vue'),
            meta: { title: '车间排名' }
          },
          {
            path: 'quality-analysis',
            name: 'QualityAnalysis',
            component: () => import('@/views/statistics/QualityAnalysis.vue'),
            meta: { title: '质量分析' }
          },
          {
            path: 'efficiency-analysis',
            name: 'EfficiencyAnalysis',
            component: () => import('@/views/statistics/EfficiencyAnalysis.vue'),
            meta: { title: '效率分析' }
          }
        ]
      },
      // 系统管理
      {
        path: 'system',
        name: 'System',
        meta: { title: '系统管理', icon: 'Tools', roles: ['owner', 'admin'] },
        children: [
          {
            path: 'tenant',
            name: 'TenantManagement',
            component: () => import('@/views/system/TenantManagement.vue'),
            meta: { title: '租户管理' }
          },
          {
            path: 'user-invitations',
            name: 'UserInvitations',
            component: () => import('@/views/system/UserInvitations.vue'),
            meta: { title: '用户邀请' }
          },
          {
            path: 'subscription',
            name: 'SubscriptionManagement',
            component: () => import('@/views/system/SubscriptionManagement.vue'),
            meta: { title: '订阅管理' }
          },
          {
            path: 'permissions',
            name: 'PermissionManagement',
            component: () => import('@/views/system/PermissionManagement.vue'),
            meta: { title: '权限管理' }
          },
          {
            path: 'settings',
            name: 'SystemSettings',
            component: () => import('@/views/system/SystemSettings.vue'),
            meta: { title: '系统设置' }
          }
        ]
      },
      // 个人中心
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/Index.vue'),
        meta: { title: '个人中心', hidden: true }
      }
    ]
  },
  {
    path: '/403',
    name: '403',
    component: () => import('@/views/error/403.vue'),
    meta: { title: '无权限', noAuth: true }
  },
  {
    path: '/404',
    name: '404',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在', noAuth: true }
  },
  {
    path: '/500',
    name: '500',
    component: () => import('@/views/error/500.vue'),
    meta: { title: '服务器错误', noAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  NProgress.start()
  
  const userStore = useUserStore()
  const token = userStore.token
  const hasAuth = to.meta.noAuth || token

  // 设置页面标题
  document.title = `${to.meta.title || '服装生产管理系统'} - 服装生产管理系统`

  if (!hasAuth) {
    // 未登录且需要认证
    ElMessage.warning('请先登录')
    next(`/login?redirect=${to.path}`)
    NProgress.done()
    return
  }

  if (token && to.path === '/login') {
    // 已登录访问登录页，重定向到首页
    next('/')
    NProgress.done()
    return
  }

  // 权限检查
  const requiredRoles = to.meta.roles as string[]
  if (requiredRoles && requiredRoles.length > 0) {
    const userRoles = userStore.roles || []
    const hasRole = requiredRoles.some(role => userRoles.includes(role))
    
    if (!hasRole) {
      ElMessage.error('您没有权限访问该页面')
      next('/403')
      NProgress.done()
      return
    }
  }

  next()
})

router.afterEach(() => {
  NProgress.done()
})

export default router




