package com.example.agronom.views

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.agronom.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db : FirebaseFirestore
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
            R.id.culturesFragment,
            R.id.fieldsFragment,
            R.id.sowingsFragment,
            R.id.harvestFragment)
        )
        setSupportActionBar(toolBar)
        NavigationUI.setupWithNavController(toolBar, navController,appBarConfiguration)

        // включаем боковое меню
        val sideBar = findViewById<NavigationView>(R.id.nav_view)
        mAuth = FirebaseAuth.getInstance()
        val mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                db = FirebaseFirestore.getInstance()
                db.collection("Users").document(mAuth.currentUser!!.uid).get()
                    .addOnSuccessListener {document ->
                        if (document.exists()) {
                            sideBar.visibility = View.VISIBLE
                            val headerView = sideBar.inflateHeaderView(R.layout.nav_header_main)
                            val fio = headerView.findViewById<TextView>(R.id.tvFio)
                            val email = headerView.findViewById<TextView>(R.id.tvEmail)
                            val btLogout = headerView.findViewById<ImageButton>(R.id.logout)
                            btLogout.setOnClickListener{
                                openDialog()
                            }
                            fio.text = document.getString("fio")
                            email.text = document.getString("email")
                            sideBar?.setupWithNavController(navController)
                        } else {

                        }
                    }
                    .addOnFailureListener {
                    }
            } else {
                sideBar.visibility = View.GONE
            }
        }
        mAuth.addAuthStateListener(mAuthListener)


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
                R.id.cultureDetailFragment, R.id.sowingsDetailFragment -> {
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

    private fun openDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_custom, null)
        val customDialog = AlertDialog.Builder(this, R.style.my_dialog)
            .setView(dialogView)
            .show()
        customDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_border)
        val btDismiss = dialogView.findViewById<Button>(R.id.btDismissCustomDialog)
        val btPositive = dialogView.findViewById<Button>(R.id.btPositiveCustomDialog)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)
        val tvText = dialogView.findViewById<TextView>(R.id.tvText)
        tvTitle.text = "Выход из аккаунта"
        tvText.text = "Вы действительно хотите покинуть учетную запись?"
        btDismiss.setOnClickListener {
            customDialog.dismiss()
        }
        btPositive.setOnClickListener {
            customDialog.dismiss()
            mAuth.signOut()
            restart()
        }
    }

    private fun restart() {
        val intent = Intent(this, MainActivity::class.java)
        this.startActivity(intent)
        finishAffinity()
    }
}