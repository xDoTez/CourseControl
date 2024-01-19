package com.example.coursecontrol.addNewCourse

import AdminChecker
import UserDataAdapter
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coursecontrol.CourseDetailsActivity
import com.example.coursecontrol.GenerateReportManagerActivity
import com.example.coursecontrol.Logout
import com.example.coursecontrol.MainActivity
import com.example.coursecontrol.R
import com.example.coursecontrol.model.CourseData
import com.example.coursecontrol.model.Program
import com.example.coursecontrol.util.SessionManager
import com.example.coursecontrol.viewmodel.CourseAddNewProgram
import com.example.coursecontrol.viewmodel.CourseViewModelHistory
import com.example.coursecontrol.viewmodel.ProgramViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProgramDisplayActivity : AppCompatActivity() {
    private val viewModel: ProgramViewModel by viewModels()
    private val viewProgramModel: CourseAddNewProgram by viewModels()
    val adminChecker = AdminChecker()
    private lateinit var sessionManager: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_program_display)

        sessionManager = SessionManager(this)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val addNewProgramButton: Button = findViewById(R.id.btnAddNewProgram)

        viewModel.programDataLiveData.observe(this, Observer { programDataList ->
            val programDataAdapter = ProgramAdapter(programDataList) { selectedProgramData ->
                onProgramItemSelected(selectedProgramData)
            }
            recyclerView.adapter = programDataAdapter
        })

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.logout -> {
                    Logout.logoutUser(this, Intent(this, MainActivity::class.java))
                    true
                }
                R.id.report -> {
                    val intent = Intent(this, GenerateReportManagerActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.profile -> {
                    true
                }
                else -> false
            }
        }

        lifecycleScope.launch {
            try {
                val sessionToken = sessionManager.getSessionToken()
                adminChecker.checkAdmin(sessionToken)
                val isAdmin = adminChecker.isAdmin()
                if (!isAdmin) {
                    addNewProgramButton.visibility = View.GONE
                }
                addNewProgramButton.setOnClickListener {
                    showNewProgramDialog()
                }
                if (sessionToken != null) {
                    viewModel.makeApiCall()
                } else {
                    // Handle the case when sessionToken is null
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun onProgramItemSelected(program: Program) {
        val intent = Intent(this, NewCoursesActivity::class.java)
        intent.putExtra("program", program)
        startActivity(intent)
    }

    private fun showNewProgramDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter New Program Name")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val programName = input.text.toString().trim()
            if (programName.isNotEmpty()) {
                onAddNewProgramButtonClick(programName)
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun onAddNewProgramButtonClick(programName: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            viewProgramModel.addNewProgram(
                context = this@ProgramDisplayActivity,
                sessionToken = sessionManager.getSessionToken() ?: return@launch,
                program = programName
            )
            val intent = Intent(this@ProgramDisplayActivity, ProgramDisplayActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }


}