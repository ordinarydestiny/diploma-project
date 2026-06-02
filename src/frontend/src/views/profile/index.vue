<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">账号基本信息</span>
        </div>
      </template>
      <div
        v-loading="loading"
        class="info-grid"
      >
        <div class="info-item">
          <span class="info-label">用户名</span>
          <span class="info-value">{{ profile.username || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">真实姓名</span>
          <span class="info-value">{{ profile.realName || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">手机号</span>
          <span class="info-value">{{ profile.phone || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">邮箱</span>
          <span class="info-value">{{ profile.email || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">当前角色</span>
          <span class="info-value">
            <el-tag type="primary">{{ profile.currentRoleName || '-' }}</el-tag>
          </span>
        </div>
        <div class="info-item">
          <span class="info-label">注册时间</span>
          <span class="info-value">{{ formatDateTime(profile.createTime) }}</span>
        </div>
      </div>
    </el-card>

    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">身份信息</span>
        </div>
      </template>
      <div
        v-loading="loading"
        class="identities-section"
      >
        <div
          v-for="identity in profile.identities"
          :key="identity.roleCode"
          class="identity-block"
        >
          <div class="identity-header">
            <el-tag
              :type="getRoleTagType(identity.roleCode)"
              size="large"
            >
              {{ identity.roleName }}
            </el-tag>
            <span
              v-if="identity.roleCode === profile.currentRoleCode"
              class="current-badge"
            >当前</span>
          </div>
          <div class="identity-detail">
            <template v-if="identity.roleCode === 'STUDENT'">
              <div class="info-item">
                <span class="info-label">姓名</span>
                <span class="info-value">{{ identity.studentName || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">学号</span>
                <span class="info-value">{{ identity.studentNo || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">性别</span>
                <span class="info-value">{{ identity.gender || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">年级</span>
                <span class="info-value">{{ identity.gradeName || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">院系</span>
                <span class="info-value">{{ identity.deptName || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">专业</span>
                <span class="info-value">{{ identity.majorName || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">班级</span>
                <span class="info-value">{{ identity.className || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">层次</span>
                <span class="info-value">{{ identity.educationLevel || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">学制(年)</span>
                <span class="info-value">{{ identity.schoolingYear || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">电话</span>
                <span class="info-value">{{ identity.studentPhone || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">是否免实习</span>
                <span class="info-value">
                  <el-tag :type="identity.exemptInternship === 1 ? 'danger' : 'info'">
                    {{ identity.exemptInternship === 1 ? '是' : '否' }}
                  </el-tag>
                </span>
              </div>
            </template>

            <template v-else-if="identity.roleCode === 'TEACHER'">
              <div class="info-item">
                <span class="info-label">工号</span>
                <span class="info-value">{{ identity.teacherNo || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">老师姓名</span>
                <span class="info-value">{{ identity.teacherName || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">老师类型</span>
                <span class="info-value">{{ formatTeacherType(identity.teacherType) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">部门</span>
                <span class="info-value">{{ identity.deptName || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">手机号码</span>
                <span class="info-value">{{ identity.teacherPhone || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">状态</span>
                <span class="info-value">{{ formatTeacherStatus(identity.teacherStatus) }}</span>
              </div>
            </template>

            <template v-else-if="identity.roleCode === 'MAJOR_DIRECTOR'">
              <div class="info-item">
                <span class="info-label">工号</span>
                <span class="info-value">{{ identity.teacherNo || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">老师姓名</span>
                <span class="info-value">{{ identity.teacherName || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">老师类型</span>
                <span class="info-value">{{ formatTeacherType(identity.teacherType) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">部门</span>
                <span class="info-value">{{ identity.deptName || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">负责专业</span>
                <span class="info-value">
                  <template v-if="identity.majorNames && identity.majorNames.length">
                    <el-tag
                      v-for="name in identity.majorNames"
                      :key="name"
                      size="small"
                      class="major-tag"
                    >{{ name }}</el-tag>
                  </template>
                  <template v-else>-</template>
                </span>
              </div>
              <div class="info-item">
                <span class="info-label">手机号码</span>
                <span class="info-value">{{ identity.teacherPhone || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">状态</span>
                <span class="info-value">{{ formatTeacherStatus(identity.teacherStatus) }}</span>
              </div>
            </template>

            <template v-else-if="identity.roleCode === 'DEPT_ADMIN'">
              <div class="info-item">
                <span class="info-label">工号</span>
                <span class="info-value">{{ identity.teacherNo || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">老师姓名</span>
                <span class="info-value">{{ identity.teacherName || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">老师类型</span>
                <span class="info-value">{{ formatTeacherType(identity.teacherType) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">部门</span>
                <span class="info-value">{{ identity.deptName || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">手机号码</span>
                <span class="info-value">{{ identity.teacherPhone || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">状态</span>
                <span class="info-value">{{ formatTeacherStatus(identity.teacherStatus) }}</span>
              </div>
            </template>

            <template v-else>
              <div class="info-item">
                <span class="info-label">角色</span>
                <span class="info-value">{{ identity.roleName }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">说明</span>
                <span class="info-value">系统管理员，拥有最高权限</span>
              </div>
            </template>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { formatDateTime } from '@/utils/dateFormat'
import request from '@/utils/request.js'

const loading = ref(false)
const profile = reactive({
  userId: null,
  username: '',
  realName: '',
  phone: '',
  email: '',
  currentRoleCode: '',
  currentRoleName: '',
  createTime: null,
  identities: []
})

async function fetchProfile() {
  loading.value = true
  try {
    const res = await request.get('/users/profile')
    const data = res.data
    Object.assign(profile, data)
  } catch (e) {
    console.warn('获取用户资料失败', e.message)
  } finally {
    loading.value = false
  }
}

function getRoleTagType(roleCode) {
  const map = {
    ADMIN: 'danger',
    DEPT_ADMIN: 'warning',
    MAJOR_DIRECTOR: 'success',
    TEACHER: '',
    STUDENT: 'info'
  }
  return map[roleCode] || ''
}

function formatTeacherType(type) {
  if (type == null) return '-'
  const map = { 1: '校内专任教师', 2: '校外兼职教师', 3: '行政人员', 4: '实习指导教师' }
  return map[type] || '-'
}

function formatTeacherStatus(status) {
  if (status == null) return '-'
  const map = { 0: '禁用', 1: '正常' }
  return map[status] || '-'
}

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.profile-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px 40px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 13px;
  color: #909399;
}

.info-value {
  font-size: 14px;
  color: #303133;
}

.identities-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.identity-block {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 20px;
  background: #fafafa;
}

.identity-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.current-badge {
  font-size: 12px;
  color: #409eff;
  background: #ecf5ff;
  padding: 2px 8px;
  border-radius: 4px;
}

.identity-detail {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px 32px;
}

.major-tag {
  margin-right: 4px;
  margin-bottom: 4px;
}
</style>
