package com.example.autoprovisioning.component.helper;

public class RequestResponse {

    private Constants.NodeStatus issue;
    private int status;
    private String message;
    private String messageDetail;
    private Object data;

    public RequestResponse() {
        this.status = 0;
        this.issue= Constants.NodeStatus.OK;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMessageDetail() {
        return messageDetail;
    }

    public void setMessageDetail(String messageDetail) {
        this.messageDetail = messageDetail;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public Constants.NodeStatus getIssue() {
        return issue;
    }

    public void setIssue(Constants.NodeStatus issue) {
        this.issue = issue;
    }

}
