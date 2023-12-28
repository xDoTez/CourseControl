package com.example.coursecontrol

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.coursecontrol.databinding.ActivityGenerateReportManagerBinding
import com.example.coursecontrol.modules.GenerateReportPdf
import com.example.coursecontrol.util.SessionManager
import com.example.coursecontrol.viewmodel.CourseViewModel
import kotlinx.coroutines.launch


class GenerateReportManagerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGenerateReportManagerBinding
    private val viewModel: CourseViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    private var reportGenerators: ArrayList<GenerateReport> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenerateReportManagerBinding.inflate(layoutInflater)
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

        initializeReportGenerators()
    }

    fun initializeReportGenerators(){
        addReportGenerator(GenerateReportPdf())
    }

    private fun addReportGenerator(reportGenerator: GenerateReport) {
        reportGenerators.add(reportGenerator)
        addGeneratorToMenu(reportGenerator)
    }

    private fun addGeneratorToMenu(reportGenerator: GenerateReport) {
        var layout = binding.ModularLayout
        var button = Button(this)
        button.setText(reportGenerator.getName(this))
        button.setCompoundDrawablesWithIntrinsicBounds(reportGenerator.getIcon(this), null, null, null)

        var params = LinearLayout.LayoutParams(resources.getDimension(R.dimen.button_width).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(0,resources.getDimension(R.dimen.button_margin_top).toInt(),0,0)
        button.setLayoutParams(params)

        button.setBackgroundColor(Color.parseColor("#BD1111"))
        button.setTextColor(Color.WHITE)

        button.setPadding(resources.getDimension(R.dimen.button_padding_start_and_end).toInt(),
            resources.getDimension(R.dimen.button_padding_top_and_bottom).toInt(),
            resources.getDimension(R.dimen.button_padding_start_and_end).toInt(),
            resources.getDimension(R.dimen.button_padding_top_and_bottom).toInt())

        button.setOnClickListener {
            if (checkPermission()) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                requestPermission()
            }

            viewModel.courseDataLiveData.observe(this, Observer { courseDataList ->
                reportGenerator.setData(courseDataList)
            })
            reportGenerator.generateReport()
            Toast.makeText(this, "Report generated in Downloads", Toast.LENGTH_SHORT).show()
            finish()
        }

        layout.addView(button)
    }

    /*private fun setButtonListener(){
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
            generateReportPdf.generateReport()
            Toast.makeText(this, "PDF report generated!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }*/

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

