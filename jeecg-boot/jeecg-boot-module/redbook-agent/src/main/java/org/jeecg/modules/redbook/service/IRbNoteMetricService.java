package org.jeecg.modules.redbook.service;

import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.redbook.entity.RbNoteMetric;
import org.jeecg.modules.redbook.vo.RedbookMetricCompletenessVO;

import java.util.Collection;
import java.util.List;

public interface IRbNoteMetricService extends JeecgService<RbNoteMetric> {
    RbNoteMetric normalizeMetric(RbNoteMetric entity);

    List<RbNoteMetric> saveBatchMetrics(String publishPlanId, List<RbNoteMetric> metrics);

    RedbookMetricCompletenessVO getMetricCompleteness(String publishPlanId);

    void refreshPublishPlanStatus(String publishPlanId);

    void refreshPublishPlanStatuses(Collection<String> publishPlanIds);
}
