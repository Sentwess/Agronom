package com.example.agronom.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.agronom.R
import com.example.agronom.data.Cultures
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.File
import java.io.IOException
import java.util.UUID


class CultureDetailFragment : Fragment() {
    private val argsCulture : CultureDetailFragmentArgs by navArgs()
    private lateinit var db : FirebaseFirestore
    private lateinit var tvName : EditText
    private lateinit var tvVarienty : EditText
    private lateinit var tvBoardingMonth : EditText
    private lateinit var tvGrowingSeason : EditText
    private lateinit var imageView : ImageView
    private lateinit var delImage : ImageButton
    private lateinit var saveBtn : Button
    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null
    private var firebaseStore: FirebaseStorage? = null
    private var storageReference: StorageReference? = null
    private var imageLast: String? = null
    lateinit var culture : Cultures
    var newCulture : Boolean = true
    var isEditMode = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_culture_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        culture = argsCulture.currentCulture
        imageLast = culture.imagePath
        firebaseStore = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        tvName = view.findViewById(R.id.tvName)
        tvVarienty = view.findViewById(R.id.tvVarienty)
        tvBoardingMonth = view.findViewById(R.id.tvBoardingMonth)
        tvGrowingSeason = view.findViewById(R.id.tvGrowingSeason)

        saveBtn = view.findViewById(R.id.saveBtn)

        if(culture.docId == null) {
            saveBtn.setOnClickListener {
                if (filePath != null) {
                    uploadImage()
                } else {
                    updateData(culture.imagePath!!)
                }
            }
        }

        imageView = view.findViewById(R.id.imageView)
        delImage = view.findViewById(R.id.delImage)

        imageView.setOnClickListener { launchGallery() }

        delImage.setOnClickListener{
            if(filePath != null){
                filePath = null
            }
            imageLast = culture.imagePath
            culture.imagePath = "https://firebasestorage.googleapis.com/v0/b/agronom-e52c4.appspot.com/o/images%2FCover.png?alt=media&token=073bfc13-a9fb-4f43-8479-2bec4079d4d7"
            Glide.with(this).load(culture.imagePath).into(imageView)
        }

        if(culture.docId != null){
            saveBtn.isVisible = false
            newCulture = false
            changeInputType(false)
            buttonsClickable(false)
            showMenuButtons()
            loadData()
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
                    if(filePath != null){
                        uploadImage()
                    }
                    else{
                        updateData(culture.imagePath!!)
                    }

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
                    if (isEditMode) {
                        menuItem.setIcon(R.drawable.edit_icon)
                    } else {
                        menuItem.setIcon(R.drawable.cancel_ic)
                    }
                    showData(false)
                    isEditMode = !isEditMode
                }
                if (id == R.id.deleteBtn) {
                    openDialog()
                }
                return true
            }
        }, viewLifecycleOwner)
    }

    private fun openDialog() {
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
        db.collection("Cultures").document(culture.docId.toString()).delete()
            .addOnSuccessListener {
                Snackbar.make(requireView(), "Данные удалены", Snackbar.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Snackbar.make(requireView(), "Ошибка", Snackbar.LENGTH_SHORT).show()
            }
        findNavController().navigate(CultureDetailFragmentDirections.actionCultureDetailFragmentToCulturesFragment())
    }

    private fun launchGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if(data == null || data.data == null){
                return
            }

            filePath = data.data
            try {
                Glide.with(this).load(filePath).into(imageView)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImage(){
        println("uploadImage")
        val fileName = File(filePath.toString()).name
        val ref = storageReference?.child("images/$fileName")
            val uploadTask = ref?.putFile(filePath!!)
            val urlTask = uploadTask?.continueWithTask(com.google.android.gms.tasks.Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation ref.downloadUrl
            })?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    filePath = null
                    updateData(task.result.toString())
                } else {
                    // Handle failures
                }
            }?.addOnFailureListener{

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

    private fun updateData(uri : String){
        println("updateData")
        culture.cultureName = tvName.text.toString()
        culture.varienty = tvVarienty.text.toString()
        culture.boardingMonth = tvBoardingMonth.text.toString()
        culture.growingSeason = tvGrowingSeason.text.toString()
        db = FirebaseFirestore.getInstance()
        val updates = mapOf(
            "cultureName" to culture.cultureName,
            "varienty" to culture.varienty,
            "boardingMonth" to culture.boardingMonth,
            "growingSeason" to culture.growingSeason,
            "imagePath" to uri
        )
        val errors = mutableListOf<String>()
        if(tvName.text.isNullOrBlank() || tvName.text.isNullOrEmpty()){
            errors.add("- Название культуры")
        }
        if(tvVarienty.text.isNullOrBlank() || tvVarienty.text.isNullOrEmpty()){
            errors.add("- Сорт культуры")
        }
        if(tvBoardingMonth.text.isNullOrBlank() || tvBoardingMonth.text.isNullOrEmpty()){
            errors.add("- Период высаживания")
        }
        if(errors.size > 0){
            createDialog(errors)
        }
        else {
            if (culture.docId != null) {
                db.collection("Cultures").document(culture.docId.toString()).update(updates)
                    .addOnSuccessListener {

                    }
                    .addOnFailureListener {

                    }


                db.collection("Sowings")
                    .whereEqualTo("culture.docId", culture.docId)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            val docId = document.id
                            val data = mapOf(
                                "culture.cultureName" to culture.cultureName,
                                "culture.varienty" to culture.varienty,
                                "culture.imagePath" to uri
                            )

                            db.collection("Sowings").document(docId)
                                .update(data)
                                .addOnSuccessListener {

                                }
                                .addOnFailureListener { e ->

                                }
                        }
                    }
                    .addOnFailureListener { e ->

                    }
            } else {
                culture.docId = UUID.randomUUID().toString()
                db.collection("Cultures").document(culture.docId!!).set(updates)
                    .addOnSuccessListener {

                    }
                    .addOnFailureListener {

                    }
            }
            showData(true)
        }
    }

    private fun showData(save : Boolean){
        if(save){
            loadData()
            changeInputType(false)
            buttonsClickable(false)
            if(isEditMode){
                isEditMode = false
            }
            if(newCulture){
                Snackbar.make(requireView(), "Данные сохранены", Snackbar.LENGTH_SHORT).show()
                findNavController().navigate(CultureDetailFragmentDirections.actionCultureDetailFragmentToCulturesFragment())
            }
            else{
                Snackbar.make(requireView(), "Данные обновлены", Snackbar.LENGTH_SHORT).show()
            }
        }
        else{
            if(!saveBtn.isVisible) {
                changeInputType(true)
                buttonsClickable(true)
            }
            else{
                culture.imagePath = imageLast
                loadData()
                changeInputType(false)
                buttonsClickable(false)
            }
        }
    }

    private fun loadData(){
        Glide.with(this).load(culture.imagePath).into(imageView)
        tvName.setText(culture.cultureName)
        tvVarienty.setText(culture.varienty)
        tvBoardingMonth.setText(culture.boardingMonth)
        tvGrowingSeason.setText(culture.growingSeason)
    }
    private fun changeInputType(type:Boolean){
        if(type){
            tvName.setInputType(InputType.TYPE_CLASS_TEXT)
            tvVarienty.setInputType(InputType.TYPE_CLASS_TEXT)
            tvBoardingMonth.setInputType(InputType.TYPE_CLASS_TEXT)
            tvGrowingSeason.setInputType(InputType.TYPE_CLASS_TEXT)
        }
        else{
            tvName.setInputType(InputType.TYPE_NULL)
            tvVarienty.setInputType(InputType.TYPE_NULL)
            tvBoardingMonth.setInputType(InputType.TYPE_NULL)
            tvGrowingSeason.setInputType(InputType.TYPE_NULL)
        }
    }
    private fun buttonsClickable(type:Boolean){
        if(type){
            imageView.isClickable = true
            delImage.isClickable = true
            saveBtn.isVisible = true
        }
        else{
            imageView.isClickable = false
            delImage.isClickable = false
            saveBtn.isVisible = false
        }
    }
}