<template>
  <div class="page-container">
    <template v-if="userStore.isStudent()">
      <el-alert
        v-if="isExempt"
        title="您已获准免实习"
        type="success"
        description="您无需进行签到、填写实习报告等操作。如有疑问，请联系指导老师。"
        show-icon
        :closable="false"
        style="margin-bottom: 16px"
      />
      <el-alert
        v-if="isExpired"
        title="您的实习已结束"
        type="info"
        description="您的实习已结束，无需再进行签到操作。如有疑问，请联系指导老师。"
        show-icon
        :closable="false"
        style="margin-bottom: 16px"
      />
      <template v-if="!isExempt && !isExpired">
      <el-row :gutter="16">
        <el-col :span="14">
          <el-card>
            <template #header>
              <span>签到定位</span>
            </template>
            <el-button
              type="primary"
              :loading="locating"
              style="margin-bottom: 12px"
              @click="handleGetLocation"
            >
              {{ locationReady ? '重新定位' : '获取定位' }}
            </el-button>
            <div
              id="checkin-map"
              style="width:100%;height:400px;"
            />
            <el-alert
              v-if="mapLoadError"
              :title="mapLoadError"
              type="warning"
              show-icon
              :closable="false"
              style="margin-top: 8px"
            >
              <template #default>
                地图加载失败，您仍可点击"获取定位"使用浏览器定位进行签到
              </template>
            </el-alert>
            <div class="location-info">
              <el-descriptions
                :column="1"
                border
                size="small"
              >
                <el-descriptions-item label="当前地址">
                  {{ currentAddress || '未定位' }}
                </el-descriptions-item>
                <el-descriptions-item label="经纬度坐标">
                  {{ currentLng && currentLat ? `${currentLng.toFixed(6)}, ${currentLat.toFixed(6)}` : '未获取' }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </el-card>
        </el-col>
        <el-col :span="10">
          <el-card>
            <template #header>
              <span>今日签到状态</span>
            </template>
            <div class="checkin-actions">
              <div class="checkin-item">
                <span>上班签到：</span>
                <el-tag
                  v-if="todayCheckin.clockIn"
                  type="success"
                >
                  已签到 {{ formatDateTime(todayCheckin.clockIn) }}
                </el-tag>
                <el-tag
                  v-else
                  type="info"
                >
                  未签到
                </el-tag>
                <el-button
                  type="primary"
                  size="small"
                  :disabled="!locationReady || !!todayCheckin.clockIn"
                  @click="handleCheckin('CLOCK_IN')"
                >
                  上班签到
                </el-button>
              </div>
              <div class="checkin-item">
                <span>下班签到：</span>
                <el-tag
                  v-if="todayCheckin.clockOut"
                  type="success"
                >
                  已签到 {{ formatDateTime(todayCheckin.clockOut) }}
                </el-tag>
                <el-tag
                  v-else
                  type="info"
                >
                  未签到
                </el-tag>
                <el-button
                  type="primary"
                  size="small"
                  :disabled="!todayCheckin.clockIn || !!todayCheckin.clockOut"
                  @click="handleCheckin('CLOCK_OUT')"
                >
                  下班签到
                </el-button>
              </div>
            </div>
          </el-card>

          <el-card style="margin-top: 16px">
            <template #header>
              <div style="display: flex; align-items: center; justify-content: space-between;">
                <span>签到历史</span>
                <el-select
                  v-model="studentCalendarMonth"
                  placeholder="选择月份"
                  style="width: 130px"
                  @change="fetchStudentCalendarData"
                >
                  <el-option
                    v-for="m in monthOptions"
                    :key="m.value"
                    :label="m.label"
                    :value="m.value"
                  />
                </el-select>
              </div>
            </template>
            <div
              v-loading="studentCalendarLoading"
              class="calendar-wrapper"
            >
              <div class="custom-calendar">
                <div class="calendar-header-row">
                  <div
                    v-for="d in weekDays"
                    :key="d"
                    class="calendar-header-cell"
                  >
                    {{ d }}
                  </div>
                </div>
                <div class="calendar-body">
                  <div
                    v-for="(week, wIdx) in studentCalendarWeeks"
                    :key="wIdx"
                    class="calendar-week-row"
                  >
                    <div
                      v-for="(day, dIdx) in week"
                      :key="dIdx"
                      class="calendar-day-cell"
                      :class="{
                        'is-other-month': !day.currentMonth,
                        'is-today': day.isToday,
                        'is-not-workday': !day.isWorkDay && day.currentMonth,
                        'is-checked': day.checkinStatus === 'checked',
                        'is-abnormal': day.checkinStatus === 'abnormal',
                        'is-missed': day.checkinStatus === 'missed'
                      }"
                      @click="handleStudentDayClick(day)"
                    >
                      <div class="day-number">
                        {{ day.dayNum }}
                      </div>
                      <div
                        v-if="day.currentMonth"
                        class="day-marks"
                      >
                        <span
                          v-if="day.checkinStatus === 'checked'"
                          class="mark-ok"
                        >✓</span>
                        <span
                          v-else-if="day.checkinStatus === 'abnormal'"
                          class="mark-warn"
                        >!</span>
                        <span
                          v-else-if="day.checkinStatus === 'missed' && day.isWorkDay"
                          class="mark-miss"
                        >✗</span>
                        <span
                          v-else-if="day.isWorkDay"
                          class="mark-should"
                        >应签</span>
                        <span
                          v-else
                          class="mark-rest"
                        >休</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div class="calendar-legend">
              <span class="legend-item"><span class="legend-dot legend-dot--ok" /> 已签到</span>
              <span class="legend-item"><span class="legend-dot legend-dot--warn" /> 异常/代签</span>
              <span class="legend-item"><span class="legend-dot legend-dot--miss" /> 应签日未签到</span>
              <span class="legend-item"><span class="legend-dot legend-dot--should" /> 应签日</span>
              <span class="legend-item"><span class="legend-dot legend-dot--rest" /> 无需签到</span>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-dialog
        v-model="dateDetailVisible"
        :title="dateDetailTitle"
        width="560px"
        destroy-on-close
      >
        <template v-if="dateDetailData">
          <el-descriptions
            :column="1"
            border
            size="small"
          >
            <el-descriptions-item label="日期">
              {{ formatDate(dateDetailData.date) }}
            </el-descriptions-item>
            <el-descriptions-item label="日期类型">
              <el-tag
                v-if="dateDetailData.isWorkDay"
                type="primary"
                size="small"
              >
                应签日
              </el-tag>
              <el-tag
                v-else
                type="info"
                size="small"
              >
                无需签到
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item
              v-if="dateDetailData.clockInTime"
              label="上班签到时间"
            >
              {{ formatDateTime(dateDetailData.clockInTime) }}
            </el-descriptions-item>
            <el-descriptions-item
              v-if="dateDetailData.clockOutTime"
              label="下班签到时间"
            >
              {{ formatDateTime(dateDetailData.clockOutTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="签到状态">
              <el-tag
                v-if="dateDetailData.status"
                :type="statusTagType(dateDetailData.status)"
              >
                {{ dateDetailData.status }}
              </el-tag>
              <span v-else>无记录</span>
            </el-descriptions-item>
            <el-descriptions-item
              v-if="dateDetailData.address"
              label="签到地址"
            >
              {{ dateDetailData.address }}
            </el-descriptions-item>
            <el-descriptions-item
              v-if="dateDetailData.longitude && dateDetailData.latitude"
              label="签到定位"
            >
              {{ dateDetailData.longitude }}, {{ dateDetailData.latitude }}
            </el-descriptions-item>
          </el-descriptions>
        </template>
        <el-empty
          v-else
          description="该日无签到记录"
        />
      </el-dialog>
      </template>
    </template>

    <template v-else>
      <template v-if="currentView === 'list'">
        <el-card>
          <div class="filter-bar">
            <el-input
              v-model="studentQueryForm.studentName"
              placeholder="学生姓名"
              clearable
              style="width: 160px"
            />
            <el-input
              v-model="studentQueryForm.studentNo"
              placeholder="学号"
              clearable
              style="width: 160px"
            />
            <el-button
              type="success"
              :icon="Search"
              @click="handleStudentSearch"
            >
              搜索
            </el-button>
            <el-button
              :icon="Refresh"
              @click="handleStudentReset"
            >
              重置
            </el-button>
          </div>
          <div class="toolbar">
            <el-button
              type="primary"
              :disabled="selectedStudents.length === 0"
              @click="handleBatchRemind"
            >
              一键提醒签到（{{ selectedStudents.length }}）
            </el-button>
            <el-button
              type="success"
              @click="handleCopyWarningStudents"
            >
              复制未签到学生
            </el-button>
          </div>
          <el-alert
            type="info"
            :closable="false"
            show-icon
            style="margin-bottom: 12px"
          >
            <template #default>
              注：连续未签到3天及以上会被预警。下方表格里的签到预警列，每天凌晨1点刷新，统计从昨天开始算起，该生已连续几天未签到。点击"签到详情"，将以日历形式展示该生具体签到情况。
            </template>
          </el-alert>
          <el-table
            ref="tableRef"
            v-loading="studentLoading"
            :data="studentTableData"
            border
            stripe
            style="width: 100%"
            @selection-change="handleSelectionChange"
          >
            <el-table-column
              type="selection"
              width="45"
            />
            <el-table-column
              prop="name"
              label="姓名"
              min-width="80"
            />
            <el-table-column
              prop="studentNo"
              label="学号"
              min-width="110"
            />
            <el-table-column
              prop="phone"
              label="电话"
              min-width="110"
            />
            <el-table-column
              prop="className"
              label="班级"
              min-width="100"
            />
            <el-table-column
              prop="majorName"
              label="专业"
              min-width="100"
            />
            <el-table-column
              label="今日签到"
              min-width="90"
              align="center"
            >
              <template #default="{ row }">
                <el-tag
                  :type="row.todayChecked ? 'success' : 'danger'"
                  size="small"
                >
                  {{ row.todayChecked ? '已签到' : '未签到' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column
              label="签到情况(已签/应签)"
              min-width="140"
              align="center"
            >
              <template #default="{ row }">
                <span>{{ row.totalCheckin }}/{{ row.totalWorkdays }}</span>
              </template>
            </el-table-column>
            <el-table-column
              label="签到预警"
              min-width="90"
              align="center"
            >
              <template #default="{ row }">
                <el-tag
                  v-if="row.consecutiveMissed >= 3"
                  type="danger"
                  size="small"
                >
                  {{ row.consecutiveMissed }}天
                </el-tag>
                <el-tag
                  v-else-if="row.consecutiveMissed > 0"
                  type="warning"
                  size="small"
                >
                  {{ row.consecutiveMissed }}天
                </el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column
              label="操作"
              min-width="90"
              fixed="right"
              :resizable="false"
            >
              <template #default="{ row }">
                <el-button
                  type="primary"
                  link
                  @click="handleViewDetail(row)"
                >
                  签到详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-bar">
            <el-pagination
              v-model:current-page="studentQueryForm.current"
              v-model:page-size="studentQueryForm.size"
              :total="studentTotal"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="fetchStudents"
              @current-change="fetchStudents"
            />
          </div>
        </el-card>
      </template>

      <template v-else>
        <el-card>
          <div class="detail-header">
            <el-button
              :icon="ArrowLeft"
              @click="handleBackToList"
            >
              返回学生列表
            </el-button>
            <span class="detail-title">{{ selectedStudent.name }}（{{ selectedStudent.studentNo }}）的签到详情</span>
            <div style="flex: 1" />
            <el-button
              type="warning"
              @click="handleHelpCheckin"
            >
              帮签到
            </el-button>
          </div>

          <div class="filter-bar">
            <el-select
              v-model="calendarMonth"
              placeholder="选择月份"
              style="width: 140px"
              @change="fetchCalendarData"
            >
              <el-option
                v-for="m in monthOptions"
                :key="m.value"
                :label="m.label"
                :value="m.value"
              />
            </el-select>
          </div>

          <div
            v-loading="calendarLoading"
            class="calendar-wrapper"
          >
            <div class="custom-calendar">
              <div class="calendar-header-row">
                <div
                  v-for="d in weekDays"
                  :key="d"
                  class="calendar-header-cell"
                >
                  {{ d }}
                </div>
              </div>
              <div class="calendar-body">
                <div
                  v-for="(week, wIdx) in calendarWeeks"
                  :key="wIdx"
                  class="calendar-week-row"
                >
                  <div
                    v-for="(day, dIdx) in week"
                    :key="dIdx"
                    class="calendar-day-cell"
                    :class="{
                      'is-other-month': !day.currentMonth,
                      'is-today': day.isToday,
                      'is-workday': day.isWorkDay && day.currentMonth,
                      'is-not-workday': !day.isWorkDay && day.currentMonth,
                      'is-checked': day.checkinStatus === 'checked',
                      'is-abnormal': day.checkinStatus === 'abnormal',
                      'is-missed': day.checkinStatus === 'missed'
                    }"
                    @click="handleDayClick(day)"
                  >
                    <div class="day-number">
                      {{ day.dayNum }}
                    </div>
                    <div
                      v-if="day.currentMonth"
                      class="day-marks"
                    >
                      <span
                        v-if="day.checkinStatus === 'checked'"
                        class="mark-ok"
                      >✓</span>
                      <span
                        v-else-if="day.checkinStatus === 'abnormal'"
                        class="mark-warn"
                      >!</span>
                      <span
                        v-else-if="day.checkinStatus === 'missed' && day.isWorkDay"
                        class="mark-miss"
                      >✗</span>
                      <span
                        v-else-if="day.isWorkDay"
                        class="mark-should"
                      >应签</span>
                      <span
                        v-else
                        class="mark-rest"
                      >休</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="calendar-legend">
            <span class="legend-item"><span class="legend-dot legend-dot--ok" /> 已签到</span>
            <span class="legend-item"><span class="legend-dot legend-dot--warn" /> 异常/代签</span>
            <span class="legend-item"><span class="legend-dot legend-dot--miss" /> 应签日未签到</span>
            <span class="legend-item"><span class="legend-dot legend-dot--should" /> 应签日</span>
            <span class="legend-item"><span class="legend-dot legend-dot--rest" /> 无需签到</span>
          </div>
        </el-card>

        <el-dialog
          v-model="dayDetailVisible"
          :title="dayDetailTitle"
          width="560px"
          destroy-on-close
        >
          <template v-if="dayDetailData">
            <el-descriptions
              :column="1"
              border
              size="small"
            >
              <el-descriptions-item label="日期">
                {{ formatDate(dayDetailData.checkinDate || dayDetailData.date) }}
              </el-descriptions-item>
              <el-descriptions-item label="日期类型">
                <el-tag
                  v-if="dayDetailData.isWorkDay"
                  type="primary"
                  size="small"
                >
                  应签日
                </el-tag>
                <el-tag
                  v-else
                  type="info"
                  size="small"
                >
                  无需签到
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item
                v-if="dayDetailData.clockInTime"
                label="上班签到时间"
              >
                {{ formatDateTime(dayDetailData.clockInTime) }}
              </el-descriptions-item>
              <el-descriptions-item
                v-if="dayDetailData.clockOutTime"
                label="下班签到时间"
              >
                {{ formatDateTime(dayDetailData.clockOutTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="签到状态">
                <el-tag
                  v-if="dayDetailData.status"
                  :type="statusTagType(dayDetailData.status)"
                >
                  {{ dayDetailData.status }}
                </el-tag>
                <span v-else>无记录</span>
              </el-descriptions-item>
              <el-descriptions-item
                v-if="dayDetailData.address"
                label="签到地址"
              >
                {{ dayDetailData.address }}
              </el-descriptions-item>
              <el-descriptions-item
                v-if="dayDetailData.longitude && dayDetailData.latitude"
                label="签到定位"
              >
                {{ dayDetailData.longitude }}, {{ dayDetailData.latitude }}
              </el-descriptions-item>
              <el-descriptions-item
                v-if="dayDetailData.areaStatus"
                label="区域状态"
              >
                {{ dayDetailData.areaStatus }}
              </el-descriptions-item>
            </el-descriptions>
          </template>
          <el-empty
            v-else
            description="该日无签到记录"
          />
        </el-dialog>
      </template>

      <el-dialog
        v-model="helpDialogVisible"
        title="帮签到"
        width="500px"
        destroy-on-close
      >
        <el-form
          ref="helpFormRef"
          :model="helpForm"
          :rules="helpFormRules"
          label-width="100px"
        >
          <el-form-item label="学生">
            <span>{{ selectedStudent.name }}（{{ selectedStudent.studentNo }}）</span>
          </el-form-item>
          <el-form-item
            label="签到日期"
            prop="checkinDate"
          >
            <el-date-picker
              v-model="helpForm.checkinDate"
              type="date"
              placeholder="请选择签到日期"
              value-format="YYYY-MM-DD"
              format="YYYY年MM月DD日"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item
            label="签到类型"
            prop="type"
          >
            <el-radio-group v-model="helpForm.type">
              <el-radio value="CLOCK_IN">
                上班签到
              </el-radio>
              <el-radio value="CLOCK_OUT">
                下班签到
              </el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item
            label="备注"
            prop="remark"
          >
            <el-input
              v-model="helpForm.remark"
              type="textarea"
              :rows="3"
              placeholder="请填写帮签到原因"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="helpDialogVisible = false">
            取消
          </el-button>
          <el-button
            type="primary"
            :loading="helpSubmitLoading"
            @click="handleHelpSubmit"
          >
            确定
          </el-button>
        </template>
      </el-dialog>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, ArrowLeft } from '@element-plus/icons-vue'
import request from '@/utils/request.js'
import { useUserStore } from '@/stores/user.js'
import { initAMapMap, getLocation, getBrowserGeolocation } from '@/utils/amap.js'
import { useTableResize } from '@/composables/useTableResize.js'

const userStore = useUserStore()

const tableRef = ref(null)
useTableResize(tableRef)

let mapInstance = null
const locationReady = ref(false)
const locating = ref(false)
const currentAddress = ref('')
const currentLng = ref(null)
const currentLat = ref(null)

const todayCheckin = reactive({
  clockIn: '',
  clockOut: ''
})

const isExempt = ref(false)
const isExpired = ref(false)

async function checkExemptStatus() {
  if (!userStore.isStudent()) return
  try {
    const res = await request.get('/internship/records/my')
    if (res.data) {
      if (res.data.internshipStatus === 'exempt') {
        isExempt.value = true
      }
      if (res.data.internshipStatus === 'expired') {
        isExpired.value = true
      }
    }
  } catch (e) {
    console.warn('检查实习状态失败', e.message)
  }
}

const studentCalendarMonth = ref('')
const studentCalendarLoading = ref(false)
const studentCalendarDates = ref([])
const studentCalendarCheckins = ref([])

const dateDetailVisible = ref(false)
const dateDetailTitle = ref('')
const dateDetailData = ref(null)

const currentView = ref('list')
const selectedStudent = ref({})

const studentTableData = ref([])
const studentTotal = ref(0)
const studentLoading = ref(false)
const studentQueryForm = reactive({
  current: 1,
  size: 10,
  studentName: '',
  studentNo: ''
})

const calendarMonth = ref('')
const calendarLoading = ref(false)
const calendarDates = ref([])
const calendarCheckins = ref([])

const dayDetailVisible = ref(false)
const dayDetailTitle = ref('')
const dayDetailData = ref(null)

const weekDays = ['一', '二', '三', '四', '五', '六', '日']

const monthOptions = computed(() => {
  const now = new Date()
  const opts = []
  for (let i = -6; i <= 6; i++) {
    const d = new Date(now.getFullYear(), now.getMonth() + i, 1)
    const val = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
    opts.push({ label: `${d.getFullYear()}年${d.getMonth() + 1}月`, value: val })
  }
  return opts
})

const calendarWeeks = computed(() => {
  if (!calendarMonth.value) return []
  const [year, month] = calendarMonth.value.split('-').map(Number)
  const firstDay = new Date(year, month - 1, 1)
  const lastDay = new Date(year, month, 0)
  let startDow = firstDay.getDay()
  if (startDow === 0) startDow = 7
  const startDate = new Date(firstDay)
  startDate.setDate(startDate.getDate() - (startDow - 1))

  const today = new Date()
  const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`

  const weeks = []
  let current = new Date(startDate)
  for (let w = 0; w < 6; w++) {
    const week = []
    for (let d = 0; d < 7; d++) {
      const dateStr = `${current.getFullYear()}-${String(current.getMonth() + 1).padStart(2, '0')}-${String(current.getDate()).padStart(2, '0')}`
      const isCurrentMonth = current.getMonth() === month - 1 && current.getFullYear() === year
      const calDate = calendarDates.value.find(c => c.date === dateStr)
      const checkin = calendarCheckins.value.find(c => c.checkinDate === dateStr)

      let checkinStatus = ''
      if (isCurrentMonth && checkin) {
        if (checkin.status === '正常') {
          checkinStatus = 'checked'
        } else if (checkin.status === '代签' || checkin.status === '异常') {
          checkinStatus = 'abnormal'
        } else {
          checkinStatus = 'missed'
        }
      } else if (isCurrentMonth && !checkin && calDate && calDate.isWorkDay === 1) {
        const dateObj = new Date(dateStr)
        if (dateObj < today) {
          checkinStatus = 'missed'
        }
      }

      week.push({
        date: dateStr,
        dayNum: current.getDate(),
        currentMonth: isCurrentMonth,
        isToday: dateStr === todayStr,
        isWorkDay: calDate ? calDate.isWorkDay === 1 : false,
        dayType: calDate ? calDate.dayType : '',
        checkinStatus,
        checkin
      })
      current.setDate(current.getDate() + 1)
    }
    weeks.push(week)
  }
  return weeks
})

const studentCalendarWeeks = computed(() => {
  if (!studentCalendarMonth.value) return []
  const [year, month] = studentCalendarMonth.value.split('-').map(Number)
  const firstDay = new Date(year, month - 1, 1)
  let startDow = firstDay.getDay()
  if (startDow === 0) startDow = 7
  const startDate = new Date(firstDay)
  startDate.setDate(startDate.getDate() - (startDow - 1))

  const today = new Date()
  const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`

  const weeks = []
  let current = new Date(startDate)
  for (let w = 0; w < 6; w++) {
    const week = []
    for (let d = 0; d < 7; d++) {
      const dateStr = `${current.getFullYear()}-${String(current.getMonth() + 1).padStart(2, '0')}-${String(current.getDate()).padStart(2, '0')}`
      const isCurrentMonth = current.getMonth() === month - 1 && current.getFullYear() === year
      const calDate = studentCalendarDates.value.find(c => c.date === dateStr)
      const checkin = studentCalendarCheckins.value.find(c => c.checkinDate === dateStr)

      let checkinStatus = ''
      if (isCurrentMonth && checkin) {
        if (checkin.status === '正常') {
          checkinStatus = 'checked'
        } else if (checkin.status === '代签' || checkin.status === '异常') {
          checkinStatus = 'abnormal'
        } else {
          checkinStatus = 'missed'
        }
      } else if (isCurrentMonth && !checkin && calDate && calDate.isWorkDay === 1) {
        const dateObj = new Date(dateStr)
        if (dateObj < today) {
          checkinStatus = 'missed'
        }
      }

      week.push({
        date: dateStr,
        dayNum: current.getDate(),
        currentMonth: isCurrentMonth,
        isToday: dateStr === todayStr,
        isWorkDay: calDate ? calDate.isWorkDay === 1 : false,
        dayType: calDate ? calDate.dayType : '',
        checkinStatus,
        checkin
      })
      current.setDate(current.getDate() + 1)
    }
    weeks.push(week)
  }
  return weeks
})

const helpDialogVisible = ref(false)
const helpSubmitLoading = ref(false)
const helpFormRef = ref(null)
const helpForm = reactive({
  checkinDate: '',
  type: 'CLOCK_IN',
  remark: ''
})
const helpFormRules = {
  checkinDate: [{ required: true, message: '请选择签到日期', trigger: 'change' }],
  type: [{ required: true, message: '请选择签到类型', trigger: 'change' }],
  remark: [{ required: true, message: '请填写帮签到原因', trigger: 'blur' }]
}

const mapLoadError = ref('')

const selectedStudents = ref([])

function handleSelectionChange(selection) {
  selectedStudents.value = selection
}

async function handleBatchRemind() {
  if (selectedStudents.value.length === 0) {
    ElMessage.warning('请先勾选需要提醒的学生')
    return
  }
  const studentIds = selectedStudents.value.map(s => s.id)
  try {
    const res = await request.post('/internship/checkin/reminders/batch', {
      studentIds
    })
    ElMessage.success(res.message || '提醒发送成功')
    fetchStudents()
  } catch (e) {
    console.warn('一键提醒失败', e.message)
  }
}

async function handleCopyWarningStudents() {
  try {
    const res = await request.get('/internship/checkin/reminders/warning-students')
    const students = res.data || []
    if (students.length === 0) {
      ElMessage.info('当前没有连续未签到3天以上的学生')
      return
    }
    const text = students.map(s => `${s.name}(${s.studentNo})`).join('、')
    await navigator.clipboard.writeText(text)
    ElMessage.success(`已复制${students.length}名预警学生信息`)
  } catch (e) {
    if (e.name === 'NotAllowedError') {
      ElMessage.warning('浏览器不允许访问剪贴板，请手动复制')
    } else {
      console.warn('复制未签到学生失败', e.message)
    }
  }
}

async function initMap() {
  try {
    mapInstance = await initAMapMap('checkin-map')
    mapLoadError.value = ''
  } catch (e) {
    console.warn('地图加载失败:', e.message)
    mapLoadError.value = e.message
    mapInstance = null
  }
}

async function handleGetLocation() {
  locating.value = true
  try {
    if (mapInstance) {
      const result = await getLocation(mapInstance)
      currentLng.value = result.lng
      currentLat.value = result.lat
      currentAddress.value = result.address
    } else {
      const result = await getBrowserGeolocation()
      currentLng.value = result.lng
      currentLat.value = result.lat
      currentAddress.value = result.address + '（浏览器定位）'
    }
    locationReady.value = true
  } catch (e) {
    currentAddress.value = `定位失败: ${e.message}`
    locationReady.value = false
    ElMessage.warning(e.message)
  } finally {
    locating.value = false
  }
}

async function fetchTodayCheckin() {
  try {
    const res = await request.get('/internship/checkin/today')
    const data = res.data || {}
    todayCheckin.clockIn = data.clockInTime || ''
    todayCheckin.clockOut = data.clockOutTime || ''
  } catch (e) {
    console.warn('获取今日签到状态失败', e.message)
  }
}

async function fetchStudentCalendarData() {
  if (!studentCalendarMonth.value) return
  studentCalendarLoading.value = true
  try {
    const [calendarRes, historyRes] = await Promise.all([
      request.get('/internship/checkin/calendar-events', {
        params: { month: studentCalendarMonth.value }
      }),
      request.get('/internship/checkin/history', {
        params: { month: studentCalendarMonth.value, current: 1, size: 100 }
      })
    ])
    studentCalendarDates.value = calendarRes.data.calendarDates || []
    const records = historyRes.data.records || []
    studentCalendarCheckins.value = records.map(c => {
      let dateStr = c.checkinDate
      if (Array.isArray(dateStr)) {
        const [y, m, d] = dateStr
        dateStr = `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`
      } else if (typeof dateStr === 'string' && dateStr.length > 10) {
        dateStr = dateStr.substring(0, 10)
      }
      return {
        id: c.id,
        checkinDate: dateStr,
        clockInTime: c.clockInTime,
        clockOutTime: c.clockOutTime,
        status: c.status,
        address: c.address,
        longitude: c.longitude,
        latitude: c.latitude,
        areaStatus: c.areaStatus
      }
    })
  } catch (e) {
    console.warn('获取签到日历数据失败', e.message)
    studentCalendarDates.value = []
    studentCalendarCheckins.value = []
  } finally {
    studentCalendarLoading.value = false
  }
}

function handleStudentDayClick(day) {
  if (!day.currentMonth) return
  dateDetailTitle.value = `${day.date} 签到详情`
  if (day.checkin) {
    dateDetailData.value = {
      ...day.checkin,
      date: day.date,
      isWorkDay: day.isWorkDay
    }
  } else {
    dateDetailData.value = {
      date: day.date,
      isWorkDay: day.isWorkDay,
      dayType: day.dayType
    }
  }
  dateDetailVisible.value = true
}

async function handleCheckin(type) {
  if (!locationReady.value) {
    ElMessage.warning('定位未就绪，请等待定位完成')
    return
  }
  try {
    await request.post('/internship/checkin', {
      checkinType: type,
      longitude: currentLng.value,
      latitude: currentLat.value,
      address: currentAddress.value
    })
    ElMessage.success(type === 'CLOCK_IN' ? '上班签到成功' : '下班签到成功')
    fetchTodayCheckin()
    fetchStudentCalendarData()
  } catch (e) {
    console.warn('签到失败', e.message)
  }
}

async function fetchStudents() {
  studentLoading.value = true
  try {
    const res = await request.get('/internship/checkin/students', {
      params: {
        current: studentQueryForm.current,
        size: studentQueryForm.size,
        studentName: studentQueryForm.studentName || undefined,
        studentNo: studentQueryForm.studentNo || undefined
      }
    })
    studentTableData.value = res.data.records
    studentTotal.value = res.data.total
  } catch (e) {
    console.warn('获取学生列表失败', e.message)
  } finally {
    studentLoading.value = false
  }
}

function handleStudentSearch() {
  studentQueryForm.current = 1
  fetchStudents()
}

function handleStudentReset() {
  studentQueryForm.studentName = ''
  studentQueryForm.studentNo = ''
  studentQueryForm.current = 1
  fetchStudents()
}

function handleViewDetail(row) {
  selectedStudent.value = row
  currentView.value = 'detail'
  const now = new Date()
  calendarMonth.value = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
  fetchCalendarData()
}

function handleBackToList() {
  currentView.value = 'list'
  fetchStudents()
}

async function fetchCalendarData() {
  if (!selectedStudent.value.id || !calendarMonth.value) return
  calendarLoading.value = true
  try {
    const [calendarRes, checkinsRes] = await Promise.all([
      request.get('/internship/checkin/calendar-events', {
        params: { month: calendarMonth.value }
      }),
      request.get(`/internship/checkin/students/${selectedStudent.value.id}/checkins`, {
        params: { month: calendarMonth.value }
      })
    ])
    calendarDates.value = calendarRes.data.calendarDates || []
    calendarCheckins.value = checkinsRes.data || []
  } catch (e) {
    console.warn('获取签到日历数据失败', e.message)
    calendarDates.value = []
    calendarCheckins.value = []
  } finally {
    calendarLoading.value = false
  }
}

function handleDayClick(day) {
  if (!day.currentMonth) return
  dayDetailTitle.value = `${day.date} 签到详情`
  if (day.checkin) {
    dayDetailData.value = {
      ...day.checkin,
      date: day.date,
      isWorkDay: day.isWorkDay
    }
  } else {
    dayDetailData.value = {
      date: day.date,
      isWorkDay: day.isWorkDay,
      dayType: day.dayType
    }
  }
  dayDetailVisible.value = true
}

function formatDateTime(dtStr) {
  if (!dtStr) return ''
  const str = String(dtStr).replace('T', ' ')
  const parts = str.match(/(\d{4})-(\d{1,2})-(\d{1,2})[T ](\d{1,2}):(\d{2}):(\d{2})/)
  if (parts) {
    return `${parts[1]}年${parts[2].padStart(2, '0')}月${parts[3].padStart(2, '0')}日 ${parts[4].padStart(2, '0')}:${parts[5]}:${parts[6]}`
  }
  return str
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const str = String(dateStr)
  const parts = str.match(/(\d{4})-(\d{1,2})-(\d{1,2})/)
  if (parts) {
    return `${parts[1]}年${parts[2].padStart(2, '0')}月${parts[3].padStart(2, '0')}日`
  }
  return dateStr
}

function handleHelpCheckin() {
  helpForm.checkinDate = ''
  helpForm.type = 'CLOCK_IN'
  helpForm.remark = ''
  helpDialogVisible.value = true
  nextTick(() => {
    helpFormRef.value?.clearValidate()
  })
}

async function handleHelpSubmit() {
  const valid = await helpFormRef.value?.validate().catch(() => false)
  if (!valid) return

  helpSubmitLoading.value = true
  try {
    await request.post('/internship/checkin/help', {
      studentId: selectedStudent.value.id,
      checkinDate: helpForm.checkinDate,
      type: helpForm.type,
      remark: helpForm.remark
    })
    ElMessage.success('帮签到成功')
    helpDialogVisible.value = false
    fetchCalendarData()
  } catch (e) {
    console.warn('帮签到失败', e.message)
  } finally {
    helpSubmitLoading.value = false
  }
}

function statusTagType(status) {
  const map = { '正常': 'success', '代签': 'warning', '异常': 'danger', '未签到': 'info' }
  return map[status] || 'info'
}

onMounted(async () => {
  if (userStore.isStudent()) {
    await checkExemptStatus()
    if (!isExempt.value && !isExpired.value) {
      fetchTodayCheckin()
      const now = new Date()
      studentCalendarMonth.value = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
      fetchStudentCalendarData()
      nextTick(async () => {
        await initMap()
      })
    }
  } else {
    fetchStudents()
  }
})

onBeforeUnmount(() => {
  if (mapInstance) {
    mapInstance.destroy()
    mapInstance = null
  }
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

.toolbar {
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

.location-info {
  margin-top: 12px;
}

.checkin-actions {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.checkin-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.detail-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.calendar-wrapper {
  min-height: 300px;
}

.custom-calendar {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
}

.calendar-header-row {
  display: flex;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

.calendar-header-cell {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: #606266;
}

.calendar-week-row {
  display: flex;
  border-bottom: 1px solid #ebeef5;
}

.calendar-week-row:last-child {
  border-bottom: none;
}

.calendar-day-cell {
  flex: 1;
  min-height: 64px;
  padding: 4px;
  border-right: 1px solid #ebeef5;
  cursor: pointer;
  transition: background 0.2s;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.calendar-day-cell:last-child {
  border-right: none;
}

.calendar-day-cell:hover {
  background: #f5f7fa;
}

.calendar-day-cell.is-other-month {
  opacity: 0.3;
  cursor: default;
}

.calendar-day-cell.is-today .day-number {
  background: #409eff;
  color: #fff;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.calendar-day-cell.is-not-workday {
  background: #f5f7fa;
}

.calendar-day-cell.is-checked {
  background: #f0f9eb;
}

.calendar-day-cell.is-abnormal {
  background: #fdf6ec;
}

.calendar-day-cell.is-missed {
  background: #fef0f0;
}

.day-number {
  font-size: 14px;
  line-height: 24px;
}

.day-marks {
  margin-top: 2px;
  font-size: 14px;
  font-weight: bold;
}

.mark-ok {
  color: #67c23a;
}

.mark-warn {
  color: #e6a23c;
}

.mark-miss {
  color: #f56c6c;
}

.mark-should {
  color: #409eff;
  font-size: 10px;
  font-weight: normal;
}

.mark-rest {
  color: #c0c4cc;
  font-size: 10px;
  font-weight: normal;
}

.calendar-legend {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-top: 12px;
  padding: 8px 0;
  font-size: 13px;
  color: #606266;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.legend-dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.legend-dot--ok {
  background: #f0f9eb;
  border: 1px solid #67c23a;
}

.legend-dot--warn {
  background: #fdf6ec;
  border: 1px solid #e6a23c;
}

.legend-dot--miss {
  background: #fef0f0;
  border: 1px solid #f56c6c;
}

.legend-dot--should {
  background: #ecf5ff;
  border: 1px solid #409eff;
}

.legend-dot--rest {
  background: #f5f7fa;
  border: 1px solid #c0c4cc;
}
</style>
