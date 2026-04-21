package org.jeecg.modules.redbook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.redbook.entity.RbProduct;
import org.jeecg.modules.redbook.service.IRbProductService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Tag(name = "RedBook产品卖点管理")
@RestController
@RequestMapping("/redbook/product")
public class RbProductController extends RedbookCrudController<RbProduct, IRbProductService> {
    @GetMapping(value = "/list")
    @Operation(summary = "产品卖点分页列表")
    public Result<?> list(RbProduct entity, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        return queryPageList(entity, pageNo, pageSize, req);
    }

    @PostMapping(value = "/add")
    @AutoLog(value = "新增产品卖点")
    @RequiresPermissions("redbook:product:add")
    public Result<?> add(@RequestBody RbProduct entity) {
        return saveEntity(entity);
    }

    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @AutoLog(value = "编辑产品卖点")
    @RequiresPermissions("redbook:product:edit")
    public Result<?> edit(@RequestBody RbProduct entity) {
        return updateEntity(entity);
    }

    @DeleteMapping(value = "/delete")
    @AutoLog(value = "删除产品卖点")
    @RequiresPermissions("redbook:product:delete")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        return removeEntity(id);
    }

    @DeleteMapping(value = "/deleteBatch")
    @RequiresPermissions("redbook:product:deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        return removeBatch(ids);
    }

    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id") String id) {
        return queryEntityById(id);
    }

    @RequestMapping(value = "/exportXls")
    @RequiresPermissions("redbook:product:exportXls")
    public ModelAndView exportXls(HttpServletRequest request, RbProduct entity) {
        return exportExcel(request, entity, RbProduct.class, "产品卖点");
    }

    @PostMapping(value = "/importExcel")
    @RequiresPermissions("redbook:product:importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return importExcelData(request, response, RbProduct.class);
    }
}
