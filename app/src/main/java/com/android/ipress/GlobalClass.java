package com.android.ipress;

import java.util.HashMap;

public class GlobalClass {
    public static String CurrentUserEmail;

    public static HashMap<Integer,String> StateMap = new HashMap<>();

    public static void setMapping(){
        GlobalClass.StateMap.put(1,"ON");
        GlobalClass.StateMap.put(0,"OFF");
    }
}
