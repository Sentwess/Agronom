package com.example.agronom.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.example.agronom.R

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentLayout = inflater.inflate(R.layout.fragment_main, container, false)
        val NavController = NavHostFragment.findNavController(this)

        fragmentLayout.findViewById<Button>(R.id.btnCultures).setOnClickListener{ NavController.navigate(
            R.id.culturesFragment
        )}
        fragmentLayout.findViewById<Button>(R.id.btnFields).setOnClickListener{ NavController.navigate(
            R.id.fieldsFragment
        )}
        fragmentLayout.findViewById<Button>(R.id.btnSowings).setOnClickListener{ NavController.navigate(
            R.id.sowingsFragment
        )}
        fragmentLayout.findViewById<Button>(R.id.btnHarvest).setOnClickListener{ NavController.navigate(
            R.id.harvestFragment
        )}
        // Inflate the layout for this fragment
        return fragmentLayout
    }
}