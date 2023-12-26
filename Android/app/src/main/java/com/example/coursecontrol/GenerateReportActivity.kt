package com.example.coursecontrol

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.coursecontrol.databinding.ActivityGenerateReportBinding


class GenerateReportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGenerateReportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenerateReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}

