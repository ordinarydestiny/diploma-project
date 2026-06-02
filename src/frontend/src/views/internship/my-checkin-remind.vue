<template>
  <div class="page-container">
    <el-card>
      <div class="filter-bar">
        <el-select
          v-model="queryForm.status"
          placeholder="提醒状态"
          clearable
          style="width: 140px"
        >
          <el-option
            label="未读"
            :value="0"
          />
          <el-option
            label="已读"
            :value="1"
          />
        </el-select>
        <el-button
          type="success"
          :icon="Search"
          @click="handleSearch"
        >
          搜索
        </el-button>
        <el-button
          :icon="Refresh"
          @click="handleReset"
        >
          重置
        </el-button>
      </div>
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        border
        stripe
      >
        <el-table-column
          prop="missedDate"
          label="未签到日期"
          min-width="120"
        />
        <el-table-column
          prop="remindTime"
          label="提醒时间"
          min-width="170"
        />
        <el-table-column
          prop="remindType"
          label="提醒方式"
          min-width="100"
        >
          <template #default="{ row }">
            <el-tag :type="row.remindType === 'SMS' ? 'success' : 'info'">
              {{ row.remindType === 'SMS' ? '短信' : '系统通知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="remindStatus"
          label="提醒状态"
          min-width="100"
        >
          <template #default="{ row }">
            <el-tag :type="row.remindStatus === 1 ? 'success' : 'warning'">
              {{ row.remindStatus === 1 ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="studentFeedback"
          label="我的反馈"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column
          label="操作"
          width="180"
          fixed="right"
          :resizable="false"
        >
          <template #default="{ row }">
            <el-button
              v-if="row.remindStatus === 0"
              type="primary"
              link
              @click="handleMarkRead(row)"
            >
              标记已读
            </el-button>
            <el-button
              type="warning"
              link
              @click="handleFeedback(row)"
            >
              提交反馈
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="queryForm.current"
          v-model:page-size="queryForm.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="feedbackVisible"
      title="提交反馈"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="feedbackFormRef"
        :model="feedbackForm"
        :rules="feedbackRules"
        label-width="80px"
      >
        <el-form-item label="未签到日期">
          <span>{{ currentReminder?.missedDate }}</span>
        </el-form-item>
        <el-form-item
          label="反馈内容"
          prop="feedback"
        >
          <el-input
            v-model="feedbackForm.feedback"
            type="textarea"
            :rows="4"
            placeholder="请输入未签到原因或反馈说明"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="feedbackVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="feedbackLoading"
          @click="handleFeedbackSubmit"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request.js'
import { useTableResize } from '@/composables/useTableResize.js'

const tableRef = ref(null)
useTableResize(tableRef)
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

const queryForm = reactive({
  current: 1,
  size: 10,
  status: null
})

const feedbackVisible = ref(false)
const feedbackLoading = ref(false)
const feedbackFormRef = ref(null)
const currentReminder = ref(null)
const feedbackForm = reactive({
  feedback: ''
})
const feedbackRules = {
  feedback: [{ required: true, message: '请输入反馈内容', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      current: queryForm.current,
      size: queryForm.size
    }
    if (queryForm.status !== null && queryForm.status !== '') {
      params.status = queryForm.status
    }
    const res = await request.get('/internship/checkin/reminders/my', { params })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.warn('获取签到提醒记录失败', e.message)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryForm.current = 1
  fetchData()
}

function handleReset() {
  queryForm.status = null
  queryForm.current = 1
  fetchData()
}

async function handleMarkRead(row) {
  try {
    await request.put(`/internship/checkin/reminders/${row.id}/read`)
    ElMessage.success('已标记为已读')
    fetchData()
  } catch (e) {
    console.warn('标记已读失败', e.message)
  }
}

function handleFeedback(row) {
  currentReminder.value = row
  feedbackForm.feedback = row.studentFeedback || ''
  feedbackVisible.value = true
}

async function handleFeedbackSubmit() {
  const valid = await feedbackFormRef.value?.validate().catch(() => false)
  if (!valid) return

  feedbackLoading.value = true
  try {
    await request.put(`/internship/checkin/reminders/${currentReminder.value.id}/feedback`, {
      feedback: feedbackForm.feedback
    })
    ElMessage.success('反馈提交成功')
    feedbackVisible.value = false
    fetchData()
  } catch (e) {
    console.warn('提交反馈失败', e.message)
  } finally {
    feedbackLoading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
