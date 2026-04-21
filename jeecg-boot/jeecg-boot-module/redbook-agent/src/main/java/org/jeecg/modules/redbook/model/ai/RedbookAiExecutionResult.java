package org.jeecg.modules.redbook.model.ai;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class RedbookAiExecutionResult {
    private boolean success;

    private boolean remoteUsed;

    private String provider;

    private String templateCode;

    private String rawResult;

    private String errorMessage;

    private String errorType;

    private int attemptCount;

    private boolean schemaValid = true;

    private List<String> validationErrors = new ArrayList<>();

    private Map<String, Object> outputs = new LinkedHashMap<>();
}
