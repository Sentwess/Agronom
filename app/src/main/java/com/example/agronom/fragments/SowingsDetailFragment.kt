package com.example.agronom.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.agronom.R
import com.example.agronom.adapters.CultureViewAdapter
import com.example.agronom.adapters.HarvestViewAdapter
import com.example.agronom.data.Sowings
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Calendar
import java.util.UUID


class SowingsDetailFragment : Fragment() {
    private lateinit var db : FirebaseFirestore
    private val argsSowing : SowingsDetailFragmentArgs by navArgs()
    lateinit var sowingData : Sowings
    var newSowing : Boolean = true
    private lateinit var fieldLayout : TextInputLayout
    private lateinit var cultureLayout : TextInputLayout
    private lateinit var varientyLayout : TextInputLayout
    private lateinit var svField : AutoCompleteTextView
    private lateinit var svCulture : AutoCompleteTextView
    private lateinit var svVarienty : AutoCompleteTextView
    private lateinit var tvCount : EditText
    private lateinit var tvCountHarvest : EditText
    private lateinit var tvDateStart : EditText
    private lateinit var tvDateEnd : EditText
    private lateinit var cancelBtn : Button
    private lateinit var harvestBtn : Button
    private lateinit var saveBtn : Button
    private lateinit var harvestLayout : LinearLayout
    private lateinit var startDateLayout : TextInputLayout
    private lateinit var endDateLayout : TextInputLayout
    private lateinit var cultureAdapter : CultureViewAdapter
    private lateinit var harvestAdapter : HarvestViewAdapter
    private lateinit var expListViewCulture : ExpandableListView
    private lateinit var expListViewHarvest : ExpandableListView
    data class FieldItem(val fieldName: String, val size: String, val docId: String)
    data class CultureItem(val cultureName: String, val varienty: String, val boardingMonth: String, val growingSeason: String, val imagePath: String, val docId: String)
    var cultureNameItems = ArrayList<CultureItem>()
    var varientyItems = ArrayList<CultureItem>()
    val fieldItems = ArrayList<FieldItem>()
    var cultureItems = ArrayList<CultureItem>()

    var cultureView = mapOf<String,String>()
    var harvestView = mapOf<String,String>()

    var isEditMode = false
    var isHarvestMode = false

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
            if(sowingData.status == true){
                harvestBtn.isVisible = true
            }
        }
        else{
            saveBtn.text = "Создать посев"
        }
        loadData()
        tvDateStart.setInputType(InputType.TYPE_NULL)
        tvDateEnd.setInputType(InputType.TYPE_NULL)
    }

    private fun initializeViews(view: View) {
        expListViewCulture = view.findViewById(R.id.expListViewCulture)
        expListViewHarvest = view.findViewById(R.id.expListViewHarvest)

        harvestLayout = view.findViewById(R.id.harvestLayout)
        startDateLayout = view.findViewById(R.id.startDateLayout)
        endDateLayout = view.findViewById(R.id.endDateLayout)
        startDateLayout.setEndIconOnClickListener{
            if(isEditMode || sowingData.docId == null){
                showDatePickerDialog(true)
                startDateLayout.isFocusable = true
            }
        }
        endDateLayout.setEndIconOnClickListener{
            showDatePickerDialog(false)
            endDateLayout.isFocusable = true
        }

        fieldLayout = view.findViewById(R.id.fieldLayout)
        cultureLayout = view.findViewById(R.id.cultureLayout)
        varientyLayout = view.findViewById(R.id.varientyLayout)

        svField = view.findViewById(R.id.svField)
        svCulture = view.findViewById(R.id.svCulture)
        svVarienty = view.findViewById(R.id.svVarienty)
        tvCount = view.findViewById(R.id.tvCount)
        tvCountHarvest = view.findViewById(R.id.tvCountHarvest)
        tvDateStart = view.findViewById(R.id.tvDateStart)
        tvDateEnd = view.findViewById(R.id.tvDateEnd)

        cancelBtn = view.findViewById(R.id.cancelBtn)
        harvestBtn = view.findViewById(R.id.harvestBtn)
        saveBtn = view.findViewById(R.id.saveBtn)
        if(sowingData.docId == null) {
            saveBtn.setOnClickListener {
                updateData()
            }
        }
        harvestBtn.setOnClickListener {
            if(sowingData.status == true) {
                isHarvestMode = true
                showHarvest()
            }
        }
        cancelBtn.setOnClickListener {
            if(cancelBtn.isVisible) {
                isHarvestMode = false
                showHarvest()
            }
        }
    }

    private fun showMenuButtons(){
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.edit_menu, menu)
                val myMenuItem = menu.findItem(R.id.editBtn)
                saveBtn.setOnClickListener {
                    updateData()
                    if(isEditMode){
                        myMenuItem?.setIcon(R.drawable.cancel_ic)
                    }
                    else{
                        myMenuItem?.setIcon(R.drawable.edit_icon)
                    }
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                val id = menuItem.itemId
                //handle item clicks
                if (id == R.id.editBtn) {
                    if(!isHarvestMode) {
                        if (isEditMode) {
                            menuItem.setIcon(R.drawable.edit_icon)
                        } else {
                            menuItem.setIcon(R.drawable.cancel_ic)
                        }
                        showData(false)
                        isEditMode = !isEditMode
                    }
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
        customDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_border)
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
    private fun showDatePickerDialog(startDate:Boolean){
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireView().context,
            { _, selectedYear, selectedMonth, dayOfMonth ->
                var day: String = dayOfMonth.toString()
                var month: String = selectedMonth.toString()
                if(dayOfMonth < 10){
                    day = "0${dayOfMonth}"
                }
                if(selectedMonth < 10){
                    month = "0${selectedMonth + 1}"
                }
                if(startDate) {
                    tvDateStart.setText("${day}.${month}.$selectedYear")
                }
                else{
                    tvDateEnd.setText("${day}.${month}.$selectedYear")
                }
            }, year, month, day
        ).show()
    }
    private fun setupFieldsSpinner() {
        db.collection("Fields").get().addOnCompleteListener{ task ->
            if (task.isSuccessful) {
                for (document in task.result) {
                    val name = document.getString("name")
                    val size = document.getString("size")
                    val docId = document.id
                    fieldItems.add(FieldItem(name.toString(), size.toString(),docId))
                }

                val arrayAdapter = ArrayAdapter(requireActivity().applicationContext, R.layout.spinner_item, fieldItems.map { it.fieldName })
                arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                svField.setAdapter(arrayAdapter)
                arrayAdapter.notifyDataSetChanged()
                loadHarvestView()
            }
        }
        svField.setOnItemClickListener { parent, view, position, id ->
            loadHarvestView()
        }

        harvestAdapter = HarvestViewAdapter(requireContext(), harvestView)
        expListViewHarvest.setAdapter(harvestAdapter)
        expListViewHarvest.setOnGroupClickListener { parent, v, groupPosition, id ->
            setListViewHeight(parent, groupPosition)
            false
        }
    }

    private fun loadHarvestView(){
        val selectedItemField = fieldItems.indexOfFirst { it.fieldName.compareTo(svField.text.toString()) == 0}
        if(!svField.text.isNullOrBlank() && selectedItemField > -1) {
            val docIdField = fieldItems[selectedItemField].docId

            var lastDocument: DocumentSnapshot? = null
            db.collection("Harvests")
                .whereEqualTo("field.docId", docIdField)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result) {
                            lastDocument = document
                        }
                        harvestView = mapOf(
                            "culture" to lastDocument?.get("culture.cultureName").toString(),
                            "imagePath" to lastDocument?.get("culture.imagePath").toString(),
                            "varienty" to lastDocument?.get("culture.varienty").toString(),
                            "field" to lastDocument?.get("field.name").toString(),
                            "count" to lastDocument?.get("count").toString(),
                            "date" to lastDocument?.get("date").toString()
                        )
                        if (harvestView["date"]?.contains("null") != true) {
                            expListViewHarvest.isVisible = true
                            harvestAdapter.updateInfo(harvestView)
                        } else {
                            expListViewHarvest.isVisible = false
                        }
                    }
                }
        }
        else{
            expListViewHarvest.isVisible = false
        }
    }

    private fun setupCulturesSpinner() {
        db.collection("Cultures").get().addOnCompleteListener{ task ->
            if (task.isSuccessful) {
                cultureItems = task.result.documents.map { document ->
                    CultureItem(
                        document.getString("cultureName").toString(),
                        document.getString("varienty").toString(),
                        document.getString("boardingMonth").toString(),
                        document.getString("growingSeason").toString(),
                        document.getString("imagePath").toString(),
                        document.id
                    )
                } as ArrayList<CultureItem>

                cultureNameItems = cultureItems.distinctBy{ t -> t.cultureName } as ArrayList<CultureItem>
                val culturesAdapter = ArrayAdapter(requireActivity().applicationContext, R.layout.spinner_item, cultureNameItems.map { it.cultureName })
                culturesAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                svCulture.setAdapter(culturesAdapter)
                culturesAdapter.notifyDataSetChanged()

                if(sowingData.docId != null) {
                    setupVarientySpinner(cultureItems)
                    svVarienty.setText(sowingData.culture!!["varienty"], false)
                }

                cultureAdapter = CultureViewAdapter(requireContext(), cultureView)
                expListViewCulture.setAdapter(cultureAdapter)
                expListViewCulture.setOnGroupClickListener { parent, v, groupPosition, id ->
                    setListViewHeight(parent, groupPosition)
                    false
                }
                loadCultureView()

                svCulture.setOnItemClickListener { parent, view, position, id ->
                    setupVarientySpinner(cultureItems)
                    loadCultureView()
                }

                svVarienty.setOnItemClickListener { parent, view, position, id ->
                    loadCultureView()
                }
            }
        }
    }

    private fun loadCultureView(){
        try {
            if (!svVarienty.text.isNullOrEmpty()) {
                expListViewCulture.isVisible = true
                val selectedItemCulture =
                    varientyItems.indexOfFirst { it.varienty.compareTo(svVarienty.text.toString()) == 0 }
                cultureView = mapOf(
                    "cultureName" to varientyItems[selectedItemCulture].cultureName,
                    "varienty" to varientyItems[selectedItemCulture].varienty,
                    "boardingMonth" to varientyItems[selectedItemCulture].boardingMonth,
                    "growingSeason" to varientyItems[selectedItemCulture].growingSeason,
                    "imagePath" to varientyItems[selectedItemCulture].imagePath,
                )
                cultureAdapter.updateCropInfo(cultureView)
            } else {
                expListViewCulture.isVisible = false
            }
        }
        catch(e: Exception){
            if (!svVarienty.text.isNullOrEmpty()) {
                expListViewCulture.isVisible = true
                cultureView = mapOf(
                    "cultureName" to sowingData.culture?.get("cultureName").toString(),
                    "varienty" to sowingData.culture?.get("varienty").toString(),
                    "boardingMonth" to sowingData.culture?.get("boardingMonth").toString(),
                    "growingSeason" to sowingData.culture?.get("growingSeason").toString(),
                    "imagePath" to sowingData.culture?.get("imagePath").toString(),
                )
                cultureAdapter.updateCropInfo(cultureView)
            } else {
                expListViewCulture.isVisible = false
            }
        }
    }

    private fun setListViewHeight(
        listView: ExpandableListView,
        group: Int
    ) {
        val listAdapter = listView.expandableListAdapter as ExpandableListAdapter
        var totalHeight = 0
        val desiredWidth = View.MeasureSpec.makeMeasureSpec(
            listView.width,
            View.MeasureSpec.EXACTLY
        )
        for (i in 0 until listAdapter.groupCount) {
            val groupItem = listAdapter.getGroupView(i, false, null, listView)
            groupItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED)
            totalHeight += groupItem.measuredHeight
            if (listView.isGroupExpanded(i) && i != group || !listView.isGroupExpanded(i) && i == group) {
                for (j in 0 until listAdapter.getChildrenCount(i)) {
                    val listItem = listAdapter.getChildView(
                        i, j, false, null,
                        listView
                    )
                    listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED)
                    totalHeight += listItem.measuredHeight
                }
            }
        }
        val params = listView.layoutParams
        var height = totalHeight + listView.dividerHeight * (listAdapter.groupCount - 1)
        if (height < 10) height = 200
        params.height = height
        listView.setLayoutParams(params)
        listView.requestLayout()
    }

    private fun setupVarientySpinner(cultureItems: ArrayList<CultureItem>){
        varientyItems = cultureItems.filter { it.cultureName == svCulture.text.toString() } as ArrayList<CultureItem>
        val varientyAdapter = ArrayAdapter(requireActivity().applicationContext, R.layout.spinner_item, varientyItems.map { it.varienty })
        varientyAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        svVarienty.setAdapter(varientyAdapter)
        varientyAdapter.notifyDataSetChanged()
        svVarienty.setText(varientyItems.first().varienty, false)
    }

    private fun loadData(){
        if(sowingData.docId != null) {
            svField.setText(sowingData.field!!["name"], false)
            svCulture.setText(sowingData.culture!!["cultureName"], false)
            svVarienty.setText(sowingData.culture!!["varienty"], false)
            tvCount.setText(sowingData.count.toString())
            tvDateStart.setText(sowingData.date)
        }
    }
    private fun updateData(){
        val errors = mutableListOf<String>()
        if(!isHarvestMode) {
            if(svField.text.isNullOrBlank() || svField.text.isNullOrEmpty()){
                errors.add("- Поле")
            }
            if(svVarienty.text.isNullOrBlank() || svVarienty.text.isNullOrEmpty()){
                errors.add("- Культура и сорт")
            }
            tvCount.setText(tvCount.text.toString().replace(',', '.'))
            tvCount.setSelection(tvCount.getText().length)
            if(tvCount.text.isNullOrBlank() || tvCount.text.isNullOrEmpty() || tvCount.text.toString().toDoubleOrNull() == null){
                errors.add("- Количество посева")
            }
            if(tvDateStart.text.isNullOrBlank() || tvDateStart.text.isNullOrEmpty()){
                errors.add("- Дата посева")
            }
        }
        else{
            tvCountHarvest.setText(tvCountHarvest.text.toString().replace(',', '.'))
            tvCountHarvest.setSelection(tvCountHarvest.getText().length)
            if (tvCountHarvest.text.isNullOrBlank() || tvCountHarvest.text.isNullOrEmpty() || tvCountHarvest.text.toString().toDoubleOrNull() == null) {
                errors.add("- Количество урожая")
            }
            if (tvDateEnd.text.isNullOrBlank() || tvDateEnd.text.isNullOrEmpty()) {
                errors.add("- Дата уборки")
            }
        }
        if(errors.size > 0){
            createDialog(errors)
        }
        else {
            if(!isEditMode) {
                sowingData.status = !isHarvestMode
            }
            sowingData.date = tvDateStart.text.toString()
            sowingData.count = tvCount.text.toString().toDouble()
            val selectedItemCulture = cultureItems.indexOfFirst { it.varienty.compareTo(svVarienty.text.toString()) == 0}
            sowingData.culture = mapOf(
                "cultureName" to cultureItems[selectedItemCulture].cultureName,
                "varienty" to cultureItems[selectedItemCulture].varienty,
                "boardingMonth" to cultureItems[selectedItemCulture].boardingMonth,
                "growingSeason" to cultureItems[selectedItemCulture].growingSeason,
                "imagePath" to cultureItems[selectedItemCulture].imagePath,
                "docId" to cultureItems[selectedItemCulture].docId
            )
            val selectedItemField = fieldItems.indexOfFirst { it.fieldName.compareTo(svField.text.toString()) == 0}
            sowingData.field = mapOf(
                "name" to fieldItems[selectedItemField].fieldName,
                "size" to fieldItems[selectedItemField].size,
                "docId" to fieldItems[selectedItemField].docId
            )
            val updates = mapOf(
                "culture" to sowingData.culture,
                "field" to sowingData.field,
                "status" to sowingData.status,
                "count" to sowingData.count,
                "date" to sowingData.date
            )

            if (sowingData.docId != null) {
                db.collection("Sowings").document(sowingData.docId.toString()).update(updates)
                    .addOnSuccessListener {

                    }
                    .addOnFailureListener {

                    }

                if (isHarvestMode) {
                    val harvest = mapOf(
                        "culture" to sowingData.culture,
                        "field" to sowingData.field,
                        "sowing" to mapOf(
                            "docId" to sowingData.docId,
                            "count" to sowingData.count,
                            "date" to sowingData.date
                        ),
                        "count" to tvCountHarvest.text.toString().toDouble(),
                        "date" to tvDateEnd.text.toString()
                    )
                    db.collection("Harvests").document(UUID.randomUUID().toString()).set(harvest)
                        .addOnSuccessListener {
                            val field = mapOf(
                                "status" to false,
                            )
                            db.collection("Fields").document(sowingData.field!!["docId"]!!).update(field)
                                .addOnSuccessListener {

                                }
                                .addOnFailureListener {

                                }
                        }
                        .addOnFailureListener {

                        }
                }
            } else {
                sowingData.docId = UUID.randomUUID().toString()
                db.collection("Sowings").document(sowingData.docId!!).set(updates)
                    .addOnSuccessListener {
                        val field = mapOf(
                            "status" to true,
                        )
                        db.collection("Fields").document(sowingData.field!!["docId"]!!).update(field)
                            .addOnSuccessListener {

                            }
                            .addOnFailureListener {

                            }
                    }
                    .addOnFailureListener {

                    }
            }
            showData(true)
        }
    }

    private fun createDialog(messages: MutableList<String>) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_message, null)
        val customDialog = AlertDialog.Builder(view?.context)
            .setView(dialogView)
            .show()
        customDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_border)
        val textView = dialogView.findViewById<TextView>(R.id.tvMessage)
        val listAsString = messages.joinToString("\n")
        textView.text = listAsString
        val okBtn = dialogView.findViewById<Button>(R.id.okBtn)
        okBtn.setOnClickListener {
            customDialog.dismiss()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showData(save : Boolean){
        if(save){
            loadData()
            changeInputType(false)
            saveBtn.isVisible = false
            if(isEditMode){
                showHarvest()
                isEditMode = false
            }
            if(newSowing){
                Snackbar.make(requireView(), "Запись о посеве создана", Snackbar.LENGTH_SHORT).show()
                findNavController().navigate(SowingsDetailFragmentDirections.actionSowingsDetailFragmentToSowingsFragment())
            }
            else if(isHarvestMode){
                showHarvest()
                isHarvestMode = false
                Snackbar.make(requireView(), "Запись о урожае создана", Snackbar.LENGTH_SHORT).show()
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
                if(sowingData.status == true){
                    harvestBtn.isVisible = false
                }
            }
            else{
                loadData()
                loadCultureView()
                loadHarvestView()
                changeInputType(false)
                saveBtn.isVisible = false
                if(sowingData.status == true){
                    harvestBtn.isVisible = true
                }
            }
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun changeInputType(type:Boolean){
        if(type){
            fieldLayout.isEndIconVisible = true
            cultureLayout.isEndIconVisible = true
            varientyLayout.isEndIconVisible = true
            tvCount.setInputType(InputType.TYPE_CLASS_TEXT)
            svField.setOnTouchListener { v, event ->
                svField.showDropDown()
                false
            }
            svCulture.setOnTouchListener { v, event ->
                svCulture.showDropDown()
                false
            }
            svVarienty.setOnTouchListener { v, event ->
                svVarienty.showDropDown()
                false
            }
        }
        else{
            fieldLayout.isEndIconVisible = false
            cultureLayout.isEndIconVisible = false
            varientyLayout.isEndIconVisible = false
            tvCount.setInputType(InputType.TYPE_NULL)
            svField.setOnTouchListener { v, event ->
                svField.requestFocus()
                true
            }
            svCulture.setOnTouchListener { v, event ->
                svCulture.requestFocus()
                true
            }
            svVarienty.setOnTouchListener { v, event ->
                svVarienty.requestFocus()
                true
            }
        }
    }

    private fun showHarvest(){
        if(sowingData.status == true) {
            if (isHarvestMode) {
                harvestLayout.isVisible = true
                saveBtn.isVisible = true
                cancelBtn.isVisible = true
                harvestBtn.isVisible = false
                saveBtn.text = "Завершить"
                saveBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.yellow) )
            } else {
                harvestLayout.isVisible = false
                saveBtn.isVisible = false
                cancelBtn.isVisible = false
                harvestBtn.isVisible = true
                saveBtn.text = "Сохранить"
                saveBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.focused) )
            }
        }
        else{
            harvestLayout.isVisible = false
            saveBtn.isVisible = false
            cancelBtn.isVisible = false
        }
    }
}