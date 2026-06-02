<template>
  <el-row class="login-page">
    <el-col
      :span="12"
      class="bg"
    />
    <el-col
      :span="6"
      :offset="3"
      class="form"
    >
      <el-form
        ref="formRef"
        :model="formModel"
        :rules="rules"
        size="large"
        autocomplete="off"
      >
        <el-form-item>
          <h1>毕业设计管理系统</h1>
        </el-form-item>

        <el-form-item prop="username">
          <el-input
            v-model="formModel.username"
            :prefix-icon="User"
            placeholder="请输入用户名"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="formModel.password"
            show-password
            name="password"
            :prefix-icon="Lock"
            type="password"
            placeholder="请输入密码"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item class="flex">
          <div class="flex">
            <el-checkbox v-model="rememberMe">
              记住我
            </el-checkbox>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            class="button"
            type="primary"
            :loading="loading"
            :disabled="isSubmitting"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-col>
  </el-row>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores'

/**
 *
 */
const userStore = useUserStore()
/**
 *
 */
const formRef = ref(null)
/**
 *
 */
const loading = ref(false)
/**
 *
 */
const isSubmitting = ref(false)
/**
 *
 */
const rememberMe = ref(false)

/**
 *
 */
const formModel = reactive({
  username: '',
  password: ''
})

/**
 *
 */
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 30, message: '密码长度为6-30个字符', trigger: 'blur' }
  ]
}

/**
 *
 */
const handleLogin = async () => {
  if (!formRef.value || isSubmitting.value) return
  
  try {
    await formRef.value.validate()
    
    isSubmitting.value = true
    loading.value = true
    
    await userStore.login(formModel)
    
    ElMessage.success('登录成功，正在跳转...')
    window.location.href = 'https://www.baidu.com'
  } catch (error) {
    ElMessage.error('账号或密码错误，请重试')
  } finally {
    loading.value = false
    setTimeout(() => {
      isSubmitting.value = false
    }, 1000)
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  height: 100vh;
  .bg {
    background: url('@/assets/images/login_logo.png') no-repeat 60% center / 740px auto,
      url('@/assets/images/login_bg.jpg') no-repeat center / cover;
    border-radius: 0 20px 20px 0;
  }
  .form {
    display: flex;
    flex-direction: column;
    justify-content: center;
    user-select: none;
    h1 {
      text-align: center;
      color: #333;
      margin-bottom: 50px;
      font-size: 36px;
      font-weight: 600;
      letter-spacing: 4px;
    }
    .button {
      width: 100%;
    }
    .flex {
      width: 100%;
      display: flex;
      justify-content: space-between;
    }
  }
}
</style>