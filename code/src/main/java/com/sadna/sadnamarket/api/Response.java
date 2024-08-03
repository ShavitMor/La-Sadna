package com.sadna.sadnamarket.api;

public class Response {
    private String dataJson;
    private boolean error;
    private String errorString;

    private Response(String dataJson, boolean error, String errorString) {
        this.dataJson = dataJson;
        this.error = error;
        this.errorString = errorString;
    }

    public static Response createResponse(boolean isError, String str) {
        String dataStr = isError ? null : str;
        String errorStr = isError ? str : null;
        return new Response(dataStr, isError, errorStr);
    }
    
    public static Response createResponse() {
        return new Response("true", false,"");
    }
    public static Response createResponse(int number) {
        String dataStr = number+"";
        String errorStr = null;
        return new Response(dataStr, false, errorStr);
    }
    public String getDataJson() {
        return this.dataJson;
    }

    public boolean getError() {
        return this.error;
    }

    public String getErrorString() {
        return this.errorString;
    }
}
