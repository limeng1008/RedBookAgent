package org.jeecg.modules.redbook.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "发布计划数据回收完整性")
public class RedbookMetricCompletenessVO {
    @Schema(description = "发布计划ID")
    private String publishPlanId;

    @Schema(description = "草稿ID")
    private String draftId;

    @Schema(description = "发布状态")
    private String publishStatus;

    @Schema(description = "是否完整")
    private Boolean completed;

    @Schema(description = "已完成节点数")
    private Long filledNodeCount;

    @Schema(description = "必填节点数")
    private Long requiredNodeCount;

    @Schema(description = "覆盖率")
    private BigDecimal coverageRate;

    @Schema(description = "必填节点")
    private List<String> requiredNodes;

    @Schema(description = "已存在节点")
    private List<String> existingNodes;

    @Schema(description = "缺失节点")
    private List<String> missingNodes;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "最近一次采集时间")
    private Date latestCollectTime;

    @Schema(description = "完整性摘要")
    private String summary;

    @Schema(description = "节点状态列表")
    private List<RedbookMetricNodeStatusVO> nodeStatusList;
}
