package com.example.agronom.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.agronom.R

class MainFragment : Fragment() {

    private lateinit var btnCultures : Button
    private lateinit var btnFields : Button
    private lateinit var btnSowings : Button
    private lateinit var btnHarvest : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnCultures = view.findViewById(R.id.btnCultures)
        btnFields = view.findViewById(R.id.btnFields)
        btnSowings = view.findViewById(R.id.btnSowings)
        btnHarvest = view.findViewById(R.id.btnHarvest)

        btnCultures.setOnClickListener{ findNavController().navigate(
            R.id.culturesFragment
        )}

        btnFields.setOnClickListener{ findNavController().navigate(
            R.id.fieldsFragment
        )}

        btnSowings.setOnClickListener{ findNavController().navigate(
            R.id.sowingsFragment
        )}

        btnHarvest.setOnClickListener{ findNavController().navigate(
            R.id.harvestFragment
        )}
    }
}