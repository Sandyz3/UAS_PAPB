package com.example.uas_papb.fragments

import android.annotation.SuppressLint
import android.net.ConnectivityManager
import android.net.Network
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uas_papb.DataListAdapter
import com.example.uas_papb.data.ControllerDB
import com.example.uas_papb.data.Item
import com.example.uas_papb.databinding.FragmentAdminHomeBinding
import com.example.uas_papb.tool.AddOn.isNetworkAvailable
import com.example.uas_papb.tool.NetworkMonitor
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage

class AdminHomeFragment : Fragment() {
    private val firestore = FirebaseFirestore.getInstance()
    private val budgetCollectionRef = firestore.collection("movie")
    private val budgetListLiveData: MutableLiveData<List<Item>> by lazy { MutableLiveData<List<Item>>()}
    private lateinit var binding: FragmentAdminHomeBinding
    private lateinit var storageRef: StorageReference
    private lateinit var localdb: ControllerDB
    private lateinit var networkMonitor: NetworkMonitor
    private var imageUris: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        localdb = ControllerDB.getDatabase(requireContext())
        networkMonitor = NetworkMonitor(requireContext())
        networkMonitor.registerNetworkCallBack(networkCallback)
        storageRef = Firebase.storage.reference
        with(binding) {
            floatAddBtn.setOnClickListener {
                contentView.visibility = View.GONE
                contentPanel.visibility = View.VISIBLE
                floatAddBtn.visibility = View.GONE
            }

            edtImage.setOnClickListener {
                galleryLauncher.launch("image/*")
            }

            btnAdd.setOnClickListener {
                val name = edtName.text.toString()
                val desc = edtDesc.text.toString()
                val author = edtAuthor.text.toString()
                val tag = edtTag.text.toString()
                val rating = edtRating.text.toString().toDouble()
                val newItem = Item(name = name, storyline = desc,
                    author = author, image = imageUris.toString(), tag = tag, rating = rating, bookmark = "false")
                addBudget(newItem)
                contentView.visibility = View.VISIBLE
                contentPanel.visibility = View.GONE
                floatAddBtn.visibility = View.VISIBLE
            }
        }
        observeBudgets()
        getAllBudgets()
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            Toast.makeText(requireContext(), "Internet on sync local database with firestore", Toast.LENGTH_SHORT).show()
        }

        override fun onLost(network: Network) {
            Toast.makeText(requireContext(), "Internet off use local Room Database", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        try{
            val sd = getFileName(uri!!)
            storageRef.child("file/$sd.jpg").putFile(uri).addOnSuccessListener { _ ->
                storageRef.child("file/$sd.jpg").downloadUrl.addOnSuccessListener {url ->
                    val photoUri: String?
                    photoUri = url.toString()
                    imageUris = Uri.parse(photoUri)
                }
            }
            binding.edtImage.setImageURI(uri)
        }catch(e:Exception){
            e.printStackTrace()
        }
    }

    private fun getAllBudgets() {
        if(isNetworkAvailable(requireContext())) {
            observeBudgetChanges()
        } else {
            localdb.ItemDao()!!.allNotes.observe(viewLifecycleOwner) {
                budgetListLiveData.postValue(it)
            }
        }
    }

    private fun observeBudgets() {
        budgetListLiveData.observe(viewLifecycleOwner) { budgets ->
            val adapter = DataListAdapter(budgets)
            binding.listView.adapter = adapter
            binding.listView.layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeBudgetChanges() {
        if(!isNetworkAvailable(requireContext())) {
            return
        }
        budgetCollectionRef.addSnapshotListener { snapshots, error ->
            if (error != null) {
                Log.d("MainActivity", "Error listening for budget changes: ", error)
                return@addSnapshotListener
            }
            val items = snapshots?.toObjects(Item::class.java)
            if (items != null) {
                budgetListLiveData.postValue(items)
                for (i in items) {
                    Thread {
                        if (localdb.ItemDao()!!.selectById(i.id) != null) {
                            localdb.ItemDao()!!.insert(i)
                        }
                    }
                }
            }
        }
    }

    private fun addBudget(item: Item) {
        if(isNetworkAvailable(requireContext())) {
            budgetCollectionRef.add(item)
                .addOnSuccessListener { documentReference ->
                    val createdBudgetId = documentReference.id
                    item.id = createdBudgetId
                    documentReference.set(item)
                        .addOnFailureListener {
                            Log.d("MainActivity", "Error updating budget ID: ", it)
                        }
                }
                .addOnFailureListener {
                    Log.d(TAG, "Error adding budget: ", it)
                }
        } else {
            Thread {
                if(localdb.ItemDao()!!.selectById(item.id) != null) {
                    localdb.ItemDao()!!.update(item)
                } else {
                    localdb.ItemDao()!!.insert(item)
                }
            }
        }
    }

    @SuppressLint("Range")
    private fun getFileName(uri: Uri): String? {
        return uri.path?.lastIndexOf('/')?.let { uri.path?.substring(it) }
    }

    companion object {
        private const val TAG = "AdminHomeFragment"
    }
}