package org.jeecg.modules.redbook.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.redbook.constant.RedbookStatusConstant;
import org.jeecg.modules.redbook.entity.RbPublishPlan;
import org.jeecg.modules.redbook.mapper.RbPublishPlanMapper;
import org.jeecg.modules.redbook.service.IRbPublishPlanService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Set;

@Service
public class RbPublishPlanServiceImpl extends ServiceImpl<RbPublishPlanMapper, RbPublishPlan> implements IRbPublishPlanService {
    private static final Set<String> PUBLISH_STATUS_VALUES = Set.of(
        RedbookStatusConstant.PUBLISH_PENDING,
        RedbookStatusConstant.PUBLISH_PUBLISHED,
        RedbookStatusConstant.PUBLISH_DELAYED,
        RedbookStatusConstant.PUBLISH_CANCELED,
        RedbookStatusConstant.PUBLISH_DATA_COLLECTED
    );

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RbPublishPlan createPlan(RbPublishPlan entity) {
        normalizeForCreate(entity);
        save(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RbPublishPlan updatePlan(RbPublishPlan entity) {
        if (isBlank(entity.getId())) {
            throw new IllegalArgumentException("发布计划ID不能为空");
        }
        RbPublishPlan oldPlan = getById(entity.getId());
        if (oldPlan == null) {
            throw new IllegalArgumentException("未找到对应发布计划");
        }
        normalizeForUpdate(oldPlan, entity);
        validateTransition(oldPlan.getPublishStatus(), entity.getPublishStatus());
        updateById(entity);
        return getById(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RbPublishPlan delayPlan(String id, Date plannedPublishTime, String remark) {
        RbPublishPlan plan = getRequiredPlan(id);
        ensureNotFinished(plan, "已发布或已回收数据的计划不允许延期");
        plan.setPublishStatus(RedbookStatusConstant.PUBLISH_DELAYED);
        if (plannedPublishTime != null) {
            plan.setPlannedPublishTime(plannedPublishTime);
        }
        if (!isBlank(remark)) {
            plan.setRemark(remark.trim());
        }
        updateById(plan);
        return plan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RbPublishPlan cancelPlan(String id, String remark) {
        RbPublishPlan plan = getRequiredPlan(id);
        ensureNotFinished(plan, "已发布或已回收数据的计划不允许取消");
        plan.setPublishStatus(RedbookStatusConstant.PUBLISH_CANCELED);
        if (!isBlank(remark)) {
            plan.setRemark(remark.trim());
        }
        updateById(plan);
        return plan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RbPublishPlan restorePending(String id, Date plannedPublishTime, String remark) {
        RbPublishPlan plan = getRequiredPlan(id);
        if (!RedbookStatusConstant.PUBLISH_DELAYED.equals(plan.getPublishStatus())
            && !RedbookStatusConstant.PUBLISH_CANCELED.equals(plan.getPublishStatus())) {
            throw new IllegalArgumentException("仅延期或已取消的计划可恢复为待发布");
        }
        plan.setPublishStatus(RedbookStatusConstant.PUBLISH_PENDING);
        if (plannedPublishTime != null) {
            plan.setPlannedPublishTime(plannedPublishTime);
        } else if (plan.getPlannedPublishTime() == null) {
            plan.setPlannedPublishTime(new Date());
        }
        if (!isBlank(remark)) {
            plan.setRemark(remark.trim());
        }
        updateById(plan);
        return plan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RbPublishPlan updateNoteUrl(String id, String noteUrl, String remark) {
        if (isBlank(noteUrl)) {
            throw new IllegalArgumentException("笔记链接不能为空");
        }
        RbPublishPlan plan = getRequiredPlan(id);
        plan.setNoteUrl(noteUrl.trim());
        if (!isBlank(remark)) {
            plan.setRemark(remark.trim());
        }
        updateById(plan);
        return plan;
    }

    private void normalizeForCreate(RbPublishPlan entity) {
        if (isBlank(entity.getPublishStatus())) {
            entity.setPublishStatus(RedbookStatusConstant.PUBLISH_PENDING);
        }
        validateStatus(entity.getPublishStatus());
        if (isBlank(entity.getDraftId())) {
            throw new IllegalArgumentException("草稿ID不能为空");
        }
        if (isBlank(entity.getAccountId())) {
            throw new IllegalArgumentException("账号ID不能为空");
        }
        if (entity.getPlannedPublishTime() == null) {
            throw new IllegalArgumentException("计划发布时间不能为空");
        }
        if (RedbookStatusConstant.PUBLISH_PUBLISHED.equals(entity.getPublishStatus())
            || RedbookStatusConstant.PUBLISH_DATA_COLLECTED.equals(entity.getPublishStatus())) {
            throw new IllegalArgumentException("新建发布计划不能直接设置为已发布或已回收数据");
        }
    }

    private void normalizeForUpdate(RbPublishPlan oldPlan, RbPublishPlan entity) {
        if (isBlank(entity.getDraftId())) {
            entity.setDraftId(oldPlan.getDraftId());
        }
        if (isBlank(entity.getAccountId())) {
            entity.setAccountId(oldPlan.getAccountId());
        }
        if (entity.getPlannedPublishTime() == null) {
            entity.setPlannedPublishTime(oldPlan.getPlannedPublishTime());
        }
        if (isBlank(entity.getPublishStatus())) {
            entity.setPublishStatus(oldPlan.getPublishStatus());
        }
        validateStatus(entity.getPublishStatus());
    }

    private RbPublishPlan getRequiredPlan(String id) {
        if (isBlank(id)) {
            throw new IllegalArgumentException("发布计划ID不能为空");
        }
        RbPublishPlan plan = getById(id);
        if (plan == null) {
            throw new IllegalArgumentException("未找到对应发布计划");
        }
        return plan;
    }

    private void ensureNotFinished(RbPublishPlan plan, String message) {
        if (RedbookStatusConstant.PUBLISH_PUBLISHED.equals(plan.getPublishStatus())
            || RedbookStatusConstant.PUBLISH_DATA_COLLECTED.equals(plan.getPublishStatus())) {
            throw new IllegalArgumentException(message);
        }
    }

    private void validateTransition(String oldStatus, String newStatus) {
        if (RedbookStatusConstant.PUBLISH_PUBLISHED.equals(oldStatus)
            && !RedbookStatusConstant.PUBLISH_PUBLISHED.equals(newStatus)
            && !RedbookStatusConstant.PUBLISH_DATA_COLLECTED.equals(newStatus)) {
            throw new IllegalArgumentException("已发布计划不允许回退到未发布状态");
        }
        if (RedbookStatusConstant.PUBLISH_DATA_COLLECTED.equals(oldStatus)
            && !RedbookStatusConstant.PUBLISH_DATA_COLLECTED.equals(newStatus)) {
            throw new IllegalArgumentException("已回收数据的计划不允许回退状态");
        }
    }

    private void validateStatus(String status) {
        if (!PUBLISH_STATUS_VALUES.contains(status)) {
            throw new IllegalArgumentException("发布状态非法：" + status);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
