package com.example.coursecontrol

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.coursecontrol.databinding.ActivityGenerateReportBinding
import com.example.coursecontrol.modules.GenerateReportPdf
import com.example.coursecontrol.util.SessionManager
import com.example.coursecontrol.viewmodel.CourseViewModel
import kotlinx.coroutines.launch


class GenerateReportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGenerateReportBinding
    private val viewModel: CourseViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenerateReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        lifecycleScope.launch {
            try {
                val sessionToken = sessionManager.getSessionToken()
                if (sessionToken != null) {
                    viewModel.makeApiCall(sessionToken)
                } else {
                    // Handle the case when the session token is null
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        setButtonListener()
    }

    private fun setButtonListener(){
        binding.ButtonTest.setOnClickListener {
            if (checkPermission()) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                requestPermission()
            }

            val generateReportPdf = GenerateReportPdf.getInstance()
            viewModel.courseDataLiveData.observe(this, Observer { courseDataList ->
                generateReportPdf.setData(courseDataList)
            })
            generateReportPdf.generatePdf()
        }
    }

    private fun checkPermission(): Boolean{
        var permission1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE)
        var permission2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE)
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        // requesting permissions if not provided.
        ActivityCompat.requestPermissions(
            this,
            arrayOf<String>(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE),
            200
        )
    }
}

