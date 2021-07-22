package com.minsap.tooldata

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.firestore.FirebaseFirestore
import com.minsap.tooldata.databinding.ActivityAddAudioBinding
import com.minsap.tooldata.databinding.ActivityMainBinding
import timber.log.Timber

class AddAudioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddAudioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAudioBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val db = FirebaseFirestore.getInstance()

        val city = hashMapOf(
            "name" to "Los Angeles",
            "state" to "CA",
            "country" to "USA"
        )

        db.collection("audio").document().set(city)
            .addOnSuccessListener {
                Timber.d("DocumentSnapshot successfully written")
            }
            .addOnFailureListener {
                Timber.d("Error: $it")
            }

    }
}