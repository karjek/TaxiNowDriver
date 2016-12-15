package com.home.yassine.taxinowdriver;

/**
 * Created by Yassine on 16/09/2016.
 */
public class Passenger {

    public enum PassengerType {
        ANON,
        CLIENT
    }

    public PassengerType type;
    public String gender;
    public String id;
    public String iconName;
    public int count;
}