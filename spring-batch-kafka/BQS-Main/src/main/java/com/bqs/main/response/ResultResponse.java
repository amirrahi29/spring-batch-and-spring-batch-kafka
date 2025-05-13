package com.bqs.main.response;

public class ResultResponse {

    private MetadataResponse metadataResponse;
    private Object result;
    public MetadataResponse getMetadataResponse() {
        return metadataResponse;
    }
    public void setMetadataResponse(MetadataResponse metadataResponse) {
        this.metadataResponse = metadataResponse;
    }
    public Object getResult() {
        return result;
    }
    public void setResult(Object result) {
        this.result = result;
    }
}