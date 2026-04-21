package org.jeecg.modules.redbook.model.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@Schema(description = "RedBook复盘报告范围生成请求")
public class RedbookReviewGenerateRequest {
    @Schema(description = "报告ID，传入则更新后再生成")
    private String id;

    @Schema(description = "报告名称")
    private String reportName;

    @Schema(description = "赛道ID")
    private String trackId;

    @Schema(description = "账号ID")
    private String accountId;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "开始日期")
    private Date periodStart;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "结束日期")
    private Date periodEnd;
}
