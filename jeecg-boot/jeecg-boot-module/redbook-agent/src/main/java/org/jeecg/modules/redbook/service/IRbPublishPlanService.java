package org.jeecg.modules.redbook.service;

import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.redbook.entity.RbPublishPlan;

import java.util.Date;

public interface IRbPublishPlanService extends JeecgService<RbPublishPlan> {
    RbPublishPlan createPlan(RbPublishPlan entity);

    RbPublishPlan updatePlan(RbPublishPlan entity);

    RbPublishPlan delayPlan(String id, Date plannedPublishTime, String remark);

    RbPublishPlan cancelPlan(String id, String remark);

    RbPublishPlan restorePending(String id, Date plannedPublishTime, String remark);

    RbPublishPlan updateNoteUrl(String id, String noteUrl, String remark);
}
