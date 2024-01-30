package com.example.coursecontrol.adminPrivileges

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coursecontrol.GenerateReportManagerActivity
import com.example.coursecontrol.Logout
import com.example.coursecontrol.MainActivity
import com.example.coursecontrol.R
import com.example.coursecontrol.model.User
import com.example.coursecontrol.util.NavigationHandler
import com.example.coursecontrol.util.SessionManager
import com.example.coursecontrol.viewmodel.UserViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class UserDisplayActivity : AppCompatActivity() {
    private val viewModel: UserViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    private lateinit var userDataAdapter: UserAdapter

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_display)
        val searchView: SearchView = findViewById(R.id.searchView)

        sessionManager = SessionManager(this)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.userDataLiveData.observe(this, Observer { userDataList ->
            Log.d("UserDisplayActivity", "User data received: $userDataList")
            userDataAdapter = UserAdapter(userDataList) { selectedUserData ->
                onUserItemSelected(selectedUserData)
            }
            recyclerView.adapter = userDataAdapter
        })

        // Dodajte ovaj blok koda za praćenje promena u SearchView-u
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                userDataAdapter.filter(newText.orEmpty())
                return true
            }
        })

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
                    // Dodajte odgovarajući kod ako nema sesije
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun onUserItemSelected(userData: User) {
        val sessionToken = sessionManager.getSessionToken()
        if (sessionToken != null) {
            val newAdminAlertDialog = NewAdminAlertDialog(
                this,
                userData,
                sessionToken
            )
            newAdminAlertDialog.show()
        }
    }
}
