package com.rachanaxc.appingchat;

public class Request {

    String to;
    String from;
    boolean isAccepted;
    String to_name;
    String from_name;
    String requestId;

    public Request() {
    }

    public Request(String to, String from, boolean isAccepted, String to_name, String from_name) {
        this.to = to;
        this.from = from;
        this.isAccepted = isAccepted;
        this.to_name = to_name;
        this.from_name = from_name;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }

    public void setTo_name(String to_name) {
        this.to_name = to_name;
    }

    public void setFrom_name(String from_name) {
        this.from_name = from_name;
    }

    public String getTo_name() {
        return to_name;
    }

    public String getFrom_name() {
        return from_name;
    }
}
