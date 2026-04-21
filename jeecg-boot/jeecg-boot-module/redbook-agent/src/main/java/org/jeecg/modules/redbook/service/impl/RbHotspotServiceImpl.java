package org.jeecg.modules.redbook.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.jeecg.modules.redbook.constant.RedbookStatusConstant;
import org.jeecg.modules.redbook.entity.RbHotspot;
import org.jeecg.modules.redbook.mapper.RbHotspotMapper;
import org.jeecg.modules.redbook.service.IRbHotspotService;
import org.jeecg.modules.redbook.service.IRbTrackService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Set;

@Service
public class RbHotspotServiceImpl extends ServiceImpl<RbHotspotMapper, RbHotspot> implements IRbHotspotService {
    private static final Set<String> HOTSPOT_STATUS_VALUES = Set.of(
        RedbookStatusConstant.HOTSPOT_PENDING_ANALYSIS,
        RedbookStatusConstant.HOTSPOT_ANALYZED,
        RedbookStatusConstant.HOTSPOT_DRAFT_GENERATED,
        RedbookStatusConstant.HOTSPOT_DISCARDED
    );

    @Resource
    private IRbTrackService trackService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RbHotspot createHotspot(RbHotspot entity) {
        normalizeForCreate(entity);
        validateUnique(entity.getTitle(), entity.getOriginalUrl(), null);
        save(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RbHotspot updateHotspot(RbHotspot entity) {
        if (isBlank(entity.getId())) {
            throw new IllegalArgumentException("热点ID不能为空");
        }
        RbHotspot oldEntity = getById(entity.getId());
        if (oldEntity == null) {
            throw new IllegalArgumentException("未找到对应热点");
        }
        normalizeForUpdate(oldEntity, entity);
        validateUnique(entity.getTitle(), entity.getOriginalUrl(), entity.getId());
        updateById(entity);
        return getById(entity.getId());
    }

    @Override
    public RbHotspot prepareImportHotspot(RbHotspot entity) {
        normalizeForCreate(entity);
        validateUnique(entity.getTitle(), entity.getOriginalUrl(), null);
        return entity;
    }

    @Override
    public boolean existsByOriginalUrl(String originalUrl, String excludeId) {
        if (isBlank(originalUrl)) {
            return false;
        }
        return lambdaQuery()
            .eq(RbHotspot::getOriginalUrl, originalUrl.trim())
            .ne(!isBlank(excludeId), RbHotspot::getId, excludeId)
            .count() > 0;
    }

    @Override
    public boolean existsByTitle(String title, String excludeId) {
        if (isBlank(title)) {
            return false;
        }
        return lambdaQuery()
            .eq(RbHotspot::getTitle, title.trim())
            .ne(!isBlank(excludeId), RbHotspot::getId, excludeId)
            .count() > 0;
    }

    private void normalizeForCreate(RbHotspot entity) {
        if (isBlank(entity.getTrackId())) {
            throw new IllegalArgumentException("赛道ID不能为空");
        }
        if (trackService.getById(entity.getTrackId()) == null) {
            throw new IllegalArgumentException("赛道ID不存在");
        }
        if (isBlank(entity.getTitle())) {
            throw new IllegalArgumentException("热点标题不能为空");
        }
        entity.setTitle(entity.getTitle().trim());
        if (!isBlank(entity.getOriginalUrl())) {
            entity.setOriginalUrl(entity.getOriginalUrl().trim());
        }
        if (isBlank(entity.getStatus())) {
            entity.setStatus(RedbookStatusConstant.HOTSPOT_PENDING_ANALYSIS);
        }
        validateStatus(entity.getStatus());
        if (entity.getCollectTime() == null) {
            entity.setCollectTime(new Date());
        }
        if (entity.getLikeCount() == null) {
            entity.setLikeCount(0L);
        }
        if (entity.getCollectCount() == null) {
            entity.setCollectCount(0L);
        }
        if (entity.getCommentCount() == null) {
            entity.setCommentCount(0L);
        }
        if (entity.getShareCount() == null) {
            entity.setShareCount(0L);
        }
    }

    private void normalizeForUpdate(RbHotspot oldEntity, RbHotspot entity) {
        if (isBlank(entity.getTrackId())) {
            entity.setTrackId(oldEntity.getTrackId());
        }
        if (trackService.getById(entity.getTrackId()) == null) {
            throw new IllegalArgumentException("赛道ID不存在");
        }
        if (isBlank(entity.getTitle())) {
            entity.setTitle(oldEntity.getTitle());
        } else {
            entity.setTitle(entity.getTitle().trim());
        }
        if (entity.getCollectTime() == null) {
            entity.setCollectTime(oldEntity.getCollectTime());
        }
        if (isBlank(entity.getOriginalUrl())) {
            entity.setOriginalUrl(oldEntity.getOriginalUrl());
        } else {
            entity.setOriginalUrl(entity.getOriginalUrl().trim());
        }
        if (isBlank(entity.getStatus())) {
            entity.setStatus(oldEntity.getStatus());
        }
        validateStatus(entity.getStatus());
        if (entity.getLikeCount() == null) {
            entity.setLikeCount(oldEntity.getLikeCount());
        }
        if (entity.getCollectCount() == null) {
            entity.setCollectCount(oldEntity.getCollectCount());
        }
        if (entity.getCommentCount() == null) {
            entity.setCommentCount(oldEntity.getCommentCount());
        }
        if (entity.getShareCount() == null) {
            entity.setShareCount(oldEntity.getShareCount());
        }
    }

    private void validateStatus(String status) {
        if (!HOTSPOT_STATUS_VALUES.contains(status)) {
            throw new IllegalArgumentException("热点状态非法：" + status);
        }
    }

    private void validateUnique(String title, String originalUrl, String excludeId) {
        if (existsByTitle(title, excludeId)) {
            throw new IllegalArgumentException("热点标题已存在");
        }
        if (!isBlank(originalUrl) && existsByOriginalUrl(originalUrl, excludeId)) {
            throw new IllegalArgumentException("原文链接已存在");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
