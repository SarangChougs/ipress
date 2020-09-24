package com.android.ipress;

public class EventInfo {
    String EventName;
    String DeviceCount;
    String Activated;
    String ApplianceIds;
    String IconUrl;

    public EventInfo() {
        //empty constructor needed
    }

    public EventInfo(String eventName, String deviceCount, String activated, String applianceIds, String iconUrl) {
        EventName = eventName;
        DeviceCount = deviceCount;
        Activated = activated;
        ApplianceIds = applianceIds;
        IconUrl = iconUrl;
    }

    public String getIconUrl() {
        return IconUrl;
    }

    public void setIconUrl(String iconUrl) {
        IconUrl = iconUrl;
    }

    public String getApplianceIds() {
        return ApplianceIds;
    }

    public void setApplianceIds(String applianceIds) {
        ApplianceIds = applianceIds;
    }

    public String getEventName() {
        return EventName;
    }

    public String getDeviceCount() {
        return DeviceCount;
    }

    public String getActivated() {
        return Activated;
    }

    public void setActivated(String activated) {
        Activated = activated;
    }

    public void setEventName(String eventName) {
        EventName = eventName;
    }

    public void setDeviceCount(String deviceCount) {
        DeviceCount = deviceCount;
    }
}
