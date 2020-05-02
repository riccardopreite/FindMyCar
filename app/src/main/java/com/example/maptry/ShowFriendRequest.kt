package com.example.maptry

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.net.URL
import java.net.URLEncoder
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
class ShowFriendRequest : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        //create connection

        val drawerLayout: FrameLayout = findViewById(R.id.drawer_layout)
        val listLayout: FrameLayout = findViewById(R.id.list_layout)
        val homeLayout: FrameLayout = findViewById(R.id.homeframe)
        val splashLayout: FrameLayout = findViewById(R.id.splashFrame)
        val listFriendLayout: FrameLayout = findViewById(R.id.friend_layout)
        val friendLayout: FrameLayout = findViewById(R.id.friendFrame)
        drawerLayout.invalidate()
        listLayout.invalidate()
        homeLayout.invalidate()
        splashLayout.invalidate()
        listFriendLayout.invalidate()

        drawerLayout.visibility = View.GONE
        listLayout.visibility = View.GONE
        homeLayout.visibility = View.GONE
        splashLayout.visibility = View.GONE
        listFriendLayout.visibility = View.GONE

        friendLayout.bringToFront()
        friendLayout.visibility = View.VISIBLE
        var extras = intent?.extras
        var sender = extras?.get("sender") as String
        var receiver = extras?.get("receiver") as String
        println("SENDER IN ON CLICK")
        println(sender)
        var buttonAccept:Button = findViewById(R.id.acceptFriendRequest)
        var friendTextView:TextView = findViewById(R.id.friendRequestText)
        friendTextView.text = sender + " ti ha inviato una richiesta di amicizia!"
        var buttonDecline:Button = findViewById(R.id.cancelFriendRequest)
        buttonAccept.setOnClickListener {
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
            drawerLayout.invalidate()
            listLayout.invalidate()
            friendLayout.invalidate()
            splashLayout.invalidate()
            listFriendLayout.invalidate()

            drawerLayout.visibility = View.GONE
            listLayout.visibility = View.GONE
            friendLayout.visibility = View.GONE
            splashLayout.visibility = View.GONE
            listFriendLayout.visibility = View.GONE
            homeLayout.bringToFront()
            homeLayout.visibility = View.VISIBLE
            finish()
        }
        buttonDecline.setOnClickListener {
            drawerLayout.invalidate()
            listLayout.invalidate()
            friendLayout.invalidate()
            splashLayout.invalidate()
            listFriendLayout.invalidate()

            drawerLayout.visibility = View.GONE
            listLayout.visibility = View.GONE
            friendLayout.visibility = View.GONE
            splashLayout.visibility = View.GONE
            listFriendLayout.visibility = View.GONE
            homeLayout.bringToFront()
            homeLayout.visibility = View.VISIBLE
            finish()
        }

    }
}