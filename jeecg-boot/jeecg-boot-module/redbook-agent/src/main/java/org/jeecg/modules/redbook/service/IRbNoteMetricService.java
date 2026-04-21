package org.jeecg.modules.redbook.service;

import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.redbook.entity.RbNoteMetric;
import org.jeecg.modules.redbook.vo.RedbookMetricCompletenessVO;

import java.util.Collection;

public interface IRbNoteMetricService extends JeecgService<RbNoteMetric> {
    RbNoteMetric normalizeMetric(RbNoteMetric entity);

    RedbookMetricCompletenessVO getMetricCompleteness(String publishPlanId);

    void refreshPublishPlanStatus(String publishPlanId);

    void refreshPublishPlanStatuses(Collection<String> publishPlanIds);
}
