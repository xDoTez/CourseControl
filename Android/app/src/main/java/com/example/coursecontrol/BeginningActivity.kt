package com.example.coursecontrol

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.coursecontrol.databinding.ActivityBeginningBinding

class BeginningActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBeginningBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBeginningBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.bottomNavigation.setOnItemSelectedListener {

            when(it.itemId){
                R.id.logout -> Logout.logoutUser(this, Intent(this, MainActivity::class.java))

            }
            true
        }
    }
}