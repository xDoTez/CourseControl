package com.example.coursecontrol

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.example.coursecontrol.databinding.ActivityBeginningBinding
import com.example.coursecontrol.viewmodel.CourseViewModel


class BeginningActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBeginningBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBeginningBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.logout -> Logout.logoutUser(this, Intent(this, MainActivity::class.java))
                R.id.profile -> {
                    val intent = Intent(this, CourseDisplayActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }
    }
}

