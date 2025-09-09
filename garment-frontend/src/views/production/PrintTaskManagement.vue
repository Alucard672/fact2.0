<template>
  <div class="page-container">
    <h1>{{ title }}</h1>
    <p>{{ title }}功能开发中...</p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
const title = ref('PrintTaskManagement')
</script>
