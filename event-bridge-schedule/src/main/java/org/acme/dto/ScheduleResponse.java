package org.acme.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ScheduleResponse {

    private String message;

    @JsonProperty("schedule_name")
    private String scheduleName;

    @JsonProperty("schedule_arn")
    private String scheduleArn;

    public ScheduleResponse() {
    }

    public ScheduleResponse(String message, String scheduleName, String scheduleArn) {
        this.message = message;
        this.scheduleName = scheduleName;
        this.scheduleArn = scheduleArn;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public String getScheduleArn() {
        return scheduleArn;
    }

    public void setScheduleArn(String scheduleArn) {
        this.scheduleArn = scheduleArn;
    }
}

