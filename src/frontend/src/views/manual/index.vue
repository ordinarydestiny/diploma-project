<template>
  <div class="page-container">
    <el-card>
      <div class="page-content">
        <h2>用户手册</h2>
        <div class="manual-cards">
          <el-card
            shadow="hover"
            class="manual-item"
          >
            <h3>顶岗实习用户手册</h3>
          </el-card>
          <el-card
            shadow="hover"
            class="manual-item"
          >
            <h3>毕设系统用户手册</h3>
          </el-card>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.page-content {
  text-align: center;
  padding: 40px 0;
}

.page-content h2 {
  color: #333;
  margin-bottom: 30px;
}

.manual-cards {
  display: flex;
  justify-content: center;
  gap: 40px;
  flex-wrap: wrap;
}

.manual-item {
  width: 300px;
  cursor: pointer;
}

.manual-item h3 {
  color: #409eff;
  margin: 0;
}
</style>
