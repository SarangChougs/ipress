package com.android.ipress;

import java.util.HashMap;

public class GlobalClass {
    public static String CurrentUserEmail;

    public static HashMap<Integer,String> StateMap = new HashMap<>();
    public static int BackCounter = 0;

    public static void setMapping(){
        GlobalClass.StateMap.put(1,"ON");
        GlobalClass.StateMap.put(0,"OFF");
    }

    public static String iconUrl;
    public static String eventName = "";
    public static String roomName = "";
    public static String applianceName = "";

    public static void setEmpty(){
        GlobalClass.iconUrl = "Empty";

        GlobalClass.applianceName = "";
        GlobalClass.eventName = "";
        GlobalClass.roomName = "";
    }
}
