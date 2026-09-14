package com.base.android.ui.main.chart;

public class ChartSummaryData {
    private final String title;
    private final String description;
    private final String totalMetric;
    private final String avgMetric;
    private final String maxMetric;

    public ChartSummaryData(String title, String description, String totalMetric, String avgMetric, String maxMetric) {
        this.title = title;
        this.description = description;
        this.totalMetric = totalMetric;
        this.avgMetric = avgMetric;
        this.maxMetric = maxMetric;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getTotalMetric() {
        return totalMetric;
    }

    public String getAvgMetric() {
        return avgMetric;
    }

    public String getMaxMetric() {
        return maxMetric;
    }
}
