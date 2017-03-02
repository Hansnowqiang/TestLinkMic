package com.han.testlinkmic;

import android.app.Application;

import com.tencent.rtmp.TXLiveBase;

/**
 * 小直播应用类，用于全局的操作，如
 * sdk初始化,全局提示框
 */
public class TCApplication extends Application {

//    private RefWatcher mRefWatcher;

    private static TCApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        initSDK();
    }

    public static TCApplication getApplication() {
        return instance;
    }


    /**
     * 初始化SDK，包括Bugly，IMSDK，RTMPSDK等
     */
    public void initSDK() {

        //设置rtmpsdk log回调，将log保存到文件
        TXLiveBase.getInstance().listener = new TCLog(getApplicationContext());
    }

}
