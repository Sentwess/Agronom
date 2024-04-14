package com.example.agronom.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agronom.R
import com.example.agronom.adapters.HarvestAdapter
import com.example.agronom.data.Harvest
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class HarvestFragment : Fragment() {

    private lateinit var db : FirebaseFirestore
    private lateinit var  harvestRecyclerView : RecyclerView
    private lateinit var harvestAdapter : HarvestAdapter
    private lateinit var  harvestArrayList : ArrayList<Harvest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_harvest, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        harvestRecyclerView = view.findViewById(R.id.harvestList)
        harvestRecyclerView.layoutManager = LinearLayoutManager(context)
        harvestRecyclerView.setHasFixedSize(true)

        harvestArrayList = arrayListOf<Harvest>()
        harvestAdapter = HarvestAdapter(harvestArrayList)
        harvestRecyclerView.adapter = harvestAdapter

        getHarvestData()
        showMenuButtons()
    }

    private fun showMenuButtons(){
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.search_menu, menu)
                val searchItem: MenuItem = menu.findItem(R.id.searchBar)
                val searchView: SearchView = searchItem.actionView as SearchView

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
                return true
            }
        }, viewLifecycleOwner)
    }

    private fun getHarvestData() {
        db = FirebaseFirestore.getInstance()
        db.collection("Harvests").addSnapshotListener { value, error ->
            if (error != null) {
                Log.e("Firestore Error", error.message.toString())
            }
            for (dc: DocumentChange in value?.documentChanges!!) {
                if (dc.type == DocumentChange.Type.ADDED) {
                    val harvest = dc.document.toObject(Harvest::class.java)
                    harvest.docId = dc.document.id
                    harvestArrayList.add(harvest)
                }
            }
            harvestAdapter.notifyDataSetChanged()
        }
    }

    fun filteredList(query: String?){
        if(query != null){
            val filteredList = ArrayList<Harvest>()
            for(i in harvestArrayList){
                if(i.culture?.get("cultureName")?.lowercase(Locale.ROOT)!!.contains(query.lowercase())
                    || i.field?.get("name")?.lowercase(Locale.ROOT)!!.contains(query.lowercase()))
                {
                    filteredList.add(i)
                }
            }

            /*
            if(spinnerView.selectedItemPosition == 0){
                filteredList.sortBy { t -> t.docId }
            }
            if(spinnerView.selectedItemPosition == 1){
                filteredList.sortByDescending { t -> t.name }
            }
            if(spinnerView.selectedItemPosition == 2){
                filteredList.sortBy { t -> t.name }
            }
            */

            if(filteredList.isEmpty()){
                filteredList.clear()
                Toast.makeText(context,"Не найдено", Toast.LENGTH_SHORT).show()
            }
            harvestAdapter.setFilteredList(filteredList)
        }
    }
}