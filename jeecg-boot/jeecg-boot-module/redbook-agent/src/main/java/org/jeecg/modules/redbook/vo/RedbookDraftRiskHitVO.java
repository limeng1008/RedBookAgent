package org.jeecg.modules.redbook.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "草稿风险命中项")
public class RedbookDraftRiskHitVO {
    @Schema(description = "命中词")
    private String word;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "风险等级")
    private String riskLevel;

    @Schema(description = "替换建议")
    private String replacementSuggestion;

    @Schema(description = "命中字段")
    private List<String> matchedFields;
}
