package org.jeecg.modules.redbook.service;

import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.redbook.entity.RbHotspot;

public interface IRbHotspotService extends JeecgService<RbHotspot> {
    RbHotspot createHotspot(RbHotspot entity);

    RbHotspot updateHotspot(RbHotspot entity);

    RbHotspot prepareImportHotspot(RbHotspot entity);

    boolean existsByOriginalUrl(String originalUrl, String excludeId);

    boolean existsByTitle(String title, String excludeId);
}
