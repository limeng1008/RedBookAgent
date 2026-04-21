package org.jeecg.modules.redbook.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.jeecg.modules.redbook.constant.RedbookStatusConstant;
import org.jeecg.modules.redbook.entity.RbNoteMetric;
import org.jeecg.modules.redbook.entity.RbPublishPlan;
import org.jeecg.modules.redbook.mapper.RbNoteMetricMapper;
import org.jeecg.modules.redbook.service.IRbNoteMetricService;
import org.jeecg.modules.redbook.service.IRbPublishPlanService;
import org.jeecg.modules.redbook.vo.RedbookMetricCompletenessVO;
import org.jeecg.modules.redbook.vo.RedbookMetricNodeStatusVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RbNoteMetricServiceImpl extends ServiceImpl<RbNoteMetricMapper, RbNoteMetric> implements IRbNoteMetricService {
    private static final List<String> REQUIRED_COLLECT_NODES = Arrays.asList("2h", "24h", "72h", "7d");

    @Resource
    private IRbPublishPlanService publishPlanService;

    @Override
    public RbNoteMetric normalizeMetric(RbNoteMetric entity) {
        if (entity == null) {
            throw new IllegalArgumentException("数据回收记录不能为空");
        }
        if (isBlank(entity.getPublishPlanId())) {
            throw new IllegalArgumentException("发布计划ID不能为空");
        }
        RbPublishPlan publishPlan = publishPlanService.getById(entity.getPublishPlanId());
        if (publishPlan == null) {
            throw new IllegalArgumentException("发布计划不存在");
        }
        if (!isPublishedStatus(publishPlan.getPublishStatus())) {
            throw new IllegalArgumentException("请先将发布计划标记为已发布，再录入数据回收");
        }

        entity.setCollectNode(normalizeCollectNode(entity.getCollectNode()));
        if (isBlank(entity.getNoteDraftId())) {
            entity.setNoteDraftId(publishPlan.getDraftId());
        }
        normalizeCountFields(entity);
        long views = entity.getViews();
        long likes = entity.getLikes();
        long collects = entity.getCollects();
        long comments = entity.getComments();
        long shares = entity.getShares();

        if (entity.getCollectTime() == null) {
            entity.setCollectTime(new Date());
        }

        if (views <= 0) {
            entity.setInteractionRate(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
            entity.setCollectRate(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
            entity.setCommentRate(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
            return entity;
        }

        BigDecimal base = BigDecimal.valueOf(views);
        entity.setInteractionRate(BigDecimal.valueOf(likes + collects + comments + shares).divide(base, 4, RoundingMode.HALF_UP));
        entity.setCollectRate(BigDecimal.valueOf(collects).divide(base, 4, RoundingMode.HALF_UP));
        entity.setCommentRate(BigDecimal.valueOf(comments).divide(base, 4, RoundingMode.HALF_UP));
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<RbNoteMetric> saveBatchMetrics(String publishPlanId, List<RbNoteMetric> metrics) {
        if (isBlank(publishPlanId)) {
            throw new IllegalArgumentException("发布计划ID不能为空");
        }
        if (metrics == null || metrics.isEmpty()) {
            throw new IllegalArgumentException("请至少录入一条数据回收记录");
        }

        Map<String, RbNoteMetric> existingMetricByNode = lambdaQuery()
            .eq(RbNoteMetric::getPublishPlanId, publishPlanId)
            .list()
            .stream()
            .filter(Objects::nonNull)
            .filter(metric -> !isBlank(metric.getCollectNode()))
            .collect(Collectors.toMap(
                metric -> normalizeExistingCollectNode(metric.getCollectNode()),
                metric -> metric,
                this::pickLaterMetric,
                LinkedHashMap::new
            ));

        Set<String> incomingNodeSet = new LinkedHashSet<>();
        List<RbNoteMetric> savedMetrics = new ArrayList<>();
        for (RbNoteMetric item : metrics) {
            if (item == null) {
                continue;
            }
            item.setPublishPlanId(publishPlanId);
            String collectNode = normalizeCollectNode(item.getCollectNode());
            item.setCollectNode(collectNode);
            if (!incomingNodeSet.add(collectNode)) {
                throw new IllegalArgumentException("采集节点重复：" + collectNode);
            }

            RbNoteMetric oldMetric = null;
            if (!isBlank(item.getId())) {
                oldMetric = getById(item.getId());
                if (oldMetric == null) {
                    throw new IllegalArgumentException("数据回收记录不存在：" + item.getId());
                }
                if (!publishPlanId.equals(oldMetric.getPublishPlanId())) {
                    throw new IllegalArgumentException("数据回收记录不属于当前发布计划：" + item.getId());
                }
            } else {
                oldMetric = existingMetricByNode.get(collectNode);
                if (oldMetric != null) {
                    item.setId(oldMetric.getId());
                }
            }

            RbNoteMetric normalized = normalizeMetric(item);
            saveOrUpdate(normalized);
            savedMetrics.add(getById(normalized.getId()));
        }
        refreshPublishPlanStatus(publishPlanId);
        return savedMetrics;
    }

    @Override
    public RedbookMetricCompletenessVO getMetricCompleteness(String publishPlanId) {
        if (isBlank(publishPlanId)) {
            throw new IllegalArgumentException("发布计划ID不能为空");
        }
        RbPublishPlan publishPlan = publishPlanService.getById(publishPlanId);
        if (publishPlan == null) {
            throw new IllegalArgumentException("未找到对应发布计划");
        }

        Map<String, RbNoteMetric> latestMetricByNode = new LinkedHashMap<>();
        lambdaQuery()
            .eq(RbNoteMetric::getPublishPlanId, publishPlanId)
            .list()
            .stream()
            .filter(Objects::nonNull)
            .filter(metric -> REQUIRED_COLLECT_NODES.contains(normalizeExistingCollectNode(metric.getCollectNode())))
            .forEach(metric -> latestMetricByNode.merge(
                normalizeExistingCollectNode(metric.getCollectNode()),
                metric,
                this::pickLaterMetric
            ));

        List<String> existingNodes = REQUIRED_COLLECT_NODES.stream()
            .filter(latestMetricByNode::containsKey)
            .collect(Collectors.toList());
        List<String> missingNodes = REQUIRED_COLLECT_NODES.stream()
            .filter(node -> !latestMetricByNode.containsKey(node))
            .collect(Collectors.toList());
        List<RedbookMetricNodeStatusVO> nodeStatusList = new ArrayList<>();
        for (String collectNode : REQUIRED_COLLECT_NODES) {
            RbNoteMetric metric = latestMetricByNode.get(collectNode);
            RedbookMetricNodeStatusVO nodeStatus = new RedbookMetricNodeStatusVO();
            nodeStatus.setMetricId(metric == null ? null : metric.getId());
            nodeStatus.setCollectNode(collectNode);
            nodeStatus.setFilled(metric != null);
            nodeStatus.setViews(metric == null ? 0L : defaultLong(metric.getViews()));
            nodeStatus.setInteractionRate(metric == null ? BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP) : defaultDecimal(metric.getInteractionRate()));
            nodeStatus.setCollectTime(metric == null ? null : metric.getCollectTime());
            nodeStatusList.add(nodeStatus);
        }

        long filledNodeCount = existingNodes.size();
        long requiredNodeCount = REQUIRED_COLLECT_NODES.size();
        boolean completed = filledNodeCount >= requiredNodeCount;

        RedbookMetricCompletenessVO result = new RedbookMetricCompletenessVO();
        result.setPublishPlanId(publishPlan.getId());
        result.setDraftId(publishPlan.getDraftId());
        result.setPublishStatus(publishPlan.getPublishStatus());
        result.setCompleted(completed);
        result.setFilledNodeCount(filledNodeCount);
        result.setRequiredNodeCount(requiredNodeCount);
        result.setCoverageRate(requiredNodeCount <= 0
            ? BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP)
            : BigDecimal.valueOf(filledNodeCount).divide(BigDecimal.valueOf(requiredNodeCount), 4, RoundingMode.HALF_UP));
        result.setRequiredNodes(new ArrayList<>(REQUIRED_COLLECT_NODES));
        result.setExistingNodes(existingNodes);
        result.setMissingNodes(missingNodes);
        result.setLatestCollectTime(latestMetricByNode.values().stream()
            .map(this::resolveMetricTime)
            .filter(Objects::nonNull)
            .max(Date::compareTo)
            .orElse(null));
        result.setSummary(buildCompletenessSummary(filledNodeCount, requiredNodeCount, missingNodes));
        result.setNodeStatusList(nodeStatusList);
        return result;
    }

    @Override
    public void refreshPublishPlanStatus(String publishPlanId) {
        if (isBlank(publishPlanId)) {
            return;
        }
        RbPublishPlan publishPlan = publishPlanService.getById(publishPlanId);
        if (publishPlan == null || !isPublishedStatus(publishPlan.getPublishStatus())) {
            return;
        }
        RedbookMetricCompletenessVO completeness = getMetricCompleteness(publishPlanId);
        String targetStatus = Boolean.TRUE.equals(completeness.getCompleted())
            ? RedbookStatusConstant.PUBLISH_DATA_COLLECTED
            : RedbookStatusConstant.PUBLISH_PUBLISHED;
        if (!targetStatus.equals(publishPlan.getPublishStatus())) {
            publishPlan.setPublishStatus(targetStatus);
            publishPlanService.updateById(publishPlan);
        }
    }

    @Override
    public void refreshPublishPlanStatuses(Collection<String> publishPlanIds) {
        if (publishPlanIds == null || publishPlanIds.isEmpty()) {
            return;
        }
        LinkedHashSet<String> uniqueIds = publishPlanIds.stream()
            .filter(item -> !isBlank(item))
            .collect(Collectors.toCollection(LinkedHashSet::new));
        uniqueIds.forEach(this::refreshPublishPlanStatus);
    }

    private String buildCompletenessSummary(long filledNodeCount, long requiredNodeCount, List<String> missingNodes) {
        if (filledNodeCount <= 0) {
            return "当前 2h / 24h / 72h / 7d 四个节点都还未录入，建议先补首轮 2h 数据。";
        }
        if (missingNodes == null || missingNodes.isEmpty()) {
            return "四个关键数据回收节点已全部录入，当前发布计划可以视为完成数据闭环。";
        }
        return "当前已完成 " + filledNodeCount + "/" + requiredNodeCount + " 个关键节点，仍缺少 "
            + String.join("、", missingNodes) + "，建议继续补录。";
    }

    private RbNoteMetric pickLaterMetric(RbNoteMetric left, RbNoteMetric right) {
        return Comparator.comparing(this::resolveMetricTime, Comparator.nullsLast(Date::compareTo))
            .compare(left, right) >= 0 ? left : right;
    }

    private Date resolveMetricTime(RbNoteMetric metric) {
        if (metric == null) {
            return null;
        }
        if (metric.getCollectTime() != null) {
            return metric.getCollectTime();
        }
        if (metric.getUpdateTime() != null) {
            return metric.getUpdateTime();
        }
        return metric.getCreateTime();
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP) : value;
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private void normalizeCountFields(RbNoteMetric entity) {
        entity.setImpressions(defaultLong(entity.getImpressions()));
        entity.setViews(defaultLong(entity.getViews()));
        entity.setLikes(defaultLong(entity.getLikes()));
        entity.setCollects(defaultLong(entity.getCollects()));
        entity.setComments(defaultLong(entity.getComments()));
        entity.setShares(defaultLong(entity.getShares()));
        entity.setFollowers(defaultLong(entity.getFollowers()));
        entity.setMessages(defaultLong(entity.getMessages()));
        entity.setLeads(defaultLong(entity.getLeads()));
        entity.setConversions(defaultLong(entity.getConversions()));
    }

    private String normalizeCollectNode(String collectNode) {
        String normalized = normalizeExistingCollectNode(collectNode);
        if (!REQUIRED_COLLECT_NODES.contains(normalized)) {
            throw new IllegalArgumentException("采集节点非法，仅支持 2h / 24h / 72h / 7d");
        }
        return normalized;
    }

    private String normalizeExistingCollectNode(String collectNode) {
        return collectNode == null ? "" : collectNode.trim().toLowerCase();
    }

    private boolean isPublishedStatus(String publishStatus) {
        return RedbookStatusConstant.PUBLISH_PUBLISHED.equals(publishStatus)
            || RedbookStatusConstant.PUBLISH_DATA_COLLECTED.equals(publishStatus);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
