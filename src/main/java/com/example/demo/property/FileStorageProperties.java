package com.example.demo.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="file")
public class FileStorageProperties {
    private String uploadDir;
    private String generatedDir;

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getGeneratedDir() {
        return generatedDir;
    }

    public void setGeneratedDir(String generatedDir) {
        this.generatedDir = generatedDir;
    }
}
