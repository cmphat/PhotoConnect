package com.photoconnect.dto;

import java.util.HashMap;
import java.util.Map;

public class CalendarEventDto {
    private String id;
    private String title;
    private String description;
    private String start;
    private String end;
    private boolean allDay;
    private String backgroundColor;
    private String borderColor;
    private String textColor = "#FFFFFF";
    private Map<String, Object> extendedProps = new HashMap<>();

    public CalendarEventDto() {
    }

    public CalendarEventDto(String id, String title, String start, String end, boolean allDay) {
        this.id = id;
        this.title = title;
        this.start = start;
        this.end = end;
        this.allDay = allDay;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public boolean isAllDay() {
        return allDay;
    }

    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public Map<String, Object> getExtendedProps() {
        return extendedProps;
    }

    public void setExtendedProps(Map<String, Object> extendedProps) {
        this.extendedProps = extendedProps;
    }

    public void addExtendedProp(String key, Object value) {
        this.extendedProps.put(key, value);
    }
}
