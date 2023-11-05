package com.disney.util;

import java.time.format.DateTimeFormatter;

public class ApiUtils {
    public static final DateTimeFormatter OF_PATTERN = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    public static boolean isASC(String order) {
        return order.compareToIgnoreCase("ASC") == 0;
    }
}
