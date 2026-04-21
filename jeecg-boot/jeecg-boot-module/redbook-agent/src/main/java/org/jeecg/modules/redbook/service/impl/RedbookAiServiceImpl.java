package org.jeecg.modules.redbook.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.redbook.config.RedbookAiProperties;
import org.jeecg.modules.redbook.constant.RedbookPromptTemplateConstant;
import org.jeecg.modules.redbook.constant.RedbookStatusConstant;
import org.jeecg.modules.redbook.entity.RbPromptTemplate;
import org.jeecg.modules.redbook.model.ai.RedbookAiExecutionResult;
import org.jeecg.modules.redbook.service.IRbPromptTemplateService;
import org.jeecg.modules.redbook.service.IRedbookAiService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
public class RedbookAiServiceImpl implements IRedbookAiService {
    private static final String ERROR_TYPE_CONFIG = "config_error";
    private static final String ERROR_TYPE_TEMPLATE = "template_error";
    private static final String ERROR_TYPE_SCHEMA = "schema_error";
    private static final String ERROR_TYPE_AUTH = "auth_error";
    private static final String ERROR_TYPE_REQUEST = "request_error";
    private static final String ERROR_TYPE_RATE_LIMIT = "rate_limit";
    private static final String ERROR_TYPE_UPSTREAM = "upstream_error";
    private static final String ERROR_TYPE_NETWORK = "network_error";
    private static final String ERROR_TYPE_UNKNOWN = "unknown_error";

    @Resource
    private RedbookAiProperties redbookAiProperties;

    @Resource
    private IRbPromptTemplateService promptTemplateService;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public RedbookAiExecutionResult runWorkflow(String templateCode, Map<String, Object> payload) {
        RedbookAiExecutionResult result = new RedbookAiExecutionResult()
            .setTemplateCode(templateCode)
            .setProvider(RedbookPromptTemplateConstant.PROVIDER_LOCAL);

        RbPromptTemplate template = promptTemplateService.lambdaQuery()
            .eq(RbPromptTemplate::getTemplateCode, templateCode)
            .eq(RbPromptTemplate::getStatus, RedbookStatusConstant.ACTIVE)
            .orderByDesc(RbPromptTemplate::getUpdateTime)
            .last("limit 1")
            .one();
        if (template == null) {
            return result
                .setErrorType(ERROR_TYPE_TEMPLATE)
                .setErrorMessage("未找到已启用的提示词模板：" + templateCode);
        }

        String provider = resolveProvider(template.getModelProvider());
        result.setProvider(provider);
        if (!redbookAiProperties.isEnabled()) {
            return result
                .setErrorType(ERROR_TYPE_CONFIG)
                .setErrorMessage("redbook.ai.enabled=false，已使用本地降级流程");
        }
        if (RedbookPromptTemplateConstant.PROVIDER_LOCAL.equals(provider)) {
            return result
                .setErrorType(ERROR_TYPE_CONFIG)
                .setErrorMessage("未配置可用的 AI 提供方，已使用本地降级流程");
        }
        if (isBlank(redbookAiProperties.getBaseUrl()) || isBlank(redbookAiProperties.getApiKey())) {
            return result
                .setErrorType(ERROR_TYPE_CONFIG)
                .setErrorMessage("AI 基础配置缺失，请检查 redbook.ai.base-url / api-key");
        }

        int maxAttempts = Math.max(1, defaultInt(redbookAiProperties.getRetryTimes()) + 1);
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            result.setAttemptCount(attempt);
            try {
                Map<String, Object> response;
                if (RedbookPromptTemplateConstant.PROVIDER_DIFY.equals(provider)) {
                    response = callDify(template, payload);
                    result.setOutputs(extractDifyOutputs(response));
                } else if (RedbookPromptTemplateConstant.PROVIDER_FASTGPT.equals(provider)) {
                    response = callFastGpt(template, payload);
                    result.setOutputs(extractFastGptOutputs(response));
                } else {
                    return result
                        .setErrorType(ERROR_TYPE_CONFIG)
                        .setErrorMessage("暂不支持的 AI 提供方：" + provider);
                }
                result.setRawResult(toJson(response));
                result.setRemoteUsed(true);
                validateOutputs(template, result);
                if (result.getOutputs().isEmpty()) {
                    result.setSuccess(false);
                    result.setErrorType(ERROR_TYPE_SCHEMA);
                    result.setErrorMessage(mergeErrorMessage(result.getErrorMessage(), "AI 已返回结果，但未解析出结构化字段"));
                } else if (!result.isSchemaValid()) {
                    result.setSuccess(false);
                    result.setErrorType(ERROR_TYPE_SCHEMA);
                    result.setErrorMessage(mergeErrorMessage(result.getErrorMessage(), "AI 输出结构校验失败"));
                } else {
                    result.setSuccess(true);
                }
                return result;
            } catch (Exception ex) {
                String errorType = classifyErrorType(ex);
                result.setRemoteUsed(true);
                result.setErrorType(errorType);
                result.setErrorMessage(buildRemoteErrorMessage(ex, errorType));
                result.setRawResult(buildRemoteErrorRaw(ex, provider, templateCode, attempt));
                boolean retryable = isRetryable(errorType);
                if (retryable && attempt < maxAttempts) {
                    log.warn("Redbook AI workflow execution failed, templateCode={}, provider={}, attempt={}, retrying, errorType={}",
                        templateCode, provider, attempt, errorType, ex);
                    continue;
                }
                log.warn("Redbook AI workflow execution failed, templateCode={}, provider={}, attempt={}, errorType={}",
                    templateCode, provider, attempt, errorType, ex);
                return result;
            }
        }
        return result;
    }

    private void validateOutputs(RbPromptTemplate template, RedbookAiExecutionResult result) {
        Map<String, Object> schema = parseJsonObject(template.getOutputSchema());
        if (schema.isEmpty()) {
            result.setSchemaValid(false);
            result.setErrorType(ERROR_TYPE_SCHEMA);
            result.setValidationErrors(List.of("模板 output_schema 为空或不是合法 JSON 对象"));
            return;
        }
        List<String> errors = new ArrayList<>();
        validateBySchema("$", schema, result.getOutputs(), errors);
        result.setSchemaValid(errors.isEmpty());
        result.setValidationErrors(errors);
    }

    private void validateBySchema(String path, Object schemaNode, Object outputNode, List<String> errors) {
        if (schemaNode instanceof Map<?, ?> schemaMap) {
            Map<String, Object> schemaObject = objectMapper.convertValue(schemaMap, new TypeReference<Map<String, Object>>() {});
            if (!(outputNode instanceof Map<?, ?> outputMap)) {
                errors.add(path + " 应为对象");
                return;
            }
            Map<String, Object> outputObject = objectMapper.convertValue(outputMap, new TypeReference<Map<String, Object>>() {});
            for (Map.Entry<String, Object> entry : schemaObject.entrySet()) {
                String key = entry.getKey();
                if (!outputObject.containsKey(key)) {
                    errors.add(path + "." + key + " 缺失");
                    continue;
                }
                validateBySchema(path + "." + key, entry.getValue(), outputObject.get(key), errors);
            }
            return;
        }
        if (schemaNode instanceof List<?> schemaList) {
            if (outputNode == null) {
                errors.add(path + " 缺失");
                return;
            }
            if (outputNode instanceof String) {
                return;
            }
            if (!(outputNode instanceof List<?> outputList)) {
                errors.add(path + " 应为数组");
                return;
            }
            if (!schemaList.isEmpty() && !outputList.isEmpty()) {
                validateBySchema(path + "[0]", schemaList.get(0), outputList.get(0), errors);
            }
            return;
        }
        if (outputNode == null) {
            errors.add(path + " 缺失");
        }
    }

    private Map<String, Object> callDify(RbPromptTemplate template, Map<String, Object> payload) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("inputs", buildWorkflowInputs(template, payload));
        body.put("response_mode", "blocking");
        body.put("user", buildUser(template.getTemplateCode()));
        return postJson(resolveDifyEndpoint(), body);
    }

    private Map<String, Object> callFastGpt(RbPromptTemplate template, Map<String, Object> payload) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("stream", false);
        body.put("detail", false);
        body.put("variables", buildWorkflowInputs(template, payload));

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(buildMessage("system", buildSystemPrompt(template)));
        messages.add(buildMessage("user", "业务上下文 JSON：\n" + prettyJson(payload)));
        body.put("messages", messages);
        return postJson(resolveFastGptEndpoint(), body);
    }

    private Map<String, Object> buildMessage(String role, String content) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private Map<String, Object> buildWorkflowInputs(RbPromptTemplate template, Map<String, Object> payload) {
        Map<String, Object> inputs = new LinkedHashMap<>();
        if (payload != null) {
            inputs.putAll(payload);
        }
        inputs.put("instruction", firstNonBlank(template.getPromptContent(), "请结合业务上下文输出结构化分析结果。"));
        inputs.put("output_schema", firstNonBlank(template.getOutputSchema(), "{}"));
        inputs.put("payload_json", toJson(payload));
        inputs.put("payload_markdown", prettyJson(payload));
        inputs.put("template_code", template.getTemplateCode());
        inputs.put("workflow_type", template.getWorkflowType());
        return inputs;
    }

    private String buildSystemPrompt(RbPromptTemplate template) {
        return firstNonBlank(template.getPromptContent(), "你是一个擅长小红书运营分析的助手。")
            + "\n\n请严格返回 JSON，不要使用 Markdown 代码块，不要补充解释。"
            + "\n输出结构："
            + firstNonBlank(template.getOutputSchema(), "{}");
    }

    private Map<String, Object> postJson(String url, Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(redbookAiProperties.getApiKey().trim());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = buildRestTemplate().exchange(url, HttpMethod.POST, entity, Map.class);
        Map<?, ?> responseBody = response.getBody();
        if (responseBody == null) {
            return new LinkedHashMap<>();
        }
        return objectMapper.convertValue(responseBody, new TypeReference<Map<String, Object>>() {});
    }

    private RestTemplate buildRestTemplate() {
        int timeoutSeconds = redbookAiProperties.getTimeoutSeconds() == null ? 60 : Math.max(redbookAiProperties.getTimeoutSeconds(), 5);
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Math.min(timeoutSeconds, 30) * 1000);
        requestFactory.setReadTimeout(timeoutSeconds * 1000);
        return new RestTemplate(requestFactory);
    }

    private Map<String, Object> extractDifyOutputs(Map<String, Object> response) {
        Map<String, Object> data = getMap(response.get("data"));
        if (!data.isEmpty()) {
            Map<String, Object> outputs = normalizeOutputs(data.get("outputs"));
            if (!outputs.isEmpty()) {
                return outputs;
            }
            Map<String, Object> textOutputs = normalizeOutputs(data.get("text"));
            if (!textOutputs.isEmpty()) {
                return textOutputs;
            }
        }
        return normalizeOutputs(response.get("outputs"));
    }

    private Map<String, Object> extractFastGptOutputs(Map<String, Object> response) {
        Object choices = response.get("choices");
        if (choices instanceof List<?> choiceList && !choiceList.isEmpty()) {
            Object first = choiceList.get(0);
            Map<String, Object> firstChoice = getMap(first);
            Map<String, Object> message = getMap(firstChoice.get("message"));
            if (!message.isEmpty()) {
                return normalizeOutputs(message.get("content"));
            }
        }
        return normalizeOutputs(response.get("responseData"));
    }

    private Map<String, Object> normalizeOutputs(Object raw) {
        Map<String, Object> directMap = getMap(raw);
        if (!directMap.isEmpty()) {
            Object result = directMap.get("result");
            if (result instanceof String resultText) {
                Map<String, Object> parsed = parseJsonObject(resultText);
                if (!parsed.isEmpty()) {
                    return parsed;
                }
            }
            return directMap;
        }
        if (raw instanceof String text) {
            Map<String, Object> parsed = parseJsonObject(text);
            if (!parsed.isEmpty()) {
                return parsed;
            }
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("result", text);
            return fallback;
        }
        return new LinkedHashMap<>();
    }

    private Map<String, Object> parseJsonObject(String text) {
        if (isBlank(text)) {
            return new LinkedHashMap<>();
        }
        String normalized = text.trim();
        if (normalized.startsWith("```")) {
            normalized = normalized.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "").trim();
        }
        int start = normalized.indexOf('{');
        int end = normalized.lastIndexOf('}');
        if (start >= 0 && end > start) {
            normalized = normalized.substring(start, end + 1);
        }
        try {
            return objectMapper.readValue(normalized, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ex) {
            return new LinkedHashMap<>();
        }
    }

    private Map<String, Object> getMap(Object raw) {
        if (!(raw instanceof Map<?, ?> mapValue)) {
            return new LinkedHashMap<>();
        }
        return objectMapper.convertValue(mapValue, new TypeReference<Map<String, Object>>() {});
    }

    private String resolveProvider(String templateProvider) {
        String provider = normalizeProvider(templateProvider);
        if (!RedbookPromptTemplateConstant.PROVIDER_LOCAL.equals(provider)) {
            return provider;
        }
        return normalizeProvider(redbookAiProperties.getProvider());
    }

    private String normalizeProvider(String provider) {
        String normalized = defaultText(provider).replace("-", "").replace("_", "");
        if ("dify".equals(normalized)) {
            return RedbookPromptTemplateConstant.PROVIDER_DIFY;
        }
        if ("fastgpt".equals(normalized)) {
            return RedbookPromptTemplateConstant.PROVIDER_FASTGPT;
        }
        return RedbookPromptTemplateConstant.PROVIDER_LOCAL;
    }

    private String resolveDifyEndpoint() {
        String baseUrl = trimTrailingSlash(redbookAiProperties.getBaseUrl());
        if (baseUrl.endsWith("/workflows/run")) {
            return baseUrl;
        }
        if (baseUrl.endsWith("/v1")) {
            return baseUrl + "/workflows/run";
        }
        return baseUrl + "/v1/workflows/run";
    }

    private String resolveFastGptEndpoint() {
        String baseUrl = trimTrailingSlash(redbookAiProperties.getBaseUrl());
        if (baseUrl.endsWith("/api/v1/chat/completions")) {
            return baseUrl;
        }
        if (baseUrl.endsWith("/api/v1") || baseUrl.endsWith("/v1")) {
            return baseUrl + "/chat/completions";
        }
        if (baseUrl.endsWith("/api")) {
            return baseUrl + "/v1/chat/completions";
        }
        return baseUrl + "/api/v1/chat/completions";
    }

    private String buildUser(String templateCode) {
        return firstNonBlank(redbookAiProperties.getUserPrefix(), "redbook-agent") + "-" + templateCode;
    }

    private String prettyJson(Object value) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value == null ? new LinkedHashMap<>() : value);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? new LinkedHashMap<>() : value);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private String trimTrailingSlash(String value) {
        String normalized = defaultText(value);
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private String defaultText(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String mergeErrorMessage(String origin, String next) {
        if (isBlank(origin)) {
            return next;
        }
        if (isBlank(next) || origin.contains(next)) {
            return origin;
        }
        return origin + "；" + next;
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : Math.max(value, 0);
    }

    private String classifyErrorType(Exception ex) {
        if (ex instanceof RestClientResponseException responseException) {
            int statusCode = responseException.getStatusCode().value();
            if (statusCode == 401 || statusCode == 403) {
                return ERROR_TYPE_AUTH;
            }
            if (statusCode == 429) {
                return ERROR_TYPE_RATE_LIMIT;
            }
            if (statusCode >= 400 && statusCode < 500) {
                return ERROR_TYPE_REQUEST;
            }
            if (statusCode >= 500) {
                return ERROR_TYPE_UPSTREAM;
            }
        }
        if (ex instanceof ResourceAccessException) {
            return ERROR_TYPE_NETWORK;
        }
        return ERROR_TYPE_UNKNOWN;
    }

    private boolean isRetryable(String errorType) {
        return ERROR_TYPE_NETWORK.equals(errorType)
            || ERROR_TYPE_RATE_LIMIT.equals(errorType)
            || ERROR_TYPE_UPSTREAM.equals(errorType);
    }

    private String buildRemoteErrorMessage(Exception ex, String errorType) {
        String detail = firstNonBlank(ex.getMessage(), ex.getClass().getSimpleName());
        return switch (errorType) {
            case ERROR_TYPE_AUTH -> "AI 鉴权失败，请检查 api-key 或服务权限：" + detail;
            case ERROR_TYPE_RATE_LIMIT -> "AI 调用被限流，稍后可重试：" + detail;
            case ERROR_TYPE_REQUEST -> "AI 请求参数非法或模板配置不兼容：" + detail;
            case ERROR_TYPE_UPSTREAM -> "AI 服务端返回异常：" + detail;
            case ERROR_TYPE_NETWORK -> "AI 网络连接或超时异常：" + detail;
            default -> "AI 调用异常：" + detail;
        };
    }

    private String buildRemoteErrorRaw(Exception ex, String provider, String templateCode, int attempt) {
        Map<String, Object> raw = new LinkedHashMap<>();
        raw.put("provider", provider);
        raw.put("template_code", templateCode);
        raw.put("attempt", attempt);
        raw.put("exception", ex.getClass().getName());
        raw.put("message", firstNonBlank(ex.getMessage(), ex.getClass().getSimpleName()));
        if (ex instanceof RestClientResponseException responseException) {
            raw.put("status_code", responseException.getStatusCode().value());
            raw.put("response_body", responseException.getResponseBodyAsString());
        }
        return toJson(raw);
    }
}
