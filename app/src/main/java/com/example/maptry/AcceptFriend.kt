package com.example.maptry

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import com.example.maptry.MapsActivity.Companion.account
import com.google.firebase.auth.FirebaseAuth
import com.squareup.okhttp.*
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.URL
import java.net.URLEncoder

class AcceptFriend : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        var notificationManager :NotificationManager = context?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        var extras = intent?.extras


        var sender = extras?.get("sender")
        var receiver = extras?.get("receiver")
        var notificaionId = NotificationRequestWorker.jsonNotifId.get(sender as String)

        notificationManager.cancel(notificaionId as Int);
        var url = URL("http://192.168.1.80:3000/confirmFriend?"+ URLEncoder.encode("receiver", "UTF-8") + "=" + URLEncoder.encode(receiver.toString(), "UTF-8")+"&"+ URLEncoder.encode("sender", "UTF-8") + "=" + URLEncoder.encode(sender.toString(), "UTF-8"))

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                println("something went wrong")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                println(response.body()?.string())
            }
        })
    }
}