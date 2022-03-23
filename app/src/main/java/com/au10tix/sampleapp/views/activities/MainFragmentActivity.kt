package com.au10tix.sampleapp.views.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.au10tix.sampleapp.R
import com.au10tix.sampleapp.models.DataViewModel

class MainFragmentActivity : AppCompatActivity() {
    private var viewModel: DataViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_fragment)
        if (savedInstanceState == null) {
            val host = NavHostFragment.create(R.navigation.navigation_graph)
            supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment, host)
                .setPrimaryNavigationFragment(host).commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel = null
    }
}