package com.example.filetracking.retrofit



import android.hardware.camera2.CameraExtensionSession.StillCaptureLatency
import com.example.filetracking.identify.IdentifyModelClass
import com.example.filetracking.inventory.CategoryModelClass
import com.example.filetracking.inventory.FetchCategoryByIDModelClass
import com.example.filetracking.inventory.adapter.FetchCategoryByID
import com.example.filetracking.inventory.data.FetchRankByCategoryModelClass
import com.example.filetracking.inventory.data.GetRankModelClass
import com.example.filetracking.inventory.data.InventoryDataClass

import com.example.filetracking.issue_file.RfidRequest
import com.example.filetracking.stock_take.data.StockData
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface RetrofitApi {


    @GET("/fetchcategory")
    fun getCategory():Call<CategoryModelClass>

    @GET("/fetchcrdbycategory")
    fun fetchCategoryByID(@Query("CategoryId") CategoryId:Int):Call<FetchCategoryByIDModelClass>

    @GET("/fetchcrdbysingleRFID")
    fun identifyFile(@Query("RFID") fetchcrdbysingleRFID:String):Call<IdentifyModelClass>

    @GET("/fetchcrdbyfilename")
    fun searchByCRD(@Query("filename") fetchcrdbysingleRFID:String):Call<IdentifyModelClass>

    @POST("/fetchcrdbyRFID")
    fun rfidNoStockTakeView(@Body RfidNoList:ArrayList<String>):Call<FetchCategoryByIDModelClass>

    @POST("/stocktake")
    fun stockTake(@Body data: StockData): Call<String>

    @POST("/internal-bulk-movement")
    fun internalBulkMovement(@Body data: RfidRequest): Call<String>

    @POST("/inventory")
    fun inventory(@Body data: InventoryDataClass): Call<String>


    @GET("/fetchrankbycategory")
    fun fetchRankByCategory(@Query("categoryid") categoryid:Int):Call<FetchRankByCategoryModelClass>



    @GET("/fetchrankbycategory")
    fun getAllRank(@Query("rankId") categoryid:Int):Call<FetchRankByCategoryModelClass>

    @GET("/fetchrankbycategory")
    fun getRankByCategory(@Query("CategoryId") CategoryId:Int):Call<GetRankModelClass>


    @GET("/fetchcrdbyrankandcategory")
    fun getCrdByRank(@Query("Rank")Rank:String,@Query("categoryid")categoryid:Int):Call<FetchCategoryByIDModelClass>







}