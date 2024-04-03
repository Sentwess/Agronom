package com.example.agronom.views

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
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

        // включаем боковое меню
        val sideBar = findViewById<NavigationView>(R.id.nav_view)
        sideBar?.setupWithNavController(navController)

        val bottomBar = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        bottomBar?.setupWithNavController(navController)

        // скрываем нижнее навменю
        val bottomNavView:BottomNavigationView = findViewById(R.id.bottom_nav_view)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.splashFragment -> bottomNavView.visibility = View.GONE
                R.id.signInFragment -> bottomNavView.visibility = View.GONE
                R.id.signUpFragment -> bottomNavView.visibility = View.GONE
                R.id.cultureDetailFragment -> bottomNavView.visibility = View.GONE
                R.id.fieldsDetailFragment -> bottomNavView.visibility = View.GONE
                else -> bottomNavView.visibility = View.VISIBLE
            }
        }
    }
}