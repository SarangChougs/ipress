package com.android.ipress;

public class RoomInfo {
    String RoomName;
    int DeviceCount;
    String iconUrl;

    //empty constructor needed
    public RoomInfo(){

    }

    public RoomInfo(String roomName, int deviceCount, String iconUrl) {
        RoomName = roomName;
        DeviceCount = deviceCount;
        this.iconUrl = iconUrl;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public void setRoomName(String roomName) {
        RoomName = roomName;
    }

    public void setDeviceCount(int deviceCount) {
        DeviceCount = deviceCount;
    }

    public String getRoomName() {
        return RoomName;
    }

    public int getDeviceCount() {
        return DeviceCount;
    }
}
