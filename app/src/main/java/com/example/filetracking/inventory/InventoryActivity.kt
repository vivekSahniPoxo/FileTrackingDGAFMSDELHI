package com.example.filetracking.inventory

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.*
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.ThreadUtils.runOnUiThread
import com.example.filetracking.MainActivity
import com.example.filetracking.R
import com.example.filetracking.databinding.ActivityInventoryBinding

import com.example.filetracking.inventory.adapter.FetchCategoryByID
import com.example.filetracking.inventory.adapter.GetCategoryAdapter
import com.example.filetracking.inventory.adapter.GetRankAdapter
import com.example.filetracking.inventory.adapter.RankByCategoryAdapter
import com.example.filetracking.inventory.data.FetchRankByCategoryModelClass
import com.example.filetracking.inventory.data.GetRankModelClass
import com.example.filetracking.inventory.data.InventoryDataClass
import com.example.filetracking.inventory.data.RfidItem
import com.example.filetracking.retrofit.RetrofitClient
import com.example.filetracking.stock_take.adapetr.StockTakeAdapter
import com.example.filetracking.stock_take.data.Rfid
import com.example.filetracking.utils.*
import com.google.android.material.snackbar.Snackbar
import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager
import com.speedata.libuhf.bean.SpdInventoryData
import com.speedata.libuhf.interfaces.OnSpdInventoryListener
import com.speedata.libuhf.utils.StringUtils

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class InventoryActivity : AppCompatActivity() {
    // Create a Mutex for synchronizing access to the rfidNoList
    val rfidNoListMutex = Mutex()
    var rfidNo = ""
    private var scant: Long = 0
    private val uhfCardBeanList: ArrayList<UhfCardBean> = ArrayList<UhfCardBean>()
    //lateinit var intentBulkMovement: ArrayList<InternalBulkMovement>
    private var EPCNum = 0

    private var scanningJob: Job? = null
    private val temSet = HashSet<String>()

    lateinit var binding:ActivityInventoryBinding
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    var categoryID = 0
    var selectedCategory = ""
    var lastTimeMillis: Long = 0
    private var jishu = 0
    lateinit var progressDialog: ProgressDialog
    lateinit var getCategoryAdapter: GetCategoryAdapter
    lateinit var fetchCategoryByID: FetchCategoryByID
    private var soundPool: SoundPool? = null
    private var soundId = 0
    lateinit var mList:ArrayList<FetchCategoryByIDModelClass>
    var isInventoryRunning = false
    lateinit var iuhfService: IUHFService
    lateinit var  handler: Handler
    private val handlerthread = Handler(Looper.getMainLooper())
    lateinit var temList:ArrayList<String>
    lateinit var rfidStockS:ArrayList<InventoryDataClass>
    var repsonseBody = ""
    lateinit var rfidNoList:ArrayList<RfidItem>
    lateinit var getRankBycategory:ArrayList<GetRankModelClass.GetRankModelClassItem>
    lateinit var getRankAdapter: GetRankAdapter
    lateinit var AllRfidList:ArrayList<String>
    lateinit var countList:ArrayList<RfidItem>


    var isCheck = true
    lateinit var category:ArrayList<CategoryModelClass.CategoryModelClassItem>
    lateinit var rankbyCategroy:ArrayList<FetchRankByCategoryModelClass.FetchRankByCategoryModelClassItem>
    lateinit var rankByCategoryAdapter: RankByCategoryAdapter
    var isSearchingStart = false
    @OptIn(DelicateCoroutinesApi::class)
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryBinding.inflate(layoutInflater)
        category = arrayListOf()
        rankbyCategroy = arrayListOf()
        temList = arrayListOf()
        rfidStockS = arrayListOf()
        getRankBycategory = arrayListOf()
        AllRfidList = arrayListOf()
        countList  = arrayListOf()
        category.add(CategoryModelClass.CategoryModelClassItem("Choose Cat..",0))
        rankbyCategroy.add(FetchRankByCategoryModelClass.FetchRankByCategoryModelClassItem("Choose rank"))

        setContentView(binding.root)

       iuhfService = UHFManager.getUHFService(this)

//        initSoundPool()
        handler = Handler()
        mList = arrayListOf()
        rfidNoList = arrayListOf()
        getRankBycategory = arrayListOf()

        binding.imBack.setOnClickListener {
            if (isInventoryRunning==true) {
                stopSearching()
            }
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            //iuhfService.closeDev()
            finish()
        }


        binding.BackButton.setOnClickListener {
            if (isInventoryRunning==true) {
                stopSearching()
            }
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

            finish()
        }
        progressDialog = ProgressDialog(this)


        getCategory()


        binding.NewButton.setOnClickListener {
            fetchCategoryByID.clearAllData()
            if (isInventoryRunning==true) {
                stopSearching()
            }
            rfidNoList.clear()
            temList.clear()
            fetchCategoryByID.refreshAdapter()
            binding.Total.text = ""
            binding.Found.text=""
            binding.notFound.text=""

        }


        binding.SubmitButton.setOnClickListener {
            try {
                if (binding.etUserId.text?.isEmpty() == true){
                    binding.etUserId.error = "Name field should not be empty"
                } else {

                    val total = binding.Total.text.toString()
                    submitInventory(InventoryDataClass(selectedCategory,binding.etUserId.text.toString(),binding.Found.text.toString().toInt(),rfidNoList,total.toInt()))
//                    submitInventory(InventoryDataClass(binding.Found.text.toString().toInt(), rfidStockS, total.toInt(), binding.etUserId.text.toString(), selectedCategory))
                }
            } catch (e:Exception){

            }

        }




        val temp = arrayListOf<String>()

        binding.btnStart.setOnClickListener {
            val selectedItem = binding.spType.selectedItemPosition

            if (repsonseBody.isEmpty() || selectedItem == 0) {
            Snackbar.make(binding.root, "No data found for search", Snackbar.LENGTH_SHORT).show()
        } else {
            if (!isInventoryRunning) {
                    startSearching()

                   // scanningJob = coroutineScope.launch(Dispatchers.IO) {
                        iuhfService.setOnInventoryListener(object : OnSpdInventoryListener {
                            override fun getInventoryData(var1: SpdInventoryData) {
                                handler.sendMessage(handler.obtainMessage(1, var1))
                                val timeMillis = System.currentTimeMillis()
                                val l = timeMillis - lastTimeMillis
                                if (l < 100) {
                                    return
                                }
                                lastTimeMillis = System.currentTimeMillis()
                                soundPool!!.play(soundId, 1f, 1f, 0, 0, 1f)


//                               ThreadUtils.runOnUiThread {
//                                   if (!temp.contains(var1.getEpc())) {
//                                       temp.add(var1.getEpc())
//                                       binding.count.text = temp.size.toString()
//                                   }
//                                    //temp.clear()
//                                }
//                                 coroutineScope.launch{
//                                     Log.d("vccc",var1.getEpc())
//                                     handleInventoryData(var1.getEpc())
//                            }

                            }

                            override fun onInventoryStatus(status: Int) {
//                                Looper.prepare()
//                                if (status == 65277) {
//                                    iuhfService.closeDev()
//                                    SystemClock.sleep(100)
//                                    iuhfService.openDev()
//                                    iuhfService.inventoryStart()
//                                } else {
//                                    iuhfService.inventoryStart()
//                                }
//                                Looper.loop()
                            }

                                // Log.d("statusCode",status.toString())

                        })
                   // }
                } else {
                    stopSearching()
                    scanningJob?.cancel()
                }
                 }
            }



        binding.spType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {


                }
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                    try {
                        selectedCategory = binding.spType.selectedItem.toString().substringAfter("category=").substringBefore(",")
                        val selectedEmpId = parent?.getItemAtPosition(position) as CategoryModelClass.CategoryModelClassItem
                        categoryID = selectedEmpId.categoryid
                        progressDialog = ProgressDialog(this@InventoryActivity)
                        progressDialog.setMessage("Please wait...")
                        progressDialog.setCancelable(false) // Prevent users from dismissing it by tapping outside
                        progressDialog.show()
                        fetchByCategoryID(selectedEmpId.categoryid)
                        getRankByCategory(selectedEmpId.categoryid)
                        //getRankByCategory(selectedEmpId.categoryid)
                        //binding.recyclerview.scrollToPosition(fetchCategoryByID.positionActivity)
                        fetchCategoryByID.clearAllData()
                        rfidNoList.clear()
                        temList.clear()
                        fetchCategoryByID.refreshAdapter()
                        // binding.Found.text = fetchCategoryByID.totalCountSize.toString()


                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

                    } catch (e: Exception) {

                    }



                }

            }


            binding.spRank.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {


                }
                @RequiresApi(Build.VERSION_CODES.M)
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                    try {


                        // val selectedRank = binding.spRank.selectedItem.toString().substringAfter("name=").substringBefore(",")
                        val selectedItem = binding.spRank.selectedItem as GetRankModelClass.GetRankModelClassItem

                       // Log.d("selectedRank",selectedItem.name)
                        // val selectedRank = parent?.getItemAtPosition(position).toString()
                        // categoryID = selectedEmpId

                        //fetchRankCategory(selectedEmpId)
                        if (binding.spRank.selectedItemPosition!=0){
                            progressDialog = ProgressDialog(this@InventoryActivity)
                            progressDialog.setMessage("Please wait...")
                            progressDialog.setCancelable(false) // Prevent users from dismissing it by tapping outside
                            progressDialog.show()
                            fetchCrdByRanlk(selectedItem.name,categoryID)
//                        fetchCategoryByID.clearAllData()
//                        rfidNoList.clear()
//                        temList.clear()
                            fetchCategoryByID.refreshAdapter()
                        }

                        // rankByCategoryAdapter.notifyDataSetChanged()
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

                    } catch (e: Exception) {

                    }



                }

            }


        handler = object : Handler(Looper.getMainLooper()) {
            @SuppressLint("SuspiciousIndentation")
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)

                when (msg.what) {
                    1 -> {
                        synchronized(uhfCardBeanList) {
                            scant++
                            val var1 = msg.obj as SpdInventoryData

                            val uhfCardBean = UhfCardBean(var1.epc, 1, var1.rssi, var1.tid)
                            rfidNo =  var1.epc
                            //Log.d("vvvv",var1.epc)
                            val index = uhfCardBeanList.indexOf(uhfCardBean)
                            if (index == -1) {
                                uhfCardBeanList.add(uhfCardBean)


                                runOnUiThread {
                                    val foundItem = rfidNoList.find { it.rfidno == rfidNo }
                                    if (foundItem != null) {
                                        foundItem.status = "Found"
                                        fetchCategoryByID.highlightedPositions.add(rfidNo)
                                        //fetchCategoryByID.changeRfidTag(temList)
                                        fetchCategoryByID.updateStatus(rfidNo, "Found")


                                        val foundValue = rfidNoList.count { it.status.equals("Found", ignoreCase = true) }
                                        binding.Found.text = foundValue.toString()

                                        val total = rfidNoList.count()
                                        try {
                                            val notFound = total - foundValue
                                            binding.notFound.text = notFound.toString()
                                        } catch (e: Exception) {
                                            // Handle NumberFormatException
                                        }
                                        fetchCategoryByID.refreshAdapter()
                                    }


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
                                binding.count.text = temList.size.toString()
                                // temp.clear()
                            })

                        }
                    }
                }
            }
        }




        }






    fun countFoundStatus(): Int {
        return rfidNoList.count { it.status.equals("Found", ignoreCase = true) } ?: 0
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getCategory(){
        if (!App.get().isConnected()) {
            InternetConnectionDialog(this, null).show()
            return
        }
        RetrofitClient.getResponseFromApi().getCategory().enqueue(object: Callback<CategoryModelClass> {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(call: Call<CategoryModelClass>, response: Response<CategoryModelClass>) {


                if (response.isSuccessful) {
                    progressDialog.dismiss()

                        try {
                            if (response.body()?.isEmpty() == true) {
                                binding.tvNoDataFound.isVisible = true

                            } else{
                                response.body()?.forEach {
                                    category.add(CategoryModelClass.CategoryModelClassItem(it.category, it.categoryid))

                                    getCategoryAdapter = GetCategoryAdapter(this@InventoryActivity, category)
                                    binding.spType.adapter = getCategoryAdapter
                                    binding.tvNoDataFound.isVisible = false
                                }
                            }



                        } catch (e: Exception) {
                            Log.d("exception", e.toString())

                    }
                } else if (response.code()==400){
                        progressDialog.dismiss()
                        Toast.makeText(this@InventoryActivity,response.message(),Toast.LENGTH_SHORT).show()
                    } else if (response.code()==500){
                    progressDialog.dismiss()
                    Toast.makeText(this@InventoryActivity,response.message(),Toast.LENGTH_SHORT).show()
                } else if (response.code()==404){
                    progressDialog.dismiss()
                    Toast.makeText(this@InventoryActivity,response.message(),Toast.LENGTH_SHORT).show()
                }
                    // handle  Api error
            }
            override fun onFailure(call: Call<CategoryModelClass>, t: Throwable) {
                Toast.makeText(this@InventoryActivity,t.localizedMessage, Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }

        })
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun fetchByCategoryID(ID:Int){
        if (!App.get().isConnected()) {
            InternetConnectionDialog(this, null).show()
            return
        }
        RetrofitClient.getResponseFromApi().fetchCategoryByID(ID).enqueue(object: Callback<FetchCategoryByIDModelClass> {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(call: Call<FetchCategoryByIDModelClass>, response: Response<FetchCategoryByIDModelClass>) {


                if (response.isSuccessful) {
                    progressDialog.dismiss()

                        try {
                            repsonseBody = response.body().toString()
                            fetchCategoryByID = FetchCategoryByID(response.body()!!)
                            binding.recyclerview.adapter = fetchCategoryByID
                            binding.Total.text = response.body()!!.size.toString()

                            binding.notFound.text = "0"
                            binding.Found.text = "0"
//                            val selectedItem = binding.spType.selectedItemPosition
//                            if(selectedItem!=0) {
//                                Snackbar.make(binding.root, "Please wait. Data is sorting", Snackbar.LENGTH_SHORT).show()
//                            }
                            response.body()!!.forEach {
//                                rfidStockS.add(StockData.StockTake(it.cdr, it.rfidNo,"Not Found"))
                              //  rfidStockS.add(InventoryDataClass.Rfid(it.rfidNo))
                                rfidNoList.add(RfidItem(it.rfidNo.uppercase().trim(),"Not Found"))
                                fetchCategoryByID.notifyDataSetChanged()
                            }




                        } catch (e: Exception) {
                            Log.d("exception", e.toString())

                    }
                } else if (response.code()==400){
                    progressDialog.dismiss()
                    Toast.makeText(this@InventoryActivity,response.message(),Toast.LENGTH_SHORT).show()
                } else if (response.code()==500){
                    progressDialog.dismiss()
                    Toast.makeText(this@InventoryActivity,response.message(),Toast.LENGTH_SHORT).show()
                } else if (response.code()==404){
                    progressDialog.dismiss()
                    Toast.makeText(this@InventoryActivity,response.message(),Toast.LENGTH_SHORT).show()
                }
                // handle  Api error



            }

            override fun onFailure(call: Call<FetchCategoryByIDModelClass>, t: Throwable) {
                Toast.makeText(this@InventoryActivity,t.localizedMessage, Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }

        })
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
        soundId = soundPool!!.load(this@InventoryActivity, R.raw.beep, 0)
    }






//    override fun onPause() {
//        super.onPause()
//        iuhfService.closeDev()
//    }

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

//    override fun onResume() {
//        super.onResume()
//        startSearching()
//    }
//
//    @SuppressLint("ResourceAsColor")
//    fun  startSearching(){
//        try {
//            iuhfService = UHFManager.getUHFService(this)
//            iuhfService.openDev()
//            iuhfService.antennaPower = 30
//
//
//        } catch (e:Exception){
//            Log.d("Exception",e.toString())
//        }
//
//        iuhfService.inventoryStart()
//    }


//    override fun onPause() {
//        super.onPause()
//        stopInventoryService()
//    }

//    override fun onStop() {
//        super.onStop()
//        stopInventoryService()
//    }

//    override fun onRestart() {
//        super.onRestart()
//        stopInventoryService()
//    }

//    override fun onResume() {
//        super.onResume()
//        stopInventoryService()
//    }

    private fun stopInventoryService() {
        if (isInventoryRunning) {
            // Stop inventory service
            iuhfService.closeDev()
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
          // iuhfService.closeDev()
        // Finish the current activity
        finish()
    }


//    @SuppressLint("LongLogTag")
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
//                Log.d("Process:Sorting StartTime",System.currentTimeMillis().toString())
//                if (var1.status == 0) {
//                    val readData = var1.readData
//                    val readHexString = StringUtils.byteToHexString(readData, var1.dataLen)
//                    stringBuilder.append("ReadData:").append(readHexString).append("\n")
//                    Toast.makeText(this,readHexString,Toast.LENGTH_SHORT).show()
//                    if (!temList.contains(readHexString)) {
//                        fetchCategoryByID.updateStatus(readHexString, "Found")
//                        fetchCategoryByID.notifyDataSetChanged()
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
//                finish()
//            }
//        }
//        return super.onKeyUp(keyCode, event)
//    }



    @RequiresApi(Build.VERSION_CODES.M)
    private fun submitInventory(stockData: InventoryDataClass) {
        if (!App.get().isConnected()) {
            InternetConnectionDialog(this, null).show()
            return
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait...")
        progressDialog.setCancelable(false) // Prevent users from dismissing it by tapping outside
        progressDialog.show()

        RetrofitClient.getResponseFromApi().inventory(stockData).enqueue(object : Callback<String> {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(call: Call<String>, response: Response<String>) {
                progressDialog.dismiss() // Dismiss the progressDialog if it's not null

                if (response.isSuccessful) {
                    stopInventoryService()
                    CacheUtils.clearAppCache(this@InventoryActivity)

                    val intent = Intent(this@InventoryActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                    Toast.makeText(this@InventoryActivity, response.body(), Toast.LENGTH_SHORT).show()
                    try {
                        progressDialog.dismiss()



                    } catch (e: Exception) {
                        Log.d("exception", e.toString())
                    }
                } else if (response.code() == 400) {
                    Toast.makeText(this@InventoryActivity, response.message(), Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                } else if (response.code() == 500) {
                    Toast.makeText(this@InventoryActivity, response.message(), Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                } else if (response.code() == 404) {
                    Toast.makeText(this@InventoryActivity, response.message(), Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
                // Handle API errors
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                progressDialog.dismiss() // Dismiss the progressDialog if it's not null
                Toast.makeText(this@InventoryActivity, t.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }


    fun getDataByRank(){
        binding.spRank.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {


            }
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                try {


                    selectedCategory = binding.spRank.selectedItem.toString().substringAfter("category=").substringBefore(",")
                    Log.d("category",selectedCategory)
                    val selectedRank = parent?.getItemAtPosition(position).toString()
                   // categoryID = selectedEmpId
                    progressDialog = ProgressDialog(this@InventoryActivity)
                    progressDialog.setMessage("Please wait...")
                    progressDialog.setCancelable(false) // Prevent users from dismissing it by tapping outside
                    progressDialog.show()
                    //fetchRankCategory(selectedEmpId)
                    if (selectedRank!="Choose Rank.."){
                        fetchCrdByRanlk(selectedRank,categoryID)
                    }

                    rankByCategoryAdapter.notifyDataSetChanged()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

                } catch (e: Exception) {

                }



            }

        }
    }




    @RequiresApi(Build.VERSION_CODES.M)
    private fun fetchRankCategory(categoryid:Int){
        if (!App.get().isConnected()) {
            InternetConnectionDialog(this, null).show()
            return
        }
        RetrofitClient.getResponseFromApi().fetchRankByCategory(categoryid).enqueue(object: Callback<FetchRankByCategoryModelClass> {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(call: Call<FetchRankByCategoryModelClass>, response: Response<FetchRankByCategoryModelClass>) {


                if (response.isSuccessful) {
                    rankbyCategroy.clear()
                    progressDialog.dismiss()

                    try {
                        if (response.body()?.isEmpty() == true) {
                            binding.tvNoDataFound.isVisible = true

                        } else{
                            response.body()?.forEach {
                                rankbyCategroy.add(FetchRankByCategoryModelClass.FetchRankByCategoryModelClassItem(it.name))
                                rankByCategoryAdapter = RankByCategoryAdapter(this@InventoryActivity, rankbyCategroy)
                                binding.spRank.adapter = rankByCategoryAdapter
                                binding.tvNoDataFound.isVisible = false
                            }
                        }



                    } catch (e: Exception) {
                        Log.d("exception", e.toString())

                    }
                } else if (response.code()==400){
                    progressDialog.dismiss()
                    Toast.makeText(this@InventoryActivity,response.message(),Toast.LENGTH_SHORT).show()
                } else if (response.code()==500){
                    progressDialog.dismiss()
                    Toast.makeText(this@InventoryActivity,response.message(),Toast.LENGTH_SHORT).show()
                } else if (response.code()==404){
                    progressDialog.dismiss()
                    Toast.makeText(this@InventoryActivity,response.message(),Toast.LENGTH_SHORT).show()
                }
                // handle  Api error
            }
            override fun onFailure(call: Call<FetchRankByCategoryModelClass>, t: Throwable) {
                Toast.makeText(this@InventoryActivity,t.localizedMessage, Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }

        })
    }

    private fun disableSpinnerItem(spinner: Spinner, position: Int) {
        val view = spinner.getChildAt(position)
        view.isEnabled = false
    }


    @SuppressLint("ResourceAsColor")
    fun  startSearching(){
        isInventoryRunning = true
        initSoundPool()
        try {
            iuhfService = UHFManager.getUHFService(this)
            iuhfService.openDev()
            iuhfService.antennaPower = 30
            iuhfService.inventoryStart()




        } catch (e:Exception){
            Log.d("Exception",e.toString())
        }
        runOnUiThread {
            binding.btnStart.text = "Stop"
        }

    }

    @SuppressLint("ResourceAsColor")
    fun stopSearching(){
        soundPool!!.release()
        binding.btnStart.text = "Start"
        isInventoryRunning = false
        iuhfService.inventoryStop()
        iuhfService.closeDev()
        fetchCategoryByID.moveFoundItemsToTop(binding.loader,binding.recyclerview)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun getRankByCategory(CategoryId:Int){
        if (!App.get().isConnected()) {
                InternetConnectionDialog(this, null).show()
                return
            }
            RetrofitClient.getResponseFromApi().getRankByCategory(CategoryId).enqueue(object: Callback<GetRankModelClass> {
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onResponse(call: Call<GetRankModelClass>, response: Response<GetRankModelClass>) {


                    if (response.isSuccessful) {
                        progressDialog.dismiss()
                        getRankBycategory.clear()
                        getRankBycategory.add(GetRankModelClass.GetRankModelClassItem("Choose Rank.."))

                        try {
                           // fetchCategoryByID = FetchCategoryByID(response.body()!!)
//                            binding.recyclerview.adapter = fetchCategoryByID
//                            binding.Total.text = response.body()!!.size.toString()

                            binding.notFound.text = ""
                            binding.Found.text = ""


                            if (response.body()?.isEmpty() == true) {
                               // binding.tvNoDataFound.isVisible = true

                            } else{
                                response.body()?.forEach {

                                   getRankBycategory.add(GetRankModelClass.GetRankModelClassItem(it.name))
                                    getRankAdapter = GetRankAdapter(this@InventoryActivity,getRankBycategory)
                                    binding.spRank.adapter = getRankAdapter
                                   // binding.tvNoDataFound.isVisible = false
                                }
                            }



                        } catch (e: Exception) {
                            Log.d("exception", e.toString())

                        }
                    } else if (response.code()==400){
                        progressDialog.dismiss()
                        Toast.makeText(this@InventoryActivity,response.message(),Toast.LENGTH_SHORT).show()
                    } else if (response.code()==500){
                        progressDialog.dismiss()
                        Toast.makeText(this@InventoryActivity,response.message(),Toast.LENGTH_SHORT).show()
                    } else if (response.code()==404){
                        progressDialog.dismiss()
                        Toast.makeText(this@InventoryActivity,response.message(),Toast.LENGTH_SHORT).show()
                    }
                    // handle  Api error



                }

                override fun onFailure(call: Call<GetRankModelClass>, t: Throwable) {
                    Toast.makeText(this@InventoryActivity,t.localizedMessage, Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }

            })
        }




    @RequiresApi(Build.VERSION_CODES.M)
    private fun fetchCrdByRanlk(Rank:String,category:Int){
        if (!App.get().isConnected()) {
            InternetConnectionDialog(this, null).show()
            return
        }
        RetrofitClient.getResponseFromApi().getCrdByRank(Rank,category).enqueue(object: Callback<FetchCategoryByIDModelClass> {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(call: Call<FetchCategoryByIDModelClass>, response: Response<FetchCategoryByIDModelClass>) {


                if (response.isSuccessful) {
                    progressDialog.dismiss()
//                    fetchCategoryByID.clearAllData()
//                    rfidNoList.clear()
//                    temList.clear()
//                    //fetchCategoryByID.clearAllData()
//                    binding.notFound.text = "0"
//                    binding.Found.text = "0"
//                    fetchCategoryByID.refreshAdapter()
                    try {
                        if (response.body()?.isNotEmpty() == true) {
                            repsonseBody = response.body().toString()
                            rfidNoList.clear()
                            temList.clear()
                            fetchCategoryByID = FetchCategoryByID(response.body()!!)
                            binding.recyclerview.adapter = fetchCategoryByID
                            fetchCategoryByID.refreshAdapter()
                            binding.Total.text = response.body()!!.size.toString()
                            binding.notFound.text = "0"
                            binding.Found.text = "0"
                        } else{
                            Toast.makeText(this@InventoryActivity,"No Data Found",Toast.LENGTH_SHORT).show()
                              fetchCategoryByID.clearAllData()
                            binding.notFound.text = ""
                            binding.Found.text = ""
                             binding.Total.text = ""
                        }

//                        binding.notFound.text = ""
//                        binding.Found.text = ""
                        // binding.Total.text = ""

                        response.body()!!.forEach {
//                                rfidStockS.add(StockData.StockTake(it.cdr, it.rfidNo,"Not Found"))
                            //  rfidStockS.add(InventoryDataClass.Rfid(it.rfidNo))
                            rfidNoList.add(RfidItem(it.rfidNo.uppercase(),"Not Found"))
                            fetchCategoryByID.notifyDataSetChanged()
                        }




                    } catch (e: Exception) {
                        Log.d("exception", e.toString())

                    }
                } else if (response.code()==400){
                    progressDialog.dismiss()
                    Toast.makeText(this@InventoryActivity,response.message(),Toast.LENGTH_SHORT).show()
                } else if (response.code()==500){
                    progressDialog.dismiss()
                    Toast.makeText(this@InventoryActivity,response.message(),Toast.LENGTH_SHORT).show()
                } else if (response.code()==404){
                    progressDialog.dismiss()
                    Toast.makeText(this@InventoryActivity,response.message(),Toast.LENGTH_SHORT).show()
                }
                // handle  Api error



            }

            override fun onFailure(call: Call<FetchCategoryByIDModelClass>, t: Throwable) {
                Toast.makeText(this@InventoryActivity,t.localizedMessage, Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }

        })
    }





//    fun handleInventoryData(var1: SpdInventoryData) {
//        val timeMillis = System.currentTimeMillis()
//        val l: Long = timeMillis - lastTimeMillis
//        if (l < 100) {
//            return
//        }
//        lastTimeMillis = timeMillis
//
//        coroutineScope.launch {
//            val epc = var1.getEpc().uppercase().trim()
//            if (!temSet.contains(epc)) {
//                temSet.add(epc)
//                val foundItem = rfidNoList.find { it.rfidno == epc }
//                if (foundItem != null) {
//                    foundItem.status = "Found"
//                    fetchCategoryByID.highlightedPositions.add(epc)
//                    fetchCategoryByID.updateStatus(epc, "Found")
//
//                    val foundValue = rfidNoList.count { it.status.equals("Found", ignoreCase = true) }
//                    binding.Found.text = foundValue.toString()
//
//                    val total = rfidNoList.size // Avoiding unnecessary list traversal
//                    val notFound = total - foundValue
//                    binding.notFound.text = notFound.toString()
//
//                    // Batch UI updates if needed
//                    // fetchCategoryByID.refreshAdapter()
//                }
//            }
//        }
//    }



    private suspend  fun handleInventoryData(var1: String) {
        Log.d("var1",var1)
       try {
//            val timeMillis = System.currentTimeMillis()
//            val l: Long = timeMillis - lastTimeMillis
//            if (l < 100) {
//                return
//            }
//            lastTimeMillis = System.currentTimeMillis()
//            try {
//                soundPool?.play(soundId, 1f, 1f, 0, 0, 1f)
//            } catch (e:Exception){
//                e.printStackTrace()
//            }

           // val epc = var1


            if (!temList.contains(var1)) {
                temList.add(var1)

                withContext(Dispatchers.Main) {
                    val foundItem = rfidNoList.find { it.rfidno == var1 }
                    if (foundItem != null) {
                        foundItem.status = "Found"
                        fetchCategoryByID.highlightedPositions.add(var1)
                        //fetchCategoryByID.changeRfidTag(temList)
                        fetchCategoryByID.updateStatus(var1, "Found")


                        val foundValue = rfidNoList.count { it.status.equals("Found", ignoreCase = true) }
                        binding.Found.text = foundValue.toString()

                        val total = rfidNoList.count()
                        try {
                            val notFound = total - foundValue
                            binding.notFound.text = notFound.toString()
                        } catch (e: Exception) {
                            // Handle NumberFormatException
                        }
                        fetchCategoryByID.refreshAdapter()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("handleInventoryData", "Exception: ${e.message}", e)
        }
    }


    private suspend fun reopenDeviceAfterDelay() {
        delay(100)
        withContext(Dispatchers.IO) {
            iuhfService.closeDev()
            iuhfService.openDev()
        }
        iuhfService.inventoryStart()
    }







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





}


