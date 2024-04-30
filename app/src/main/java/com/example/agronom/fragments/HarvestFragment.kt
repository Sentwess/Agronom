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
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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

        harvestAdapter.setOnItemClickListener(object : HarvestAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val harvest = harvestArrayList[position]
                openDialog(harvest)
            }
        })
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