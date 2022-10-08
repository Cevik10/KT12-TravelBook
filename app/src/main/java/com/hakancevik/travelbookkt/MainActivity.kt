package com.hakancevik.travelbookkt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.hakancevik.travelbookkt.databinding.ActivityMainBinding
import com.hakancevik.travelbookkt.databinding.ActivityMapsBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


    }


    fun addPlace(view: View) {

        var intent = Intent(this@MainActivity, MapsActivity::class.java)
        intent.putExtra("info", "new")
        startActivity(intent)


    }


}