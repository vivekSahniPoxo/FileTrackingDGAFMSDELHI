package com.example.filetracking.stock_take

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.*
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.filetracking.MainActivity
import com.example.filetracking.R
import com.example.filetracking.databinding.ActivityStockTakeBinding
import com.example.filetracking.inventory.data.RfidItem
import com.example.filetracking.retrofit.RetrofitClient
import com.example.filetracking.stock_take.adapetr.StockTakeAdapter
import com.example.filetracking.stock_take.data.Rfid
import com.example.filetracking.stock_take.data.StockData
import com.example.filetracking.stock_take.view.StockTakeView
import com.example.filetracking.utils.*
import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager
import com.speedata.libuhf.bean.SpdInventoryData
import com.speedata.libuhf.interfaces.OnSpdInventoryListener
import com.speedata.libuhf.utils.StringUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class StockTake : AppCompatActivity() {
    private var soundPool: SoundPool? = null
    private var soundId = 0
    var isInventoryRunning = false
    lateinit var iuhfService: IUHFService
    var lastTimeMillis: Long = 0
    lateinit var binding:ActivityStockTakeBinding
    lateinit var stockTakeAdapter: StockTakeAdapter
    lateinit var rfiList:ArrayList<Rfid>
    lateinit var rfid:ArrayList<String>
    lateinit var rfidStockS:ArrayList<StockData>
    lateinit var rfidNo:ArrayList<RfidItem>
    lateinit var progressDialog: ProgressDialog
    lateinit var  handler: Handler
    var isSearchingStart = false
    private var jishu = 0
    lateinit var listRfid:ArrayList<Rfid>
    private val handlerthread = Handler(Looper.getMainLooper())
    lateinit var temList:ArrayList<String>
    private var scant: Long = 0
    private val uhfCardBeanList: ArrayList<UhfCardBean> = ArrayList<UhfCardBean>()
    //lateinit var intentBulkMovement: ArrayList<InternalBulkMovement>
    private var EPCNum = 0
    private var wakeLock: PowerManager.WakeLock? = null
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStockTakeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        iuhfService = UHFManager.getUHFService(this)
        handler = Handler()
        rfiList = arrayListOf()
        rfid = arrayListOf()
        rfidStockS = arrayListOf()
        rfidNo = arrayListOf()
        listRfid = arrayListOf()
        temList = arrayListOf()



//        try {
//
//            iuhfService = UHFManager.getUHFService(this)
//            iuhfService.openDev()
//            iuhfService.antennaPower = 30
//            initSoundPool()
//
//
//
//        } catch (e:Exception){
//            // Log.d("Exception",e.toString())
//        }
//        isInventoryRunning = true
//        iuhfService.inventoryStart()



        binding.btnImBack.setOnClickListener {
           // stopInventoryService()
            // Create an intent for the target activity
            val intent = Intent(this@StockTake, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            iuhfService.closeDev()
            finish()
        }

        binding.btnSubmit.setOnClickListener {

            if (binding.etUserId.text?.isNotEmpty() == true) {
                stockTakeView(StockData(binding.etUserId.text.toString(), rfidNo))
            } else{
                binding.etUserId.error = "Name field should not be empty"
            }


        }

        binding.btnView.setOnClickListener {
            if(isInventoryRunning==true){
                stopSearching()
            }
            val intent = Intent(this, StockTakeView::class.java)
            intent.putStringArrayListExtra("myListKey", rfid)
            startActivity(intent)
        }


        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)

                when (msg.what) {
                    1 -> {
                        synchronized(uhfCardBeanList) {
                            scant++
                            val var1 = msg.obj as SpdInventoryData

                            val uhfCardBean = UhfCardBean(var1.epc, 1, var1.rssi, var1.tid)

                            val index = uhfCardBeanList.indexOf(uhfCardBean)
                            if (index == -1) {
                                uhfCardBeanList.add(uhfCardBean)
                                rfiList.add(Rfid(var1.epc))
                                EPCNum = uhfCardBeanList.size
                                binding.tvCount.text = "count ${EPCNum}"

                                //  if (!rfid.contains(epc)) {
                                rfid.add(var1.epc)
                                listRfid.add(Rfid(var1.epc))
                                // }

                                runOnUiThread {
                                    //val listSize = rfiList.distinct().size

                                    rfidNo.add(RfidItem(var1.epc, "Found"))
                                    Log.d("TotalRfidNo",rfidNo.toString())
                                    val stockTakeAdapter = StockTakeAdapter(rfid)
                                    binding.rfidList.adapter = stockTakeAdapter
                                    stockTakeAdapter.notifyDataSetChanged()
                                }

                            } else {
                                val temp = uhfCardBeanList[index]
                                temp.valid++
                                temp.rssi = var1.rssi
                                if (!var1.tid.isNullOrEmpty()) {
                                    temp.tidUser = var1.tid
                                }
                                uhfCardBeanList[index] = temp
                            }
                            runOnUiThread(kotlinx.coroutines.Runnable {
                                temList.add(var1.epc)
                                binding.tvCountAll.text = temList.size.toString()
                                // temp.clear()
                            })

                        }
                    }
                }
            }
        }

        binding.btnRead.setOnClickListener {
            if (!isInventoryRunning) {
                startSearching()
                //val executor = Executors.newFixedThreadPool(5)
                iuhfService.setOnInventoryListener(object : OnSpdInventoryListener {
                    @SuppressLint("NotifyDataSetChanged", "SuspiciousIndentation")
                    override fun getInventoryData(var1: SpdInventoryData) {
                        //executor.execute {

                        handler.sendMessage(handler.obtainMessage(1, var1))
                        val timeMillis = System.currentTimeMillis()
                        val l = timeMillis - lastTimeMillis
                        if (l < 100) {
                            return
                        }
                        lastTimeMillis = System.currentTimeMillis()
                        soundPool!!.play(soundId, 1f, 1f, 0, 0, 1f)

//                        try {
//
//                            handlerthread.post {
//                                temList.add(var1.getEpc())
//                                binding.tvCountAll.text = temList.size.toString()
//                               // temList.clear()
//                            }
//
//                            val timeMillis = System.currentTimeMillis()
//                            val l: Long = timeMillis - lastTimeMillis
//                            if (l < 100) {
//                                return
//                            }
//                            lastTimeMillis = System.currentTimeMillis()
//
//                                try {
//                                    soundPool?.play(soundId, 1f, 1f, 0, 0, 1f)
//                                } catch (e: Exception) {
//                                    e.printStackTrace()
//                                }
//
//                            val epc = var1.getEpc().uppercase()
//
//                            if (!rfiList.contains(Rfid(var1.getEpc()))) {
//                                rfiList.add(Rfid(epc))
//                              //  if (!rfid.contains(epc)) {
//                                    rfid.add(epc)
//                                    listRfid.add(Rfid(epc))
//                               // }
//                                runOnUiThread {
//                                     //val listSize = rfiList.distinct().size
//                                    binding.tvCount.text = "count ${rfid.size}"
//                                    rfidNo.add(RfidItem(epc, "Found"))
//                                    Log.d("TotalRfidNo",rfidNo.toString())
//                                    val stockTakeAdapter = StockTakeAdapter(rfid)
//                                    binding.rfidList.adapter = stockTakeAdapter
//                                    stockTakeAdapter.notifyDataSetChanged()
//                                }
//                            }
//
//
//                        } catch (e: Exception) {
//
//                        }
//                    //}
                    }

                    override fun onInventoryStatus(p0: Int) {
//                        Looper.prepare()
//                        if (p0 == 65277) {
//                            iuhfService.closeDev()
//                            SystemClock.sleep(100)
//                            iuhfService.openDev()
//                            iuhfService.inventoryStart()
//                        } else {
//                            iuhfService.inventoryStart()
//                        }
//                        Looper.loop()
//                        threadpooling(p0)
                    }
                })

//                binding.btnRead.text = "Stop"
//                iuhfService.inventoryStart()
//                isInventoryRunning = true
               // stopSearching()

            } else if (isInventoryRunning) {
//                binding.btnRead.text = "Start"
//                isInventoryRunning = false
//                iuhfService.inventoryStop()
                stopSearching()

            }
        }


//        binding.btnRead.setOnClickListener {
//            if (isSearchingStart == false) {
//                startSearching()
//              //  if (iuhfService!=null){
//                iuhfService.setOnInventoryListener(object : OnSpdInventoryListener {
//                    @SuppressLint("NotifyDataSetChanged", "SuspiciousIndentation")
//                    override fun getInventoryData(var1: SpdInventoryData) {
//                        try {
//                            val timeMillis = System.currentTimeMillis()
//                            val l: Long = timeMillis - lastTimeMillis
//                            if (l < 100) {
//                                return
//                            }
//                            lastTimeMillis = System.currentTimeMillis()
//                            runOnUiThread {
//                                try {
//                                    soundPool?.play(soundId, 1f, 1f, 0, 0, 1f)
//                                } catch (e: Exception) {
//                                   // Log.d("Sound Playback Error", e.toString())
//                                }
//                            }
//                          //  Log.d("RFFFF", var1.getEpc())
//
//                            val epc = var1.getEpc()
//
//                            // Check if the epc is not already in the list
//                           // if (!rfiList.any { it.rfid == epc }) {
//                                rfiList.add(Rfid(epc))
//                            if (!rfid.contains(epc)) {
//                                rfid.add(epc)
//                                listRfid.add(Rfid(epc))
//                            }
//                                runOnUiThread {
//                                    val listSize = rfiList.distinct().size
//                                    binding.tvCount.text = "count $listSize"
//                                    rfidNo.add(RfidItem(epc,"Found"))
//                                    val stockTakeAdapter = StockTakeAdapter(listRfid)
//                                    binding.rfidList.adapter = stockTakeAdapter
//                                    stockTakeAdapter.notifyDataSetChanged()
//                                }
//
//
//                           // }
//                        } catch (e: Exception) {
//                          //  Log.d("exception", e.toString())
//                        }
//                    }
//
//                    override fun onInventoryStatus(p0: Int) {
//                        threadpooling(p0)
//                   }
//                })
//
//                binding.btnRead.text = "Stop"
//               // isInventoryRunning = true
//                isSearchingStart = true
//                startSearching()
//
//            } else {
//                binding.btnRead.text = "Start"
////                isInventoryRunning = false
//               // iuhfService.inventoryStop()
//                isSearchingStart =  false
//                stopSearching()
//                // Remove the periodic UI update callbacks
//                //handler.removeCallbacksAndMessages(null)
//            }
//            }
        }
   // }



    fun threadpooling(p0:Int) {
        // Create a thread pool with 4 threads
        val threadPool: ExecutorService = Executors.newFixedThreadPool(4)

        // Define a Runnable with your code
        val runnable = Runnable {
            Looper.prepare()
            if (p0 == 65277) {
                // Log.d("p0", p0.toString())
                iuhfService.closeDev()
                SystemClock.sleep(100)
                startSearching()
            } else {
                iuhfService.inventoryStart()
            }
            Looper.loop()
        }

        // Submit the Runnable to the thread pool
        threadPool.submit(runnable)

        // Shutdown the thread pool when done
        threadPool.shutdown()
    }



    fun initSoundPool() {
        soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val attributes = AudioAttributes.Builder()
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .build()
            SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(attributes)
                .build()
        } else {
            SoundPool(1, AudioManager.STREAM_MUSIC, 0)
        }
        soundId = soundPool?.load(this, R.raw.beep, 1)!!
    }



    override fun onPause() {
        super.onPause()
        if (isInventoryRunning==true){
            stopSearching()

        }

        iuhfService.closeDev()
    }

    override fun onStop() {
        super.onStop()
        try {
            soundPool?.release()
        } catch (e:Exception){

        }
        iuhfService.closeDev()
    }










    private fun stopInventoryService() {
        if (isInventoryRunning) {
            // Stop inventory service
            iuhfService.inventoryStop()
            isInventoryRunning = false
        }
    }

    override fun onBackPressed() {
      //  stopInventoryService()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        iuhfService.closeDev()
        finish()
    }



//    @RequiresApi(Build.VERSION_CODES.M)
//    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
//        if (keyCode == KeyEvent.KEYCODE_BUTTON_R2) {
//            iuhfService.setOnReadListener { var1 ->
//                val stringBuilder = StringBuilder()
//                val epcData = var1.epcData
//                val hexString = StringUtils.byteToHexString(epcData, var1.epcLen)
//                if (!TextUtils.isEmpty(hexString)) {
//                    stringBuilder.append("EPC：").append(hexString).append("\n")
//                }
//                if (var1.status == 0) {
//                    val readData = var1.readData
//                    val readHexString = StringUtils.byteToHexString(readData, var1.dataLen)
//                    stringBuilder.append("ReadData:").append(readHexString).append("\n")
//                    Toast.makeText(this,readHexString,Toast.LENGTH_SHORT).show()
//                    if (!rfiList.any { it.rfid == readHexString }) {
//                        rfiList.add(Rfid(readHexString))
//                        rfid.add(readHexString)
////                        rfidStockS.add(StockData.StockTake("",readHexString,""))
//                       // rfidStockS.add(StockData.StockTake(readHexString))
//                        rfidNo.add(RfidItem(readHexString,"Found"))
//                        val stockTakeAdapter = StockTakeAdapter(rfid)
//                        binding.rfidList.adapter = stockTakeAdapter
//                        stockTakeAdapter.notifyDataSetChanged()
//                    }
//                } else {
//                    stringBuilder.append(this.resources.getString(R.string.read_fail)).append(":").append(
//                        ErrorStatus.getErrorStatus(var1.status)).append("\n")
//                }
//                handler.sendMessage(handler.obtainMessage(1, stringBuilder))
//
//            }
//            val readArea = iuhfService.readArea(1, 2, 6, "00000000")
//            if (readArea != 0) {
//                val err: String = this.resources.getString(R.string.read_fail) + ":" + ErrorStatus.getErrorStatus(readArea) + "\n"
//                handler.sendMessage(handler.obtainMessage(1, err))
//
//            }
//
//            return true
//        }
//        else {
//            if (keyCode == KeyEvent.KEYCODE_BACK) {
//                // startActivity(Intent(this, MainActivity::class.java))
//                iuhfService.closeDev()
//                finish()
//            }
//        }
//        return super.onKeyUp(keyCode, event)
//    }



//    override fun onRestart() {
//        super.onRestart()
//        stopInventoryService()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        stopInventoryService()
//    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun stockTakeView(stockData: StockData) {
        if (!App.get().isConnected()) {
            InternetConnectionDialog(this, null).show()
            return
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait...")
        progressDialog.setCancelable(false) // Prevent users from dismissing it by tapping outside
        progressDialog.show()

        RetrofitClient.getResponseFromApi().stockTake(stockData).enqueue(object : Callback<String> {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(call: Call<String>, response: Response<String>) {
                progressDialog.dismiss() // Dismiss the progressDialog if it's not null

                if (response.isSuccessful) {
                    stopInventoryService()
                    CacheUtils.clearAppCache(this@StockTake)
                    val intent = Intent(this@StockTake, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                    Toast.makeText(this@StockTake, response.body(), Toast.LENGTH_SHORT).show()
                    try {
                        progressDialog.dismiss()
                        rfiList.clear()
                        rfid.clear()
                        rfidStockS.clear()
                        stockTakeAdapter.notifyDataSetChanged()


                    } catch (e: Exception) {
                      //  Log.d("exception", e.toString())
                    }
                } else if (response.code() == 400) {
                    Toast.makeText(this@StockTake, response.message(), Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                } else if (response.code() == 500) {
                    Toast.makeText(this@StockTake, response.message(), Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                } else if (response.code() == 404) {
                    Toast.makeText(this@StockTake, response.message(), Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
                // Handle API errors
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                progressDialog.dismiss() // Dismiss the progressDialog if it's not null
                Toast.makeText(this@StockTake, t.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }





    @SuppressLint("ResourceAsColor")
    fun stopSearching(){
        soundPool!!.release()
        binding.btnRead.text = "Start"
        isInventoryRunning = false
        iuhfService.inventoryStop()
        iuhfService.closeDev()
    }


    @SuppressLint("ResourceAsColor")
    fun  startSearching(){
        isInventoryRunning = true
        initSoundPool()
        try {
            iuhfService = UHFManager.getUHFService(this)
            iuhfService.openDev()
            iuhfService.antennaPower = 30


        } catch (e:Exception){
            Log.d("Exception",e.toString())
        }
        binding.btnRead.text = "Stop"
        iuhfService.inventoryStart()
    }







}