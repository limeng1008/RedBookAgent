package org.jeecg.modules.redbook.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "RedBook通用备注请求")
public class RedbookRemarkRequest {
    @Schema(description = "业务ID")
    private String id;

    @Schema(description = "备注")
    private String remark;
}
