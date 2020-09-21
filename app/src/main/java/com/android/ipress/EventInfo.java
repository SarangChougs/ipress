package com.android.ipress;

public class EventInfo {
    String EventName;
    String DeviceCount;
    String Activated;

    public EventInfo() {
        //empty constructor needed
    }

    public EventInfo(String eventName, String deviceCount, String activated) {
        EventName = eventName;
        DeviceCount = deviceCount;
        Activated = activated;
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
