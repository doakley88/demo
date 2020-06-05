package com.example.demo.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="host")
public class ContextProperties {

    private String host;

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }
}
