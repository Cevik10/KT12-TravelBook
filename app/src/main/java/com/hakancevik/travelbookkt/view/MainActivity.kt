package com.hakancevik.travelbookkt.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.hakancevik.travelbookkt.adapter.PlaceAdapter
import com.hakancevik.travelbookkt.databinding.ActivityMainBinding
import com.hakancevik.travelbookkt.model.Place
import com.hakancevik.travelbookkt.roomdb.PlaceDao
import com.hakancevik.travelbookkt.roomdb.PlaceDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // room database
    private lateinit var db: PlaceDatabase
    private lateinit var placeDao: PlaceDao

    //rxJava
    private val compositeDisposable = CompositeDisposable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        db = Room.databaseBuilder(applicationContext, PlaceDatabase::class.java, "Places")
            .build()

        placeDao = db.placeDao()

        compositeDisposable.add(
            placeDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )


    }

    private fun handleResponse(placeList: List<Place>){
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = PlaceAdapter(placeList)
        binding.recyclerView.adapter = adapter

    }


    fun addPlace(view: View) {
        var intent = Intent(this@MainActivity, MapsActivity::class.java)
        intent.putExtra("info", "new")
        startActivity(intent)
    }


}