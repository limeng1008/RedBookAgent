package org.jeecg.modules.redbook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.redbook.entity.RbHotspotAnalysis;
import org.jeecg.modules.redbook.entity.RbNoteDraft;
import org.jeecg.modules.redbook.model.req.RedbookIdRequest;
import org.jeecg.modules.redbook.service.IRbHotspotAnalysisService;
import org.jeecg.modules.redbook.service.IRedbookWorkflowService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import jakarta.annotation.Resource;

@Tag(name = "RedBook热点分析")
@RestController
@RequestMapping("/redbook/hotspotAnalysis")
public class RbHotspotAnalysisController extends RedbookCrudController<RbHotspotAnalysis, IRbHotspotAnalysisService> {
    @Resource
    private IRedbookWorkflowService redbookWorkflowService;

    @GetMapping(value = "/list")
    @Operation(summary = "热点分析分页列表")
    public Result<?> list(RbHotspotAnalysis entity, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        return queryPageList(entity, pageNo, pageSize, req);
    }

    @PostMapping(value = "/add")
    @AutoLog(value = "新增热点分析")
    @RequiresPermissions("redbook:hotspotAnalysis:add")
    public Result<?> add(@RequestBody RbHotspotAnalysis entity) {
        return saveEntity(entity);
    }

    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @AutoLog(value = "编辑热点分析")
    @RequiresPermissions("redbook:hotspotAnalysis:edit")
    public Result<?> edit(@RequestBody RbHotspotAnalysis entity) {
        return updateEntity(entity);
    }

    @DeleteMapping(value = "/delete")
    @AutoLog(value = "删除热点分析")
    @RequiresPermissions("redbook:hotspotAnalysis:delete")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        return removeEntity(id);
    }

    @DeleteMapping(value = "/deleteBatch")
    @RequiresPermissions("redbook:hotspotAnalysis:deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        return removeBatch(ids);
    }

    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id") String id) {
        return queryEntityById(id);
    }

    @RequestMapping(value = "/exportXls")
    @RequiresPermissions("redbook:hotspotAnalysis:exportXls")
    public ModelAndView exportXls(HttpServletRequest request, RbHotspotAnalysis entity) {
        return exportExcel(request, entity, RbHotspotAnalysis.class, "热点分析");
    }

    @PostMapping(value = "/importExcel")
    @RequiresPermissions("redbook:hotspotAnalysis:importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return importExcelData(request, response, RbHotspotAnalysis.class);
    }

    @PostMapping(value = "/generateDraft")
    @AutoLog(value = "分析生成笔记草稿")
    @RequiresPermissions("redbook:hotspotAnalysis:generateDraft")
    @Operation(summary = "基于分析生成笔记草稿")
    public Result<RbNoteDraft> generateDraft(@RequestBody RedbookIdRequest request) {
        RbNoteDraft draft = redbookWorkflowService.generateDraftByAnalysis(request.getId());
        return Result.OK("笔记草稿已生成", draft);
    }
}
