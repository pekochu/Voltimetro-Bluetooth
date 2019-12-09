package com.angelbrv.instru;

import android.content.Context;
import android.content.SharedPreferences;

public class PersistentData {

    private Context context;

    public PersistentData(Context context){
        this.context = context;
    }

    private SharedPreferences getSettings(){
        return context.getSharedPreferences("persistent_data", Context.MODE_PRIVATE);
    }

    public void setString(String key, String value){
        SharedPreferences.Editor editor = getSettings().edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key){
        return getSettings().getString(key, null);
    }

}
