package com.android.ipress;

import java.util.ArrayList;
import java.util.List;

public class ActivityStack {
    public static List<String> stack = new ArrayList<>();
    public static int top = -1;

    public static void push(String Element) {
        if (ActivityStack.notContains(Element)) {
            ActivityStack.top += 1;
            ActivityStack.stack.add(top,Element);
        }
    }

    public static String pop() {
        if (ActivityStack.isNotEmpty()) {
            String PoppedElement = ActivityStack.stack.get(ActivityStack.top);
            ActivityStack.top -= 1;
            return PoppedElement;
        } else
            return "Empty";
    }

    public static boolean isNotEmpty() {
        return ActivityStack.top != -1;
    }

    public static boolean notContains(String Element) {
        return !ActivityStack.stack.contains(Element);
    }

    public static void setEmpty() {
        ActivityStack.top = -1;
    }
}
