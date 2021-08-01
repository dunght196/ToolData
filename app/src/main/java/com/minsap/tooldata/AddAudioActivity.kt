package com.minsap.tooldata

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.minsap.tooldata.databinding.ActivityAddAudioBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class AddAudioActivity : BaseActivity() {
    private lateinit var binding: ActivityAddAudioBinding
    private var category = ""
    private var album = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_audio)

        binding = ActivityAddAudioBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val db = Firebase.firestore

        val listType = mutableListOf("NotType", "quickaction", "daily")
        val listAlbum = mutableListOf<String>()
        val adapterType =
            ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, listType)
        val adapterAlbum =
            ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, listAlbum)

        binding.typeCategory.adapter = adapterType
        binding.typeCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                category = listType[p2]
                binding.album.isEnabled = category == "NotType"
                binding.imageUrl.isEnabled = category != "NotType"
                Timber.d("Category: $category")
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        binding.album.adapter = adapterAlbum
        binding.album.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                album = listAlbum[p2]
                Timber.d("Album: $album")
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            getAlbums(db, listAlbum, adapterAlbum)
        }

        binding.add.setOnClickListener {
            showProgressDialog(this)
            CoroutineScope(Dispatchers.IO).launch {
                addAudio(db)
            }
        }
    }

    private suspend fun getAlbums(
        db: FirebaseFirestore,
        listAlbum: MutableList<String>,
        arrayAdapter: ArrayAdapter<String>
    ) {
        val data = db.collection("album").get().await()
        data.documents.forEach {
            listAlbum.add(it.getString("name") ?: "")
        }
        withContext(Dispatchers.Main) {
            arrayAdapter.notifyDataSetChanged()
        }
    }

    private suspend fun addAudio(db: FirebaseFirestore) {
        val name = binding.name.text.trim().toString()
        val content = binding.content.text.trim().toString()
        val imageUrl = binding.imageUrl.text.trim().toString()
        val audioUrl = binding.url.text.trim().toString()
        val mp = MediaPlayer.create(this, Uri.parse(audioUrl))
        val duration = mp.duration / 1000L
        val stf = db.collection("audio").document()
        var audio: HashMap<String, Any?> = hashMapOf()
        if (category != "NotType") {
            val category = db.collection("category")
                .whereEqualTo("type", category)
                .get().await()
            val categoryId = category.documents.first().getString("id")
            audio = hashMapOf(
                "id" to stf.id,
                "name" to name,
                "content" to content,
                "imageUrl" to imageUrl,
                "audioUrl" to audioUrl,
                "duration" to duration,
                "categoryId" to categoryId
            )
        } else {
            val album = db.collection("album")
                .whereEqualTo("name", album)
                .get().await()
            val albumId = album.documents.first().getString("id")
            audio = hashMapOf(
                "id" to stf.id,
                "name" to name,
                "content" to content,
                "audioUrl" to audioUrl,
                "duration" to duration,
                "albumId" to albumId
            )

            album.documents.first().apply {
                val totalTime = (this.getLong("totalTime") ?: 0).plus(duration)
                val totalTrack = (this.getLong("totalTrack") ?: 0).plus(1)
                this.reference.update("totalTime", totalTime)
                this.reference.update("totalTrack", totalTrack)
            }
        }

        val set = stf.set(audio)

        withContext(Dispatchers.Main) {
            set.addOnSuccessListener {
                dismissProgress()
                Toast.makeText(this@AddAudioActivity, "Add Success", Toast.LENGTH_LONG)
                    .show()
            }.addOnFailureListener {
                dismissProgress()
                Timber.d("Ex: $it")
            }
        }
    }

}