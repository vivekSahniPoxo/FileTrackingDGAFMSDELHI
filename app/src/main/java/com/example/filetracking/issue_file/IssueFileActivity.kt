package com.example.filetracking.issue_file

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.*
import android.util.Log
import android.view.KeyEvent
import android.view.Window
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.filetracking.MainActivity
import com.example.filetracking.R
import com.example.filetracking.databinding.ActivityIssueFileBinding
import com.example.filetracking.inventory.FetchCategoryByIDModelClass
import com.example.filetracking.retrofit.RetrofitClient
import com.example.filetracking.stock_take.adapetr.StockTakeAdapter
import com.example.filetracking.stock_take.data.Rfid
import com.example.filetracking.stock_take.data.RfidDetails
import com.example.filetracking.utils.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager
import com.speedata.libuhf.bean.SpdInventoryData
import com.speedata.libuhf.interfaces.OnSpdInventoryListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class IssueFileActivity : AppCompatActivity(),ItemSwipeListener{

    private var handler = Handler()
    val uniqueRfidSet = HashSet<String>()
    lateinit var binding:ActivityIssueFileBinding
    private var soundPool: SoundPool? = null
    private var soundId = 0
    var isInventoryRunning = false
    lateinit var iuhfService: IUHFService
    lateinit var stockTakeAdapter: StockTakeAdapter
    lateinit var rfiList:ArrayList<Rfid>
    var lastTimeMillis: Long = 0
    var rfidNo = ""
    lateinit var dialog:Dialog
    val temp = arrayListOf<String>()
    lateinit var rfid:ArrayList<String>
    lateinit var tempListDetails:ArrayList<RfidDetails>
    lateinit var listRfid:ArrayList<Rfid>
    var rdClick = ""
    lateinit var tempDetails:ArrayList<String>
    private var isDialogShowing = false
    lateinit var fetchCategoryByIDAdapter: FileStatusAdapter
    lateinit var getDatabyID:ArrayList<FetchCategoryByIDModelClass.FetchCategoryByIDItem>
    lateinit var rfidList:ArrayList<String>
    lateinit var tempRfidList:ArrayList<RfidItemList>
    var count = 0
    private var scant: Long = 0
    private val uhfCardBeanList: ArrayList<UhfCardBean> = ArrayList<UhfCardBean>()
    //lateinit var intentBulkMovement: ArrayList<InternalBulkMovement>
    private var EPCNum = 0
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIssueFileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        iuhfService = UHFManager.getUHFService(this)
        rfiList =  arrayListOf()
        rfid = arrayListOf()
        tempRfidList =  arrayListOf()
        listRfid =  arrayListOf()
        tempListDetails = arrayListOf()
        tempDetails = arrayListOf()
        getDatabyID = arrayListOf()
        rfidList = arrayListOf()
        ///intentBulkMovement = arrayListOf()
        fetchCategoryByIDAdapter = FileStatusAdapter(this,getDatabyID)
        stockTakeAdapter = StockTakeAdapter(rfid)
        binding.rfidList.adapter = StockTakeAdapter(rfid)
        updateAdapterWithNewData(getDatabyID)



//        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
//            override fun onLongPress(e: MotionEvent) {
//                val child = binding.rfidList.findChildViewUnder(e.x, e.y)
//                if (child != null) {
//                    val position = binding.rfidList.getChildAdapterPosition(child)
//                    showYesNoDialog(position)
//                    // 'position' now contains the position of the long-pressed item
//                    // Use it as needed
//                }
//            }
//        })
//
//        binding.rfidList.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
//            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
//                return gestureDetector.onTouchEvent(e)
//            }
//
//            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
//            }
//
//            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
//            }
//        })




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
                               // rfiList.add(Rfid(var1.epc))

                                rfid.add(var1.epc)
                                EPCNum = uhfCardBeanList.size
                                binding.count.text = EPCNum.toString()
                                runOnUiThread {
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
                                temp.add(var1.epc)
                                binding.countTemp.text = temp.size.toString()
                               // temp.clear()
                            })

                        }
                    }
                }
            }
        }




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

        binding.radioButtonOption1.setOnClickListener {
            rdClick =  "Issue"
        }

        binding.radioButtonOption2.setOnClickListener {
            rdClick = "Return"
        }


        val itemTouchHelperCallback = SwipeToDeleteCallback(fetchCategoryByIDAdapter)
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.rfidList)

//        binding.count.text = SwipeToDeleteCallback(fetchCategoryByIDAdapter).listSize

        binding.btnRead.setOnClickListener {
            if (!isInventoryRunning) {
                startSearching()
                iuhfService.setOnInventoryListener(object : OnSpdInventoryListener {
                    @SuppressLint("NotifyDataSetChanged", "SuspiciousIndentation")
                    override fun getInventoryData(var1: SpdInventoryData) {

                        handler.sendMessage(handler.obtainMessage(1, var1))
                        val timeMillis = System.currentTimeMillis()
                        val l = timeMillis - lastTimeMillis
                        if (l < 100) {
                            return
                        }
                        lastTimeMillis = System.currentTimeMillis()
                        soundPool!!.play(soundId, 1f, 1f, 0, 0, 1f)


//                        try {
////                            val timeMillis = System.currentTimeMillis()
////                            val l: Long = timeMillis - lastTimeMillis
////                            if (l < 100) {
////                                return
////                            }
//
////                            runOnUiThread(kotlinx.coroutines.Runnable {
////                                temp.add(var1.getEpc())
////                                binding.countTemp.text = temp.size.toString()
////                                temp.clear()
////                            })
//
//                            lastTimeMillis = System.currentTimeMillis()
//                           // runOnUiThread {
//                                try {
//                                    soundPool?.play(soundId, 1f, 1f, 0, 0, 1f)
//                                } catch (e: Exception) {
//                                    // Log.d("Sound Playback Error", e.toString())
//                                //}
//                            }
//                            val epc = var1.getEpc().uppercase()
//
//                            if (!rfid.contains(epc)){
//                                rfid.add(epc)
//                                rfiList.add(Rfid(epc))
//                            //}
////                            if (!rfiList.contains(Rfid(var1.getEpc()))) {
//
//                                //  if (!rfid.contains(epc)) {
//
//                                listRfid.add(Rfid(epc))
//                                // }
//                                runOnUiThread {
//                                    val listSize = rfiList.distinct().size
//                                    binding.count.text = "count $listSize"
//                                    //rfidNo.add(RfidItem(epc, "Found"))
//
//                                    val stockTakeAdapter = StockTakeAdapter(rfiList)
//                                    binding.rfidList.adapter = stockTakeAdapter
//                                    stockTakeAdapter.notifyDataSetChanged()
//                                }
//                            }
//
//
//                        } catch (e: Exception) {
//
//                        }
                    }

                    override fun onInventoryStatus(p0: Int) {
                        threadpooling(p0)
                    }
                })

//                binding.btnRead.text = "Stop"
//                iuhfService.inventoryStart()
//                isInventoryRunning = true
                // stopSearching()

            } else if (isInventoryRunning == true) {
//                binding.btnRead.text = "Start"
//                isInventoryRunning = false
//                iuhfService.inventoryStop()
                stopSearching()


            }
        }

        binding.btnSubmit.setOnClickListener {
            dialog()
        }




    }


    @SuppressLint("NotifyDataSetChanged")
    private fun updateAdapterWithNewData(newItemList: List<FetchCategoryByIDModelClass.FetchCategoryByIDItem>) {
        fetchCategoryByIDAdapter.itemList.clear()
        fetchCategoryByIDAdapter.itemList.addAll(newItemList)
        fetchCategoryByIDAdapter.notifyDataSetChanged()
    }


    @SuppressLint("SetTextI18n")
    private fun showYesNoDialog(position: Int) {
        if (!isDialogShowing) { // Check if the dialog is not already showing
            isDialogShowing = true // Set the flag to true to indicate that the dialog is being displayed

            dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setContentView(R.layout.remove_item_dialog)
            dialog.setCancelable(true)
            dialog.show()

            val cancel: MaterialButton = dialog.findViewById(R.id.btnNo)
            cancel.setOnClickListener {
                dialog.dismiss()
                isDialogShowing = false // Set the flag to false to indicate that the dialog is dismissed
            }

            val yes: MaterialButton = dialog.findViewById(R.id.btnYEs)
            yes.setOnClickListener {
                Handler(Looper.getMainLooper()).postDelayed({

                    val indexToRemove = position
                   // val indexForBatchList = position

                    if (indexToRemove >= 0 && indexToRemove < getDatabyID.size) {
                        getDatabyID.removeAt(indexToRemove)
                        binding.count.text = getDatabyID.size.toString()
                    } else {
                        binding.count.text = getDatabyID.size.toString()
                    }

                    val popItemAnimator = PopItemAnimator()
                    binding.rfidList.itemAnimator = popItemAnimator
                    //val positionToDelete = position // Replace with the position you want to delete
                    getDatabyID.removeAt(indexToRemove) // Remove the item from your dataset
                    fetchCategoryByIDAdapter.notifyItemRemoved(indexToRemove)
                    isDialogShowing = false // Set the flag to false to indicate that the dialog is dismissed
                }, 200) // Adjust the delay time as needed
                dialog.dismiss()
            }
        }
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



    @RequiresApi(Build.VERSION_CODES.M)
    private fun stockTakeView(RfidNo:ArrayList<String>){
        if (!App.get().isConnected()) {
            InternetConnectionDialog(this, null).show()
            return
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait...")
        progressDialog.setCancelable(false) // Prevent users from dismissing it by tapping outside
        progressDialog.show()
        RetrofitClient.getResponseFromApi().rfidNoStockTakeView(RfidNo).enqueue(object:
            Callback<FetchCategoryByIDModelClass> {
            @SuppressLint("SetTextI18n")
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(call: Call<FetchCategoryByIDModelClass>, response: Response<FetchCategoryByIDModelClass>) {


                if (response.isSuccessful) {

                    try {
                        progressDialog.dismiss()
                        rfid.clear()
                        rfiList.clear()
                        uhfCardBeanList.clear()
                        isInventoryRunning = false
                        fetchCategoryByIDAdapter.notifyDataSetChanged()
//                        response.body()?.forEach {
//                            getDatabyID.add(FetchCategoryByIDModelClass.FetchCategoryByIDItem(it.category,it.cdr,it.createdAt,it.id,it.isIssued,it.isReturned,it.issuedDate,it.issuedTo,it.rank,it.remark,it.returnDate,it.rfidNo))
//                        }
                        fetchCategoryByIDAdapter = FileStatusAdapter(this@IssueFileActivity,response.body()!!)
                        binding.count.text = "count ${response.body()!!.size}"
                        binding.rfidList.adapter = fetchCategoryByIDAdapter
                        fetchCategoryByIDAdapter.notifyDataSetChanged()

                        response.body()?.forEach {
                            rfidList.add(it.rfidNo)
                            tempRfidList.add(RfidItemList(it.rfidNo,""))
                        }

                    } catch (e: Exception) {
                        Log.d("exception", e.toString())
                    }

                } else if (response.code()==400){
                    progressDialog.dismiss()
                    Toast.makeText(this@IssueFileActivity,response.message(), Toast.LENGTH_SHORT).show()
                } else if (response.code()==500){
                    progressDialog.dismiss()
                    Toast.makeText(this@IssueFileActivity,response.message(), Toast.LENGTH_SHORT).show()
                } else if (response.code()==404){
                    progressDialog.dismiss()
                    Toast.makeText(this@IssueFileActivity,response.message(), Toast.LENGTH_SHORT).show()
                }
                // handle  Api error



            }

            override fun onFailure(call: Call<FetchCategoryByIDModelClass>, t: Throwable) {
                Toast.makeText(this@IssueFileActivity,t.localizedMessage, Toast.LENGTH_SHORT).show()
                if (!isFinishing && progressDialog.window != null && progressDialog.window!!.decorView != null && progressDialog.window!!.decorView.isAttachedToWindow) {
                    runOnUiThread {
                        progressDialog.dismiss()
                    }
                }
            }

        })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    private fun dialog() {
        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.layout_description)
        dialog.setCancelable(true)
        dialog.show()


        val description: EditText = dialog.findViewById(R.id.et_description)
        val progressBar: ProgressBar = dialog.findViewById(R.id.progressBar_remark)
        val send: MaterialButton = dialog.findViewById(R.id.btn_send)
        val etNAme:EditText = dialog.findViewById(R.id.etName)

        if (rdClick=="Issue"){
            etNAme.hint = "Issue By"
        } else if (rdClick=="Return"){
            etNAme.hint = "Received By"
        }



        progressBar.isVisible = false
        send.isVisible = true



        send.setOnClickListener {
            if (description.text.isNotEmpty()) {
               // Toast.makeText(this,"Submitted",Toast.LENGTH_SHORT).show()
                //intentBulkMovement.add(InternalBulkMovement(rdClick,description.text.toString(),tempRfidList))
                //internalBulMovement(intentBulkMovement)
                dialog.dismiss()

               Log.d(" tempRfidList", tempRfidList.toString())
                val bulkMovement = RfidRequest(tempRfidList,rdClick,description.text.toString(),etNAme.text.toString())
                internalBulMovement(bulkMovement)

            } else{
                Snackbar.make(binding.root,"Remark description is required", Snackbar.LENGTH_SHORT).show()
            }
        }

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
            rfid.clear()



        } catch (e:Exception){
           e.printStackTrace()
        }





//        runOnUiThread {
//            val stockTakeAdapter =  StockTakeAdapter(rfiList)
//            binding.rfidList.adapter = stockTakeAdapter
//        }
        // binding.btnStart.text = "Stop"

    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ResourceAsColor")
    fun stopSearching(){
        try {
            soundPool!!.release()
        } catch (e:Exception){
            e.printStackTrace()
        }

        isInventoryRunning = false
        iuhfService.inventoryStop()
        iuhfService.closeDev()
        if (!isInventoryRunning){
            stockTakeView(rfid)
        }
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
        soundId = soundPool!!.load(this@IssueFileActivity, R.raw.beep, 0)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BUTTON_R2 || keyCode == KeyEvent.KEYCODE_F1) {
            if (rdClick.isEmpty()){
                Snackbar.make(binding.root,"Please choose file status",Snackbar.LENGTH_SHORT).show()
            } else
            if (!isInventoryRunning) {
                startSearching()
                iuhfService.setOnInventoryListener(object : OnSpdInventoryListener {
                @SuppressLint("SetTextI18n")
                override fun getInventoryData(var1: SpdInventoryData) {
                    handler.sendMessage(handler.obtainMessage(1, var1))
                    val timeMillis = System.currentTimeMillis()
                    val l = timeMillis - lastTimeMillis
                    if (l < 100) {
                        return
                    }
                    lastTimeMillis = System.currentTimeMillis()
                    soundPool!!.play(soundId, 1f, 1f, 0, 0, 1f)


                    // Launch a coroutine for asynchronous processing
//                        CoroutineScope(Dispatchers.Default).launch {
//                            try {
//                                val timeMillis = System.currentTimeMillis()
//                                val l: Long = timeMillis - lastTimeMillis
//                                if (l < 100) {
//                                    return@launch
//                                }
//                                lastTimeMillis = System.currentTimeMillis()
//
//                                // Update UI elements
//                                withContext(Dispatchers.Main) {
//                                    binding.countTemp.text = l.toString()
//                                }
//
//                                try {
//                                    // Play sound asynchronously
//                                    soundPool?.play(soundId, 1f, 1f, 0, 0, 1f)
//                                } catch (e: Exception) {
//                                    // Handle sound playback error
//                                    withContext(Dispatchers.Main) {
//                                        Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_SHORT).show()
//                                    }
//                                }
//
//                                // Process RFID tag
//                                val epc = var1.getEpc().uppercase()
//                                if (!rfid.contains(epc)) {
//                                    rfid.add(epc)
//                                    rfiList.add(Rfid(epc))
//                                    withContext(Dispatchers.Main) {
//                                        // Update UI with batched RFID tag updates
//                                        binding.count.text = "count ${rfid.size}"
//                                        binding.rfidList.adapter?.notifyDataSetChanged()
//                                    }
//                                }
//                            } catch (e: Exception) {
//                                // Handle other exceptions
//                                withContext(Dispatchers.Main) {
//                                    Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_SHORT).show()
//                                }
//                            }
//                        }


                }

                    override fun onInventoryStatus(p0: Int) {

                    }
                })




                // Start inventory service
//                val executor = Executors.newFixedThreadPool(5)
//                iuhfService.setOnInventoryListener(object : OnSpdInventoryListener {
//                    @SuppressLint("NotifyDataSetChanged", "SuspiciousIndentation")
//                    override fun getInventoryData(var1: SpdInventoryData) {
//
//
//
//
//
//                        //executor.execute {
//                        try {
//                            val timeMillis = System.currentTimeMillis()
//                            val l: Long = timeMillis - lastTimeMillis
//                            if (l < 100) {
//                                return
//                            }
//                            lastTimeMillis = System.currentTimeMillis()
//
//                            runOnUiThread(kotlinx.coroutines.Runnable {
//                                //temp.add(var1.getEpc())
//                                binding.countTemp.text = l.toString()
//                                // temp.clear()
//                            })
//
//                            try {
//                                soundPool?.play(soundId, 1f, 1f, 0, 0, 1f)
//                            } catch (e: Exception) {
//                                Toast.makeText(applicationContext,e.toString(),Toast.LENGTH_SHORT).show()
//                            }
//                            val epc = var1.getEpc().uppercase()
//
//
//
//                            if (!rfid.contains(epc)) {
//                                rfid.add(epc)
//                                rfiList.add(Rfid(epc))
//                               // listRfid.add(Rfid(epc))
//
//                                runOnUiThread {
//                                   // val stockTakeAdapter = StockTakeAdapter(rfiList)
//                                    binding.rfidList.adapter = stockTakeAdapter
//                                    //stockTakeAdapter.notifyDataSetChanged()
//                                    //val listSize = rfiList.size
//                                    binding.count.text = "count ${rfid.size}"
//
//                                }
//                            }
//
//                            Log.d("rfffff",rfid.size.toString())
//
//
//
//                        } catch (e: Exception) {
//                          Toast.makeText(applicationContext,e.toString(),Toast.LENGTH_SHORT).show()
//                        }
//                        //}
//
//
//
//                    }
//
//                    override fun onInventoryStatus(p0: Int) {
//
//                    }
//                })
            } else {
                stopSearching()
            }
        }

        return super.onKeyDown(keyCode, event)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStop() {
        super.onStop()
        try {
            soundPool?.release()
        } catch (e:Exception){

        }
        stopSearching()
    }








    @SuppressLint("NotifyDataSetChanged")
    override fun onItemSwiped(position: Int, updatedList: List<FetchCategoryByIDModelClass.FetchCategoryByIDItem>) {
        try {
            // Check if position is within the valid range
            if (position >= 0 && position < rfidList.size) {
                // Remove the item from rfidList
                rfidList.removeAt(position)
                fetchCategoryByIDAdapter.removeItemFromAdapter(position)
                fetchCategoryByIDAdapter.notifyDataSetChanged()

                // Update tempRfidList based on rfidList
                tempRfidList.clear()
                rfidList.forEach { item ->
                    tempRfidList.add(RfidItemList(item, ""))
                }

                binding.count.text = rfidList.size.toString()
                //Log.d("upd", rfidList.toString())
            } else {
                //Log.e("onItemSwiped", "Invalid position: $position")
            }
        } catch (e: ArrayIndexOutOfBoundsException) {
            Log.e("exceptionAfterSwiped", e.toString())
            binding.count.text = rfidList.size.toString()
        } catch (e: Exception) {
           // Log.e("exceptionAfterSwiped", e.toString())
            binding.count.text = rfidList.size.toString()
        }
    }



    @RequiresApi(Build.VERSION_CODES.M)
    private fun internalBulMovement(internalBulkMovement: RfidRequest) {
        if (!App.get().isConnected()) {
            InternetConnectionDialog(this, null).show()
            return
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait...")
        progressDialog.setCancelable(false) // Prevent users from dismissing it by tapping outside
        progressDialog.show()

        RetrofitClient.getResponseFromApi().internalBulkMovement(internalBulkMovement).enqueue(object : Callback<String> {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(call: Call<String>, response: Response<String>) {
                progressDialog.dismiss() // Dismiss the progressDialog if it's not null

                if (response.isSuccessful) {
                    CacheUtils.clearAppCache(this@IssueFileActivity)
                    val intent = Intent(this@IssueFileActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                    Toast.makeText(this@IssueFileActivity, response.body(), Toast.LENGTH_SHORT).show()
                    try {
                        progressDialog.dismiss()
                        rfiList.clear()
                        rfid.clear()
                        tempRfidList.clear()
                        stockTakeAdapter.notifyDataSetChanged()


                    } catch (e: Exception) {
                        //  Log.d("exception", e.toString())
                    }
                } else if (response.code() == 400) {
                    Toast.makeText(this@IssueFileActivity, response.message(), Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                } else if (response.code() == 500) {
                    Toast.makeText(this@IssueFileActivity, response.message(), Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                } else if (response.code() == 404) {
                    Toast.makeText(this@IssueFileActivity, response.message(), Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
                // Handle API errors
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                progressDialog.dismiss() // Dismiss the progressDialog if it's not null
                Toast.makeText(this@IssueFileActivity, t.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }



}