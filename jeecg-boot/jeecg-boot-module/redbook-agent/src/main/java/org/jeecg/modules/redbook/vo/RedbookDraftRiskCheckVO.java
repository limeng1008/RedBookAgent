package org.jeecg.modules.redbook.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Schema(description = "草稿风险检查结果")
public class RedbookDraftRiskCheckVO {
    @Schema(description = "草稿ID")
    private String draftId;

    @Schema(description = "草稿标题")
    private String title;

    @Schema(description = "是否通过")
    private Boolean passed;

    @Schema(description = "是否建议重新人工审核")
    private Boolean requiresManualReview;

    @Schema(description = "风险等级")
    private String riskLevel;

    @Schema(description = "命中数量")
    private Long hitCount;

    @Schema(description = "命中字段")
    private List<String> matchedFields;

    @Schema(description = "替换建议列表")
    private List<String> replacementSuggestions;

    @Schema(description = "检查摘要")
    private String summary;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "检查时间")
    private Date checkedTime;

    @Schema(description = "命中明细")
    private List<RedbookDraftRiskHitVO> hits;
}
