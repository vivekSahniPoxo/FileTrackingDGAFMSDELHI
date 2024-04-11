package com.example.filetracking.utils;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;
import com.example.filetracking.R;
import com.speedata.libuhf.UHFManager;
import com.speedata.libuhf.interfaces.OnSpdBanMsgListener;
import com.speedata.libutils.ConfigUtils;



/**
 * @author zzc
 */
public class BaseActivity extends Activity {
    public static boolean isLowPower = false;
    public static boolean isHighTemp = false;
    public String model;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //全屏显示  Full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //强制为竖屏     Force to vertical screen mode
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        setBuilder();
        Log.d("zzc", "BaseActivity onCreate");
        model = ConfigUtils.getModel();
        if (model.contains("FG80")) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            LogUtils.d("SCREEN_ORIENTATION_LANDSCAPE");
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private void setBuilder() {
        UHFManager uhfManager = new UHFManager();
        uhfManager.setOnBanMsgListener(new OnSpdBanMsgListener() {
            @Override
            public void getBanMsg(String var1) {
                Log.e("zzc:UHFService", "====监听报警====");
                if (var1.contains("Low")) {
                    isLowPower = true;
                    var1 = BaseActivity.this.getResources().getString(R.string.low_power);
                } else if (var1.contains("High")) {
                    isHighTemp = true;
                    var1 = BaseActivity.this.getResources().getString(R.string.high_temp);
                }
                final String finalVar = var1;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        FloatWarnManager.getInstance(getApplicationContext(), finalVar);
//                        FloatWarnManager floatWarnManager = FloatWarnManager.getFloatWarnManager();
//                        if (floatWarnManager != null) {
//                            FloatWindow.get("FloatWarnTag").show();
//                        }
                    }
                });
            }
        });
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (MyApp.getInstance().getIuhfService() != null) {
//            MyApp.getInstance().getIuhfService().inventoryStop();
//            MyApp.isStart = false;
//        }
//    }

    @Override
    protected void onDestroy() {
        Log.d("zzc", "BaseActivity onDestroy");
        super.onDestroy();
    }
}
