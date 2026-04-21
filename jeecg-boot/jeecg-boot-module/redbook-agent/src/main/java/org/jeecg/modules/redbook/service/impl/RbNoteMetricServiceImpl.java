package org.jeecg.modules.redbook.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.redbook.entity.RbNoteMetric;
import org.jeecg.modules.redbook.mapper.RbNoteMetricMapper;
import org.jeecg.modules.redbook.service.IRbNoteMetricService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

@Service
public class RbNoteMetricServiceImpl extends ServiceImpl<RbNoteMetricMapper, RbNoteMetric> implements IRbNoteMetricService {
    @Override
    public RbNoteMetric normalizeMetric(RbNoteMetric entity) {
        normalizeCountFields(entity);
        long views = entity.getViews();
        long likes = entity.getLikes();
        long collects = entity.getCollects();
        long comments = entity.getComments();
        long shares = entity.getShares();

        if (entity.getCollectTime() == null) {
            entity.setCollectTime(new Date());
        }

        if (views <= 0) {
            entity.setInteractionRate(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
            entity.setCollectRate(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
            entity.setCommentRate(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
            return entity;
        }

        BigDecimal base = BigDecimal.valueOf(views);
        entity.setInteractionRate(BigDecimal.valueOf(likes + collects + comments + shares).divide(base, 4, RoundingMode.HALF_UP));
        entity.setCollectRate(BigDecimal.valueOf(collects).divide(base, 4, RoundingMode.HALF_UP));
        entity.setCommentRate(BigDecimal.valueOf(comments).divide(base, 4, RoundingMode.HALF_UP));
        return entity;
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private void normalizeCountFields(RbNoteMetric entity) {
        entity.setImpressions(defaultLong(entity.getImpressions()));
        entity.setViews(defaultLong(entity.getViews()));
        entity.setLikes(defaultLong(entity.getLikes()));
        entity.setCollects(defaultLong(entity.getCollects()));
        entity.setComments(defaultLong(entity.getComments()));
        entity.setShares(defaultLong(entity.getShares()));
        entity.setFollowers(defaultLong(entity.getFollowers()));
        entity.setMessages(defaultLong(entity.getMessages()));
        entity.setLeads(defaultLong(entity.getLeads()));
        entity.setConversions(defaultLong(entity.getConversions()));
    }
}
