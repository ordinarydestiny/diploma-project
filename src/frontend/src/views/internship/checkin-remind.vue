<template>
  <div class="page-container">
    <el-card>
      <div class="filter-bar">
        <el-input
          v-model="queryForm.studentName"
          placeholder="学生姓名"
          clearable
          style="width: 160px"
        />
        <el-date-picker
          v-model="queryForm.remindDate"
          type="date"
          placeholder="请选择提醒日期"
          value-format="YYYY-MM-DD"
          format="YYYY年MM月DD日"
          clearable
          style="width: 160px"
        />
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
          prop="studentName"
          label="学生姓名"
          min-width="100"
        />
        <el-table-column
          prop="studentNo"
          label="学号"
          min-width="130"
        />
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
          label="学生反馈"
          min-width="200"
          show-overflow-tooltip
          :resizable="false"
        />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request.js'
import { useUserStore } from '@/stores/user.js'
import { useTableResize } from '@/composables/useTableResize.js'

const userStore = useUserStore()
const tableRef = ref(null)
useTableResize(tableRef)

const tableData = ref([])
const total = ref(0)
const loading = ref(false)

const queryForm = reactive({
  current: 1,
  size: 10,
  studentName: '',
  remindDate: ''
})

async function fetchData() {
  loading.value = true
  try {
    const res = await request.get('/internship/checkin/reminders', {
      params: {
        current: queryForm.current,
        size: queryForm.size,
        studentName: queryForm.studentName || undefined,
        remindDate: queryForm.remindDate || undefined
      }
    })
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
  queryForm.studentName = ''
  queryForm.remindDate = ''
  queryForm.current = 1
  fetchData()
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
