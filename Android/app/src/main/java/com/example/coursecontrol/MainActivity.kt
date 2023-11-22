package com.example.coursecontrol

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.coursecontrol.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.bottomNavigation.setOnItemSelectedListener {

            when(it.itemId){
                R.id.logout -> logoutUser()

            }
            true
        }
    }

    private fun logoutUser(){
        TODO("forget user")
        /*val loginActivityIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginActivityIntent);*/
    }


}