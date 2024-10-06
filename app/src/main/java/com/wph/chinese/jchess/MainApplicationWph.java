package com.wph.chinese.jchess;

import android.app.Application;

import com.blankj.utilcode.util.Utils;


public class MainApplicationWph extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
//        LogUtils.getConfig().setLogSwitch(BuildConfig.DEBUG)
//                .setLogHeadSwitch(false).setBorderSwitch(false);
    }
}
