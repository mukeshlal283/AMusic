package com.example.cicd.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.cicd.R
import com.example.cicd.databinding.FragmentRegisterBinding
import com.example.cicd.model.User
import com.example.cicd.utils.Constant.getName
import com.example.cicd.view.activity.MainActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private val auth = Firebase.auth
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(layoutInflater)

        binding.alreadyHaveAccountBtn.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        binding.registerBtn.setOnClickListener {

            val name = binding.userName.text.toString()
            val email = binding.userEmail.text.toString()
            val password = binding.userPassword.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val currentUserId = auth.currentUser!!.uid
                        val userData = User(currentUserId, name, email, password)
                        firestore.collection("users").document(currentUserId).set(userData).addOnSuccessListener {
                            getNameFromdb(currentUserId)
                        }
                    }
                } .addOnFailureListener {
                    Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Please fill all fields!!", Toast.LENGTH_SHORT).show()
            }

        }

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun getNameFromdb(userId: String) {
        firestore.collection("users").document(userId).get().addOnSuccessListener { document ->
            getName = document.getString("name")
            Toast.makeText(requireContext(), "Registered Successfully!! $getName", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), MainActivity::class.java))
            requireActivity().finish()
        }
    }


}