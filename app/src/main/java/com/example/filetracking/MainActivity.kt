package com.example.filetracking

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.filetracking.search.SearchActivity
import com.example.filetracking.databinding.ActivityMainBinding
import com.example.filetracking.identify.Identify
import com.example.filetracking.inventory.InventoryActivity
import com.example.filetracking.issue_file.IssueFileActivity
import com.example.filetracking.settings.SettingsActivity
import com.example.filetracking.stock_take.StockTake
import com.example.filetracking.utils.Cons
import com.example.filetracking.utils.SharePref

class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding
    lateinit var sharedPreferences:SharePref
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)




         try{
             sharedPreferences =SharePref()
            val baseURl =  sharedPreferences.getData("baseUrl")
             if (baseURl != null) {
                 if (baseURl.isNotEmpty()){
                   Cons.BASE_URL = baseURl
                 }
             }
         } catch (e:Exception){

         }

        binding.mCardViewIdentify.setOnClickListener {
            val intent = Intent(this,Identify::class.java)
            startActivity(intent)
        }

        binding.mCardIssue.setOnClickListener {
            val intent = Intent(this, IssueFileActivity::class.java)
            startActivity(intent)
        }
        binding.cardViewSearch.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        binding.mCardViewStockTake.setOnClickListener {
            val intent = Intent(this,StockTake::class.java)
            startActivity(intent)
        }
        binding.mCardInventory.setOnClickListener {
            val intent = Intent(this,InventoryActivity::class.java)
            startActivity(intent)
        }

        binding.mCardChangeBaseUrl.setOnClickListener {
            val intent = Intent(this,SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}