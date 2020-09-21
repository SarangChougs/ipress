package com.android.ipress;

public class ApplianceInfo {
    String ApplianceId;
    int State, Favourite;
    String Name, Parent;

    public ApplianceInfo() {
        //empty constructor needed
    }

    public ApplianceInfo(String applianceId, int state, int favourite, String name, String parent) {
        ApplianceId = applianceId;
        State = state;
        Favourite = favourite;
        Name = name;
        Parent = parent;
    }

    public String getApplianceId() {
        return ApplianceId;
    }

    public void setApplianceId(String applianceId) {
        ApplianceId = applianceId;
    }

    public String getParent() {
        return Parent;
    }

    public void setParent(String parent) {
        Parent = parent;
    }

    public void setFavourite(int favourite) {
        Favourite = favourite;
    }

    public int getFavourite() {
        return Favourite;
    }

    public int getState() {
        return this.State;
    }

    public void setState(String state) {
        this.State = Integer.parseInt(state);
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }
}
