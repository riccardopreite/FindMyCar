package com.example.maptry


import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.maptry.MapsActivity.Companion.context
import org.json.JSONObject


class NotifyService : Service() {
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
            Toast.makeText(this, "Service destroyed", Toast.LENGTH_SHORT).show()
        }

        private fun startService() {
            if (isServiceStarted) return
            println("Starting the foreground service task")
            Toast.makeText(this, "Service starting its task", Toast.LENGTH_SHORT).show()
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
                            println("ID NON NULL IN NOTIFICATION")
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
                                        println("CIAOOOO in live")
                                        querySnapshot.documents.forEach { child ->

                                            child.data?.forEach { chi ->
                                                println(chi.key)
                                                println(chi.value)
                                                notificationJson.put(chi.key, chi.value)
                                            }
                                            MapsActivity.db.collection("user").document(idDB)
                                                .collection("live").document(child.id).delete()

                                            val nm =
                                                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                            val notification =
                                                NotificationCompat.Builder(context, "first").apply {
                                                    setContentTitle("Evento Live")
                                                    setContentText(notificationJson.getString("origin") + ": Ha aggiunto un nuovo POI live!")
//                            setSmallIcon(R.drawable.ic_launcher_foreground)

                                                    priority = NotificationCompat.PRIORITY_DEFAULT
                                                }.build()

                                            nm.notify(
                                                System.currentTimeMillis().toInt(),
                                                notification
                                            )

                                        }
                                    }
                                }
                            //Listner for friend Request
                            MapsActivity.db.collection("user").document(idDB).collection("friendrequest")
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



                                            MapsActivity.db.collection("user").document(idDB)
                                                .collection("friendrequest")
                                                .document(child.id).delete()

                                            jsonNotifId.put(
                                                notificationJson.getString("origin"),
                                                notificationId
                                            )
                                            notification =
                                                NotificationCompat.Builder(context, "first").apply {
                                                    setContentTitle("Richiesta d'amicizia")
                                                    setContentText(notificationJson.getString("origin") + ": Ti ha inviato una richiesta di amicizia!")

                                                    setSmallIcon(R.drawable.ic_addfriend)
                                                    setAutoCancel(true) //collegato a tap notification
                                                    val notificationClickIntent: Intent =
                                                        Intent(
                                                            context,
                                                            ShowFriendRequest::class.java
                                                        )
                                                    notificationClickIntent.putExtra(
                                                        "sender",
                                                        notificationJson.getString("origin")
                                                    )
                                                    notificationClickIntent.putExtra(
                                                        "receiver",
                                                        idDB
                                                    )
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
                                                        PendingIntent.getBroadcast(
                                                            context,
                                                            0,
                                                            acceptFriendIntent,
                                                            0
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
                                                        notificationJson.getString("origin")
                                                    )
                                                    val declinePendingIntent =
                                                        PendingIntent.getBroadcast(
                                                            context,
                                                            999,
                                                            declineFriendIntent,
                                                            PendingIntent.FLAG_ONE_SHOT
                                                        )
                                                    addAction(
                                                        R.drawable.ic_close,
                                                        "Rifiuta",
                                                        declinePendingIntent
                                                    )
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                        println("create channel")
                                                        val name: CharSequence =
                                                            "Channel prova"// The user-visible name of the channel.
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
                                            querySnapshot.documents.remove(child)
                                        }
                                    }
                                }
                            MapsActivity.db.collection("user").document(idDB).collection("friend")
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
                                        println("CIAOOOOnotify")
                                        querySnapshot.documents.forEach { child ->
                                            child.data?.forEach { chi ->
                                                    var founded = false

                                                    println("NUOVO AMICO")
                                                    println(chi.key)
                                                    println(chi.value)

                                                    for (x in MapsActivity.friendJson.keys()) {
                                                        if (MapsActivity.friendJson.get(x) == chi.value
                                                        ) {
                                                            founded = true
                                                            break
                                                        }
                                                    }
                                                    if(!founded){

                                                        println(chi.key)
                                                        println(chi.value)
//                                                newFriendJson.put(chi.key, chi.value)

                                                        val CHANNEL_ID =
                                                            "my_channel_01"
                                                        println("PREPARE NOTIFICATION")
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
                                                                        ShowFriendRequest::class.java
                                                                    )
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
                                        println("CIAOOOOnotify")
                                        querySnapshot.documents.forEach { child ->


                                            child.data?.forEach { chi ->

                                                println("each child")
                                                println(chi.key)
                                                println(chi.value)
                                                json = JSONObject(chi.value as HashMap<*, *>)
                                                key =
                                                    json.get("owner") as String + json.get("name") as String
                                                println("JSON")
                                                var name = json.get("name") as String
                                                println(json.getString("name"))
                                                val CHANNEL_ID =
                                                    "my_channel_01"
                                                println("PREPARE NOTIFICATION")


                                                jsonNotifId.put(
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
                                                                    99,
                                                                    notificationClickIntent,
                                                                    PendingIntent.FLAG_UPDATE_CURRENT
                                                                )
                                                            );
                                                            //intent for open car
                                                            priority =
                                                                NotificationCompat.PRIORITY_DEFAULT
                                                            println("INTENT")
                                                            println(name)
                                                            val acceptReminderIntent: Intent =
                                                                Intent(
                                                                    context, /*ShowCar::class.java*/
                                                                    RemindTimer::class.java
                                                                ) // change intent
                                                            acceptReminderIntent.putExtra(
                                                                "name",
                                                                name
                                                            )//modify to enter timer or open to set timer


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
                                                                println("create channel")
                                                                val name: CharSequence =
                                                                    "Channel prova"// The user-visible name of the channel.
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
                            MapsActivity.db.collection("user").document(idDB).collection("prova")
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
                                        println("CIAOOOOnotify")
                                        querySnapshot.documents.forEach { child ->


                                            child.data?.forEach { chi ->

                                                println("each child")
                                                println(chi.key)
                                                println(chi.value)

                                                val CHANNEL_ID =
                                                    "my_channel_01"
                                                println("PREPARE NOTIFICATION")


                                                notification =
                                                    NotificationCompat.Builder(context, "first")
                                                        .apply {
                                                            setContentTitle("LIVE PROVA")
                                                            setContentText("STA ANDANDO")

                                                            setSmallIcon(R.drawable.ic_live)
                                                            setAutoCancel(true) //collegato a tap notification


                                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                                println("create channel")
                                                                val name: CharSequence =
                                                                    "Channel prova"// The user-visible name of the channel.
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

                                            }

                                            MapsActivity.db.collection("user").document(idDB)
                                                .collection("prova").document(child.id).delete()
                                        }
                                    }

                                }
                        }
        }

            private fun stopService() {
                println("Stopping the foreground service")
                Toast.makeText(this, "Service stopping", Toast.LENGTH_SHORT).show()
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

//        private fun pingFakeServer() {
//            val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmmZ")
//            val gmtTime = df.format(Date())
//
//            val deviceId = Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)
//
//            val json =
//                """
//                {
//                    "deviceId": "$deviceId",
//                    "createdAt": "$gmtTime"
//                }
//            """
//            try {
//                Fuel.post("https://jsonplaceholder.typicode.com/posts")
//                    .jsonBody(json)
//                    .response { _, _, result ->
//                        val (bytes, error) = result
//                        if (bytes != null) {
//                            println("[response bytes] ${String(bytes)}")
//                        } else {
//                            println("[response error] ${error?.message}")
//                        }
//                    }
//            } catch (e: Exception) {
//                println("Error making the request: ${e.message}")
//            }
//        }
//
//            @RequiresApi(Build.VERSION_CODES.N)
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