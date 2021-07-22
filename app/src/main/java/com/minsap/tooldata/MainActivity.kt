package com.minsap.tooldata

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.minsap.tooldata.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val db = Firebase.firestore
        val sfDocRef = db.collection("album")

        binding.btn1.setOnClickListener {
//            startActivity(Intent(this@MainActivity, AddAudioActivity::class.java))
            sfDocRef.get().addOnCompleteListener {task ->
                val batch = db.batch()
                task.result?.documents?.forEach { document ->
                    val docRef = document.reference
                    val id = UUID.randomUUID()
                    val map = hashMapOf("id" to id)
                    batch.set(docRef, map as Map<String, Any>)
                }
            }


        }
    }
}