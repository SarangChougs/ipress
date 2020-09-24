package com.android.ipress;

public class ApplianceInfo {
    String applianceId;
    int State, Favourite;
    String Name,Parent;
    String IconUrl;

    public ApplianceInfo() {
        //empty constructor needed
    }

    public ApplianceInfo(String applianceId, int state, int favourite, String name, String parent, String iconUrl) {
        this.applianceId = applianceId;
        State = state;
        Favourite = favourite;
        Name = name;
        Parent = parent;
        IconUrl = iconUrl;
    }

    public String getIconUrl() {
        return IconUrl;
    }

    public void setIconUrl(String iconUrl) {
        IconUrl = iconUrl;
    }

    public String getApplianceId() {
        return applianceId;
    }

    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
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
