<template>
  <div class="page-container">
    <el-card>
      <template v-if="userStore.isStudent()">
        <div class="filter-bar">
          <el-select
            v-model="queryForm.planId"
            placeholder="请输入实习计划"
            filterable
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="item in planOptions"
              :key="item.id"
              :label="item.planName"
              :value="item.id"
            />
          </el-select>
          <el-date-picker
            v-model="queryForm.startDate"
            type="date"
            placeholder="请选择实习开始时间"
            value-format="YYYY-MM-DD"
            format="YYYY年MM月DD日"
            style="width: 200px"
          />
          <el-date-picker
            v-model="queryForm.endDate"
            type="date"
            placeholder="请选择实习结束时间"
            value-format="YYYY-MM-DD"
            format="YYYY年MM月DD日"
            style="width: 200px"
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
          <div style="flex: 1" />
          <el-button
            type="warning"
            :icon="Plus"
            @click="handleAdd"
          >
            新增
          </el-button>
        </div>
        <el-table
          ref="tableRef1"
          v-loading="loading"
          :data="tableData"
          border
          stripe
        >
          <el-table-column
            type="selection"
            width="55"
          />
          <el-table-column
            prop="planName"
            label="实习计划"
          />
          <el-table-column
            prop="teacherName"
            label="指导老师"
          />
          <el-table-column
            prop="companyName"
            label="单位名称"
          />
          <el-table-column
            prop="companyAddress"
            label="单位地址"
            show-overflow-tooltip
          />
          <el-table-column
            prop="companyTeacherName"
            label="单位指导老师姓名"
          />
          <el-table-column
            prop="companyTeacherPhone"
            label="单位指导老师联系电话"
          />
          <el-table-column
            prop="position"
            label="单位岗位"
          />
          <el-table-column
            prop="salary"
            label="单位待遇"
          />
          <el-table-column
            prop="startDate"
            label="实习开始时间"
          >
            <template #default="{ row }">
              {{ formatDate(row.startDate) }}
            </template>
          </el-table-column>
          <el-table-column
            prop="endDate"
            label="实习结束时间"
          >
            <template #default="{ row }">
              {{ formatDate(row.endDate) }}
            </template>
          </el-table-column>
          <el-table-column
            label="操作"
            fixed="right"
            width="150"
            :resizable="false"
          >
            <template #default="{ row }">
              <el-button
                type="primary"
                link
                @click="handleViewDetail(row)"
              >
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
      <template v-else>
        <div class="filter-bar">
          <el-input
            v-model="queryForm.studentName"
            placeholder="学生姓名"
            clearable
            style="width: 160px"
          />
          <el-input
            v-model="queryForm.studentNo"
            placeholder="学号"
            clearable
            style="width: 160px"
          />
          <el-input
            v-model="queryForm.teacherName"
            placeholder="指导老师姓名"
            clearable
            style="width: 160px"
          />
          <el-select
            v-model="queryForm.planId"
            placeholder="实习计划"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="item in planOptions"
              :key="item.id"
              :label="item.planName"
              :value="item.id"
            />
          </el-select>
          <el-select
            v-model="queryForm.internshipStatus"
            placeholder="实习状态"
            clearable
            style="width: 140px"
          >
            <el-option
              v-for="item in internshipStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
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
          <div style="flex: 1" />
          <el-button
            v-if="userStore.isTeacher() || userStore.isDeptAdmin() || userStore.isAdmin()"
            type="warning"
            :icon="CircleCheck"
            @click="handleOpenExempt(null)"
          >
            申请免实习
          </el-button>
        </div>
        <el-table
          ref="tableRef2"
          v-loading="loading"
          :data="tableData"
          border
          stripe
          @selection-change="handleSelectionChange"
        >
          <el-table-column
            type="selection"
            width="55"
          />
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
            prop="companyName"
            label="实习单位"
            min-width="160"
            show-overflow-tooltip
          />
          <el-table-column
            prop="position"
            label="实习岗位"
            min-width="120"
          />
          <el-table-column
            prop="teacherName"
            label="指导老师"
            min-width="100"
          />
          <el-table-column
            prop="startDate"
            label="实习开始时间"
            min-width="120"
          >
            <template #default="{ row }">
              {{ formatDate(row.startDate) }}
            </template>
          </el-table-column>
          <el-table-column
            prop="endDate"
            label="实习结束时间"
            min-width="120"
          >
            <template #default="{ row }">
              {{ formatDate(row.endDate) }}
            </template>
          </el-table-column>
          <el-table-column
            prop="internshipStatus"
            label="实习状态"
            min-width="100"
          >
            <template #default="{ row }">
              <el-tag :type="internshipStatusTagType(row.internshipStatus)">
                {{ internshipStatusLabel(row.internshipStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column
            label="操作"
            fixed="right"
            width="200"
            :resizable="false"
          >
            <template #default="{ row }">
              <el-button
                type="primary"
                link
                @click="handleViewDetail(row)"
              >
                查看详情
              </el-button>
              <el-button
                v-if="(userStore.isTeacher() || userStore.isDeptAdmin() || userStore.isAdmin()) && row.internshipStatus !== 'exempt'"
                type="warning"
                link
                @click="handleOpenExempt(row)"
              >
                申请免实习
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
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
      v-model="detailVisible"
      title="实习记录详情"
      width="700px"
      destroy-on-close
    >
      <el-descriptions
        :column="2"
        border
      >
        <el-descriptions-item label="实习计划">
          {{ detailData.planName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="指导老师">
          {{ detailData.teacherName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="单位名称">
          {{ detailData.companyName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="单位地址">
          {{ detailData.companyAddress || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="单位指导老师姓名">
          {{ detailData.companyTeacherName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="单位指导老师联系电话">
          {{ detailData.companyTeacherPhone || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="单位岗位">
          {{ detailData.position || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="单位待遇">
          {{ detailData.salary ?? '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="实习开始时间">
          {{ formatDate(detailData.startDate) || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="实习结束时间">
          {{ formatDate(detailData.endDate) || '-' }}
        </el-descriptions-item>
        <el-descriptions-item
          v-if="!userStore.isStudent()"
          label="学生姓名"
        >
          {{ detailData.studentName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item
          v-if="!userStore.isStudent()"
          label="学号"
        >
          {{ detailData.studentNo || '-' }}
        </el-descriptions-item>
        <el-descriptions-item
          v-if="!userStore.isStudent()"
          label="实习状态"
        >
          <el-tag :type="internshipStatusTagType(detailData.internshipStatus)">
            {{ internshipStatusLabel(detailData.internshipStatus) }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <el-divider>附件</el-divider>
      <div
        v-if="detailData.safetyAgreementUrl || detailData.applicationFormUrl || detailData.acceptanceLetterUrl"
        style="margin-top: 16px;"
      >
        <div
          v-if="detailData.safetyAgreementUrl"
          style="margin-bottom: 10px; display: flex; align-items: center;"
        >
          <span style="color: #606266; margin-right: 8px; min-width: 90px;">安全协议书：</span>
          <el-button
            type="primary"
            link
            size="small"
            @click="handlePreviewFile(detailData.safetyAgreementUrl, '安全协议书')"
          >
            预览
          </el-button>
          <el-button
            type="primary"
            link
            size="small"
            @click="handleDownloadFile(detailData.safetyAgreementUrl, '安全协议书')"
          >
            下载
          </el-button>
        </div>
        <div
          v-if="detailData.applicationFormUrl"
          style="margin-bottom: 10px; display: flex; align-items: center;"
        >
          <span style="color: #606266; margin-right: 8px; min-width: 90px;">实习申请表：</span>
          <el-button
            type="primary"
            link
            size="small"
            @click="handlePreviewFile(detailData.applicationFormUrl, '实习申请表')"
          >
            预览
          </el-button>
          <el-button
            type="primary"
            link
            size="small"
            @click="handleDownloadFile(detailData.applicationFormUrl, '实习申请表')"
          >
            下载
          </el-button>
        </div>
        <div
          v-if="detailData.acceptanceLetterUrl"
          style="margin-bottom: 10px; display: flex; align-items: center;"
        >
          <span style="color: #606266; margin-right: 8px; min-width: 90px;">接收函：</span>
          <el-button
            type="primary"
            link
            size="small"
            @click="handlePreviewFile(detailData.acceptanceLetterUrl, '接收函')"
          >
            预览
          </el-button>
          <el-button
            type="primary"
            link
            size="small"
            @click="handleDownloadFile(detailData.acceptanceLetterUrl, '接收函')"
          >
            下载
          </el-button>
        </div>
      </div>
      <div
        v-else
        style="margin-top: 16px; color: #909399; text-align: center; padding: 16px; background: #f5f7fa; border-radius: 4px;"
      >
        暂无上传材料
      </div>
    </el-dialog>

    <el-dialog
      v-model="addDialogVisible"
      title="新增实习记录"
      width="700px"
      destroy-on-close
    >
      <el-form
        ref="addFormRef"
        :model="addForm"
        :rules="addFormRules"
        label-width="140px"
      >
        <el-form-item
          label="实习计划"
          prop="planId"
        >
          <el-select
            v-model="addForm.planId"
            placeholder="请选择实习计划"
            style="width: 100%"
          >
            <el-option
              v-for="item in planOptions"
              :key="item.id"
              :label="item.planName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="实习企业全称"
          prop="companyName"
        >
          <el-input
            v-model="addForm.companyName"
            placeholder="请输入实习企业全称"
          />
        </el-form-item>
        <el-form-item
          label="单位地址"
          prop="companyAddress"
        >
          <el-input
            v-model="addForm.companyAddress"
            placeholder="请输入单位地址"
          />
        </el-form-item>
        <el-form-item
          label="实习岗位"
          prop="position"
        >
          <el-input
            v-model="addForm.position"
            placeholder="请输入实习岗位"
          />
        </el-form-item>
        <el-form-item
          label="实习开始日期"
          prop="startDate"
        >
          <el-date-picker
            v-model="addForm.startDate"
            type="date"
            placeholder="请选择开始日期"
            value-format="YYYY-MM-DD"
            format="YYYY年MM月DD日"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item
          label="实习结束日期"
          prop="endDate"
        >
          <el-date-picker
            v-model="addForm.endDate"
            type="date"
            placeholder="请选择结束日期"
            value-format="YYYY-MM-DD"
            format="YYYY年MM月DD日"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item
          label="薪资"
          prop="salary"
        >
          <el-input-number
            v-model="addForm.salary"
            :min="0"
            :precision="2"
            :step="100"
            placeholder="请输入薪资"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item
          label="校外指导教师"
          prop="externalTeacher"
        >
          <el-input
            v-model="addForm.externalTeacher"
            placeholder="请输入校外指导教师"
          />
        </el-form-item>
        <el-form-item
          label="校外指导教师电话"
          prop="externalTeacherPhone"
        >
          <el-input
            v-model="addForm.externalTeacherPhone"
            maxlength="11"
            placeholder="请输入校外指导教师电话"
          />
        </el-form-item>
        <el-form-item
          label="家长姓名"
          prop="parentName"
        >
          <el-input
            v-model="addForm.parentName"
            placeholder="请输入家长姓名"
          />
        </el-form-item>
        <el-form-item
          label="家长电话"
          prop="parentPhone"
        >
          <el-input
            v-model="addForm.parentPhone"
            maxlength="11"
            placeholder="请输入家长电话"
          />
        </el-form-item>
        <el-form-item
          label="是否专业相关"
          prop="majorRelated"
        >
          <el-radio-group v-model="addForm.majorRelated">
            <el-radio :value="1">
              是
            </el-radio>
            <el-radio :value="0">
              否
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          label="实习内容"
          prop="content"
        >
          <el-input
            v-model="addForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入实习内容"
          />
        </el-form-item>
        <el-form-item label="安全协议书">
          <el-upload
            v-model:file-list="addForm.safetyAgreementFiles"
            action="/api/v1/files/upload"
            accept=".doc,.docx,.pdf,.jpg,.png"
            :limit="1"
            :headers="uploadHeaders"
            :on-success="(res, file) => handleUploadSuccess(res, file, 'safetyAgreement')"
            :on-remove="() => handleUploadRemove('safetyAgreement')"
            :before-upload="beforeUpload"
          >
            <el-button
              type="primary"
              size="small"
            >
              点击上传
            </el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="实习申请书">
          <el-upload
            v-model:file-list="addForm.applicationFormFiles"
            action="/api/v1/files/upload"
            accept=".doc,.docx,.pdf,.jpg,.png"
            :limit="1"
            :headers="uploadHeaders"
            :on-success="(res, file) => handleUploadSuccess(res, file, 'applicationForm')"
            :on-remove="() => handleUploadRemove('applicationForm')"
            :before-upload="beforeUpload"
          >
            <el-button
              type="primary"
              size="small"
            >
              点击上传
            </el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="接收函扫描件">
          <el-upload
            v-model:file-list="addForm.acceptanceLetterFiles"
            action="/api/v1/files/upload"
            accept=".doc,.docx,.pdf,.jpg,.png"
            :limit="1"
            :headers="uploadHeaders"
            :on-success="(res, file) => handleUploadSuccess(res, file, 'acceptanceLetter')"
            :on-remove="() => handleUploadRemove('acceptanceLetter')"
            :before-upload="beforeUpload"
          >
            <el-button
              type="primary"
              size="small"
            >
              点击上传
            </el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="addSubmitLoading"
          @click="handleAddSubmit"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="exemptDialogVisible"
      title="申请免实习"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="exemptFormRef"
        :model="exemptForm"
        :rules="exemptFormRules"
        label-width="100px"
      >
        <el-form-item label="学生姓名">
          <el-input
            :model-value="exemptForm.studentName"
            disabled
          />
        </el-form-item>
        <el-form-item label="学号">
          <el-input
            :model-value="exemptForm.studentNo"
            disabled
          />
        </el-form-item>
        <el-form-item
          label="免实习原因"
          prop="reason"
        >
          <el-input
            v-model="exemptForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入免实习原因（至少10个字符）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="exemptDialogVisible = false">
          取消
        </el-button>
        <el-button
          type="warning"
          :loading="exemptSubmitLoading"
          @click="handleExemptSubmit"
        >
          提交申请
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="previewDialogVisible"
      :title="previewTitle"
      width="80%"
      top="5vh"
      destroy-on-close
    >
      <div style="text-align: center;">
        <iframe
          v-if="previewUrl"
          :src="previewUrl"
          style="width: 100%; height: 75vh; border: none;"
        />
      </div>
      <template #footer>
        <el-button @click="previewDialogVisible = false">
          关闭
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus, CircleCheck } from '@element-plus/icons-vue'
import request from '@/utils/request.js'
import { useUserStore } from '@/stores/user.js'
import { formatDate } from '@/utils/dateFormat'
import { useTableResize } from '@/composables/useTableResize.js'

const userStore = useUserStore()

const tableRef1 = ref(null)
const tableRef2 = ref(null)
useTableResize(tableRef1)
useTableResize(tableRef2)

const internshipStatusOptions = [
  { value: 'active', label: '实习中' },
  { value: 'resigned', label: '已提前离职' },
  { value: 'exempt', label: '免实习' },
  { value: 'expired', label: '已结束' }
]

const planOptions = ref([])
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

const queryForm = reactive({
  current: 1,
  size: 10,
  studentName: '',
  studentNo: '',
  teacherName: '',
  planId: null,
  startDate: '',
  endDate: '',
  internshipStatus: null
})

const detailVisible = ref(false)
const detailData = ref({})

const addDialogVisible = ref(false)
const addSubmitLoading = ref(false)
const addFormRef = ref(null)

const addForm = reactive({
  planId: null,
  companyName: '',
  companyAddress: '',
  position: '',
  startDate: '',
  endDate: '',
  salary: null,
  externalTeacher: '',
  externalTeacherPhone: '',
  parentName: '',
  parentPhone: '',
  majorRelated: 1,
  content: '',
  safetyAgreementUrl: '',
  safetyAgreementFiles: [],
  applicationFormUrl: '',
  applicationFormFiles: [],
  acceptanceLetterUrl: '',
  acceptanceLetterFiles: []
})

const validateEndDate = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请选择结束日期'))
  } else if (addForm.startDate && value < addForm.startDate) {
    callback(new Error('结束日期不能早于开始日期'))
  } else {
    callback()
  }
}

const addFormRules = {
  planId: [{ required: true, message: '请选择实习计划', trigger: 'change' }],
  companyName: [{ required: true, message: '请输入实习企业全称', trigger: 'blur' }],
  position: [{ required: true, message: '请输入实习岗位', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, validator: validateEndDate, trigger: 'change' }],
  externalTeacherPhone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  parentPhone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  content: [{ required: true, message: '请输入实习内容', trigger: 'blur' }]
}

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

const selectedRows = ref([])

const exemptDialogVisible = ref(false)
const exemptSubmitLoading = ref(false)
const exemptFormRef = ref(null)
const exemptForm = reactive({
  studentId: null,
  studentName: '',
  studentNo: '',
  reason: ''
})

const exemptFormRules = {
  reason: [
    { required: true, message: '请输入免实习原因', trigger: 'blur' },
    { min: 10, message: '免实习原因至少输入10个字符', trigger: 'blur' }
  ]
}

const previewDialogVisible = ref(false)
const previewUrl = ref('')
const previewTitle = ref('')

function internshipStatusTagType(status) {
  const map = { active: 'success', resigned: 'danger', exempt: 'warning', expired: 'info' }
  return map[status] || 'info'
}

function internshipStatusLabel(status) {
  const map = { active: '实习中', resigned: '已提前离职', exempt: '免实习', expired: '已结束' }
  return map[status] || '未知'
}

async function loadPlanOptions() {
  try {
    const url = userStore.isStudent() ? '/internship/plans/my-plans' : '/internship/plans'
    const res = await request.get(url, { params: { size: 100 } })
    planOptions.value = res.data.records || []
  } catch (e) {
    console.warn('获取实习计划列表失败', e.message)
  }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await request.get('/internship/records', {
      params: {
        current: queryForm.current,
        size: queryForm.size,
        studentName: queryForm.studentName || undefined,
        studentNo: queryForm.studentNo || undefined,
        teacherName: queryForm.teacherName || undefined,
        planId: queryForm.planId || undefined,
        startDate: queryForm.startDate || undefined,
        endDate: queryForm.endDate || undefined,
        internshipStatus: queryForm.internshipStatus !== null && queryForm.internshipStatus !== '' ? queryForm.internshipStatus : undefined
      }
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.warn('获取实习记录列表失败', e.message)
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
  queryForm.studentNo = ''
  queryForm.teacherName = ''
  queryForm.planId = null
  queryForm.startDate = ''
  queryForm.endDate = ''
  queryForm.internshipStatus = null
  queryForm.current = 1
  fetchData()
}

async function handleViewDetail(row) {
  try {
    const res = await request.get(`/internship/records/${row.id}`)
    detailData.value = res.data || {}
    detailVisible.value = true
  } catch (e) {
    console.warn('获取实习记录详情失败', e.message)
  }
}

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

function handleOpenExempt(row) {
  if (row) {
    exemptForm.studentId = row.studentId
    exemptForm.studentName = row.studentName
    exemptForm.studentNo = row.studentNo
  } else if (selectedRows.value.length === 1) {
    const selected = selectedRows.value[0]
    exemptForm.studentId = selected.studentId
    exemptForm.studentName = selected.studentName
    exemptForm.studentNo = selected.studentNo
  } else if (selectedRows.value.length > 1) {
    ElMessage.warning('申请免实习每次只能选择一名学生')
    return
  } else {
    ElMessage.warning('请先选择一名学生')
    return
  }
  exemptForm.reason = ''
  exemptDialogVisible.value = true
}

async function handleExemptSubmit() {
  const valid = await exemptFormRef.value.validate().catch(() => false)
  if (!valid) return

  exemptSubmitLoading.value = true
  try {
    await request.post('/internship/student-changes/exempt', {
      studentId: exemptForm.studentId,
      reason: exemptForm.reason
    })
    ElMessage.success('申请免实习成功')
    exemptDialogVisible.value = false
    fetchData()
  } catch (e) {
    console.warn('申请免实习失败', e.message)
  } finally {
    exemptSubmitLoading.value = false
  }
}

function handlePreviewFile(url, title) {
  if (!url) {
    ElMessage.warning('文件不存在')
    return
  }
  const lower = url.toLowerCase()
  if (lower.endsWith('.doc') || lower.endsWith('.docx')) {
    ElMessage.warning('Word 文件不支持浏览器预览，请下载后查看')
    handleDownloadFile(url, title)
    return
  }
  previewTitle.value = `预览：${title}`
  previewUrl.value = url
  previewDialogVisible.value = true
}

function handleDownloadFile(url, filename) {
  if (!url) {
    ElMessage.warning('文件不存在')
    return
  }
  const link = document.createElement('a')
  link.href = url
  link.download = filename || 'download'
  link.target = '_blank'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  ElMessage.success('开始下载')
}

function handleAdd() {
  resetAddForm()
  addDialogVisible.value = true
}

function resetAddForm() {
  addForm.planId = null
  addForm.companyName = ''
  addForm.companyAddress = ''
  addForm.position = ''
  addForm.startDate = ''
  addForm.endDate = ''
  addForm.salary = null
  addForm.externalTeacher = ''
  addForm.externalTeacherPhone = ''
  addForm.parentName = ''
  addForm.parentPhone = ''
  addForm.majorRelated = 1
  addForm.content = ''
  addForm.safetyAgreementUrl = ''
  addForm.safetyAgreementFiles = []
  addForm.applicationFormUrl = ''
  addForm.applicationFormFiles = []
  addForm.acceptanceLetterUrl = ''
  addForm.acceptanceLetterFiles = []
}

function handleUploadSuccess(response, uploadFile, field) {
  uploadFile.url = response.data?.filePath
  if (field === 'safetyAgreement') {
    addForm.safetyAgreementUrl = response.data?.filePath
  } else if (field === 'applicationForm') {
    addForm.applicationFormUrl = response.data?.filePath
  } else if (field === 'acceptanceLetter') {
    addForm.acceptanceLetterUrl = response.data?.filePath
  }
}

function handleUploadRemove(field) {
  if (field === 'safetyAgreement') {
    addForm.safetyAgreementUrl = ''
  } else if (field === 'applicationForm') {
    addForm.applicationFormUrl = ''
  } else if (field === 'acceptanceLetter') {
    addForm.acceptanceLetterUrl = ''
  }
}

function beforeUpload(file) {
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.warning('文件大小不能超过10MB')
    return false
  }
  return true
}

async function handleAddSubmit() {
  const valid = await addFormRef.value.validate().catch(() => false)
  if (!valid) return

  addSubmitLoading.value = true
  try {
    const submitData = {
      planId: addForm.planId,
      companyName: addForm.companyName,
      companyAddress: addForm.companyAddress,
      position: addForm.position,
      startDate: addForm.startDate,
      endDate: addForm.endDate,
      salary: addForm.salary,
      externalTeacher: addForm.externalTeacher,
      externalTeacherPhone: addForm.externalTeacherPhone,
      parentName: addForm.parentName,
      parentPhone: addForm.parentPhone,
      majorRelated: addForm.majorRelated,
      content: addForm.content,
      safetyAgreementUrl: addForm.safetyAgreementUrl,
      applicationFormUrl: addForm.applicationFormUrl,
      acceptanceLetterUrl: addForm.acceptanceLetterUrl
    }
    await request.post('/internship/records', submitData)
    ElMessage.success('新增成功')
    addDialogVisible.value = false
    fetchData()
  } catch (e) {
    console.warn('新增实习记录失败', e.message)
  } finally {
    addSubmitLoading.value = false
  }
}

onMounted(() => {
  loadPlanOptions()
  fetchData()
})

onBeforeUnmount(() => {
  detailVisible.value = false
  addDialogVisible.value = false
  exemptDialogVisible.value = false
  previewDialogVisible.value = false
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
