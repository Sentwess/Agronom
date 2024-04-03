package com.example.agronom.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.agronom.R
import com.example.agronom.data.Cultures
import com.example.agronom.databinding.FragmentCultureDetailBinding
import com.google.android.gms.tasks.Task
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
    private lateinit var binding: FragmentCultureDetailBinding
    private lateinit var tvName : EditText
    private lateinit var tvVarienty : EditText
    private lateinit var tvBoardingMonth : EditText
    private lateinit var tvGrowingSeason : EditText
    private lateinit var imageView : ImageView
    private lateinit var delImage : ImageButton
    private lateinit var addImage : ImageButton
    private lateinit var editBtn : Button
    private lateinit var deleteBtn : Button
    private lateinit var saveBtn : Button
    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null
    private var firebaseStore: FirebaseStorage? = null
    private var storageReference: StorageReference? = null
    private var imageLast: String? = null
    lateinit var culture : Cultures

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCultureDetailBinding.inflate(inflater, container, false)
        return binding.root
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

        editBtn = view.findViewById(R.id.editBtn)
        deleteBtn = view.findViewById(R.id.deleteBtn)
        saveBtn = view.findViewById(R.id.saveBtn)

        imageView = view.findViewById(R.id.imageView)
        addImage = view.findViewById(R.id.addImage)
        delImage = view.findViewById(R.id.delImage)

        addImage.setOnClickListener { launchGallery() }

        delImage.setOnClickListener{
            if(filePath != null){
                filePath = null
            }
            imageLast = culture.imagePath
            culture.imagePath = "https://firebasestorage.googleapis.com/v0/b/agronom-e52c4.appspot.com/o/images%2Fimage%253A1000018888?alt=media&token=b6d1d9d9-37e9-4c52-8a4f-25ef2ab942a5"
            Glide.with(this).load(culture.imagePath).into(imageView)
        }

        editBtn.setOnClickListener { showData(false) }

        deleteBtn.setOnClickListener{
            openDialog()
        }

        saveBtn.setOnClickListener{
            if(filePath != null){
                uploadImage()
            }
            else{
                updateData(culture.imagePath!!)
            }
        }

        if(culture.docId == null){
            deleteBtn.isVisible = false
            editBtn.isVisible = false
            saveBtn.isVisible = true
            Glide.with(this).load(culture.imagePath).into(imageView)
        }
        else{
            deleteBtn.isVisible = true
            editBtn.isVisible = true
            showData(false)
        }
    }

    private fun openDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_custom, null)
        val customDialog = AlertDialog.Builder(view?.context)
            .setView(dialogView)
            .show()
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
                val bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, filePath)
                imageView.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun deleteData(){
        db = FirebaseFirestore.getInstance()
        db.collection("Cultures").document(culture.docId.toString()).delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Данные удалены", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Ошибка", Toast.LENGTH_SHORT).show()
            }
        findNavController().navigate(CultureDetailFragmentDirections.actionCultureDetailFragmentToCulturesFragment())
    }

    private fun uploadImage(){
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

    private fun updateData(uri : String){
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
        if(culture.docId != null) {
            db.collection("Cultures").document(culture.docId.toString()).update(updates)
                .addOnSuccessListener {
                    Toast.makeText(context, "Данные обновлены", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Ошибка", Toast.LENGTH_SHORT).show()
                }
        }
        else{
            culture.docId = UUID.randomUUID().toString()
            db.collection("Cultures").document(culture.docId!!).set(updates)
                .addOnSuccessListener {
                    Toast.makeText(context, "Данные сохранены", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Ошибка", Toast.LENGTH_SHORT).show()
                }
        }
        showData(true)
    }

    private fun showData(save : Boolean){
        if(save){
            tvName.setInputType(InputType.TYPE_NULL)
            tvVarienty.setInputType(InputType.TYPE_NULL)
            tvBoardingMonth.setInputType(InputType.TYPE_NULL)
            tvGrowingSeason.setInputType(InputType.TYPE_NULL)
            addImage.isClickable = false
            delImage.isClickable = false
            editBtn.text = "Редактировать"
            deleteBtn.isVisible = true
            editBtn.isVisible = true
            saveBtn.isVisible = false

            tvName.setText(culture.cultureName)
            tvVarienty.setText(culture.varienty)
            tvBoardingMonth.setText(culture.boardingMonth)
            tvGrowingSeason.setText(culture.growingSeason)
        }
        else{
            if(!saveBtn.isVisible) {
                tvName.setInputType(InputType.TYPE_CLASS_TEXT)
                tvVarienty.setInputType(InputType.TYPE_CLASS_TEXT)
                tvBoardingMonth.setInputType(InputType.TYPE_CLASS_TEXT)
                tvGrowingSeason.setInputType(InputType.TYPE_CLASS_TEXT)
                addImage.isClickable = true
                delImage.isClickable = true
                editBtn.text = "Отменить"
                saveBtn.isVisible = true
            }
            else{
                tvName.setInputType(InputType.TYPE_NULL)
                tvVarienty.setInputType(InputType.TYPE_NULL)
                tvBoardingMonth.setInputType(InputType.TYPE_NULL)
                tvGrowingSeason.setInputType(InputType.TYPE_NULL)
                addImage.isClickable = false
                delImage.isClickable = false
                editBtn.text = "Редактировать"
                saveBtn.isVisible = false

                culture.imagePath = imageLast
                Glide.with(this).load(culture.imagePath).into(imageView)
                tvName.setText(culture.cultureName)
                tvVarienty.setText(culture.varienty)
                tvBoardingMonth.setText(culture.boardingMonth)
                tvGrowingSeason.setText(culture.growingSeason)
            }
        }
    }
}