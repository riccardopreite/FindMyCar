package com.example.maptry

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.maptry.MapsActivity.Companion.account
import com.example.maptry.MapsActivity.Companion.dataFromfirestore
import com.example.maptry.MapsActivity.Companion.db
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.json.JSONObject
import java.lang.Math.abs

class NotificationRequestWorker(private val context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {
    companion object{
        var jsonNotifId = JSONObject()
    }
    var notificationJson = JSONObject()
//    val postListener = object : ValueEventListener {
//        override fun onDataChange(dataSnapshot: DataSnapshot) {
//
//            MapsActivity.dataFromfirebase = dataSnapshot
//            if(dataSnapshot.hasChildren()) {
//                dataSnapshot.children.forEach { child ->
////                    myjson = JSONObject()
//                    child.children.forEach { chi ->
//                        println(chi.key)
//                        println(chi.value)
////                        myjson.put(chi.key,chi.value)
//                    }
////                    var pos : LatLng = LatLng(myjson.getString("lat").toDouble(),myjson.getString("lon").toDouble())
////                    println("CREATEEEEEEEE")
////                    println(pos)
////                    var mark = createMarker(pos)
////                    mymarker.put(pos.toString(),mark)
////                    myList.put(pos.toString(),myjson)
//                }
//            }
//
//        }
//
//        override fun onCancelled(databaseError: DatabaseError) {
//            // Getting Post failed, log a message
//            Log.w("ON CANCELLED", "loadPost:onCancelled", databaseError.toException())
//            // ...
//        }
//    }
    override fun doWork(): Result {
    println("DO WORK")
    var idDB = account?.email?.replace("@gmail.com","")
    if (idDB != null) {
        println("ID NON NULL IN NOTIFICATION")
        //Listner for live marker
        db.collection("user").document(idDB).collection("live")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                if (firebaseFirestoreException != null) {
                    Log.w("TAG", "Listen failed.", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                    notificationJson = JSONObject()
                    dataFromfirestore = querySnapshot.documents

                    Log.d("TAG", "Current data: ${querySnapshot.documents}")
                    println("CIAOOOO in live")
                    querySnapshot.documents.forEach { child ->

                        child.data?.forEach { chi ->
                            println(chi.key)
                            println(chi.value)
                            notificationJson.put(chi.key, chi.value)
                        }
                        db.collection("user").document(idDB).collection("live").document(child.id).delete()

                        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        val notification = NotificationCompat.Builder(context, "first").apply {
                            setContentTitle("Evento Live")
                            setContentText(notificationJson.getString("origin") + ": Ha aggiunto un nuovo POI live!")
                            setSmallIcon(R.drawable.ic_launcher_foreground)

                            priority = NotificationCompat.PRIORITY_DEFAULT
                        }.build()

                        nm.notify(System.currentTimeMillis().toInt(), notification)

                    }
                }
            }
        //Listner for friend Request
        db.collection("user").document(idDB).collection("friendrequest")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                if (firebaseFirestoreException != null) {
                    Log.w("TAG", "Listen failed.", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                    notificationJson = JSONObject()
                    dataFromfirestore = querySnapshot.documents

                    Log.d("TAGnotify", "Current data: ${querySnapshot.documents}")
                    println("CIAOOOOnotify")
                    querySnapshot.documents.forEach { child ->
                        child.data?.forEach { chi ->
                            println(chi.key)
                            println(chi.value)
                            notificationJson.put(chi.key, chi.value)
                        }
                        var notificationId = abs(System.nanoTime().toInt())

                        println("NOTIFICATION IDDDDDD")
                        println(notificationId.toString())
                        db.collection("user").document(idDB).collection("friendrequest").document(child.id).delete()
                        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        jsonNotifId.put(notificationJson.getString("origin"), notificationId)
                        val notification = NotificationCompat.Builder(context, "first").apply {
                            setContentTitle("Richiesta d'amicizia")
                            setContentText(notificationJson.getString("origin") + ": Ti ha inviato una richiesta di amicizia!")

                            setSmallIcon(R.drawable.ic_launcher_foreground)
                            setAutoCancel(true) //collegato a tap notification
                            val notificationClickIntent : Intent=  Intent(context,ShowFriendRequest::class.java)
                            notificationClickIntent.putExtra("sender", notificationJson.getString("origin"))
                            notificationClickIntent.putExtra("receiver", idDB)
                            setContentIntent(PendingIntent.getActivity(context, 0,  notificationClickIntent, 0));
                            priority = NotificationCompat.PRIORITY_DEFAULT

                            val acceptFriendIntent : Intent=  Intent(context,AcceptFriend::class.java)
                            acceptFriendIntent.putExtra("sender", notificationJson.getString("origin"))
                            acceptFriendIntent.putExtra("receiver", idDB);






                            val acceptPendingIntent = PendingIntent.getBroadcast(context,0,acceptFriendIntent,0)

                            addAction(R.drawable.ic_addfriendnotification, "Accetta", acceptPendingIntent )

                            val declineFriendIntent : Intent=  Intent(context,DeclineFriend::class.java)
                            declineFriendIntent.putExtra("sender", notificationJson.getString("origin"))
                            val declinePendingIntent = PendingIntent.getBroadcast(context,999,declineFriendIntent,PendingIntent.FLAG_ONE_SHOT)
                            addAction(R.drawable.ic_closenotification, "Rifiuta", declinePendingIntent )

                        }.build()
                        nm.notify(notificationId, notification)
                        querySnapshot.documents.remove(child)
                    }
                }
            }

    }

        // Indicate whether the task finished successfully with the Result
        return Result.success()
    }
}