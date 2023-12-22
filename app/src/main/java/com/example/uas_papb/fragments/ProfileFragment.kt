package com.example.uas_papb.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.uas_papb.DetailAccountActivity
import com.example.uas_papb.SplashActivity
import com.example.uas_papb.data.User
import com.example.uas_papb.databinding.FragmentProfileBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import java.util.concurrent.Executors

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sharefpref: SharedPreferences
    companion object {
        const val SHAREDPREF = "shared_keys"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        firestore = Firebase.firestore
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        auth = Firebase.auth
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharefpref = requireActivity().getSharedPreferences(SHAREDPREF, Context.MODE_PRIVATE)
    }

    override fun onStart() {
        super.onStart()
        if(auth.currentUser != null) binding.username.text = auth.currentUser!!.displayName.toString() else onDestroy()

        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        var image: Bitmap?
        var url: String
        firestore.collection("users").document(auth.currentUser!!.uid).get().addOnSuccessListener {
            url = it.toObject<User>()?.profileImage!!
            executor.execute {
                try {
                    val `in` = java.net.URL(url).openStream()
                    image = BitmapFactory.decodeStream(`in`)
                    handler.post {
                        binding.profileImage.setImageBitmap(image)
                    }
                }
                catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        binding.accountDetail.setOnClickListener {
            startActivity(Intent(requireContext(), DetailAccountActivity::class.java))
        }
        binding.logoutBtn.setOnClickListener {
            val edit = sharefpref.edit()
            edit.clear()
            edit.apply()
            Firebase.auth.signOut()
            startActivity(Intent(requireContext(), SplashActivity::class.java))
            onDestroy()
        }
    }
}