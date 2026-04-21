package org.jeecg.modules.redbook.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jeecg.modules.redbook.entity.RbNoteMetric;

import java.util.List;

@Data
@Schema(description = "RedBook数据回收批量保存请求")
public class RedbookMetricBatchSaveRequest {
    @Schema(description = "发布计划ID")
    private String publishPlanId;

    @Schema(description = "数据回收记录列表")
    private List<RbNoteMetric> metrics;
}
