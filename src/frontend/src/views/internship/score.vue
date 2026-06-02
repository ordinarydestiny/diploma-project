<template>
  <div class="page-container">
    <el-card>
      <template #header><span class="card-title">实习成绩管理</span></template>

      <el-form :model="queryForm" inline class="search-form">
        <el-form-item label="学生姓名"><el-input v-model="queryForm.studentName" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="专业"><el-input v-model="queryForm.majorName" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="班级"><el-input v-model="queryForm.className" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="实习计划">
          <el-select v-model="queryForm.planId" placeholder="全部" clearable style="width:200px"><el-option v-for="p in planOptions" :key="p.id" :label="p.planName" :value="p.id"/></el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="fetchData">查询</el-button><el-button @click="resetQuery">重置</el-button></el-form-item>
      </el-form>

      <el-table ref="tableRef" :data="tableData" border stripe v-loading="loading" style="width:100%">
        <el-table-column prop="studentNo" label="学号" min-width="120" />
        <el-table-column prop="studentName" label="姓名" min-width="100" />
        <el-table-column prop="majorName" label="专业" min-width="120" show-overflow-tooltip />
        <el-table-column prop="className" label="班级" min-width="120" show-overflow-tooltip />
        <el-table-column prop="planName" label="实习计划" min-width="150" show-overflow-tooltip />
        <el-table-column label="签到" min-width="90" align="center">
          <template #default="{ row }"><el-tag v-if="row.checkinGrade" :type="getGradeType(row.checkinGrade)">{{ getGradeText(row.checkinGrade) }}</el-tag><span v-else>-</span></template>
        </el-table-column>
        <el-table-column label="日报" min-width="90" align="center">
          <template #default="{ row }"><el-tag v-if="row.dailyReportGrade" :type="getGradeType(row.dailyReportGrade)">{{ getGradeText(row.dailyReportGrade) }}</el-tag><span v-else>-</span></template>
        </el-table-column>
        <el-table-column label="周报" min-width="90" align="center">
          <template #default="{ row }"><el-tag v-if="row.weeklyReportGrade" :type="getGradeType(row.weeklyReportGrade)">{{ getGradeText(row.weeklyReportGrade) }}</el-tag><span v-else>-</span></template>
        </el-table-column>
        <el-table-column label="月报" min-width="90" align="center">
          <template #default="{ row }"><el-tag v-if="row.monthlyReportGrade" :type="getGradeType(row.monthlyReportGrade)">{{ getGradeText(row.monthlyReportGrade) }}</el-tag><span v-else>-</span></template>
        </el-table-column>
        <el-table-column label="校内考核" min-width="90" align="center">
          <template #default="{ row }"><el-tag v-if="row.innerAssessmentGrade" :type="getGradeType(row.innerAssessmentGrade)">{{ getGradeText(row.innerAssessmentGrade) }}</el-tag><span v-else>-</span></template>
        </el-table-column>
        <el-table-column label="校外考核" min-width="90" align="center">
          <template #default="{ row }"><el-tag v-if="row.outerAssessmentGrade" :type="getGradeType(row.outerAssessmentGrade)">{{ getGradeText(row.outerAssessmentGrade) }}</el-tag><span v-else>-</span></template>
        </el-table-column>
        <el-table-column label="实习报告" min-width="90" align="center">
          <template #default="{ row }"><el-tag v-if="row.reportGrade" :type="getGradeType(row.reportGrade)">{{ getGradeText(row.reportGrade) }}</el-tag><span v-else>-</span></template>
        </el-table-column>
        <el-table-column prop="totalScore" label="总分" min-width="80" align="center">
          <template #default="{ row }"><strong>{{ row.totalScore != null ? row.totalScore.toFixed(1) : '-' }}</strong></template>
        </el-table-column>
        <el-table-column prop="gradeLevel" label="等级" min-width="90" align="center">
          <template #default="{ row }"><el-tag v-if="row.gradeLevel" :type="getGradeType(row.gradeLevel)">{{ getGradeText(row.gradeLevel) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right" :resizable="false">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size" :total="pagination.total"
          :page-sizes="[10,20,50]" layout="total, sizes, prev, pager, next" @size-change="fetchData" @current-change="fetchData"/>
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="实习成绩详情" width="750px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学号">{{ detailData.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ detailData.studentName }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ detailData.majorName||'-' }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ detailData.className||'-' }}</el-descriptions-item>
        <el-descriptions-item label="实习计划" :span="2">{{ detailData.planName||'-' }}</el-descriptions-item>
      </el-descriptions>

      <el-table :data="detailItems" border stripe style="width:100%;margin-top:16px" size="small">
        <el-table-column label="评分项" prop="name" width="100" />
        <el-table-column label="等级" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.grade" :type="getGradeType(row.grade)">{{ getGradeText(row.grade) }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="计算详情" prop="detail" show-overflow-tooltip />
      </el-table>

      <el-descriptions :column="2" border style="margin-top:16px">
        <el-descriptions-item label="总成绩"><strong>{{ detailData.totalScore!=null?detailData.totalScore.toFixed(1):'-' }}</strong></el-descriptions-item>
        <el-descriptions-item label="等级评定">
          <el-tag v-if="detailData.gradeLevel" :type="getGradeType(detailData.gradeLevel)">{{ getGradeText(detailData.gradeLevel) }}</el-tag>
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request.js'
import { useTableResize } from '@/composables/useTableResize.js'

const tableRef = ref(null)
useTableResize(tableRef)

const loading = ref(false), tableData = ref([]), detailVisible = ref(false)
const queryForm = reactive({ studentName: '', majorName: '', className: '', planId: null })
const pagination = reactive({ current: 1, size: 10, total: 0 })
const detailData = ref({})
const planOptions = ref([])

function getGradeType(g) { return { EXCELLENT:'success', GOOD:'', MEDIUM:'info', PASS:'warning', FAIL:'danger' }[g]||'' }
function getGradeText(g) { return { EXCELLENT:'优秀', GOOD:'良好', MEDIUM:'中等', PASS:'及格', FAIL:'不及格' }[g]||g }

const detailItems = computed(() => [
  { name: '签到', grade: detailData.value.checkinGrade, detail: detailData.value.checkinDetail||'-' },
  { name: '日报', grade: detailData.value.dailyReportGrade, detail: detailData.value.dailyReportDetail||'-' },
  { name: '周报', grade: detailData.value.weeklyReportGrade, detail: detailData.value.weeklyReportDetail||'-' },
  { name: '月报', grade: detailData.value.monthlyReportGrade, detail: detailData.value.monthlyReportDetail||'-' },
  { name: '校内考核', grade: detailData.value.innerAssessmentGrade, detail: detailData.value.innerAssessmentDetail||'-' },
  { name: '校外考核', grade: detailData.value.outerAssessmentGrade, detail: detailData.value.outerAssessmentDetail||'-' },
  { name: '实习报告', grade: detailData.value.reportGrade, detail: detailData.value.reportDetail||'-' }
])

async function fetchData() {
  loading.value = true
  try {
    const res = await request.get('/internship/scores', { params: { current: pagination.current, size: pagination.size, ...queryForm } })
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (e) { console.error(e.message) } finally { loading.value = false }
}
function resetQuery() { Object.assign(queryForm, { studentName:'', majorName:'', className:'', planId:null }); fetchData() }

async function fetchPlans() {
  try { const res = await request.get('/internship/plans', { params: { size:999 } }); planOptions.value = Array.isArray(res.data)?res.data:(res.data.records||[]) } catch(e){ console.warn(e) }
}

async function handleDetail(row) {
  try {
    const res = await request.get(`/internship/scores/student/${row.studentId}/plan/${row.planId}`)
    detailData.value = res.data || {}
    detailVisible.value = true
  } catch (e) { ElMessage.error(e.response?.data?.message||'获取详情失败') }
}

onMounted(() => { fetchData(); fetchPlans() })
</script>

<style scoped>
.page-container{padding:20px}.card-title{font-size:16px;font-weight:600}.search-form{margin-bottom:16px}.pagination-wrapper{margin-top:16px;display:flex;justify-content:flex-end}
</style>
