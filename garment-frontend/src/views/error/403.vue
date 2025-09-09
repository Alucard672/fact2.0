<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-code">403</div>
      <div class="error-desc">抱歉，您没有权限访问此页面</div>
      <div class="error-action">
        <el-button type="primary" @click="router.push('/')">返回首页</el-button>
        <el-button @click="router.back()">返回上一页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style lang="scss" scoped>
.error-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ff9a56 0%, #ff6a88 100%);
  
  .error-content {
    text-align: center;
    
    .error-code {
      font-size: 120px;
      font-weight: 600;
      color: #fff;
      text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
      margin-bottom: 20px;
    }
    
    .error-desc {
      font-size: 24px;
      color: #fff;
      margin-bottom: 40px;
    }
    
    .error-action {
      .el-button + .el-button {
        margin-left: 20px;
      }
    }
  }
}
</style>




