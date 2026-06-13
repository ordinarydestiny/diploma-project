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
            <el-tag :type="batchInfo.status === 'active' ? 'success' : batchInfo.status === 'draft' ? 'info' : 'danger'" size="small" class="status-tag">
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
              <el-tag :type="getTaskBookStatusType(taskBookInfo.status)" size="small">
                {{ formatTaskBookStatus(taskBookInfo.status) }}
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
            <el-descriptions-item label="最新进度">
              <el-progress
                :percentage="getMidtermProgress(midtermInfo.status, midtermInfo.progress)"
                :status="getMidtermProgressStatus(midtermInfo.status)"
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
                    <strong>第 {{ index + 1 }} 次提交</strong>
                    <el-tag :type="report.status === 'approved' ? 'success' : report.status === 'rejected' ? 'danger' : 'warning'" size="small">
                      {{ formatMidtermStatus(report.status) }}
                    </el-tag>
                  </div>
                  <div class="report-progress">
                    <span>完成进度：</span>
                        <el-progress
                          :percentage="getMidtermProgress(report.status, report.progress)"
                          :color="getProgressColor(report.status)"
                          :stroke-width="10"
                          :show-text="false"
                          style="width: 120px; display: inline-block;"
                        />
                        <span class="progress-text" :style="{ color: getProgressTextColor(report.status) }">
                          {{ getMidtermProgress(report.status, report.progress) }}%
                        </span>
                      </div>
                      <div class="report-content" v-if="report.comment">
                        <p><strong>老师反馈：</strong>{{ report.comment }}</p>
                      </div>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </div>

          <div class="action-buttons">
            <el-button type="primary" @click="handleSubmitMidtermReport"
                       v-if="!midtermInfo.checkId || ['draft', 'rejected'].includes(midtermInfo.status)">
              <el-icon><Upload /></el-icon>
              提交中期检查报告
            </el-button>
            <el-button type="success" plain @click="downloadMidtermTemplate">
              <el-icon><Download /></el-icon>
              下载报告模板
            </el-button>
          </div>

          <!-- 提交中期检查报告弹窗 -->
          <el-dialog
            v-model="midtermDialogVisible"
            title="提交中期检查报告"
            width="600px"
            :close-on-click-modal="false"
            @close="resetMidtermForm"
          >
            <el-form :model="midtermForm" :rules="midtermRules" ref="midtermFormRef" label-width="120px">
              <el-form-item label="完成进度" prop="progress">
                <el-slider v-model="midtermForm.progress" :marks="progressMarks" :format-tooltip="(val) => val + '%'" />
                <div style="margin-top: 8px; color: #909399; font-size: 12px;">
                  当前进度：{{ midtermForm.progress }}%
                </div>
              </el-form-item>

              <el-form-item label="报告说明" prop="description">
                <el-input
                  v-model="midtermForm.description"
                  type="textarea"
                  :rows="5"
                  placeholder="请简要说明当前完成的任务、遇到的问题及下一步计划..."
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="附件上传">
                <el-upload
                  class="upload-demo"
                  drag
                  action="/api/files/upload"
                  :headers="uploadHeaders"
                  :data="uploadData"
                  :on-success="handleMidtermFileSuccess"
                  :before-upload="beforeMidtermUpload"
                  :limit="1"
                  accept=".pdf,.doc,.docx,.zip,.rar"
                >
                  <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
                  <div class="el-upload__text">
                    将文件拖到此处，或<em>点击上传</em>
                  </div>
                  <template #tip>
                    <div class="el-upload__tip">
                      支持 PDF/Word/压缩包格式，单个文件不超过 50MB
                    </div>
                  </template>
                </el-upload>
                <div v-if="midtermForm.fileName" style="margin-top: 8px; color: #67c23a;">
                  <el-icon><Check /></el-icon> 已选择文件：{{ midtermForm.fileName }}
                </div>
              </el-form-item>
            </el-form>

            <template #footer>
              <span class="dialog-footer">
                <el-button @click="midtermDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="submitMidtermReport" :loading="midtermSubmitting">
                  确认提交
                </el-button>
              </span>
            </template>
          </el-dialog>
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
              <!-- 学生自录入口：当状态为"未开始"时显示填写按钮 -->
              <div v-if="!defenseInfo.defenseId || defenseInfo.status === 'not_started' || defenseInfo.status === '未答辩'" 
                   style="margin-bottom: 16px;">
                <el-alert
                  title="💡 提示：您可以提前填写答辩信息"
                  type="success"
                  :closable="false"
                  show-icon
                  style="margin-bottom: 12px;"
                >
                  <template #default>
                    填写您的答辩预计时间、上传PPT或报告文档、进行自我评价。
                    提交后，指导教师将审核并确认您的答辩安排。
                  </template>
                </el-alert>

                <div style="text-align: center;">
                  <el-button type="primary" size="large" @click="showDefenseSubmitDialog = true">
                    <el-icon><EditPen /></el-icon>
                    📝 填写答辩信息
                  </el-button>
                </div>
              </div>

              <el-alert
                v-if="(!defenseInfo.defenseId || defenseInfo.status === 'not_started' || defenseInfo.status === '未答辩') && !showDefenseSubmitDialog"
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

              <!-- 答辩安排已确定时显示具体信息 -->
              <el-alert
                v-if="defenseInfo.defenseId && defenseInfo.status !== 'not_started' && defenseInfo.status !== '未答辩'"
                title="答辩安排"
                type="success"
                :closable="false"
                show-icon
                style="margin-bottom: 16px;"
              >
                <template #default>
                  答辩时间：<strong>{{ defenseInfo.defenseDatetime || '待通知' }}</strong><br/>
                  答辩地点：<strong>{{ defenseInfo.location || '待通知' }}</strong><br/>
                  <span v-if="defenseInfo.committee">答辩委员会：<strong>{{ defenseInfo.committee }}</strong></span>
                  <span v-else>请提前准备好答辩PPT和相关材料。</span>
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
                  <!-- 部分成绩提示：报告或答辩有一项未完成 -->
                  <el-tag v-if="scoreInfo.isPartialScore" type="warning" size="small" effect="plain" 
                          style="margin-left: 12px;" title="当前为部分成绩（报告或答辩未全部完成）">
                    <el-icon><Warning /></el-icon>
                    部分
                  </el-tag>
                  <!-- 完整成绩提示 -->
                  <el-tooltip v-else content="✅ 报告和答辩成绩均已录入，此为最终总成绩" placement="top">
                    <el-tag type="success" size="small" effect="plain" style="margin-left: 12px;">
                      <el-icon><CircleCheck /></el-icon>
                      完整
                    </el-tag>
                  </el-tooltip>
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

          <!-- 学生填写答辩信息弹窗 -->
          <el-dialog 
            v-model="showDefenseSubmitDialog" 
            title="📝 填写答辩信息" 
            width="700px" 
            :close-on-click-modal="false"
            destroy-on-close
          >
            <el-alert
              title="请认真填写以下信息"
              type="info"
              :closable="false"
              show-icon
              style="margin-bottom: 20px;"
            >
              <template #default>
                您填写的信息将提交给指导教师审核。所有标有 * 的字段为必填项。
              </template>
            </el-alert>

            <el-form
              ref="defenseSubmitFormRef"
              :model="defenseSubmitForm"
              :rules="defenseSubmitRules"
              label-width="120px"
            >
              <el-form-item label="预计答辩日期" prop="defenseDate">
                <el-date-picker
                  v-model="defenseSubmitForm.defenseDate"
                  type="datetime"
                  placeholder="选择预计答辩日期时间"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%;"
                />
                <div style="color: #909399; font-size: 12px; margin-top: 4px;">
                  * 请选择您期望的答辩时间（具体时间以学校安排为准）
                </div>
              </el-form-item>

              <el-form-item label="自我评价" prop="selfEvaluation">
                <el-input
                  v-model="defenseSubmitForm.selfEvaluation"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入您的自我评价，包括：&#10;1. 毕设完成情况概述&#10;2. 创新点和技术亮点&#10;3. 答辩准备情况&#10;4. 其他需要说明的内容（不少于30字）"
                />
                <div style="color: #909399; font-size: 12px; margin-top: 4px;">
                  * 自我评价将作为教师参考，请认真填写（不少于30字）
                </div>
              </el-form-item>

              <el-form-item label="答辩文档" prop="pdfFile">
                <div class="upload-section">
                  <el-upload
                    ref="studentDefenseUploadRef"
                    :auto-upload="false"
                    :limit="1"
                    accept=".pdf,.doc,.docx,.ppt,.pptx"
                    :on-change="handleStudentDefenseFileChange"
                    :on-remove="handleStudentDefenseFileRemove"
                    :file-list="studentDefenseFileList"
                  >
                    <template #trigger>
                      <el-button type="primary" plain>
                        <el-icon><Upload /></el-icon>
                        选择文件（PDF/PPT/Word）
                      </el-button>
                    </template>
                    <template #tip>
                      <div class="el-upload__tip">
                        可上传：答辩PPT、毕设报告、演示文档等<br/>
                        文件大小不超过20MB
                      </div>
                    </template>
                  </el-upload>
                </div>
                <div style="color: #909399; font-size: 12px; margin-top: 4px;">
                  * 建议上传答辩PPT或相关材料（选填）
                </div>
              </el-form-item>
            </el-form>

            <template #footer>
              <el-button @click="showDefenseSubmitDialog = false">取消</el-button>
              <el-button type="primary" @click="submitStudentDefenseInfo" :loading="submittingDefense">
                {{ submittingDefense ? '提交中...' : '✅ 提交答辩信息' }}
              </el-button>
            </template>
          </el-dialog>

        </el-collapse>
      </div>
    </template>

    <script setup>
    import { ref, computed, onMounted } from 'vue'
    import { Search, Check, Download, Upload, View, Document, Finished, UploadFilled, Warning, CircleCheck } from '@element-plus/icons-vue'
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

    // 【新增】学生填写答辩信息相关
    const showDefenseSubmitDialog = ref(false)
    const submittingDefense = ref(false)
    const defenseSubmitFormRef = ref(null)
    const studentDefenseFileList = ref([])
    
    // 学生提交表单数据
    const defenseSubmitForm = reactive({
      defenseDate: '',           // 预计答辩日期
      selfEvaluation: '',        // 自我评价
      pdfFile: null              // 上传的文件
    })
    
    // 学生提交表单验证规则
    const defenseSubmitRules = {
      defenseDate: [
        { required: true, message: '请选择预计答辩日期', trigger: 'change' }
      ],
      selfEvaluation: [
        { required: true, message: '请输入自我评价', trigger: 'blur' },
        { min: 30, message: '自我评价至少30个字符', trigger: 'blur' }
      ]
    }

    // 中期检查提交弹窗相关
    const midtermDialogVisible = ref(false)
    const midtermSubmitting = ref(false)
    const midtermFormRef = ref(null)
    const midtermForm = ref({
      progress: 40,
      description: '',
      fileId: null,
      fileName: ''
    })
    const midtermRules = {
      progress: [
        { required: true, message: '请选择完成进度', trigger: 'change' }
      ],
      description: [
        { required: true, message: '请填写报告说明', trigger: 'blur' },
        { min: 10, message: '说明内容至少10个字符', trigger: 'blur' }
      ]
    }
    const progressMarks = {
      0: '0%',
      25: '25%',
      50: '50%',
      75: '75%',
      100: '100%'
    }

    // 文件上传请求头（携带JWT Token）
    const uploadHeaders = {
      Authorization: `Bearer ${localStorage.getItem('token') || ''}`
    }

    // 文件上传额外参数
    const uploadData = computed(() => ({
      uploaderId: JSON.parse(localStorage.getItem('userInfo') || '{}')?.userId || '',
      relationType: 'midterm_check',
      relationId: topicInfo.value?.selectionId || ''
    }))

    // 页面加载时获取数据
    onMounted(async () => {
      await fetchMyGraduationInfo()
    })

    /**
     * 【新增】学生提交答辩信息
     */
    async function submitStudentDefenseInfo() {
      // 表单验证
      if (!defenseSubmitFormRef.value) return
      
      await defenseSubmitFormRef.value.validate(async (valid) => {
        if (!valid) {
          ElMessage.warning('请检查表单填写是否完整')
          return
        }

        // 【重要】检查selectionId是否存在
        const selectionId = batchInfo.value?.selectionId || topicInfo.value?.selectionId
        if (!selectionId) {
          ElMessage.error('❌ 无法获取选题ID，请刷新页面重试')
          console.error('❌ 缺少selectionId:', {
            batchInfo: batchInfo.value,
            topicInfo: topicInfo.value
          })
          return
        }

        submittingDefense.value = true

        try {
          // 1. 如果有文件，先上传文件
          let fileId = null
          if (defenseSubmitForm.pdfFile && studentDefenseFileList.value.length > 0) {
            const uploadFormData = new FormData()
            uploadFormData.append('file', defenseSubmitForm.pdfFile)
            uploadFormData.append('uploaderId', JSON.parse(localStorage.getItem('userInfo') || '{}')?.userId || '')
            uploadFormData.append('relationType', 'student_defense')
            
            try {
              const uploadRes = await request.post('/files/upload', uploadFormData, {
                headers: { 'Content-Type': 'multipart/form-data' }
              })
              
              if (uploadRes.data && uploadRes.data.fileId) {
                fileId = uploadRes.data.fileId
                console.log('✅ 文件上传成功，fileId:', fileId)
              }
            } catch (uploadError) {
              console.warn('⚠️ 文件上传失败，但不影响提交（可后续补充）:', uploadError.message)
              // 文件上传失败不阻止提交，继续执行
            }
          }

          // 2. 调用后端API提交答辩信息
          const params = new URLSearchParams()
          params.append('selectionId', selectionId)
          params.append('defenseScore', 'pending')  // 学生自评状态（待定）
          params.append('selfEvaluation', defenseSubmitForm.selfEvaluation || '')
          params.append('defenseDatetime', defenseSubmitForm.defenseDate || '')
          
          if (fileId) {
            params.append('recordFileId', fileId.toString())
          }

          console.log('📤 提交答辩信息参数:', {
            selectionId: selectionId,
            defenseScore: 'pending',
            selfEvaluation: defenseSubmitForm.selfEvaluation?.substring(0, 50),
            defenseDatetime: defenseSubmitForm.defenseDate,
            recordFileId: fileId
          })

          const res = await request.post('/defenses/submitByStudent', params, {
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
          })

          // 3. 提交成功处理
          console.log('✅ 后端返回数据:', res.data)
          
          ElMessage.success('✅ 答辩信息提交成功！\n\n您的答辩申请已提交给指导教师审核。\n教师确认后，您将收到通知。')
          
          // 4. 关闭弹窗并刷新数据
          showDefenseSubmitDialog.value = false
          
          // 重置表单
          resetDefenseSubmitForm()
          
          // 5. 刷新毕设信息以更新答辩状态
          await fetchMyGraduationInfo()
          
        } catch (error) {
          console.error('❌ 提交答辩信息失败详情:', error)
          console.error('   - 错误类型:', error.name)
          console.error('   - 错误消息:', error.message)
          console.error('   - 响应数据:', error.response?.data)
          console.error('   - 响应状态:', error.response?.status)
          
          // 显示更详细的错误信息
          let errorMsg = '提交失败'
          if (error.response?.data?.message) {
            errorMsg += ': ' + error.response.data.message
          } else if (error.message) {
            errorMsg += ': ' + error.message
          } else {
            errorMsg += '，请稍后重试或联系管理员'
          }
          
          ElMessage.error('❌ ' + errorMsg)
        } finally {
          submittingDefense.value = false
        }
      })
    }

    /**
     * 【新增】重置学生提交表单
     */
    function resetDefenseSubmitForm() {
      defenseSubmitForm.defenseDate = ''
      defenseSubmitForm.selfEvaluation = ''
      defenseSubmitForm.pdfFile = null
      studentDefenseFileList.value = []
      
      if (defenseSubmitFormRef.value) {
        defenseSubmitFormRef.value.resetFields()
      }
    }

    /**
     * 【新增】学生答辩文件选择变化
     */
    function handleStudentDefenseFileChange(uploadFile) {
      defenseSubmitForm.pdfFile = uploadFile.raw
      studentDefenseFileList.value = [uploadFile]
    }

    /**
     * 【新增】学生答辩文件移除
     */
    function handleStudentDefenseFileRemove() {
      defenseSubmitForm.pdfFile = null
      studentDefenseFileList.value = []
    }

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
        'pending': 'warning',
        'approved': 'success',
        'rejected': 'danger'
      }
      return map[status] || 'info'
    }

    function getTaskBookStatusType(status) {
      const map = {
        'unissued': 'info',
        'issued': 'success',
        'confirmed': 'success',
        'rejected': 'danger'
      }
      return map[status] || 'info'
    }

    function getMidtermStatusType(status) {
      const map = {
        'draft': 'info',
        'submitted': 'info',
        'pending': 'warning',
        'approved': 'success',
        'rejected': 'danger'
      }
      return map[status] || 'info'
    }

    function getFinalStatusType(status) {
      const map = {
        'draft': 'info',
        'submitted': 'info',
        'pending': 'warning',
        'approved': 'success',
        'finalized': 'success',
        'rejected': 'danger'
      }
      return map[status] || 'info'
    }

    function getDefenseStatusType(status) {
      const map = {
        'not_started': 'info',
        'submitted': 'warning',
        'pending': 'warning',
        'approved': 'success',
        'rejected': 'danger'
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

    /**
     * 根据中期检查状态计算显示进度
     * 业务规则：
     * - 未提交(draft)：0%
     * - 已提交/待审核(submitted, pending)：50%
     * - 已通过(approved)：100%
     * - 已驳回(rejected)：50%（需重新修改）
     */
    function getMidtermProgress(status, progress) {
      const statusMap = {
        'draft': 0,
        'submitted': 50,
        'pending': 50,
        'approved': 100,
        'rejected': 50
      }
      
      const calculatedProgress = statusMap[status]
      if (calculatedProgress !== undefined) {
        return calculatedProgress
      }
      
      // 如果状态不在映射中，使用原始进度值（兜底）
      return progress || 0
    }

    /**
     * 根据中期检查状态获取进度条样式
     */
    function getMidtermProgressStatus(status) {
      const statusMap = {
        'draft': '',           // 灰色（未开始）
        'submitted': 'warning', // 橙色（已提交）
        'pending': 'warning',   // 橙色（待审核）
        'approved': 'success',  // 绿色（已通过）
        'rejected': 'exception' // 红色（已驳回，需重新修改）
      }
      
      return statusMap[status] || ''
    }

    /**
     * 获取进度文字颜色
     */
    function getProgressTextColor(status) {
      const colorMap = {
        'draft': '#909399',       // 灰色
        'submitted': '#E6A23C',   // 橙色
        'pending': '#E6A23C',     // 橙色
        'approved': '#67C23A',    // 绿色
        'rejected': '#F56C6C'     // 红色
      }
      
      return colorMap[status] || '#909399'
    }

    /**
     * 获取进度条填充颜色
     */
    function getProgressColor(status) {
      const colorMap = {
        'draft': '#C0C4CC',       // 浅灰色（未开始）
        'submitted': '#E6A23C',   // 橙色（已提交）
        'pending': '#E6A23C',     // 橙色（待审核）
        'approved': '#67C23A',    // 绿色（已通过）
        'rejected': '#F56C6C'     // 红色（已驳回）
      }
      
      return colorMap[status] || '#409EFF'
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
      }).then(async () => {
        try {
          if (!taskBookInfo.value.taskId) {
            ElMessage.warning('暂无任务书可确认')
            return
          }

          console.log('正在确认任务书, taskId:', taskBookInfo.value.taskId)

          const response = await request.put(`/taskbooks/${taskBookInfo.value.taskId}/confirm`)

          console.log('确认成功, 响应:', response)

          await fetchMyGraduationInfo()

          ElMessage.success('已成功确认接收任务书')
        } catch (error) {
          console.error('确认接收任务书失败 - 完整错误:', error)
          console.error('响应数据:', error.response?.data)
          console.error('响应状态:', error.response?.status)
          console.error('响应头:', error.response?.headers)

          const errorMsg = error.response?.data?.message 
                        || error.response?.data?.msg 
                        || error.message 
                        || '未知错误'
          
          ElMessage.error('确认接收失败: ' + errorMsg)
          
          alert('调试信息:\n' + 
                '状态码: ' + (error.response?.status) + '\n' +
                '错误信息: ' + JSON.stringify(error.response?.data, null, 2))
        }
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
      // 检查是否有选题信息
      if (!topicInfo.value.selectionId) {
        ElMessage.warning('您尚未选择毕业设计题目，无法提交中期检查报告')
        return
      }

      // 重置表单
      midtermForm.value = {
        progress: 40,
        description: '',
        fileId: null,
        fileName: ''
      }

      midtermDialogVisible.value = true
    }

    function resetMidtermForm() {
      midtermFormRef.value?.resetFields()
      midtermForm.value = {
        progress: 40,
        description: '',
        fileId: null,
        fileName: ''
      }
    }

    function beforeMidtermUpload(file) {
      const isValidType = ['application/pdf', 'application/msword',
                          'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
                          'application/zip', 'application/x-rar-compressed'].includes(file.type)
      const isLt50M = file.size / 1024 / 1024 < 50

      if (!isValidType) {
        ElMessage.error('只能上传 PDF/Word/压缩包文件!')
        return false
      }
      if (!isLt50M) {
        ElMessage.error('文件大小不能超过 50MB!')
        return false
      }
      return true
    }

    function handleMidtermFileSuccess(response) {
      if (response.code === 200 && response.data) {
        midtermForm.value.fileId = response.data.fileId
        midtermForm.value.fileName = response.data.originalName || '已上传文件'
        ElMessage.success('文件上传成功')
      } else {
        ElMessage.error(response.message || '文件上传失败')
      }
    }

    async function submitMidtermReport() {
      if (!midtermFormRef.value) return

      await midtermFormRef.value.validate(async (valid) => {
        if (!valid) return

        try {
          midtermSubmitting.value = true

          const params = new URLSearchParams()
          params.append('selectionId', topicInfo.value.selectionId)
          params.append('progress', midtermForm.value.progress)

          if (midtermForm.value.fileId) {
            params.append('fileId', midtermForm.value.fileId)
          }

          const res = await request.post('/midterm-checks/submit', params, {
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
          })

          if (res.code === 200 || res.data) {
            ElMessage.success('中期检查报告提交成功！')

            midtermDialogVisible.value = false

            await fetchMyGraduationInfo()
          } else {
            throw new Error(res.message || '提交失败')
          }
        } catch (error) {
          console.error('提交中期检查报告失败:', error)
          ElMessage.error('提交失败：' + (error.response?.data?.message || error.message))
        } finally {
          midtermSubmitting.value = false
        }
      })
    }

    async function downloadMidtermTemplate() {
      try {
        ElMessage.info('正在准备下载中期检查报告模板...')

        // 创建新窗口
        const printWindow = window.open('', '_blank')

        if (!printWindow) {
          ElMessage.error('无法弹出窗口，请允许弹窗后重试')
          return
        }

        // 显示加载提示
        printWindow.document.write(`
          <!DOCTYPE html>
          <html>
          <head>
            <title>正在加载模板...</title>
            <style>
              body {
                font-family: Arial, sans-serif;
                display: flex;
                justify-content: center;
                align-items: center;
                height: 100vh;
                margin: 0;
                background-color: #f5f5f5;
              }
              .loading-container {
                text-align: center;
                padding: 40px;
                background: white;
                border-radius: 8px;
                box-shadow: 0 2px 10px rgba(0,0,0,0.1);
              }
              .spinner {
                width: 50px;
                height: 50px;
                border: 5px solid #f3f3f3;
                border-top: 5px solid #409EFF;
                border-radius: 50%;
                animation: spin 1s linear infinite;
                margin: 0 auto 20px;
              }
              @keyframes spin {
                0% { transform: rotate(0deg); }
                100% { transform: rotate(360deg); }
              }
              p { color: #666; margin-top: 20px; }
            </style>
          </head>
          <body>
            <div class="loading-container">
              <div class="spinner"></div>
              <p>正在加载中期检查报告模板...</p>
              <p style="font-size: 12px; color: #999;">加载完成后将自动打印</p>
            </div>
          </body>
          </html>
        `)

        // 获取HTML模板
        const response = await fetch('/templates/midterm-check-report-template.html')
        const htmlContent = await response.text()

        // 写入新窗口
        printWindow.document.open()
        printWindow.document.write(htmlContent)
        printWindow.document.close()

        // 等待页面加载完成后自动打印
        printWindow.onload = function() {
          setTimeout(() => {
            printWindow.print()
            ElMessage.success('已打开打印对话框，请选择"另存为PDF"保存')
          }, 500)
        }

      } catch (error) {
        console.error('下载模板失败:', error)
        ElMessage.error('下载失败：' + error.message)
      }
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

    .progress-text {
      font-size: 14px;
      font-weight: 600;
      min-width: 40px;
      display: inline-block;
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
