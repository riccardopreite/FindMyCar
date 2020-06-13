package com.example.maptry


import android.app.*
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.maptry.MapsActivity.Companion.context
import com.example.maptry.MapsActivity.Companion.isRunning
import com.example.maptry.MapsActivity.Companion.myLive
import org.json.JSONObject


class NotifyService : Service() {
    companion object {
        var jsonNotifIdLive = JSONObject()
        var jsonNotifIdFriendRequest = JSONObject()
        var jsonNotifIdRemind = JSONObject()
        var jsonNotifIdExpired = JSONObject()
    }


/*
    private val TAG = "ServiceExample"


    override fun onCreate() {
        Log.i(TAG, "Service onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var idDB = MapsActivity.account?.email?.replace("@gmail.com", "")
        Log.i(TAG, "ENTRATOOOOOOOOOOO")
        if (idDB != null) {
            MapsActivity.db.collection("user").document(idDB).collection("prova")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    val nm =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val notification = NotificationCompat.Builder(context, "first").apply {
                        setContentTitle("Evento Live")
                        setContentText(": Ha aggiunto un nuovo POI live!")
                        setSmallIcon(R.drawable.ic_launcher_foreground)

                        priority = NotificationCompat.PRIORITY_DEFAULT
                    }.build()

                    nm.notify(System.currentTimeMillis().toInt(), notification)
                }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.i(TAG, "Service onBind")
        return null
    }

    override fun onDestroy() {
        Log.i(TAG, "Service onDestroy")
    }

 */

        lateinit var notificationManager : NotificationManager
        var notificationChannelId : String = ""
        private var wakeLock: PowerManager.WakeLock? = null
        private var isServiceStarted = false

        override fun onBind(intent: Intent): IBinder? {
            println("Some component want to bind with the service")
            // We don't provide binding, so return null
            return null
        }

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            println("onStartCommand executed with startId: $startId")
            if (intent != null) {
                startService()
            } else {
                println(
                    "with a null intent. It has been probably restarted by the system."
                )
            }
            // by returning this we make sure the service is restarted if the system kills the service
            return START_STICKY
        }

        @RequiresApi(Build.VERSION_CODES.N)
        override fun onCreate() {
            super.onCreate()
            println("The service has been created".toUpperCase())
            var notification = this.createNotification()
            startForeground(1, notification)


        }

        override fun onDestroy() {
            super.onDestroy()
            println("The service has been destroyed".toUpperCase())
        }

        private fun startService() {
            val CHANNEL_ID =
                "findMyCarChannel"
            val name: CharSequence =
                "findMyCar"
            if (isServiceStarted) return
            println("Starting the foreground service task")
            isServiceStarted = true
//            setServiceState(this, ServiceState.STARTED)

            // we need this lock so our service gets not affected by Doze Mode
            wakeLock =
                (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                    newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock").apply {
                        acquire()
                    }
                }

            // we're starting a loop in a coroutine
//            GlobalScope.launch(Dispatchers.IO) {
//                while (isServiceStarted) {
//                    launch(Dispatchers.IO) {

                        var notification: Notification
                        val nm =
                            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        var idDB = MapsActivity.account?.email?.replace("@gmail.com", "")
                        if (idDB != null) {
                            //Listner for live marker
                            MapsActivity.db.collection("user").document(idDB).collection("live")
                                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                                    var notificationId = Math.abs(System.nanoTime().toInt())
                                    if (firebaseFirestoreException != null) {
                                        Log.w("TAG", "Listen failed.", firebaseFirestoreException)
                                        return@addSnapshotListener
                                    }

                                    if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                                        notificationJson = JSONObject()
                                        MapsActivity.dataFromfirestore = querySnapshot.documents

                                        Log.d("TAG", "Current data: ${querySnapshot.documents}")
                                        querySnapshot.documents.forEach { child ->
                                            var json = JSONObject()
                                            child.data?.forEach { chi ->
                                                json = JSONObject(chi.value as HashMap<*, *>)

                                                val nm =
                                                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                                val notification =
                                                    NotificationCompat.Builder(context, "first")
                                                        .apply {
                                                            setContentTitle("Evento Live")
                                                            setContentText(
                                                                json.getString(
                                                                    "owner"
                                                                ) + ": Ha aggiunto un nuovo POI live!"
                                                            )
                                                            setSmallIcon(R.drawable.ic_addfriend)
                                                            setAutoCancel(true)
//                            setSmallIcon(R.drawable.ic_launcher_foreground)

                                                                val showLiveEvent: Intent =
                                                                    Intent(
                                                                        context,
                                                                        ShowLiveEvent::class.java
                                                                    )
                                                                showLiveEvent.putExtra(
                                                                    "owner",
                                                                    json.get("owner") as String
//                                                            notificationJson.getString("origin")
                                                                )
                                                                showLiveEvent.putExtra(
                                                                    "address",
                                                                    json.get("address") as String
//                                                            notificationJson.getString("origin")
                                                                )
                                                                showLiveEvent.putExtra(
                                                                    "name",
                                                                    json.get("name") as String
//                                                            notificationJson.getString("origin")
                                                                )
                                                                showLiveEvent.putExtra(
                                                                    "timer",
                                                                    json.get("timer") as String
                                                                )
                                                                setContentIntent(
                                                                    PendingIntent.getActivity(
                                                                        context,
                                                                        87,
                                                                        showLiveEvent,
                                                                        FLAG_UPDATE_CURRENT
                                                                    )
                                                                )

                                                          /*  else{
                                                                val intent = Intent(
                                                                    context,
                                                                    MapsActivity::class.java
                                                                )
                                                                val showLiveEvent =
                                                                    Intent(
                                                                        context,
                                                                        ShowLiveEvent::class.java
                                                                    )
                                                                showLiveEvent.putExtra(
                                                                    "owner",
                                                                    json.get("owner") as String
//                                                            notificationJson.getString("origin")
                                                                )
                                                                showLiveEvent.putExtra(
                                                                    "address",
                                                                    json.get("address") as String
//                                                            notificationJson.getString("origin")
                                                                )
                                                                showLiveEvent.putExtra(
                                                                    "name",
                                                                    json.get("name") as String
//                                                            notificationJson.getString("origin")
                                                                )
                                                                showLiveEvent.putExtra(
                                                                    "timer",
                                                                    json.get("timer") as String
                                                                )

                                                                val stackBuilder =
                                                                    TaskStackBuilder.create(context)
                                                                stackBuilder.addParentStack(
                                                                    MapsActivity::class.java
                                                                )
                                                               // stackBuilder.addNextIntent(
                                                                 //   intent
                                                                //)
                                                                stackBuilder.addNextIntent(
                                                                    showLiveEvent
                                                                )

                                                                val pendingIntent =
                                                                    stackBuilder.getPendingIntent(
                                                                        86,
                                                                        FLAG_UPDATE_CURRENT
                                                                    )
                                                                setContentIntent(pendingIntent)
                                                            }*/


                                                            priority =
                                                                NotificationCompat.PRIORITY_DEFAULT
                                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                                                                val importance =
                                                                    NotificationManager.IMPORTANCE_HIGH
                                                                val mChannel = NotificationChannel(
                                                                    CHANNEL_ID,
                                                                    name,
                                                                    importance
                                                                )
                                                                nm.createNotificationChannel(mChannel)
                                                                setChannelId(CHANNEL_ID)
                                                            }
                                                        }.build()

                                                nm.notify(notificationId, notification)

                                            }
                                            MapsActivity.db.collection("user").document(idDB)
                                                .collection("live").document(child.id).delete()
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

                                        Log.d(
                                            "TAGnotify",
                                            "Current data: ${querySnapshot.documents}"
                                        )
                                        querySnapshot.documents.forEach { child ->
                                            child.data?.forEach {    chi ->
                                                var notificationId = Math.abs(System.nanoTime().toInt())
                                                jsonNotifIdFriendRequest.put(
                                                    chi.value as String,
                                                    notificationId
                                                )
                                                notification =
                                                    NotificationCompat.Builder(context, "first").apply {
                                                        setContentTitle("Richiesta d'amicizia")
//                                                        setContentText(notificationJson.getString("origin") + ": Ti ha inviato una richiesta di amicizia!")
                                                        setContentText(chi.value as String + ": Ti ha inviato una richiesta di amicizia!")

                                                        setSmallIcon(R.drawable.ic_addfriend)
                                                        setAutoCancel(true) //collegato a tap notification


                                                            val notificationClickIntent: Intent =
                                                                Intent(
                                                                    context,
                                                                    ShowFriendRequest::class.java
                                                                )
                                                            notificationClickIntent.putExtra(
                                                                "sender",
                                                                chi.value as String
//                                                            notificationJson.getString("origin")
                                                            )
                                                            notificationClickIntent.putExtra(
                                                                "receiver",
                                                                idDB
                                                            )
                                                            setContentIntent(
                                                                PendingIntent.getActivity(
                                                                    context,
                                                                    88,
                                                                    notificationClickIntent,
                                                                    FLAG_UPDATE_CURRENT
                                                                )
                                                            )

                                                       /* else{
                                                            val intent = Intent(
                                                                context,
                                                                MapsActivity::class.java
                                                            )
                                                            val intentFriendRequest =
                                                                Intent(
                                                                    context,
                                                                    ShowFriendRequest::class.java
                                                                )
                                                            intentFriendRequest.putExtra(
                                                                        "sender",
                                                                        chi.value as String
//                                                            notificationJson.getString("origin")
                                                                    )
                                                            intentFriendRequest.putExtra(
                                                                "receiver",
                                                                idDB
                                                            )

                                                            val stackBuilder =
                                                                TaskStackBuilder.create(context)
                                                            stackBuilder.addParentStack(
                                                                MapsActivity::class.java
                                                            )
                                                            //stackBuilder.addNextIntent(intent)
                                                            stackBuilder.addNextIntent(
                                                                intentFriendRequest
                                                            )

                                                            val pendingIntent =
                                                                stackBuilder.getPendingIntent(
                                                                    89,
                                                                    FLAG_UPDATE_CURRENT
                                                                )
                                                            setContentIntent(pendingIntent)
                                                        }*/


                                                        priority = NotificationCompat.PRIORITY_DEFAULT

                                                        val acceptFriendIntent: Intent =
                                                            Intent(context, AcceptFriend::class.java)
                                                        acceptFriendIntent.putExtra(
                                                            "sender",
                                                            chi.value as String
//                                                            notificationJson.getString("origin")
                                                        )
                                                        acceptFriendIntent.putExtra("receiver", idDB);


                                                        val acceptPendingIntent =
                                                            PendingIntent.getBroadcast(
                                                                context,
                                                                90,
                                                                acceptFriendIntent,
                                                                PendingIntent.FLAG_ONE_SHOT
                                                            )

                                                        addAction(
                                                            R.drawable.ic_add,
                                                            "Accetta",
                                                            acceptPendingIntent
                                                        )

                                                        val declineFriendIntent: Intent =
                                                            Intent(context, DeclineFriend::class.java)
                                                        declineFriendIntent.putExtra(
                                                            "sender",
                                                            chi.value as String
//                                                            notificationJson.getString("origin")
                                                        )
                                                        val declinePendingIntent =
                                                            PendingIntent.getBroadcast(
                                                                context,
                                                                91,
                                                                declineFriendIntent,
                                                                PendingIntent.FLAG_ONE_SHOT
                                                            )
                                                        addAction(
                                                            R.drawable.ic_close,
                                                            "Rifiuta",
                                                            declinePendingIntent
                                                        )
                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                            val importance =
                                                                NotificationManager.IMPORTANCE_HIGH
                                                            val mChannel = NotificationChannel(
                                                                CHANNEL_ID,
                                                                name,
                                                                importance
                                                            )
                                                            nm.createNotificationChannel(mChannel)
                                                            setChannelId(CHANNEL_ID)
                                                        }

                                                    }.build()
                                                nm.notify(notificationId, notification)
                                                MapsActivity.db.collection("user").document(idDB)
                                                    .collection("friendrequest")
                                                    .document(child.id).delete()
                                            }

//                                            querySnapshot.documents.remove(child)
                                        }
                                    }
                                }
                            MapsActivity.db.collection("user").document(idDB).collection("addedfriend")
                                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                                    var notificationId = Math.abs(System.nanoTime().toInt())
                                    if (firebaseFirestoreException != null) {
                                        Log.w("TAG", "Listen failed.", firebaseFirestoreException)
                                        return@addSnapshotListener
                                    }

                                    if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                                        notificationJson = JSONObject()
                                        MapsActivity.dataFromfirestore = querySnapshot.documents

                                        Log.d(
                                            "TAGnotify",
                                            "Current data: ${querySnapshot.documents}"
                                        )
                                        querySnapshot.documents.forEach { child ->
                                            child.data?.forEach { chi ->
                                                        val string = "Tu e " + chi.value + " ora siete Amici!"
                                                        notification =
                                                            NotificationCompat.Builder(context, "first").apply {
                                                                setContentTitle("Nuovo Amico!")
                                                                setContentText(string)

                                                                setSmallIcon(R.drawable.ic_accessibility)
                                                                setAutoCancel(true) //collegato a tap notification

                                                                    val notificationClickIntent: Intent =
                                                                        Intent(
                                                                            context,
                                                                            ShowFriendList::class.java
                                                                        )
                                                                    setContentIntent(
                                                                        PendingIntent.getActivity(
                                                                            context,
                                                                            92,
                                                                            notificationClickIntent,
                                                                            FLAG_UPDATE_CURRENT
                                                                        )
                                                                    )

                                                                /*else{
                                                                    val intent = Intent(
                                                                        context,
                                                                        MapsActivity::class.java
                                                                    )
                                                                    val intentFriendList =
                                                                        Intent(
                                                                            context,
                                                                            ShowFriendList::class.java
                                                                        )
                                                                    val stackBuilder =
                                                                        TaskStackBuilder.create(context)
                                                                    stackBuilder.addParentStack(
                                                                        MapsActivity::class.java
                                                                    )
                                                                    //stackBuilder.addNextIntent(
                                                                     //   intent
                                                                    //)
                                                                    stackBuilder.addNextIntent(
                                                                        intentFriendList
                                                                    )

                                                                    val pendingIntent =
                                                                        stackBuilder.getPendingIntent(
                                                                            93,
                                                                            FLAG_UPDATE_CURRENT
                                                                        )
                                                                    setContentIntent(pendingIntent)
                                                                }*/


                                                                priority = NotificationCompat.PRIORITY_DEFAULT

                                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                                    val importance =
                                                                        NotificationManager.IMPORTANCE_HIGH
                                                                    val mChannel = NotificationChannel(
                                                                        CHANNEL_ID,
                                                                        name,
                                                                        importance
                                                                    )
                                                                    nm.createNotificationChannel(mChannel)
                                                                    setChannelId(CHANNEL_ID)
                                                                }

                                                            }.build()
                                                        nm.notify(notificationId, notification)
//                                                    }
                                                MapsActivity.db.collection("user").document(idDB)
                                                    .collection("addedfriend").document(child.id).delete()
                                                }
                                            }
                                        }
                                    }
//                                }
                            MapsActivity.db.collection("user").document(idDB).collection("timed")
                                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                                    var notificationId = Math.abs(System.nanoTime().toInt())

                                    if (firebaseFirestoreException != null) {
                                        Log.w("TAG", "Listen failed.", firebaseFirestoreException)
                                        return@addSnapshotListener
                                    }

                                    if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                                        notificationJson = JSONObject()
                                        MapsActivity.dataFromfirestore = querySnapshot.documents
                                        var key = ""
                                        var json = JSONObject()
                                        Log.d(
                                            "TAGnotify",
                                            "Current data: ${querySnapshot.documents}"
                                        )
                                        querySnapshot.documents.forEach { child ->


                                            child.data?.forEach { chi ->

                                                json = JSONObject(chi.value as HashMap<*, *>)
                                                key =
                                                    json.get("owner") as String + json.get("name") as String
                                                var name = json.get("name") as String
                                                var owner = json.get("owner") as String

                                                jsonNotifIdRemind.put(
                                                    json.getString("owner"),
                                                    notificationId
                                                )
                                                notification =
                                                    NotificationCompat.Builder(context, "first")
                                                        .apply {
                                                            setContentTitle("Reminder auto")
                                                            setContentText(
                                                                "Sta finendo il timer di " + json.getString(
                                                                    "name"
                                                                ) + ". 5 minuti rimanenti"
                                                            )

                                                            setSmallIcon(R.drawable.ic_car)
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
                                                                        95,
                                                                        notificationClickIntent,
                                                                        FLAG_UPDATE_CURRENT
                                                                    )
                                                                )
                                                           /* else{
                                                                val intent = Intent(
                                                                    context,
                                                                    MapsActivity::class.java
                                                                )
                                                                val intentFriendList =
                                                                    Intent(
                                                                        context,
                                                                        ShowCar::class.java
                                                                    )
                                                                val stackBuilder =
                                                                    TaskStackBuilder.create(context)
                                                                stackBuilder.addParentStack(
                                                                    MapsActivity::class.java
                                                                )
                                                                //stackBuilder.addNextIntent(
                                                                 //   intent
                                                                //)
                                                                stackBuilder.addNextIntent(
                                                                    intentFriendList
                                                                )

                                                                val pendingIntent =
                                                                    stackBuilder.getPendingIntent(
                                                                        94,
                                                                        FLAG_UPDATE_CURRENT
                                                                    )
                                                                setContentIntent(pendingIntent)
                                                            }*/





                                                            //intent for open car
                                                            priority =
                                                                NotificationCompat.PRIORITY_DEFAULT
                                                            val acceptReminderIntent: Intent =
                                                                Intent(
                                                                    context, /*ShowCar::class.java*/
                                                                    RemindTimer::class.java
                                                                ) // change intent
                                                            acceptReminderIntent.putExtra(
                                                                "name",
                                                                name
                                                            )
                                                            acceptReminderIntent.putExtra(
                                                                "owner",
                                                                owner
                                                            )

                                                            val acceptPendingIntent =
                                                                PendingIntent.getActivity(
                                                                    context,
                                                                    0,
                                                                    acceptReminderIntent,
                                                                    PendingIntent.FLAG_UPDATE_CURRENT
                                                                )

                                                            addAction(
                                                                R.drawable.ic_add,
                                                                "Rimanda",
                                                                acceptPendingIntent
                                                            )

                                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                                val importance =
                                                                    NotificationManager.IMPORTANCE_HIGH
                                                                val mChannel = NotificationChannel(
                                                                    CHANNEL_ID,
                                                                    name,
                                                                    importance
                                                                )
                                                                nm.createNotificationChannel(
                                                                    mChannel
                                                                )
                                                                setChannelId(CHANNEL_ID)
                                                            }

                                                        }.build()
                                                nm.notify(notificationId, notification)

                                                notificationJson.put(key, json)
                                            }
                                            MapsActivity.db.collection("user").document(idDB)
                                                .collection("timed").document(child.id).delete()

                                        }
                                    }
                                }
                            MapsActivity.db.collection("user").document(idDB).collection("timedExpired")
                                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                                    var notificationId = Math.abs(System.nanoTime().toInt())

                                    if (firebaseFirestoreException != null) {
                                        Log.w("TAG", "Listen failed.", firebaseFirestoreException)
                                        return@addSnapshotListener
                                    }

                                    if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                                        notificationJson = JSONObject()
                                        MapsActivity.dataFromfirestore = querySnapshot.documents
                                        var key = ""
                                        var json = JSONObject()
                                        Log.d(
                                            "TAGnotify",
                                            "Current data: ${querySnapshot.documents}"
                                        )
                                        querySnapshot.documents.forEach { child ->


                                            child.data?.forEach { chi ->

                                                json = JSONObject(chi.value as HashMap<*, *>)
                                                key =
                                                    json.get("owner") as String + json.get("name") as String
                                                val name = json.get("name") as String
                                                val owner = json.get("owner") as String
                                                val address = json.get("address") as String
                                                //divide jsonNotify for each listner

                                                jsonNotifIdExpired.put(
                                                    json.getString("owner"),
                                                    notificationId
                                                )
                                                notification =
                                                    NotificationCompat.Builder(context, "first")
                                                        .apply {
                                                            setContentTitle("Reminder auto")
                                                            setContentText(
                                                                "E' finito il timer di " + json.getString(
                                                                    "name"
                                                                ) + "."
                                                            )

                                                            setSmallIcon(R.drawable.ic_car)
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
                                                                        FLAG_UPDATE_CURRENT
                                                                    )
                                                                )

                                                            /*else{
                                                                println("IS RUNNING E FALSE")
                                                                val intent = Intent(
                                                                    context,
                                                                    MapsActivity::class.java
                                                                )
                                                                val intentFriendList =
                                                                    Intent(
                                                                        context,
                                                                        ShowCar::class.java
                                                                    )
                                                                val stackBuilder =
                                                                    TaskStackBuilder.create(context)
                                                                stackBuilder.addParentStack(
                                                                    MapsActivity::class.java
                                                                )
                                                              //  stackBuilder.addNextIntent(
                                                                //    intent
                                                               // )
                                                                stackBuilder.addNextIntent(
                                                                    intentFriendList
                                                                )

                                                                val pendingIntent =
                                                                    stackBuilder.getPendingIntent(
                                                                        96,
                                                                        FLAG_UPDATE_CURRENT
                                                                    )
                                                                setContentIntent(pendingIntent)
                                                            }*/





                                                            //intent for open car
                                                            priority =
                                                                NotificationCompat.PRIORITY_DEFAULT
                                                            val acceptReminderIntent: Intent =
                                                                Intent(
                                                                    context, /*ShowCar::class.java*/
                                                                    RemindTimer::class.java
                                                                ) // change intent
                                                            acceptReminderIntent.putExtra(
                                                                "name",
                                                                name
                                                            )
                                                            acceptReminderIntent.putExtra(
                                                                "owner",
                                                                owner
                                                            )

                                                            val acceptPendingIntent =
                                                                PendingIntent.getActivity(
                                                                    context,
                                                                    97,
                                                                    acceptReminderIntent,
                                                                    PendingIntent.FLAG_UPDATE_CURRENT
                                                                )

                                                            addAction(
                                                                R.drawable.ic_add,
                                                                "Rimanda",
                                                                acceptPendingIntent
                                                            )
                                                            val deleteReminderIntent: Intent =
                                                                Intent(
                                                                    context, /*ShowCar::class.java*/
                                                                    DeleteTimer::class.java
                                                                ) // change intent
                                                            deleteReminderIntent.putExtra(
                                                                "name",
                                                                name
                                                            )
                                                            deleteReminderIntent.putExtra(
                                                                "owner",
                                                                owner
                                                            )
                                                            deleteReminderIntent.putExtra(
                                                                "address",
                                                                address
                                                            )

                                                            val deletePendingIntent =
                                                                PendingIntent.getActivity(
                                                                    context,
                                                                    98,
                                                                    deleteReminderIntent,
                                                                    FLAG_UPDATE_CURRENT
                                                                )

                                                            addAction(
                                                                R.drawable.ic_closenotification,
                                                                "Elimina",
                                                                deletePendingIntent
                                                            )

                                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                                val importance =
                                                                    NotificationManager.IMPORTANCE_HIGH
                                                                val mChannel = NotificationChannel(
                                                                    CHANNEL_ID,
                                                                    name,
                                                                    importance
                                                                )
                                                                nm.createNotificationChannel(
                                                                    mChannel
                                                                )
                                                                setChannelId(CHANNEL_ID)
                                                            }

                                                        }.build()
                                                nm.notify(notificationId, notification)

                                                notificationJson.put(key, json)
                                            }
                                            MapsActivity.db.collection("user").document(idDB)
                                                .collection("timedExpired").document(child.id).delete()

                                        }
                                    }
                                }

                        }
        }

            private fun stopService() {
                println("Stopping the foreground service")
                try {
                    wakeLock?.let {
                        if (it.isHeld) {
                            it.release()
                        }
                    }
                    stopForeground(true)
                    stopSelf()
                } catch (e: Exception) {
                    println("Service stopped without being started: ${e.message}")
                }
                isServiceStarted = false
//            setServiceState(this, ServiceState.STOPPED)
            }

            private fun createNotification(): Notification {
                notificationChannelId = "ENDLESS SERVICE CHANNEL"

                // depending on the Android API that we're dealing with we will have
                // to use a specific method to create the notification
                notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {



                    val channel = NotificationChannel(
                        notificationChannelId,
                        "Endless Service notifications channel",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).let {
                        it.description = "Endless Service channel"
                        it.enableLights(true)
                        it.lightColor = Color.RED
                        it.enableVibration(true)
                        it.vibrationPattern =
                            longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                        it
                    }
                    notificationManager.createNotificationChannel(channel)
                }

//            val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
//                PendingIntent.getActivity(this, 0, notificationIntent, 0)
//            }

                val builder: Notification.Builder =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
                        this,
                        notificationChannelId
                    ) else Notification.Builder(this)
                var notification =builder
                    .setContentTitle("Servizio Live")
                    .setContentText("Rimaniamo in ascolto per tenerti sempre aggiornato!")
                    .setSmallIcon(R.drawable.ic_location)
                    .build()
                return  notification
            }


}