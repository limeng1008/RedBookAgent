package org.jeecg.modules.redbook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.redbook.entity.RbHotspot;
import org.jeecg.modules.redbook.entity.RbHotspotAnalysis;
import org.jeecg.modules.redbook.model.req.RedbookIdRequest;
import org.jeecg.modules.redbook.service.IRbHotspotService;
import org.jeecg.modules.redbook.service.IRedbookWorkflowService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import jakarta.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Tag(name = "RedBook热点池")
@RestController
@RequestMapping("/redbook/hotspot")
public class RbHotspotController extends RedbookCrudController<RbHotspot, IRbHotspotService> {
    @Resource
    private IRedbookWorkflowService redbookWorkflowService;

    @GetMapping(value = "/list")
    @Operation(summary = "热点分页列表")
    public Result<?> list(RbHotspot entity, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        return queryPageList(entity, pageNo, pageSize, req);
    }

    @PostMapping(value = "/add")
    @AutoLog(value = "新增热点")
    @RequiresPermissions("redbook:hotspot:add")
    public Result<?> add(@RequestBody RbHotspot entity) {
        return Result.OK("添加成功！", service.createHotspot(entity));
    }

    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @AutoLog(value = "编辑热点")
    @RequiresPermissions("redbook:hotspot:edit")
    public Result<?> edit(@RequestBody RbHotspot entity) {
        return Result.OK("编辑成功！", service.updateHotspot(entity));
    }

    @DeleteMapping(value = "/delete")
    @AutoLog(value = "删除热点")
    @RequiresPermissions("redbook:hotspot:delete")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        return removeEntity(id);
    }

    @DeleteMapping(value = "/deleteBatch")
    @RequiresPermissions("redbook:hotspot:deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        return removeBatch(ids);
    }

    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id") String id) {
        return queryEntityById(id);
    }

    @RequestMapping(value = "/exportXls")
    @RequiresPermissions("redbook:hotspot:exportXls")
    public ModelAndView exportXls(HttpServletRequest request, RbHotspot entity) {
        return exportExcel(request, entity, RbHotspot.class, "热点池");
    }

    @PostMapping(value = "/importExcel")
    @RequiresPermissions("redbook:hotspot:importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        if (fileMap.isEmpty()) {
            return Result.error("文件导入失败:未上传文件");
        }
        int total = 0;
        List<String> errors = new ArrayList<>();
        Set<String> fileUrlSet = new HashSet<>();
        Set<String> fileTitleSet = new HashSet<>();
        for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
            MultipartFile file = entry.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try (InputStream inputStream = file.getInputStream()) {
                List<RbHotspot> hotspots = ExcelImportUtil.importExcel(inputStream, RbHotspot.class, params);
                List<RbHotspot> readyToSave = new ArrayList<>();
                int startRow = params.getTitleRows() + params.getHeadRows() + 1;
                for (int i = 0; i < hotspots.size(); i++) {
                    int rowNo = startRow + i;
                    RbHotspot hotspot = hotspots.get(i);
                    try {
                        service.prepareImportHotspot(hotspot);
                        validateImportHotspot(hotspot, fileUrlSet, fileTitleSet);
                        readyToSave.add(hotspot);
                    } catch (IllegalArgumentException e) {
                        errors.add("第" + rowNo + "行: " + e.getMessage());
                    }
                }
                if (!errors.isEmpty()) {
                    return Result.error("文件导入失败:\n" + String.join("\n", errors));
                }
                if (!readyToSave.isEmpty()) {
                    service.saveBatch(readyToSave);
                    total += readyToSave.size();
                }
            } catch (Exception e) {
                return Result.error("文件导入失败:" + e.getMessage());
            }
        }
        return Result.OK("文件导入成功！数据行数：" + total);
    }

    @PostMapping(value = "/analyze")
    @AutoLog(value = "热点一键分析")
    @RequiresPermissions("redbook:hotspot:analyze")
    @Operation(summary = "基于热点生成选题分析")
    public Result<RbHotspotAnalysis> analyze(@RequestBody RedbookIdRequest request) {
        RbHotspotAnalysis analysis = redbookWorkflowService.analyzeHotspot(request.getId());
        return Result.OK("热点分析已生成", analysis);
    }

    private void validateImportHotspot(RbHotspot hotspot, Set<String> fileUrlSet, Set<String> fileTitleSet) {
        String titleKey = hotspot.getTitle().trim();
        if (!fileTitleSet.add(titleKey)) {
            throw new IllegalArgumentException("热点标题重复");
        }
        if (hotspot.getOriginalUrl() != null && !hotspot.getOriginalUrl().trim().isEmpty()) {
            String urlKey = hotspot.getOriginalUrl().trim();
            if (!fileUrlSet.add(urlKey)) {
                throw new IllegalArgumentException("原文链接重复");
            }
        }
    }
}
