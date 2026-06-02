<template>
  <div class="page-container">
    <div class="header-bar">
      <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
      <span class="page-title">师生分配 - {{ planInfo.planName }}</span>
    </div>

    <el-card class="info-card">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="计划名称">{{ planInfo.planName }}</el-descriptions-item>
        <el-descriptions-item label="学期">{{ planInfo.semesterName }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ planInfo.majorName }}</el-descriptions-item>
        <el-descriptions-item label="年级">{{ planInfo.gradeName }}</el-descriptions-item>
        <el-descriptions-item label="开始日期">{{ formatDate(planInfo.startDate) }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ formatDate(planInfo.endDate) }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-form :inline="true" class="filter-form">
      <el-form-item>
        <el-select v-model="queryForm.className" placeholder="请选择班级" clearable style="width: 180px">
          <el-option v-for="cls in classOptions" :key="cls.id" :label="cls.className" :value="cls.className" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-input v-model="queryForm.teacherName" placeholder="请输入老师姓名" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item>
        <el-select v-model="queryForm.assigned" placeholder="请选择" clearable style="width: 180px">
          <el-option label="全部" value="" />
          <el-option label="已分配" value="1" />
          <el-option label="未分配" value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="success" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="openAddStudentDialog">纳入学生</el-button>
      <el-button type="success" :icon="User" :disabled="!selectedStudents.length" @click="openAssignTeacherDialog">分配指导老师</el-button>
      <el-button type="danger" :icon="Delete" :disabled="!selectedStudents.length" @click="handleBatchRemove">移除学生</el-button>
    </div>

    <el-table
      ref="tableRef1"
      v-loading="studentsLoading"
      :data="studentsData"
      border
      stripe
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="50" />
      <el-table-column prop="studentName" label="学生姓名" />
      <el-table-column prop="studentNo" label="学号" />
      <el-table-column prop="className" label="班级" align="center" />
      <el-table-column label="校内指导老师">
        <template #default="{ row }">
          <el-tag v-if="!row.teacherName" type="info">未分配</el-tag>
          <span v-else>{{ row.teacherName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center" :resizable="false">
        <template #default="{ row }">
          <el-button v-if="!row.teacherName" type="primary" link @click="handleAssignOne(row)">分配老师</el-button>
          <el-button v-if="row.teacherName" type="primary" link @click="handleChangeTeacher(row)">更换老师</el-button>
          <el-button type="danger" link @click="handleRemove(row)">移除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="queryForm.current"
        v-model:page-size="queryForm.size"
        :total="studentsTotal"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchStudents"
        @current-change="fetchStudents"
      />
    </div>

    <el-dialog v-model="addStudentDialogVisible" title="纳入学生" width="800px" destroy-on-close>
      <el-form :inline="true" class="filter-form">
        <el-form-item>
          <el-select v-model="addStudentQuery.classId" placeholder="请选择班级" clearable style="width: 180px">
            <el-option v-for="cls in addStudentClassOptions" :key="cls.id" :label="cls.className" :value="cls.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input v-model="addStudentQuery.keyword" placeholder="姓名/学号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="success" :icon="Search" @click="searchAddStudents">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table
        ref="tableRef2"
        :data="addStudentList"
        border
        stripe
        style="width: 100%"
        @selection-change="handleAddStudentSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="name" label="学生姓名" />
        <el-table-column prop="studentNo" label="学号" />
        <el-table-column prop="className" label="班级" />
        <el-table-column prop="majorName" label="专业" :resizable="false" />
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="addStudentQuery.current"
          v-model:page-size="addStudentQuery.size"
          :total="addStudentTotal"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadAddStudents"
          @current-change="loadAddStudents"
        />
      </div>

      <template #footer>
        <el-button @click="addStudentDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!addStudentSelected.length" @click="confirmAddStudents">确定纳入</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assignTeacherDialogVisible" title="分配指导老师" width="500px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="指导老师">
          <el-select
            v-model="assignTeacherId"
            placeholder="请选择指导老师"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="teacher in teacherOptions"
              :key="teacher.id"
              :label="teacher.name"
              :value="teacher.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="assignMode === 'change'" label="更换原因">
          <el-input v-model="assignReason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignTeacherDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!assignTeacherId" @click="confirmAssignTeacher">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Search, Refresh, Plus, User, Delete } from '@element-plus/icons-vue'
import request from '@/utils/request.js'
import { formatDate } from '@/utils/dateFormat'
import { useTableResize } from '@/composables/useTableResize.js'

const route = useRoute()
const router = useRouter()

const tableRef1 = ref(null)
const tableRef2 = ref(null)
useTableResize(tableRef1)
useTableResize(tableRef2)

const planId = route.params.id

const planInfo = reactive({
  planName: '',
  semesterName: '',
  majorName: '',
  gradeName: '',
  startDate: '',
  endDate: ''
})

const studentsData = ref([])
const studentsTotal = ref(0)
const studentsLoading = ref(false)
const selectedStudents = ref([])

const classOptions = ref([])
const teacherOptions = ref([])

const queryForm = reactive({
  current: 1,
  size: 10,
  className: '',
  teacherName: '',
  assigned: ''
})

const addStudentDialogVisible = ref(false)
const addStudentList = ref([])
const addStudentTotal = ref(0)
const addStudentSelected = ref([])
const addStudentClassOptions = ref([])

const addStudentQuery = reactive({
  current: 1,
  size: 10,
  classId: null,
  keyword: ''
})

const assignTeacherDialogVisible = ref(false)
const assignTeacherId = ref(null)
const assignMode = ref('batch')
const assignTargetStudentId = ref(null)
const assignReason = ref('')

function goBack() {
  router.push('/internship/plan')
}

async function loadPlanInfo() {
  try {
    const res = await request.get(`/internship/plans/${planId}`)
    const data = res.data
    planInfo.planName = data.planName
    planInfo.startDate = data.startDate
    planInfo.endDate = data.endDate
    if (data.semesterId) {
      try {
        const semRes = await request.get(`/admin/semesters/${data.semesterId}`)
        planInfo.semesterName = semRes.data.semesterName
      } catch (e) {
        console.warn('获取学期名称失败', e.message)
      }
    }
    if (data.majorId) {
      try {
        const majorRes = await request.get(`/admin/majors/${data.majorId}`)
        planInfo.majorName = majorRes.data.majorName
      } catch (e) {
        console.warn('获取专业名称失败', e.message)
      }
    }
    if (data.gradeId) {
      try {
        const gradeRes = await request.get(`/admin/grades/${data.gradeId}`)
        planInfo.gradeName = gradeRes.data.gradeName
      } catch (e) {
        console.warn('获取年级名称失败', e.message)
      }
    }
  } catch (e) {
    console.warn('获取计划详情失败', e.message)
  }
}

async function loadClassOptions() {
  try {
    const res = await request.get('/admin/classes', { params: { current: 1, size: 1000 } })
    classOptions.value = res.data.records || []
  } catch (e) {
    console.warn('获取班级列表失败', e.message)
  }
}

async function loadTeacherOptions() {
  try {
    const res = await request.get('/admin/teachers', { params: { current: 1, size: 1000 } })
    teacherOptions.value = res.data.records || []
  } catch (e) {
    console.warn('获取教师列表失败', e.message)
  }
}

async function fetchStudents() {
  studentsLoading.value = true
  try {
    const params = {
      current: queryForm.current,
      size: queryForm.size
    }
    if (queryForm.className) params.className = queryForm.className
    if (queryForm.teacherName) params.teacherName = queryForm.teacherName
    if (queryForm.assigned) params.assigned = queryForm.assigned
    const res = await request.get(`/internship/plans/${planId}/students`, { params })
    studentsData.value = res.data.records
    studentsTotal.value = res.data.total
  } catch (e) {
    console.warn('获取学生列表失败', e.message)
  } finally {
    studentsLoading.value = false
  }
}

function handleSearch() {
  queryForm.current = 1
  fetchStudents()
}

function handleReset() {
  queryForm.className = ''
  queryForm.teacherName = ''
  queryForm.assigned = ''
  queryForm.current = 1
  fetchStudents()
}

function handleSelectionChange(selection) {
  selectedStudents.value = selection
}

async function openAddStudentDialog() {
  addStudentDialogVisible.value = true
  addStudentQuery.current = 1
  addStudentQuery.classId = null
  addStudentQuery.keyword = ''
  addStudentList.value = []
  addStudentTotal.value = 0
  addStudentSelected.value = []
  try {
    const res = await request.get('/admin/classes', { params: { current: 1, size: 1000 } })
    addStudentClassOptions.value = res.data.records || []
  } catch (e) {
    console.warn('获取班级列表失败', e.message)
  }
  loadAddStudents()
}

async function loadAddStudents() {
  try {
    const params = {
      current: addStudentQuery.current,
      size: addStudentQuery.size
    }
    if (addStudentQuery.classId) params.classId = addStudentQuery.classId
    if (addStudentQuery.keyword) params.keyword = addStudentQuery.keyword
    const res = await request.get('/admin/students', { params })
    const allStudents = res.data.records || []
    const existingIds = new Set(studentsData.value.map(s => s.studentId))
    addStudentList.value = allStudents.filter(s => !existingIds.has(s.id))
    addStudentTotal.value = res.data.total
  } catch (e) {
    console.warn('获取可纳入学生列表失败', e.message)
  }
}

function searchAddStudents() {
  addStudentQuery.current = 1
  loadAddStudents()
}

function handleAddStudentSelectionChange(selection) {
  addStudentSelected.value = selection
}

async function confirmAddStudents() {
  if (!addStudentSelected.value.length) {
    ElMessage.warning('请选择要纳入的学生')
    return
  }
  try {
    const studentIds = addStudentSelected.value.map(s => s.id)
    await request.post(`/internship/plans/${planId}/students`, { studentIds })
    ElMessage.success('纳入学生成功')
    addStudentDialogVisible.value = false
    fetchStudents()
  } catch (e) {
    console.warn('纳入学生失败', e.message)
  }
}

function openAssignTeacherDialog() {
  if (!selectedStudents.value.length) {
    ElMessage.warning('请先选择学生')
    return
  }
  const assignedStudents = selectedStudents.value.filter(s => s.teacherName)
  if (assignedStudents.length > 0) {
    const names = assignedStudents.map(s => s.studentName).join('、')
    ElMessageBox.confirm(
      `学生"${names}"已有指导老师，是否覆盖分配？`,
      '确认覆盖',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    ).then(() => {
      doOpenAssignTeacher('batch', null)
    }).catch(() => {})
  } else {
    doOpenAssignTeacher('batch', null)
  }
}

function handleAssignOne(row) {
  doOpenAssignTeacher('one', row.studentId)
}

function handleChangeTeacher(row) {
  doOpenAssignTeacher('change', row.studentId)
}

function doOpenAssignTeacher(mode, studentId) {
  assignMode.value = mode
  assignTargetStudentId.value = studentId
  assignTeacherId.value = null
  assignReason.value = ''
  assignTeacherDialogVisible.value = true
  loadTeacherOptions()
}

async function confirmAssignTeacher() {
  if (!assignTeacherId.value) {
    ElMessage.warning('请选择指导老师')
    return
  }
  try {
    if (assignMode.value === 'change') {
      await request.put(`/internship/plans/${planId}/students/change-teacher`, {
        studentId: assignTargetStudentId.value,
        newTeacherId: assignTeacherId.value,
        reason: assignReason.value || undefined
      })
      ElMessage.success('更换指导老师成功')
    } else {
      const studentIds = selectedStudents.value.map(s => s.studentId)
      await request.put(`/internship/plans/${planId}/students/assign-teacher`, {
        studentIds,
        teacherId: assignTeacherId.value
      })
      ElMessage.success('分配指导老师成功')
    }
    assignTeacherDialogVisible.value = false
    selectedStudents.value = []
    fetchStudents()
  } catch (e) {
    console.warn('分配指导老师失败', e.message)
  }
}

function handleRemove(row) {
  ElMessageBox.confirm(`确定要移除学生"${row.studentName}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/internship/plans/${planId}/students/${row.studentId}`)
      ElMessage.success('移除成功')
      fetchStudents()
    } catch (e) {
      console.warn('移除学生失败', e.message)
    }
  }).catch(() => {})
}

function handleBatchRemove() {
  if (!selectedStudents.value.length) {
    ElMessage.warning('请先选择要移除的学生')
    return
  }
  const names = selectedStudents.value.map(s => s.studentName).join('、')
  ElMessageBox.confirm(`确定要移除学生"${names}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const studentIds = selectedStudents.value.map(s => s.studentId)
      for (const studentId of studentIds) {
        await request.delete(`/internship/plans/${planId}/students/${studentId}`)
      }
      ElMessage.success('批量移除成功')
      selectedStudents.value = []
      fetchStudents()
    } catch (e) {
      console.warn('批量移除失败', e.message)
    }
  }).catch(() => {})
}

onMounted(async () => {
  await loadPlanInfo()
  await loadClassOptions()
  await fetchStudents()
})

onBeforeUnmount(() => {
  addStudentDialogVisible.value = false
  assignTeacherDialogVisible.value = false
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.header-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
}

.info-card {
  margin-bottom: 20px;
}

.filter-form {
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
</style>
