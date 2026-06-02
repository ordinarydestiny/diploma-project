<template>
  <div class="page-container">
    <el-card>
      <div class="filter-bar">
        <el-select
          v-model="selectedSemesterId"
          placeholder="请选择学期"
          style="width: 240px"
          @change="handleSemesterChange"
        >
          <el-option
            v-for="item in semesterOptions"
            :key="item.id"
            :label="item.semesterName"
            :value="item.id"
          />
        </el-select>
        <div v-if="currentSemester" class="semester-info">
          <span>本学期开始时间: <span class="date-highlight">{{ formatDate(currentSemester.startDate) }}</span></span>
          <span>本学期结束时间: <span class="date-highlight">{{ formatDate(currentSemester.endDate) }}</span></span>
        </div>
      </div>

      <el-alert
        type="warning"
        show-icon
        :closable="false"
        class="warning-alert"
      >
        点击日期可设置校历事件。默认周一至周五为工作日，周六周日为休息日。只需设置例外情况（法定节假日、调休补班等）。
      </el-alert>

      <div class="calendar-wrapper">
        <el-calendar v-model="currentDate" v-loading="loading">
          <template #date-cell="{ data }">
            <div
              :class="['calendar-cell', { 'out-of-semester': isOutOfSemester(data.day) }]"
              @click="handleDateClick(data.day)"
            >
              <div class="cell-header">
                <span :class="['calendar-day', { 'is-today': isToday(data.day) }]">
                  {{ parseInt(data.day.split('-')[2]) }}
                </span>
                <el-tag
                  v-if="!isOutOfSemester(data.day)"
                  :type="isWorkDay(data.day) ? 'success' : 'info'"
                  size="small"
                  class="status-tag"
                >
                  {{ isWorkDay(data.day) ? '班' : '休' }}
                </el-tag>
              </div>
              <div v-if="getEventForDate(data.day)" class="cell-content">
                <div
                  v-if="getEventForDate(data.day).eventType === 'WORK'"
                  class="event-label makeup-work"
                >
                  补班
                </div>
                <div
                  v-else
                  class="event-label holiday"
                >
                  {{ getEventForDate(data.day).eventName }}
                </div>
              </div>
            </div>
          </template>
        </el-calendar>

        <div class="statistics-panel">
          <div class="stat-item">
            <div class="stat-label">工作日统计</div>
            <div class="stat-value">{{ statistics.workDays }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">休息日统计</div>
            <div class="stat-value">{{ statistics.restDays }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">法定节假日</div>
            <div class="stat-value">{{ statistics.holidayCount }}</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">调休补班</div>
            <div class="stat-value">{{ statistics.makeupWorkCount }}</div>
          </div>
        </div>
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="460px"
      destroy-on-close
    >
      <div class="dialog-date-display">
        {{ dialogDate }}
        <el-tag :type="isWorkDay(dialogDate) ? 'success' : 'info'" size="small">
          {{ isWorkDay(dialogDate) ? '工作日' : '休息日' }}
        </el-tag>
      </div>
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="90px"
      >
        <el-form-item label="日期状态" prop="eventType">
          <el-radio-group v-model="form.eventType">
            <el-radio value="DEFAULT">默认(按星期)</el-radio>
            <el-radio value="HOLIDAY">法定节假日</el-radio>
            <el-radio value="REST">调休休息</el-radio>
            <el-radio value="WORK">调休补班</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="form.eventType !== 'DEFAULT'"
          label="事件名称"
          prop="eventName"
        >
          <el-input
            v-model="form.eventName"
            placeholder="请输入事件名称"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button
          v-if="existingEvent"
          type="danger"
          @click="handleDeleteEvent"
        >
          删除事件
        </el-button>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="submitLoading"
          @click="handleSubmit"
        >
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request.js'
import { formatDate } from '@/utils/dateFormat'

const route = useRoute()

const selectedSemesterId = ref(null)
const semesterOptions = ref([])
const currentDate = ref(new Date())
const eventsMap = ref({})
const loading = ref(false)

const statistics = reactive({
  workDays: 0,
  restDays: 0,
  holidayCount: 0,
  makeupWorkCount: 0
})

const dialogVisible = ref(false)
const dialogDate = ref('')
const existingEvent = ref(null)
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  eventType: 'DEFAULT',
  eventName: ''
})

const dialogTitle = computed(() => {
  return existingEvent.value ? '编辑校历事件' : '设置校历事件'
})

const formRules = computed(() => ({
  eventType: [
    { required: true, message: '请选择日期状态', trigger: 'change' }
  ],
  eventName: form.eventType !== 'DEFAULT'
    ? [{ required: true, message: '请输入事件名称', trigger: 'blur' }]
    : []
}))

const currentSemester = computed(() => {
  if (!selectedSemesterId.value) return null
  return semesterOptions.value.find(s => s.id === selectedSemesterId.value) || null
})

const navigateToSemester = () => {
  if (currentSemester.value && currentSemester.value.startDate) {
    const [y, m] = currentSemester.value.startDate.split('-')
    currentDate.value = new Date(Number(y), Number(m) - 1, 1)
  }
}

const fetchSemesterOptions = async () => {
  try {
    const res = await request.get('/admin/semesters', {
      params: { size: 100 }
    })
    semesterOptions.value = res.data.records || res.data || []
    const querySemesterId = route.query.semesterId
    if (querySemesterId) {
      const matched = semesterOptions.value.find(s => String(s.id) === String(querySemesterId))
      if (matched) {
        selectedSemesterId.value = matched.id
      }
    }
    if (!selectedSemesterId.value && semesterOptions.value.length > 0) {
      selectedSemesterId.value = semesterOptions.value[0].id
    }
    if (selectedSemesterId.value) {
      navigateToSemester()
      fetchEvents()
    }
  } catch (e) {
    console.warn('获取学期列表失败', e.message)
  }
}

const fetchEvents = async () => {
  if (!selectedSemesterId.value) return
  loading.value = true
  try {
    const res = await request.get(`/admin/calendar/${selectedSemesterId.value}/events`)
    const map = {}
    const events = res.data || []
    for (const ev of events) {
      map[ev.eventDate] = ev
    }
    eventsMap.value = map
    fetchStatistics()
  } catch (e) {
    console.warn('获取校历事件失败', e.message)
  } finally {
    loading.value = false
  }
}

const fetchStatistics = async () => {
  if (!selectedSemesterId.value) return
  try {
    const res = await request.get(`/admin/calendar/${selectedSemesterId.value}/statistics`)
    const data = res.data || {}
    statistics.workDays = data.workDays ?? 0
    statistics.restDays = data.restDays ?? 0
    statistics.holidayCount = data.holidayCount ?? 0
    statistics.makeupWorkCount = data.makeupWorkCount ?? 0
  } catch (e) {
    console.warn('获取统计信息失败', e.message)
  }
}

const isOutOfSemester = (dayStr) => {
  if (!currentSemester.value) return false
  return dayStr < currentSemester.value.startDate || dayStr > currentSemester.value.endDate
}

const isToday = (dayStr) => {
  const today = new Date()
  const year = today.getFullYear()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  return dayStr === `${year}-${month}-${day}`
}

const isWorkDay = (dayStr) => {
  const ev = eventsMap.value[dayStr]
  if (ev) {
    if (ev.eventType === 'HOLIDAY' || ev.eventType === 'REST') return false
    if (ev.eventType === 'WORK') return true
  }
  const parts = dayStr.split('-')
  if (parts.length === 3) {
    const date = new Date(Number(parts[0]), Number(parts[1]) - 1, Number(parts[2]))
    const dow = date.getDay()
    return dow >= 1 && dow <= 5
  }
  return true
}

const getEventForDate = (dayStr) => {
  return eventsMap.value[dayStr] || null
}

const handleSemesterChange = () => {
  navigateToSemester()
  fetchEvents()
}

const handleDateClick = (dayStr) => {
  if (isOutOfSemester(dayStr)) return
  dialogDate.value = dayStr
  const ev = eventsMap.value[dayStr]
  existingEvent.value = ev || null
  if (ev) {
    form.eventType = ev.eventType
    form.eventName = ev.eventName || ''
  } else {
    form.eventType = 'DEFAULT'
    form.eventName = ''
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (form.eventType === 'DEFAULT') {
      if (existingEvent.value) {
        await request.delete(`/admin/calendar/${selectedSemesterId.value}/events/${existingEvent.value.id}`)
        ElMessage.success('已恢复为默认状态')
      }
    } else if (existingEvent.value) {
      await request.put(`/admin/calendar/${selectedSemesterId.value}/events/${existingEvent.value.id}`, {
        eventDate: dialogDate.value,
        eventType: form.eventType,
        eventName: form.eventName
      })
      ElMessage.success('更新成功')
    } else {
      await request.post(`/admin/calendar/${selectedSemesterId.value}/events`, {
        eventDate: dialogDate.value,
        eventType: form.eventType,
        eventName: form.eventName
      })
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchEvents()
  } catch (e) {
    console.warn('保存校历事件失败', e.message)
  } finally {
    submitLoading.value = false
  }
}

const handleDeleteEvent = async () => {
  if (!existingEvent.value) return
  submitLoading.value = true
  try {
    await request.delete(`/admin/calendar/${selectedSemesterId.value}/events/${existingEvent.value.id}`)
    ElMessage.success('删除成功')
    dialogVisible.value = false
    fetchEvents()
  } catch (e) {
    console.warn('删除校历事件失败', e.message)
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchSemesterOptions()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.semester-info {
  display: flex;
  gap: 24px;
  font-size: 14px;
}

.date-highlight {
  color: #F56C6C;
  font-weight: 500;
}

.warning-alert {
  margin-bottom: 16px;
}

.calendar-wrapper {
  display: flex;
  gap: 20px;
}

.calendar-wrapper :deep(.el-calendar) {
  flex: 1;
  min-width: 0;
}

.calendar-cell {
  height: 100%;
  cursor: pointer;
  padding: 4px;
}

.calendar-cell.out-of-semester {
  opacity: 0.4;
  cursor: not-allowed;
}

.cell-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.calendar-day {
  font-size: 14px;
  font-weight: 500;
}

.calendar-day.is-today {
  color: #409EFF;
  font-weight: 700;
}

.status-tag {
  font-size: 11px;
}

.cell-content {
  margin-top: 2px;
}

.event-label {
  font-size: 11px;
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.event-label.holiday {
  color: #67C23A;
}

.event-label.makeup-work {
  color: #E6A23C;
}

.statistics-panel {
  width: 180px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 20px 16px;
  border: 1px solid #EBEEF5;
  border-radius: 4px;
  background: #FAFAFA;
  align-self: flex-start;
  position: sticky;
  top: 20px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.dialog-date-display {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 20px;
  color: #303133;
}
</style>
