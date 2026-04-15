package org.acme.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateScheduleRequest {

    @JsonProperty("schedule_name")
    @JsonAlias("scheduleName")
    private String scheduleName;

    @JsonProperty("schedule_time")
    @JsonAlias("scheduleTime")
    private String scheduleTime; // ISO 8601 format e.g. "2026-04-15T10:30:00"

    public CreateScheduleRequest() {
    }

    public CreateScheduleRequest(String scheduleName, String scheduleTime) {
        this.scheduleName = scheduleName;
        this.scheduleTime = scheduleTime;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public String getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(String scheduleTime) {
        this.scheduleTime = scheduleTime;
    }
}

