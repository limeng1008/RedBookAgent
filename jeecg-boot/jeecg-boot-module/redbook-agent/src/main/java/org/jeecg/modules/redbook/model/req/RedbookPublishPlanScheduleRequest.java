package org.jeecg.modules.redbook.model.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@Schema(description = "RedBook发布计划时间调整请求")
public class RedbookPublishPlanScheduleRequest {
    @Schema(description = "发布计划ID")
    private String id;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "计划发布时间")
    private Date plannedPublishTime;

    @Schema(description = "备注")
    private String remark;
}
