package com.example.maptry

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import com.example.maptry.MapsActivity
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.net.URL
import java.net.URLEncoder
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
class ShowFriendList : AppCompatActivity() {
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
        friendLayout.invalidate()

        drawerLayout.visibility = View.GONE
        listLayout.visibility = View.GONE
        homeLayout.visibility = View.GONE
        splashLayout.visibility = View.GONE
        friendLayout.visibility = View.GONE

        listFriendLayout.bringToFront()
        listFriendLayout.visibility = View.VISIBLE
        finish()
    }

    fun closeDrawerCar(view: View) {}
}