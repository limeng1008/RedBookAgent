package org.jeecg.modules.redbook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.redbook.entity.RbTrack;
import org.jeecg.modules.redbook.service.IRbTrackService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Tag(name = "RedBook赛道管理")
@RestController
@RequestMapping("/redbook/track")
public class RbTrackController extends RedbookCrudController<RbTrack, IRbTrackService> {
    @GetMapping(value = "/list")
    @Operation(summary = "赛道分页列表")
    public Result<?> list(RbTrack entity, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        return queryPageList(entity, pageNo, pageSize, req);
    }

    @PostMapping(value = "/add")
    @AutoLog(value = "新增运营赛道")
    @RequiresPermissions("redbook:track:add")
    public Result<?> add(@RequestBody RbTrack entity) {
        return saveEntity(entity);
    }

    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @AutoLog(value = "编辑运营赛道")
    @RequiresPermissions("redbook:track:edit")
    public Result<?> edit(@RequestBody RbTrack entity) {
        return updateEntity(entity);
    }

    @DeleteMapping(value = "/delete")
    @AutoLog(value = "删除运营赛道")
    @RequiresPermissions("redbook:track:delete")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        return removeEntity(id);
    }

    @DeleteMapping(value = "/deleteBatch")
    @RequiresPermissions("redbook:track:deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        return removeBatch(ids);
    }

    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id") String id) {
        return queryEntityById(id);
    }

    @RequestMapping(value = "/exportXls")
    @RequiresPermissions("redbook:track:exportXls")
    public ModelAndView exportXls(HttpServletRequest request, RbTrack entity) {
        return exportExcel(request, entity, RbTrack.class, "运营赛道");
    }

    @PostMapping(value = "/importExcel")
    @RequiresPermissions("redbook:track:importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return importExcelData(request, response, RbTrack.class);
    }
}
