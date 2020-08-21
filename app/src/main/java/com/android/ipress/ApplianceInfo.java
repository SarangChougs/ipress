package com.android.ipress;

public class ApplianceInfo {
    int State;
    String Name;

    public ApplianceInfo(){
        //empty constructor needed
    }
    public ApplianceInfo(int State, String Name){
        this.State = State;
        this.Name = Name;
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
