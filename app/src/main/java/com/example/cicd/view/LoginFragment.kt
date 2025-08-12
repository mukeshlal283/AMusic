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
import com.example.cicd.databinding.FragmentLoginBinding
import com.example.cicd.utils.Constant.getName
import com.example.cicd.view.activity.MainActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val auth = Firebase.auth
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(layoutInflater)

        binding.noHaveAccountBtn.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.loginBtn.setOnClickListener {

            val email = binding.userEmail.text.toString()
            val password = binding.userPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val currentUserId = auth.currentUser!!.uid
                        getNameFromdb(currentUserId)
                    }
                } .addOnFailureListener {
                    Toast.makeText(requireContext(), "${it.localizedMessage}", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(requireContext(), "Login Successfully!! $getName", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), MainActivity::class.java))
            requireActivity().finish()
        }
    }

}