<template>
  <div class="page-container">
    <template v-if="userStore.isStudent()">
      <el-card>
        <el-alert
          v-if="resubmitHint"
          :title="resubmitHint"
          type="warning"
          show-icon
          :closable="false"
          style="margin-bottom: 16px;"
        />
        <el-form
          ref="studentFormRef"
          :model="studentForm"
          :rules="studentFormRules"
          label-width="180px"
          :disabled="isFormReadonly"
        >
          <el-form-item label="实习计划" prop="planId">
            <el-select
              v-model="studentForm.planId"
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
          <el-form-item label="审批状态">
            <el-tag :type="statusTagType(studentForm.status)">
              {{ statusLabel(studentForm.status) }}
            </el-tag>
          </el-form-item>
          <el-form-item label="实习企业全称" prop="companyName">
            <el-input
              v-model="studentForm.companyName"
              placeholder="请输入企业全称"
            />
          </el-form-item>
          <el-form-item label="实习岗位" prop="position">
            <el-input
              v-model="studentForm.position"
              placeholder="请输入实习岗位"
            />
          </el-form-item>
          <el-form-item label="实习单位地址" prop="companyAddress">
            <el-input
              v-model="studentForm.companyAddress"
              placeholder="请输入实习单位地址"
            />
          </el-form-item>
          <el-form-item label="实习开始日期" prop="startDate">
            <el-date-picker
              v-model="studentForm.startDate"
              type="date"
              placeholder="请选择开始日期"
              value-format="YYYY-MM-DD"
              format="YYYY年MM月DD日"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="实习结束日期" prop="endDate">
            <el-date-picker
              v-model="studentForm.endDate"
              type="date"
              placeholder="请选择结束日期"
              value-format="YYYY-MM-DD"
              format="YYYY年MM月DD日"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="日薪（元/天）" prop="salary">
            <el-input-number
              v-model="studentForm.salary"
              :min="0"
              :precision="2"
              :step="10"
              placeholder="请输入日薪"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="实习单位指导教师" prop="externalTeacher">
            <el-input
              v-model="studentForm.externalTeacher"
              placeholder="请输入实习单位的指导教师"
            />
          </el-form-item>
          <el-form-item label="实习单位指导教师电话" prop="externalTeacherPhone">
            <el-input
              v-model="studentForm.externalTeacherPhone"
              placeholder="请输入实习单位的指导教师电话"
            />
          </el-form-item>
          <el-form-item label="安全协议书上传" prop="safetyAgreementUrl">
            <el-upload
              ref="safetyUploadRef"
              action="/api/v1/files/upload"
              :limit="1"
              accept=".pdf"
              :headers="uploadHeaders"
              :on-success="handleSafetySuccess"
              :on-remove="handleSafetyRemove"
              :on-error="handleUploadError"
              :on-exceed="() => ElMessage.warning('只能上传一个文件，请先移除已选文件')"
              :before-upload="beforePdfUpload"
            >
              <el-button type="primary">选择文件</el-button>
              <template #tip>
                <div class="el-upload__tip">仅支持 .pdf 格式，最大 10MB</div>
              </template>
            </el-upload>
          </el-form-item>
          <el-form-item label="实习申请书上传" prop="applicationFormUrl">
            <el-upload
              ref="applicationUploadRef"
              action="/api/v1/files/upload"
              :limit="1"
              accept=".pdf"
              :headers="uploadHeaders"
              :on-success="handleApplicationSuccess"
              :on-remove="handleApplicationRemove"
              :on-error="handleUploadError"
              :on-exceed="() => ElMessage.warning('只能上传一个文件，请先移除已选文件')"
              :before-upload="beforePdfUpload"
            >
              <el-button type="primary">选择文件</el-button>
              <template #tip>
                <div class="el-upload__tip">仅支持 .pdf 格式，最大 10MB</div>
              </template>
            </el-upload>
          </el-form-item>
          <el-form-item label="接收函扫描件上传">
            <el-upload
              ref="acceptanceUploadRef"
              action="/api/v1/files/upload"
              :limit="1"
              accept=".doc,.docx,.pdf"
              :headers="uploadHeaders"
              :on-success="handleAcceptanceSuccess"
              :on-remove="handleAcceptanceRemove"
              :on-error="handleUploadError"
              :on-exceed="() => ElMessage.warning('只能上传一个文件，请先移除已选文件')"
              :before-upload="beforeDocPdfUpload"
            >
              <el-button type="primary">选择文件</el-button>
              <template #tip>
                <div class="el-upload__tip">支持 .doc/.docx/.pdf 格式，最大 10MB</div>
              </template>
            </el-upload>
          </el-form-item>
        </el-form>
        <div v-if="studentForm.status === STATUS_UNSUBMITTED" class="form-footer">
          <el-button
            type="primary"
            :loading="submitLoading"
            @click="handleStudentSubmit"
          >
            提交申请
          </el-button>
        </div>
        <div v-if="studentForm.status === STATUS_PENDING" class="form-footer">
          <el-button disabled>
            待审核中，无法修改
          </el-button>
        </div>
        <div v-if="studentForm.status === STATUS_REJECTED" class="form-footer">
          <template v-if="!isEditMode">
            <el-button
              type="primary"
              @click="handleEnterEditMode"
            >
              修改
            </el-button>
          </template>
          <template v-else>
            <el-button
              type="primary"
              :loading="submitLoading"
              @click="handleStudentSubmit"
            >
              提交申请
            </el-button>
            <el-button @click="handleCancelEdit">
              取消修改
            </el-button>
          </template>
        </div>
        <div v-if="studentForm.status === STATUS_APPROVED" class="form-footer">
          <el-button disabled>
            审核已通过，无法修改
          </el-button>
          <el-button
            v-if="canFinishInternship"
            type="success"
            :loading="finishLoading"
            @click="handleFinishInternship"
          >
            结束实习
          </el-button>
          <el-button
            type="danger"
            :loading="resignLoading"
            @click="handleOpenResign"
          >
            申请提前离职
          </el-button>
        </div>
        <div v-if="studentForm.status === STATUS_RESIGNED" class="form-footer">
          <el-button disabled>
            审核已通过，无法修改
          </el-button>
        </div>
      </el-card>
    </template>

    <template v-else>
      <el-card>
        <template #header>
          <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
            <h2 style="margin: 0;">实习申请管理</h2>
            <el-button
              type="success"
              :disabled="!selectedRows.length"
              @click="handleBatchReview"
            >
              批量审批
            </el-button>
          </div>
        </template>
        <div class="filter-bar">
          <el-input
            v-model="queryForm.studentName"
            placeholder="学生姓名"
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
            v-model="queryForm.status"
            placeholder="申请状态"
            clearable
            style="width: 140px"
          >
            <el-option
              v-for="item in teacherStatusOptions"
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
        </div>
        <el-table
          ref="tableRef"
          v-loading="loading"
          :data="tableData"
          border
          stripe
          style="width: 100%"
          @selection-change="handleSelectionChange"
        >
          <el-table-column
            type="selection"
            width="50"
          />
          <el-table-column
            prop="studentNo"
            label="学号"
            min-width="130"
          />
          <el-table-column
            prop="studentName"
            label="学生姓名"
            min-width="100"
          />
          <el-table-column
            prop="companyName"
            label="实习单位"
            min-width="180"
            show-overflow-tooltip
          />
          <el-table-column
            prop="position"
            label="岗位"
            min-width="120"
            show-overflow-tooltip
          />
          <el-table-column
            prop="startDate"
            label="开始日期"
            min-width="110"
          >
            <template #default="{ row }">
              {{ formatDate(row.startDate) }}
            </template>
          </el-table-column>
          <el-table-column
            prop="endDate"
            label="结束日期"
            min-width="110"
          >
            <template #default="{ row }">
              {{ formatDate(row.endDate) }}
            </template>
          </el-table-column>
          <el-table-column
            prop="status"
            label="状态"
            min-width="100"
          >
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)">
                {{ statusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column
            label="操作"
            width="160"
            :resizable="false"
          >
            <template #default="{ row }">
              <el-button
                type="primary"
                link
                @click="handleViewOnly(row)"
              >
                查看
              </el-button>
              <el-button
                v-if="row.status === STATUS_PENDING || row.status === STATUS_REJECTED"
                type="warning"
                link
                @click="handleOpenApprove(row)"
              >
                审批
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
        v-model="detailDialogVisible"
        :title="dialogTitle"
        width="720px"
        top="5vh"
        destroy-on-close
      >
        <el-descriptions
          :column="2"
          border
        >
          <el-descriptions-item label="学号">{{ detailData.studentNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="实习计划">{{ detailData.planName }}</el-descriptions-item>
          <el-descriptions-item label="审批状态" :span="2">
            <el-tag :type="statusTagType(detailData.status)">{{ statusLabel(detailData.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="实习企业全称" :span="2">{{ detailData.companyName }}</el-descriptions-item>
          <el-descriptions-item label="实习岗位">{{ detailData.position }}</el-descriptions-item>
          <el-descriptions-item label="实习单位地址">{{ detailData.companyAddress }}</el-descriptions-item>
          <el-descriptions-item label="实习开始日期">{{ formatDate(detailData.startDate) }}</el-descriptions-item>
          <el-descriptions-item label="实习结束日期">{{ formatDate(detailData.endDate) }}</el-descriptions-item>
          <el-descriptions-item label="日薪（元/天）">{{ detailData.salary }}</el-descriptions-item>
          <el-descriptions-item label="指导教师">{{ detailData.externalTeacher }}</el-descriptions-item>
          <el-descriptions-item label="指导教师电话">{{ detailData.externalTeacherPhone }}</el-descriptions-item>
        </el-descriptions>
        <div
          v-if="detailData.safetyAgreementUrl || detailData.applicationFormUrl || detailData.acceptanceLetterUrl"
          style="margin-top: 16px;"
        >
          <h4 style="margin-bottom: 12px; color: #303133;">上传材料</h4>
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
        <template #footer>
          <template v-if="viewMode === 'view'">
            <el-button @click="detailDialogVisible = false">关闭</el-button>
          </template>
          <template v-else-if="isBatchMode">
            <div style="display: flex; justify-content: space-between; width: 100%;">
              <div>
                <el-button
                  size="small"
                  :disabled="batchCurrentIndex <= 0"
                  @click="handleBatchPrev"
                >
                  ← 上一条
                </el-button>
                <span style="margin: 0 12px; color: #606266;">{{ batchCurrentIndex + 1 }} / {{ batchReviewList.length }}</span>
                <el-button
                  size="small"
                  :disabled="batchCurrentIndex >= batchReviewList.length - 1"
                  @click="handleBatchNext"
                >
                  下一条 →
                </el-button>
              </div>
              <div>
                <template v-if="detailData.status === STATUS_PENDING || detailData.status === STATUS_REJECTED">
                  <el-button
                    type="success"
                    size="small"
                    @click="handleApproveDetail"
                  >
                    通过
                  </el-button>
                  <el-button
                    type="danger"
                    size="small"
                    @click="handleRejectDetail"
                  >
                    驳回
                  </el-button>
                </template>
                <el-button
                  v-if="detailData.status === STATUS_APPROVED"
                  type="warning"
                  size="small"
                  @click="handleDetailResign"
                >
                  离职
                </el-button>
                <el-button
                  type="success"
                  size="small"
                  :disabled="!hasPendingOrRejectedItems"
                  :loading="batchApproveLoading"
                  @click="handleBatchApproveAll"
                >
                  全部通过
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  :disabled="!hasPendingOrRejectedItems"
                  :loading="batchRejectLoading"
                  @click="handleBatchRejectAll"
                >
                  全部驳回
                </el-button>
                <el-button
                  size="small"
                  @click="detailDialogVisible = false"
                >
                  关闭
                </el-button>
              </div>
            </div>
          </template>
          <template v-else>
            <template v-if="detailData.status === STATUS_PENDING || detailData.status === STATUS_REJECTED">
              <el-button
                type="success"
                @click="handleApproveDetail"
              >
                审批通过
              </el-button>
              <el-button
                type="danger"
                @click="handleRejectDetail"
              >
                驳回
              </el-button>
            </template>
            <el-button
              v-if="detailData.status === STATUS_APPROVED"
              type="warning"
              @click="handleDetailResign"
            >
              标记提前离职
            </el-button>
            <el-button @click="detailDialogVisible = false">关闭</el-button>
          </template>
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
          <el-button @click="previewDialogVisible = false">关闭</el-button>
        </template>
      </el-dialog>

      <el-dialog
        v-model="rejectDialogVisible"
        title="驳回申请"
        width="480px"
        destroy-on-close
      >
        <el-form
          ref="rejectFormRef"
          :model="rejectForm"
          :rules="rejectFormRules"
          label-width="80px"
        >
          <el-form-item
            label="驳回原因"
            prop="reason"
          >
            <el-input
              v-model="rejectForm.reason"
              type="textarea"
              :rows="3"
              placeholder="请输入驳回原因（至少10个字符）"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button
            type="danger"
            :loading="rejectLoading"
            @click="handleRejectSubmit"
          >
            确定驳回
          </el-button>
        </template>
      </el-dialog>

      <el-dialog
        v-model="resignDialogVisible"
        title="申请提前离职"
        width="500px"
        destroy-on-close
      >
        <el-form
          ref="resignFormRef"
          :model="resignForm"
          :rules="resignFormRules"
          label-width="100px"
        >
          <el-form-item
            label="提前离职原因"
            prop="reason"
          >
            <el-input
              v-model="resignForm.reason"
              type="textarea"
              :rows="4"
              placeholder="请详细说明提前离职原因（至少10个字）"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="resignDialogVisible = false">
            取消
          </el-button>
          <el-button
            type="danger"
            :loading="resignLoading"
            @click="confirmResign"
          >
            提交申请
          </el-button>
        </template>
      </el-dialog>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request.js'
import { useUserStore } from '@/stores/user.js'
import { formatDate } from '@/utils/dateFormat'
import { useTableResize } from '@/composables/useTableResize.js'

const userStore = useUserStore()

const tableRef = ref(null)
useTableResize(tableRef)

const STATUS_UNSUBMITTED = -1
const STATUS_PENDING = 0
const STATUS_APPROVED = 1
const STATUS_REJECTED = 2
const STATUS_RESIGNED = 3
const STATUS_WITHDRAWN = 4

const statusMap = {
  [STATUS_UNSUBMITTED]: { label: '未提交', tagType: 'info' },
  [STATUS_PENDING]: { label: '待审核', tagType: 'warning' },
  [STATUS_APPROVED]: { label: '已通过', tagType: 'success' },
  [STATUS_REJECTED]: { label: '已驳回', tagType: 'danger' },
  [STATUS_RESIGNED]: { label: '已提前离职', tagType: 'info' },
  [STATUS_WITHDRAWN]: { label: '已撤回', tagType: 'info' }
}

const teacherStatusOptions = [
  { value: STATUS_UNSUBMITTED, label: '未提交' },
  { value: STATUS_PENDING, label: '待审核' },
  { value: STATUS_REJECTED, label: '已驳回' },
  { value: STATUS_APPROVED, label: '已通过' },
  { value: STATUS_RESIGNED, label: '已提前离职' }
]

function statusTagType(status) {
  return statusMap[status]?.tagType || 'info'
}

function statusLabel(status) {
  return statusMap[status]?.label || '未知'
}

const planOptions = ref([])
const loading = ref(false)
const submitLoading = ref(false)
const resignLoading = ref(false)
const finishLoading = ref(false)
const internshipRecordId = ref(null)
const internshipRecordStatus = ref('')
const resignDialogVisible = ref(false)
const resignFormRef = ref(null)
const resignForm = reactive({
  reason: ''
})
const resignFormRules = {
  reason: [
    { required: true, message: '请填写提前提前离职原因', trigger: 'blur' },
    { min: 10, message: '提前提前离职原因至少10个字', trigger: 'blur' }
  ]
}
const isEditMode = ref(false)

const studentFormRef = ref(null)
const safetyUploadRef = ref(null)
const applicationUploadRef = ref(null)
const acceptanceUploadRef = ref(null)

const studentForm = reactive({
  id: null,
  planId: null,
  status: STATUS_UNSUBMITTED,
  companyName: '',
  position: '',
  companyAddress: '',
  startDate: '',
  endDate: '',
  salary: null,
  externalTeacher: '',
  externalTeacherPhone: '',
  safetyAgreementUrl: '',
  applicationFormUrl: '',
  acceptanceLetterUrl: ''
})

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

const isFormReadonly = computed(() => {
  if (studentForm.status === STATUS_APPROVED || studentForm.status === STATUS_RESIGNED) {
    return true
  }
  if (studentForm.status === STATUS_PENDING) {
    return true
  }
  if (studentForm.status === STATUS_REJECTED) {
    return !isEditMode.value
  }
  return false
})

const canFinishInternship = computed(() => {
  if (studentForm.status !== STATUS_APPROVED) return false
  if (!internshipRecordId.value) return false
  if (!studentForm.endDate) return false
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const endDate = new Date(studentForm.endDate)
  return today >= endDate
})

const resubmitHint = computed(() => {
  if (studentForm.status !== STATUS_UNSUBMITTED) return ''
  if (internshipRecordStatus.value === 'expired') return '您的实习已超期，请重新提交实习申请'
  if (internshipRecordStatus.value === 'resigned') return '您已提前离职，请重新提交实习申请'
  return ''
})

const hasPendingOrRejectedItems = computed(() => {
  return batchReviewList.value.some(item => item.status === STATUS_PENDING || item.status === STATUS_REJECTED)
})

const dialogTitle = computed(() => {
  if (isBatchMode.value) {
    return `批量审批 (${batchCurrentIndex.value + 1}/${batchReviewList.value.length}) - ${detailData.studentName}`
  }
  if (viewMode.value === 'view') {
    return '申请详情'
  }
  return '审批申请'
})

const validateEndDate = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请选择结束日期'))
  } else if (studentForm.startDate && value < studentForm.startDate) {
    callback(new Error('结束日期必须晚于开始日期'))
  } else {
    callback()
  }
}

const validateSafetyFile = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请上传安全协议书'))
  } else {
    callback()
  }
}

const validateApplicationFile = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请上传实习申请书'))
  } else {
    callback()
  }
}

const studentFormRules = {
  planId: [{ required: true, message: '请选择实习计划', trigger: 'change' }],
  companyName: [{ required: true, message: '请输入企业全称', trigger: 'blur' }],
  position: [{ required: true, message: '请输入实习岗位', trigger: 'blur' }],
  companyAddress: [{ required: true, message: '请输入实习单位地址', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, validator: validateEndDate, trigger: 'change' }],
  salary: [{ required: true, message: '请输入日薪', trigger: 'blur' }],
  externalTeacher: [{ required: true, message: '请输入实习单位指导教师', trigger: 'blur' }],
  externalTeacherPhone: [
    { required: true, message: '请输入实习单位指导教师电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  safetyAgreementUrl: [{ required: true, validator: validateSafetyFile, trigger: 'change' }],
  applicationFormUrl: [{ required: true, validator: validateApplicationFile, trigger: 'change' }]
}

const queryForm = reactive({
  current: 1,
  size: 10,
  studentName: '',
  planId: null,
  status: null
})

const tableData = ref([])
const total = ref(0)
const selectedRows = ref([])

const detailDialogVisible = ref(false)
const isBatchMode = ref(false)
const viewMode = ref('view')
const batchCurrentIndex = ref(0)
const batchReviewList = ref([])
const batchApproveLoading = ref(false)
const batchRejectLoading = ref(false)
const previewDialogVisible = ref(false)
const previewUrl = ref('')
const previewTitle = ref('')
const detailData = reactive({
  id: null,
  studentNo: '',
  planName: '',
  status: STATUS_UNSUBMITTED,
  companyName: '',
  position: '',
  companyAddress: '',
  startDate: '',
  endDate: '',
  salary: null,
  externalTeacher: '',
  externalTeacherPhone: '',
  safetyAgreementUrl: '',
  applicationFormUrl: '',
  acceptanceLetterUrl: ''
})

const rejectDialogVisible = ref(false)
const rejectLoading = ref(false)
const rejectFormRef = ref(null)
const rejectId = ref(null)
const rejectForm = reactive({
  reason: ''
})

const rejectFormRules = {
  reason: [
    { required: true, message: '请输入驳回原因', trigger: 'blur' },
    { min: 10, message: '驳回原因至少输入10个字符', trigger: 'blur' }
  ]
}

function beforePdfUpload(file) {
  const isPdf = file.type === 'application/pdf'
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isPdf) {
    ElMessage.error('仅支持上传 PDF 格式文件')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB')
    return false
  }
  return true
}

function beforeDocPdfUpload(file) {
  const allowedTypes = [
    'application/pdf',
    'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
  ]
  const isAllowed = allowedTypes.includes(file.type)
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isAllowed) {
    ElMessage.error('仅支持上传 .doc/.docx/.pdf 格式文件')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB')
    return false
  }
  return true
}

function handleSafetySuccess(response) {
  studentForm.safetyAgreementUrl = response.data?.filePath
}

function handleSafetyRemove() {
  studentForm.safetyAgreementUrl = ''
}

function handleApplicationSuccess(response) {
  studentForm.applicationFormUrl = response.data?.filePath
}

function handleApplicationRemove() {
  studentForm.applicationFormUrl = ''
}

function handleAcceptanceSuccess(response) {
  studentForm.acceptanceLetterUrl = response.data?.filePath
}

function handleAcceptanceRemove() {
  studentForm.acceptanceLetterUrl = ''
}

function handleUploadError(error) {
  let msg = '文件上传失败'
  try {
    const data = JSON.parse(error.message)
    msg = data.message || msg
  } catch (e) {
    console.warn(e)
  }
  ElMessage.error(msg)
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

async function loadStudentApplication() {
  loading.value = true
  isEditMode.value = false
  try {
    const res = await request.get('/internship/applications/my')
    const data = res.data
    if (data) {
      studentForm.id = data.id
      studentForm.planId = data.planId
      studentForm.status = data.status ?? STATUS_UNSUBMITTED
      studentForm.companyName = data.companyName || ''
      studentForm.position = data.position || ''
      studentForm.companyAddress = data.companyAddress || ''
      studentForm.startDate = data.startDate || ''
      studentForm.endDate = data.endDate || ''
      studentForm.salary = data.salary ?? null
      studentForm.externalTeacher = data.externalTeacher || ''
      studentForm.externalTeacherPhone = data.externalTeacherPhone || ''
      studentForm.safetyAgreementUrl = data.safetyAgreementUrl || ''
      studentForm.applicationFormUrl = data.applicationFormUrl || ''
      studentForm.acceptanceLetterUrl = data.acceptanceLetterUrl || ''
    }
    if (studentForm.status === STATUS_APPROVED || studentForm.status === STATUS_UNSUBMITTED) {
      try {
        const recordRes = await request.get('/internship/records/my')
        if (recordRes.data) {
          internshipRecordId.value = recordRes.data.id
          internshipRecordStatus.value = recordRes.data.internshipStatus || ''
        }
      } catch (e) {
        console.warn('获取实习记录失败', e.message)
      }
    } else {
      internshipRecordId.value = null
      internshipRecordStatus.value = ''
    }
  } catch (e) {
    console.warn('获取实习申请失败', e.message)
  } finally {
    loading.value = false
  }
}

async function handleStudentSubmit() {
  const valid = await studentFormRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const submitData = {
      planId: studentForm.planId,
      companyName: studentForm.companyName,
      position: studentForm.position,
      companyAddress: studentForm.companyAddress,
      startDate: studentForm.startDate,
      endDate: studentForm.endDate,
      salary: studentForm.salary,
      externalTeacher: studentForm.externalTeacher,
      externalTeacherPhone: studentForm.externalTeacherPhone,
      safetyAgreementUrl: studentForm.safetyAgreementUrl || undefined,
      applicationFormUrl: studentForm.applicationFormUrl || undefined,
      acceptanceLetterUrl: studentForm.acceptanceLetterUrl || undefined
    }

    if (studentForm.id) {
      await request.put(`/internship/applications/${studentForm.id}`, submitData)
      ElMessage.success('更新成功')
    } else {
      await request.post('/internship/applications', submitData)
      ElMessage.success('提交成功')
    }
    isEditMode.value = false
    loadStudentApplication()
  } catch (e) {
    console.warn('提交实习申请失败', e.message)
  } finally {
    submitLoading.value = false
  }
}

function handleEnterEditMode() {
  isEditMode.value = true
}

async function handleCancelEdit() {
  try {
    await ElMessageBox.confirm(
      '确定要取消修改吗？未保存的更改将丢失。',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '继续编辑',
        type: 'warning'
      }
    )
    isEditMode.value = false
    loadStudentApplication()
  } catch (e) {
    if (e !== 'cancel') {
      console.warn('取消修改失败', e.message)
    }
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

async function handleOpenResign() {
  resignForm.reason = ''
  resignDialogVisible.value = true
  nextTick(() => {
    resignFormRef.value?.clearValidate()
  })
}

async function confirmResign() {
  const valid = await resignFormRef.value?.validate().catch(() => false)
  if (!valid) return
  resignLoading.value = true
  try {
    await request.post('/internship/student-changes/resign', {
      planId: studentForm.planId,
      reason: resignForm.reason
    })
    ElMessage.success('提前离职申请已提交，请等待指导老师或管理员审批')
    resignDialogVisible.value = false
  } catch (e) {
    console.warn('提交提前离职申请失败', e.message)
  } finally {
    resignLoading.value = false
  }
}

async function handleFinishInternship() {
  try {
    await ElMessageBox.confirm(
      '确定要结束当前实习吗？结束后将无法再进行签到、提交报告等操作。',
      '结束实习确认',
      {
        confirmButtonText: '确认结束',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    finishLoading.value = true
    await request.put(`/internship/records/${internshipRecordId.value}/finish`)
    ElMessage.success('实习已结束')
    loadStudentApplication()
  } catch (e) {
    if (e !== 'cancel') {
      console.warn('结束实习失败', e.message)
    }
  } finally {
    finishLoading.value = false
  }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await request.get('/internship/applications', {
      params: {
        current: queryForm.current,
        size: queryForm.size,
        studentName: queryForm.studentName || undefined,
        planId: queryForm.planId || undefined,
        status: queryForm.status !== null && queryForm.status !== '' ? queryForm.status : undefined
      }
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.warn('获取实习申请列表失败', e.message)
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
  queryForm.planId = null
  queryForm.status = null
  queryForm.current = 1
  fetchData()
}

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

function handleBatchReview() {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先选择要审批的申请')
    return
  }
  batchReviewList.value = [...selectedRows.value]
  isBatchMode.value = true
  viewMode.value = 'batch'
  batchCurrentIndex.value = 0
  const firstRow = batchReviewList.value[0]
  handleViewDetail(firstRow).then(() => {
    detailDialogVisible.value = true
  })
}

function handleBatchPrev() {
  if (batchCurrentIndex.value > 0) {
    batchCurrentIndex.value--
    const row = batchReviewList.value[batchCurrentIndex.value]
    handleViewDetail(row)
  }
}

function handleBatchNext() {
  if (batchCurrentIndex.value < batchReviewList.value.length - 1) {
    batchCurrentIndex.value++
    const row = batchReviewList.value[batchCurrentIndex.value]
    handleViewDetail(row)
  }
}

async function handleViewOnly(row) {
  isBatchMode.value = false
  viewMode.value = 'view'
  await handleViewDetail(row)
  detailDialogVisible.value = true
}

async function handleOpenApprove(row) {
  isBatchMode.value = false
  viewMode.value = 'approve'
  await handleViewDetail(row)
  detailDialogVisible.value = true
}

async function handleReviewApprove(row) {
  try {
    await ElMessageBox.confirm(`确定要通过 ${row.studentName} 的申请吗？`, '审批确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.put(`/internship/applications/${row.id}/approve`)
    ElMessage.success(`${row.studentName} 的申请已通过`)
    row.status = STATUS_APPROVED
    const index = batchReviewList.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      batchReviewList.value[index].status = STATUS_APPROVED
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.warn('审批失败', e.message)
    }
  }
}

async function handleReviewReject(row) {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入驳回原因（至少10个字符）', `驳回 ${row.studentName} 的申请`, {
      confirmButtonText: '确定驳回',
      cancelButtonText: '取消',
      inputPattern: /.{10,}/,
      inputErrorMessage: '驳回原因至少需要10个字符'
    })
    await request.put(`/internship/applications/${row.id}/reject`, { reason })
    ElMessage.success(`${row.studentName} 的申请已驳回`)
    const index = batchReviewList.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      batchReviewList.value[index].status = STATUS_REJECTED
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.warn('驳回失败', e.message)
    }
  }
}

async function handleReviewResign(row) {
  try {
    await ElMessageBox.confirm(
      `确定要将 ${row.studentName} 标记为提前离职吗？`,
      '提前离职确认',
      {
        confirmButtonText: '确认提前离职',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await request.put(`/internship/applications/${row.id}/resign`)
    ElMessage.success(`${row.studentName} 已标记为提前离职`)
    const index = batchReviewList.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      batchReviewList.value[index].status = STATUS_RESIGNED
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.warn('提前离职操作失败', e.message)
    }
  }
}

async function handleBatchApproveAll() {
  const pendingOrRejected = batchReviewList.value.filter(r => r.status === STATUS_PENDING || r.status === STATUS_REJECTED)
  if (!pendingOrRejected.length) {
    ElMessage.warning('没有可操作的申请')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确定要全部通过 ${pendingOrRejected.length} 条待审核/已驳回的申请吗？`,
      '全部通过确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    batchApproveLoading.value = true
    for (const row of pendingOrRejected) {
      await request.put(`/internship/applications/${row.id}/approve`)
      const index = batchReviewList.value.findIndex(item => item.id === row.id)
      if (index > -1) {
        batchReviewList.value[index].status = STATUS_APPROVED
      }
    }
    ElMessage.success('全部通过成功')
  } catch (e) {
    if (e !== 'cancel') {
      console.warn('全部通过失败', e.message)
    }
  } finally {
    batchApproveLoading.value = false
  }
}

async function handleBatchRejectAll() {
  const pendingOrRejected = batchReviewList.value.filter(r => r.status === STATUS_PENDING || r.status === STATUS_REJECTED)
  if (!pendingOrRejected.length) {
    ElMessage.warning('没有可操作的申请')
    return
  }
  try {
    const { value: reason } = await ElMessageBox.prompt(
      `请输入全部驳回的原因（将应用于 ${pendingOrRejected.length} 条申请）`,
      '全部驳回确认',
      {
        confirmButtonText: '确定驳回',
        cancelButtonText: '取消',
        inputPattern: /.{10,}/,
        inputErrorMessage: '驳回原因至少需要10个字符'
      }
    )
    batchRejectLoading.value = true
    for (const row of pendingOrRejected) {
      await request.put(`/internship/applications/${row.id}/reject`, { reason })
      const index = batchReviewList.value.findIndex(item => item.id === row.id)
      if (index > -1) {
        batchReviewList.value[index].status = STATUS_REJECTED
      }
    }
    ElMessage.success('全部驳回成功')
  } catch (e) {
    if (e !== 'cancel') {
      console.warn('全部驳回失败', e.message)
    }
  } finally {
    batchRejectLoading.value = false
  }
}

async function handleViewDetail(row) {
  try {
    const res = await request.get(`/internship/applications/${row.id}`)
    const data = res.data
    detailData.id = data.id
    detailData.studentNo = data.studentNo || ''
    detailData.studentName = data.studentName || ''
    detailData.planName = data.planName || ''
    detailData.status = data.status ?? STATUS_UNSUBMITTED
    detailData.companyName = data.companyName || ''
    detailData.position = data.position || ''
    detailData.companyAddress = data.companyAddress || ''
    detailData.startDate = data.startDate || ''
    detailData.endDate = data.endDate || ''
    detailData.salary = data.salary
    detailData.externalTeacher = data.externalTeacher || ''
    detailData.externalTeacherPhone = data.externalTeacherPhone || ''
    detailData.safetyAgreementUrl = data.safetyAgreementUrl || data.safetyAgreementPath || ''
    detailData.applicationFormUrl = data.applicationFormUrl || data.applicationPath || ''
    detailData.acceptanceLetterUrl = data.acceptanceLetterUrl || data.receptionLetterPath || ''
  } catch (e) {
    console.warn('获取申请详情失败', e.message)
  }
}

async function handleApproveDetail() {
  try {
    await ElMessageBox.confirm('确定要审批通过该申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.put(`/internship/applications/${detailData.id}/approve`)
    ElMessage.success('审批通过')
    detailData.status = STATUS_APPROVED
    if (isBatchMode.value) {
      const index = batchReviewList.value.findIndex(item => item.id === detailData.id)
      if (index > -1) {
        batchReviewList.value[index].status = STATUS_APPROVED
      }
    } else {
      detailDialogVisible.value = false
      fetchData()
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.warn('审批通过失败', e.message)
    }
  }
}

function handleRejectDetail() {
  rejectId.value = detailData.id
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

async function handleDetailResign() {
  try {
    await ElMessageBox.confirm(
      `确定要将 ${detailData.studentName} 标记为提前离职吗？`,
      '提前离职确认',
      {
        confirmButtonText: '确认提前离职',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await request.put(`/internship/applications/${detailData.id}/resign`)
    ElMessage.success(`${detailData.studentName} 已标记为提前离职`)
    detailData.status = STATUS_RESIGNED
    if (isBatchMode.value) {
      const index = batchReviewList.value.findIndex(item => item.id === detailData.id)
      if (index > -1) {
        batchReviewList.value[index].status = STATUS_RESIGNED
      }
    } else {
      detailDialogVisible.value = false
      fetchData()
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.warn('提前离职操作失败', e.message)
    }
  }
}

async function handleRejectSubmit() {
  const valid = await rejectFormRef.value.validate().catch(() => false)
  if (!valid) return

  rejectLoading.value = true
  try {
    await request.put(`/internship/applications/${rejectId.value}/reject`, {
      reason: rejectForm.reason
    })
    ElMessage.success('已驳回')
    rejectDialogVisible.value = false
    detailData.status = STATUS_REJECTED
    if (isBatchMode.value) {
      const index = batchReviewList.value.findIndex(item => item.id === rejectId.value)
      if (index > -1) {
        batchReviewList.value[index].status = STATUS_REJECTED
      }
    } else {
      detailDialogVisible.value = false
      fetchData()
    }
  } catch (e) {
    console.warn('驳回申请失败', e.message)
  } finally {
    rejectLoading.value = false
  }
}

onMounted(() => {
  loadPlanOptions()
  if (userStore.isStudent()) {
    loadStudentApplication()
  } else {
    fetchData()
  }
})

onBeforeUnmount(() => {
  detailDialogVisible.value = false
  rejectDialogVisible.value = false
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

.form-footer {
  display: flex;
  justify-content: center;
  padding-top: 20px;
}
</style>
