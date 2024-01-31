package com.example.coursecontrol

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import com.example.coursecontrol.util.NavigationHandler
import com.example.coursecontrol.util.SessionManager
import com.example.coursecontrol.viewmodel.CourseViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dalvik.system.BaseDexClassLoader
import dalvik.system.DexFile
import hr.foi.air.core.GenerateReport
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.reflect.Field


class GenerateReportManagerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGenerateReportManagerBinding
    private val viewModel: CourseViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    private var reportGenerators: ArrayList<GenerateReport> = ArrayList()
    private lateinit var instance: GenerateReport

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenerateReportManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val navigationHandler = NavigationHandler(this)

        bottomNavigationView.setOnItemSelectedListener { item ->
            navigationHandler.handleItemSelected(item)
        }

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

    private fun getClassesOfPackage(packageName: String): MutableList<String>? {
        val classes = ArrayList<String>()
        try {
            val dexFiles = getDexFiles()
            for(dexFile in dexFiles){
                val iter = dexFile.entries()
                while (iter.hasMoreElements()) {
                    val className = iter.nextElement()
                    if (className.contains(packageName)) {
                        classes.add(
                            className.substring(
                                className.lastIndexOf(".") + 1,
                                className.length
                            )
                        )
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return classes
    }

    private fun getDexFiles(): List<DexFile> {
        val classLoader = classLoader as BaseDexClassLoader

        val pathListField = field("dalvik.system.BaseDexClassLoader", "pathList")
        val pathList = pathListField.get(classLoader) // Type is DexPathList

        val dexElementsField = field("dalvik.system.DexPathList", "dexElements")
        @Suppress("UNCHECKED_CAST")
        val dexElements = dexElementsField.get(pathList) as Array<Any> // Type is Array<DexPathList.Element>

        val dexFileField = field("dalvik.system.DexPathList\$Element", "dexFile")
        return dexElements.map {
            dexFileField.get(it) as DexFile
        }
    }

    private fun field(className: String, fieldName: String): Field {
        val clazz = Class.forName(className)
        val field = clazz.getDeclaredField(fieldName)
        field.isAccessible = true
        return field
    }

    private fun initializeReportGenerators(){
        try {
            var modulePath = "com.example.coursecontrol.modules."
            val name = "modules"

            var classNames = getClassesOfPackage(name)!!.toSet()
            for(className in classNames!!){
                if(className.contains("LiveLiterals")){
                    continue
                } else {
                    Log.d("naziv modula", className)
                    val fullName = modulePath + className
                    val module = Class.forName(fullName)
                    instance = module.newInstance() as GenerateReport
                    addReportGenerator(instance)
                }
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
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

