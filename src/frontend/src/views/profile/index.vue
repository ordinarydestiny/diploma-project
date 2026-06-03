<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <el-col :xs="24" :lg="8">
        <el-card class="user-card" shadow="hover">
          <div class="user-avatar-wrapper">
            <el-avatar :size="100" :src="userInfo.avatar || ''" class="user-avatar">
              {{ (userInfo.realName || userInfo.username || 'U').charAt(0) }}
            </el-avatar>
            <div class="user-basic-info">
              <h2 class="user-name">{{ userInfo.realName || userInfo.username || '访客' }}</h2>
              <el-tag :type="getRoleType(roleCode)" size="large">{{ roleLabel }}</el-tag>
            </div>
          </div>

          <el-divider />

          <div class="user-stats">
            <div class="stat-item">
              <div class="stat-number">{{ stats.topicCount }}</div>
              <div class="stat-label">选题数</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ stats.taskCount }}</div>
              <div class="stat-label">任务书</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ stats.defenseCount }}</div>
              <div class="stat-label">答辩</div>
            </div>
          </div>

          <el-divider />

          <div class="user-details">
            <div class="detail-item">
              <el-icon><User /></el-icon>
              <span class="label">账号：</span>
              <span class="value">{{ userInfo.username || '-' }}</span>
            </div>
            <div class="detail-item">
              <el-icon><Phone /></el-icon>
              <span class="label">手机：</span>
              <span class="value">{{ userInfo.phone || '未绑定' }}</span>
            </div>
            <div class="detail-item">
              <el-icon><Message /></el-icon>
              <span class="label">邮箱：</span>
              <span class="value">{{ userInfo.email || '未绑定' }}</span>
            </div>
            <div class="detail-item">
              <el-icon><OfficeBuilding /></el-icon>
              <span class="label">院系：</span>
              <span class="value">{{ userInfo.collegeName || '-' }}</span>
            </div>
            <div class="detail-item">
              <el-icon><School /></el-icon>
              <span class="label">专业：</span>
              <span class="value">{{ userInfo.majorName || '-' }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="16">
        <el-tabs v-model="activeTab" type="border-card">
          <el-tab-pane label="基本信息" name="basic">
            <el-form ref="basicFormRef" :model="basicForm" :rules="basicRules" label-width="100px" style="max-width: 600px">
              <el-form-item label="真实姓名" prop="realName">
                <el-input v-model="basicForm.realName" placeholder="请输入真实姓名" />
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="basicForm.phone" placeholder="请输入手机号" />
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="basicForm.email" placeholder="请输入邮箱" />
              </el-form-item>
              <el-form-item label="性别" prop="gender">
                <el-radio-group v-model="basicForm.gender">
                  <el-radio value="male">男</el-radio>
                  <el-radio value="female">女</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSaveBasic">保存修改</el-button>
                <el-button @click="handleResetBasic">重置</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="安全设置" name="security">
            <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="120px" style="max-width: 500px">
              <el-alert title="为了账户安全，请定期修改密码" type="info" :closable="false" show-icon style="margin-bottom: 20px" />
              <el-form-item label="当前密码" prop="oldPassword">
                <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码（至少6位）" />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleChangePassword">修改密码</el-button>
                <el-button @click="handleResetPassword">重置</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="我的消息" name="messages">
            <div class="message-list">
              <el-empty v-if="messages.length === 0" description="暂无消息" />
              <div v-for="(msg, index) in messages" :key="index" class="message-item" :class="{ unread: !msg.isRead }">
                <div class="message-header">
                  <el-tag :type="msg.type === 'system' ? 'primary' : msg.type === 'warning' ? 'danger' : 'success'" size="small">
                    {{ msg.type === 'system' ? '系统' : msg.type === 'warning' ? '警告' : '通知' }}
                  </el-tag>
                  <span class="message-time">{{ msg.time }}</span>
                </div>
                <div class="message-title">{{ msg.title }}</div>
                <div class="message-content">{{ msg.content }}</div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="操作日志" name="logs">
            <el-table :data="logs" border stripe size="small">
              <el-table-column prop="time" label="时间" width="170" align="center" />
              <el-table-column prop="action" label="操作" min-width="150" />
              <el-table-column prop="target" label="对象" min-width="150" show-overflow-tooltip />
              <el-table-column prop="ip" label="IP地址" width="140" align="center" />
              <el-table-column prop="result" label="结果" width="80" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.result === '成功' ? 'success' : 'danger'" size="small">
                    {{ row.result }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { User, Phone, Message, OfficeBuilding, School } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const activeTab = ref('basic')

const userInfo = computed(() => userStore.userInfo)
const roleCode = computed(() => userStore.roleCode)
const roleLabel = computed(() => userStore.roleLabel)

const stats = ref({
  topicCount: 1,
  taskCount: 1,
  defenseCount: 0
})

const basicFormRef = ref(null)
const basicForm = reactive({
  realName: '',
  phone: '',
  email: '',
  gender: ''
})

const basicRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

const passwordFormRef = ref(null)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const messages = ref([
  {
    type: 'system',
    isRead: false,
    time: '2026-06-02 10:30',
    title: '系统公告：毕业设计批次已开启',
    content: '2026届毕业设计选题工作已正式启动，请各位同学及时登录系统选择题目。'
  },
  {
    type: 'notice',
    isRead: true,
    time: '2026-06-01 15:20',
    title: '任务书审核通过',
    content: '您的毕业设计任务书已通过指导教师审核，请按计划开展研究工作。'
  }
])

const logs = ref([
  { time: '2026-06-02 14:30', action: '登录系统', target: '-', ip: '192.168.1.100', result: '成功' },
  { time: '2026-06-01 09:15', action: '提交任务书', target: 'task_001', ip: '192.168.1.100', result: '成功' },
  { time: '2026-05-28 16:45', action: '选择题目', target: 'topic_003', ip: '192.168.1.100', result: '成功' }
])

function getRoleType(code) {
  const map = { ADMIN: '', DEPT_ADMIN: 'success', MAJOR_DIRECTOR: 'warning', TEACHER: '', STUDENT: 'info' }
  return map[code] || 'info'
}

function handleSaveBasic() {
  basicFormRef.value?.validate((valid) => {
    if (valid) {
      ElMessage.success('个人信息保存成功')
    }
  })
}

function handleResetBasic() {
  basicFormRef.value?.resetFields()
}

function handleChangePassword() {
  passwordFormRef.value?.validate((valid) => {
    if (valid) {
      ElMessage.success('密码修改成功')
      handleResetPassword()
    }
  })
}

function handleResetPassword() {
  passwordFormRef.value?.resetFields()
}
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.user-card {
  margin-bottom: 20px;
}

.user-avatar-wrapper {
  text-align: center;
  padding: 20px 0;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  font-size: 40px;
  color: white;
  margin-bottom: 16px;
}

.user-basic-info .user-name {
  font-size: 22px;
  color: #303133;
  margin: 12px 0 8px;
}

.user-stats {
  display: flex;
  justify-content: space-around;
  text-align: center;
  padding: 16px 0;
}

.stat-item .stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
}

.stat-item .stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.user-details .detail-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.user-details .detail-item:last-child {
  border-bottom: none;
}

.detail-item .el-icon {
  color: #909399;
  margin-right: 8px;
  font-size: 16px;
}

.detail-item .label {
  color: #606266;
  min-width: 50px;
}

.detail-item .value {
  color: #303133;
  font-weight: 500;
}

.message-list {
  max-height: 500px;
  overflow-y: auto;
}

.message-item {
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: background-color 0.3s;
}

.message-item:hover {
  background-color: #f5f7fa;
}

.message-item.unread {
  background-color: #ecf5ff;
}

.message-item .message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.message-time {
  font-size: 12px;
  color: #909399;
}

.message-title {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 6px;
}

.message-content {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

@media (max-width: 992px) {
  .user-card {
    margin-bottom: 20px;
  }
}
</style>
