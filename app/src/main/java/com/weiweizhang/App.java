package com.weiweizhang;

import android.app.Application;
import android.support.annotation.NonNull;
import android.widget.Toast;

import me.yokeyword.fragmentation.Fragmentation;
import me.yokeyword.fragmentation.helper.ExceptionHandler;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fragmentation.builder()
                .stackViewMode(Fragmentation.BUBBLE)
                .debug(true)
                .handleException(new ExceptionHandler() {
                    @Override
                    public void onException(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .install();
    }
}
