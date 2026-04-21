package org.jeecg.modules.redbook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.redbook.entity.RbHotspot;
import org.jeecg.modules.redbook.entity.RbReviewReport;
import org.jeecg.modules.redbook.model.req.RedbookIdRequest;
import org.jeecg.modules.redbook.model.req.RedbookReviewGenerateRequest;
import org.jeecg.modules.redbook.service.IRbReviewReportService;
import org.jeecg.modules.redbook.service.IRedbookWorkflowService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import jakarta.annotation.Resource;
import java.util.List;

@Tag(name = "RedBook复盘报告")
@RestController
@RequestMapping("/redbook/reviewReport")
public class RbReviewReportController extends RedbookCrudController<RbReviewReport, IRbReviewReportService> {
    @Resource
    private IRedbookWorkflowService redbookWorkflowService;

    @GetMapping(value = "/list")
    @Operation(summary = "复盘报告分页列表")
    public Result<?> list(RbReviewReport entity, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        return queryPageList(entity, pageNo, pageSize, req);
    }

    @PostMapping(value = "/add")
    @AutoLog(value = "新增复盘报告")
    @RequiresPermissions("redbook:reviewReport:add")
    public Result<?> add(@RequestBody RbReviewReport entity) {
        return saveEntity(entity);
    }

    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @AutoLog(value = "编辑复盘报告")
    @RequiresPermissions("redbook:reviewReport:edit")
    public Result<?> edit(@RequestBody RbReviewReport entity) {
        return updateEntity(entity);
    }

    @DeleteMapping(value = "/delete")
    @AutoLog(value = "删除复盘报告")
    @RequiresPermissions("redbook:reviewReport:delete")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        return removeEntity(id);
    }

    @DeleteMapping(value = "/deleteBatch")
    @RequiresPermissions("redbook:reviewReport:deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        return removeBatch(ids);
    }

    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id") String id) {
        return queryEntityById(id);
    }

    @RequestMapping(value = "/exportXls")
    @RequiresPermissions("redbook:reviewReport:exportXls")
    public ModelAndView exportXls(HttpServletRequest request, RbReviewReport entity) {
        return exportExcel(request, entity, RbReviewReport.class, "复盘报告");
    }

    @PostMapping(value = "/importExcel")
    @RequiresPermissions("redbook:reviewReport:importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return importExcelData(request, response, RbReviewReport.class);
    }

    @PostMapping(value = "/generate")
    @AutoLog(value = "生成复盘报告")
    @RequiresPermissions("redbook:reviewReport:generate")
    @Operation(summary = "基于周期数据生成复盘报告")
    public Result<RbReviewReport> generate(@RequestBody RedbookIdRequest request) {
        RbReviewReport reviewReport = redbookWorkflowService.generateReviewReport(request.getId());
        return Result.OK("复盘报告已生成", reviewReport);
    }

    @PostMapping(value = "/generateScoped")
    @AutoLog(value = "按范围生成复盘报告")
    @RequiresPermissions("redbook:reviewReport:generateScoped")
    @Operation(summary = "按时间、赛道、账号范围直接生成复盘报告")
    public Result<RbReviewReport> generateScoped(@RequestBody RedbookReviewGenerateRequest request) {
        RbReviewReport report = new RbReviewReport();
        report.setId(request.getId());
        report.setReportName(request.getReportName());
        report.setTrackId(request.getTrackId());
        report.setAccountId(request.getAccountId());
        report.setPeriodStart(request.getPeriodStart());
        report.setPeriodEnd(request.getPeriodEnd());
        RbReviewReport reviewReport = redbookWorkflowService.generateReviewReportByScope(report);
        return Result.OK("复盘报告已生成", reviewReport);
    }

    @PostMapping(value = "/createHotspots")
    @AutoLog(value = "复盘报告回流热点池")
    @RequiresPermissions("redbook:reviewReport:createHotspots")
    @Operation(summary = "将下一轮选题建议回流到热点池")
    public Result<List<RbHotspot>> createHotspots(@RequestBody RedbookIdRequest request) {
        List<RbHotspot> hotspots = redbookWorkflowService.createHotspotsFromReviewReport(request.getId());
        return Result.OK("已回流 " + hotspots.size() + " 条选题到热点池", hotspots);
    }
}
