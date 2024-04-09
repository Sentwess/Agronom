package com.example.agronom.views

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.agronom.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // получаем navController
        val host: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.navFragment) as NavHostFragment? ?: return
        val navController = host.navController

        val toolBar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.mainFragment,
            R.id.culturesFragment,
            R.id.fieldsFragment,
            R.id.sowingsFragment,
            R.id.harvestFragment)
        )
        setSupportActionBar(toolBar)
        NavigationUI.setupWithNavController(toolBar, navController,appBarConfiguration)

        // включаем боковое меню
        val sideBar = findViewById<NavigationView>(R.id.nav_view)
        sideBar?.setupWithNavController(navController)

        val bottomBar = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        bottomBar?.setupWithNavController(navController)

        // скрываем навигацию
        val bottomNavView:BottomNavigationView = findViewById(R.id.bottom_nav_view)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.splashFragment, R.id.signInFragment, R.id.signUpFragment -> {
                    bottomNavView.visibility = View.GONE
                    toolBar.visibility = View.GONE
                }
                R.id.cultureDetailFragment, R.id.fieldsDetailFragment -> {
                    bottomNavView.visibility = View.GONE
                    toolBar.visibility = View.VISIBLE
                }
                else -> {
                    toolBar.visibility = View.VISIBLE
                    bottomNavView.visibility = View.VISIBLE
                }
            }
        }
    }
}