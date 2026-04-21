package org.jeecg.modules.redbook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.redbook.entity.RbPersona;
import org.jeecg.modules.redbook.service.IRbPersonaService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Tag(name = "RedBook人设管理")
@RestController
@RequestMapping("/redbook/persona")
public class RbPersonaController extends RedbookCrudController<RbPersona, IRbPersonaService> {
    @GetMapping(value = "/list")
    @Operation(summary = "人设分页列表")
    public Result<?> list(RbPersona entity, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        return queryPageList(entity, pageNo, pageSize, req);
    }

    @PostMapping(value = "/add")
    @AutoLog(value = "新增账号人设")
    @RequiresPermissions("redbook:persona:add")
    public Result<?> add(@RequestBody RbPersona entity) {
        return saveEntity(entity);
    }

    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @AutoLog(value = "编辑账号人设")
    @RequiresPermissions("redbook:persona:edit")
    public Result<?> edit(@RequestBody RbPersona entity) {
        return updateEntity(entity);
    }

    @DeleteMapping(value = "/delete")
    @AutoLog(value = "删除账号人设")
    @RequiresPermissions("redbook:persona:delete")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        return removeEntity(id);
    }

    @DeleteMapping(value = "/deleteBatch")
    @RequiresPermissions("redbook:persona:deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        return removeBatch(ids);
    }

    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id") String id) {
        return queryEntityById(id);
    }

    @RequestMapping(value = "/exportXls")
    @RequiresPermissions("redbook:persona:exportXls")
    public ModelAndView exportXls(HttpServletRequest request, RbPersona entity) {
        return exportExcel(request, entity, RbPersona.class, "账号人设");
    }

    @PostMapping(value = "/importExcel")
    @RequiresPermissions("redbook:persona:importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return importExcelData(request, response, RbPersona.class);
    }
}
