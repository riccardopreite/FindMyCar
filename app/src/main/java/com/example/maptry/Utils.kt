package com.example.maptry

import android.R
import android.app.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.startActivity
import com.example.maptry.MapsActivity.Companion.account
import com.example.maptry.MapsActivity.Companion.addrThread
import com.example.maptry.MapsActivity.Companion.alertDialog
import com.example.maptry.MapsActivity.Companion.friendTempPoi
import com.example.maptry.MapsActivity.Companion.geocoder
import com.example.maptry.MapsActivity.Companion.listAddr
import com.example.maptry.MapsActivity.Companion.mAnimation
import com.example.maptry.MapsActivity.Companion.mMap
import com.example.maptry.MapsActivity.Companion.myList
import com.example.maptry.MapsActivity.Companion.mymarker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import org.json.JSONObject
import java.io.IOException


var jsonNotifId = JSONObject()
var notificationJson = JSONObject()
var newFriendJson = JSONObject()

fun switchFrame(toView: FrameLayout, toGone1: FrameLayout, toGone2: FrameLayout, toGone3: FrameLayout, toGone4: FrameLayout,toGone5: FrameLayout,toGone6: FrameLayout){
    toGone1.invalidate()
    toGone2.invalidate()
    toGone3.invalidate()
    toGone4.invalidate()
    toGone5.invalidate()
    toGone6.invalidate()

    toView.visibility = View.VISIBLE
    toGone1.visibility = View.GONE
    toGone2.visibility = View.GONE
    toGone3.visibility = View.GONE
    toGone4.visibility = View.GONE
    toGone5.visibility = View.GONE
    toGone6.visibility = View.GONE

    toView.startAnimation(mAnimation)
    mAnimation.start()
    toView.bringToFront()
}

fun createMarker(p0: LatLng): Marker? {

    var background = object : Runnable {
        override fun run() {
            try {
                listAddr = geocoder.getFromLocation(p0.latitude, p0.longitude, 1)
                return
            } catch (e: IOException) {
                Log.e("Error", "grpc failed2: " + e.message, e)
                // ... retry again your code that throws the exeception
            }
        }

    }
    addrThread = Thread(background)
    addrThread?.start()
    try {
        addrThread?.join()
    } catch (e:InterruptedException) {
        e.printStackTrace()
    }


    var text = "Indirizzo:" + listAddr?.get(0)?.getAddressLine(0)+"\nGeoLocalita:" +  listAddr?.get(0)?.getLocality() + "\nAdminArea: " + listAddr?.get(0)?.getAdminArea() + "\nCountryName: " + listAddr?.get(0)?.getCountryName()+ "\nPostalCode: " + listAddr?.get(0)?.getPostalCode() + "\nFeatureName: " + listAddr?.get(0)?.getFeatureName();

    var x= mMap.addMarker(
        MarkerOptions()
            .position(p0)
            .title(text)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            .alpha(0.7f)
    )

    mymarker.put(p0.toString(),x)
    return x
}
fun createNotification(context:Context){

    var notificationId = Math.abs(System.nanoTime().toInt())
    var notification : Notification
    val nm =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    var idDB = MapsActivity.account?.email?.replace("@gmail.com","")
    if (idDB != null) {
        println("ID NON NULL IN NOTIFICATION")
        //Listner for live marker
        MapsActivity.db.collection("user").document(idDB).collection("live")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                if (firebaseFirestoreException != null) {
                    Log.w("TAG", "Listen failed.", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                    notificationJson = JSONObject()
                    MapsActivity.dataFromfirestore = querySnapshot.documents

                    Log.d("TAG", "Current data: ${querySnapshot.documents}")
                    println("CIAOOOO in live")
                    querySnapshot.documents.forEach { child ->

                        child.data?.forEach { chi ->
                            println(chi.key)
                            println(chi.value)
                            notificationJson.put(chi.key, chi.value)
                        }
                        MapsActivity.db.collection("user").document(idDB).collection("live").document(child.id).delete()

                        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        val notification = NotificationCompat.Builder(context, "first").apply {
                            setContentTitle("Evento Live")
                            setContentText(notificationJson.getString("origin") + ": Ha aggiunto un nuovo POI live!")
//                            setSmallIcon(R.drawable.ic_launcher_foreground)

                            priority = NotificationCompat.PRIORITY_DEFAULT
                        }.build()

                        nm.notify(System.currentTimeMillis().toInt(), notification)

                    }
                }
            }
        //Listner for friend Request

        MapsActivity.db.collection("user").document(idDB).collection("friendrequest")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                if (firebaseFirestoreException != null) {
                    Log.w("TAG", "Listen failed.", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                    notificationJson = JSONObject()
                    MapsActivity.dataFromfirestore = querySnapshot.documents

                    Log.d("TAGnotify", "Current data: ${querySnapshot.documents}")
                    println("CIAOOOOnotify")
                    querySnapshot.documents.forEach { child ->
                        child.data?.forEach { chi ->
                            println(chi.key)
                            println(chi.value)
                            notificationJson.put(chi.key, chi.value)
                        }
                        val CHANNEL_ID =
                            "my_channel_01"
                        println("PREPARE NOTIFICATION")



                        MapsActivity.db.collection("user").document(idDB).collection("friendrequest")
                            .document(child.id).delete()

                        jsonNotifId.put(notificationJson.getString("origin"), notificationId)
                        notification = NotificationCompat.Builder(context, "first").apply {
                            setContentTitle("Richiesta d'amicizia")
                            setContentText(notificationJson.getString("origin") + ": Ti ha inviato una richiesta di amicizia!")

                            setSmallIcon(R.drawable.ic_dialog_map)
                            setAutoCancel(true) //collegato a tap notification
                            val notificationClickIntent: Intent =
                                Intent(context, ShowFriendRequest::class.java)
                            notificationClickIntent.putExtra(
                                "sender",
                                notificationJson.getString("origin")
                            )
                            notificationClickIntent.putExtra("receiver", idDB)
                            setContentIntent(
                                PendingIntent.getActivity(
                                    context,
                                    0,
                                    notificationClickIntent,
                                    0
                                )
                            );
                            priority = NotificationCompat.PRIORITY_DEFAULT

                            val acceptFriendIntent: Intent =
                                Intent(context, AcceptFriend::class.java)
                            acceptFriendIntent.putExtra(
                                "sender",
                                notificationJson.getString("origin")
                            )
                            acceptFriendIntent.putExtra("receiver", idDB);


                            val acceptPendingIntent =
                                PendingIntent.getBroadcast(context, 0, acceptFriendIntent, 0)

                            addAction(
                                R.drawable.ic_input_add,
                                "Accetta",
                                acceptPendingIntent
                            )

                            val declineFriendIntent: Intent =
                                Intent(context, DeclineFriend::class.java)
                            declineFriendIntent.putExtra(
                                "sender",
                                notificationJson.getString("origin")
                            )
                            val declinePendingIntent = PendingIntent.getBroadcast(
                                context,
                                999,
                                declineFriendIntent,
                                PendingIntent.FLAG_ONE_SHOT
                            )
                            addAction(
                                R.drawable.ic_delete,
                                "Rifiuta",
                                declinePendingIntent
                            )
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                println("create channel")
                                val name: CharSequence = "Channel prova"// The user-visible name of the channel.
                                val importance = NotificationManager.IMPORTANCE_HIGH
                                val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
                                nm.createNotificationChannel(mChannel)
                                setChannelId(CHANNEL_ID)
                            }

                        }.build()
                        nm.notify(notificationId, notification)
                        querySnapshot.documents.remove(child)
                    }
                }
            }

        MapsActivity.db.collection("user").document(idDB).collection("friend")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                if (firebaseFirestoreException != null) {
                    Log.w("TAG", "Listen failed.", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                    notificationJson = JSONObject()
                    MapsActivity.dataFromfirestore = querySnapshot.documents

                    Log.d("TAGnotify", "Current data: ${querySnapshot.documents}")
                    println("CIAOOOOnotify")
                    querySnapshot.documents.forEach { child ->
                        child.data?.forEach { chi ->
                            println(chi.key)
                            println(chi.value)
                            newFriendJson.put(chi.key, chi.value)
                        }

                    }
                    var founded = false
                    var newFriend = ""
                    for (i in newFriendJson.keys()) {
                        for (x in MapsActivity.friendJson.keys()) {
                            if (MapsActivity.friendJson.get(x) == newFriendJson.get(i)) {
                                founded = true
                                break
                            }
                        }
                        if (!founded) {
                            newFriend = newFriendJson.get(i) as String
                            break
                        }
                        founded = false

                    }
                    if(newFriend == ""){
                        val CHANNEL_ID =
                            "my_channel_01"
                        println("PREPARE NOTIFICATION")

                        notification = NotificationCompat.Builder(context, "first").apply {
                            setContentTitle("Nuovo Amico!")
                            setContentText("Tu e " + newFriend + " ora siete Amici!")

                            setSmallIcon(R.drawable.ic_dialog_map)
                            setAutoCancel(true) //collegato a tap notification
                            val notificationClickIntent: Intent =
                                Intent(context, ShowFriendRequest::class.java)
                            setContentIntent(
                                PendingIntent.getActivity(
                                    context,
                                    0,
                                    notificationClickIntent,
                                    0
                                )
                            );
                            priority = NotificationCompat.PRIORITY_DEFAULT

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                println("create channel")
                                val name: CharSequence =
                                    "Channel prova"// The user-visible name of the channel.
                                val importance = NotificationManager.IMPORTANCE_HIGH
                                val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
                                nm.createNotificationChannel(mChannel)
                                setChannelId(CHANNEL_ID)
                            }

                        }.build()
                        nm.notify(notificationId, notification)
                    }
                }
            }

        MapsActivity.db.collection("user").document(idDB).collection("timed")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->



                if (firebaseFirestoreException != null) {
                    Log.w("TAG", "Listen failed.", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                    notificationJson = JSONObject()
                    MapsActivity.dataFromfirestore = querySnapshot.documents
                    var key = ""
                    var json = JSONObject()
                    Log.d("TAGnotify", "Current data: ${querySnapshot.documents}")
                    println("CIAOOOOnotify")
                    querySnapshot.documents.forEach { child ->


                        child.data?.forEach { chi ->

                            println("each child")
                            println(chi.key)
                            println(chi.value)
                            json = JSONObject(chi.value as HashMap<*, *>)
                            key = json.get("owner") as String + json.get("name") as String
                            println("JSON")
                            var name = json.get("name") as String
                            println(json.getString("name"))
                            val CHANNEL_ID =
                                "my_channel_01"
                            println("PREPARE NOTIFICATION")


                            jsonNotifId.put(json.getString("owner"), notificationId)
                            notification = NotificationCompat.Builder(context, "first").apply {
                                setContentTitle("Reminder auto")
                                setContentText("Sta finendo il timer di " + json.getString("name") + ". 5 minuti rimanenti")

                                setSmallIcon(R.drawable.ic_dialog_map)
                                setAutoCancel(true) //collegato a tap notification
                                val notificationClickIntent: Intent =
                                    Intent(context, ShowCar::class.java)
                                notificationClickIntent.putExtra(
                                    "name",
                                    name
                                )
                                setContentIntent(
                                    PendingIntent.getActivity(
                                        context,
                                        99,
                                        notificationClickIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                    )
                                );
                                //intent for open car
                                priority = NotificationCompat.PRIORITY_DEFAULT
                                println("INTENT")
                                println(name)
                                val acceptReminderIntent: Intent =
                                    Intent(context, /*ShowCar::class.java*/RemindTimer::class.java) // change intent
                                acceptReminderIntent.putExtra(
                                    "name",
                                    name
                                )//modify to enter timer or open to set timer


                                val acceptPendingIntent =
                                    PendingIntent.getActivity(context, 0, acceptReminderIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                                addAction(
                                    R.drawable.ic_input_add,
                                    "Rimanda",
                                    acceptPendingIntent
                                )

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    println("create channel")
                                    val name: CharSequence = "Channel prova"// The user-visible name of the channel.
                                    val importance = NotificationManager.IMPORTANCE_HIGH
                                    val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
                                    nm.createNotificationChannel(mChannel)
                                    setChannelId(CHANNEL_ID)
                                }

                            }.build()
                            nm.notify(notificationId, notification)

                            notificationJson.put(key, json)
                        }
                        MapsActivity.db.collection("user").document(idDB).collection("timed").document(child.id).delete()

                    }
                }
            }
        MapsActivity.db.collection("user").document(idDB).collection("prova")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->



                if (firebaseFirestoreException != null) {
                    Log.w("TAG", "Listen failed.", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                    notificationJson = JSONObject()
                    MapsActivity.dataFromfirestore = querySnapshot.documents
                    var key = ""
                    var json = JSONObject()
                    Log.d("TAGnotify", "Current data: ${querySnapshot.documents}")
                    println("CIAOOOOnotify")
                    querySnapshot.documents.forEach { child ->


                        child.data?.forEach { chi ->

                            println("each child")
                            println(chi.key)
                            println(chi.value)

                            val CHANNEL_ID =
                                "my_channel_01"
                            println("PREPARE NOTIFICATION")


                            notification = NotificationCompat.Builder(context, "first").apply {
                                setContentTitle("LIVE PROVA")
                                setContentText("STA ANDANDO")

                                setSmallIcon(R.drawable.ic_dialog_map)
                                setAutoCancel(true) //collegato a tap notification


                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    println("create channel")
                                    val name: CharSequence = "Channel prova"// The user-visible name of the channel.
                                    val importance = NotificationManager.IMPORTANCE_HIGH
                                    val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
                                    nm.createNotificationChannel(mChannel)
                                    setChannelId(CHANNEL_ID)
                                }

                            }.build()
                            nm.notify(notificationId, notification)

                        }

                        MapsActivity.db.collection("user").document(idDB).collection("prova").document(child.id).delete()
                    }
                }

            }
//        MapsActivity.db.collection("user").document(idDB).collection("prova")
//            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
//                println("HELOOOO")
//                val nm =
//                    MapsActivity.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//                val notification = NotificationCompat.Builder(MapsActivity.context, "first").apply {
//                    setContentTitle("Evento Live")
//                    setContentText("da file attivo")
//                    setSmallIcon(com.example.maptry.R.drawable.ic_launcher_foreground)
//
//                    priority = NotificationCompat.PRIORITY_DEFAULT
//                }.build()
//
//                nm.notify(System.currentTimeMillis().toInt(), notification)
//            }
    }

    // Indicate whether the task finished successfully with the Result
//    return ListenableWorker.Result.success()
}

fun showPOIPreferences(p0 : String,inflater:LayoutInflater,context:Context,mark:Marker){

    // add your position control
    val dialogView: View = inflater.inflate(com.example.maptry.R.layout.dialog_custom_friend_poi, null)
    var added = 0
    var address: TextView = dialogView.findViewById(com.example.maptry.R.id.txt_addressattr)
    var phone: TextView = dialogView.findViewById(com.example.maptry.R.id.phone_contentattr)
    var header: TextView = dialogView.findViewById(com.example.maptry.R.id.headerattr)
    var url: TextView = dialogView.findViewById(com.example.maptry.R.id.uri_lblattr)
    var text : String =  friendTempPoi.getJSONObject(p0).get("type") as String+": "+ friendTempPoi.getJSONObject(p0).get("name") as String
    header.text =  text
    address.text = friendTempPoi.getJSONObject(p0).get("addr") as String
    url.text = friendTempPoi.getJSONObject(p0).get("url") as String
    phone.text = friendTempPoi.getJSONObject(p0).get("phone") as String
    val routebutton: Button = dialogView.findViewById(com.example.maptry.R.id.routeBtn)
    val removebutton: Button = dialogView.findViewById(com.example.maptry.R.id.removeBtnattr)
    removebutton.text = "Aggiungi"
    removebutton.setOnClickListener {

        //check same position and? same name
        myList.put(p0,friendTempPoi.getJSONObject(p0))
        added = 1
        mymarker.put(p0,mark)
        alertDialog.dismiss()
    }
        routebutton.setOnClickListener {
            var intent =
                Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + address.text))
            added = 0
            startActivity(context,intent,null)

            alertDialog.dismiss()
        }

    val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
    dialogBuilder.setOnDismissListener(object : DialogInterface.OnDismissListener {
        override fun onDismiss(arg0: DialogInterface) {
            if(added == 0) {
                mymarker.remove(p0)
                mark.remove()
            }
            else writeNewPOI(account?.email?.replace("@gmail.com","") as String, friendTempPoi.getJSONObject(p0).get("name") as String,friendTempPoi.getJSONObject(p0).get("addr") as String,friendTempPoi.getJSONObject(p0).get("cont") as String,friendTempPoi.getJSONObject(p0).get("type") as String,mark,friendTempPoi.getJSONObject(p0).get("url") as String,friendTempPoi.getJSONObject(p0).get("phone") as String)
        }
    })
    dialogBuilder.setView(dialogView)

    alertDialog = dialogBuilder.create();
    alertDialog.show()
}

