package com.example.demo.payload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComposeResponse {
    private String fileName;
    private String fileDownloadUri;
    private String fileType;
    private long size;
    private String errorMessage;

    public ComposeResponse() {
        this.errorMessage = "FAILED";
    }

    public ComposeResponse(String generalErrorMessage) {
        this.errorMessage = generalErrorMessage;
    }

    public ComposeResponse(String fileName, String fileDownloadUri, String fileType, long size) {
        this.fileName = fileName;
        this.fileDownloadUri = fileDownloadUri;
        this.fileType = fileType;
        this.size = size;
    }
}
