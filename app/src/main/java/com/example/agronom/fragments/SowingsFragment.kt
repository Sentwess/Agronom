package com.example.agronom.fragments

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
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agronom.R
import com.example.agronom.adapters.SowingsAdapter
import com.example.agronom.data.Sowings
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.util.Locale

class SowingsFragment : Fragment() {

    private lateinit var db : FirebaseFirestore
    private lateinit var  sowingsRecyclerView : RecyclerView
    private lateinit var sowingsAdapter : SowingsAdapter
    private lateinit var  sowingsArrayList : ArrayList<Sowings>
    private lateinit var addBtn : ImageButton
    private lateinit var tvNoItems : TextView
    private lateinit var svCulture : AutoCompleteTextView
    private lateinit var svField : AutoCompleteTextView
    private lateinit var svDate : AutoCompleteTextView
    private lateinit var svStatus : AutoCompleteTextView
    private lateinit var sortLayout : LinearLayout
    var isSortMode = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sowings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sowingsRecyclerView = view.findViewById(R.id.sowingsList)
        addBtn = view.findViewById(R.id.addBtn)
        tvNoItems = view.findViewById(R.id.tvNoItems)

        sortLayout = view.findViewById(R.id.sortLayout)

        svCulture = view.findViewById(R.id.svCulture)
        svField = view.findViewById(R.id.svField)
        svDate = view.findViewById(R.id.svDate)
        svStatus = view.findViewById(R.id.svStatus)

        sowingsRecyclerView.layoutManager = LinearLayoutManager(context)
        sowingsRecyclerView.setHasFixedSize(true)

        sowingsArrayList = arrayListOf<Sowings>()
        sowingsAdapter = SowingsAdapter(sowingsArrayList)
        sowingsRecyclerView.adapter = sowingsAdapter

        getSowingsData()

        addBtn.setOnClickListener{
            val sowing = Sowings(null,
                null,
                null,
                null,
                null,
                null)
            val action = SowingsFragmentDirections.actionSowingsFragmentToSowingsDetailFragment(sowing)
            findNavController().navigate(action)
        }

        sowingsAdapter.setOnItemClickListener(object : SowingsAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                val item = sowingsArrayList[position]
                val sowing = Sowings(item.docId,
                    item.culture,
                    item.field,
                    item.count,
                    item.date,
                    item.status)
                val action = SowingsFragmentDirections.actionSowingsFragmentToSowingsDetailFragment(sowing)
                findNavController().navigate(action)
            }
        })

        showMenuButtons()
    }

    private fun loadSortLayout(){
        val cultureNamesList: MutableList<String?> = ArrayList()
        cultureNamesList.add("Все культуры")
        for (harvest in sowingsArrayList) {
            val cultureName = harvest.culture?.get("cultureName")
            if (!cultureNamesList.contains(cultureName)) {
                cultureNamesList.add(cultureName);
            }
        }
        val adapterC = ArrayAdapter(requireContext(), R.layout.spinner_item, cultureNamesList)
        adapterC.setDropDownViewResource(R.layout.spinner_dropdown_item)
        svCulture.setAdapter(adapterC)
        svCulture.setText("Все культуры", false)

        val fieldNamesList: MutableList<String?> = ArrayList()
        fieldNamesList.add("Все поля")
        for (harvest in sowingsArrayList) {
            val fieldName = harvest.field?.get("name")
            if (!fieldNamesList.contains(fieldName)) {
                fieldNamesList.add(fieldName);
            }
        }
        val adapterF = ArrayAdapter(requireContext(), R.layout.spinner_item, fieldNamesList)
        adapterF.setDropDownViewResource(R.layout.spinner_dropdown_item)
        svField.setAdapter(adapterF)
        svField.setText("Все поля", false)


        val yearList: MutableList<String?> = ArrayList()
        yearList.add("Все года")
        for (document in sowingsArrayList) {
            val date = document.date
            val year = date?.substring(date.length - 4)
            if (!yearList.contains(year)) {
                yearList.add(year);
            }
        }
        val adapterD = ArrayAdapter(requireContext(), R.layout.spinner_item, yearList)
        adapterD.setDropDownViewResource(R.layout.spinner_dropdown_item)
        svDate.setAdapter(adapterD)
        svDate.setText("Все года", false)


        val adapter = ArrayAdapter.createFromResource(requireContext(), R.array.sowingStatus, R.layout.spinner_item)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        svStatus.setAdapter(adapter)
        svStatus.setText("Все записи", false)
    }

    private fun showMenuButtons(){
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.search_menu, menu)
                val searchItem: MenuItem = menu.findItem(R.id.searchBar)
                val searchView: SearchView = searchItem.actionView as SearchView
                svCulture.setOnItemClickListener { parent, view, position, id ->
                    filteredList(searchView.query.toString())
                }
                svField.setOnItemClickListener { parent, view, position, id ->
                    filteredList(searchView.query.toString())
                }
                svDate.setOnItemClickListener { parent, view, position, id ->
                    filteredList(searchView.query.toString())
                }
                svStatus.setOnItemClickListener { parent, view, position, id ->
                    filteredList(searchView.query.toString())
                }

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
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
                        svCulture.setText("Все культуры",false)
                        svField.setText("Все поля",false)
                        svDate.setText("Все года",false)
                        svStatus.setText("Все записи",false)
                        filteredList("")
                    }
                }
                return true
            }
        }, viewLifecycleOwner)
    }

    private fun getSowingsData() {
        db = FirebaseFirestore.getInstance()
        db.collection("Sowings").addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if(error != null){
                    Log.e("Firestore Error",error.message.toString())
                }
                for(dc: DocumentChange in value?.documentChanges!!){
                    if(dc.type == DocumentChange.Type.ADDED){
                        val sowing = dc.document.toObject(Sowings::class.java)
                        sowing.docId = dc.document.id
                        sowingsArrayList.add(sowing)
                    }
                }
                sowingsAdapter.notifyDataSetChanged()
                noItems()
                loadSortLayout()
            }
        })
    }

    fun filteredList(query: String?){
        var filteredList = ArrayList<Sowings>()
        if(query != null){
            for(i in sowingsArrayList){
                if(i.culture?.get("cultureName")?.lowercase(Locale.ROOT)!!.contains(query.lowercase())
                    || i.field?.get("name")?.lowercase(Locale.ROOT)!!.contains(query.lowercase())
                    || i.date?.lowercase(Locale.ROOT)!!.contains(query.lowercase()))
                {
                    filteredList.add(i)
                }
            }
        }

        if(!svCulture.text.contains("Все культуры")){
            filteredList = filteredList.filter { t -> t.culture?.get("cultureName")!!.contains(svCulture.text) } as ArrayList<Sowings>
        }
        if(!svField.text.contains("Все поля")){
            filteredList = filteredList.filter { t -> t.field?.get("name")!!.contains(svField.text) } as ArrayList<Sowings>
        }
        if(!svDate.text.contains("Все года")){
            filteredList = filteredList.filter { t -> t.date!!.substring(t.date!!.length - 4).contains(svDate.text) } as ArrayList<Sowings>
        }
        if(svStatus.text.contains("Засеян")){
            filteredList = filteredList.filter { t -> t.status!! } as ArrayList<Sowings>
        }
        if(svStatus.text.contains("Завершён")){
            filteredList = filteredList.filter { t -> !t.status!! } as ArrayList<Sowings>
        }


        if(filteredList.isEmpty()){
            filteredList.clear()
        }
        sowingsAdapter.setFilteredList(filteredList)
        noItems()
    }

    private fun noItems(){
        if(sowingsAdapter.itemCount == 0){
            tvNoItems.text = "Нет записей"
        }
        else{
            tvNoItems.text = ""
        }
    }
}