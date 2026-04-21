package org.jeecg.modules.redbook.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "数据回收节点状态")
public class RedbookMetricNodeStatusVO {
    @Schema(description = "数据回收记录ID")
    private String metricId;

    @Schema(description = "采集节点")
    private String collectNode;

    @Schema(description = "是否已录入")
    private Boolean filled;

    @Schema(description = "阅读/播放量")
    private Long views;

    @Schema(description = "互动率")
    private BigDecimal interactionRate;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "采集时间")
    private Date collectTime;
}
