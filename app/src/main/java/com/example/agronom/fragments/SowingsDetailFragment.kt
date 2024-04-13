package com.example.agronom.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.agronom.R
import com.example.agronom.data.Sowings
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.UUID


class SowingsDetailFragment : Fragment() {
    private lateinit var db : FirebaseFirestore
    private val argsSowing : SowingsDetailFragmentArgs by navArgs()
    lateinit var sowingData : Sowings
    var newSowing : Boolean = true
    private lateinit var svField : Spinner
    private lateinit var svCulture : Spinner
    private lateinit var svVarienty : Spinner
    private lateinit var svStatus : Spinner
    private lateinit var tvCount : EditText
    private lateinit var tvDateStart : TextView
    private lateinit var tvDateEnd : EditText
    private lateinit var saveBtn : Button
    private lateinit var pickDateStartBtn : ImageButton
    data class FieldItem(val fieldName: String, val size: String, val docId: String)
    data class CultureItem(val cultureName: String, val varienty: String, val boardingMonth: String, val growingSeason: String, val imagePath: String, val docId: String)
    var cultureNameItems = ArrayList<CultureItem>()
    var varientyItems = ArrayList<CultureItem>()
    val fieldItems = ArrayList<FieldItem>()
    var isEditMode = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sowings_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sowingData = argsSowing.currentSowing
        db = FirebaseFirestore.getInstance()

        initializeViews(view)
        setupFieldsSpinner()
        setupCulturesSpinner()
        if(sowingData.docId != null) {
            saveBtn.isVisible = false
            newSowing = false
            changeInputType(false)
            showMenuButtons()
        }
        loadData()
    }

    private fun initializeViews(view: View) {
        pickDateStartBtn = view.findViewById(R.id.pickDateStartBtn)
        pickDateStartBtn.setOnClickListener {
            showDatePickerDialog()
        }

        svField = view.findViewById(R.id.svField)
        svCulture = view.findViewById(R.id.svCulture)
        svVarienty = view.findViewById(R.id.svVarienty)
        tvCount = view.findViewById(R.id.tvCount)
        tvDateStart = view.findViewById(R.id.tvDateStart)
        tvDateEnd = view.findViewById(R.id.tvDateEnd)
        svStatus = view.findViewById(R.id.svStatus)
        val statusAdapter = ArrayAdapter(view.context, R.layout.spinner_item, resources.getStringArray(R.array.sowingStatus))
        statusAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        svStatus.setAdapter(statusAdapter)

        saveBtn = view.findViewById(R.id.saveBtn)
        saveBtn.setOnClickListener {
            updateData()
        }
    }
    private fun showMenuButtons(){
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.edit_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                val id = menuItem.itemId
                //handle item clicks
                if (id == R.id.editBtn) {
                    if (isEditMode) {
                        menuItem.setIcon(R.drawable.edit_icon)
                    } else {
                        menuItem.setIcon(R.drawable.cancel_ic)
                    }
                    showData(false)
                    isEditMode = !isEditMode
                }
                if (id == R.id.deleteBtn) {
                    deleteDialog()
                }
                return true
            }
        }, viewLifecycleOwner)
    }
    private fun deleteDialog() {
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
        db.collection("Sowings").document(sowingData.docId.toString()).delete()
            .addOnSuccessListener {
                Snackbar.make(requireView(), "Данные удалены", Snackbar.LENGTH_SHORT).show()
                findNavController().navigate(SowingsDetailFragmentDirections.actionSowingsDetailFragmentToSowingsFragment())
            }
            .addOnFailureListener {
                Snackbar.make(requireView(), "Ошибка", Snackbar.LENGTH_SHORT).show()
            }
    }
    private fun showDatePickerDialog(){
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireView().context,
            { _, selectedYear, selectedMonth, dayOfMonth ->
                tvDateStart.text = "${dayOfMonth}.${selectedMonth + 1}.$selectedYear"
            }, year, month, day
        ).show()
    }
    private fun setupFieldsSpinner() {
        db.collection("Fields").get().addOnCompleteListener{ task ->
            if (task.isSuccessful) {
                for (document in task.result) {
                    val name = document.getString("name")
                    val size = document.getString("name")
                    val docId = document.id
                    fieldItems.add(FieldItem(name.toString(), size.toString(),docId))
                }

                val arrayAdapter = ArrayAdapter(requireActivity().applicationContext, R.layout.spinner_item, fieldItems.map { it.fieldName })
                arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                svField.setAdapter(arrayAdapter)
                arrayAdapter.notifyDataSetChanged()
                if(sowingData.docId != null){
                    svField.setSelection(fieldItems.indexOfFirst { it.docId == sowingData.field!!["docId"] })
                }
            }
        }
    }
    private fun setupCulturesSpinner() {
        db.collection("Cultures").get().addOnCompleteListener{ task ->
            if (task.isSuccessful) {
                val cultureItems = task.result.documents.map { document ->
                    CultureItem(
                        document.getString("cultureName").toString(),
                        document.getString("varienty").toString(),
                        document.getString("boardingMonth").toString(),
                        document.getString("growingSeason").toString(),
                        document.getString("imagePath").toString(),
                        document.id
                    )
                }

                cultureNameItems = cultureItems.distinctBy{ t -> t.cultureName } as ArrayList<CultureItem>
                val cultureAdapter = ArrayAdapter(requireActivity().applicationContext, R.layout.spinner_item, cultureNameItems.map { it.cultureName })
                cultureAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                svCulture.setAdapter(cultureAdapter)
                cultureAdapter.notifyDataSetChanged()
                if(sowingData.docId != null){
                    svCulture.setSelection(cultureNameItems.indexOfFirst { it.cultureName == sowingData.culture!!["cultureName"] })
                }


                svCulture.onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        varientyItems = cultureItems.filter { it.cultureName == svCulture.selectedItem.toString() } as ArrayList<CultureItem>
                        val varientyAdapter = ArrayAdapter(requireActivity().applicationContext, R.layout.spinner_item, varientyItems.map { it.varienty })
                        varientyAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                        svVarienty.setAdapter(varientyAdapter)
                        varientyAdapter.notifyDataSetChanged()
                        if(sowingData.docId != null){
                            svVarienty.setSelection(varientyItems.indexOfFirst { it.docId == sowingData.culture!!["docId"] })
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Действия при отсутствии выбора
                    }
                }
            }
        }
    }
    private fun loadData(){
        if(sowingData.docId != null) {
            svField.setSelection(fieldItems.indexOfFirst { it.docId == sowingData.field!!["docId"] })
            svCulture.setSelection(cultureNameItems.indexOfFirst { it.cultureName == sowingData.culture!!["cultureName"] })
            tvCount.setText(sowingData.count.toString())
            tvDateStart.text = sowingData.date
            if (sowingData.status!!) {
                svStatus.setSelection(1)
            } else {
                svStatus.setSelection(0)
            }
        }
    }
    private fun updateData(){
        if(svStatus.selectedItemPosition == 0){
            sowingData.status = false
        }
        else if(svStatus.selectedItemPosition == 1){
            sowingData.status = true
        }
        sowingData.date = tvDateStart.text.toString()
        sowingData.count = tvCount.text.toString().toDouble()
        sowingData.culture = mapOf(
            "cultureName" to varientyItems[svVarienty.selectedItemPosition].cultureName,
            "varienty" to varientyItems[svVarienty.selectedItemPosition].varienty,
            "boardingMonth" to varientyItems[svVarienty.selectedItemPosition].boardingMonth,
            "growingSeason" to varientyItems[svVarienty.selectedItemPosition].growingSeason,
            "imagePath" to varientyItems[svVarienty.selectedItemPosition].imagePath,
            "docId" to varientyItems[svVarienty.selectedItemPosition].docId
        )
        sowingData.field = mapOf(
            "name" to fieldItems[svField.selectedItemPosition].fieldName,
            "size" to fieldItems[svField.selectedItemPosition].size,
            "docId" to fieldItems[svField.selectedItemPosition].docId
        )
        val updates = mapOf(
            "culture" to sowingData.culture,
            "field" to sowingData.field,
            "status" to sowingData.status,
            "count" to sowingData.count,
            "date" to sowingData.date
        )
        if(sowingData.docId != null) {
            db.collection("Sowings").document(sowingData.docId.toString()).update(updates)
                .addOnSuccessListener {

                }
                .addOnFailureListener {

                }
        }
        else{
            sowingData.docId = UUID.randomUUID().toString()
            db.collection("Sowings").document(sowingData.docId!!).set(updates)
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
            if(newSowing){
                Snackbar.make(requireView(), "Данные сохранены", Snackbar.LENGTH_SHORT).show()
                findNavController().navigate(SowingsDetailFragmentDirections.actionSowingsDetailFragmentToSowingsFragment())
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
    @SuppressLint("ClickableViewAccessibility")
    private fun changeInputType(type:Boolean){
        if(type){
            tvCount.setInputType(InputType.TYPE_CLASS_TEXT)
            svField.setOnTouchListener { v, event ->
                false
            }
            svCulture.setOnTouchListener { v, event ->
                false
            }
            svVarienty.setOnTouchListener { v, event ->
                false
            }
            svStatus.setOnTouchListener { v, event ->
                false
            }
        }
        else{
            tvCount.setInputType(InputType.TYPE_NULL)
            svField.setOnTouchListener { v, event ->
                true
            }
            svCulture.setOnTouchListener { v, event ->
                true
            }
            svVarienty.setOnTouchListener { v, event ->
                true
            }
            svStatus.setOnTouchListener { v, event ->
                true
            }
        }
    }
}