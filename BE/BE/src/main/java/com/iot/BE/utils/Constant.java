package com.iot.BE.utils;

import java.util.ArrayList;
import java.util.List;

public class Constant {

    // shared class
    public static final String BROKER = "tcp://192.168.224.180:1883";
    public static final String CLIENT_ID = "ESP8266Client";
    public static final List<Integer> sharedList = new ArrayList<>();

    public final static String LED_CONTROL = "LED_CONTROL";
    public final static String FAN_CONTROL = "FAN_CONTROL";
    public final static String AC_CONTROL = "AC_CONTROL";
    public final static String WARNING_CONTROL = "WARNING_CONTROL";
    //Sua
    public final static String LED2_CONTROL = "LED2_CONTROL";
    public final static String LED3_CONTROL = "LED3_CONTROL";

    public final static String DATA_SENSOR = "SENSOR/DATA";
    public final static String LED_RESPONSE = "LED_RESPONSE";
    public final static String FAN_RESPONSE = "FAN_RESPONSE";
    public final static String AC_RESPONSE = "AC_RESPONSE";
    //Sua
    public final static String LED2_RESPONSE = "LED2_RESPONSE";
    public final static String LED3_RESPONSE = "LED3_RESPONSE";
    public final static String WARNING_RESPONSE = "WARNING_RESPONSE";
    public final static String ASC = "ASC";
    public final static String DESC = "DESC";
}
