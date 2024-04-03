package com.example.agronom.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.agronom.R
import com.example.agronom.data.Fields
import com.example.agronom.databinding.FragmentFieldsDetailBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class FieldsDetailFragment : Fragment() {
    private val argsField : FieldsDetailFragmentArgs by navArgs()
    private lateinit var db : FirebaseFirestore
    private lateinit var binding: FragmentFieldsDetailBinding
    private lateinit var tvName : EditText
    private lateinit var tvSize : EditText
    private lateinit var svStatus : Spinner
    private lateinit var editBtn : Button
    private lateinit var deleteBtn : Button
    private lateinit var saveBtn : Button
    lateinit var fieldData : Fields

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFieldsDetailBinding.inflate(inflater, container, false)
        return binding.root
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

        editBtn = view.findViewById(R.id.editBtn)
        deleteBtn = view.findViewById(R.id.deleteBtn)
        saveBtn = view.findViewById(R.id.saveBtn)

        editBtn.setOnClickListener { showData(false) }

        deleteBtn.setOnClickListener{
            openDialog()
        }

        saveBtn.setOnClickListener{
            updateData()
        }

        if(fieldData.docId == null){
            deleteBtn.isVisible = false
            editBtn.isVisible = false
            saveBtn.isVisible = true
        }
        else{
            deleteBtn.isVisible = true
            editBtn.isVisible = true
            showData(false)
        }
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
                Toast.makeText(context, "Данные удалены", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Ошибка", Toast.LENGTH_SHORT).show()
            }
        findNavController().navigate(CultureDetailFragmentDirections.actionCultureDetailFragmentToCulturesFragment())
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
            "status" to fieldData.status,
        )
        if(fieldData.docId != null) {
            db.collection("Fields").document(fieldData.docId.toString()).update(updates)
                .addOnSuccessListener {
                    Toast.makeText(context, "Данные обновлены", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Ошибка", Toast.LENGTH_SHORT).show()
                }
        }
        else{
            fieldData.docId = UUID.randomUUID().toString()
            db.collection("Fields").document(fieldData.docId!!).set(updates)
                .addOnSuccessListener {
                    Toast.makeText(context, "Данные сохранены", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Ошибка", Toast.LENGTH_SHORT).show()
                }
        }
        showData(true)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showData(save : Boolean){
        if(save){
            tvName.setInputType(InputType.TYPE_NULL)
            tvSize.setInputType(InputType.TYPE_NULL)
            editBtn.text = "Редактировать"
            deleteBtn.isVisible = true
            editBtn.isVisible = true
            saveBtn.isVisible = false

            tvName.setText(fieldData.name)
            tvSize.setText(fieldData.size)
            if(fieldData.status!!){
                svStatus.setSelection(1)
            }
            else{
                svStatus.setSelection(0)
            }
        }
        else{
            if(!saveBtn.isVisible) {
                tvName.setInputType(InputType.TYPE_CLASS_TEXT)
                tvSize.setInputType(InputType.TYPE_CLASS_TEXT)
                svStatus.setOnTouchListener { v, event ->
                    false
                }
                editBtn.text = "Отменить"
                saveBtn.isVisible = true
            }
            else{
                tvName.setText(fieldData.name)
                tvSize.setText(fieldData.size)
                if(fieldData.status!!){
                    svStatus.setSelection(1)
                }
                else{
                    svStatus.setSelection(0)
                }

                tvName.setInputType(InputType.TYPE_NULL)
                tvSize.setInputType(InputType.TYPE_NULL)
                svStatus.setOnTouchListener { v, event ->
                    true
                }
                editBtn.text = "Редактировать"
                saveBtn.isVisible = false
            }
        }
    }

}