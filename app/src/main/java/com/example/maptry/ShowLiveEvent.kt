package com.example.maptry

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.maptry.MapsActivity.Companion.mMap
import com.example.maptry.MapsActivity.Companion.myList
import com.example.maptry.MapsActivity.Companion.myLive
import com.example.maptry.MapsActivity.Companion.mymarker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import org.json.JSONObject
import java.lang.Exception
import java.util.*
import kotlin.concurrent.schedule


class ShowLiveEvent: AppCompatActivity() {
    var name = ""
    var owner = ""
    var timer = ""
    var address = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val extras = intent?.extras
        var json = JSONObject()

        name = extras?.get("name") as String
        owner = extras.get("owner") as String
        timer = extras.get("timer") as String
        address = extras.get("address") as String
        json.put("name",name)
        json.put("owner",owner)
        json.put("timer",timer)
        json.put("address",address)





        var exp = (timer.toInt() *60*1000).toLong()
        val list = MapsActivity.geocoder.getFromLocationName(address,1)
        val lat = list[0].latitude
        val lon = list[0].longitude
        val p0 = LatLng(lat,lon)
        val mark = createMarker(p0)
        println("ARRIVATOO")

        myLive.put(p0.toString(), json)
        json.put("addr",address)
        json.remove("address")
        json.put("cont", "live")
        json.put("type", "live")
        json.put("marker", mark)
        json.put("url", "da implementare")
        json.put("phone", "da implementare")
        myList.put(p0.toString(), json)

        println(p0)
        println("ARRIVATOO")


        mMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                p0, 20F
            )
        )
        var done = false
        runOnUiThread {
            val mainHandler = Handler(Looper.getMainLooper())
            mainHandler.postDelayed(object : Runnable {
                override fun run() {
                    if(!done) {
                        println("TIME TO REMOVEEEE")
                        println(p0.toString())

                        val mark = mymarker[p0.toString()] as Marker
                        mymarker.remove(p0.toString())
                        mark.remove()

                        println("removed?")
                        myLive.remove(p0.toString())
                        println("myLive")
                        println(myLive)
                        done = true
                    }
                    else mainHandler.postDelayed(this, exp)
                }
            },exp)

           // val x = Timer()
          //  x.schedule(object : TimerTask() {
          //      override fun run() {


            //    }
         //   },exp)
        }

        val drawerLayout: FrameLayout = findViewById(R.id.drawer_layout)
        val listLayout: FrameLayout = findViewById(R.id.list_layout)
        val homeLayout: FrameLayout = findViewById(R.id.homeframe)
        val splashLayout: FrameLayout = findViewById(R.id.splashFrame)
        val friendLayout: FrameLayout = findViewById(R.id.friendFrame)
        val carLayout: FrameLayout = findViewById(R.id.car_layout)
        val friendRequestLayout: FrameLayout = findViewById(R.id.friend_layout)

        switchFrame(homeLayout,friendLayout,listLayout,carLayout,drawerLayout,splashLayout,friendRequestLayout)
        finish()
    }
}