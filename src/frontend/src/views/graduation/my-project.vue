<template>
  <div class="page-container">
    <el-card>
      <template #header><div class="card-header"><span class="card-title">我的毕业设计</span><el-button type="primary" size="small" @click="handleSubmit">提交设计</el-button></div></template>

      <el-table ref="tableRef" :data="tableData" border stripe v-loading="loading" style="width:100%">
        <el-table-column prop="title" label="题目" min-width="200" show-overflow-tooltip />
        <el-table-column prop="teacherName" label="指导教师" min-width="100" />
        <el-table-column prop="status" label="状态" min-width="90" align="center"><template #default="{row}"><el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag></template></el-table-column>
        <el-table-column prop="submitTime" label="提交时间" min-width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="reviewTime" label="批阅时间" min-width="170" />
        <el-table-column label="操作" width="180" fixed="right" :resizable="false">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button v-if="canEdit(row)" link type="warning" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status==='REJECTED'" link type="primary" size="small" @click="handleResubmit(row)">重新提交</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="viewVisible" title="毕业设计详情" width="650px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="题目">{{ viewData.title||'-' }}</el-descriptions-item>
        <el-descriptions-item label="指导教师">{{ viewData.teacherName||'-' }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="getStatusType(viewData.status)">{{ getStatusText(viewData.status) }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ formatDateTime(viewData.submitTime)||'-' }}</el-descriptions-item>
        <el-descriptions-item label="内容摘要" :span="2">{{ viewData.content||'-' }}</el-descriptions-item>
        <el-descriptions-item label="评语" :span="2">{{ viewData.reviewComment||'-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="editVisible" title="提交/编辑毕业设计" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px">
        <el-form-item label="题目" prop="title"><el-input v-model="formData.title" placeholder="请输入毕业设计题目"/></el-form-item>
        <el-form-item label="内容" prop="content"><el-input v-model="formData.content" type="textarea" :rows="8" placeholder="请输入毕业设计内容"/></el-form-item>
        <el-form-item label="附件"><el-upload action="#" :auto-upload="false" :limit="3" accept=".pdf,.doc,.docx,.zip"><el-button type="primary">选择文件</el-button></el-upload></el-form-item>
      </el-form>
      <template #footer><el-button @click="editVisible=false">取消</el-button><el-button type="primary" :loading="sLoading" @click="handleSave">提交</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { formatDateTime } from '@/utils/dateFormat'
import request from '@/utils/request.js'
import { useTableResize } from '@/composables/useTableResize.js'

const tableRef = ref(null)
useTableResize(tableRef)
const loading = ref(false), tableData = ref([]), viewVisible = ref(false), editVisible = ref(false), sLoading = ref(false), formRef = ref(null)
const formData = ref({ title: '', content: '' })
const rules = { title: [{ required:true, message:'请输入题目', trigger:'blur' }], content: [{ required:true, message:'请输入内容', trigger:'blur' }] }
const viewData = ref({})

async function fetchData() {
  loading.value = true
  try { const res = await request.get('/graduation/projects/my'); tableData.value = Array.isArray(res.data) ? res.data : (res.data.records || []) } catch(e){ console.warn(e) } finally{loading.value=false}
}

function handleView(row) { viewData.value={...row}; viewVisible.value=true }
function handleEdit(row) { formData.value={ id:row.id, title:row.title, content:row.content }; editVisible.value=true }
function handleSubmit() { formData.value={ title:'', content:'' }; editVisible.value=true }

function canEdit(r) { return r.status==='DRAFT'||r.status==='REJECTED' }
function handleResubmit(r) { handleEdit(r) }

async function handleSave() {
  if(!formRef.value) return
  await formRef.value.validate()
  sLoading.value = true
  try {
    if(formData.value.id) await request.put(`/graduation/projects/${formData.value.id}`, formData.value)
    else await request.post('/graduation/projects', formData.value)
    ElMessage.success('提交成功'); editVisible.value=false; fetchData()
  } catch(e) { ElMessage.error(e.response?.data?.message||'失败') } finally { sLoading.value=false }
}
function getStatusType(s) { return { DRAFT:'info', SUBMITTED:'warning', APPROVED:'success', REJECTED:'danger' }[s]||'info' }
function getStatusText(s) { return { DRAFT:'草稿', SUBMITTED:'待审', APPROVED:'通过', REJECTED:'驳回' }[s]||s }
onMounted(fetchData)
</script>

<style scoped>
.page-container{padding:20px}.card-header{display:flex;justify-content:space-between;align-items:center}.card-title{font-size:16px;font-weight:600}
</style>
