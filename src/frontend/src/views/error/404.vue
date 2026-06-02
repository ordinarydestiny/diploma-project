<template>
  <div class="not-found-container">
    <el-card>
      <div class="not-found-content">
        <h1>404</h1>
        <p>页面不存在</p>
        <el-button
          type="primary"
          @click="goHome"
        >
          返回首页
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

/** 路由实例 */
const router = useRouter()

/** 返回首页 */
const goHome = () => {
  router.push('/home')
}
</script>

<style scoped>
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.not-found-content {
  text-align: center;
  padding: 40px;
}

.not-found-content h1 {
  font-size: 72px;
  color: #409eff;
  margin: 0 0 10px 0;
}

.not-found-content p {
  font-size: 18px;
  color: #666;
  margin-bottom: 30px;
}
</style>
