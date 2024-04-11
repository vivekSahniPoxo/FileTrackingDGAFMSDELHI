package com.example.filetracking.search

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.SoundPool
import android.os.*
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.filetracking.MainActivity
import com.example.filetracking.R
import com.example.filetracking.databinding.ActivitySearchBinding
import com.example.filetracking.identify.IdentifyModelClass
import com.example.filetracking.retrofit.RetrofitClient
import com.example.filetracking.utils.App
import com.example.filetracking.utils.InternetConnectionDialog
import com.google.android.material.snackbar.Snackbar
import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager
import com.speedata.libuhf.bean.SpdInventoryData
import com.speedata.libuhf.interfaces.OnSpdInventoryListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class SearchActivity : AppCompatActivity() {
    lateinit var binding:ActivitySearchBinding
    lateinit var progressDialog: ProgressDialog
    lateinit var iuhfService: IUHFService
    lateinit var  handlerr: Handler
    private var soundPool: SoundPool? = null
    private var soundId = 0
    private var soundId1 = 0
    var isInventoryRunning = false
    private var epcToStr: String? = null
    var rfidNo  = ""
    var isSearchingStart = false
    @SuppressLint("SuspiciousIndentation", "ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        iuhfService = UHFManager.getUHFService(this)

        handlerr = Handler()

        binding.imBack.setOnClickListener {
            stopInventoryService()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            iuhfService.closeDev()
            finish()
        }



        binding.tvBtnSearch.setOnClickListener {
            if (binding.tvBtnSearch.text=="Search") {
                hideKeyboard()
                if (binding.etSearchFile.text.isNotEmpty()) {
                    progressDialog = ProgressDialog(this)
                    progressDialog.setMessage("Please wait...")
                    progressDialog.setCancelable(false) // Prevent users from dismissing it by tapping outside
                    progressDialog.show()
                    searchFile(binding.etSearchFile.text.toString())

                } else {
                    binding.etSearchFile.error = "Please input field should not empty"
                }
            } else if(binding.tvBtnSearch.text=="Stop"){
                stopSearching()
            }
        }




        binding.btnStart.setOnClickListener {

            try {
                if (rfidNo.isEmpty()){
                  Snackbar.make(binding.root,"No data found for search",Snackbar.LENGTH_SHORT).show()
                } else {
                    initSoundPool()
                    binding.btnStart.isVisible = false
                    binding.btnStop.isVisible = true
                    iuhfService.inventoryStart()
                    iuhfService.setOnInventoryListener(object : OnSpdInventoryListener {
                        override fun getInventoryData(var1: SpdInventoryData) {
                            handler.sendMessage(handler.obtainMessage(1, var1))
                            Log.d("as3992_6C", "id is $soundId")
                        }

                        override fun onInventoryStatus(status: Int) {
//                        iuhfService.inventoryStart()
                        }
                    })
                }
            } catch (e:Exception){

            }

        }

        binding.btnStop.setOnClickListener {
            isInventoryRunning = false
            binding.btnStart.isVisible=  true
            binding.btnStop.isVisible=  false
            iuhfService.inventoryStop()
            //stopInventoryService()
        }

        }



//
//            initData()
//           // iuhfService.inventoryStart()
//            iuhfService.setOnInventoryListener(object : OnSpdInventoryListener {
//                override fun getInventoryData(var1: SpdInventoryData) {
//                   iuhfService.inventoryStart()
////                    initData()
//                    handler.sendMessage(handler.obtainMessage(1, var1))
//                    Log.w("as3992_6C", "id is $soundId")
//
//                    Log.d("rfidsss",var1.getEpc())
//                }
//
//                override fun onInventoryStatus(status: Int) {
//                    iuhfService.inventoryStart()
//                }
//            })
//
//            if (!isInventoryRunning) {
//                // Start inventory service
//                binding.btnStart.text = "Stop"
//                isInventoryRunning = true
//            } else {
//                // Stop inventory service
//                stopInventoryService()
//
//                binding.btnStart.text = "Start"
//                isInventoryRunning = false
//            }
//        }



    @RequiresApi(Build.VERSION_CODES.M)
    private fun searchFile(RfidNo:String){
        if (!App.get().isConnected()) {
            InternetConnectionDialog(this, null).show()
            return
        }
        RetrofitClient.getResponseFromApi().searchByCRD(RfidNo).enqueue(object:
            Callback<IdentifyModelClass> {
            @SuppressLint("SuspiciousIndentation")
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(call: Call<IdentifyModelClass>, response: Response<IdentifyModelClass>) {


                if (response.isSuccessful) {
                    progressDialog.dismiss()
                    try {
                     if (response.body()?.rfidNo==null){
                        Toast.makeText(this@SearchActivity,"No Data Found",Toast.LENGTH_SHORT).show()
                     }else
                         binding.gifImage.isVisible = true
                        binding.apply {
                            tvCdr.text = response.body()?.cdr.toString()
                            tvCategroy.text = response.body()?.category.toString()
//                            tvCreated.text  = response.body()?.createdAt.toString()
                            tvRemark.text = response.body()?.remark.toString()
                            tvRank.text  = response.body()?.rank.toString()
                            tvCategroy.text = response.body()?.category.toString()
                            // epcToStr = response.body()?.rfidNo.toString()
                            rfidNo =  response.body()?.rfidNo.toString().toUpperCase()
                           // epcToStr = "A20000001234567898765432"
                            binding.tvTemRfid.text = epcToStr
                            val inputDateString = response.body()?.createdAt
                            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS", Locale.US)
                            val outputFormat = SimpleDateFormat("MMM d h:mm a", Locale.US)


                            try {
                                val date = inputDateString?.let { inputFormat.parse(it) }
                                val formattedDate = date?.let { outputFormat.format(it) }
                                tvCreated.text = formattedDate.toString()

                                println("Formatted Date: $formattedDate")
                            } catch (e: Exception) {
                                println("Error parsing date: ${e.message}")
                            }
                            if (response.body()?.isIssued ==1){
                                tvIssue.isVisible = true
                                tvIssueTo.isVisible = true
                                llIssue.isVisible = true
                                llIssueTo.isVisible = true
                                tvIssue.text = response.body()?.isIssued.toString()
                                tvIssueTo.text = response.body()?.issuedTo.toString()

                            } else{
                                tvIssue.isVisible = false
                                tvIssueTo.isVisible = false
                                llIssue.isVisible = false
                                llIssueTo.isVisible = false
                            }

                        }





                    } catch (e: Exception) {
                        Log.d("exception", e.toString())
                    }

                } else if (response.code()==400){
                    progressDialog.dismiss()
                    Toast.makeText(this@SearchActivity,response.message(), Toast.LENGTH_SHORT).show()
                } else if (response.code()==500){
                    progressDialog.dismiss()
                    Toast.makeText(this@SearchActivity,response.message(), Toast.LENGTH_SHORT).show()
                } else if (response.code()==404){
                    progressDialog.dismiss()
                    Toast.makeText(this@SearchActivity,response.message(), Toast.LENGTH_SHORT).show()
                }
                // handle  Api error



            }

            override fun onFailure(call: Call<IdentifyModelClass>, t: Throwable) {
                Toast.makeText(this@SearchActivity,t.localizedMessage, Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }

        })
    }

        @SuppressLint("ResourceAsColor")
        @RequiresApi(Build.VERSION_CODES.M)
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == 105 || keyCode==131) {
            if (rfidNo.isNotEmpty()) {
                if (isSearchingStart == false) {
                    startSearching()
                    iuhfService.setOnInventoryListener(object : OnSpdInventoryListener {
                        override fun getInventoryData(var1: SpdInventoryData) {
                            handler.sendMessage(handler.obtainMessage(1, var1))
                            Log.d("as3992_6C", "id is $soundId")
                        }

                        override fun onInventoryStatus(status: Int) {
                              iuhfService.inventoryStart()
                        }
                    })

                } else {
                    stopSearching()
                 }
            } else{
                Snackbar.make(binding.root,"No Data for search",Snackbar.LENGTH_SHORT).show()
            }

            return true
        }
        else {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                // startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    @SuppressLint("ResourceAsColor")
    fun  startSearching(){
        isSearchingStart = true
        initSoundPool()
        try {
            iuhfService = UHFManager.getUHFService(this)
            iuhfService.openDev()
            iuhfService.antennaPower = 30


        } catch (e:Exception){
            Log.d("Exception",e.toString())
        }
        binding.tvBtnSearch.text = "Stop"
        binding.tvBtnSearch.setBackgroundColor(ContextCompat.getColor(this,R.color.red))
        iuhfService.inventoryStart()
    }

    @SuppressLint("ResourceAsColor")
    fun stopSearching(){
        soundPool!!.release()
        binding.tvBtnSearch.text = "Search"
        binding.tvBtnSearch.setBackgroundColor(ContextCompat.getColor(this,R.color.blue))
        isSearchingStart = false
        iuhfService.inventoryStop()
        iuhfService.closeDev()
    }





//    @RequiresApi(Build.VERSION_CODES.M)
//    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
//        if (keyCode == 105) {
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
//                    progressDialog = ProgressDialog(this)
//                    progressDialog.setMessage("Please wait...")
//                    progressDialog.setCancelable(false) // Prevent users from dismissing it by tapping outside
//                    progressDialog.show()
//                    identifyFile(readHexString)
//                } else {
//                    stringBuilder.append(this.resources.getString(R.string.read_fail)).append(":").append(
//                        ErrorStatus.getErrorStatus(var1.status)).append("\n")
//                }
//                handlerr.sendMessage(handlerr.obtainMessage(1, stringBuilder))
//
//            }
//            val readArea = iuhfService.readArea(1, 2, 6, "00000000")
//            if (readArea != 0) {
//                val err: String = this.resources.getString(R.string.read_fail) + ":" + ErrorStatus.getErrorStatus(readArea) + "\n"
//                handlerr.sendMessage(handlerr.obtainMessage(1, err))
//
//            }
//
//            return true
//        }
//        else {
//            if (keyCode == KeyEvent.KEYCODE_BACK) {
//                // startActivity(Intent(this, MainActivity::class.java))
//                finish()
//            }
//        }
//        return super.onKeyUp(keyCode, event)
//    }



    @RequiresApi(Build.VERSION_CODES.M)
    private fun identifyFile(RfidNo:String){
        if (!App.get().isConnected()) {
            InternetConnectionDialog(this, null).show()
            return
        }
        RetrofitClient.getResponseFromApi().identifyFile(RfidNo).enqueue(object:
            Callback<IdentifyModelClass> {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(call: Call<IdentifyModelClass>, response: Response<IdentifyModelClass>) {


                if (response.isSuccessful) {
                    progressDialog.dismiss()
                    try {

                        binding.apply {
                            tvCdr.text = response.body()?.cdr.toString()
                            tvCategroy.text = response.body()?.category.toString()
                            //tvCreated.text  = response.body()?.createdAt.toString()
                            tvRemark.text = response.body()?.remark.toString()
                            tvRank.text  = response.body()?.rank.toString()
                            tvCategroy.text = response.body()?.category.toString()
                            rfidNo = response.body()?.rfidNo.toString().toUpperCase()
                            Log.d("UpperCaseRFidNo",rfidNo)

                            val inputDateString = response.body()?.createdAt
                            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS", Locale.US)
                            val outputFormat = SimpleDateFormat("MMM d h:mm a", Locale.US)


                            try {
                                val date = inputDateString?.let { inputFormat.parse(it) }
                                val formattedDate = date?.let { outputFormat.format(it) }
                                tvCreated.text = formattedDate.toString()

                                println("Formatted Date: $formattedDate")
                            } catch (e: Exception) {
                                println("Error parsing date: ${e.message}")
                            }
                            if (response.body()?.isIssued ==1){
                                tvIssue.isVisible = true
                                tvIssueTo.isVisible = true
                                llIssue.isVisible=  true
                                llIssueTo.isVisible = true

                                tvIssue.text = response.body()?.isIssued.toString()
                                tvIssueTo.text = response.body()?.issuedTo.toString()

                            } else{
                                tvIssue.isVisible = false
                                tvIssueTo.isVisible = false
                                llIssue.isVisible=  false
                                llIssueTo.isVisible = false
                            }

                        }



                        progressDialog.dismiss()

                    } catch (e: Exception) {
                        Log.d("exception", e.toString())
                    }

                } else if (response.code()==400){
                    progressDialog.dismiss()
                    Toast.makeText(this@SearchActivity,response.message(), Toast.LENGTH_SHORT).show()
                } else if (response.code()==500){
                    progressDialog.dismiss()
                    Toast.makeText(this@SearchActivity,response.message(), Toast.LENGTH_SHORT).show()
                } else if (response.code()==404){
                    progressDialog.dismiss()
                    Toast.makeText(this@SearchActivity,response.message(), Toast.LENGTH_SHORT).show()
                }
                // handle  Api error



            }

            override fun onFailure(call: Call<IdentifyModelClass>, t: Throwable) {
                Toast.makeText(this@SearchActivity,t.localizedMessage, Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }

        })
    }



    private fun stopInventoryService() {
        if (isInventoryRunning) {
            // Stop inventory service
            iuhfService.inventoryStop()
            isInventoryRunning = false
        }
    }

    override fun onBackPressed() {
       // stopInventoryService()


        // Create an intent for the target activity
        val intent = Intent(this, MainActivity::class.java)

        // Add flags to clear the activity stack and start a new task
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

        // Start the target activity
        startActivity(intent)
       // iuhfService.inventoryStop()
        // Finish the current activity
        iuhfService.closeDev()
        finish()
    }




    fun initSoundPool() {
        soundPool = SoundPool(2, AudioManager.STREAM_MUSIC, 0)
        soundId = soundPool!!.load("/system/media/audio/ui/VideoRecord.ogg", 0)
        Log.w("as3992_6C", "id is $soundId")
        soundId1 = soundPool!!.load(this, R.raw.scankey, 0)
//        soundPool = SoundPool(2, AudioManager.STREAM_MUSIC, 0)
//        soundId = soundPool!!.load("/system/media/audio/ui/VideoRecord.ogg", 0)
//        Log.w("as3992_6C", "id is $soundId")
//        //soundId1 = soundPool!!.load(this, R.raw.scankey, 0)
//        soundId = soundPool!!.load(this, R.raw.scankey, 1)
//        soundId1 = soundPool!!.load(this, R.raw.scankey, 1)
    }

    fun initData() {
            // Play the sound here
            initSoundPool()

    }






    override fun onPause() {
        super.onPause()
        if (isSearchingStart==true){
            stopSearching()
        }
        iuhfService.closeDev()
    }






    //新的Listener回调参考代码
    @SuppressLint("HandlerLeak")
    private val handler: Handler = object : Handler() {
        @SuppressLint("SetJavaScriptEnabled")
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 1 && isSearchingStart==true) {
                if (!TextUtils.isEmpty(rfidNo)) {
                    val spdInventoryData = msg.obj as SpdInventoryData
                    val epc = spdInventoryData.getEpc()
                    Log.d("wkfpkp",epc)
                    if (epc == rfidNo) {
                       // binding.mCardView.setBackgroundColor(ContextCompat.getColor(this@SearchActivity, R.color.green2))
                        val rssi = spdInventoryData.getRssi().toInt()
                        Log.d("rssi",rssi.toString())
                        val i = -60
                        val j = -40
                        if (rssi > i) {
                            if (rssi > j) {
                                Log.d("rssiSound1",rssi.toString())
                                binding.gifImage.isVisible = false
                                binding.mCardView.isVisible = true
                                soundPool!!.play(soundId1, 1f, 1f, 0, 0, 3f)
                            } else {
                                Log.d("rssiSound2",rssi.toString())
                                soundPool!!.play(soundId1, 0.6f, 0.6f, 0, 0, 2f)
                            }
                        } else {
                            Log.d("rssiSound3",rssi.toString())
                            soundPool!!.play(soundId1, 0.3f, 0.3f, 0, 0, 1f)
                        }
                    } else{
//                        binding.gifImage.isVisible = true
//                        binding.mCardView.isVisible = false
                    }
                }
            }
        }
    }



    override fun onStop() {
        super.onStop()
        Log.w("stop", "im stopping")
        try {
            soundPool!!.release()
            iuhfService.inventoryStop()
           // super.onStop()
        } catch (e:Exception){
            Log.d("eee",e.toString())
        }
    }




    fun Activity.hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusView = currentFocus
        if (currentFocusView != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocusView.windowToken, 0)
        }
    }

}