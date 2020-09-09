package com.android.ipress;

public class ApplianceInfo {
    int State, Favourite;
    String Name;

    public ApplianceInfo() {
        //empty constructor needed
    }

    public ApplianceInfo(int State, String Name, int Favourite) {
        this.State = State;
        this.Name = Name;
        this.Favourite = Favourite;
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
