package com.creditbank.platform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "trtc")
public class TrtcProperties {

    private boolean enabled = false;
    private long sdkAppId = 0;
    private String secretKey = "";
    private long expireSeconds = 86400;
    private int joinWindowMinutesBefore = 30;
    private int joinWindowMinutesAfter = 120;

    public boolean isConfigured() {
        return enabled && sdkAppId > 0 && secretKey != null && !secretKey.isBlank();
    }
}
