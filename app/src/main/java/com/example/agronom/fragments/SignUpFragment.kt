package com.example.agronom.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.agronom.R
import com.example.agronom.databinding.FragmentSignUpBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * A simple [Fragment] subclass.
 * Use the [SignUpFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SignUpFragment : Fragment() {
    private lateinit var navController: NavController
    private lateinit var mAuth: FirebaseAuth
    private lateinit var binding: FragmentSignUpBinding
    private lateinit var db : FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)

        binding.textViewSignIn.setOnClickListener {
            navController.navigate(SignUpFragmentDirections.actionSignUpFragmentToSignInFragment())
        }

        binding.nextBtn.setOnClickListener {
            val fio = binding.fioEt.text.toString()
            val phone = binding.phoneEt.text.toString()
            val email = binding.emailEt.text.toString()
            val pass = binding.passEt.text.toString()
            val verifyPass = binding.verifyPassEt.text.toString()

            val errors = mutableListOf<String>()
            if(fio.isBlank() || fio.isEmpty()){
                errors.add("- Фамилия и Имя")
            }
            if(phone.isBlank() || phone.isEmpty()){
                errors.add("- Телефон")
            }
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                errors.add("- Email")
            }
            if(pass.isBlank() || pass.isEmpty()){
                errors.add("- Пароль")
            }
            if(verifyPass.isBlank() || verifyPass.isEmpty()){
                errors.add("- Пароль повторно")
            }
            if(errors.size > 0){
                createDialog(errors)
            }
            else{
                if(pass.length > 5) {
                    if (pass == verifyPass) {
                        registerUser(fio, phone, email, pass)
                    } else {
                        errors.add("- Пароли не совпадают")
                        createDialog(errors)
                    }
                }
                else{
                    errors.add("- Минимальная длина пароля 6 символов")
                    createDialog(errors)
                }
            }
        }

    }

    private fun registerUser(fio: String, phone: String, email: String, pass: String) {
        db = FirebaseFirestore.getInstance()
        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {task ->
            if (task.isSuccessful) {
                val user = mapOf(
                    "fio" to fio,
                    "phone" to phone,
                    "email" to email,
                    "user uid" to task.result.user?.uid
                )
                db.collection("Users").document(task.result.user?.uid.toString()).set(user)
                    .addOnSuccessListener {
                    }
                    .addOnFailureListener {
                    }
                navController.navigate(SignUpFragmentDirections.actionSignUpFragmentToSignInFragment())
                mAuth.signOut()
                Snackbar.make(requireView(), "Аккаунт зарегистрирован", Toast.LENGTH_SHORT).show()
            }
            else {
                Snackbar.make(requireView(), "Не удалось зарегистрироваться", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun init(view: View) {
        navController = Navigation.findNavController(view)
        mAuth = FirebaseAuth.getInstance()
    }

    private fun createDialog(messages: MutableList<String>) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_message, null)
        val customDialog = AlertDialog.Builder(view?.context)
            .setView(dialogView)
            .show()
        customDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_border)
        val textView = dialogView.findViewById<TextView>(R.id.tvMessage)
        val textViewTitle = dialogView.findViewById<TextView>(R.id.tvTitle)
        textViewTitle.text = "Заполните следующие данные:"
        val listAsString = messages.joinToString("\n")
        textView.text = listAsString
        val okBtn = dialogView.findViewById<Button>(R.id.okBtn)
        okBtn.setOnClickListener {
            customDialog.dismiss()
        }
    }

}