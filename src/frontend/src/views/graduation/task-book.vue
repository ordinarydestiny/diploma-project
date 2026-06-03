<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>下达任务书</h2>
          <el-button type="primary" @click="handleAdd" :disabled="!hasSelection">
            <el-icon><Plus /></el-icon>
            新建任务书
          </el-button>
        </div>
      </template>

      <el-alert v-if="!hasSelection" title="提示" type="warning" :closable="false" show-icon style="margin-bottom: 16px">
        学生尚未选题，无法下达任务书
      </el-alert>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="studentName" label="学生姓名" width="120" align="center" />
        <el-table-column prop="studentId" label="学号" width="140" align="center" />
        <el-table-column prop="topicName" label="题目名称" min-width="250" show-overflow-tooltip />
        <el-table-column prop="version" label="版本" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'approved' ? 'success' : 'warning'">
              {{ row.status === 'approved' ? '已确认' : '待确认' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160" align="center" />
        <el-table-column prop="updatedAt" label="更新时间" width="160" align="center" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="primary" link @click="handleEdit(row)" :disabled="row.status === 'approved'">编辑</el-button>
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
import { ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const hasSelection = ref(true)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const tableData = ref([])

function handleAdd() {
  console.log('新建任务书')
}

function handleView(row) {
  console.log('查看', row)
}

function handleEdit(row) {
  console.log('编辑', row)
}

function handleDelete(row) {
  console.log('删除', row)
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

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
