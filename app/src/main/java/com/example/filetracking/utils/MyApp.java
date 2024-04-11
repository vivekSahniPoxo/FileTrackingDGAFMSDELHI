package com.example.filetracking.utils;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;


import com.apkfuns.logutils.LogUtils;
import com.example.filetracking.R;
import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.UHFManager;
import com.speedata.libuhf.utils.SharedXmlUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * @author 张明_
 * @date 2018/3/15
 */
public class MyApp extends Application {
    public final static String SERVER_PREFIX = "server_prefix";
    public final static String SERVER_SUFFIX = "server_suffix";
    public final static String SERVER_STOP_FLAG = "server_stop_flag";
    public final static String SERVER_PREFIX_CUSTOM = "server_prefix_custom";
    public final static String SERVER_SUFFIX_CUSTOM = "server_suffix_custom";
    public final static String SERVER_STOP_FLAG_CUSTOM = "server_stop_flag_custom";
    public final static String SERVER_IS_LONG_PRESS = "server_is_long_press";
    public final static String SERVER_IS_FILTER = "server_is_filter";
    public final static String SERVER_IS_ASCII = "server_is_ascii";
    public final static String UHF_FREQ = "uhf_freq";
    public final static String UHF_SESSION = "uhf_session";
    public final static String UHF_POWER = "uhf_power";
    public final static String UHF_INV_CON = "uhf_inv_con";
    public final static String UHF_INV_CON_START = "uhf_inv_con_start";
    public final static String UHF_INV_CON_SIZE = "uhf_inv_con_size";
    public final static String UHF_INV_TIME = "uhf_inv_time";
    public final static String UHF_INV_SLEEP = "uhf_inv_sleep";
    public final static String UHF_IS_STOP_TIME = "uhf_is_stop_time";
    public final static String UHF_M_STOP_TIME = "uhf_m_stop_time";
    public final static String ACTION_SEND_CUSTOM = "action_send_custom";
    public final static String ACTION_KEY_EPC = "action_key_epc";
    public final static String ACTION_KEY_TID = "action_key_tid";
    public final static String ACTION_KEY_RSSI = "action_key_rssi";
    public final static String KEY_TAG_FOUCS = "uhf_tag_foucs";
    /**
     * 快速模式是否支持附加数据
     */
    public final static String UHF_FASTMODE_EXTRA = "uhf_fastmode_enable_extradata";
    /**
     *  智能模式
     */
    public final static String UHF_SMART_MODE = "uhf_smart_mode";

    /**
     * 是否焦点显示epc
     */
    public final static String IS_FOCUS_SHOW = "isFocusShow";
    public final static String EPC_OR_TID = "focus_switch_tid";
    public static boolean isStart = false;
    public static boolean isOpenDev = false;
    public static boolean isOpenServer = true;
    public static int mPrefix = 3;
    public static int mSuffix = 3;
    public static int mStopFlag = 3;
    public static boolean isLongDown = false;
    public static String mStopTime = "10";
    public static boolean isStopTime = false;
    /**
     * 是否过滤
     */
    public static boolean isFilter = false;
    /**
     * 是否启动快速模式
     * Whether to start fast mode
     */
    public static boolean isFastMode = false;
    /**
     * 缓存列表
     */

    /**
     * 单例   Single case
     */
    private static MyApp m_application;
    PlaySoundPool playSoundPool;
    private IUHFService iuhfService;

    public static MyApp getInstance() {
        return m_application;
    }

    /**
     * 获取进程号对应的进程名
     * Gets the process name corresponding to the process number
     *
     * @param pid
     *         进程号
     *
     * @return 进程名
     */
    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        LogUtils.getConfig().setGlobalTag("UHFDemo")
//                .setLogSwitch(true)
//                .setLog2FileSwitch(true)
//                .setDir("sdcard/UHF/log/")
//                .setSaveDays(10);

        m_application = this;
        Context context = getApplicationContext();
        // 获取当前包名   Gets the current package name
        String packageName = context.getPackageName();
        // 获取当前进程名  Gets the current process name
        String processName = getProcessName(android.os.Process.myPid());
        // 设置是否为上报进程    Set whether it is an escalation process

        playSoundPool = PlaySoundPool.getPlaySoundPool(getBaseContext());
        LogUtils.d("application onCreate finish");
    }

    public PlaySoundPool getPlaySoundPool() {
        return playSoundPool;
    }

    public IUHFService getIuhfService() {
        return iuhfService;
    }

    public void releaseIuhfService() {
        if (iuhfService != null) {
            iuhfService.closeDev();
            iuhfService = null;
            UHFManager.closeUHFService();
            LogUtils.d("releaseIuhfService");
        }
    }

    public void setIuhfService() {
        try {
            iuhfService = UHFManager.getUHFService(getApplicationContext());
            LogUtils.d("iuhfService初始化: " + iuhfService);
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showLong(getApplicationContext(), getResources().getString(R.string.dialog_module_none));
            LogUtils.d("iuhfService初始化: 失败" + e.getMessage());
        }

    }

    public void initParam() {
        int i;
        if(!UHFManager.getUHFModel().equals(UHFManager.FACTORY_GUOXIN)) {
            i = iuhfService.setFreqRegion(SharedXmlUtil.getInstance(this).read(MyApp.UHF_FREQ, 1));
            LogUtils.d("===isFirstInit===setFreqRegionResult:" + i);
            SystemClock.sleep(600);
            LogUtils.d("===isFirstInit===FreqRegion:" + iuhfService.getFreqRegion());
            i = iuhfService.setAntennaPower(SharedXmlUtil.getInstance(this).read(MyApp.UHF_POWER, 30));
            LogUtils.d("===isFirstInit===setAntennaPowerResult:" + i);
            SystemClock.sleep(100);
            if (!UHFManager.getUHFModel().equals(UHFManager.FACTORY_YIXIN)) {
                i = iuhfService.setQueryTagGroup(0, SharedXmlUtil.getInstance(this).read(MyApp.UHF_SESSION, 0), 0);
                LogUtils.d("===isFirstInit===setQueryTagGroupResult:" + i);
            }
            SystemClock.sleep(100);
            i = iuhfService.setInvMode(SharedXmlUtil.getInstance(this).read(MyApp.UHF_INV_CON, 0),
                    SharedXmlUtil.getInstance(this).read(MyApp.UHF_INV_CON_START, 0),
                    SharedXmlUtil.getInstance(this).read(MyApp.UHF_INV_CON_SIZE, 0));
            LogUtils.d("===isFirstInit===setInvModeResult:" + i);
            if (UHFManager.getUHFModel().contains(UHFManager.FACTORY_XINLIAN)) {
                iuhfService.setLowpowerScheduler(SharedXmlUtil.getInstance(this)
                        .read(MyApp.UHF_INV_TIME, 50), SharedXmlUtil.getInstance(this)
                        .read(MyApp.UHF_INV_SLEEP, 0));
                iuhfService.setTagfoucs(SharedXmlUtil
                        .getInstance(this).read(MyApp.KEY_TAG_FOUCS, true));
            }
        }
        SystemClock.sleep(100);
        MyApp.mPrefix = SharedXmlUtil.getInstance(this).read(MyApp.SERVER_PREFIX, 3);
        MyApp.mSuffix = SharedXmlUtil.getInstance(this).read(MyApp.SERVER_SUFFIX, 3);
        MyApp.mStopFlag = SharedXmlUtil.getInstance(this).read(MyApp.SERVER_STOP_FLAG, 4);
        MyApp.isLongDown = SharedXmlUtil.getInstance(this).read(MyApp.SERVER_IS_LONG_PRESS, false);
        MyApp.isFilter = SharedXmlUtil.getInstance(this).read(MyApp.SERVER_IS_FILTER, false);
        isStopTime = SharedXmlUtil.getInstance(this).read(UHF_IS_STOP_TIME, false);
        mStopTime = SharedXmlUtil.getInstance(this).read(UHF_M_STOP_TIME, "10");
        LogUtils.d("cache MyApp.SERVER_STOP_FLAG:"+SharedXmlUtil.getInstance(this).read(MyApp.SERVER_STOP_FLAG, 4));
        LogUtils.d("cache MyApp.UHF_POWER:"+SharedXmlUtil.getInstance(this).read(MyApp.UHF_POWER, 30));
        LogUtils.d("cache MyApp.UHF_FREQ:"+SharedXmlUtil.getInstance(this).read(MyApp.UHF_FREQ, 0));
    }

//    @Override
//    public void onTerminate() {
//        stopService(new Intent(this, MyService.class));
//        SharedXmlUtil.getInstance(this).write("server", false);
//        releaseIuhfService();
//        MyApp.isOpenDev = false;
//
//        if (FloatBallManager.getFloatBallManager() != null) {
//            FloatBallManager.getFloatBallManager().closeFloatBall();
//        }
//        if (FloatListManager.getFloatListManager() != null) {
//            FloatListManager.getFloatListManager().closeFloatList();
//        }
//        SharedXmlUtil.getInstance(this).write("floatWindow", "close");
//        playSoundPool.release();
//        super.onTerminate();
//    }

    /**
     * 适用于整个应用
     * android:keepScreenOn="true" 适用于view
     */
    private void weakLockUp() {
        Log.d("screen", "==weakLockUp==");
        Settings.System.putInt(getBaseContext().getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, Integer.MAX_VALUE);
    }



}
