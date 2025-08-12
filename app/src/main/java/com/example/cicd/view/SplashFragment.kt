package com.example.cicd.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.cicd.R
import com.example.cicd.utils.Constant.getName
import com.example.cicd.view.activity.MainActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class SplashFragment : Fragment() {

    private val auth = Firebase.auth
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Handler(Looper.getMainLooper()).postDelayed({

            val currentUser = auth.currentUser
            if (currentUser != null) {
                getNameFromDb(currentUser.uid)
                startActivity(Intent(requireContext(), MainActivity::class.java))
                requireActivity().finish()
            } else {
                findNavController().navigate(R.id.action_splashFragment_to_registerFragment)
            }

        }, 4000)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    private fun getNameFromDb(currentUserId: String) {
        firestore.collection("users").document(currentUserId).get().addOnSuccessListener { document ->
            getName = document.getString("name")
        }
    }

}