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
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agronom.R
import com.example.agronom.adapters.CultureAdapter
import com.example.agronom.data.Cultures
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import java.util.Locale


class CulturesFragment : Fragment() {
    private lateinit var db : FirebaseFirestore
    private lateinit var  cultureRecyclerView : RecyclerView
    private lateinit var cultureAdapter : CultureAdapter
    private lateinit var  cultureArrayList : ArrayList<Cultures>
    private lateinit var addBtn : ImageButton
    private lateinit var tvNoItems : TextView
    private lateinit var svCulture : AutoCompleteTextView
    private lateinit var sortLayout : LinearLayout
    var isSortMode = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cultures, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sortLayout = view.findViewById(R.id.sortLayout)
        cultureRecyclerView = view.findViewById(R.id.culturesList)
        addBtn = view.findViewById(R.id.addBtn)
        tvNoItems = view.findViewById(R.id.tvNoItems)
        svCulture = view.findViewById(R.id.svCulture)
        loadSortCulture()

        cultureRecyclerView.layoutManager = LinearLayoutManager(context)
        cultureRecyclerView.setHasFixedSize(true)

        cultureArrayList = arrayListOf<Cultures>()
        cultureAdapter = CultureAdapter(cultureArrayList)
        cultureRecyclerView.adapter = cultureAdapter

        getCulturesData()

        addBtn.setOnClickListener{
            val imagePath = "https://firebasestorage.googleapis.com/v0/b/agronom-e52c4.appspot.com/o/images%2Fwheat.png?alt=media&token=fc45c7d6-abb1-4dbf-b930-73d49202a24d"
            val culture = Cultures(null,
                null,
                null,
                null,
                null,
                imagePath)
            val action = CulturesFragmentDirections.actionCulturesFragmentToCultureDetailFragment(culture)
            findNavController().navigate(action)
        }

        cultureAdapter.setOnItemClickListener(object : CultureAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                val item = cultureArrayList[position]
                val culture = Cultures(item.docId.toString(),
                    item.cultureName.toString(),
                    item.varienty.toString(),
                    item.boardingMonth.toString(),
                    item.growingSeason.toString(),
                    item.imagePath.toString())
                val action = CulturesFragmentDirections.actionCulturesFragmentToCultureDetailFragment(culture)
                findNavController().navigate(action)
            }

        })
        showMenuButtons()
    }

    private fun loadSortCulture(){
        val cultureNamesList: MutableList<String?> = ArrayList()
        cultureNamesList.add("Все культуры")

        db = FirebaseFirestore.getInstance()
        db.collection("Cultures").get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                for (document in queryDocumentSnapshots) {
                    val cultureName = document.getString("cultureName")
                    if (!cultureNamesList.contains(cultureName)) {
                        cultureNamesList.add(cultureName);
                    }
                }
                val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, cultureNamesList)
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                svCulture.setAdapter(adapter)
            }
        svCulture.setText("Все культуры")
    }

    private fun showMenuButtons(){
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.search_menu, menu)
                val searchItem: MenuItem = menu.findItem(R.id.searchBar)
                val searchView: androidx.appcompat.widget.SearchView = searchItem.actionView as androidx.appcompat.widget.SearchView
                svCulture.setOnItemClickListener { parent, view, position, id ->
                    filteredList(searchView.query.toString())
                }

                searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener{
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
                        filteredList("")
                    }
                }
                return true
            }
        }, viewLifecycleOwner)
    }

    private fun getCulturesData() {
        db = FirebaseFirestore.getInstance()
        db.collection("Cultures").addSnapshotListener(object : EventListener<QuerySnapshot>{
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if(error != null){
                    Log.e("Firestore Error",error.message.toString())
                }
                for(dc: DocumentChange in value?.documentChanges!!){
                    if(dc.type == DocumentChange.Type.ADDED){
                        val culture = dc.document.toObject(Cultures::class.java)
                        culture.docId = dc.document.id
                        cultureArrayList.add(culture)
                    }
                }
                cultureAdapter.notifyDataSetChanged()
                noItems()
            }
        })
    }

    fun filteredList(query: String?){
        var filteredList = ArrayList<Cultures>()
        if(query != null){
            for(i in cultureArrayList){
                if(i.cultureName?.lowercase(Locale.ROOT)!!.contains(query.lowercase())
                    || i.varienty?.lowercase(Locale.ROOT)!!.contains(query.lowercase()))
                {
                    filteredList.add(i)
                }
            }
        }

        if(!svCulture.text.contains("Все культуры")){
            filteredList = filteredList.filter { t -> t.cultureName!!.contains(svCulture.text) } as ArrayList<Cultures>
        }

        if(filteredList.isEmpty()){
            filteredList.clear()
        }
        cultureAdapter.setFilteredList(filteredList)
        noItems()
    }

    private fun noItems(){
        if(cultureAdapter.itemCount == 0){
            tvNoItems.text = "Нет записей"
        }
        else{
            tvNoItems.text = ""
        }
    }
}