package org.jeecg.modules.redbook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.redbook.entity.RbNoteMetric;
import org.jeecg.modules.redbook.service.IRbNoteMetricService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "RedBook数据回收")
@RestController
@RequestMapping("/redbook/noteMetric")
public class RbNoteMetricController extends RedbookCrudController<RbNoteMetric, IRbNoteMetricService> {
    @GetMapping(value = "/list")
    @Operation(summary = "笔记数据分页列表")
    public Result<?> list(RbNoteMetric entity, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        return queryPageList(entity, pageNo, pageSize, req);
    }

    @PostMapping(value = "/add")
    @AutoLog(value = "新增笔记数据")
    @RequiresPermissions("redbook:noteMetric:add")
    public Result<?> add(@RequestBody RbNoteMetric entity) {
        return saveEntity(service.normalizeMetric(entity));
    }

    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @AutoLog(value = "编辑笔记数据")
    @RequiresPermissions("redbook:noteMetric:edit")
    public Result<?> edit(@RequestBody RbNoteMetric entity) {
        return updateEntity(service.normalizeMetric(entity));
    }

    @DeleteMapping(value = "/delete")
    @AutoLog(value = "删除笔记数据")
    @RequiresPermissions("redbook:noteMetric:delete")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        return removeEntity(id);
    }

    @DeleteMapping(value = "/deleteBatch")
    @RequiresPermissions("redbook:noteMetric:deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        return removeBatch(ids);
    }

    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id") String id) {
        return queryEntityById(id);
    }

    @RequestMapping(value = "/exportXls")
    @RequiresPermissions("redbook:noteMetric:exportXls")
    public ModelAndView exportXls(HttpServletRequest request, RbNoteMetric entity) {
        return exportExcel(request, entity, RbNoteMetric.class, "笔记数据回收");
    }

    @PostMapping(value = "/importExcel")
    @RequiresPermissions("redbook:noteMetric:importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        if (fileMap.isEmpty()) {
            return Result.error("文件导入失败:未上传文件");
        }
        int total = 0;
        for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
            MultipartFile file = entry.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try (InputStream inputStream = file.getInputStream()) {
                List<RbNoteMetric> metrics = ExcelImportUtil.importExcel(inputStream, RbNoteMetric.class, params);
                List<RbNoteMetric> normalizedMetrics = metrics.stream()
                    .map(service::normalizeMetric)
                    .collect(Collectors.toList());
                if (!normalizedMetrics.isEmpty()) {
                    service.saveBatch(normalizedMetrics);
                }
                total += normalizedMetrics.size();
            } catch (Exception e) {
                return Result.error("文件导入失败:" + e.getMessage());
            }
        }
        return Result.OK("文件导入成功！数据行数：" + total);
    }
}
