package be.imec.apt.iwslib.example;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

public class ExampleApp extends Application {
	@Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
    }
}