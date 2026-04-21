package org.jeecg.modules.redbook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.redbook.entity.RbPromptTemplate;
import org.jeecg.modules.redbook.service.IRbPromptTemplateService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Tag(name = "RedBook提示词模板")
@RestController
@RequestMapping("/redbook/promptTemplate")
public class RbPromptTemplateController extends RedbookCrudController<RbPromptTemplate, IRbPromptTemplateService> {
    @GetMapping(value = "/list")
    @Operation(summary = "提示词模板分页列表")
    public Result<?> list(RbPromptTemplate entity, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        return queryPageList(entity, pageNo, pageSize, req);
    }

    @PostMapping(value = "/add")
    @AutoLog(value = "新增提示词模板")
    @RequiresPermissions("redbook:promptTemplate:add")
    public Result<?> add(@RequestBody RbPromptTemplate entity) {
        return saveEntity(entity);
    }

    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @AutoLog(value = "编辑提示词模板")
    @RequiresPermissions("redbook:promptTemplate:edit")
    public Result<?> edit(@RequestBody RbPromptTemplate entity) {
        return updateEntity(entity);
    }

    @DeleteMapping(value = "/delete")
    @AutoLog(value = "删除提示词模板")
    @RequiresPermissions("redbook:promptTemplate:delete")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        return removeEntity(id);
    }

    @DeleteMapping(value = "/deleteBatch")
    @RequiresPermissions("redbook:promptTemplate:deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        return removeBatch(ids);
    }

    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id") String id) {
        return queryEntityById(id);
    }

    @RequestMapping(value = "/exportXls")
    @RequiresPermissions("redbook:promptTemplate:exportXls")
    public ModelAndView exportXls(HttpServletRequest request, RbPromptTemplate entity) {
        return exportExcel(request, entity, RbPromptTemplate.class, "提示词模板");
    }

    @PostMapping(value = "/importExcel")
    @RequiresPermissions("redbook:promptTemplate:importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return importExcelData(request, response, RbPromptTemplate.class);
    }
}
