<template>
  <div class="my-graduation-container">
    <div v-if="loading" v-loading="loading" style="min-height: 400px;"></div>
    
    <el-collapse v-else v-model="activeNames" class="custom-collapse">
      <!-- 1. 毕业设计批次信息 -->
      <el-collapse-item name="batch">
        <template #title>
          <div class="collapse-title">
            <span class="title-icon">📋</span>
            <span class="title-text">毕业设计批次信息</span>
            <el-tag :type="batchInfo.status === 'active' ? 'success' : batchInfo.status === 'draft' ? 'info' : 'info'" size="small" class="status-tag">
              {{ batchInfo.status === 'active' ? '进行中' : batchInfo.status === 'draft' ? '未开始' : '已结束' }}
            </el-tag>
          </div>
        </template>

        <div class="collapse-content">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="批次名称">{{ batchInfo.batchName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="学期">{{ batchInfo.semester || '-' }}</el-descriptions-item>
            <el-descriptions-item label="开始时间">{{ batchInfo.startDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="结束时间">{{ batchInfo.endDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="当前阶段">
              {{ formatPhase(batchInfo.currentPhase) }}
            </el-descriptions-item>
            <el-descriptions-item label="指导老师">{{ batchInfo.teacherName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="所属专业/班级" :span="2">{{ batchInfo.majorName || '-' }} / {{ batchInfo.className || '-' }}</el-descriptions-item>
          </el-descriptions>

          <div class="timeline-section" v-if="batchInfo.timeline && batchInfo.timeline.length > 0">
            <h4>📅 重要时间节点</h4>
            <el-timeline>
              <el-timeline-item
                v-for="(item, index) in batchInfo.timeline"
                :key="index"
                :timestamp="item.date"
                :type="item.type"
                placement="top"
              >
                <strong>{{ item.title }}</strong>
                <p>{{ item.description }}</p>
              </el-timeline-item>
            </el-timeline>
          </div>
        </div>
      </el-collapse-item>

      <!-- 2. 毕业设计题目信息 -->
      <el-collapse-item name="topic">
        <template #title>
          <div class="collapse-title">
            <span class="title-icon">📝</span>
            <span class="title-text">毕业设计题目信息</span>
            <el-tag :type="getTopicStatusType(topicInfo.selectionStatus)" size="small" class="status-tag">
              {{ formatSelectionStatus(topicInfo.selectionStatus) }}
            </el-tag>
          </div>
        </template>

        <div class="collapse-content">
          <el-alert
            v-if="!topicInfo.selectionId || topicInfo.selectionStatus === 'pending'"
            title="您尚未选择毕业设计题目或正在等待审核"
            type="warning"
            :closable="false"
            show-icon
            style="margin-bottom: 16px;"
          >
            <template #default>
              {{ !topicInfo.selectionId ? '请尽快在规定时间内选择您的毕设题目。您可以浏览题库并提交选题申请。' : '您的选题已提交，正在等待指导老师审核，请耐心等待。' }}
            </template>
          </el-alert>

          <el-descriptions :column="1" border v-if="topicInfo.topicName">
            <el-descriptions-item label="题目名称">
              <strong style="font-size: 16px; color: #303133;">{{ topicInfo.topicName }}</strong>
            </el-descriptions-item>
            <el-descriptions-item label="题目类型">{{ formatTopicType(topicInfo.topicType) }}</el-descriptions-item>
            <el-descriptions-item label="题目来源">{{ formatSource(topicInfo.source) }}</el-descriptions-item>
            <el-descriptions-item label="难度等级" v-if="topicInfo.difficulty">
              <el-rate v-model="topicInfo.difficulty" disabled />
              <span style="margin-left: 8px;">{{ ['简单', '较易', '中等', '较难', '困难'][topicInfo.difficulty - 1] }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="选择时间">{{ topicInfo.selectTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="审核状态">
              <el-tag :type="topicInfo.selectionStatus === 'approved' ? 'success' : topicInfo.selectionStatus === 'rejected' ? 'danger' : 'warning'" size="small">
                {{ topicInfo.selectionStatus === 'approved' ? '✅ 已通过' : topicInfo.selectionStatus === 'rejected' ? '❌ 已驳回' : '⏳ 待审核' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="题目描述" :span="2" v-if="topicInfo.description">
              <div class="description-content">{{ topicInfo.description }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="技术要求" :span="2" v-if="topicInfo.requirements">
              <div class="description-content">{{ topicInfo.requirements }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="参考文献" :span="2" v-if="topicInfo.references">
              <div class="reference-list">
                <p v-for="(ref, index) in parseReferences(topicInfo.references)" :key="index">· {{ ref }}</p>
              </div>
            </el-descriptions-item>
          </el-descriptions>

          <div class="action-buttons" v-if="!topicInfo.selectionId || topicInfo.selectionStatus === 'rejected'">
            <el-button type="primary" @click="handleSelectTopic">
              <el-icon><Search /></el-icon>
              浏览题库并选择
            </el-button>
          </div>
        </div>
      </el-collapse-item>

      <!-- 3. 毕业设计任务书信息 -->
      <el-collapse-item name="taskbook">
        <template #title>
          <div class="collapse-title">
            <span class="title-icon">📄</span>
            <span class="title-text">毕业设计任务书信息</span>
            <el-tag :type="getTaskBookStatusType(taskBookInfo.status)" size="small" class="status-tag">
              {{ formatTaskBookStatus(taskBookInfo.status) }}
            </el-tag>
          </div>
        </template>

        <div class="collapse-content">
          <el-alert
            v-if="!taskBookInfo.taskId || taskBookInfo.status === 'unissued'"
            title="任务书尚未下达"
            type="info"
            :closable="false"
            show-icon
            style="margin-bottom: 16px;"
          >
            <template #default>
              您的指导老师还未下达任务书，请耐心等待或联系老师。
            </template>
          </el-alert>

          <el-descriptions :column="2" border v-if="taskBookInfo.content">
            <el-descriptions-item label="下达时间">{{ taskBookInfo.issuedAt || '-' }}</el-descriptions-item>
            <el-descriptions-item label="完成期限">{{ taskBookInfo.deadline || '-' }}</el-descriptions-item>
            <el-descriptions-item label="任务书状态">
              <el-tag :type="taskBookInfo.status === 'confirmed' ? 'success' : taskBookInfo.status === 'rejected' ? 'danger' : 'warning'" size="small">
                {{ taskBookInfo.status === 'confirmed' ? '✅ 已确认' : taskBookInfo.status === 'rejected' ? '❌ 已驳回' : '⏳ 待确认' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="指导老师">{{ taskBookInfo.issuerName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="主要任务" :span="2">
              <div class="task-content">{{ taskBookInfo.content }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="基本要求" :span="2" v-if="taskBookInfo.requirementList && taskBookInfo.requirementList.length > 0">
              <div class="requirement-list">
                <p v-for="(req, index) in taskBookInfo.requirementList" :key="index">
                  <el-icon><Check /></el-icon> {{ req.text }}
                </p>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="技术参数" :span="2" v-if="taskBookInfo.techParamList && taskBookInfo.techParamList.length > 0">
              <div class="tech-params">
                <el-table :data="taskBookInfo.techParamList" size="small" border>
                  <el-table-column prop="name" label="参数名称" width="200" />
                  <el-table-column prop="value" label="要求值" />
                  <el-table-column prop="note" label="备注" />
                </el-table>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="参考资料" :span="2" v-if="taskBookInfo.referenceList && taskBookInfo.referenceList.length > 0">
              <div class="reference-list">
                <p v-for="(ref, index) in taskBookInfo.referenceList" :key="index">· {{ ref }}</p>
              </div>
            </el-descriptions-item>
          </el-descriptions>

          <div class="action-buttons" v-if="taskBookInfo.status === 'issued' && !taskBookInfo.confirmBy">
            <el-button type="success" @click="handleConfirmTaskBook">
              <el-icon><Check /></el-icon>
              确认接收任务书
            </el-button>
          </div>

          <div class="action-buttons" v-if="taskBookInfo.content">
            <el-button type="primary" plain @click="handleDownloadTaskBook">
              <el-icon><Download /></el-icon>
              下载任务书PDF
            </el-button>
          </div>
        </div>
      </el-collapse-item>

      <!-- 4. 毕业设计中期检查信息 -->
      <el-collapse-item name="midterm">
        <template #title>
          <div class="collapse-title">
            <span class="title-icon">✏️</span>
            <span class="title-text">毕业设计中期检查信息</span>
            <el-tag :type="getMidtermStatusType(midtermInfo.status)" size="small" class="status-tag">
              {{ formatMidtermStatus(midtermInfo.status) }}
            </el-tag>
          </div>
        </template>

        <div class="collapse-content">
          <el-descriptions :column="2" border v-if="midtermInfo.checkId">
            <el-descriptions-item label="当前状态">
              <el-tag :type="getMidtermStatusType(midtermInfo.status)" size="large">
                {{ formatMidtermStatus(midtermInfo.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="最新进度" v-if="midtermInfo.progress !== null && midtermInfo.progress !== undefined">
              <el-progress
                :percentage="midtermInfo.progress"
                :status="getProgressStatus(midtermInfo.progress)"
                :stroke-width="20"
                :text-inside="true"
                style="width: 200px;"
              />
            </el-descriptions-item>
            <el-descriptions-item label="最近提交时间">{{ midtermInfo.submitTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="提交次数">{{ midtermInfo.versions ? midtermInfo.versions.length : 0 }} 次</el-descriptions-item>
            <el-descriptions-item label="老师评语" :span="2" v-if="midtermInfo.reviewComment">
              <div class="teacher-comment-box">
                <div class="comment-label">💬 指导老师意见</div>
                <div class="comment-content">{{ midtermInfo.reviewComment }}</div>
              </div>
            </el-descriptions-item>
          </el-descriptions>

          <el-alert
            v-else
            title="暂无中期检查记录"
            type="info"
            :closable="false"
            show-icon
            style="margin-bottom: 16px;"
          >
            <template #default>
              您尚未提交中期检查报告。请根据任务书要求，在规定时间内提交。
            </template>
          </el-alert>

          <div class="report-section" v-if="midtermInfo.versions && midtermInfo.versions.length > 0">
            <h4>📊 历次中期检查报告</h4>
            <el-timeline>
              <el-timeline-item
                v-for="(report, index) in midtermInfo.versions"
                :key="index"
                :timestamp="report.submitTime"
                :type="report.status === 'approved' ? 'success' : report.status === 'rejected' ? 'danger' : 'warning'"
                placement="top"
              >
                <el-card shadow="hover" class="report-card">
                  <div class="report-header">
                    <strong>第 {{ report.version }} 次提交</strong>
                    <el-tag :type="report.status === 'approved' ? 'success' : report.status === 'rejected' ? 'danger' : 'warning'" size="small">
                      {{ formatMidtermStatus(report.status) }}
                    </el-tag>
                  </div>
                  <div class="report-progress" v-if="report.progress !== null && report.progress !== undefined">
                    <span>完成进度：</span>
                        <el-progress
                          :percentage="report.progress"
                          :status="getProgressStatus(report.progress)"
                          :stroke-width="10"
                          style="width: 150px; display: inline-block;"
                        />
                      </div>
                      <div class="report-content" v-if="report.comment">
                        <p><strong>老师反馈：</strong>{{ report.comment }}</p>
                      </div>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </div>

          <div class="action-buttons" v-if="!midtermInfo.checkId || ['draft', 'rejected'].includes(midtermInfo.status)">
            <el-button type="primary" @click="handleSubmitMidtermReport">
              <el-icon><Upload /></el-icon>
              提交中期检查报告
            </el-button>
          </div>
        </div>
      </el-collapse-item>

          <!-- 5. 毕业设计最终检查信息 -->
          <el-collapse-item name="final">
            <template #title>
              <div class="collapse-title">
                <span class="title-icon">🎯</span>
                <span class="title-text">毕业设计最终检查信息</span>
                <el-tag :type="getFinalStatusType(finalInfo.status)" size="small" class="status-tag">
                  {{ formatFinalStatus(finalInfo.status) }}
                  <span v-if="finalInfo.isFinal" style="margin-left: 4px;">✨</span>
                </el-tag>
              </div>
            </template>

            <div class="collapse-content">
              <el-alert
                v-if="finalInfo.isFinal"
                title="🎉 恭喜！您的毕设报告已定稿"
                type="success"
                :closable="false"
                show-icon
                style="margin-bottom: 16px;"
              >
                <template #default>
                  您的毕设报告已于 <strong>{{ finalInfo.finalizedAt }}</strong> 正式定稿，该版本将用于学校存档和答辩评审。
                </template>
              </el-alert>

              <el-descriptions :column="2" border v-if="finalInfo.checkId">
                <el-descriptions-item label="定稿状态">
                  <el-tag :type="getFinalStatusType(finalInfo.status)" size="large" effect="dark">
                    {{ formatFinalStatus(finalInfo.status) }}
                    <el-icon v-if="finalInfo.isFinal" style="margin-left: 4px;"><Finished /></el-icon>
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="当前版本">
                  <el-tag type="primary" effect="dark">V{{ finalInfo.version }}.0</el-tag>
                  <span v-if="finalInfo.isFinal" style="margin-left: 8px; color: #67c23a; font-weight: bold;">(最终版)</span>
                </el-descriptions-item>
                <el-descriptions-item label="定稿时间" v-if="finalInfo.finalizedAt">
                  <span style="color: #67c23a; font-weight: bold;">{{ finalInfo.finalizedAt }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="提交次数">{{ finalInfo.history ? finalInfo.history.length : 0 }} 次</el-descriptions-item>
                <el-descriptions-item label="教师评语（存档）" :span="2" v-if="finalInfo.reviewComment">
                  <div class="teacher-comment-archive">
                    <div class="archive-label">⭐ 该评语将作为正式教师评语存档</div>
                    <div class="archive-content">{{ finalInfo.reviewComment }}</div>
                  </div>
                </el-descriptions-item>
              </el-descriptions>

              <el-alert
                v-else
                title="暂无最终检查记录"
                type="info"
                :closable="false"
                show-icon
                style="margin-bottom: 16px;"
              >
                <template #default>
                  您尚未提交最终毕设报告。请在中期检查通过后，根据要求提交最终版本。
                </template>
              </el-alert>

              <div class="revision-history" v-if="finalInfo.history && finalInfo.history.length > 0">
                <h4>📜 版本修改记录</h4>
                <el-timeline>
                  <el-timeline-item
                    v-for="(item, index) in finalInfo.history"
                    :key="index"
                    :timestamp="item.submitTime"
                    :type="item.action === '通过并定稿' ? 'success' : item.action === '驳回' ? 'danger' : 'primary'"
                    placement="top"
                  >
                    <div class="history-item">
                      <strong>V{{ item.version }}.0 - {{ item.action }}</strong>
                      <p v-if="item.comment">{{ item.comment }}</p>
                    </div>
                  </el-timeline-item>
                </el-timeline>
              </div>

              <div class="action-buttons" v-if="!finalInfo.checkId || ['pending', 'rejected'].includes(finalInfo.status)">
                <el-button type="success" @click="handleSubmitFinalReport">
                  <el-icon><Upload /></el-icon>
                  提交最终毕设报告
                </el-button>
              </div>
            </div>
          </el-collapse-item>

          <!-- 6. 毕业设计答辩信息 -->
          <el-collapse-item name="defense">
            <template #title>
              <div class="collapse-title">
                <span class="title-icon">🎤</span>
                <span class="title-text">毕业设计论文评阅信息（答辩）</span>
                <el-tag :type="getDefenseStatusType(defenseInfo.status)" size="small" class="status-tag">
                  {{ formatDefenseStatus(defenseInfo.status) }}
                </el-tag>
              </div>
            </template>

            <div class="collapse-content">
              <el-alert
                v-if="!defenseInfo.defenseId || defenseInfo.status === 'not_started'"
                title="答辩安排"
                type="info"
                :closable="false"
                show-icon
                style="margin-bottom: 16px;"
              >
                <template #default>
                  答辩时间：<strong>待通知</strong><br/>
                  答辩地点：<strong>待通知</strong><br/>
                  请提前准备好答辩PPT和相关材料。
                </template>
              </el-alert>

              <el-descriptions :column="2" border v-if="defenseInfo.defenseId && defenseInfo.status !== 'not_started'">
                <el-descriptions-item label="答辩状态">
                  <el-tag :type="getDefenseStatusType(defenseInfo.status)" size="large">
                    {{ formatDefenseStatus(defenseInfo.status) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="答辩成绩" v-if="defenseInfo.defenseScoreNum !== null && defenseInfo.defenseScoreNum !== undefined">
                  <span style="font-size: 24px; font-weight: bold; color: #409eff;">{{ defenseInfo.defenseScoreNum }}</span>
                  <span style="margin-left: 8px;">分</span>
                </el-descriptions-item>
                <el-descriptions-item label="成绩等级" v-if="scoreInfo.gradeLevel">
                  <el-tag :type="getGradeType(scoreInfo.gradeLevel)" size="large" effect="dark">
                    {{ scoreInfo.gradeLevel }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="总成绩" v-if="scoreInfo.totalScore !== null && scoreInfo.totalScore !== undefined">
                  <span style="font-size: 24px; font-weight: bold; color: #67c23a;">{{ scoreInfo.totalScore }}</span>
                  <span style="margin-left: 8px;">分</span>
                </el-descriptions-item>
                <el-descriptions-item label="答辩日期" v-if="defenseInfo.defenseDatetime">{{ defenseInfo.defenseDatetime }}</el-descriptions-item>
                <el-descriptions-item label="答辩地点" v-if="defenseInfo.location">{{ defenseInfo.location }}</el-descriptions-item>
                <el-descriptions-item label="答辩委员会" v-if="defenseInfo.committee">{{ defenseInfo.committee }}</el-descriptions-item>
                <el-descriptions-item label="答辩评语" :span="2" v-if="defenseInfo.reviewComment">
                  <div class="defense-comment">
                    <div class="comment-label">📝 答辩委员会评语</div>
                    <div class="comment-text">{{ defenseInfo.reviewComment }}</div>
                  </div>
                </el-descriptions-item>
              </el-descriptions>

              <div class="document-section" v-if="defenseInfo.recordFileName">
                <h4>📋 答辩相关文档</h4>
                <el-card shadow="hover">
                  <div class="file-item">
                    <el-icon style="color: #f56c6c; font-size: 32px;"><Document /></el-icon>
                    <div class="file-info">
                      <div class="file-name">{{ defenseInfo.recordFileName }}</div>
                      <div class="file-action">
                        <el-button type="primary" link size="small" @click="handleViewDefensePdf">
                          <el-icon><View /></el-icon>
                          在线查看
                        </el-button>
                        <el-button type="success" link size="small" @click="handleDownloadDefensePdf">
                          <el-icon><Download /></el-icon>
                          下载文档
                        </el-button>
                      </div>
                    </div>
                  </div>
                </el-card>
              </div>

              <div class="tips-section">
                <el-alert
                  title="💡 温馨提示"
                  type="warning"
                  :closable="false"
                  show-icon
                >
                  <template #default>
                    <ul style="margin: 0; padding-left: 20px;">
                      <li>请携带学生证、身份证参加答辩</li>
                      <li>建议提前15分钟到达答辩地点</li>
                      <li>准备5-8分钟的PPT汇报</li>
                      <li>准备好纸质版毕设论文（按要求的份数）</li>
                    </ul>
                  </template>
                </el-alert>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
    </template>

    <script setup>
    import { ref, onMounted } from 'vue'
    import { Search, Check, Download, Upload, View, Document, Finished } from '@element-plus/icons-vue'
    import { ElMessage, ElMessageBox } from 'element-plus'
    import request from '@/utils/request'
    import { useUserStore } from '@/stores/user'

    const userStore = useUserStore()
    const activeNames = ref(['batch'])
    const loading = ref(false)

    // 从API获取的数据
    const batchInfo = ref({})
    const topicInfo = ref({})
    const taskBookInfo = ref({})
    const midtermInfo = ref({})
    const finalInfo = ref({})
    const defenseInfo = ref({})
    const scoreInfo = ref({})

    // 页面加载时获取数据
    onMounted(async () => {
      await fetchMyGraduationInfo()
    })

    /**
     * 获取学生个人毕设完整信息
     */
    async function fetchMyGraduationInfo() {
      loading.value = true
      try {
        const res = await request.get('/v1/graduation/student/my-info')
        if (res.data) {
          // 批次信息
          batchInfo.value = res.data.batchInfo || {}
          
          // 题目信息
          topicInfo.value = res.data.topicInfo || {}
          
          // 任务书信息
          taskBookInfo.value = res.data.taskBookInfo || {}
          
          // 中期检查信息
          midtermInfo.value = res.data.midtermInfo || {}
          
          // 最终检查信息
          finalInfo.value = res.data.finalInfo || {}
          
          // 答辩信息
          defenseInfo.value = res.data.defenseInfo || {}
          
          // 成绩信息
          scoreInfo.value = res.data.scoreInfo || {}
        }
      } catch (error) {
        console.error('获取毕设信息失败:', error)
        ElMessage.error('获取毕设信息失败，请刷新页面重试')
      } finally {
        loading.value = false
      }
    }

    function getTopicStatusType(status) {
      const map = {
        '待选择': 'info',
        '已选题': 'success',
        '审核中': 'warning'
      }
      return map[status] || 'info'
    }

    function getTaskBookStatusType(status) {
      const map = {
        '未下达': 'info',
        '已下达': 'warning',
        '已确认': 'success'
      }
      return map[status] || 'info'
    }

    function getMidtermStatusType(status) {
      const map = {
        '未提交': 'info',
        '待审核': 'warning',
        '已通过': 'success',
        '需修改': 'danger'
      }
      return map[status] || 'info'
    }

    function getFinalStatusType(status) {
      const map = {
        '未提交': 'info',
        '待审核': 'warning',
        '已定稿': 'success',
        '需修改': 'danger'
      }
      return map[status] || 'info'
    }

    function getDefenseStatusType(status) {
      const map = {
        '未答辩': 'info',
        '待提交': 'warning',
        '待审核': 'warning',
        '已通过': 'success',
        '需修改': 'danger'
      }
      return map[status] || 'info'
    }

    function getGradeType(grade) {
      const map = {
        '优秀': 'success',
        '良好': '',
        '中等': 'warning',
        '及格': 'info',
        '不及格': 'danger'
      }
      return map[grade] || 'info'
    }

    function getProgressStatus(progress) {
      if (progress >= 90) return 'success'
      if (progress >= 70) return ''
      if (progress >= 50) return 'warning'
      return 'exception'
    }

    // 格式化函数
    function formatPhase(phase) {
      const map = {
        'preparation': '准备阶段',
        'selection': '选题阶段',
        'taskbook': '任务书下达阶段',
        'midterm': '中期检查阶段',
        'final': '最终检查阶段',
        'defense': '答辩阶段',
        'finished': '已全部完成'
      }
      return map[phase] || phase || '-'
    }

    function formatSelectionStatus(status) {
      const map = {
        'pending': '待审核',
        'approved': '已通过',
        'rejected': '已驳回',
        'cancelled': '已取消'
      }
      return map[status] || status || '未选题'
    }

    function formatTopicType(type) {
      const map = {
        'research': '科研型',
        'engineering': '工程型',
        'thesis': '论文型'
      }
      return map[type] || type || '-'
    }

    function formatSource(source) {
      const map = {
        'teacher': '教师命题',
        'student': '学生自拟',
        'enterprise': '企业课题'
      }
      return map[source] || source || '-'
    }

    function parseReferences(refs) {
      if (!refs) return []
      if (Array.isArray(refs)) return refs
      return refs.split('\n').filter(r => r.trim())
    }

    function formatTaskBookStatus(status) {
      const map = {
        'unissued': '未下达',
        'issued': '已下达',
        'confirmed': '已确认',
        'rejected': '已驳回'
      }
      return map[status] || status || '未知'
    }

    function formatMidtermStatus(status) {
      const map = {
        'draft': '未提交',
        'submitted': '已提交',
        'pending': '待审核',
        'approved': '已通过',
        'rejected': '需修改'
      }
      return map[status] || status || '未知'
    }

    function formatFinalStatus(status) {
      const map = {
        'pending': '待审核',
        'approved': '已通过',
        'rejected': '需修改'
      }
      return map[status] || (status === null ? '未提交' : status || '未知')
    }

    function formatDefenseStatus(status) {
      const map = {
        'not_started': '未答辩',
        'submitted': '待提交',
        'pending': '待审核',
        'approved': '已通过',
        'rejected': '需修改'
      }
      return map[status] || status || '未知'
    }

    function handleSelectTopic() {
      ElMessage.info('跳转到题库浏览页面...')
    }

    function handleConfirmTaskBook() {
      ElMessageBox.confirm('确定要确认接收任务书吗？确认后将开始按照任务书要求开展工作。', '确认接收', {
        confirmButtonText: '确定确认',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        taskBookInfo.value.confirmed = true
        ElMessage.success('✅ 已成功确认接收任务书')
      }).catch(() => {})
    }

    function handleDownloadTaskBook() {
      if (!taskBookInfo.value.taskId) {
        ElMessage.warning('暂无任务书可下载')
        return
      }
      
      ElMessage.info('正在准备下载任务书PDF...')
      
      // 由于任务书是动态生成的，这里我们生成一个模拟的PDF下载
      // 实际项目中，应该从后端获取真实的任务书文件ID
      downloadFileAsPDF('任务书_' + (topicInfo.value.topicName || '毕业设计') + '.pdf', generateTaskBookContent())
    }

    /**
     * 生成任务书内容（用于PDF导出）
     */
    function generateTaskBookContent() {
      const content = `
毕业设计任务书
===============================

题目名称：${topicInfo.value.topicName || '未选题'}
指导老师：${taskBookInfo.value.issuerName || '未知'}
下达时间：${taskBookInfo.value.issuedAt || '未知'}
完成期限：${taskBookInfo.value.deadline || '未知'}

一、主要任务
${taskBookInfo.value.content || '暂无'}

二、基本要求
${(taskBookInfo.value.requirementList || []).map((req, i) => `${i + 1}. ${req.text}`).join('\n') || '暂无'}

三、技术参数
${(taskBookInfo.value.techParamList || []).map(param => `• ${param.name}：${param.value} ${param.note ? '(' + param.note + ')' : ''}`).join('\n') || '暂无'}

四、参考资料
${(taskBookInfo.value.referenceList || []).map(ref => ref).join('\n') || '暂无'}
      `.trim()
      
      return content
    }

    function handleSubmitMidtermReport() {
      ElMessage.info('打开中期检查报告提交页面...')
    }

    function handleSubmitFinalReport() {
      ElMessage.info('打开最终毕设报告提交页面...')
    }

    function handleViewDefensePdf() {
      if (!defenseInfo.value.recordFileId) {
        ElMessage.warning('暂无答辩文档可预览')
        return
      }
      
      // 打开新窗口预览PDF
      const previewUrl = `/api/files/preview/${defenseInfo.value.recordFileId}`
      window.open(previewUrl, '_blank', 'width=900,height=700,scrollbars=yes,resizable=yes')
    }

    function handleDownloadDefensePdf() {
      if (!defenseInfo.value.recordFileId) {
        ElMessage.warning('暂无答辩文档可下载')
        return
      }
      
      // 下载文件
      const downloadUrl = `/api/files/download/${defenseInfo.value.recordFileId}`
      
      // 创建隐藏的a标签触发下载
      const link = document.createElement('a')
      link.href = downloadUrl
      link.download = defenseInfo.value.recordFileName || '答辩记录.pdf'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      ElMessage.success('开始下载答辩记录文档...')
    }

    /**
     * 将文本内容下载为PDF（简化版，实际应使用后端生成）
     */
    function downloadFileAsPDF(fileName, content) {
      // 创建Blob对象
      const blob = new Blob([content], { type: 'application/pdf;charset=utf-8' })
      
      // 创建下载链接
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = fileName
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      // 释放URL对象
      window.URL.revokeObjectURL(url)
      
      ElMessage.success(`正在下载：${fileName}`)
    }
    </script>

    <style scoped>
    .my-graduation-container {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 84px);
}

.custom-collapse {
      background: transparent;
    }

    .custom-collapse :deep(.el-collapse-item) {
      margin-bottom: 12px;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    }

    .custom-collapse :deep(.el-collapse-item__header) {
      background: #fff;
      height: auto;
      min-height: 60px;
      padding: 16px 20px;
      border: none;
      font-size: 16px;
      font-weight: 500;
    }

    .custom-collapse :deep(.el-collapse-item__wrap) {
      background: #fafbfc;
      border: none;
    }

    .custom-collapse :deep(.el-collapse-item__content) {
      padding: 20px;
    }

    .collapse-title {
      display: flex;
      align-items: center;
      width: 100%;
      gap: 12px;
    }

    .title-icon {
      font-size: 24px;
    }

    .title-text {
      flex: 1;
      text-align: left;
    }

    .status-tag {
      flex-shrink: 0;
    }

    .collapse-content {
      color: #606266;
    }

    .timeline-section {
      margin-top: 20px;
      padding: 16px;
      background: #fff;
      border-radius: 6px;
    }

    .timeline-section h4 {
      margin: 0 0 16px 0;
      color: #303133;
    }

    .description-content,
    .task-content {
      white-space: pre-wrap;
      line-height: 1.8;
      color: #303133;
      font-size: 14px;
    }

    .requirement-list p,
    .reference-list p {
      margin: 6px 0;
      line-height: 1.8;
      color: #606266;
    }

    .requirement-list .el-icon {
      color: #67c23a;
      margin-right: 6px;
    }

    .tech-params {
      margin-top: 8px;
    }

    .action-buttons {
      margin-top: 20px;
      padding-top: 16px;
      border-top: 1px solid #ebeef5;
      display: flex;
      justify-content: center;
      gap: 12px;
    }

    .teacher-comment-box {
      border: 2px solid #e6a23c;
      border-radius: 6px;
      padding: 12px;
      background: #fdf6ec;
    }

    .comment-label {
      color: #e6a23c;
      font-weight: bold;
      margin-bottom: 8px;
      font-size: 13px;
    }

    .comment-content {
      white-space: pre-wrap;
      line-height: 1.8;
      color: #303133;
    }

    .report-section {
      margin-top: 20px;
    }

    .report-section h4 {
      margin: 0 0 16px 0;
      color: #303133;
    }

    .report-card {
      margin-bottom: 12px;
    }

    .report-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
    }

    .report-progress {
      margin: 8px 0;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .report-content p,
    .report-comment p {
      margin: 8px 0;
      line-height: 1.6;
      color: #606266;
    }

    .teacher-comment-archive {
      border: 2px solid #67c23a;
      border-radius: 6px;
      padding: 16px;
      background: #f0f9eb;
    }

    .archive-label {
      color: #67c23a;
      font-weight: bold;
      margin-bottom: 8px;
      font-size: 14px;
    }

    .archive-content {
      white-space: pre-wrap;
      line-height: 1.8;
      color: #303133;
      font-size: 14px;
    }

    .report-preview {
      margin-top: 20px;
    }

    .report-preview h4 {
      margin: 0 0 12px 0;
      color: #303133;
    }

    .preview-content {
      max-height: 400px;
      overflow-y: auto;
      background: #fff;
      border: 1px solid #dcdfe6;
      border-radius: 6px;
      padding: 16px;
    }

    .preview-content pre {
      margin: 0;
      white-space: pre-wrap;
      word-wrap: break-word;
      font-family: 'Microsoft YaHei', Arial, sans-serif;
      font-size: 13px;
      line-height: 1.8;
      color: #303133;
    }

    .revision-history {
      margin-top: 20px;
    }

    .revision-history h4 {
      margin: 0 0 16px 0;
      color: #303133;
    }

    .history-item strong {
      color: #303133;
    }

    .history-item p {
      margin: 6px 0 0 0;
      color: #606266;
      font-size: 13px;
    }

    .document-section {
      margin-top: 20px;
    }

    .document-section h4 {
      margin: 0 0 12px 0;
      color: #303133;
    }

    .file-item {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 12px;
    }

    .file-info {
      flex: 1;
    }

    .file-name {
      font-size: 15px;
      font-weight: 500;
      color: #303133;
      margin-bottom: 8px;
    }

    .file-action {
      display: flex;
      gap: 8px;
    }

    .defense-comment {
      border: 2px solid #409eff;
      border-radius: 6px;
      padding: 16px;
      background: #ecf5ff;
    }

    .defense-comment .comment-label {
      color: #409eff;
    }

    .defense-comment .comment-text {
      white-space: pre-wrap;
      line-height: 1.8;
      color: #303133;
      font-size: 14px;
    }

    .tips-section {
      margin-top: 20px;
    }

    .tips-section ul li {
      line-height: 2;
      color: #606266;
    }
    </style>
