package com.example.maptry

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.net.URLEncoder
import androidx.core.content.ContextCompat.getSystemService
import com.example.maptry.MapsActivity.Companion.context
import com.example.maptry.MapsActivity.Companion.isRunning
import com.example.maptry.MapsActivity.Companion.zoom


@SuppressLint("Registered")
class ShowFriendList : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        //create connection
        println("IN SHOW FRIEND LIST")
        val drawerLayout: FrameLayout = findViewById(R.id.drawer_layout)
        val listLayout: FrameLayout = findViewById(R.id.list_layout)
        val homeLayout: FrameLayout = findViewById(R.id.homeframe)
        val splashLayout: FrameLayout = findViewById(R.id.splashFrame)
        val listFriendLayout: FrameLayout = findViewById(R.id.friend_layout)
        val friendLayout: FrameLayout = findViewById(R.id.friendFrame)
        val carLayout: FrameLayout = findViewById(R.id.car_layout)
        var closeDrawer :ImageView = findViewById(R.id.close_listfriend)
        val liveLayout: FrameLayout = findViewById(R.id.live_layout)
        val loginLayout: FrameLayout = findViewById(R.id.login_layout)
        switchFrame(listFriendLayout,homeLayout,drawerLayout,listLayout,splashLayout,friendLayout,carLayout,liveLayout,loginLayout)

        closeDrawer.setOnClickListener {
            switchFrame(homeLayout,listFriendLayout,drawerLayout,listLayout,splashLayout,friendLayout,carLayout,liveLayout,loginLayout)
            if(!isRunning) {
                val main = Intent(context,MapsActivity::class.java)
                zoom = 1
                startActivity(main)

            }
            finish()

        }

        // Return a list of the tasks that are currently running,
        // with the most recent being first and older ones after in order.
        // Taken 1 inside getRunningTasks method means want to take only
        // top activity from stack and forgot the olders.


        // Return a list of the tasks that are currently running,
        // with the most recent being first and older ones after in order.
        // Taken 1 inside getRunningTasks method means want to take only
        // top activity from stack and forgot the olders.


        //finish()
    }

    fun showFriendinActivity(){
        val len = MapsActivity.friendJson.length()
        var index = 0
        val txt: TextView = findViewById(R.id.nofriend)
        val inflater: LayoutInflater = this.layoutInflater
        val id = MapsActivity.account?.email?.replace("@gmail.com","")

        val drawerLayout: FrameLayout = findViewById(R.id.drawer_layout)
        val listLayout: FrameLayout = findViewById(R.id.list_layout)
        val homeLayout: FrameLayout = findViewById(R.id.homeframe)
        val splashLayout: FrameLayout = findViewById(R.id.splashFrame)
        val friendLayout: FrameLayout = findViewById(R.id.friend_layout)
        val friendRequestLayout: FrameLayout = findViewById(R.id.friendFrame)
        val carLayout: FrameLayout = findViewById(R.id.car_layout)
        val liveLayout: FrameLayout = findViewById(R.id.live_layout)
        val loginLayout: FrameLayout = findViewById(R.id.login_layout)
        switchFrame(friendLayout,listLayout,homeLayout,drawerLayout,friendRequestLayout,splashLayout,carLayout,liveLayout,loginLayout)


        var  lv: ListView = findViewById<ListView>(R.id.fv)
        val friendList = MutableList<String>(len,{""})
        if(len == 0) txt.visibility = View.VISIBLE;
        else txt.visibility = View.INVISIBLE;
        println("PRINT FRIEND LIST")
        println(MapsActivity.friendJson)
        for (i in MapsActivity.friendJson.keys()){
            friendList[index] = MapsActivity.friendJson[i] as String
            index++
        }

        var  arrayAdapter : ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, friendList)
        lv.setOnItemLongClickListener { parent, view, position, id ->

            val inflater: LayoutInflater = this.layoutInflater
            val dialogView: View = inflater.inflate(R.layout.dialog_custom_eliminate, null)
            println("LONGCLICK")
            val eliminateBtn: Button = dialogView.findViewById(R.id.eliminateBtn)
            eliminateBtn.setOnClickListener {

                val selectedItem = parent.getItemAtPosition(position) as String

                for(i in MapsActivity.friendJson.keys()){
                    if(selectedItem == MapsActivity.friendJson[i] as String) {
                        var removed = selectedItem
                        MapsActivity.friendJson.remove(i)
                        var key = i
                        var AC:String
                        AC = "Annulla"
                        var text = "Rimosso "+selectedItem
                        var id = MapsActivity.account?.email?.replace("@gmail.com","")
                        val snackbar = Snackbar.make(view, text, 2000)
                            .setAction(AC,View.OnClickListener {

                                id?.let { it1 ->
                                    MapsActivity.friendJson.put(key,removed)
                                    confirmFriend(id,removed)
                                    Toast.makeText(this,"undo" + selectedItem.toString(), Toast.LENGTH_LONG)
                                    showFriendinActivity()

                                }
                            })

                        snackbar.setActionTextColor(Color.DKGRAY)
                        val snackbarView = snackbar.view
                        snackbarView.setBackgroundColor(Color.BLACK)
                        snackbar.show()
                        if (id != null) {
                            removeFriend(id,removed)
                            showFriendinActivity()
                            MapsActivity.alertDialog.dismiss()
                            return@setOnClickListener
                        }
                    }
                }

            }
            val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
            dialogBuilder.setOnDismissListener(object : DialogInterface.OnDismissListener {
                override fun onDismiss(arg0: DialogInterface) { }
            })
            dialogBuilder.setView(dialogView)

            MapsActivity.alertDialog = dialogBuilder.create();
            MapsActivity.alertDialog.show()


            return@setOnItemLongClickListener true
        }


        lv.setOnItemClickListener { parent, view, position, id ->
            val inflater: LayoutInflater = this.layoutInflater
            val dialogView: View = inflater.inflate(R.layout.dialog_friend_view, null)
            var txtName :TextView = dialogView.findViewById(R.id.friendNameTxt)
            var spinner : Spinner = dialogView.findViewById(R.id.planets_spinner_POI)
            val selectedItem = parent.getItemAtPosition(position) as String

            var context = this
            txtName.text = selectedItem
            println("CLICK")
            //   var url = URL("http://192.168.1.80:3000/getPoiFromFriend?"+ URLEncoder.encode("friend", "UTF-8") + "=" + URLEncoder.encode(selectedItem, "UTF-8"))
            var url = URL("http://192.168.1.138:3000/getPoiFromFriend?"+ URLEncoder.encode("friend", "UTF-8") + "=" + URLEncoder.encode(selectedItem, "UTF-8"))
            var result = JSONObject()
            val client = OkHttpClient()
            val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
            dialogBuilder.setOnDismissListener(object : DialogInterface.OnDismissListener {
                override fun onDismiss(arg0: DialogInterface) { }
            })
            dialogBuilder.setView(dialogView)

            var alertDialog2 = dialogBuilder.create();

            val request = Request.Builder()
                .url(url)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    println("something went wrong")
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    println("ON RESPONSEEEEEEE")

                    this@ShowFriendList.runOnUiThread(Runnable {
                        try {
                            var check = 0
                            alertDialog2.show()
                            result = JSONObject(response.body()?.string()!!)
                            val length = result.length()
                            val markerList = MutableList<String>(length,{""})
                            var index = 0
                            for(i in result.keys()){
                                println(result.get(i))
                                markerList[index] = result.getJSONObject(i).get("name") as String
                                index++
                            }
                            var arrayAdapter2: ArrayAdapter<String> = ArrayAdapter<String>(context,R.layout.support_simple_spinner_dropdown_item,markerList)
                            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                                override fun onNothingSelected(parent: AdapterView<*>?) {

                                }

                                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                    if(check == 1){
                                        var key = ""
                                        val selectedMarker =
                                            parent?.getItemAtPosition(position) as String
                                        var lat = 0.0
                                        var lon = 0.0
                                        for (i in result.keys()) {

                                            if (result.getJSONObject(i).get("name") == selectedMarker) {
                                                key = i
                                                lat = result.getJSONObject(i).get("lat").toString()
                                                    .toDouble()
                                                lon = result.getJSONObject(i).get("lon").toString()
                                                    .toDouble()
                                            }

                                        }

                                        var pos: LatLng = LatLng(
                                            lat,
                                            lon
                                        )
                                        //                                // refactor create marker to not call getaddress
                                        var mark = createMarker(pos)
                                        MapsActivity.friendTempPoi.put(pos.toString(), result.getJSONObject(key))
                                        MapsActivity.mMap.moveCamera(
                                            CameraUpdateFactory.newLatLngZoom(
                                                LatLng(
                                                    lat,
                                                    lon
                                                ), 20F
                                            )
                                        )
                                        switchFrame(homeLayout,friendLayout,listLayout,drawerLayout,friendRequestLayout,carLayout,splashLayout,liveLayout,loginLayout)
                                        alertDialog2.dismiss()
                                        showPOIPreferences(pos.toString(),inflater,context,mark!!)
                                    }
                                    else{
                                        check = 1
                                    }
                                }

                            }
                            spinner.adapter = arrayAdapter2;

                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    })



//                    val x:String = response.body()?.string()!!
//                    response.body()!!.close()
//
//                    println(x)
//                    result = JSONObject(x)
//                    println(result)

                }

            })



//            println("CALLING SERVER")
//            var json = getPoiFromFriend(selectedItem)
//            println("SERVER RETURNEDDDDDDD")
//            println(json)


            //show friend
        }
        lv.adapter = arrayAdapter;
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation === Configuration.ORIENTATION_LANDSCAPE) {

            onSaveInstanceState(MapsActivity.newBundy)
        } else if (newConfig.orientation === Configuration.ORIENTATION_PORTRAIT) {

            onSaveInstanceState(MapsActivity.newBundy)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle("newBundy", MapsActivity.newBundy)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.getBundle("newBundy")
    }
}