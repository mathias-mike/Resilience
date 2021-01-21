package com.psyclone.resilience.utils;

import com.google.gson.Gson;

public class GsonSingleton {

    private static Gson instance;

    public static Gson getInstance(){
        if(instance == null) instance = new Gson();
        return instance;
    }

    private GsonSingleton () {}
}
