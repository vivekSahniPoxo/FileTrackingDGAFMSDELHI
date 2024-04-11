package com.example.filetracking

import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.filetracking.utils.BaseActivity
import com.example.filetracking.utils.MyApp
import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager
import com.speedata.libuhf.bean.SpdInventoryData
import com.speedata.libuhf.interfaces.OnSpdInventoryListener
import java.util.*

class SearchDirectionActivity : BaseActivity(), View.OnClickListener {
    private var soundPool: SoundPool? = null
    private var soundId = 0
    private var soundId1 = 0
    private var iuhfService: IUHFService? = null
    private var epcToStr: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        initView()
        initData()


        initSoundPool()
        iuhfService = UHFManager.getUHFService(this)
        iuhfService?.openDev()
        iuhfService?.inventoryStart()
        iuhfService?.setOnInventoryListener(object : OnSpdInventoryListener {
            override fun getInventoryData(var1: SpdInventoryData) {
                handler.sendMessage(handler.obtainMessage(1, var1))
                Log.w("as3992_6C", "id is $soundId")
            }

            override fun onInventoryStatus(status: Int) {
                iuhfService!!.inventoryStart()
            }
        })
    }

    fun initView() {
        //返回
//        val mIvQuit = findViewById(R.id.search_title_iv) as ImageView
//        //停止
//        val mStopBtn = findViewById(R.id.search_stop) as Button
//        mStopBtn.setOnClickListener(this)
//        mIvQuit.setOnClickListener(this)
//        imageViewSearch = findViewById(R.id.search_card)
    }

    fun initSoundPool() {
        soundPool = SoundPool(2, AudioManager.STREAM_MUSIC, 0)
        soundId = soundPool!!.load("/system/media/audio/ui/VideoRecord.ogg", 0)
        Log.w("as3992_6C", "id is $soundId")
        soundId1 = soundPool!!.load(this, R.raw.scankey, 0)
    }

    fun initData() {
        val options = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
       // Glide.with(this).load(R.drawable.bg_search_card).apply(options).into(imageViewSearch)
        try {
            iuhfService = MyApp.getInstance().iuhfService
            initSoundPool()
            //取消掩码
            iuhfService!!.selectCard(1, "", false)
            bundle
        } catch (e:Exception){

        }
    }

    //        assert bundle != null;
    val bundle: Unit
        get() {
            val intent: Intent = getIntent()
            val bundle = intent.extras
            //        assert bundle != null;
            epcToStr = "E280117000000214249CD4C9"
        }

//     override fun onResume() {
//        super.onResume()
//        initSoundPool()
//        iuhfService!!.inventoryStart()
//        iuhfService!!.setOnInventoryListener(object : OnSpdInventoryListener {
//            override fun getInventoryData(var1: SpdInventoryData) {
//                handler.sendMessage(handler.obtainMessage(1, var1))
//                Log.w("as3992_6C", "id is $soundId")
//            }
//
//            override fun onInventoryStatus(status: Int) {
//                iuhfService!!.inventoryStart()
//            }
//        })
//    }

    //新的Listener回调参考代码
    @SuppressLint("HandlerLeak")
    private val handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 1) {
                if (!TextUtils.isEmpty(epcToStr)) {
                    val spdInventoryData = msg.obj as SpdInventoryData
                    val epc = spdInventoryData.getEpc()
                    if (epc == epcToStr) {
                        val rssi = spdInventoryData.getRssi().toInt()
                        val i = -60
                        val j = -40
                        if (rssi > i) {
                            if (rssi > j) {
                                soundPool!!.play(soundId1, 1f, 1f, 0, 0, 3f)
                            } else {
                                soundPool!!.play(soundId1, 0.6f, 0.6f, 0, 0, 2f)
                            }
                        } else {
                            soundPool!!.play(soundId1, 0.3f, 0.3f, 0, 0, 1f)
                        }
                    }
                }
            }
        }
    }

//    override fun onClick(v: View) {
//        when (v.id) {
//            R.id.search_title_iv, R.id.search_stop -> {
//                iuhfService!!.inventoryStop()
//                finish()
//            }
//            else -> {}
//        }
//    }

    protected override fun onStop() {
        Log.w("stop", "im stopping")
        soundPool!!.release()
        super.onStop()
    }

    override fun onClick(p0: View?) {

    }
}