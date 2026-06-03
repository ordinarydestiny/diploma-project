<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>题目管理</h2>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            发布题目
          </el-button>
        </div>
      </template>

      <div class="search-bar">
        <el-input v-model="searchForm.keyword" placeholder="搜索题目名称" clearable style="width: 300px" />
        <el-select v-model="searchForm.majorId" placeholder="选择专业" clearable style="width: 200px; margin-left: 10px" />
        <el-button type="primary" style="margin-left: 10px" @click="handleSearch">搜索</el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="topicName" label="题目名称" min-width="250" show-overflow-tooltip />
        <el-table-column prop="teacherName" label="指导教师" width="120" align="center" />
        <el-table-column prop="majorName" label="所属专业" width="140" align="center" />
        <el-table-column prop="type" label="题目类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag>{{ row.type === 'design' ? '设计' : '论文' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="maxStudents" label="最大人数" width="100" align="center" />
        <el-table-column prop="selectedCount" label="已选人数" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  keyword: '',
  majorId: ''
})

const tableData = ref([])

function getStatusType(status) {
  const map = { draft: 'info', published: 'success', full: 'danger', closed: 'warning' }
  return map[status] || 'info'
}

function getStatusText(status) {
  const map = { draft: '草稿', published: '已发布', full: '已满员', closed: '已关闭' }
  return map[status] || status
}

function handleAdd() {
  console.log('发布题目')
}

function handleEdit(row) {
  console.log('编辑', row)
}

function handleDelete(row) {
  console.log('删除', row)
}

function handleSearch() {
  console.log('搜索')
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.search-bar {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
