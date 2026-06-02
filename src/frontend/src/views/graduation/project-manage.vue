<template>
  <div class="page-container">
    <el-card>
      <template #header><span class="card-title">毕业设计管理</span></template>

      <el-form :model="queryForm" inline class="search-form">
        <el-form-item label="学生姓名"><el-input v-model="queryForm.studentName" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width:120px">
            <el-option label="草稿" value="DRAFT"/><el-option label="待审" value="SUBMITTED"/><el-option label="通过" value="APPROVED"/><el-option label="驳回" value="REJECTED"/>
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="fetchData">查询</el-button><el-button @click="resetQuery">重置</el-button></el-form-item>
      </el-form>

      <el-table ref="tableRef" :data="tableData" border stripe v-loading="loading" style="width:100%">
        <el-table-column prop="studentNo" label="学号" min-width="120" />
        <el-table-column prop="studentName" label="学生姓名" min-width="100" />
        <el-table-column prop="title" label="题目" min-width="200" show-overflow-tooltip />
        <el-table-column prop="teacherName" label="指导教师" min-width="100" />
        <el-table-column prop="status" label="状态" min-width="90" align="center"><template #default="{row}"><el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag></template></el-table-column>
        <el-table-column prop="submitTime" label="提交时间" min-width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right" :resizable="false">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status==='SUBMITTED' && (userStore.isTeacher() || userStore.isDeptAdmin() || userStore.isAdmin())" link type="success" size="small" @click="handleReview(row)">批阅</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size" :total="pagination.total"
          :page-sizes="[10,20,50]" layout="total, sizes, prev, pager, next" @size-change="fetchData" @current-change="fetchData"/>
      </div>
    </el-card>

    <el-dialog v-model="viewVisible" title="毕业设计详情" width="650px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学号">{{ viewData.studentNo||'-' }}</el-descriptions-item>
        <el-descriptions-item label="学生姓名">{{ viewData.studentName||'-' }}</el-descriptions-item>
        <el-descriptions-item label="题目">{{ viewData.title||'-' }}</el-descriptions-item>
        <el-descriptions-item label="指导教师">{{ viewData.teacherName||'-' }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="getStatusType(viewData.status)">{{ getStatusText(viewData.status) }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ formatDateTime(viewData.submitTime)||'-' }}</el-descriptions-item>
        <el-descriptions-item label="内容摘要" :span="2" class="proj-content">{{ viewData.content||'-' }}</el-descriptions-item>
        <el-descriptions-item label="评语" :span="2">{{ viewData.reviewComment||'-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="reviewVisible" title="批阅毕业设计" width="550px" destroy-on-close>
      <el-descriptions :column="1" border class="mb-4">
        <el-descriptions-item label="学生">{{ reviewData.studentName||'-' }} ({{ reviewData.studentNo||'' }})</el-descriptions-item>
        <el-descriptions-item label="题目">{{ reviewData.title||'-' }}</el-descriptions-item>
      </el-descriptions>
      <el-form ref="rf" :model="reviewForm" :rules="rrules" label-width="80px">
        <el-form-item label="审核结果" prop="status">
          <el-radio-group v-model="reviewForm.status"><el-radio value="APPROVED">通过</el-radio><el-radio value="REJECTED">驳回</el-radio></el-radio-group>
        </el-form-item>
        <el-form-item label="评语" prop="reviewComment"><el-input v-model="reviewForm.reviewComment" type="textarea" :rows="3" placeholder="请输入评语"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="reviewVisible=false">取消</el-button><el-button type="primary" :loading="rLoading" @click="handleSaveReview">确认批阅</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { formatDateTime } from '@/utils/dateFormat'
import request from '@/utils/request.js'
import { useUserStore } from '@/stores/user.js'
import { useTableResize } from '@/composables/useTableResize.js'

const userStore = useUserStore()
const tableRef = ref(null)
useTableResize(tableRef)

const loading = ref(false), tableData = ref([]), viewVisible = ref(false), reviewVisible = ref(false), rLoading = ref(false), rf = ref(null)
const queryForm = reactive({ studentName: '', status: null })
const pagination = reactive({ current: 1, size: 10, total: 0 })
const viewData = ref({}), reviewData = ref({})
const reviewForm = ref({ status: 'APPROVED', reviewComment: '' })
const rrules = { status: [{ required:true, message:'请选择', trigger:'change' }] }

async function fetchData() {
  loading.value = true
  try { const res = await request.get('/graduation/projects', { params: { current: pagination.current, size: pagination.size, ...queryForm } }); tableData.value = res.data.records || []; pagination.total = res.data.total || 0 } catch(e){ console.warn(e) } finally{loading.value=false}
}
function resetQuery() { queryForm.studentName=''; queryForm.status=null; fetchData() }

function handleView(row) { viewData.value={...row}; viewVisible.value=true }
function handleReview(row) { reviewData.value={...row}; reviewForm.value={ status:'APPROVED', reviewComment:'' }; reviewVisible.value=true }

async function handleSaveReview() {
  if(!rf.value) return
  await rf.value.validate()
  rLoading.value = true
  try { await request.put(`/graduation/projects/${reviewData.value.id}/review`, reviewForm.value); ElMessage.success('批阅成功'); reviewVisible.value=false; fetchData() } catch(e){ ElMessage.error(e.response?.data?.message||'失败') } finally{rLoading.value=false}
}

function getStatusType(s) { return { DRAFT:'info', SUBMITTED:'warning', APPROVED:'success', REJECTED:'danger' }[s]||'info' }
function getStatusText(s) { return { DRAFT:'草稿', SUBMITTED:'待审', APPROVED:'通过', REJECTED:'驳回' }[s]||s }
onMounted(fetchData)
</script>

<style scoped>
.page-container{padding:20px}.card-title{font-size:16px;font-weight:600}.search-form{margin-bottom:16px}.pagination-wrapper{margin-top:16px;display:flex;justify-content:flex-end}.mb-4{margin-bottom:16px}.proj-content{white-space:pre-wrap;line-height:1.6}
</style>
