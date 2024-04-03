package com.example.agronom.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agronom.R
import com.example.agronom.adapters.FieldsAdapter
import com.example.agronom.data.Fields
import com.example.agronom.databinding.FragmentFieldsBinding
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import java.util.Locale

class FieldsFragment : Fragment() {
    private lateinit var db : FirebaseFirestore
    private lateinit var  fieldsRecyclerView : RecyclerView
    private lateinit var fieldsAdapter : FieldsAdapter
    private lateinit var  fieldsArrayList : ArrayList<Fields>
    private lateinit var binding: FragmentFieldsBinding
    private lateinit var searchView : SearchView
    private lateinit var spinnerView : Spinner
    private lateinit var addBtn : ImageButton
    private var isSpinnerInitial = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFieldsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fieldsRecyclerView = view.findViewById(R.id.fieldsList)
        searchView = view.findViewById(R.id.searchView)
        spinnerView = view.findViewById(R.id.spinnerView)
        addBtn = view.findViewById(R.id.addBtn)

        fieldsRecyclerView.layoutManager = LinearLayoutManager(context)
        fieldsRecyclerView.setHasFixedSize(true)

        fieldsArrayList = arrayListOf<Fields>()
        fieldsAdapter = FieldsAdapter(fieldsArrayList)
        fieldsRecyclerView.adapter = fieldsAdapter

        getFieldsData()

        addBtn.setOnClickListener{
            val field = Fields(null,
                null,
                null,
                false)
            val action = FieldsFragmentDirections.actionFieldsFragmentToFieldsDetailFragment(field)
            findNavController().navigate(action)
        }

        fieldsAdapter.setOnItemClickListener(object : FieldsAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                val item = fieldsArrayList[position]
                val field = Fields(item.docId.toString(),
                    item.name.toString(),
                    item.size.toString(),
                    item.status)
                val action = FieldsFragmentDirections.actionFieldsFragmentToFieldsDetailFragment(field)
                findNavController().navigate(action)
            }

        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filteredList(newText)
                return true
            }

        })


        spinnerView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(isSpinnerInitial) {
                    isSpinnerInitial = false;
                    return;
                }
                filteredList(searchView.query.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
    }

    private fun getFieldsData() {
        db = FirebaseFirestore.getInstance()
        db.collection("Fields").addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if(error != null){
                    Log.e("Firestore Error",error.message.toString())
                }
                for(dc: DocumentChange in value?.documentChanges!!){
                    if(dc.type == DocumentChange.Type.ADDED){
                        val field = dc.document.toObject(Fields::class.java)
                        field.docId = dc.document.id
                        fieldsArrayList.add(field)
                    }
                }
                fieldsAdapter.notifyDataSetChanged()
            }
        })
    }

    fun filteredList(query: String?){
        if(query != null){
            val filteredList = ArrayList<Fields>()
            for(i in fieldsArrayList){
                if(i.name?.lowercase(Locale.ROOT)!!.contains(query.lowercase())
                    || i.size?.lowercase(Locale.ROOT)!!.contains(query.lowercase()))
                {
                    filteredList.add(i)
                }
            }

            if(spinnerView.selectedItemPosition == 0){
                filteredList.sortBy { t -> t.docId }
            }
            if(spinnerView.selectedItemPosition == 1){
                filteredList.sortByDescending { t -> t.name }
            }
            if(spinnerView.selectedItemPosition == 2){
                filteredList.sortBy { t -> t.name }
            }


            if(filteredList.isEmpty()){
                filteredList.clear()
                Toast.makeText(context,"Не найдено", Toast.LENGTH_SHORT).show()
            }
            fieldsAdapter.setFilteredList(filteredList)
        }
    }
}