package com.example.filetracking.stock_take.view

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.filetracking.MainActivity
import com.example.filetracking.R
import com.example.filetracking.databinding.ActivityStockTakeViewBinding
import com.example.filetracking.inventory.FetchCategoryByIDModelClass
import com.example.filetracking.inventory.adapter.FetchCategoryByID
import com.example.filetracking.inventory.adapter.GetCategoryAdapter
import com.example.filetracking.retrofit.RetrofitClient
import com.example.filetracking.stock_take.StockTake
import com.example.filetracking.stock_take.adapetr.StockTakeViewAdapter
import com.example.filetracking.stock_take.data.Temp
import com.example.filetracking.utils.App
import com.example.filetracking.utils.InternetConnectionDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StockTakeView : AppCompatActivity() {
    lateinit var progressDialog: ProgressDialog
    lateinit var binding:ActivityStockTakeViewBinding
    lateinit var fetchCategoryByIDAdapter: StockTakeViewAdapter
     var rfidNoList = arrayListOf<String>()
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStockTakeViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this@StockTakeView)
        progressDialog.setMessage("Please wait...")
        progressDialog.setCancelable(false) // Prevent users from dismissing it by tapping outside
        progressDialog.show()
        val receivedList = intent.getStringArrayListExtra("myListKey")
        if (receivedList != null) {
            stockTakeView(receivedList)
        }

        binding.imBack.setOnClickListener {
            val intent = Intent(this, StockTake::class.java)
          //  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
           // finish()
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun stockTakeView(RfidNo:ArrayList<String>){
        if (!App.get().isConnected()) {
            InternetConnectionDialog(this, null).show()
            return
        }
        RetrofitClient.getResponseFromApi().rfidNoStockTakeView(RfidNo).enqueue(object:
            Callback<FetchCategoryByIDModelClass> {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(call: Call<FetchCategoryByIDModelClass>, response: Response<FetchCategoryByIDModelClass>) {


                if (response.isSuccessful) {

                        try {
                            progressDialog.dismiss()
                            fetchCategoryByIDAdapter = StockTakeViewAdapter(response.body()!!)
                            binding.recyclerview.adapter = fetchCategoryByIDAdapter



                        } catch (e: Exception) {
                            Log.d("exception", e.toString())
                        }

                } else if (response.code()==400){
                    progressDialog.dismiss()
                    Toast.makeText(this@StockTakeView,response.message(), Toast.LENGTH_SHORT).show()
                } else if (response.code()==500){
                    progressDialog.dismiss()
                    Toast.makeText(this@StockTakeView,response.message(), Toast.LENGTH_SHORT).show()
                } else if (response.code()==404){
                    progressDialog.dismiss()
                    Toast.makeText(this@StockTakeView,response.message(), Toast.LENGTH_SHORT).show()
                }
                // handle  Api error



            }

            override fun onFailure(call: Call<FetchCategoryByIDModelClass>, t: Throwable) {
                Toast.makeText(this@StockTakeView,t.localizedMessage, Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }

        })
    }
}