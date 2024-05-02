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
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.agronom.R
import com.example.agronom.adapters.HarvestAdapter
import com.example.agronom.data.Harvest
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class HarvestFragment : Fragment() {

    private lateinit var db : FirebaseFirestore
    private lateinit var  harvestRecyclerView : RecyclerView
    private lateinit var harvestAdapter : HarvestAdapter
    private lateinit var  harvestArrayList : ArrayList<Harvest>
    private lateinit var tvNoItems : TextView
    private lateinit var svCulture : AutoCompleteTextView
    private lateinit var svField : AutoCompleteTextView
    private lateinit var svDate : AutoCompleteTextView
    private lateinit var svCount : AutoCompleteTextView
    private lateinit var sortLayout : LinearLayout
    var isSortMode = false

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

        sortLayout = view.findViewById(R.id.sortLayout)

        svCulture = view.findViewById(R.id.svCulture)
        svField = view.findViewById(R.id.svField)
        svDate = view.findViewById(R.id.svDate)
        svCount = view.findViewById(R.id.svCount)

        tvNoItems = view.findViewById(R.id.tvNoItems)

        harvestArrayList = arrayListOf<Harvest>()
        harvestAdapter = HarvestAdapter(harvestArrayList)
        harvestRecyclerView.adapter = harvestAdapter

        getHarvestData()
        showMenuButtons()

        harvestAdapter.setOnItemClickListener(object : HarvestAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val harvest = harvestArrayList[position]
                openDialog(harvest)
            }
        })
    }

    private fun loadSortLayout(){
        val cultureNamesList: MutableList<String?> = ArrayList()
        cultureNamesList.add("Все культуры")
        for (harvest in harvestArrayList) {
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
        for (harvest in harvestArrayList) {
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
        for (document in harvestArrayList) {
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

        val adapter = ArrayAdapter.createFromResource(requireContext(), R.array.count, R.layout.spinner_item)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        svCount.setAdapter(adapter)
        svCount.setText("Без фильтра", false)
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
                svCount.setOnItemClickListener { parent, view, position, id ->
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
                        svCount.setText("Без фильтра",false)
                        filteredList("")
                    }
                }
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
            noItems()
            loadSortLayout()
        }
    }

    fun filteredList(query: String?){
        var filteredList = ArrayList<Harvest>()
        if(query != null){
            for(i in harvestArrayList){
                if(i.culture?.get("cultureName")?.lowercase(Locale.ROOT)!!.contains(query.lowercase())
                    || i.field?.get("name")?.lowercase(Locale.ROOT)!!.contains(query.lowercase())
                    || i.count?.toString()?.lowercase(Locale.ROOT)!!.contains(query.lowercase())
                    || i.date?.lowercase(Locale.ROOT)!!.contains(query.lowercase()))
                {
                    filteredList.add(i)
                }
            }
        }

        if(!svCulture.text.contains("Все культуры")){
            filteredList = filteredList.filter { t -> t.culture?.get("cultureName")!!.contains(svCulture.text) } as ArrayList<Harvest>
        }
        if(!svField.text.contains("Все поля")){
            filteredList = filteredList.filter { t -> t.field?.get("name")!!.contains(svField.text) } as ArrayList<Harvest>
        }
        if(!svDate.text.contains("Все года")){
            filteredList = filteredList.filter { t -> t.date!!.substring(t.date!!.length - 4).contains(svDate.text) } as ArrayList<Harvest>
        }
        if(svCount.text.contains("По возрастанию")){
            filteredList.sortBy { t -> t.count }
        }
        if(svCount.text.contains("По убыванию")){
            filteredList.sortByDescending { t -> t.count }
        }

        if(filteredList.isEmpty()){
            filteredList.clear()
        }
        harvestAdapter.setFilteredList(filteredList)
        noItems()
    }

    private fun noItems(){
        if(harvestAdapter.itemCount == 0){
            tvNoItems.text = "Нет записей"
        }
        else{
            tvNoItems.text = ""
        }
    }

    private fun openDialog(harvest: Harvest) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_harvest, null)
        val customDialog = AlertDialog.Builder(view?.context).setView(dialogView).show()
        customDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_border)
        val btClose = dialogView.findViewById<Button>(R.id.closeBtn)
        val btDelete = dialogView.findViewById<Button>(R.id.deleteBtn)

        val imageView = dialogView.findViewById<ImageView>(R.id.imageView)

        val fieldName = dialogView.findViewById<GridLayout>(R.id.fieldName)
        val cultureName = dialogView.findViewById<GridLayout>(R.id.cultureName)
        val sowingName = dialogView.findViewById<GridLayout>(R.id.sowingName)

        val fieldLayout = dialogView.findViewById<LinearLayout>(R.id.fieldLayout)
        val cultureLayout = dialogView.findViewById<LinearLayout>(R.id.cultureLayout)
        val sowingLayout = dialogView.findViewById<LinearLayout>(R.id.sowingLayout)

        val dropField = dialogView.findViewById<ImageButton>(R.id.dropField)
        val dropCulture = dialogView.findViewById<ImageButton>(R.id.dropCulture)
        val dropSowing = dialogView.findViewById<ImageButton>(R.id.dropSowing)

        val tvField = dialogView.findViewById<TextView>(R.id.tvField)
        val tvSize = dialogView.findViewById<TextView>(R.id.tvSize)

        val tvCulture = dialogView.findViewById<TextView>(R.id.tvCulture)
        val tvVarienty = dialogView.findViewById<TextView>(R.id.tvVarienty)
        val tvboardingMonth = dialogView.findViewById<TextView>(R.id.tvboardingMonth)
        val tvgrowingSeason = dialogView.findViewById<TextView>(R.id.tvgrowingSeason)

        val tvCountSowing = dialogView.findViewById<TextView>(R.id.tvCountSowing)
        val tvDateStart = dialogView.findViewById<TextView>(R.id.tvDateStart)

        val tvCountHarvest = dialogView.findViewById<TextView>(R.id.tvCountHarvest)
        val tvDateEnd = dialogView.findViewById<TextView>(R.id.tvDateEnd)

        Glide.with(requireContext()).load(harvest.culture!!["imagePath"]).into(imageView)

        tvField.text = harvest.field!!["name"]
        tvSize.text = harvest.field!!["size"] + " Га"

        tvCulture.text = harvest.culture!!["cultureName"]
        tvVarienty.text = harvest.culture!!["varienty"]
        tvboardingMonth.text = harvest.culture!!["boardingMonth"]
        tvgrowingSeason.text = harvest.culture!!["growingSeason"]

        tvCountSowing.text = harvest.sowing!!["count"].toString() + " т"
        tvDateStart.text = harvest.sowing!!["date"].toString()

        tvCountHarvest.text = harvest.count.toString() + " т"
        tvDateEnd.text = harvest.date.toString()


        fieldName.setOnClickListener {
            showMoreInfo(dropField,fieldLayout)
        }
        cultureName.setOnClickListener {
            showMoreInfo(dropCulture,cultureLayout)
        }
        sowingName.setOnClickListener {
            showMoreInfo(dropSowing,sowingLayout)
        }

        dropField.setOnClickListener {
            showMoreInfo(dropField,fieldLayout)
        }
        dropCulture.setOnClickListener {
            showMoreInfo(dropCulture,cultureLayout)
        }
        dropSowing.setOnClickListener {
            showMoreInfo(dropSowing,sowingLayout)
        }

        btClose.setOnClickListener {
            customDialog.dismiss()
        }
        btDelete.setOnClickListener {
            openDeleteDialog(harvest, customDialog)
        }
    }

    private fun showMoreInfo(button: View, layout: View){
        if(!layout.isVisible) {
            layout.isVisible = true
            button.setBackgroundResource(R.drawable.arrow_up)
        }
        else{
            layout.isVisible = false
            button.setBackgroundResource(R.drawable.arrow_down)
        }
    }

    private fun openDeleteDialog(harvest: Harvest, dialogEdit: AlertDialog) {
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
            db.collection("Harvests").document(harvest.docId.toString()).delete().addOnSuccessListener {
                Snackbar.make(requireView(), "Данные удалены", Snackbar.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Snackbar.make(requireView(), "Ошибка", Snackbar.LENGTH_SHORT).show()
            }
            customDialog.dismiss()
            dialogEdit.dismiss()
            harvestArrayList.clear()
            getHarvestData()
        }
    }
}