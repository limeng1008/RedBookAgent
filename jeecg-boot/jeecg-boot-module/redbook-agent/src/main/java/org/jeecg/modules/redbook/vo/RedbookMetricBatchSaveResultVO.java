package org.jeecg.modules.redbook.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jeecg.modules.redbook.entity.RbNoteMetric;

import java.util.List;

@Data
@Schema(description = "数据回收批量保存结果")
public class RedbookMetricBatchSaveResultVO {
    @Schema(description = "发布计划ID")
    private String publishPlanId;

    @Schema(description = "已保存的数据回收记录")
    private List<RbNoteMetric> records;

    @Schema(description = "保存后的完整性")
    private RedbookMetricCompletenessVO completeness;
}
