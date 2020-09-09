package com.android.ipress;

public class RoomInfo {
    String RoomName;
    int DeviceCount;

    //empty constructor needed
    public RoomInfo(){

    }
    public RoomInfo(String roomName, int deviceCount) {
        RoomName = roomName;
        DeviceCount = deviceCount;
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
