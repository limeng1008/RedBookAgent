package org.jeecg.modules.redbook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.redbook.entity.RbNoteMetric;
import org.jeecg.modules.redbook.entity.RbPublishPlan;
import org.jeecg.modules.redbook.model.req.RedbookIdRequest;
import org.jeecg.modules.redbook.model.req.RedbookPublishPlanNoteUrlRequest;
import org.jeecg.modules.redbook.model.req.RedbookPublishPlanScheduleRequest;
import org.jeecg.modules.redbook.model.req.RedbookRemarkRequest;
import org.jeecg.modules.redbook.service.IRbPublishPlanService;
import org.jeecg.modules.redbook.service.IRedbookWorkflowService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import jakarta.annotation.Resource;

@Tag(name = "RedBook发布计划")
@RestController
@RequestMapping("/redbook/publishPlan")
public class RbPublishPlanController extends RedbookCrudController<RbPublishPlan, IRbPublishPlanService> {
    @Resource
    private IRedbookWorkflowService redbookWorkflowService;

    @GetMapping(value = "/list")
    @Operation(summary = "发布计划分页列表")
    public Result<?> list(RbPublishPlan entity, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        return queryPageList(entity, pageNo, pageSize, req);
    }

    @PostMapping(value = "/add")
    @AutoLog(value = "新增发布计划")
    @RequiresPermissions("redbook:publishPlan:add")
    public Result<?> add(@RequestBody RbPublishPlan entity) {
        return Result.OK("添加成功！", service.createPlan(entity));
    }

    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @AutoLog(value = "编辑发布计划")
    @RequiresPermissions("redbook:publishPlan:edit")
    public Result<?> edit(@RequestBody RbPublishPlan entity) {
        return Result.OK("编辑成功！", service.updatePlan(entity));
    }

    @DeleteMapping(value = "/delete")
    @AutoLog(value = "删除发布计划")
    @RequiresPermissions("redbook:publishPlan:delete")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        return removeEntity(id);
    }

    @DeleteMapping(value = "/deleteBatch")
    @RequiresPermissions("redbook:publishPlan:deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        return removeBatch(ids);
    }

    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id") String id) {
        return queryEntityById(id);
    }

    @RequestMapping(value = "/exportXls")
    @RequiresPermissions("redbook:publishPlan:exportXls")
    public ModelAndView exportXls(HttpServletRequest request, RbPublishPlan entity) {
        return exportExcel(request, entity, RbPublishPlan.class, "发布计划");
    }

    @PostMapping(value = "/importExcel")
    @RequiresPermissions("redbook:publishPlan:importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return importExcelData(request, response, RbPublishPlan.class);
    }

    @PostMapping(value = "/markPublished")
    @AutoLog(value = "发布计划标记已发布")
    @RequiresPermissions("redbook:publishPlan:markPublished")
    @Operation(summary = "标记排期内容已发布")
    public Result<RbPublishPlan> markPublished(@RequestBody RedbookIdRequest request) {
        RbPublishPlan publishPlan = redbookWorkflowService.markPublished(request.getId());
        return Result.OK("发布计划已标记为已发布", publishPlan);
    }

    @PostMapping(value = "/createMetric")
    @AutoLog(value = "发布计划生成数据回收记录")
    @RequiresPermissions("redbook:publishPlan:createMetric")
    @Operation(summary = "为已发布内容生成下一轮数据回收记录")
    public Result<RbNoteMetric> createMetric(@RequestBody RedbookIdRequest request) {
        RbNoteMetric metric = redbookWorkflowService.createMetricRecord(request.getId());
        return Result.OK("数据回收记录已生成", metric);
    }

    @PostMapping(value = "/delay")
    @AutoLog(value = "发布计划延期")
    @RequiresPermissions("redbook:publishPlan:delay")
    @Operation(summary = "发布计划延期")
    public Result<RbPublishPlan> delay(@RequestBody RedbookPublishPlanScheduleRequest request) {
        RbPublishPlan publishPlan = service.delayPlan(request.getId(), request.getPlannedPublishTime(), request.getRemark());
        return Result.OK("发布计划已延期", publishPlan);
    }

    @PostMapping(value = "/cancel")
    @AutoLog(value = "发布计划取消")
    @RequiresPermissions("redbook:publishPlan:cancel")
    @Operation(summary = "发布计划取消")
    public Result<RbPublishPlan> cancel(@RequestBody RedbookRemarkRequest request) {
        RbPublishPlan publishPlan = service.cancelPlan(request.getId(), request.getRemark());
        return Result.OK("发布计划已取消", publishPlan);
    }

    @PostMapping(value = "/restorePending")
    @AutoLog(value = "发布计划恢复待发布")
    @RequiresPermissions("redbook:publishPlan:restorePending")
    @Operation(summary = "发布计划恢复待发布")
    public Result<RbPublishPlan> restorePending(@RequestBody RedbookPublishPlanScheduleRequest request) {
        RbPublishPlan publishPlan = service.restorePending(request.getId(), request.getPlannedPublishTime(), request.getRemark());
        return Result.OK("发布计划已恢复为待发布", publishPlan);
    }

    @PostMapping(value = "/updateNoteUrl")
    @AutoLog(value = "发布计划补录笔记链接")
    @RequiresPermissions("redbook:publishPlan:updateNoteUrl")
    @Operation(summary = "发布计划补录笔记链接")
    public Result<RbPublishPlan> updateNoteUrl(@RequestBody RedbookPublishPlanNoteUrlRequest request) {
        RbPublishPlan publishPlan = service.updateNoteUrl(request.getId(), request.getNoteUrl(), request.getRemark());
        return Result.OK("笔记链接已更新", publishPlan);
    }
}
