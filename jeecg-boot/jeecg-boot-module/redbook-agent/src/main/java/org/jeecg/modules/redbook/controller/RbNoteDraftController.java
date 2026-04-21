package org.jeecg.modules.redbook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.redbook.entity.RbNoteDraft;
import org.jeecg.modules.redbook.entity.RbNoteDraftVersion;
import org.jeecg.modules.redbook.entity.RbPublishPlan;
import org.jeecg.modules.redbook.model.req.RedbookAuditRequest;
import org.jeecg.modules.redbook.model.req.RedbookIdRequest;
import org.jeecg.modules.redbook.service.IRbNoteDraftService;
import org.jeecg.modules.redbook.service.IRedbookWorkflowService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import jakarta.annotation.Resource;
import java.util.List;

@Tag(name = "RedBook笔记草稿")
@RestController
@RequestMapping("/redbook/noteDraft")
public class RbNoteDraftController extends RedbookCrudController<RbNoteDraft, IRbNoteDraftService> {
    @Resource
    private IRedbookWorkflowService redbookWorkflowService;

    @GetMapping(value = "/list")
    @Operation(summary = "笔记草稿分页列表")
    public Result<?> list(RbNoteDraft entity, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        return queryPageList(entity, pageNo, pageSize, req);
    }

    @PostMapping(value = "/add")
    @AutoLog(value = "新增笔记草稿")
    @RequiresPermissions("redbook:noteDraft:add")
    public Result<?> add(@RequestBody RbNoteDraft entity) {
        return Result.OK("添加成功！", service.createDraft(entity));
    }

    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @AutoLog(value = "编辑笔记草稿")
    @RequiresPermissions("redbook:noteDraft:edit")
    public Result<?> edit(@RequestBody RbNoteDraft entity) {
        return Result.OK("更新成功！", service.updateDraft(entity));
    }

    @DeleteMapping(value = "/delete")
    @AutoLog(value = "删除笔记草稿")
    @RequiresPermissions("redbook:noteDraft:delete")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        return removeEntity(id);
    }

    @DeleteMapping(value = "/deleteBatch")
    @RequiresPermissions("redbook:noteDraft:deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        return removeBatch(ids);
    }

    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id") String id) {
        return queryEntityById(id);
    }

    @RequestMapping(value = "/exportXls")
    @RequiresPermissions("redbook:noteDraft:exportXls")
    public ModelAndView exportXls(HttpServletRequest request, RbNoteDraft entity) {
        return exportExcel(request, entity, RbNoteDraft.class, "笔记草稿");
    }

    @PostMapping(value = "/importExcel")
    @RequiresPermissions("redbook:noteDraft:importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return importExcelData(request, response, RbNoteDraft.class);
    }

    @PostMapping(value = "/createPublishPlan")
    @AutoLog(value = "草稿加入发布计划")
    @RequiresPermissions("redbook:noteDraft:createPublishPlan")
    @Operation(summary = "基于草稿生成发布计划")
    public Result<RbPublishPlan> createPublishPlan(@RequestBody RedbookIdRequest request) {
        RbPublishPlan publishPlan = redbookWorkflowService.createPublishPlan(request.getId());
        return Result.OK("发布计划已生成", publishPlan);
    }

    @PostMapping(value = "/approve")
    @AutoLog(value = "草稿审核通过")
    @RequiresPermissions("redbook:noteDraft:approve")
    @Operation(summary = "草稿审核通过")
    public Result<RbNoteDraft> approve(@RequestBody RedbookAuditRequest request) {
        RbNoteDraft draft = service.approveDraft(request.getId(), request.getAuditOpinion());
        return Result.OK("草稿已审核通过", draft);
    }

    @PostMapping(value = "/reject")
    @AutoLog(value = "草稿审核退回")
    @RequiresPermissions("redbook:noteDraft:reject")
    @Operation(summary = "草稿审核退回")
    public Result<RbNoteDraft> reject(@RequestBody RedbookAuditRequest request) {
        RbNoteDraft draft = service.rejectDraft(request.getId(), request.getAuditOpinion());
        return Result.OK("草稿已退回修改", draft);
    }

    @GetMapping(value = "/versions")
    @Operation(summary = "查询草稿版本列表")
    @RequiresPermissions("redbook:noteDraft:versions")
    public Result<List<RbNoteDraftVersion>> versions(@RequestParam(name = "draftId") String draftId) {
        return Result.OK(service.listVersions(draftId));
    }

    @PostMapping(value = "/restoreVersion")
    @AutoLog(value = "恢复草稿版本")
    @RequiresPermissions("redbook:noteDraft:restoreVersion")
    @Operation(summary = "恢复草稿版本")
    public Result<RbNoteDraft> restoreVersion(@RequestBody RedbookIdRequest request) {
        RbNoteDraft draft = service.restoreVersion(request.getId());
        return Result.OK("草稿版本已恢复", draft);
    }
}
