package com.minsap.tooldata

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.minsap.tooldata.databinding.ActivityAddAlbumBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber

class AddAlbumActivity : BaseActivity() {
    private lateinit var binding: ActivityAddAlbumBinding
    private var category = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAlbumBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val db = Firebase.firestore

        val list = mutableListOf<String>()
        val adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, list)

        binding.spinner.adapter = adapter
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                category = list[p2]
                Timber.d("Category: $category")
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        binding.button.setOnClickListener {
            showProgressDialog(this)
            CoroutineScope(Dispatchers.IO).launch {
                addAlbum(db)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            getCategory(db, list, adapter)
        }
    }

    private suspend fun getCategory(
        db: FirebaseFirestore,
        listCategory: MutableList<String>,
        arrayAdapter: ArrayAdapter<String>
    ) {
        val data = db.collection("category")
            .whereNotIn("type", listOf("daily", "quickaction", "meditate", "sound")).get().await()
        data.documents.forEach {
            listCategory.add(it.getString("type") ?: "")
        }
        withContext(Dispatchers.Main) {
            arrayAdapter.notifyDataSetChanged()
        }
    }

    private suspend fun addAlbum(db: FirebaseFirestore) {
        val data = db.collection("category")
            .whereEqualTo("type", category)
            .get().await()
        val categoryId = data.documents.first().getString("id")
        val ref = db.collection("album").document()
        val album = hashMapOf(
            "id" to ref.id,
            "name" to binding.edtName.text.trim().toString(),
            "url" to binding.edtUrl.text.trim().toString(),
            "categoryId" to categoryId
        )
        ref.set(album).addOnSuccessListener {
            dismissProgress()
            Toast.makeText(this@AddAlbumActivity, "Add Success", Toast.LENGTH_LONG)
                .show()
        }.addOnFailureListener {
            dismissProgress()
            Toast.makeText(this@AddAlbumActivity, "Add Failure", Toast.LENGTH_LONG)
                .show()
        }
    }
}