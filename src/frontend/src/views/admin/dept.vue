<template>
  <div class="page-container">
    <el-card>
      <div class="filter-bar">
        <el-input
          v-model="queryForm.deptName"
          placeholder="院系名称"
          clearable
          style="width: 200px"
        />
        <el-select
          v-model="queryForm.deptType"
          placeholder="院系层级"
          clearable
          style="width: 160px"
        >
          <el-option
            label="一级学院"
            :value="1"
          />
          <el-option
            label="二级学院"
            :value="2"
          />
        </el-select>
        <el-select
          v-if="queryForm.deptType === 2"
          v-model="queryForm.parentId"
          placeholder="上级院系"
          clearable
          style="width: 200px"
        >
          <el-option
            v-for="dept in primaryDeptOptions"
            :key="dept.id"
            :label="dept.deptName"
            :value="dept.id"
          />
        </el-select>
        <el-button
          type="primary"
          @click="handleSearch"
        >
          搜索
        </el-button>
        <el-button @click="handleReset">
          重置
        </el-button>
        <el-button
          type="primary"
          @click="handleAdd"
        >
          新增
        </el-button>
      </div>
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column
          prop="deptCode"
          label="院系编号"
          min-width="150"
        />
        <el-table-column
          prop="deptName"
          label="院系名称"
        />
        <el-table-column
          prop="deptType"
          label="院系层级"
          min-width="120"
        >
          <template #default="{ row }">
            <el-tag
              v-if="row.deptType === 1"
              type="primary"
            >
              一级学院
            </el-tag>
            <el-tag
              v-else-if="row.deptType === 2"
              type="info"
            >
              二级学院
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="上级院系"
          min-width="180"
        >
          <template #default="{ row }">
            {{ getParentDeptName(row.parentId) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="leader"
          label="负责人"
          min-width="120"
        />
        <el-table-column
          prop="createTime"
          label="创建时间"
          min-width="180"
        >
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="160"
          fixed="right"
          :resizable="false"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              link
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryForm.current"
          v-model:page-size="queryForm.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="computedRules"
        label-width="100px"
      >
        <el-form-item
          label="院系编号"
          prop="deptCode"
        >
          <el-input
            v-model="form.deptCode"
            placeholder="请输入院系编号"
          />
        </el-form-item>
        <el-form-item
          label="院系名称"
          prop="deptName"
        >
          <el-input
            v-model="form.deptName"
            placeholder="请输入院系名称"
          />
        </el-form-item>
        <el-form-item
          v-if="!isDeptAdmin"
          label="院系层级"
          prop="deptType"
        >
          <el-select
            v-model="form.deptType"
            placeholder="请选择院系层级"
            style="width: 100%"
            @change="handleDeptTypeChange"
          >
            <el-option
              label="一级学院"
              :value="1"
            />
            <el-option
              label="二级学院"
              :value="2"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-if="form.deptType === 2"
          label="上级院系"
          prop="parentId"
        >
          <el-select
            v-model="form.parentId"
            placeholder="请选择上级院系"
            :disabled="isDeptAdmin"
            style="width: 100%"
          >
            <el-option
              v-for="dept in primaryDeptOptions"
              :key="dept.id"
              :label="dept.deptName"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="负责人"
          prop="leader"
        >
          <el-input
            v-model="form.leader"
            placeholder="请输入负责人"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          @click="handleSubmit"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDateTime } from '@/utils/dateFormat'
import { useTableResize } from '@/composables/useTableResize.js'
import request from '@/utils/request.js'
import { useUserStore } from '@/stores/user.js'

/** 用户Store实例 */
const userStore = useUserStore()

const tableRef = ref(null)
useTableResize(tableRef)

/** 当前用户是否为院系管理员 */
const isDeptAdmin = computed(() => userStore.isDeptAdmin())

/** 当前院系管理员所属一级学院ID */
const deptAdminParentId = ref(null)

/** 表格数据 */
const tableData = ref([])
/** 数据总条数 */
const total = ref(0)
/** 加载状态 */
const loading = ref(false)
/** 弹窗显示状态 */
const dialogVisible = ref(false)
/** 弹窗标题 */
const dialogTitle = ref('新增院系')
/** 编辑时的院系ID */
const editId = ref(null)
/** 表单引用 */
const formRef = ref(null)
/** 一级学院下拉选项列表 */
const primaryDeptOptions = ref([])

/** 查询参数 */
const queryForm = reactive({
  current: 1,
  size: 10,
  deptName: '',
  deptType: null,
  parentId: null
})

/** 表单数据 */
const form = reactive({
  deptCode: '',
  deptName: '',
  deptType: null,
  parentId: null,
  leader: ''
})

/** 基础表单校验规则 */
const baseRules = {
  deptCode: [{ required: true, message: '请输入院系编号', trigger: 'blur' }],
  deptName: [{ required: true, message: '请输入院系名称', trigger: 'blur' }],
  deptType: [{ required: true, message: '请选择院系层级', trigger: 'change' }]
}

/** 动态表单校验规则，根据deptType和角色动态调整 */
const computedRules = computed(() => {
  /** 复制基础校验规则 */
  const rules = { ...baseRules }
  if (form.deptType === 2) {
    rules.parentId = [{ required: true, message: '请选择上级院系', trigger: 'change' }]
  }
  return rules
})

/**
 * 根据上级院系ID获取院系名称
 * @param {number|null} parentId 上级院系ID
 * @returns {string|number|null} 上级院系名称，未找到时返回原始ID
 */
function getParentDeptName(parentId) {
  if (!parentId) return ''
  /** 在一级学院选项中查找匹配项 */
  const dept = primaryDeptOptions.value.find(item => item.id === parentId)
  return dept ? dept.deptName : parentId
}

/** 加载一级学院下拉选项 */
async function loadPrimaryDeptOptions() {
  try {
    /** 请求一级学院列表数据 */
    const res = await request.get('/admin/depts', { params: { deptType: 1, size: 100 } })
    primaryDeptOptions.value = res.data.records
  } catch (e) {
    console.warn('获取一级学院列表失败', e.message)
  }
}

/**
 * 获取院系管理员所属一级学院ID
 * 直接从userStore中获取后端返回的deptId
 */
function loadDeptAdminInfo() {
  if (!isDeptAdmin.value) return
  deptAdminParentId.value = userStore.userInfo.deptId || null
}

/** 获取院系列表数据 */
async function fetchData() {
  loading.value = true
  try {
    /** 构建查询参数 */
    const params = {
      current: queryForm.current,
      size: queryForm.size,
      deptName: queryForm.deptName || undefined,
      deptType: queryForm.deptType || undefined,
      parentId: queryForm.parentId || undefined
    }
    /** 院系列表接口响应数据 */
    const res = await request.get('/admin/depts', { params })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.warn('获取院系列表失败', e.message)
  } finally {
    loading.value = false
  }
}

/** 搜索 */
function handleSearch() {
  queryForm.current = 1
  fetchData()
}

/** 重置 */
function handleReset() {
  queryForm.deptName = ''
  queryForm.deptType = null
  queryForm.parentId = null
  queryForm.current = 1
  fetchData()
}

/** 新增 */
function handleAdd() {
  editId.value = null
  dialogTitle.value = '新增院系'
  form.deptCode = ''
  form.deptName = ''
  form.leader = ''
  if (isDeptAdmin.value) {
    form.deptType = 2
    form.parentId = deptAdminParentId.value
  } else {
    form.deptType = null
    form.parentId = null
  }
  dialogVisible.value = true
}

/**
 * 编辑
 * @param {object} row 当前行数据
 */
function handleEdit(row) {
  editId.value = row.id
  dialogTitle.value = '编辑院系'
  form.deptCode = row.deptCode
  form.deptName = row.deptName
  form.deptType = row.deptType
  form.parentId = row.parentId
  form.leader = row.leader
  dialogVisible.value = true
}

/**
 * 院系层级变更处理
 * 从二级学院切换到一级学院时清空上级院系
 * @param {number} val 新的层级值
 */
function handleDeptTypeChange(val) {
  if (val === 1) {
    form.parentId = null
  }
}

/** 提交表单 */
async function handleSubmit() {
  /** 表单校验结果 */
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    /** 构建提交数据，一级学院不传parentId */
    const data = {
      deptCode: form.deptCode,
      deptName: form.deptName,
      deptType: form.deptType,
      parentId: form.deptType === 2 ? form.parentId : null,
      leader: form.leader
    }
    if (editId.value) {
      await request.put(`/admin/depts/${editId.value}`, data)
      ElMessage.success('更新成功')
    } else {
      await request.post('/admin/depts', data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    console.warn('提交失败', e.message)
  }
}

/**
 * 删除
 * @param {object} row 当前行数据
 */
function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除院系"${row.deptName}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/admin/depts/${row.id}`)
      ElMessage.success('删除成功')
      fetchData()
    } catch (e) {
      console.warn('删除失败', e.message)
    }
  }).catch(() => {})
}

onMounted(async () => {
  await loadPrimaryDeptOptions()
  loadDeptAdminInfo()
  fetchData()
})
</script>
