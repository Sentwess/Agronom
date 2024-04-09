package com.example.agronom.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agronom.R
import com.example.agronom.adapters.SowingsAdapter
import com.example.agronom.data.Fields
import com.example.agronom.data.Sowings
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import java.util.Locale

class SowingsFragment : Fragment() {

    private lateinit var db : FirebaseFirestore
    private lateinit var  sowingsRecyclerView : RecyclerView
    private lateinit var sowingsAdapter : SowingsAdapter
    private lateinit var  sowingsArrayList : ArrayList<Sowings>
    private lateinit var addBtn : ImageButton

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

        sowingsRecyclerView.layoutManager = LinearLayoutManager(context)
        sowingsRecyclerView.setHasFixedSize(true)

        sowingsArrayList = arrayListOf<Sowings>()
        sowingsAdapter = SowingsAdapter(sowingsArrayList)
        sowingsRecyclerView.adapter = sowingsAdapter

        getSowingsData()

        addBtn.setOnClickListener{
            val field = Fields(null,
                null,
                null,
                false)
           // val action = FieldsFragmentDirections.actionFieldsFragmentToFieldsDetailFragment(field)
           // findNavController().navigate(action)
        }

        sowingsAdapter.setOnItemClickListener(object : SowingsAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                val item = sowingsArrayList[position]
                val field = Sowings(item.docId.toString(),
                    item.culture,
                    item.field,
                    item.count,
                    item.date,
                    item.status)
                //val action = FieldsFragmentDirections.actionFieldsFragmentToFieldsDetailFragment(field)
                //findNavController().navigate(action)
            }

        })

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
            }
        })
    }

    fun filteredList(query: String?){
        if(query != null){
            val filteredList = ArrayList<Sowings>()
            for(i in sowingsArrayList){
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
            sowingsAdapter.setFilteredList(filteredList)
        }
    }
}