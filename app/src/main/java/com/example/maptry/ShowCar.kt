package com.example.maptry
//DO SAME THING OF SHOW FRIEND REQUEST FOR SHOW CAR AND RIMANDA
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
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
import com.example.maptry.MapsActivity.Companion.myCar
import com.google.android.material.snackbar.Snackbar
import java.io.IOException
import java.net.URL
import java.net.URLEncoder
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
class ShowCar : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        //create connection
        //refactor to car layout
        val drawerLayout: FrameLayout = findViewById(R.id.drawer_layout)
        val listLayout: FrameLayout = findViewById(R.id.list_layout)
        val homeLayout: FrameLayout = findViewById(R.id.homeframe)
        val splashLayout: FrameLayout = findViewById(R.id.splashFrame)
        val listFriendLayout: FrameLayout = findViewById(R.id.friend_layout)
        val friendLayout: FrameLayout = findViewById(R.id.friendFrame)
        val carLayout: FrameLayout = findViewById(R.id.car_layout)
        val friendRequestLayout: FrameLayout = findViewById(R.id.friend_layout)
        friendRequestLayout.invalidate()
        splashLayout.invalidate()

        friendRequestLayout.visibility = View.GONE
        splashLayout.visibility = View.GONE
        switchFrame(carLayout,friendLayout,listLayout,homeLayout,drawerLayout)

        var close = findViewById<ImageView>(R.id.close_car)
        close.setOnClickListener {

            friendRequestLayout.invalidate()
            splashLayout.invalidate()

            friendRequestLayout.visibility = View.GONE
            splashLayout.visibility = View.GONE

            switchFrame(homeLayout,carLayout,friendLayout,listLayout,drawerLayout)

            finish()
        }

        showCar()



    }
    private fun showCar(){
        val len = myCar.length()
        var index = 0
        var indexFull = 0
        val txt: TextView = findViewById(R.id.nocar)
        val inflater: LayoutInflater = this.layoutInflater
        val id = account?.email?.replace("@gmail.com","")

        val drawerLayout: FrameLayout = findViewById(R.id.drawer_layout)
        val listLayout: FrameLayout = findViewById(R.id.list_layout)
        val homeLayout: FrameLayout = findViewById(R.id.homeframe)
        val friendLayout: FrameLayout = findViewById(R.id.friend_layout)
        val friendRequestLayout: FrameLayout = findViewById(R.id.friendFrame)
        val carLayout: FrameLayout = findViewById(R.id.car_layout)
        friendRequestLayout.invalidate()
        friendRequestLayout.visibility = View.GONE
        switchFrame(carLayout,friendLayout,listLayout,homeLayout,drawerLayout)


        var  lv: ListView = findViewById<ListView>(R.id.lvCar)
        val carList = MutableList<String>(len,{""})
        val carListFull = MutableList<String>(len*4,{""})
        if(len == 0) txt.visibility = View.VISIBLE;
        else txt.visibility = View.INVISIBLE;
        println("CAR LIST")
        println(myCar)
        println(myCar.length())
        for (i in myCar.keys()){
            println(i)
            println(myCar.getJSONObject(i))
            carList[index] = myCar.getJSONObject(i).get("name") as String
            index++
            for (x in myCar.getJSONObject(i).keys()) {
                carListFull[indexFull] = myCar.getJSONObject(i).get("name") as String
                indexFull++

            }
        }
        println(carList)

        var  arrayAdapter : ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, carList)
        lv.setOnItemLongClickListener { parent, view, position, id ->

            val inflater: LayoutInflater = this.layoutInflater
            val dialogView: View = inflater.inflate(R.layout.dialog_custom_eliminate, null)
            println("LONGCLICK")
            val eliminateBtn: Button = dialogView.findViewById(R.id.eliminateBtn)
            eliminateBtn.setOnClickListener {

                val selectedItem = parent.getItemAtPosition(position) as String

                for(i in myCar.keys()){
                    if(selectedItem == myCar.getJSONObject(i).get("name") as String) {
                        var removed = myCar.getJSONObject(i)
                        myCar.remove(i)
                        var key = i
                        var AC:String
                        AC = "Annulla"
                        var text = "Rimosso "+selectedItem
                        var id = account?.email?.replace("@gmail.com","")
                        val snackbar = Snackbar.make(view, text, 2000)
                            .setAction(AC,View.OnClickListener {

                                id?.let { it1 ->
                                    myCar.put(key,removed)
                                    Toast.makeText(this,"undo" + selectedItem.toString(), Toast.LENGTH_LONG)
                                    showCar()

                                }
                            })

                        snackbar.setActionTextColor(Color.DKGRAY)
                        val snackbarView = snackbar.view
                        snackbarView.setBackgroundColor(Color.BLACK)
                        snackbar.show()
                        if (id != null) {
                            showCar()
                            alertDialog.dismiss()
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

            alertDialog = dialogBuilder.create();
            alertDialog.show()


            return@setOnItemLongClickListener true
        }


        lv.setOnItemClickListener { parent, view, position, id ->
            val inflater: LayoutInflater = this.layoutInflater
            val dialogView: View = inflater.inflate(R.layout.dialog_car_view, null)
            var txtName :TextView = dialogView.findViewById(R.id.car_name_txt)
            var address : TextView = dialogView.findViewById(R.id.carAddressValue)
            var timer : TimePicker = dialogView.findViewById(R.id.timePickerView)
            var remindButton : Button = dialogView.findViewById(R.id.remindButton)
            var key = ""
            val selectedItem = parent.getItemAtPosition(position) as String

            var context = this
            txtName.text = selectedItem
            for (i in myCar.keys()){
                println(i)
                println(myCar.getJSONObject(i))
                if(myCar.getJSONObject(i).get("name") as String == selectedItem){
                    key = i
                    address.text = myCar.getJSONObject(i).get("address") as String
                    var time = (myCar.getJSONObject(i).get("timer").toString()).toInt()
                    var hour = time/60
                    var minute = time - hour*60
                    println("timeee")
                    println(time)
                    println(hour)
                    println(minute)
                    timer.setIs24HourView(true)
                    timer.hour = hour
                    timer.minute = minute
                }
            }

            remindButton.setOnClickListener {
                myCar.getJSONObject(key).put("timer",timer.hour*60 + timer.minute)
                println(myCar.getJSONObject(key))
                alertDialog.dismiss()
                reminderAuto(myCar.getJSONObject(key))

            }

            val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
            dialogBuilder.setOnDismissListener(object : DialogInterface.OnDismissListener {
                override fun onDismiss(arg0: DialogInterface) { }
            })
            dialogBuilder.setView(dialogView)

            alertDialog = dialogBuilder.create();
            alertDialog.show()


            //show friend
        }
        lv.adapter = arrayAdapter;
    }


}