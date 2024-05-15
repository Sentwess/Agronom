package com.example.agronom.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
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
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.text.isDigitsOnly
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agronom.R
import com.example.agronom.adapters.FieldsAdapter
import com.example.agronom.data.Fields
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import java.util.Locale
import java.util.UUID

class FieldsFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var fieldsRecyclerView: RecyclerView
    private lateinit var fieldsAdapter: FieldsAdapter
    private lateinit var fieldsArrayList: ArrayList<Fields>
    private lateinit var addBtn: ImageButton
    private lateinit var tvNoItems : TextView
    private lateinit var svStatus : AutoCompleteTextView
    private lateinit var sortLayout : LinearLayout
    var isSortMode = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fields, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sortLayout = view.findViewById(R.id.sortLayout)
        fieldsRecyclerView = view.findViewById(R.id.fieldsList)
        addBtn = view.findViewById(R.id.addBtn)
        tvNoItems = view.findViewById(R.id.tvNoItems)
        svStatus = view.findViewById(R.id.svStatus)
        val status = ArrayList<String>()
        status.add("Все поля");
        status.add("Свободно");
        status.add("Засеяно")
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, status)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        svStatus.setAdapter(adapter)
        svStatus.setText("Все поля", false)

        fieldsRecyclerView.layoutManager = GridLayoutManager(context, 2)
        fieldsRecyclerView.setHasFixedSize(true)

        fieldsArrayList = arrayListOf<Fields>()
        fieldsAdapter = FieldsAdapter(fieldsArrayList)
        fieldsRecyclerView.adapter = fieldsAdapter

        getFieldsData()

        addBtn.setOnClickListener {
            val field = Fields(
                null,
                null,
                null,
                false
            )
            openDialog(field)
        }

        fieldsAdapter.setOnItemClickListener(object : FieldsAdapter.OnItemClickListener {
            override fun onItemClick(item: Fields) {
                openDialog(item)
            }

        })

        showMenuButtons()
    }

    private fun openDeleteDialog(field: Fields, dialogEdit: AlertDialog) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_custom, null)
        val customDialog = AlertDialog.Builder(view?.context).setView(dialogView).show()
        customDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_border)
        val btDismiss = dialogView.findViewById<Button>(R.id.btDismissCustomDialog)
        val btPositive = dialogView.findViewById<Button>(R.id.btPositiveCustomDialog)
        btDismiss.setOnClickListener {
            customDialog.dismiss()
        }
        btPositive.setOnClickListener {
            db = FirebaseFirestore.getInstance()
            db.collection("Fields").document(field.docId.toString()).delete().addOnSuccessListener {
                    Snackbar.make(requireView(), "Данные удалены", Snackbar.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Snackbar.make(requireView(), "Ошибка", Snackbar.LENGTH_SHORT).show()
                }
            customDialog.dismiss()
            dialogEdit.dismiss()
            fieldsArrayList.clear()
            getFieldsData()
        }
    }

    private fun openDialog(field: Fields) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_field, null)
        val customDialog = AlertDialog.Builder(view?.context).setView(dialogView).show()
        customDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_border)

        val btnSave = dialogView.findViewById<Button>(R.id.saveBtn)
        val btDismiss = dialogView.findViewById<Button>(R.id.cancelBtn)
        val btDelete = dialogView.findViewById<Button>(R.id.deleteBtn)
        val tvName = dialogView.findViewById<EditText>(R.id.tvName)
        val tvSize = dialogView.findViewById<EditText>(R.id.tvSize)
        val toggleButton = dialogView.findViewById<MaterialButtonToggleGroup>(R.id.toggleButton)

        if (field.docId != null) {

            if (field.status!!) {
                (toggleButton.getChildAt(1) as MaterialButton).isChecked = true
            } else {
                (toggleButton.getChildAt(0) as MaterialButton).isChecked = true
            }

            tvName.setText(field.name)
            tvSize.setText(field.size)
            btnSave.text = "Изменить"
        } else {
            btDelete.isVisible = false

            btnSave.text = "Добавить"
        }
        btDismiss.setOnClickListener {
            customDialog.dismiss()
        }
        btDelete.setOnClickListener {
            openDeleteDialog(field, customDialog)
        }
        btnSave.setOnClickListener {
            val errors = mutableListOf<String>()
            if(tvName.text.isNullOrBlank() || tvName.text.isNullOrEmpty()){
                errors.add("- Название поля")
            }
            if(tvSize.text.isNullOrBlank() || tvSize.text.isNullOrEmpty() || !tvSize.text.isDigitsOnly()){
                errors.add("- Площадь поля")
            }
            if(errors.size > 0){
                createDialog(errors)
            }
            else {
                field.name = tvName.text.toString()
                field.size = tvSize.text.toString()
                field.status = if ((toggleButton.getChildAt(0) as MaterialButton).isChecked) {
                    false
                } else {
                    true
                }

                updateData(field)
                customDialog.dismiss()
            }
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

    private fun updateData(fieldData: Fields) {
        db = FirebaseFirestore.getInstance()
        val updates = mapOf(
            "name" to fieldData.name,
            "size" to fieldData.size,
            "status" to fieldData.status
        )
        if (fieldData.docId != null) {
            db.collection("Fields").document(fieldData.docId.toString()).update(updates)
                .addOnSuccessListener {
                    fieldsArrayList.clear()
                    getFieldsData()
                }.addOnFailureListener {

                }

            db.collection("Sowings").whereEqualTo("field.docId", fieldData.docId).get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val docId = document.id
                        val status = when (fieldData.status) {
                            true -> "Засеяно"
                            false -> "Свободно"
                            null -> ""
                        }
                        val data = mapOf(
                            "field.name" to fieldData.name,
                            "field.size" to fieldData.size,
                            "field.status" to status
                        )

                        db.collection("Sowings").document(docId).update(data).addOnSuccessListener {

                            }.addOnFailureListener { e ->

                            }
                    }
                }.addOnFailureListener { e ->

                }

            db.collection("Harvests").whereEqualTo("field.docId", fieldData.docId).get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val docId = document.id
                        val status = when (fieldData.status) {
                            true -> "Засеяно"
                            false -> "Свободно"
                            null -> ""
                        }
                        val data = mapOf(
                            "field.name" to fieldData.name,
                            "field.size" to fieldData.size,
                            "field.status" to status
                        )

                        db.collection("Harvests").document(docId).update(data).addOnSuccessListener {

                        }.addOnFailureListener { e ->

                        }
                    }
                }.addOnFailureListener { e ->

                }
        } else {
            fieldData.docId = UUID.randomUUID().toString()
            db.collection("Fields").document(fieldData.docId!!).set(updates).addOnSuccessListener {
                    fieldsArrayList.clear()
                    getFieldsData()
                }.addOnFailureListener {

                }
        }
    }

    private fun showMenuButtons() {
        val menuHost: MenuHost = requireActivity()
        var searchView: SearchView? = null
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.search_menu, menu)
                val searchItem: MenuItem = menu.findItem(R.id.searchBar)
                searchView = searchItem.actionView as SearchView
                svStatus.setOnItemClickListener { parent, view, position, id ->
                    filteredList(searchView?.query.toString())
                }

                searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        filteredList(newText)
                        return true
                    }

                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                val id = menuItem.itemId
                if (id == R.id.sortBtn) {
                    isSortMode = !isSortMode
                    if (isSortMode) {
                        sortLayout.isVisible = true
                        menuItem.setIcon(R.drawable.settings_sliders_clicked)
                    } else {
                        sortLayout.isVisible = false
                        menuItem.setIcon(R.drawable.settings_sliders)
                        svStatus.setText("Все поля",false)
                        filteredList(searchView?.query.toString())
                    }
                }
                return true
            }
        }, viewLifecycleOwner)
    }

    private fun getFieldsData() {
        db = FirebaseFirestore.getInstance()
        db.collection("Fields").addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if (error != null) {
                    Log.e("Firestore Error", error.message.toString())
                }
                for (dc: DocumentChange in value?.documentChanges!!) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val field = dc.document.toObject(Fields::class.java)
                        field.docId = dc.document.id
                        fieldsArrayList.add(field)
                    }
                }
                fieldsAdapter.notifyDataSetChanged()
                noItems()
            }
        })
    }

    fun filteredList(query: String?) {
        var filteredList = ArrayList<Fields>()
        if (query != null) {
            for (i in fieldsArrayList) {
                if (i.name?.lowercase(Locale.ROOT)!!.contains(query.lowercase())
                    || i.size?.lowercase(Locale.ROOT)!!.contains(query.lowercase())
                ) {
                    filteredList.add(i)
                }
            }
        }

        if(svStatus.text.contains("Свободно")){
            filteredList = filteredList.filter { t -> !t.status!! } as ArrayList<Fields>
        }
        if(svStatus.text.contains("Засеяно")){
            filteredList = filteredList.filter { t -> t.status!! } as ArrayList<Fields>
        }

        if (filteredList.isEmpty()) {
            filteredList.clear()
        }
        fieldsAdapter.setFilteredList(filteredList)
        noItems()
    }

    private fun noItems(){
        if(fieldsAdapter.itemCount == 0){
            tvNoItems.text = "Нет записей"
        }
        else{
            tvNoItems.text = ""
        }
    }
}