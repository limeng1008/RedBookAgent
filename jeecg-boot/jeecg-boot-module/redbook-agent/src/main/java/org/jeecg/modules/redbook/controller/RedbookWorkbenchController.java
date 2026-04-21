package org.jeecg.modules.redbook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.redbook.service.IRedbookWorkflowService;
import org.jeecg.modules.redbook.vo.RedbookReviewDashboardVO;
import org.jeecg.modules.redbook.vo.RedbookWorkbenchOverviewVO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.Date;

@Tag(name = "RedBook工作台")
@RestController
@RequestMapping("/redbook/workbench")
public class RedbookWorkbenchController {
    @Resource
    private IRedbookWorkflowService redbookWorkflowService;

    @GetMapping(value = "/overview")
    @Operation(summary = "获取小红书运营工作台概览")
    @RequiresPermissions("redbook:workbench:overview")
    public Result<RedbookWorkbenchOverviewVO> overview() {
        return Result.OK(redbookWorkflowService.getWorkbenchOverview());
    }

    @GetMapping(value = "/reviewDashboard")
    @Operation(summary = "获取小红书复盘看板")
    @RequiresPermissions("redbook:workbench:reviewDashboard")
    public Result<RedbookReviewDashboardVO> reviewDashboard(
        @RequestParam(value = "periodStart", required = false)
        @DateTimeFormat(pattern = "yyyy-MM-dd") Date periodStart,
        @RequestParam(value = "periodEnd", required = false)
        @DateTimeFormat(pattern = "yyyy-MM-dd") Date periodEnd,
        @RequestParam(value = "trackId", required = false) String trackId,
        @RequestParam(value = "accountId", required = false) String accountId
    ) {
        return Result.OK(redbookWorkflowService.getReviewDashboard(periodStart, periodEnd, trackId, accountId));
    }
}
