package com.android.ipress;

public class ApplianceInfo {
    int State;

    public ApplianceInfo(){
        //empty constructor needed
    }
    public ApplianceInfo(int state){
        State = state;
    }

    public int getState() {
        return State;
    }

    public void setState(int state) {
        State = state;
    }
}
