package ite.project.hbase.utils;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
public class LogUtils {
    protected Logger logger = LogManager.getLogger("vccorp-hbase");
    protected Gson gsonBuilder = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .disableHtmlEscaping().create();
    public final String STEP_BEGIN_PROCESS = "Begin process";
    public final String STEP_END_PROCESS = "End process";
    public final String STEP_BEGIN_CALL_DB = "Begin call database";
    public final String STEP_END_CALL_DB = "End call database";
    public final String STEP_BEGIN_CALL_PARTNER = "Begin call partner";
    public final String STEP_END_CALL_PARTNER = "End call partner";
    public final String STEP_BEGIN_CALL_INTERNAL_SERVICE = "Begin call internal service";
    public final String STEP_END_CALL_INTERNAL_SERVICE = "End call internal service";
    public final String STEP_TRACE = "Step trace";

    public void setProcessName(String processName) {
        ThreadContext.put("process-name", processName);
    }

    public String getProcessName() {
        return ThreadContext.get("process-name");
    }

    private String getRequestId() {
        return ThreadContext.get("request-id");
    }

    public void logInfo(String stepName, String contentInfo) {
        try {
            logger.info("{} - {} - {} - {}", getRequestId(), getProcessName(), stepName, contentInfo);
        } catch (Exception e) {
            logger.info("{} - {} - {} - {}", getRequestId(), getProcessName(), STEP_TRACE, "Exception gen logs");
        }
    }


    public void logErr(String stepName, Exception ex) {
        try {
            logger.error("{} - {} - {} - {}", getRequestId(), getProcessName(), stepName, "Exception", ex);
        } catch (Exception e) {
            logger.error("{} - {} - {} - {}", getRequestId(), getProcessName(), STEP_TRACE, "Exception gen logs");
        }
    }

    public String genContentLog(String reqBody, String reqHeader) {
        try {
            JsonObject jsonObject = new JsonObject();

            try {
                JsonObject jsonBody = JsonParser.parseString(StringUtils.isEmpty(reqBody) ? reqBody : "{}").getAsJsonObject();
                jsonObject.add("request_body", jsonBody);
            } catch (Exception e) {
                jsonObject.addProperty("request_body", reqBody);
            }

            try {
                JsonObject jsonHeader = JsonParser.parseString(StringUtils.isEmpty(reqHeader) ? reqHeader : "{}").getAsJsonObject();
                jsonObject.add("request_header", jsonHeader);
            } catch (Exception e) {
                jsonObject.addProperty("request_header", reqHeader);
            }
            return "req=" + jsonObject;
        } catch (Exception e) {
            logger.error("Exception when gen logs", e);
            return "ex=" + e.getMessage();
        }
    }

    public String genContentLog(ResponseEntity<String> response) {
        try {
            String respStatusCode = "";
            String respBody = "";
            String respHeader = "";
            if (response != null) {
                respStatusCode = String.valueOf(response.getStatusCodeValue());
                respBody = response.getBody();
                respHeader = gsonBuilder.toJson(response.getHeaders());
            }

            JsonObject jsonObject = new JsonObject();
            jsonObject.add("response_body", JsonParser.parseString((StringUtils.isEmpty(respBody) ? "{}" : respBody)).getAsJsonObject());
            jsonObject.add("response_header", JsonParser.parseString((StringUtils.isEmpty(respHeader) ? "{}" : respHeader)).getAsJsonObject());
            jsonObject.addProperty("response_status_code", respStatusCode);

            return "response=" + jsonObject;
        } catch (Exception e) {
            logger.error("Exception gen logs", e);
            return "ex=" + e.getMessage();
        }
    }
}
