package com.example.agronom.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.SearchView
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agronom.R
import com.example.agronom.adapters.CultureAdapter
import com.example.agronom.data.Cultures
import com.example.agronom.databinding.FragmentCulturesBinding
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import java.util.Locale

/**
 * A simple [Fragment] subclass.
 * Use the [CulturesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CulturesFragment : Fragment() {
    private lateinit var db : FirebaseFirestore
    private lateinit var  cultureRecyclerView : RecyclerView
    private lateinit var cultureAdapter : CultureAdapter
    private lateinit var  cultureArrayList : ArrayList<Cultures>
    private lateinit var binding: FragmentCulturesBinding
    private lateinit var searchView : SearchView
    private lateinit var spinnerView : Spinner
    private var isSpinnerInitial = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cultureRecyclerView = view.findViewById(R.id.culturesList)
        searchView = view.findViewById(R.id.searchView)
        spinnerView = view.findViewById(R.id.spinnerView)

        cultureRecyclerView.layoutManager = LinearLayoutManager(context)
        cultureRecyclerView.setHasFixedSize(true)

        cultureArrayList = arrayListOf<Cultures>()
        cultureAdapter = CultureAdapter(cultureArrayList)
        cultureRecyclerView.adapter = cultureAdapter

        getCulturesData()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filteredList(newText)
                return true
            }

        })

        spinnerView.onItemSelectedListener = object : OnItemSelectedListener{
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

                        // Получаем id документа из другой коллекции
                        val otherCollectionDocumentId = culture.cultureName

                        // Запрашиваем документ из другой коллекции
                        db.collection("CultureName").document(otherCollectionDocumentId.toString())
                            .get()
                            .addOnSuccessListener { otherDocumentSnapshot ->
                                if (otherDocumentSnapshot.exists()) {
                                    val name = otherDocumentSnapshot.getString("Name")
                                    culture.cultureName = name
                                    cultureArrayList.add(culture)
                                    cultureAdapter.notifyDataSetChanged()
                                } else {
                                    Log.e("Firestore Error", "Document not found in OtherCollection")
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore Error", e.message.toString())
                            }
                    }
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCulturesBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun filteredList(query: String?){
        if(query != null){
            val filteredList = ArrayList<Cultures>()
            for(i in cultureArrayList){
                if(i.cultureName?.lowercase(Locale.ROOT)!!.contains(query.lowercase())
                    || i.varienty?.lowercase(Locale.ROOT)!!.contains(query.lowercase())
                    || i.boardingMonth?.lowercase(Locale.ROOT)!!.contains(query.lowercase())
                    || i.growingSeason?.lowercase(Locale.ROOT)!!.contains(query.lowercase()))
                {
                    filteredList.add(i)
                }
            }

            if(spinnerView.selectedItemPosition == 0){
                filteredList.sortBy { t -> t.id }
            }
            if(spinnerView.selectedItemPosition == 1){
                filteredList.sortByDescending { t -> t.cultureName }
            }
            if(spinnerView.selectedItemPosition == 2){
                filteredList.sortBy { t -> t.cultureName }
            }


            if(filteredList.isEmpty()){
                filteredList.clear()
                Toast.makeText(context,"Не найдено",Toast.LENGTH_SHORT).show()
            }
            cultureAdapter.setFilteredList(filteredList)
        }
    }

}