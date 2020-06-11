package com.example.maptry

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.location.Address
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.example.maptry.MapsActivity
import androidx.appcompat.app.AppCompatActivity
import com.example.maptry.MapsActivity.Companion.account
import com.example.maptry.MapsActivity.Companion.alertDialog
import com.example.maptry.MapsActivity.Companion.context
import com.example.maptry.MapsActivity.Companion.geocoder
import com.example.maptry.MapsActivity.Companion.listAddr
import com.example.maptry.MapsActivity.Companion.myCar
import com.example.maptry.NotifyService.Companion.jsonNotifIdExpired
import com.example.maptry.NotifyService.Companion.jsonNotifIdRemind
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import java.io.IOException
import java.net.URL
import java.net.URLEncoder
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Exception

@SuppressLint("Registered")
class DeleteTimer : AppCompatActivity() {
    var name = ""
    var owner = ""
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val notificationManager : NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val extras = intent?.extras
        owner = extras?.get("owner") as String
        name = extras.get("name") as String

        listAddr = geocoder.getFromLocationName(extras.get("address") as String, 1)
        val location = (listAddr)?.get(0);
        val p0 = location?.latitude?.let { LatLng(it, location.longitude) }

        println("NGIALOSTIU")
        println(myCar)
        println(name)

        for (i in myCar.keys()){
            println(myCar.getJSONObject(i).get("name"))

            if(myCar.getJSONObject(i).get("name") as String == name){
                myCar.remove(i)
                break
            }
        }
        val notificaionId = jsonNotifIdExpired.get(owner)
       try{
           val notificaionId2 = jsonNotifIdRemind.get(owner)
           notificationManager.cancel(notificaionId2 as Int)

       }
       catch (e:Exception){

       }
        notificationManager.cancel(notificaionId as Int)
        finish()
    }
}