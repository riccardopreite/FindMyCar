package com.example.maptry

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.example.maptry.MapsActivity
import androidx.appcompat.app.AppCompatActivity
import com.example.maptry.MapsActivity.Companion.account
import com.example.maptry.MapsActivity.Companion.alertDialog
import com.example.maptry.MapsActivity.Companion.context
import com.example.maptry.MapsActivity.Companion.myCar
import com.google.android.material.snackbar.Snackbar
import java.io.IOException
import java.net.URL
import java.net.URLEncoder
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
class RemindTimer : AppCompatActivity() {
    var name = ""
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val extras = intent?.extras
        name = extras?.get("name") as String

        val close = findViewById<ImageView>(R.id.close_car)
        close.setOnClickListener {
            println("HELLOOOOOOO")
            val drawerLayout: FrameLayout = findViewById(R.id.drawer_layout)
            val listLayout: FrameLayout = findViewById(R.id.list_layout)
            val homeLayout: FrameLayout = findViewById(R.id.homeframe)
            val splashLayout: FrameLayout = findViewById(R.id.splashFrame)
            val listFriendLayout: FrameLayout = findViewById(R.id.friend_layout)
            val friendLayout: FrameLayout = findViewById(R.id.friendFrame)
            val carLayout: FrameLayout = findViewById(R.id.car_layout)
            drawerLayout.invalidate()
            listLayout.invalidate()
            carLayout.invalidate()
            splashLayout.invalidate()
            listFriendLayout.invalidate()
            friendLayout.invalidate()

            friendLayout.visibility = View.GONE
            drawerLayout.visibility = View.GONE
            listLayout.visibility = View.GONE
            carLayout.visibility = View.GONE
            splashLayout.visibility = View.GONE
            listFriendLayout.visibility = View.GONE

            homeLayout.bringToFront()
            homeLayout.visibility = View.VISIBLE
        }
        showCar()
        finish()
    }
    private fun showCar(){

        val inflater: LayoutInflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_car_view, null)

        var txtName :TextView = dialogView.findViewById(R.id.car_name_txt)
        var address : TextView = dialogView.findViewById(R.id.carAddressValue)
        var timer : TimePicker = dialogView.findViewById(R.id.timePickerView)
        var remindButton : Button = dialogView.findViewById(R.id.remindButton)
        var key = ""
        val id = account?.email?.replace("@gmail.com","")


        remindButton.setOnClickListener {
            myCar.getJSONObject(key).put("timer",timer.hour*60 + timer.minute)
            println(myCar.getJSONObject(key))
            alertDialog.dismiss()
            reminderAuto(myCar.getJSONObject(key))

        }
        for (i in myCar.keys()){

            if(myCar.getJSONObject(i).get("name") as String == name){
                key = i
                txtName.text = name
                address.text = myCar.getJSONObject(i).get("address") as String
                val time = (myCar.getJSONObject(i).get("timer").toString()).toInt()
                val hour = time/60
                val minute = time - hour*60
                timer.setIs24HourView(true)
                timer.hour = hour
                timer.minute = minute
                val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
                dialogBuilder.setOnDismissListener(object : DialogInterface.OnDismissListener {
                    override fun onDismiss(arg0: DialogInterface) { }
                })
                dialogBuilder.setView(dialogView)
                try{
                    alertDialog.dismiss()
                }
                catch(e:Exception){
                    println("Exception")
                    println(e)

                }

                alertDialog = dialogBuilder.create();
                runOnUiThread(Runnable {
                    alertDialog.show()
                })



            }
        }
    }

}