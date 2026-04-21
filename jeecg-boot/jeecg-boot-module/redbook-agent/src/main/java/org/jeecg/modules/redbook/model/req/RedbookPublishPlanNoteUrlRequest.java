package org.jeecg.modules.redbook.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "RedBook发布计划补录笔记链接请求")
public class RedbookPublishPlanNoteUrlRequest {
    @Schema(description = "发布计划ID")
    private String id;

    @Schema(description = "小红书笔记链接")
    private String noteUrl;

    @Schema(description = "备注")
    private String remark;
}
