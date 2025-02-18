package com.example.final_project.model;

public class DevTask {
    private int id;
    private String taskName;
    private String devName;
    private int taskId;
    private String startDate;
    private String endDate;
    private  int estimateDay;


    public DevTask(int id, String taskName, String devName, int taskId, String startDate, String endDate, int estimateDay) {
        this.id = id;
        this.taskName = taskName;
        this.devName = devName;
        this.taskId = taskId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.estimateDay = estimateDay;
    }

    public int getId() {
        return id;
    }

    public String getDevName() {
        return devName;
    }
    public String getTaskName(){
        return taskName;
    }

    public int getTaskId() {
        return taskId;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public int getEstimateDay() {
        return estimateDay;
    }
}
