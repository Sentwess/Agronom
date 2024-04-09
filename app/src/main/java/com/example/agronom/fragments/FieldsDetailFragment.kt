package com.example.agronom.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.agronom.R
import com.example.agronom.data.Fields
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class FieldsDetailFragment : Fragment() {
    private val argsField : FieldsDetailFragmentArgs by navArgs()
    private lateinit var db : FirebaseFirestore
    private lateinit var tvName : EditText
    private lateinit var tvSize : EditText
    private lateinit var svStatus : Spinner
    private lateinit var saveBtn : Button
    lateinit var fieldData : Fields
    var newField : Boolean = true


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fields_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fieldData = argsField.currentField

        tvName = view.findViewById(R.id.tvName)
        tvSize = view.findViewById(R.id.tvSize)
        svStatus = view.findViewById(R.id.svStatus)
        val arrayAdapter = ArrayAdapter(view.context, R.layout.spinner_item, resources.getStringArray(R.array.status))
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        svStatus.setAdapter(arrayAdapter)

        saveBtn = view.findViewById(R.id.saveBtn)

        saveBtn.setOnClickListener{
            updateData()
        }

        if(fieldData.docId != null) {
            saveBtn.isVisible = false
            newField = false
            changeInputType(false)
            showMenuButtons()
        }
        loadData()
    }

    private fun showMenuButtons(){
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.edit_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                val id = menuItem.itemId
                //handle item clicks
                if (id == R.id.editBtn) {
                    showData(false)
                }
                if (id == R.id.deleteBtn) {
                    openDialog()
                }
                return true
            }
        }, viewLifecycleOwner)
    }

    private fun openDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_custom, null)
        val customDialog = AlertDialog.Builder(view?.context)
            .setView(dialogView)
            .show()
        val btDismiss = dialogView.findViewById<Button>(R.id.btDismissCustomDialog)
        val btPositive = dialogView.findViewById<Button>(R.id.btPositiveCustomDialog)
        btDismiss.setOnClickListener {
            customDialog.dismiss()
        }
        btPositive.setOnClickListener {
            deleteData()
            customDialog.dismiss()
        }
    }

    private fun deleteData(){
        db = FirebaseFirestore.getInstance()
        db.collection("Fields").document(fieldData.docId.toString()).delete()
            .addOnSuccessListener {
                Snackbar.make(requireView(), "Данные удалены", Snackbar.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Snackbar.make(requireView(), "Ошибка", Snackbar.LENGTH_SHORT).show()
            }
        findNavController().navigate(FieldsDetailFragmentDirections.actionFieldsDetailFragmentToFieldsFragment())
    }

    private fun updateData(){
        fieldData.name = tvName.text.toString()
        fieldData.size = tvSize.text.toString()
        if(svStatus.selectedItemPosition == 0){
            fieldData.status = false
        }
        else if(svStatus.selectedItemPosition == 1){
            fieldData.status = true
        }

        db = FirebaseFirestore.getInstance()
        val updates = mapOf(
            "name" to fieldData.name,
            "size" to fieldData.size,
            "status" to fieldData.status
        )
        if(fieldData.docId != null) {
            db.collection("Fields").document(fieldData.docId.toString()).update(updates)
                .addOnSuccessListener {

                }
                .addOnFailureListener {

                }

            db.collection("Sowings")
                .whereEqualTo("field.docId", fieldData.docId)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val docId = document.id
                        val status = when(fieldData.status){
                            true -> "Засеяно"
                            false -> "Свободно"
                            null -> ""
                        }
                        val data = mapOf(
                            "field.name" to fieldData.name,
                            "field.size" to fieldData.size,
                            "field.status" to status
                        )

                        db.collection("Sowings").document(docId)
                            .update(data)
                            .addOnSuccessListener {

                            }
                            .addOnFailureListener { e ->

                            }
                    }
                }
                .addOnFailureListener { e ->

                }
        }
        else{
            fieldData.docId = UUID.randomUUID().toString()
            db.collection("Fields").document(fieldData.docId!!).set(updates)
                .addOnSuccessListener {

                }
                .addOnFailureListener {

                }
        }
        showData(true)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showData(save : Boolean){
        if(save){
            loadData()
            changeInputType(false)
            saveBtn.isVisible = false
            if(newField){
                Snackbar.make(requireView(), "Данные сохранены", Snackbar.LENGTH_SHORT).show()
                findNavController().navigate(FieldsDetailFragmentDirections.actionFieldsDetailFragmentToFieldsFragment())
            }
            else{
                Snackbar.make(requireView(), "Данные обновлены", Snackbar.LENGTH_SHORT).show()
            }
        }
        else{
            if(!saveBtn.isVisible) {
                changeInputType(true)
                saveBtn.isVisible = true
            }
            else{
                loadData()
                changeInputType(false)
                saveBtn.isVisible = false
            }
        }
    }

    private fun loadData(){
        tvName.setText(fieldData.name)
        tvSize.setText(fieldData.size)
        if(fieldData.status!!){
            svStatus.setSelection(1)
        }
        else{
            svStatus.setSelection(0)
        }
    }
    private fun changeInputType(type:Boolean){
        if(type){
            tvName.setInputType(InputType.TYPE_CLASS_TEXT)
            tvSize.setInputType(InputType.TYPE_CLASS_TEXT)
            svStatus.setOnTouchListener { v, event ->
                false
            }
        }
        else{
            tvName.setInputType(InputType.TYPE_NULL)
            tvSize.setInputType(InputType.TYPE_NULL)
            svStatus.setOnTouchListener { v, event ->
                true
            }
        }
    }

}