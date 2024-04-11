package com.example.filetracking.identify

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.filetracking.MainActivity
import com.example.filetracking.R
import com.example.filetracking.databinding.ActivityIdentifyBinding
import com.example.filetracking.inventory.FetchCategoryByIDModelClass
import com.example.filetracking.inventory.adapter.FetchCategoryByID
import com.example.filetracking.retrofit.RetrofitClient
import com.example.filetracking.utils.App
import com.example.filetracking.utils.ErrorStatus
import com.example.filetracking.utils.InternetConnectionDialog
import com.google.android.material.snackbar.Snackbar
import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager
import com.speedata.libuhf.utils.StringUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class Identify : AppCompatActivity() {
    lateinit var progressDialog: ProgressDialog
    lateinit var binding:ActivityIdentifyBinding
    lateinit var  handler: Handler
    lateinit var iuhfService: IUHFService
    lateinit var  handlerr: Handler
    var isSearchingStart = false
   // var isInventoryRunning = false

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIdentifyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handler = Handler()
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait...")
        progressDialog.setCancelable(false)
//        try {
//            iuhfService = UHFManager.getUHFService(this)
//            iuhfService.openDev()
//
//
////            iuhfService.openDev()
////            iuhfService.antennaPower = 15
////            iuhfService.inventoryStart()
////
////
//        } catch (e:Exception){
//            Log.d("Exception",e.toString())
//        }




        handlerr = Handler()

        binding.imBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        binding.button.setOnClickListener {
           // if (!isInventoryRunning) {
                //startSearching()

                iuhfService.setOnReadListener { var1 ->
                    //iuhfService.inventoryStart()
                    val stringBuilder = StringBuilder()
                    val epcData = var1.epcData
                    val hexString = StringUtils.byteToHexString(epcData, var1.epcLen)
                    if (!TextUtils.isEmpty(hexString)) {
                        stringBuilder.append("EPC：").append(hexString).append("\n")
                    }
                    if (var1.status == 0) {
                        val readData = var1.readData
                        val readHexString = StringUtils.byteToHexString(readData, var1.dataLen)
                        stringBuilder.append("ReadData:").append(readHexString).append("\n")
                        Toast.makeText(this, readHexString, Toast.LENGTH_SHORT).show()
                        progressDialog = ProgressDialog(this)
                        progressDialog.setMessage("Please wait...")
                        progressDialog.setCancelable(false) // Prevent users from dismissing it by tapping outside
                        progressDialog.show()
                        identifyFile(readHexString.toUpperCase())

                    } else {
                        stringBuilder.append(this.resources.getString(R.string.read_fail))
                            .append(":").append(
                            ErrorStatus.getErrorStatus(var1.status)
                        ).append("\n")
                    }
                    handler.sendMessage(handler.obtainMessage(1, stringBuilder))

                }
                val readArea = iuhfService.readArea(1, 2, 6, "00000000")
                if (readArea != 0) {
                    val err: String = this.resources.getString(R.string.read_fail) + ":" + ErrorStatus.getErrorStatus(
                            readArea
                        ) + "\n"
                    handler.sendMessage(handler.obtainMessage(1, err))

                }


//            progressDialog = ProgressDialog(this)
//            progressDialog.setMessage("Please wait...")
//            progressDialog.setCancelable(false) // Prevent users from dismissing it by tapping outside
//            progressDialog.show()
//            identifyFile("")
//            } else{
//                stopSearching()
//            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun identifyFile(RfidNo:String){
        if (!App.get().isConnected()) {
            InternetConnectionDialog(this, null).show()
            return
        }


        progressDialog.show()
        RetrofitClient.getResponseFromApi().identifyFile(RfidNo).enqueue(object:
            Callback<IdentifyModelClass> {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(call: Call<IdentifyModelClass>, response: Response<IdentifyModelClass>) {


                if (response.isSuccessful) {
                    //stopSearching()
                    progressDialog.dismiss()
                        try {

//                            if (response.body()?.cdr.toString()==null){
//
//                                Snackbar.make(binding.root,"Unknown Tag",Toast.LENGTH_SHORT).show()
//                            } else {
                                binding.apply {
                                    tvCreated.text = ""
                                    tvCdr.text = response.body()?.cdr.toString()
                                    tvCategroy.text = response.body()?.category.toString()
                                    //tvCreated.text  = response.body()?.createdAt.toString()
                                    tvRemark.text = response.body()?.remark.toString()
                                    tvRank.text = response.body()?.rank.toString()
                                    tvCategroy.text = response.body()?.category.toString()

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
                                    if (response.body()?.isIssued == 1) {
                                        tvIssue.isVisible = true
                                        tvIssueTo.isVisible = true
                                        llIssue.isVisible = true
                                        llIssueTo.isVisible = true

                                        tvIssue.text = response.body()?.isIssued.toString()
                                        tvIssueTo.text = response.body()?.issuedTo.toString()

                                    } else {
                                        tvIssue.isVisible = false
                                        tvIssueTo.isVisible = false
                                        llIssue.isVisible = false
                                        llIssueTo.isVisible = false
                                    }

                                }
                           // }





                        } catch (e: Exception) {
                            Log.d("exception", e.toString())
                        }

                } else if (response.code()==400){
                    progressDialog.dismiss()
                    Toast.makeText(this@Identify,response.message(), Toast.LENGTH_SHORT).show()
                } else if (response.code()==500){
                    progressDialog.dismiss()
                    Toast.makeText(this@Identify,response.message(), Toast.LENGTH_SHORT).show()
                } else if (response.code()==404){
                    progressDialog.dismiss()
                    Toast.makeText(this@Identify,response.message(), Toast.LENGTH_SHORT).show()
                }
                // handle  Api error



            }

            override fun onFailure(call: Call<IdentifyModelClass>, t: Throwable) {
                Toast.makeText(this@Identify,t.localizedMessage, Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }

        })
    }


    override fun onBackPressed() {

        // Create an intent for the target activity
        val intent = Intent(this, MainActivity::class.java)

        // Add flags to clear the activity stack and start a new task
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

        // Start the target activity
        startActivity(intent)
//        iuhfService.closeDev()
        // Finish the current activity
        finish()


    }

    override fun onStart() {
        super.onStart()
        startSearching()
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BUTTON_R2 || keyCode==131) {

//            if (isInventoryRunning == false) {
//                startSearching()

                // startSearching()
//            try {
//                iuhfService = UHFManager.getUHFService(this)
//                iuhfService.openDev()
//                iuhfService.antennaPower = 10
//            } catch (e:Exception){
//                Log.d("exception",e.toString())
//            }
                // iuhfService.inventoryStart()


                iuhfService.setOnReadListener { var1 ->
                    val stringBuilder = StringBuilder()
                    val epcData = var1.epcData
                    val hexString = StringUtils.byteToHexString(epcData, var1.epcLen)
                    if (!TextUtils.isEmpty(hexString)) {
                        stringBuilder.append("EPC：").append(hexString).append("\n")
                    } else {
                        //Toast.makeText(this, "No Scan", Toast.LENGTH_SHORT).show()
                    }
                    if (var1.status == 0) {
                        val readData = var1.readData
                        val readHexString = StringUtils.byteToHexString(readData, var1.dataLen)
                        stringBuilder.append("ReadData:").append(readHexString).append("\n")
                        //Toast.makeText(this, readHexString, Toast.LENGTH_SHORT).show()


                       identifyFile(readHexString)
                    } else {
                        stringBuilder.append(this.resources.getString(R.string.read_fail))
                            .append(":").append(
                            ErrorStatus.getErrorStatus(var1.status)
                        ).append("\n")
                    }
                   // Toast.makeText(applicationContext,ErrorStatus.getErrorStatus(var1.status),Toast.LENGTH_SHORT).show()
                    handlerr.sendMessage(handlerr.obtainMessage(1, stringBuilder))

                }
                val readArea = iuhfService.readArea(1, 2, 6, "00000000")
                if (readArea != 0) {
                    val err: String =
                        this.resources.getString(R.string.read_fail) + ":" + ErrorStatus.getErrorStatus(
                            readArea
                        ) + "\n"
                    handlerr.sendMessage(handlerr.obtainMessage(1, err))

                }

                return true
            //}
//            else {
//                  stopSearching()
//            }
        }
        else {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                // startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
        return super.onKeyUp(keyCode, event)
    }



//    override fun onPause() {
//        super.onPause()
//        //if (isInventoryRunning==true){
//            stopSearching()
//
//        //}
//
//       // iuhfService.closeDev()
//    }

    override fun onStop() {
        super.onStop()
        stopSearching()
    }





    @SuppressLint("ResourceAsColor")
    fun stopSearching(){
       // isInventoryRunning = false
        //iuhfService.inventoryStop()
        iuhfService.closeDev()
    }


    @SuppressLint("ResourceAsColor")
    fun  startSearching(){
        //isInventoryRunning = true

        try {
            iuhfService = UHFManager.getUHFService(this)
            iuhfService.openDev()



        } catch (e:Exception){
            Log.d("Exception",e.toString())
        }

       // iuhfService.inventoryStart()
    }








}