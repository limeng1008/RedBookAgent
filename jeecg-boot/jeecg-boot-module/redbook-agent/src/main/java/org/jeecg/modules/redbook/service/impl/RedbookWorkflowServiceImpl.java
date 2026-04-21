package org.jeecg.modules.redbook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jeecg.modules.redbook.constant.RedbookPromptTemplateConstant;
import org.jeecg.modules.redbook.constant.RedbookStatusConstant;
import org.jeecg.modules.redbook.entity.RbAccount;
import org.jeecg.modules.redbook.entity.RbHotspot;
import org.jeecg.modules.redbook.entity.RbHotspotAnalysis;
import org.jeecg.modules.redbook.entity.RbNoteDraft;
import org.jeecg.modules.redbook.entity.RbNoteMetric;
import org.jeecg.modules.redbook.entity.RbPublishPlan;
import org.jeecg.modules.redbook.entity.RbReviewReport;
import org.jeecg.modules.redbook.entity.RbSensitiveWord;
import org.jeecg.modules.redbook.entity.RbTrack;
import org.jeecg.modules.redbook.model.ai.RedbookAiExecutionResult;
import org.jeecg.modules.redbook.service.IRbAccountService;
import org.jeecg.modules.redbook.service.IRbHotspotAnalysisService;
import org.jeecg.modules.redbook.service.IRbHotspotService;
import org.jeecg.modules.redbook.service.IRbNoteDraftService;
import org.jeecg.modules.redbook.service.IRbNoteMetricService;
import org.jeecg.modules.redbook.service.IRbPublishPlanService;
import org.jeecg.modules.redbook.service.IRbReviewReportService;
import org.jeecg.modules.redbook.service.IRbSensitiveWordService;
import org.jeecg.modules.redbook.service.IRbTrackService;
import org.jeecg.modules.redbook.service.IRedbookAiService;
import org.jeecg.modules.redbook.service.IRedbookWorkflowService;
import org.jeecg.modules.redbook.vo.RedbookReviewDashboardVO;
import org.jeecg.modules.redbook.vo.RedbookReviewDimensionVO;
import org.jeecg.modules.redbook.vo.RedbookReviewRankItemVO;
import org.jeecg.modules.redbook.vo.RedbookDraftRiskCheckVO;
import org.jeecg.modules.redbook.vo.RedbookDraftRiskHitVO;
import org.jeecg.modules.redbook.vo.RedbookWorkbenchOverviewVO;
import org.jeecg.modules.redbook.vo.RedbookWorkbenchTodoVO;
import org.jeecg.modules.redbook.vo.RedbookWorkbenchTrackVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class RedbookWorkflowServiceImpl implements IRedbookWorkflowService {
    private static final Pattern SPLIT_PATTERN = Pattern.compile("[,，、#\\s|/]+");
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Asia/Shanghai");
    private static final List<String> METRIC_COLLECT_NODES = Arrays.asList("2h", "24h", "72h", "7d");

    @Resource
    private IRbHotspotService hotspotService;

    @Resource
    private IRbHotspotAnalysisService hotspotAnalysisService;

    @Resource
    private IRbNoteDraftService noteDraftService;

    @Resource
    private IRbPublishPlanService publishPlanService;

    @Resource
    private IRbNoteMetricService noteMetricService;

    @Resource
    private IRbReviewReportService reviewReportService;

    @Resource
    private IRbTrackService trackService;

    @Resource
    private IRbAccountService accountService;

    @Resource
    private IRbSensitiveWordService sensitiveWordService;

    @Resource
    private IRedbookAiService redbookAiService;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RbHotspotAnalysis analyzeHotspot(String hotspotId) {
        RbHotspot hotspot = hotspotService.getById(hotspotId);
        if (hotspot == null) {
            throw new IllegalArgumentException("未找到对应热点");
        }

        RbTrack track = getTrack(hotspot.getTrackId());
        List<String> keywords = buildKeywords(hotspot.getTags(), hotspot.getTitle(), hotspot.getSummary(), track == null ? null : track.getKeywords());
        List<String> sensitiveHits = findSensitiveHits(hotspot.getTitle(), hotspot.getSummary());
        BigDecimal score = buildAnalysisScore(hotspot, sensitiveHits);
        String trackName = track == null ? "当前赛道" : defaultText(track.getTrackName(), "当前赛道");
        String summary = defaultText(hotspot.getSummary(), "这条热点在目标用户中引起了明显讨论，适合转成运营选题。");
        String keywordText = keywords.isEmpty() ? "真实经验、场景问题、执行步骤" : String.join(" / ", keywords);
        String riskWarning = buildRiskWarning(hotspot, sensitiveHits);
        Map<String, Object> analysisContext = buildHotspotAnalysisContext(hotspot, track, keywords, sensitiveHits, score);
        RedbookAiExecutionResult aiResult = redbookAiService.runWorkflow(RedbookPromptTemplateConstant.HOTSPOT_ANALYSIS, analysisContext);
        Map<String, Object> aiOutputs = aiResult.getOutputs();

        RbHotspotAnalysis analysis = hotspotAnalysisService.lambdaQuery()
            .eq(RbHotspotAnalysis::getHotspotId, hotspotId)
            .orderByDesc(RbHotspotAnalysis::getUpdateTime)
            .last("limit 1")
            .one();
        if (analysis == null) {
            analysis = new RbHotspotAnalysis();
            analysis.setHotspotId(hotspotId);
        }

        String fallbackPainPoints = trackName + "用户最关心的是“有没有更低成本、更可复制的做法”。结合热点内容，可以围绕" + keywordText + "来拆解真实痛点。";
        String fallbackHookAnalysis = "热点标题“" + defaultText(hotspot.getTitle(), "未命名热点") + "”本身具备强钩子，且互动信号为点赞 "
            + defaultLong(hotspot.getLikeCount()) + "、收藏 " + defaultLong(hotspot.getCollectCount()) + "、评论 "
            + defaultLong(hotspot.getCommentCount()) + "、分享 " + defaultLong(hotspot.getShareCount()) + "。适合用结论前置 + 细节补证的结构承接。";
        String fallbackContentAngle = "从“" + trackName + "真实场景复盘”切入，先交付结论，再补充做法、避坑点和执行门槛，避免仅复述原热点。";
        String fallbackTitleDirections = "1. 这个" + trackName + "选题为什么突然火了\n2. 如果你也在做" + trackName + "，这 3 个细节一定要补上\n3. 同样的热点，换这个角度更容易出爆文";
        String fallbackOutline = "开头先给结论 -> 说明热点为什么火 -> 拆 3 个可复用做法 -> 给出执行提醒 -> 结尾加评论区引导。原热点摘要：" + summary;
        String fallbackCoverCopy = "别再只抄标题了\n这个角度更容易出单";
        String fallbackTags = buildTagSuggestion(trackName, keywords);
        String fallbackProductFit = "优先植入“真实体验、步骤清单、结果对比”型卖点，不做绝对化承诺。";
        String fallbackOriginality = "保留热点问题意识，但换成账号自己的案例、表达顺序和结论框架。至少新增 1 段真实经验或 1 个反常识提醒。";

        analysis.setPainPoints(firstNonBlank(textValue(aiOutputs, "pain_points", "painPoints"), fallbackPainPoints));
        analysis.setHookAnalysis(firstNonBlank(textValue(aiOutputs, "hook_analysis", "hookAnalysis", "result"), fallbackHookAnalysis));
        analysis.setContentAngle(firstNonBlank(textValue(aiOutputs, "content_angle", "contentAngle"), fallbackContentAngle));
        analysis.setTitleDirections(firstNonBlank(textValue(aiOutputs, "title_directions", "titleDirections"), fallbackTitleDirections));
        analysis.setOutlineSuggestion(firstNonBlank(textValue(aiOutputs, "outline_suggestion", "outlineSuggestion"), fallbackOutline));
        analysis.setCoverCopySuggestion(firstNonBlank(textValue(aiOutputs, "cover_copy_suggestion", "coverCopySuggestion"), fallbackCoverCopy));
        analysis.setTagSuggestion(firstNonBlank(textValueWithDelimiter(aiOutputs, " ", "tag_suggestion", "tagSuggestion", "tags"), fallbackTags));
        analysis.setProductFit(firstNonBlank(textValue(aiOutputs, "product_fit", "productFit"), fallbackProductFit));
        analysis.setRiskWarning(firstNonBlank(textValue(aiOutputs, "risk_warning", "riskWarning"), riskWarning));
        analysis.setOriginalitySuggestion(firstNonBlank(textValue(aiOutputs, "originality_suggestion", "originalitySuggestion"), fallbackOriginality));
        analysis.setScore(numberValue(aiOutputs, score, "score", "综合评分"));
        analysis.setRawResult(buildWorkflowRawResult(aiResult, analysisContext, Map.of(
            "track", trackName,
            "keywords", keywordText,
            "sensitive_hits", sensitiveHits
        )));
        analysis.setStatus(RedbookStatusConstant.ANALYSIS_ANALYZED);
        hotspotAnalysisService.saveOrUpdate(analysis);

        hotspot.setStatus(RedbookStatusConstant.HOTSPOT_ANALYZED);
        hotspotService.updateById(hotspot);
        return analysis;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RbNoteDraft generateDraftByAnalysis(String analysisId) {
        RbHotspotAnalysis analysis = hotspotAnalysisService.getById(analysisId);
        if (analysis == null) {
            throw new IllegalArgumentException("未找到对应分析记录");
        }

        RbHotspot hotspot = hotspotService.getById(analysis.getHotspotId());
        if (hotspot == null) {
            throw new IllegalArgumentException("分析对应的热点不存在");
        }

        RbTrack track = getTrack(hotspot.getTrackId());
        RbAccount account = pickAccount(hotspot.getTrackId());
        String trackName = track == null ? "当前赛道" : defaultText(track.getTrackName(), "当前赛道");
        String title = buildDraftTitle(hotspot, trackName);
        String body = buildDraftBody(hotspot, analysis, trackName, account);
        String publishSuggestion = buildPublishSuggestion(hotspot);
        Map<String, Object> draftContext = buildDraftContext(hotspot, analysis, track, account, title, body, publishSuggestion);
        RedbookAiExecutionResult aiResult = redbookAiService.runWorkflow(RedbookPromptTemplateConstant.NOTE_DRAFT, draftContext);
        Map<String, Object> aiOutputs = aiResult.getOutputs();

        RbNoteDraft draft = noteDraftService.lambdaQuery()
            .eq(RbNoteDraft::getAnalysisId, analysisId)
            .orderByDesc(RbNoteDraft::getUpdateTime)
            .last("limit 1")
            .one();
        if (draft == null) {
            draft = new RbNoteDraft();
            draft.setAnalysisId(analysisId);
            draft.setHotspotId(hotspot.getId());
        }

        draft.setTrackId(hotspot.getTrackId());
        draft.setAccountId(account == null ? null : account.getId());
        draft.setTitle(firstNonBlank(textValue(aiOutputs, "title", "titles"), title));
        draft.setCoverCopy(firstNonBlank(textValue(aiOutputs, "cover_copy", "cover_copies", "coverCopy"), analysis.getCoverCopySuggestion(), "先把热点讲明白，再谈转化"));
        draft.setBody(firstNonBlank(textValue(aiOutputs, "body", "content"), body));
        draft.setTags(firstNonBlank(textValueWithDelimiter(aiOutputs, " ", "tags", "tag_suggestion", "tagSuggestion"), analysis.getTagSuggestion(), buildTagSuggestion(trackName, buildKeywords(hotspot.getTags(), hotspot.getTitle(), hotspot.getSummary(), track == null ? null : track.getKeywords()))));
        draft.setCommentGuide(firstNonBlank(textValue(aiOutputs, "comment_guide", "commentGuide"), "如果你也在做" + trackName + "，评论区回复“选题”，我把这套拆解框架继续展开。"));
        draft.setPublishTimeSuggestion(firstNonBlank(textValue(aiOutputs, "publish_time_suggestion", "publishTimeSuggestion"), publishSuggestion));
        draft.setContentType(firstNonBlank(textValue(aiOutputs, "content_type", "contentType"), buildContentType(hotspot)));
        draft.setAiVersion(aiResult.isSuccess() ? aiResult.getProvider() + "-workflow" : "template-v1");
        draft.setManualVersion("v0");
        draft.setRiskCheckResult(firstNonBlank(textValue(aiOutputs, "risk_check_result", "riskCheckResult"), analysis.getRiskWarning(), "未命中明显敏感词，建议发布前再做一次人工校对。"));
        draft.setAuditStatus(RedbookStatusConstant.AUDIT_PENDING);
        draft.setAuditOpinion("待人工审核，重点确认标题力度与结尾转化动作。");
        draft.setStatus(RedbookStatusConstant.DRAFT_PENDING_REVIEW);
        draft = noteDraftService.saveOrUpdateDraft(draft, "ai_generate", "AI 生成草稿");

        analysis.setStatus(RedbookStatusConstant.ANALYSIS_ADOPTED);
        hotspotAnalysisService.updateById(analysis);
        hotspot.setStatus(RedbookStatusConstant.HOTSPOT_DRAFT_GENERATED);
        hotspotService.updateById(hotspot);
        return draft;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RedbookDraftRiskCheckVO checkDraftRisk(String draftId) {
        if (isBlank(draftId)) {
            throw new IllegalArgumentException("草稿ID不能为空");
        }
        RbNoteDraft draft = noteDraftService.getById(draftId);
        if (draft == null) {
            throw new IllegalArgumentException("未找到对应草稿");
        }

        Map<String, String> riskFields = new LinkedHashMap<>();
        riskFields.put("标题", draft.getTitle());
        riskFields.put("封面文案", draft.getCoverCopy());
        riskFields.put("正文", draft.getBody());
        riskFields.put("标签", draft.getTags());
        riskFields.put("评论引导", draft.getCommentGuide());

        List<RedbookDraftRiskHitVO> hits = listActiveSensitiveWords().stream()
            .map(word -> buildDraftRiskHit(word, riskFields))
            .filter(Objects::nonNull)
            .sorted(Comparator.comparingInt((RedbookDraftRiskHitVO item) -> riskLevelWeight(item.getRiskLevel())).reversed()
                .thenComparing(RedbookDraftRiskHitVO::getWord, Comparator.nullsLast(String::compareTo)))
            .collect(Collectors.toList());

        List<String> matchedFields = hits.stream()
            .flatMap(item -> item.getMatchedFields().stream())
            .distinct()
            .collect(Collectors.toList());
        List<String> replacementSuggestions = hits.stream()
            .map(RedbookDraftRiskHitVO::getReplacementSuggestion)
            .filter(item -> !isBlank(item))
            .distinct()
            .limit(5)
            .collect(Collectors.toList());
        String riskLevel = resolveDraftRiskLevel(hits);
        String summary = buildDraftRiskSummary(hits, matchedFields, replacementSuggestions, riskLevel);

        draft.setRiskCheckResult(summary);
        noteDraftService.updateById(draft);

        RedbookDraftRiskCheckVO result = new RedbookDraftRiskCheckVO();
        result.setDraftId(draft.getId());
        result.setTitle(defaultText(draft.getTitle(), "未命名草稿"));
        result.setPassed(hits.isEmpty());
        result.setRequiresManualReview(!hits.isEmpty());
        result.setRiskLevel(riskLevel);
        result.setHitCount((long) hits.size());
        result.setMatchedFields(matchedFields);
        result.setReplacementSuggestions(replacementSuggestions);
        result.setSummary(summary);
        result.setCheckedTime(new Date());
        result.setHits(hits);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RbPublishPlan createPublishPlan(String draftId) {
        RbNoteDraft draft = noteDraftService.getById(draftId);
        if (draft == null) {
            throw new IllegalArgumentException("未找到对应草稿");
        }
        if (!RedbookStatusConstant.AUDIT_APPROVED.equals(draft.getAuditStatus())
            || !RedbookStatusConstant.DRAFT_PENDING_PUBLISH.equals(draft.getStatus())) {
            throw new IllegalArgumentException("草稿需先审核通过后才能加入发布计划");
        }

        RbPublishPlan publishPlan = publishPlanService.lambdaQuery()
            .eq(RbPublishPlan::getDraftId, draftId)
            .orderByDesc(RbPublishPlan::getUpdateTime)
            .last("limit 1")
            .one();
        if (publishPlan == null) {
            publishPlan = new RbPublishPlan();
            publishPlan.setDraftId(draftId);
        }

        publishPlan.setAccountId(draft.getAccountId());
        publishPlan.setPlannedPublishTime(nextPublishTime());
        publishPlan.setPublishStatus(RedbookStatusConstant.PUBLISH_PENDING);
        publishPlan.setRemark("由系统根据草稿状态自动加入排期，建议在发布前完成终审与素材核对。");
        publishPlanService.saveOrUpdate(publishPlan);

        draft.setAuditStatus(RedbookStatusConstant.AUDIT_APPROVED);
        if (isBlank(draft.getAuditOpinion())) {
            draft.setAuditOpinion("已加入发布计划");
        }
        draft.setStatus(RedbookStatusConstant.DRAFT_PENDING_PUBLISH);
        noteDraftService.updateById(draft);
        return publishPlan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RbPublishPlan markPublished(String publishPlanId) {
        RbPublishPlan publishPlan = publishPlanService.getById(publishPlanId);
        if (publishPlan == null) {
            throw new IllegalArgumentException("未找到对应发布计划");
        }

        if (publishPlan.getActualPublishTime() == null) {
            publishPlan.setActualPublishTime(new Date());
        }
        publishPlan.setPublishStatus(RedbookStatusConstant.PUBLISH_PUBLISHED);
        publishPlanService.updateById(publishPlan);

        if (!isBlank(publishPlan.getDraftId())) {
            RbNoteDraft draft = noteDraftService.getById(publishPlan.getDraftId());
            if (draft != null && !RedbookStatusConstant.DRAFT_PUBLISHED.equals(draft.getStatus())) {
                draft.setStatus(RedbookStatusConstant.DRAFT_PUBLISHED);
                draft.setAuditStatus(RedbookStatusConstant.AUDIT_APPROVED);
                noteDraftService.saveOrUpdateDraft(draft, "published", "发布计划标记已发布");
            }
        }
        return publishPlan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RbNoteMetric createMetricRecord(String publishPlanId) {
        RbPublishPlan publishPlan = publishPlanService.getById(publishPlanId);
        if (publishPlan == null) {
            throw new IllegalArgumentException("未找到对应发布计划");
        }
        if (!RedbookStatusConstant.PUBLISH_PUBLISHED.equals(publishPlan.getPublishStatus())
            && !RedbookStatusConstant.PUBLISH_DATA_COLLECTED.equals(publishPlan.getPublishStatus())) {
            throw new IllegalArgumentException("请先将发布计划标记为已发布，再创建数据回收记录");
        }

        List<RbNoteMetric> existingMetrics = noteMetricService.lambdaQuery()
            .eq(RbNoteMetric::getPublishPlanId, publishPlanId)
            .list();
        String nextCollectNode = nextMetricCollectNode(existingMetrics);
        if (isBlank(nextCollectNode)) {
            throw new IllegalArgumentException("2h / 24h / 72h / 7d 四个关键数据节点已全部生成，请直接编辑现有记录");
        }
        RbNoteMetric metric = new RbNoteMetric();
        metric.setPublishPlanId(publishPlanId);
        metric.setNoteDraftId(publishPlan.getDraftId());
        metric.setCollectNode(nextCollectNode);
        metric.setImpressions(0L);
        metric.setViews(0L);
        metric.setLikes(0L);
        metric.setCollects(0L);
        metric.setComments(0L);
        metric.setShares(0L);
        metric.setFollowers(0L);
        metric.setMessages(0L);
        metric.setLeads(0L);
        metric.setConversions(0L);
        metric.setCollectTime(new Date());
        metric.setRemark("由发布计划生成，请录入当前节点的小红书后台数据。");
        noteMetricService.save(noteMetricService.normalizeMetric(metric));
        noteMetricService.refreshPublishPlanStatus(publishPlanId);
        return metric;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RbReviewReport generateReviewReport(String reviewReportId) {
        RbReviewReport report = reviewReportService.getById(reviewReportId);
        if (report == null) {
            throw new IllegalArgumentException("未找到对应复盘报告");
        }

        Date periodEnd = endOfDay(report.getPeriodEnd() == null ? new Date() : report.getPeriodEnd());
        Date periodStart = startOfDay(report.getPeriodStart() == null ? daysBefore(periodEnd, 6) : report.getPeriodStart());
        report.setPeriodStart(periodStart);
        report.setPeriodEnd(periodEnd);

        RbTrack track = getTrack(report.getTrackId());
        RbAccount account = isBlank(report.getAccountId()) ? null : accountService.getById(report.getAccountId());
        String trackName = track == null ? "全部赛道" : defaultText(track.getTrackName(), "全部赛道");
        String accountName = account == null ? "全部账号" : defaultText(account.getAccountName(), "全部账号");

        Map<String, RbNoteDraft> draftMap = noteDraftService.list().stream()
            .filter(item -> !isBlank(item.getId()))
            .collect(Collectors.toMap(RbNoteDraft::getId, Function.identity(), (left, right) -> left));

        List<RbPublishPlan> publishedPlans = publishPlanService.list().stream()
            .filter(item -> isPublishedStatus(item.getPublishStatus()))
            .filter(item -> isWithinPeriod(resolvePublishTime(item), periodStart, periodEnd))
            .filter(item -> matchReportScope(item, draftMap.get(item.getDraftId()), report))
            .sorted(Comparator.comparing(this::resolvePublishTime, Comparator.nullsLast(Date::compareTo)).reversed())
            .collect(Collectors.toList());

        List<String> publishPlanIds = publishedPlans.stream()
            .map(RbPublishPlan::getId)
            .filter(id -> !isBlank(id))
            .collect(Collectors.toList());

        Map<String, List<RbNoteMetric>> metricGroup = noteMetricService.list().stream()
            .filter(item -> !isBlank(item.getPublishPlanId()) && publishPlanIds.contains(item.getPublishPlanId()))
            .collect(Collectors.groupingBy(RbNoteMetric::getPublishPlanId));

        Map<String, RbNoteMetric> latestMetricMap = new LinkedHashMap<>();
        metricGroup.forEach((publishPlanId, metrics) -> latestMetricMap.put(publishPlanId, pickLatestMetric(metrics)));

        List<RbPublishPlan> highPerformPlans = publishedPlans.stream()
            .sorted(Comparator.<RbPublishPlan, BigDecimal>comparing(plan -> buildReviewWeight(latestMetricMap.get(plan.getId())), Comparator.nullsLast(BigDecimal::compareTo)).reversed())
            .limit(3)
            .collect(Collectors.toList());
        List<RbPublishPlan> lowPerformPlans = publishedPlans.stream()
            .sorted(Comparator.<RbPublishPlan, BigDecimal>comparing(plan -> buildReviewWeight(latestMetricMap.get(plan.getId())), Comparator.nullsLast(BigDecimal::compareTo)))
            .limit(Math.min(3, publishedPlans.size()))
            .collect(Collectors.toList());

        BigDecimal avgViews = averageLongValues(latestMetricMap.values().stream().map(RbNoteMetric::getViews).collect(Collectors.toList()), 1);
        BigDecimal avgInteractionRate = average(latestMetricMap.values().stream().map(RbNoteMetric::getInteractionRate).collect(Collectors.toList()), 4);
        Map<String, Object> reviewContext = buildReviewContext(report, track, account, publishedPlans, latestMetricMap, draftMap, avgViews, avgInteractionRate);
        RedbookAiExecutionResult aiResult = redbookAiService.runWorkflow(RedbookPromptTemplateConstant.REVIEW_REPORT, reviewContext);
        Map<String, Object> aiOutputs = aiResult.getOutputs();

        String defaultReportName = trackName + "复盘报告（" + formatDate(periodStart) + " - " + formatDate(periodEnd) + "）";
        String fallbackSummary = buildReviewSummary(trackName, accountName, publishedPlans.size(), latestMetricMap.size(), avgViews, avgInteractionRate);
        String fallbackHighFactors = buildHighPerformingFactors(trackName, highPerformPlans, draftMap, latestMetricMap);
        String fallbackLowReasons = buildLowPerformingReasons(lowPerformPlans, draftMap, latestMetricMap);
        String fallbackReusableTopics = buildReusableTopics(highPerformPlans, draftMap);
        String fallbackStoppedDirections = buildStoppedDirections(lowPerformPlans, draftMap);
        String fallbackNextTopics = buildNextTopicSuggestions(trackName, highPerformPlans, draftMap);
        String fallbackNextTitles = buildNextTitleSuggestions(trackName, highPerformPlans, draftMap);
        String fallbackNextPublish = buildNextPublishSuggestions(highPerformPlans, latestMetricMap);

        report.setReportName(firstNonBlank(report.getReportName(), defaultReportName));
        report.setSummary(firstNonBlank(textValue(aiOutputs, "summary"), fallbackSummary));
        report.setHighPerformingFactors(firstNonBlank(textValue(aiOutputs, "high_performing_factors", "highPerformingFactors"), fallbackHighFactors));
        report.setLowPerformingReasons(firstNonBlank(textValue(aiOutputs, "low_performing_reasons", "lowPerformingReasons"), fallbackLowReasons));
        report.setReusableTopics(firstNonBlank(textValue(aiOutputs, "reusable_topics", "reusableTopics"), fallbackReusableTopics));
        report.setStoppedDirections(firstNonBlank(textValue(aiOutputs, "stopped_directions", "stoppedDirections"), fallbackStoppedDirections));
        report.setNextTopicSuggestions(firstNonBlank(textValue(aiOutputs, "next_topic_suggestions", "nextTopicSuggestions"), fallbackNextTopics));
        report.setNextTitleSuggestions(firstNonBlank(textValue(aiOutputs, "next_title_suggestions", "nextTitleSuggestions"), fallbackNextTitles));
        report.setNextPublishSuggestions(firstNonBlank(textValue(aiOutputs, "next_publish_suggestions", "nextPublishSuggestions"), fallbackNextPublish));
        report.setRawResult(buildWorkflowRawResult(aiResult, reviewContext, Map.of(
            "published_count", publishedPlans.size(),
            "collected_count", latestMetricMap.size(),
            "avg_views", avgViews,
            "avg_interaction_rate", avgInteractionRate
        )));
        report.setStatus(RedbookStatusConstant.REVIEW_GENERATED);
        reviewReportService.updateById(report);
        return report;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RbReviewReport generateReviewReportByScope(RbReviewReport report) {
        if (report == null) {
            throw new IllegalArgumentException("复盘报告请求不能为空");
        }
        RbReviewReport target = report;
        if (!isBlank(report.getId())) {
            RbReviewReport existing = reviewReportService.getById(report.getId());
            if (existing == null) {
                throw new IllegalArgumentException("未找到对应复盘报告");
            }
            existing.setReportName(firstNonBlank(report.getReportName(), existing.getReportName()));
            existing.setTrackId(firstNonBlank(report.getTrackId(), existing.getTrackId()));
            existing.setAccountId(firstNonBlank(report.getAccountId(), existing.getAccountId()));
            existing.setPeriodStart(firstNonNull(report.getPeriodStart(), existing.getPeriodStart()));
            existing.setPeriodEnd(firstNonNull(report.getPeriodEnd(), existing.getPeriodEnd()));
            existing.setStatus(RedbookStatusConstant.REVIEW_DRAFT);
            target = existing;
            reviewReportService.updateById(target);
        } else {
            target.setStatus(RedbookStatusConstant.REVIEW_DRAFT);
            reviewReportService.save(target);
        }
        if (!isBlank(target.getTrackId()) && trackService.getById(target.getTrackId()) == null) {
            throw new IllegalArgumentException("赛道ID不存在");
        }
        if (!isBlank(target.getAccountId()) && accountService.getById(target.getAccountId()) == null) {
            throw new IllegalArgumentException("账号ID不存在");
        }
        return generateReviewReport(target.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<RbHotspot> createHotspotsFromReviewReport(String reviewReportId) {
        RbReviewReport report = reviewReportService.getById(reviewReportId);
        if (report == null) {
            throw new IllegalArgumentException("未找到对应复盘报告");
        }

        List<String> suggestions = splitSuggestionLines(report.getNextTopicSuggestions(), report.getNextTitleSuggestions()).stream()
            .map(this::cleanSuggestionTitle)
            .filter(item -> !isBlank(item))
            .distinct()
            .limit(8)
            .collect(Collectors.toList());
        List<RbHotspot> createdHotspots = new ArrayList<>();
        for (String suggestion : suggestions) {
            boolean exists = hotspotService.lambdaQuery()
                .eq(RbHotspot::getTitle, suggestion)
                .last("limit 1")
                .one() != null;
            if (exists) {
                continue;
            }
            RbHotspot hotspot = new RbHotspot();
            hotspot.setTrackId(report.getTrackId());
            hotspot.setSourcePlatform("复盘建议");
            hotspot.setTitle(suggestion);
            hotspot.setSummary("由复盘报告《" + defaultText(report.getReportName(), "未命名复盘") + "》回流生成，建议进入热点分析后再二创。");
            hotspot.setAuthorName("复盘报告");
            hotspot.setLikeCount(0L);
            hotspot.setCollectCount(0L);
            hotspot.setCommentCount(0L);
            hotspot.setShareCount(0L);
            hotspot.setCollectTime(new Date());
            hotspot.setTags("#复盘建议 #下一轮选题");
            hotspot.setHeatScore(BigDecimal.valueOf(70));
            hotspot.setRemixScore(BigDecimal.valueOf(88));
            hotspot.setRiskLevel("low");
            hotspot.setStatus(RedbookStatusConstant.HOTSPOT_PENDING_ANALYSIS);
            createdHotspots.add(hotspot);
        }
        if (!createdHotspots.isEmpty()) {
            hotspotService.saveBatch(createdHotspots);
        }
        return createdHotspots;
    }

    @Override
    public RedbookReviewDashboardVO getReviewDashboard() {
        return getReviewDashboard(null, null, null, null);
    }

    @Override
    public RedbookReviewDashboardVO getReviewDashboard(Date periodStart, Date periodEnd, String trackId, String accountId) {
        Date scopeStart = periodStart == null ? null : startOfDay(periodStart);
        Date scopeEnd = periodEnd == null ? null : endOfDay(periodEnd);
        Map<String, RbNoteDraft> draftMap = noteDraftService.list().stream()
            .filter(item -> !isBlank(item.getId()))
            .collect(Collectors.toMap(RbNoteDraft::getId, Function.identity(), (left, right) -> left));
        Map<String, RbTrack> trackMap = trackService.list().stream()
            .filter(item -> !isBlank(item.getId()))
            .collect(Collectors.toMap(RbTrack::getId, Function.identity(), (left, right) -> left));
        Map<String, RbAccount> accountMap = accountService.list().stream()
            .filter(item -> !isBlank(item.getId()))
            .collect(Collectors.toMap(RbAccount::getId, Function.identity(), (left, right) -> left));

        List<RbPublishPlan> publishedPlans = publishPlanService.list().stream()
            .filter(item -> isPublishedStatus(item.getPublishStatus()))
            .filter(item -> matchDashboardScope(item, draftMap.get(item.getDraftId()), scopeStart, scopeEnd, trackId, accountId))
            .collect(Collectors.toList());
        LinkedHashSet<String> publishPlanIds = publishedPlans.stream()
            .map(RbPublishPlan::getId)
            .filter(item -> !isBlank(item))
            .collect(Collectors.toCollection(LinkedHashSet::new));
        List<RbNoteMetric> metrics = noteMetricService.list().stream()
            .filter(item -> !isBlank(item.getPublishPlanId()) && publishPlanIds.contains(item.getPublishPlanId()))
            .collect(Collectors.toList());

        Map<String, List<RbNoteMetric>> metricGroup = metrics.stream()
            .collect(Collectors.groupingBy(RbNoteMetric::getPublishPlanId));
        Map<String, RbNoteMetric> latestMetricMap = new LinkedHashMap<>();
        metricGroup.forEach((publishPlanId, values) -> latestMetricMap.put(publishPlanId, pickLatestMetric(values)));
        List<RbNoteMetric> latestMetrics = latestMetricMap.values().stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        List<RedbookReviewRankItemVO> rankedItems = publishedPlans.stream()
            .filter(plan -> latestMetricMap.get(plan.getId()) != null)
            .map(plan -> toReviewRankItem(plan, latestMetricMap.get(plan.getId()), draftMap, trackMap, accountMap))
            .collect(Collectors.toList());

        List<RbReviewReport> matchedReports = reviewReportService.list().stream()
            .filter(report -> matchDashboardReportScope(report, scopeStart, scopeEnd, trackId, accountId))
            .collect(Collectors.toList());
        RbReviewReport latestReport = matchedReports.stream()
            .max(Comparator.comparing(report -> firstNonNull(report.getUpdateTime(), report.getCreateTime()), Comparator.nullsLast(Date::compareTo)))
            .orElse(null);
        RedbookReviewDashboardVO dashboard = new RedbookReviewDashboardVO();
        dashboard.setPublishCount((long) publishedPlans.size());
        dashboard.setCollectedPublishCount((long) latestMetricMap.size());
        dashboard.setUncollectedPublishCount(Math.max(0L, publishedPlans.size() - latestMetricMap.size()));
        dashboard.setMetricCount((long) metrics.size());
        dashboard.setReviewReportCount((long) matchedReports.size());
        dashboard.setAvgViews(averageLongValues(latestMetrics.stream().map(RbNoteMetric::getViews).collect(Collectors.toList()), 1));
        dashboard.setAvgInteractionRate(average(latestMetrics.stream().map(RbNoteMetric::getInteractionRate).collect(Collectors.toList()), 4));
        dashboard.setAvgCollectRate(average(latestMetrics.stream().map(RbNoteMetric::getCollectRate).collect(Collectors.toList()), 4));
        dashboard.setAvgCommentRate(average(latestMetrics.stream().map(RbNoteMetric::getCommentRate).collect(Collectors.toList()), 4));
        dashboard.setLatestReportName(latestReport == null ? "" : defaultText(latestReport.getReportName(), ""));
        dashboard.setLatestSummary(latestReport == null ? "" : defaultText(latestReport.getSummary(), ""));
        dashboard.setHighPerformList(rankedItems.stream()
            .sorted(Comparator.comparing(RedbookReviewRankItemVO::getScore, Comparator.nullsLast(BigDecimal::compareTo)).reversed())
            .limit(8)
            .collect(Collectors.toList()));
        dashboard.setLowPerformList(rankedItems.stream()
            .sorted(Comparator.comparing(RedbookReviewRankItemVO::getScore, Comparator.nullsLast(BigDecimal::compareTo)))
            .limit(8)
            .collect(Collectors.toList()));
        dashboard.setTrackBoard(buildReviewDimensionBoard(
            publishedPlans,
            latestMetricMap,
            plan -> {
                RbNoteDraft draft = draftMap.get(plan.getDraftId());
                return draft == null ? "" : draft.getTrackId();
            },
            id -> Optional.ofNullable(trackMap.get(id)).map(RbTrack::getTrackName).orElse("未绑定赛道")
        ));
        dashboard.setAccountBoard(buildReviewDimensionBoard(
            publishedPlans,
            latestMetricMap,
            plan -> firstNonBlank(plan.getAccountId(), Optional.ofNullable(draftMap.get(plan.getDraftId())).map(RbNoteDraft::getAccountId).orElse("")),
            id -> Optional.ofNullable(accountMap.get(id)).map(RbAccount::getAccountName).orElse("未绑定账号")
        ));
        dashboard.setPublishWindowBoard(buildReviewDimensionBoard(
            publishedPlans,
            latestMetricMap,
            plan -> resolvePublishWindow(resolvePublishTime(plan)),
            id -> id
        ));
        dashboard.setNextTopicSuggestions(latestReport == null ? buildFallbackTopicSuggestionList() : splitSuggestionLines(latestReport.getNextTopicSuggestions()));
        dashboard.setNextTitleSuggestions(latestReport == null ? buildFallbackTitleSuggestionList() : splitSuggestionLines(latestReport.getNextTitleSuggestions()));
        dashboard.setNextPublishSuggestions(latestReport == null ? buildFallbackPublishSuggestionList() : splitSuggestionLines(latestReport.getNextPublishSuggestions()));
        return dashboard;
    }

    @Override
    public RedbookWorkbenchOverviewVO getWorkbenchOverview() {
        RedbookWorkbenchOverviewVO overview = new RedbookWorkbenchOverviewVO();
        overview.setTrackCount(trackService.count());
        overview.setHotspotCount(hotspotService.count());
        overview.setAnalysisCount(hotspotAnalysisService.count());
        overview.setDraftCount(noteDraftService.count());
        overview.setPublishPlanCount(publishPlanService.count());
        overview.setMetricCount(noteMetricService.count());
        overview.setReviewReportCount(reviewReportService.count());
        overview.setPendingHotspotCount(hotspotService.lambdaQuery().eq(RbHotspot::getStatus, RedbookStatusConstant.HOTSPOT_PENDING_ANALYSIS).count());
        overview.setPendingReviewCount(noteDraftService.lambdaQuery().eq(RbNoteDraft::getStatus, RedbookStatusConstant.DRAFT_PENDING_REVIEW).count());
        overview.setPendingPublishCount(publishPlanService.lambdaQuery().eq(RbPublishPlan::getPublishStatus, RedbookStatusConstant.PUBLISH_PENDING).count());
        overview.setPublishedCount(publishPlanService.lambdaQuery().in(RbPublishPlan::getPublishStatus, Arrays.asList(RedbookStatusConstant.PUBLISH_PUBLISHED, RedbookStatusConstant.PUBLISH_DATA_COLLECTED)).count());
        overview.setAvgHeatScore(average(hotspotService.list().stream().map(RbHotspot::getHeatScore).collect(Collectors.toList()), 1));
        overview.setAvgInteractionRate(average(noteMetricService.list().stream().map(RbNoteMetric::getInteractionRate).collect(Collectors.toList()), 4));
        overview.setTodoList(buildTodoList());
        overview.setTrackBoard(buildTrackBoard());
        return overview;
    }

    private List<RedbookWorkbenchTodoVO> buildTodoList() {
        List<RedbookWorkbenchTodoVO> todoList = new ArrayList<>();

        hotspotService.lambdaQuery()
            .eq(RbHotspot::getStatus, RedbookStatusConstant.HOTSPOT_PENDING_ANALYSIS)
            .orderByDesc(RbHotspot::getUpdateTime)
            .last("limit 4")
            .list()
            .forEach(item -> todoList.add(toTodo(item.getId(), defaultText(item.getTitle(), "未命名热点"), "热点池", "/redbook/hotspot", item.getStatus(), firstNonBlank(item.getSummary(), "等待运营分析"), item.getUpdateTime())));

        noteDraftService.lambdaQuery()
            .eq(RbNoteDraft::getStatus, RedbookStatusConstant.DRAFT_PENDING_REVIEW)
            .orderByDesc(RbNoteDraft::getUpdateTime)
            .last("limit 4")
            .list()
            .forEach(item -> todoList.add(toTodo(item.getId(), defaultText(item.getTitle(), "未命名草稿"), "笔记草稿", "/redbook/note-draft", item.getAuditStatus(), firstNonBlank(item.getAuditOpinion(), "等待人工审核"), item.getUpdateTime())));

        publishPlanService.lambdaQuery()
            .eq(RbPublishPlan::getPublishStatus, RedbookStatusConstant.PUBLISH_PENDING)
            .orderByAsc(RbPublishPlan::getPlannedPublishTime)
            .last("limit 4")
            .list()
            .forEach(item -> todoList.add(toTodo(item.getId(), "排期 " + defaultText(item.getDraftId(), "未绑定草稿"), "发布计划", "/redbook/publish-plan", item.getPublishStatus(), "计划发布时间：" + formatDateTime(item.getPlannedPublishTime()), item.getPlannedPublishTime())));

        List<String> collectedPlanIds = noteMetricService.list().stream()
            .map(RbNoteMetric::getPublishPlanId)
            .filter(item -> !isBlank(item))
            .distinct()
            .collect(Collectors.toList());
        publishPlanService.lambdaQuery()
            .eq(RbPublishPlan::getPublishStatus, RedbookStatusConstant.PUBLISH_PUBLISHED)
            .notIn(!collectedPlanIds.isEmpty(), RbPublishPlan::getId, collectedPlanIds)
            .orderByAsc(RbPublishPlan::getActualPublishTime)
            .last("limit 4")
            .list()
            .forEach(item -> todoList.add(toTodo(item.getId(), "回收 " + defaultText(item.getDraftId(), "未绑定草稿"), "数据回收", "/redbook/publish-plan", item.getPublishStatus(), "已发布，等待录入首轮数据", firstNonNull(item.getActualPublishTime(), item.getUpdateTime()))));

        return todoList.stream()
            .sorted(Comparator.comparing(RedbookWorkbenchTodoVO::getUpdateTime, Comparator.nullsLast(Date::compareTo)).reversed())
            .limit(8)
            .collect(Collectors.toList());
    }

    private List<RedbookWorkbenchTrackVO> buildTrackBoard() {
        List<RbTrack> tracks = trackService.list();
        if (tracks.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Long> hotspotCountMap = hotspotService.list().stream()
            .filter(item -> !isBlank(item.getTrackId()))
            .collect(Collectors.groupingBy(RbHotspot::getTrackId, Collectors.counting()));

        List<RbNoteDraft> drafts = noteDraftService.list();
        Map<String, Long> draftCountMap = drafts.stream()
            .filter(item -> !isBlank(item.getTrackId()))
            .collect(Collectors.groupingBy(RbNoteDraft::getTrackId, Collectors.counting()));
        Map<String, String> draftTrackMap = drafts.stream()
            .filter(item -> !isBlank(item.getId()) && !isBlank(item.getTrackId()))
            .collect(Collectors.toMap(RbNoteDraft::getId, RbNoteDraft::getTrackId, (left, right) -> left));

        Map<String, Long> publishedCountMap = publishPlanService.list().stream()
            .filter(item -> isPublishedStatus(item.getPublishStatus()))
            .map(item -> draftTrackMap.get(item.getDraftId()))
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return tracks.stream()
            .map(track -> {
                RedbookWorkbenchTrackVO vo = new RedbookWorkbenchTrackVO();
                vo.setTrackId(track.getId());
                vo.setTrackName(defaultText(track.getTrackName(), "未命名赛道"));
                vo.setHotspotCount(hotspotCountMap.getOrDefault(track.getId(), 0L));
                vo.setDraftCount(draftCountMap.getOrDefault(track.getId(), 0L));
                vo.setPublishedCount(publishedCountMap.getOrDefault(track.getId(), 0L));
                return vo;
            })
            .sorted(Comparator.comparing(this::trackBoardWeight).reversed())
            .limit(5)
            .collect(Collectors.toList());
    }

    private long trackBoardWeight(RedbookWorkbenchTrackVO vo) {
        return vo.getPublishedCount() * 100 + vo.getDraftCount() * 10 + vo.getHotspotCount();
    }

    private RedbookWorkbenchTodoVO toTodo(String id, String title, String moduleTitle, String routePath, String status, String summary, Date updateTime) {
        RedbookWorkbenchTodoVO todo = new RedbookWorkbenchTodoVO();
        todo.setId(id);
        todo.setTitle(title);
        todo.setModuleTitle(moduleTitle);
        todo.setRoutePath(routePath);
        todo.setStatus(defaultText(status, "-"));
        todo.setSummary(summary);
        todo.setUpdateTime(updateTime);
        return todo;
    }

    private BigDecimal buildAnalysisScore(RbHotspot hotspot, List<String> sensitiveHits) {
        BigDecimal heatScore = safeScore(hotspot.getHeatScore(), socialScore(hotspot));
        BigDecimal remixScore = safeScore(hotspot.getRemixScore(), inferRemixScore(hotspot));
        BigDecimal riskPenalty = sensitiveHits.isEmpty() ? BigDecimal.ZERO : BigDecimal.valueOf(Math.min(20, sensitiveHits.size() * 5L));
        BigDecimal score = heatScore.multiply(BigDecimal.valueOf(0.55))
            .add(remixScore.multiply(BigDecimal.valueOf(0.35)))
            .add(BigDecimal.valueOf(10))
            .subtract(riskPenalty);
        return score.max(BigDecimal.ZERO).min(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP);
    }

    private BigDecimal socialScore(RbHotspot hotspot) {
        long total = defaultLong(hotspot.getLikeCount()) + defaultLong(hotspot.getCollectCount()) * 2 + defaultLong(hotspot.getCommentCount()) * 2 + defaultLong(hotspot.getShareCount()) * 3;
        if (total <= 0) {
            return BigDecimal.valueOf(55);
        }
        long bounded = Math.min(total / 20, 100);
        return BigDecimal.valueOf(Math.max(bounded, 55));
    }

    private BigDecimal inferRemixScore(RbHotspot hotspot) {
        String title = defaultText(hotspot.getTitle(), "");
        if (title.contains("攻略") || title.contains("教程") || title.contains("方法")) {
            return BigDecimal.valueOf(88);
        }
        if (title.contains("避坑") || title.contains("清单") || title.contains("复盘")) {
            return BigDecimal.valueOf(85);
        }
        return BigDecimal.valueOf(76);
    }

    private String buildRiskWarning(RbHotspot hotspot, List<String> sensitiveHits) {
        List<String> warnings = new ArrayList<>();
        if (!sensitiveHits.isEmpty()) {
            warnings.add("命中敏感词：" + String.join("、", sensitiveHits) + "，需要改写后再发布。");
        }
        if (!isBlank(hotspot.getRiskLevel())) {
            warnings.add("当前热点风险等级为 " + hotspot.getRiskLevel() + "。");
        }
        if (warnings.isEmpty()) {
            warnings.add("未发现明显高风险表达，仍建议人工核对绝对化承诺、医疗功效、夸张收益等表述。");
        }
        return String.join(" ", warnings);
    }

    private List<String> findSensitiveHits(String... texts) {
        String joinedText = Arrays.stream(texts)
            .filter(Objects::nonNull)
            .collect(Collectors.joining(" "))
            .toLowerCase(Locale.ROOT);
        if (isBlank(joinedText)) {
            return new ArrayList<>();
        }
        return listActiveSensitiveWords().stream()
            .map(RbSensitiveWord::getWord)
            .filter(word -> !isBlank(word) && joinedText.contains(word.toLowerCase(Locale.ROOT)))
            .distinct()
            .limit(8)
            .collect(Collectors.toList());
    }

    private List<RbSensitiveWord> listActiveSensitiveWords() {
        return sensitiveWordService.lambdaQuery()
            .eq(RbSensitiveWord::getStatus, RedbookStatusConstant.ACTIVE)
            .list();
    }

    private RedbookDraftRiskHitVO buildDraftRiskHit(RbSensitiveWord sensitiveWord, Map<String, String> riskFields) {
        if (sensitiveWord == null || isBlank(sensitiveWord.getWord())) {
            return null;
        }
        List<String> matchedFields = riskFields.entrySet().stream()
            .filter(entry -> containsIgnoreCase(entry.getValue(), sensitiveWord.getWord()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        if (matchedFields.isEmpty()) {
            return null;
        }
        RedbookDraftRiskHitVO hit = new RedbookDraftRiskHitVO();
        hit.setWord(sensitiveWord.getWord());
        hit.setCategory(defaultText(sensitiveWord.getCategory(), "未分类"));
        hit.setRiskLevel(firstNonBlank(sensitiveWord.getRiskLevel(), "medium"));
        hit.setReplacementSuggestion(firstNonBlank(sensitiveWord.getReplacementSuggestion(), "建议改成更客观、可验证的表达"));
        hit.setMatchedFields(matchedFields);
        return hit;
    }

    private String resolveDraftRiskLevel(List<RedbookDraftRiskHitVO> hits) {
        if (hits == null || hits.isEmpty()) {
            return "low";
        }
        if (hits.stream().anyMatch(item -> "high".equalsIgnoreCase(item.getRiskLevel()))) {
            return "high";
        }
        if (hits.size() >= 3 || hits.stream().anyMatch(item -> "medium".equalsIgnoreCase(item.getRiskLevel()))) {
            return "medium";
        }
        return "low";
    }

    private String buildDraftRiskSummary(
        List<RedbookDraftRiskHitVO> hits,
        List<String> matchedFields,
        List<String> replacementSuggestions,
        String riskLevel
    ) {
        if (hits == null || hits.isEmpty()) {
            return "未命中明显敏感词，建议发布前再人工核对绝对化承诺、收益夸大和功效表述。";
        }
        String hitWords = hits.stream()
            .map(RedbookDraftRiskHitVO::getWord)
            .filter(item -> !isBlank(item))
            .distinct()
            .limit(6)
            .collect(Collectors.joining("、"));
        String fieldText = matchedFields == null || matchedFields.isEmpty() ? "内容字段" : String.join("、", matchedFields);
        String suggestionText = replacementSuggestions == null || replacementSuggestions.isEmpty()
            ? "建议改写后再进入人工审核。"
            : "建议优先替换为：" + String.join("；", replacementSuggestions.stream().limit(3).collect(Collectors.toList()));
        return "本次风险检查命中 " + hits.size() + " 个敏感词，主要分布在 " + fieldText
            + "。当前风险等级为 " + riskLevel + "，命中词包括：" + hitWords + "。" + suggestionText;
    }

    private int riskLevelWeight(String riskLevel) {
        if ("high".equalsIgnoreCase(riskLevel)) {
            return 3;
        }
        if ("medium".equalsIgnoreCase(riskLevel)) {
            return 2;
        }
        return 1;
    }

    private boolean containsIgnoreCase(String text, String keyword) {
        if (isBlank(text) || isBlank(keyword)) {
            return false;
        }
        return text.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    private List<String> buildKeywords(String... rawTexts) {
        LinkedHashSet<String> keywords = new LinkedHashSet<>();
        for (String rawText : rawTexts) {
            if (isBlank(rawText)) {
                continue;
            }
            for (String token : SPLIT_PATTERN.split(rawText)) {
                String normalized = token == null ? "" : token.trim();
                if (normalized.length() >= 2 && normalized.length() <= 12) {
                    keywords.add(normalized);
                }
                if (keywords.size() >= 6) {
                    break;
                }
            }
            if (keywords.size() >= 6) {
                break;
            }
        }
        return new ArrayList<>(keywords);
    }

    private String buildTagSuggestion(String trackName, List<String> keywords) {
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        tags.add("#" + trackName);
        keywords.stream().limit(4).forEach(keyword -> tags.add(keyword.startsWith("#") ? keyword : "#" + keyword));
        tags.add("#小红书运营");
        return String.join(" ", tags);
    }

    private String buildDraftTitle(RbHotspot hotspot, String trackName) {
        String title = defaultText(hotspot.getTitle(), trackName + "选题拆解");
        if (title.length() > 24) {
            return "别只盯着热点，" + title.substring(0, 20);
        }
        return "这条" + trackName + "热点，值得拆一次：" + title;
    }

    private String buildDraftBody(RbHotspot hotspot, RbHotspotAnalysis analysis, String trackName, RbAccount account) {
        String accountPositioning = account == null ? "你的账号定位" : defaultText(account.getPositioning(), defaultText(account.getAccountName(), "你的账号定位"));
        String summary = firstNonBlank(hotspot.getSummary(), "原热点没有完整摘要，建议补充原文关键信息。");
        return "先说结论：这条热点值得做，但不要直接复述。\n\n"
            + "为什么它会火？\n"
            + analysis.getHookAnalysis() + "\n\n"
            + "如果你做的是" + accountPositioning + "，更适合从下面这个角度切：\n"
            + analysis.getContentAngle() + "\n\n"
            + "可以直接照着这个结构展开：\n"
            + analysis.getOutlineSuggestion() + "\n\n"
            + "原热点关键信息：\n"
            + summary + "\n\n"
            + "最后提醒：\n"
            + firstNonBlank(analysis.getRiskWarning(), "发布前做一次敏感表达自检，避免夸张承诺。");
    }

    private String buildContentType(RbHotspot hotspot) {
        String title = defaultText(hotspot.getTitle(), "");
        if (title.contains("测评") || title.contains("对比")) {
            return "测评";
        }
        if (title.contains("避坑")) {
            return "避坑";
        }
        if (title.contains("清单")) {
            return "清单";
        }
        if (title.contains("教程") || title.contains("方法") || title.contains("攻略")) {
            return "教程";
        }
        return "干货";
    }

    private String buildPublishSuggestion(RbHotspot hotspot) {
        Date publishTime = hotspot.getPublishTime();
        if (publishTime == null) {
            return "建议工作日 19:30 发布，发布后 2 小时重点观察收藏和评论。";
        }
        LocalTime localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(publishTime.getTime()), DEFAULT_ZONE_ID).toLocalTime();
        if (localTime.isAfter(LocalTime.of(10, 30)) && localTime.isBefore(LocalTime.of(14, 0))) {
            return "建议中午 12:00-13:30 发布，适合测试收藏率。";
        }
        if (localTime.isAfter(LocalTime.of(17, 0)) && localTime.isBefore(LocalTime.of(22, 30))) {
            return "建议晚间 19:00-21:00 发布，适合观察评论与私信转化。";
        }
        return "建议工作日 19:30 发布，发布后 2 小时重点观察互动率。";
    }

    private Date nextPublishTime() {
        LocalDateTime now = LocalDateTime.now(DEFAULT_ZONE_ID);
        LocalDateTime planned = now.withHour(20).withMinute(0).withSecond(0).withNano(0);
        if (!planned.isAfter(now)) {
            planned = planned.plusDays(1);
        }
        return Date.from(planned.atZone(DEFAULT_ZONE_ID).toInstant());
    }

    private Map<String, Object> buildHotspotAnalysisContext(RbHotspot hotspot, RbTrack track, List<String> keywords, List<String> sensitiveHits, BigDecimal score) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("track", buildTrackContext(track));
        context.put("hotspot", buildHotspotContext(hotspot));
        context.put("keywords", keywords);
        context.put("sensitive_hits", sensitiveHits);
        context.put("default_score", score);
        return context;
    }

    private Map<String, Object> buildDraftContext(RbHotspot hotspot, RbHotspotAnalysis analysis, RbTrack track, RbAccount account, String title, String body, String publishSuggestion) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("track", buildTrackContext(track));
        context.put("account", buildAccountContext(account));
        context.put("hotspot", buildHotspotContext(hotspot));
        context.put("analysis", buildAnalysisContext(analysis));
        context.put("draft_hint", Map.of(
            "title", title,
            "body", body,
            "publish_time_suggestion", publishSuggestion
        ));
        return context;
    }

    private Map<String, Object> buildReviewContext(
        RbReviewReport report,
        RbTrack track,
        RbAccount account,
        List<RbPublishPlan> publishedPlans,
        Map<String, RbNoteMetric> latestMetricMap,
        Map<String, RbNoteDraft> draftMap,
        BigDecimal avgViews,
        BigDecimal avgInteractionRate
    ) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("report", Map.of(
            "report_name", firstNonBlank(report.getReportName(), "未命名复盘"),
            "period_start", formatDate(report.getPeriodStart()),
            "period_end", formatDate(report.getPeriodEnd())
        ));
        context.put("track", buildTrackContext(track));
        context.put("account", buildAccountContext(account));
        context.put("summary_stats", Map.of(
            "published_count", publishedPlans.size(),
            "collected_count", latestMetricMap.size(),
            "avg_views", avgViews,
            "avg_interaction_rate", avgInteractionRate
        ));
        context.put("high_examples", buildPerformanceSamples(
            publishedPlans.stream()
                .sorted(Comparator.<RbPublishPlan, BigDecimal>comparing(plan -> buildReviewWeight(latestMetricMap.get(plan.getId())), Comparator.nullsLast(BigDecimal::compareTo)).reversed())
                .limit(5)
                .collect(Collectors.toList()),
            draftMap,
            latestMetricMap
        ));
        context.put("low_examples", buildPerformanceSamples(
            publishedPlans.stream()
                .sorted(Comparator.<RbPublishPlan, BigDecimal>comparing(plan -> buildReviewWeight(latestMetricMap.get(plan.getId())), Comparator.nullsLast(BigDecimal::compareTo)))
                .limit(5)
                .collect(Collectors.toList()),
            draftMap,
            latestMetricMap
        ));
        return context;
    }

    private Map<String, Object> buildTrackContext(RbTrack track) {
        Map<String, Object> context = new LinkedHashMap<>();
        if (track == null) {
            context.put("track_name", "全部赛道");
            return context;
        }
        context.put("track_id", track.getId());
        context.put("track_name", defaultText(track.getTrackName(), "未命名赛道"));
        context.put("keywords", defaultText(track.getKeywords(), ""));
        context.put("target_audience", defaultText(track.getTargetAudience(), ""));
        context.put("content_direction", defaultText(track.getContentDirection(), ""));
        return context;
    }

    private Map<String, Object> buildAccountContext(RbAccount account) {
        Map<String, Object> context = new LinkedHashMap<>();
        if (account == null) {
            context.put("account_name", "全部账号");
            return context;
        }
        context.put("account_id", account.getId());
        context.put("account_name", defaultText(account.getAccountName(), "未命名账号"));
        context.put("positioning", defaultText(account.getPositioning(), ""));
        context.put("target_audience", defaultText(account.getTargetAudience(), ""));
        context.put("content_style", defaultText(account.getContentStyle(), ""));
        return context;
    }

    private Map<String, Object> buildHotspotContext(RbHotspot hotspot) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("hotspot_id", hotspot.getId());
        context.put("track_id", hotspot.getTrackId());
        context.put("title", defaultText(hotspot.getTitle(), "未命名热点"));
        context.put("summary", defaultText(hotspot.getSummary(), ""));
        context.put("source_platform", defaultText(hotspot.getSourcePlatform(), ""));
        context.put("author_name", defaultText(hotspot.getAuthorName(), ""));
        context.put("tags", defaultText(hotspot.getTags(), ""));
        context.put("heat_score", hotspot.getHeatScore());
        context.put("remix_score", hotspot.getRemixScore());
        context.put("risk_level", hotspot.getRiskLevel());
        context.put("metrics", Map.of(
            "likes", defaultLong(hotspot.getLikeCount()),
            "collects", defaultLong(hotspot.getCollectCount()),
            "comments", defaultLong(hotspot.getCommentCount()),
            "shares", defaultLong(hotspot.getShareCount())
        ));
        return context;
    }

    private Map<String, Object> buildAnalysisContext(RbHotspotAnalysis analysis) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("analysis_id", analysis.getId());
        context.put("pain_points", defaultText(analysis.getPainPoints(), ""));
        context.put("hook_analysis", defaultText(analysis.getHookAnalysis(), ""));
        context.put("content_angle", defaultText(analysis.getContentAngle(), ""));
        context.put("title_directions", defaultText(analysis.getTitleDirections(), ""));
        context.put("outline_suggestion", defaultText(analysis.getOutlineSuggestion(), ""));
        context.put("cover_copy_suggestion", defaultText(analysis.getCoverCopySuggestion(), ""));
        context.put("tag_suggestion", defaultText(analysis.getTagSuggestion(), ""));
        context.put("risk_warning", defaultText(analysis.getRiskWarning(), ""));
        context.put("score", analysis.getScore());
        return context;
    }

    private List<Map<String, Object>> buildPerformanceSamples(List<RbPublishPlan> plans, Map<String, RbNoteDraft> draftMap, Map<String, RbNoteMetric> latestMetricMap) {
        return plans.stream()
            .map(plan -> {
                RbNoteDraft draft = draftMap.get(plan.getDraftId());
                RbNoteMetric metric = latestMetricMap.get(plan.getId());
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("publish_plan_id", plan.getId());
                row.put("title", draft == null ? "未关联草稿" : defaultText(draft.getTitle(), "未命名内容"));
                row.put("content_type", draft == null ? "未知" : defaultText(draft.getContentType(), "未分类"));
                row.put("publish_time", formatDateTime(resolvePublishTime(plan)));
                row.put("publish_window", resolvePublishWindow(resolvePublishTime(plan)));
                row.put("views", metric == null ? 0L : defaultLong(metric.getViews()));
                row.put("interaction_rate", metric == null ? BigDecimal.ZERO : Optional.ofNullable(metric.getInteractionRate()).orElse(BigDecimal.ZERO));
                row.put("collect_rate", metric == null ? BigDecimal.ZERO : Optional.ofNullable(metric.getCollectRate()).orElse(BigDecimal.ZERO));
                row.put("note_url", defaultText(plan.getNoteUrl(), ""));
                return row;
            })
            .collect(Collectors.toList());
    }

    private String buildReviewSummary(String trackName, String accountName, int publishedCount, int collectedCount, BigDecimal avgViews, BigDecimal avgInteractionRate) {
        if (publishedCount <= 0) {
            return "当前周期暂无已发布内容，建议先完成发布排期和数据回收，再生成复盘报告。";
        }
        return "本周期 " + trackName + " / " + accountName + " 共发布 " + publishedCount + " 条内容，已回收 " + collectedCount + " 条有效数据。"
            + "数据覆盖率约 " + coverageRateText(publishedCount, collectedCount) + "，可用于判断当前结论可信度。"
            + "平均阅读/播放量约 " + formatNumber(avgViews) + "，平均互动率约 " + percentageText(avgInteractionRate) + "。"
            + "从当前样本看，用户更愿意为结论前置、步骤明确、带真实经验的内容停留与互动。";
    }

    private String buildHighPerformingFactors(String trackName, List<RbPublishPlan> plans, Map<String, RbNoteDraft> draftMap, Map<String, RbNoteMetric> latestMetricMap) {
        if (plans.isEmpty()) {
            return "当前周期高表现样本不足，建议继续补录 24 小时和 72 小时数据后再放大结论。";
        }
        String contentTypes = plans.stream()
            .map(plan -> draftMap.get(plan.getDraftId()))
            .filter(Objects::nonNull)
            .map(item -> defaultText(item.getContentType(), "未分类"))
            .distinct()
            .collect(Collectors.joining("、"));
        String topTitles = plans.stream()
            .map(plan -> draftMap.get(plan.getDraftId()))
            .filter(Objects::nonNull)
            .map(item -> defaultText(item.getTitle(), "未命名内容"))
            .limit(2)
            .collect(Collectors.joining(" / "));
        BigDecimal topInteraction = average(
            plans.stream()
                .map(plan -> latestMetricMap.get(plan.getId()))
                .filter(Objects::nonNull)
                .map(RbNoteMetric::getInteractionRate)
                .collect(Collectors.toList()),
            4
        );
        return "高表现内容主要集中在 " + firstNonBlank(contentTypes, trackName) + " 相关表达，标题更偏结论前置与问题直给。"
            + "\n代表样本：" + firstNonBlank(topTitles, "暂无样本标题")
            + "\n高表现样本平均互动率约 " + percentageText(topInteraction) + "，说明“真实场景 + 可执行步骤 + 避坑提醒”更容易带来收藏和评论。";
    }

    private String buildLowPerformingReasons(List<RbPublishPlan> plans, Map<String, RbNoteDraft> draftMap, Map<String, RbNoteMetric> latestMetricMap) {
        if (plans.isEmpty()) {
            return "当前没有明确的低表现样本，建议继续扩大样本量后观察。";
        }
        String titles = plans.stream()
            .map(plan -> draftMap.get(plan.getDraftId()))
            .filter(Objects::nonNull)
            .map(item -> defaultText(item.getTitle(), "未命名内容"))
            .collect(Collectors.joining(" / "));
        BigDecimal avgInteraction = average(
            plans.stream()
                .map(plan -> latestMetricMap.get(plan.getId()))
                .filter(Objects::nonNull)
                .map(RbNoteMetric::getInteractionRate)
                .collect(Collectors.toList()),
            4
        );
        return "低表现内容通常存在标题钩子偏弱、正文价值点露出靠后、评论区引导不够明确的问题。"
            + "\n较弱样本：" + firstNonBlank(titles, "暂无样本标题")
            + "\n这部分内容平均互动率约 " + percentageText(avgInteraction) + "，建议下轮减少泛泛表述，尽量把结果、对比和步骤前置。";
    }

    private String buildReusableTopics(List<RbPublishPlan> plans, Map<String, RbNoteDraft> draftMap) {
        if (plans.isEmpty()) {
            return "当前还没有稳定复用的主题，建议继续积累高表现样本。";
        }
        return plans.stream()
            .map(plan -> draftMap.get(plan.getDraftId()))
            .filter(Objects::nonNull)
            .map(item -> defaultText(item.getTitle(), "未命名内容"))
            .distinct()
            .limit(5)
            .collect(Collectors.joining("\n"));
    }

    private String buildStoppedDirections(List<RbPublishPlan> plans, Map<String, RbNoteDraft> draftMap) {
        if (plans.isEmpty()) {
            return "当前暂无需要完全停止的方向，建议继续小样本验证。";
        }
        String contentTypes = plans.stream()
            .map(plan -> draftMap.get(plan.getDraftId()))
            .filter(Objects::nonNull)
            .map(item -> defaultText(item.getContentType(), "未分类"))
            .distinct()
            .collect(Collectors.joining("、"));
        return "建议减少纯信息搬运、场景不具体、缺少可执行动作的内容方向。当前较弱样本多出现在 "
            + firstNonBlank(contentTypes, "泛话题内容")
            + " 类型，后续可只保留小样本测试。";
    }

    private String buildNextTopicSuggestions(String trackName, List<RbPublishPlan> plans, Map<String, RbNoteDraft> draftMap) {
        List<String> keywords = plans.stream()
            .map(plan -> draftMap.get(plan.getDraftId()))
            .filter(Objects::nonNull)
            .flatMap(item -> buildKeywords(item.getTitle(), item.getTags(), item.getBody()).stream())
            .distinct()
            .limit(4)
            .collect(Collectors.toList());
        if (keywords.isEmpty()) {
            return "继续围绕 " + trackName + " 的真实场景、步骤拆解、避坑提醒和对比复盘展开选题。";
        }
        return "下一轮优先围绕 " + trackName + " 的 " + String.join("、", keywords)
            + " 继续做专题化拆解，优先保留真实经验、具体动作和结果对比。";
    }

    private String buildNextTitleSuggestions(String trackName, List<RbPublishPlan> plans, Map<String, RbNoteDraft> draftMap) {
        String sampleTitle = plans.stream()
            .map(plan -> draftMap.get(plan.getDraftId()))
            .filter(Objects::nonNull)
            .map(RbNoteDraft::getTitle)
            .filter(item -> !isBlank(item))
            .findFirst()
            .orElse(trackName + "内容复盘");
        return "1. 为什么这条 " + trackName + " 内容更容易被收藏\n"
            + "2. 做 " + trackName + " 别再只讲结论，这 3 个细节更关键\n"
            + "3. 同样一个热点，换成这个结构更容易出互动\n"
            + "4. 从样本“" + sampleTitle + "”里，我总结出一个更稳的写法";
    }

    private String buildNextPublishSuggestions(List<RbPublishPlan> plans, Map<String, RbNoteMetric> latestMetricMap) {
        if (plans.isEmpty()) {
            return "优先测试工作日 19:00-21:00，保留 12:00-13:30 作为对照时段，并在 24 小时内补录数据。";
        }
        Map<String, List<BigDecimal>> interactionByWindow = new LinkedHashMap<>();
        plans.forEach(plan -> {
            String window = resolvePublishWindow(resolvePublishTime(plan));
            BigDecimal interactionRate = Optional.ofNullable(latestMetricMap.get(plan.getId()))
                .map(RbNoteMetric::getInteractionRate)
                .orElse(BigDecimal.ZERO);
            interactionByWindow.computeIfAbsent(window, key -> new ArrayList<>()).add(interactionRate);
        });
        String bestWindow = interactionByWindow.entrySet().stream()
            .max(Comparator.comparing(entry -> average(entry.getValue(), 4), Comparator.nullsLast(BigDecimal::compareTo)))
            .map(Map.Entry::getKey)
            .orElse("晚间 19:00-21:00");
        return "当前高表现样本更集中在 " + bestWindow + " 发布。建议下一轮优先测试这个时段，同时保留一个次优时间窗做 A/B 对照。";
    }

    private RedbookReviewRankItemVO toReviewRankItem(
        RbPublishPlan plan,
        RbNoteMetric metric,
        Map<String, RbNoteDraft> draftMap,
        Map<String, RbTrack> trackMap,
        Map<String, RbAccount> accountMap
    ) {
        RbNoteDraft draft = draftMap.get(plan.getDraftId());
        String trackId = draft == null ? "" : draft.getTrackId();
        String accountId = firstNonBlank(plan.getAccountId(), draft == null ? "" : draft.getAccountId());
        RedbookReviewRankItemVO item = new RedbookReviewRankItemVO();
        item.setPublishPlanId(plan.getId());
        item.setDraftId(plan.getDraftId());
        item.setTitle(draft == null ? "未关联草稿" : defaultText(draft.getTitle(), "未命名内容"));
        item.setTrackId(trackId);
        item.setTrackName(Optional.ofNullable(trackMap.get(trackId)).map(RbTrack::getTrackName).orElse("未绑定赛道"));
        item.setAccountId(accountId);
        item.setAccountName(Optional.ofNullable(accountMap.get(accountId)).map(RbAccount::getAccountName).orElse("未绑定账号"));
        item.setPublishTime(resolvePublishTime(plan));
        item.setCollectNode(metric == null ? "" : defaultText(metric.getCollectNode(), ""));
        item.setViews(metric == null ? 0L : defaultLong(metric.getViews()));
        item.setLikes(metric == null ? 0L : defaultLong(metric.getLikes()));
        item.setCollects(metric == null ? 0L : defaultLong(metric.getCollects()));
        item.setComments(metric == null ? 0L : defaultLong(metric.getComments()));
        item.setShares(metric == null ? 0L : defaultLong(metric.getShares()));
        item.setInteractionRate(metric == null ? BigDecimal.ZERO : Optional.ofNullable(metric.getInteractionRate()).orElse(BigDecimal.ZERO));
        item.setCollectRate(metric == null ? BigDecimal.ZERO : Optional.ofNullable(metric.getCollectRate()).orElse(BigDecimal.ZERO));
        item.setScore(buildReviewWeight(metric));
        item.setNoteUrl(defaultText(plan.getNoteUrl(), ""));
        return item;
    }

    private List<RedbookReviewDimensionVO> buildReviewDimensionBoard(
        List<RbPublishPlan> plans,
        Map<String, RbNoteMetric> latestMetricMap,
        Function<RbPublishPlan, String> idResolver,
        Function<String, String> nameResolver
    ) {
        Map<String, List<RbPublishPlan>> groupedPlans = plans.stream()
            .collect(Collectors.groupingBy(plan -> defaultText(idResolver.apply(plan), "未分组"), LinkedHashMap::new, Collectors.toList()));
        return groupedPlans.entrySet().stream()
            .map(entry -> {
                List<RbPublishPlan> groupPlans = entry.getValue();
                List<RbNoteMetric> groupMetrics = groupPlans.stream()
                    .map(plan -> latestMetricMap.get(plan.getId()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
                RedbookReviewDimensionVO vo = new RedbookReviewDimensionVO();
                vo.setDimensionId(entry.getKey());
                vo.setDimensionName(nameResolver.apply(entry.getKey()));
                vo.setPublishCount((long) groupPlans.size());
                vo.setCollectedCount((long) groupMetrics.size());
                vo.setTotalViews(groupMetrics.stream().mapToLong(metric -> defaultLong(metric.getViews())).sum());
                vo.setAvgViews(averageLongValues(groupMetrics.stream().map(RbNoteMetric::getViews).collect(Collectors.toList()), 1));
                vo.setAvgInteractionRate(average(groupMetrics.stream().map(RbNoteMetric::getInteractionRate).collect(Collectors.toList()), 4));
                vo.setScore(vo.getAvgViews().add(vo.getAvgInteractionRate().multiply(BigDecimal.valueOf(10000))).setScale(1, RoundingMode.HALF_UP));
                return vo;
            })
            .sorted(Comparator.comparing(RedbookReviewDimensionVO::getScore, Comparator.nullsLast(BigDecimal::compareTo)).reversed())
            .limit(8)
            .collect(Collectors.toList());
    }

    private RbReviewReport pickLatestReviewReport() {
        return reviewReportService.list().stream()
            .max(Comparator.comparing(report -> firstNonNull(report.getUpdateTime(), report.getCreateTime()), Comparator.nullsLast(Date::compareTo)))
            .orElse(null);
    }

    private boolean matchDashboardScope(
        RbPublishPlan publishPlan,
        RbNoteDraft draft,
        Date periodStart,
        Date periodEnd,
        String trackId,
        String accountId
    ) {
        Date publishTime = resolvePublishTime(publishPlan);
        if (!matchesOptionalPeriod(publishTime, periodStart, periodEnd)) {
            return false;
        }
        if (!isBlank(trackId)) {
            String currentTrackId = draft == null ? "" : draft.getTrackId();
            if (!trackId.equals(currentTrackId)) {
                return false;
            }
        }
        if (!isBlank(accountId)) {
            String currentAccountId = firstNonBlank(publishPlan.getAccountId(), draft == null ? null : draft.getAccountId());
            if (!accountId.equals(currentAccountId)) {
                return false;
            }
        }
        return true;
    }

    private boolean matchDashboardReportScope(
        RbReviewReport report,
        Date periodStart,
        Date periodEnd,
        String trackId,
        String accountId
    ) {
        if (report == null) {
            return false;
        }
        if (!isBlank(trackId) && !isBlank(report.getTrackId()) && !trackId.equals(report.getTrackId())) {
            return false;
        }
        if (!isBlank(accountId) && !isBlank(report.getAccountId()) && !accountId.equals(report.getAccountId())) {
            return false;
        }
        return overlapsOptionalPeriod(periodStart, periodEnd, report.getPeriodStart(), report.getPeriodEnd());
    }

    private List<String> splitSuggestionLines(String... texts) {
        List<String> values = new ArrayList<>();
        for (String text : texts) {
            if (isBlank(text)) {
                continue;
            }
            Arrays.stream(text.split("[\\n；;]+"))
                .map(String::trim)
                .filter(item -> !isBlank(item))
                .forEach(values::add);
        }
        return values.stream().distinct().limit(12).collect(Collectors.toList());
    }

    private String cleanSuggestionTitle(String text) {
        if (isBlank(text)) {
            return "";
        }
        String cleaned = text.trim()
            .replaceFirst("^[0-9]+[.、)）\\s]+", "")
            .replaceFirst("^[-*•\\s]+", "")
            .trim();
        if (cleaned.length() > 120) {
            return cleaned.substring(0, 120);
        }
        return cleaned;
    }

    private List<String> buildFallbackTopicSuggestionList() {
        return Arrays.asList("围绕高收藏内容继续拆解真实场景", "把高互动标题改写成系列化选题", "补充低表现内容的反向避坑复盘");
    }

    private List<String> buildFallbackTitleSuggestionList() {
        return Arrays.asList("这类内容为什么更容易被收藏", "同样一个热点，换这个结构更容易出互动", "我从低表现内容里总结出的 3 个问题");
    }

    private List<String> buildFallbackPublishSuggestionList() {
        return Arrays.asList("优先测试工作日 19:00-21:00", "保留中午 12:00-13:30 作为对照", "发布后 2h、24h、72h 连续回收数据");
    }

    private RbNoteMetric pickLatestMetric(List<RbNoteMetric> metrics) {
        return metrics.stream()
            .filter(Objects::nonNull)
            .max(Comparator.comparing(this::resolveMetricTime, Comparator.nullsLast(Date::compareTo)))
            .orElse(null);
    }

    private Date resolveMetricTime(RbNoteMetric metric) {
        if (metric == null) {
            return null;
        }
        return firstNonNull(metric.getCollectTime(), metric.getUpdateTime(), metric.getCreateTime());
    }

    private BigDecimal buildReviewWeight(RbNoteMetric metric) {
        if (metric == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal views = BigDecimal.valueOf(defaultLong(metric.getViews()));
        BigDecimal collects = BigDecimal.valueOf(defaultLong(metric.getCollects()) * 5L);
        BigDecimal interaction = Optional.ofNullable(metric.getInteractionRate()).orElse(BigDecimal.ZERO).multiply(BigDecimal.valueOf(10000));
        return views.add(collects).add(interaction);
    }

    private boolean matchReportScope(RbPublishPlan publishPlan, RbNoteDraft draft, RbReviewReport report) {
        if (!isBlank(report.getTrackId())) {
            if (draft == null || !report.getTrackId().equals(draft.getTrackId())) {
                return false;
            }
        }
        if (!isBlank(report.getAccountId())) {
            String accountId = firstNonBlank(publishPlan.getAccountId(), draft == null ? null : draft.getAccountId());
            return report.getAccountId().equals(accountId);
        }
        return true;
    }

    private Date resolvePublishTime(RbPublishPlan publishPlan) {
        return firstNonNull(publishPlan.getActualPublishTime(), publishPlan.getPlannedPublishTime(), publishPlan.getUpdateTime(), publishPlan.getCreateTime());
    }

    private boolean isWithinPeriod(Date target, Date start, Date end) {
        if (target == null) {
            return false;
        }
        return !target.before(start) && !target.after(end);
    }

    private boolean matchesOptionalPeriod(Date target, Date start, Date end) {
        if (start == null && end == null) {
            return true;
        }
        if (target == null) {
            return false;
        }
        if (start != null && target.before(start)) {
            return false;
        }
        if (end != null && target.after(end)) {
            return false;
        }
        return true;
    }

    private boolean overlapsOptionalPeriod(Date filterStart, Date filterEnd, Date valueStart, Date valueEnd) {
        if (filterStart == null && filterEnd == null) {
            return true;
        }
        Date normalizedValueStart = valueStart == null ? valueEnd : valueStart;
        Date normalizedValueEnd = valueEnd == null ? valueStart : valueEnd;
        if (normalizedValueStart == null && normalizedValueEnd == null) {
            return false;
        }
        if (normalizedValueStart == null) {
            normalizedValueStart = normalizedValueEnd;
        }
        if (normalizedValueEnd == null) {
            normalizedValueEnd = normalizedValueStart;
        }
        if (filterStart != null && normalizedValueEnd.before(filterStart)) {
            return false;
        }
        if (filterEnd != null && normalizedValueStart.after(filterEnd)) {
            return false;
        }
        return true;
    }

    private Date startOfDay(Date date) {
        LocalDate localDate = LocalDateTime.ofInstant(date.toInstant(), DEFAULT_ZONE_ID).toLocalDate();
        return Date.from(localDate.atStartOfDay(DEFAULT_ZONE_ID).toInstant());
    }

    private Date endOfDay(Date date) {
        LocalDate localDate = LocalDateTime.ofInstant(date.toInstant(), DEFAULT_ZONE_ID).toLocalDate();
        return Date.from(localDate.plusDays(1).atStartOfDay(DEFAULT_ZONE_ID).minusNanos(1).toInstant());
    }

    private Date daysBefore(Date date, int days) {
        LocalDate localDate = LocalDateTime.ofInstant(date.toInstant(), DEFAULT_ZONE_ID).toLocalDate().minusDays(days);
        return Date.from(localDate.atStartOfDay(DEFAULT_ZONE_ID).toInstant());
    }

    private BigDecimal averageLongValues(List<Long> values, int scale) {
        List<BigDecimal> decimals = values.stream()
            .filter(Objects::nonNull)
            .map(BigDecimal::valueOf)
            .collect(Collectors.toList());
        return average(decimals, scale);
    }

    private String resolvePublishWindow(Date date) {
        if (date == null) {
            return "待定时段";
        }
        LocalTime localTime = LocalDateTime.ofInstant(date.toInstant(), DEFAULT_ZONE_ID).toLocalTime();
        if (!localTime.isBefore(LocalTime.of(7, 0)) && localTime.isBefore(LocalTime.of(11, 0))) {
            return "上午 07:00-11:00";
        }
        if (!localTime.isBefore(LocalTime.of(11, 0)) && localTime.isBefore(LocalTime.of(14, 0))) {
            return "中午 11:00-14:00";
        }
        if (!localTime.isBefore(LocalTime.of(17, 0)) && localTime.isBefore(LocalTime.of(22, 0))) {
            return "晚间 17:00-22:00";
        }
        return "其他时段";
    }

    private String textValue(Map<String, Object> values, String... keys) {
        return textValueWithDelimiter(values, "\n", keys);
    }

    private String textValueWithDelimiter(Map<String, Object> values, String delimiter, String... keys) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        for (String key : keys) {
            if (!values.containsKey(key)) {
                continue;
            }
            String rendered = renderText(values.get(key), delimiter);
            if (!isBlank(rendered)) {
                return rendered;
            }
        }
        return "";
    }

    private String renderText(Object value, String delimiter) {
        if (value == null) {
            return "";
        }
        if (value instanceof String text) {
            return text.trim();
        }
        if (value instanceof List<?> listValue) {
            return listValue.stream()
                .filter(Objects::nonNull)
                .map(item -> item instanceof Map<?, ?> ? toJson(item) : String.valueOf(item).trim())
                .filter(item -> !item.isEmpty())
                .collect(Collectors.joining(delimiter));
        }
        if (value instanceof Map<?, ?>) {
            return toJson(value);
        }
        return String.valueOf(value).trim();
    }

    private BigDecimal numberValue(Map<String, Object> values, BigDecimal fallback, String... keys) {
        if (values != null) {
            for (String key : keys) {
                if (!values.containsKey(key)) {
                    continue;
                }
                BigDecimal parsed = parseBigDecimal(values.get(key));
                if (parsed != null) {
                    return parsed.setScale(fallback.scale(), RoundingMode.HALF_UP);
                }
            }
        }
        return fallback;
    }

    private BigDecimal parseBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(value).trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private String buildWorkflowRawResult(RedbookAiExecutionResult aiResult, Map<String, Object> context, Map<String, Object> fallback) {
        Map<String, Object> raw = new LinkedHashMap<>();
        raw.put("provider", aiResult == null ? RedbookPromptTemplateConstant.PROVIDER_LOCAL : aiResult.getProvider());
        raw.put("template_code", aiResult == null ? "" : aiResult.getTemplateCode());
        raw.put("remote_used", aiResult != null && aiResult.isRemoteUsed());
        raw.put("success", aiResult != null && aiResult.isSuccess());
        raw.put("attempt_count", aiResult == null ? 0 : aiResult.getAttemptCount());
        if (aiResult != null && !isBlank(aiResult.getErrorType())) {
            raw.put("error_type", aiResult.getErrorType());
        }
        raw.put("schema_valid", aiResult == null || aiResult.isSchemaValid());
        if (aiResult != null && aiResult.getValidationErrors() != null && !aiResult.getValidationErrors().isEmpty()) {
            raw.put("validation_errors", aiResult.getValidationErrors());
        }
        if (aiResult != null && !isBlank(aiResult.getErrorMessage())) {
            raw.put("error_message", aiResult.getErrorMessage());
        }
        if (aiResult != null && !isBlank(aiResult.getRawResult())) {
            raw.put("remote_result", aiResult.getRawResult());
        }
        raw.put("context", context);
        raw.put("fallback", fallback);
        return toJson(raw);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private String formatDate(Date date) {
        if (date == null) {
            return "-";
        }
        return LocalDateTime.ofInstant(date.toInstant(), DEFAULT_ZONE_ID).toLocalDate().toString();
    }

    private String formatNumber(BigDecimal value) {
        return Optional.ofNullable(value).orElse(BigDecimal.ZERO).stripTrailingZeros().toPlainString();
    }

    private String percentageText(BigDecimal rate) {
        BigDecimal normalized = Optional.ofNullable(rate).orElse(BigDecimal.ZERO);
        return normalized.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "%";
    }

    private String coverageRateText(int total, int collected) {
        if (total <= 0) {
            return "0%";
        }
        return BigDecimal.valueOf(collected)
            .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .setScale(1, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString() + "%";
    }

    @SafeVarargs
    private final <T> T firstNonNull(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private RbTrack getTrack(String trackId) {
        if (isBlank(trackId)) {
            return null;
        }
        return trackService.getById(trackId);
    }

    private RbAccount pickAccount(String trackId) {
        LambdaQueryWrapper<RbAccount> wrapper = new LambdaQueryWrapper<RbAccount>()
            .eq(RbAccount::getStatus, RedbookStatusConstant.ACTIVE)
            .orderByDesc(RbAccount::getUpdateTime);
        if (!isBlank(trackId)) {
            wrapper.eq(RbAccount::getPrimaryTrackId, trackId);
        }
        wrapper.last("limit 1");
        RbAccount account = accountService.getOne(wrapper, false);
        if (account != null || isBlank(trackId)) {
            return account;
        }
        return accountService.lambdaQuery()
            .eq(RbAccount::getStatus, RedbookStatusConstant.ACTIVE)
            .orderByDesc(RbAccount::getUpdateTime)
            .last("limit 1")
            .one();
    }

    private BigDecimal average(List<BigDecimal> values, int scale) {
        List<BigDecimal> validValues = values.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (validValues.isEmpty()) {
            return BigDecimal.ZERO.setScale(scale, RoundingMode.HALF_UP);
        }
        BigDecimal sum = validValues.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(validValues.size()), scale, RoundingMode.HALF_UP);
    }

    private BigDecimal safeScore(BigDecimal primary, BigDecimal fallback) {
        return Optional.ofNullable(primary).orElse(fallback);
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private String defaultText(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isPublishedStatus(String publishStatus) {
        return RedbookStatusConstant.PUBLISH_PUBLISHED.equals(publishStatus)
            || RedbookStatusConstant.PUBLISH_DATA_COLLECTED.equals(publishStatus);
    }

    private String nextMetricCollectNode(List<RbNoteMetric> existingMetrics) {
        List<String> existingNodes = existingMetrics.stream()
            .map(RbNoteMetric::getCollectNode)
            .filter(item -> !isBlank(item))
            .collect(Collectors.toList());
        return METRIC_COLLECT_NODES.stream()
            .filter(item -> !existingNodes.contains(item))
            .findFirst()
            .orElse("");
    }

    private String formatDateTime(Date date) {
        if (date == null) {
            return "待定";
        }
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), DEFAULT_ZONE_ID);
        return localDateTime.toLocalDate() + " " + localDateTime.toLocalTime().withSecond(0).withNano(0);
    }

    private String escapeJson(String text) {
        return defaultText(text, "").replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
