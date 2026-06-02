<template>
  <div class="page-container">
    <el-card>
      <div class="filter-bar">
        <el-select
          v-model="queryForm.semesterId"
          placeholder="请选择学期"
          clearable
          style="width: 180px"
          @change="handleSemesterChange"
        >
          <el-option
            v-for="item in semesterOptions"
            :key="item.id"
            :label="item.semesterName"
            :value="item.id"
          />
        </el-select>
        <el-select
          v-model="queryForm.deptId"
          placeholder="院系"
          clearable
          style="width: 160px"
          @change="handleDeptChange"
        >
          <el-option
            v-for="item in filteredDepts"
            :key="item.id"
            :label="item.deptName"
            :value="item.id"
          />
        </el-select>
        <el-select
          v-model="queryForm.majorId"
          placeholder="专业"
          clearable
          style="width: 160px"
          @change="handleMajorChange"
        >
          <el-option
            v-for="item in filteredMajors"
            :key="item.id"
            :label="item.majorName"
            :value="item.id"
          />
        </el-select>
        <el-select
          v-model="queryForm.classId"
          placeholder="班级"
          clearable
          style="width: 160px"
          @change="handleClassChange"
        >
          <el-option
            v-for="item in filteredClasses"
            :key="item.id"
            :label="item.className"
            :value="item.id"
          />
        </el-select>
        <el-select
          v-model="queryForm.teacherId"
          placeholder="指导教师"
          clearable
          filterable
          style="width: 160px"
        >
          <el-option
            v-for="item in filteredTeachers"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
        <el-select
          v-model="queryForm.studentId"
          placeholder="学生（可选）"
          filterable
          clearable
          style="width: 180px"
        >
          <el-option
            v-for="item in studentOptions"
            :key="item.studentId"
            :label="item.studentName"
            :value="item.studentId"
          />
        </el-select>
        <el-button
          type="success"
          :icon="Search"
          @click="fetchStatistics"
        >
          查询
        </el-button>
      </div>
    </el-card>

    <el-row
      :gutter="16"
      class="stat-cards"
    >
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-title">
              签到统计
            </div>
            <div class="stat-values">
              <span class="stat-done">{{ checkinStats.done }}</span>
              <span class="stat-divider">/</span>
              <span class="stat-total">{{ checkinStats.total }}</span>
            </div>
            <div class="stat-label">
              已签到 / 应签到天数
            </div>
            <el-progress
              :percentage="checkinStats.rate"
              :color="progressColor(checkinStats.rate)"
              :stroke-width="10"
            />
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-title">
              日报统计
            </div>
            <div class="stat-values">
              <span class="stat-done">{{ dailyStats.done }}</span>
              <span class="stat-divider">/</span>
              <span class="stat-total">{{ dailyStats.total }}</span>
            </div>
            <div class="stat-label">
              已提交 / 应提交天数
            </div>
            <el-progress
              :percentage="dailyStats.rate"
              :color="progressColor(dailyStats.rate)"
              :stroke-width="10"
            />
            <div
              v-if="dailyStats.avgScore > 0"
              class="stat-score"
            >
              <span class="stat-score-label">平均评分</span>
              <el-rate
                :model-value="dailyStats.avgScore"
                disabled
                :texts="scoreTexts"
                show-text
              />
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-title">
              周报统计
            </div>
            <div class="stat-values">
              <span class="stat-done">{{ weeklyStats.done }}</span>
              <span class="stat-divider">/</span>
              <span class="stat-total">{{ weeklyStats.total }}</span>
            </div>
            <div class="stat-label">
              已提交 / 应提交周数
            </div>
            <el-progress
              :percentage="weeklyStats.rate"
              :color="progressColor(weeklyStats.rate)"
              :stroke-width="10"
            />
            <div
              v-if="weeklyStats.avgScore > 0"
              class="stat-score"
            >
              <span class="stat-score-label">平均评分</span>
              <el-rate
                :model-value="weeklyStats.avgScore"
                disabled
                :texts="scoreTexts"
                show-text
              />
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-title">
              月报统计
            </div>
            <div class="stat-values">
              <span class="stat-done">{{ monthlyStats.done }}</span>
              <span class="stat-divider">/</span>
              <span class="stat-total">{{ monthlyStats.total }}</span>
            </div>
            <div class="stat-label">
              已提交 / 应提交月数
            </div>
            <el-progress
              :percentage="monthlyStats.rate"
              :color="progressColor(monthlyStats.rate)"
              :stroke-width="10"
            />
            <div
              v-if="monthlyStats.avgScore > 0"
              class="stat-score"
            >
              <span class="stat-score-label">平均评分</span>
              <el-rate
                :model-value="monthlyStats.avgScore"
                disabled
                :texts="scoreTexts"
                show-text
              />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row
      :gutter="16"
      class="stat-cards"
    >
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-title">
              实习学生数
            </div>
            <div class="stat-values">
              <span class="stat-done">{{ studentCount }}</span>
              <span class="stat-unit">人</span>
            </div>
            <div class="stat-label">
              {{ queryForm.studentId ? '当前选择学生' : '该学期全部学生' }}
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-title">
              指导记录统计
            </div>
            <div class="stat-values">
              <span class="stat-done">{{ guidanceStats.count }}</span>
              <span class="stat-unit">次</span>
            </div>
            <div class="stat-label">
              最近指导日期：{{ guidanceStats.lastDate || '-' }}
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-title">
              巡访记录统计
            </div>
            <div class="stat-values">
              <span class="stat-done">{{ visitStats.count }}</span>
              <span class="stat-unit">次</span>
            </div>
            <div class="stat-label">
              最近巡访日期：{{ visitStats.lastDate || '-' }}
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-title">
              综合完成度
            </div>
            <div class="stat-circle">
              <el-progress
                type="circle"
                :percentage="overallRate"
                :color="progressColor(overallRate)"
                :width="120"
              />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="detail-card">
      <template #header>
        <span>月度明细</span>
      </template>
      <el-table
        v-loading="detailLoading"
        :data="detailData"
        border
        stripe
      >
        <el-table-column
          prop="month"
          label="月份"
          min-width="100"
        />
        <el-table-column
          prop="checkinDays"
          label="签到天数"
          width="130"
        >
          <template #default="{ row }">
            {{ row.checkinDays }} / {{ row.checkinExpected }}
          </template>
        </el-table-column>
        <el-table-column
          prop="dailyCount"
          label="日报数"
          width="130"
        >
          <template #default="{ row }">
            {{ row.dailyCount }} / {{ row.dailyExpected }}
          </template>
        </el-table-column>
        <el-table-column
          prop="weeklyCount"
          label="周报数"
          width="130"
        >
          <template #default="{ row }">
            {{ row.weeklyCount }} / {{ row.weeklyExpected }}
          </template>
        </el-table-column>
        <el-table-column
          prop="monthlyCount"
          label="月报数"
          width="130"
        >
          <template #default="{ row }">
            {{ row.monthlyCount }} / {{ row.monthlyExpected }}
          </template>
        </el-table-column>
        <el-table-column
          prop="guidanceCount"
          label="指导记录数"
          width="110"
        />
        <el-table-column
          prop="visitCount"
          label="巡访记录数"
          width="110"
        />
        <el-table-column
          prop="completionRate"
          label="完成率"
          width="100"
        >
          <template #default="{ row }">
            <el-tag :type="row.completionRate >= 80 ? 'success' : row.completionRate >= 60 ? 'warning' : 'danger'">
              {{ row.completionRate }}%
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="detailQuery.current"
          v-model:page-size="detailQuery.size"
          :total="detailTotal"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchDetailData"
          @current-change="fetchDetailData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import request from '@/utils/request.js'

const semesterOptions = ref([])
const studentOptions = ref([])
const deptOptions = ref([])
const majorOptions = ref([])
const classOptions = ref([])
const teacherOptions = ref([])
const detailLoading = ref(false)
const detailData = ref([])
const detailTotal = ref(0)
const studentCount = ref(0)

const queryForm = reactive({
  semesterId: null,
  deptId: null,
  majorId: null,
  classId: null,
  teacherId: null,
  studentId: null
})

const detailQuery = reactive({
  current: 1,
  size: 10
})

const checkinStats = reactive({ total: 0, done: 0, rate: 0 })
const dailyStats = reactive({ total: 0, done: 0, rate: 0, avgScore: 0 })
const weeklyStats = reactive({ total: 0, done: 0, rate: 0, avgScore: 0 })
const monthlyStats = reactive({ total: 0, done: 0, rate: 0, avgScore: 0 })
const guidanceStats = reactive({ count: 0, lastDate: '' })
const visitStats = reactive({ count: 0, lastDate: '' })

const scoreTexts = ['不合格', '待改进', '普通', '良好', '优秀']

const overallRate = computed(() => {
  const rates = [checkinStats.rate, dailyStats.rate, weeklyStats.rate, monthlyStats.rate]
  const validRates = rates.filter(r => r > 0)
  return validRates.length > 0 ? Math.round(validRates.reduce((a, b) => a + b, 0) / validRates.length) : 0
})

const filteredDepts = computed(() => deptOptions.value)

const filteredMajors = computed(() => {
  if (!queryForm.deptId) return majorOptions.value
  return majorOptions.value.filter(m => m.deptId === queryForm.deptId)
})

const filteredClasses = computed(() => {
  let list = classOptions.value
  if (queryForm.deptId) {
    list = list.filter(c => c.deptId === queryForm.deptId)
  }
  if (queryForm.majorId) {
    list = list.filter(c => c.majorId === queryForm.majorId)
  }
  return list
})

const filteredTeachers = computed(() => {
  if (!queryForm.deptId) return teacherOptions.value
  return teacherOptions.value.filter(t => t.deptId === queryForm.deptId)
})

function progressColor(percentage) {
  if (percentage >= 80) return '#67c23a'
  if (percentage >= 60) return '#e6a23c'
  return '#f56c6c'
}

async function loadSemesterOptions() {
  try {
    const res = await request.get('/admin/semesters', { params: { size: 999 } })
    semesterOptions.value = res.data.records || []
  } catch (e) {
    console.warn('获取学期列表失败', e.message)
  }
}

async function loadStudentOptions() {
  try {
    const params = { size: 999 }
    if (queryForm.semesterId) {
      params.semesterId = queryForm.semesterId
    }
    const res = await request.get('/internship/plan-students', { params })
    const data = res.data
    studentOptions.value = Array.isArray(data) ? data : (data.records || [])
  } catch (e) {
    console.warn('获取学生列表失败', e.message)
  }
}

async function loadFilterOptions() {
  try {
    const params = {}
    if (queryForm.semesterId) params.semesterId = queryForm.semesterId
    const res = await request.get('/internship/statistics/filter-options', { params })
    const data = res.data || {}
    deptOptions.value = data.depts || []
    majorOptions.value = data.majors || []
    classOptions.value = data.classes || []
    teacherOptions.value = data.teachers || []
  } catch (e) {
    console.warn('获取筛选选项失败', e.message)
  }
}

function handleSemesterChange() {
  queryForm.deptId = null
  queryForm.majorId = null
  queryForm.classId = null
  queryForm.teacherId = null
  queryForm.studentId = null
  loadStudentOptions()
  loadFilterOptions()
  if (queryForm.semesterId) {
    fetchStatistics()
  } else {
    resetStats()
  }
}

function handleDeptChange() {
  queryForm.majorId = null
  queryForm.classId = null
  queryForm.teacherId = null
}

function handleMajorChange() {
  queryForm.classId = null
}

function handleClassChange() {
}

async function fetchStatistics() {
  if (!queryForm.semesterId && !queryForm.studentId) {
    resetStats()
    return
  }

  const params = {}
  if (queryForm.semesterId) params.semesterId = queryForm.semesterId
  if (queryForm.studentId) params.studentId = queryForm.studentId
  if (queryForm.deptId) params.deptId = queryForm.deptId
  if (queryForm.majorId) params.majorId = queryForm.majorId
  if (queryForm.classId) params.classId = queryForm.classId
  if (queryForm.teacherId) params.teacherId = queryForm.teacherId

  try {
    const res = await request.get('/internship/statistics', { params })
    const data = res.data || {}
    studentCount.value = data.studentCount || 0

    checkinStats.done = data.checkinDone || 0
    checkinStats.total = data.checkinTotal || 0
    checkinStats.rate = checkinStats.total > 0 ? Math.round((checkinStats.done / checkinStats.total) * 100) : 0

    dailyStats.done = data.dailyDone || 0
    dailyStats.total = data.dailyTotal || 0
    dailyStats.rate = dailyStats.total > 0 ? Math.round((dailyStats.done / dailyStats.total) * 100) : 0
    dailyStats.avgScore = data.dailyAvgScore || 0

    weeklyStats.done = data.weeklyDone || 0
    weeklyStats.total = data.weeklyTotal || 0
    weeklyStats.rate = weeklyStats.total > 0 ? Math.round((weeklyStats.done / weeklyStats.total) * 100) : 0
    weeklyStats.avgScore = data.weeklyAvgScore || 0

    monthlyStats.done = data.monthlyDone || 0
    monthlyStats.total = data.monthlyTotal || 0
    monthlyStats.rate = monthlyStats.total > 0 ? Math.round((monthlyStats.done / monthlyStats.total) * 100) : 0
    monthlyStats.avgScore = data.monthlyAvgScore || 0

    guidanceStats.count = data.guidanceCount || 0
    guidanceStats.lastDate = data.lastGuidanceDate || ''

    visitStats.count = data.visitCount || 0
    visitStats.lastDate = data.lastVisitDate || ''
  } catch (e) {
    console.warn('获取统计数据失败', e.message)
  }

  fetchDetailData()
}

function resetStats() {
  studentCount.value = 0
  checkinStats.total = 0
  checkinStats.done = 0
  checkinStats.rate = 0
  dailyStats.total = 0
  dailyStats.done = 0
  dailyStats.rate = 0
  dailyStats.avgScore = 0
  weeklyStats.total = 0
  weeklyStats.done = 0
  weeklyStats.rate = 0
  weeklyStats.avgScore = 0
  monthlyStats.total = 0
  monthlyStats.done = 0
  monthlyStats.rate = 0
  monthlyStats.avgScore = 0
  guidanceStats.count = 0
  guidanceStats.lastDate = ''
  visitStats.count = 0
  visitStats.lastDate = ''
  detailData.value = []
  detailTotal.value = 0
}

async function fetchDetailData() {
  if (!queryForm.semesterId && !queryForm.studentId) return

  detailLoading.value = true
  try {
    const params = {
      current: detailQuery.current,
      size: detailQuery.size
    }
    if (queryForm.semesterId) params.semesterId = queryForm.semesterId
    if (queryForm.studentId) params.studentId = queryForm.studentId
    if (queryForm.deptId) params.deptId = queryForm.deptId
    if (queryForm.majorId) params.majorId = queryForm.majorId
    if (queryForm.classId) params.classId = queryForm.classId
    if (queryForm.teacherId) params.teacherId = queryForm.teacherId
    const res = await request.get('/internship/statistics/monthly-detail', { params })
    detailData.value = res.data.records || []
    detailTotal.value = res.data.total || 0
  } catch (e) {
    console.warn('获取月度明细失败', e.message)
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => {
  loadSemesterOptions()
  loadStudentOptions()
  loadFilterOptions()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.filter-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
}

.stat-cards {
  margin-bottom: 16px;
}

.stat-card {
  text-align: center;
  padding: 10px 0;
}

.stat-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 12px;
}

.stat-values {
  margin-bottom: 8px;
}

.stat-done {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
}

.stat-divider {
  font-size: 20px;
  color: #909399;
  margin: 0 4px;
}

.stat-total {
  font-size: 20px;
  color: #909399;
}

.stat-unit {
  font-size: 16px;
  color: #909399;
  margin-left: 4px;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 12px;
}

.stat-score {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 8px;
}

.stat-score-label {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
}

.stat-circle {
  display: flex;
  justify-content: center;
  padding: 10px 0;
}

.detail-card {
  margin-top: 0;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
