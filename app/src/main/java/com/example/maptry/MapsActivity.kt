package com.example.maptry

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.maptry.R.id
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.gcm.Task
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.core.Constants
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.HttpsCallableResult
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import okhttp3.*
import okio.Okio.buffer
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLEncoder
import java.nio.Buffer
import java.util.*
import java.util.Arrays.asList
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

//import khttp.get
@Suppress("DEPRECATION")
class MapsActivity  : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener,NavigationView.OnNavigationItemSelectedListener{

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var listAddr:MutableList<Address>? = null

    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var mqttAndroidClient: MqttAndroidClient
    private lateinit var locationRequest: LocationRequest
    private var addrThread:Thread? = null
    private lateinit var geocoder : Geocoder
    private lateinit var mAnimation : Animation
    public val myList = JSONObject() // POI json
    var myjson = JSONObject() //tmp json
    private val mymarker = JSONObject() //marker
    private var locationUpdateState = false
    private var zoom = 1
    private var timer = Timer()


    private lateinit var database: FirebaseDatabase
//    public lateinit var dataFromfirebase : DataSnapshot



    private lateinit var alertDialog: AlertDialog
    private var drawed = false
    private var mainHandler = Handler()


    private var mFunctions : FirebaseFunctions = FirebaseFunctions.getInstance();
    private var run = object : Runnable {
        override fun run() {
            if(drawed) {

                val drawerLayout: FrameLayout = findViewById(R.id.drawer_layout)
                val listLayout: FrameLayout = findViewById(R.id.list_layout)
                val homeLayout: FrameLayout = findViewById(R.id.homeframe)
                val splashLayout: FrameLayout = findViewById(R.id.splashFrame)
                val friendLayout: FrameLayout = findViewById(R.id.friend_layout)
                splashLayout.invalidate()
                splashLayout.visibility = View.GONE
                switchFrame(homeLayout,listLayout,drawerLayout,friendLayout)
                mainHandler.removeCallbacksAndMessages(null);
            }
            else mainHandler.postDelayed(this, 1500)
        }
    }
    val postListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {

            dataFromfirebase = dataSnapshot
            if(dataSnapshot.hasChildren()) {
                dataSnapshot.children.forEach { child ->
                    myjson = JSONObject()
                    child.children.forEach { chi ->
                        println(chi.key)
                        println(chi.value)
                        myjson.put(chi.key,chi.value)
                    }
                    var pos :LatLng = LatLng(myjson.getString("lat").toDouble(),myjson.getString("lon").toDouble())
                    println("CREATEEEEEEEE")
                    println(pos)
                    var mark = createMarker(pos)
                    mymarker.put(pos.toString(),mark)
                    myList.put(pos.toString(),myjson)
                }
            }
            drawed = true

        }

        override fun onCancelled(databaseError: DatabaseError) {
            // Getting Post failed, log a message
            Log.w("ON CANCELLED", "loadPost:onCancelled", databaseError.toException())
            // ...
        }
    }

    companion object {
        lateinit var dataFromfirebase: DataSnapshot
        public var account : GoogleSignInAccount? = null
        lateinit var dataFromfirestore :List<DocumentSnapshot>
        val db = Firebase.firestore
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1

        private const val REQUEST_CHECK_SETTINGS = 2

        private const val PLACE_PICKER_REQUEST = 3
    }

    /*Start Initialize Function*/


    private fun setUpMap() {
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return

        }

        mMap.isMyLocationEnabled = true

        val mapFragment =  supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        var locationButton : View? = mapFragment.view?.findViewById<LinearLayout>(Integer.parseInt("1"))
        val prov : View? = (locationButton?.parent) as View
        locationButton = prov?.findViewById(Integer.parseInt("2"));
        val layoutParams = locationButton?.getLayoutParams() as RelativeLayout.LayoutParams
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        layoutParams.setMargins(0, 0, 30, 30);

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                if (zoom == 1) {
                    mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                location.latitude,
                                location.longitude
                            ), 20F
                        )
                    )

                }

                if (zoom == 0) {
                    var mark: Marker = mymarker.get(
                        LatLng(
                            lastLocation.latitude,
                            lastLocation.longitude
                        ) .toString()
                    ) as Marker

                    mark.remove()
                    mymarker.remove(LatLng(lastLocation.latitude, lastLocation.longitude).toString())
                }
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                createMarker(currentLatLng)
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng))
                zoom = 0
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        geocoder = Geocoder(this)
        setContentView(R.layout.activity_maps)
        //create connection

        database = FirebaseDatabase.getInstance()
        database.setPersistenceEnabled(true)
        val drawerLayout: FrameLayout = findViewById(R.id.drawer_layout)
        val listLayout: FrameLayout = findViewById(R.id.list_layout)
        val homeLayout: FrameLayout = findViewById(R.id.homeframe)
        val splashLayout: FrameLayout = findViewById(R.id.splashFrame)
        mAnimation = AnimationUtils.loadAnimation(this, R.anim.enlarge);
        mAnimation.backgroundColor = Color.TRANSPARENT;

        drawerLayout.invalidate()
        listLayout.invalidate()
        homeLayout.invalidate()

        splashLayout.visibility = View.VISIBLE
        drawerLayout.visibility = View.GONE
        listLayout.visibility = View.GONE
        homeLayout.visibility = View.GONE

        splashLayout.startAnimation(mAnimation);
        mAnimation.start();
        splashLayout.bringToFront()
        mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post(run)
        val menuIntent : Intent=  Intent(this,LoginActivity::class.java)
        val component : ComponentName = ComponentName(this,LoginActivity::class.java)
        intent.component = component
        startActivityForResult(menuIntent,40);
        val displayMetrics = DisplayMetrics()

        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val mapFragment = supportFragmentManager
            .findFragmentById(id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                if (zoom == 1) {
                    mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                p0.lastLocation.latitude,
                                p0.lastLocation.longitude
                            ), 20F
                        )
                    )
                }
                if(zoom == 0) {
                    var mark: Marker = mymarker.get(
                        LatLng(
                            lastLocation.latitude,
                            lastLocation.longitude
                        ).toString()
                    ) as Marker
                    mark.remove()
                    mymarker.remove(LatLng(lastLocation.latitude, lastLocation.longitude).toString())
                }


                lastLocation = p0.lastLocation

                createMarker(LatLng(lastLocation.latitude, lastLocation.longitude))
                zoom = 0
            }
        }

        createLocationRequest()


    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMapClickListener(this)
        setUpMap()
        setUpSearch()
    }

    /*End Initialize Function*/


    /*Start Map Function*/

    override fun onMarkerClick(p0: Marker): Boolean {


        // add your position control
        val inflater: LayoutInflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_custom_view, null)
        var address: TextView = dialogView.findViewById(id.txt_addressattr)
        var phone: TextView = dialogView.findViewById(id.phone_contentattr)
        var header: TextView = dialogView.findViewById(id.headerattr)
        var url: TextView = dialogView.findViewById(id.uri_lblattr)
        var text : String =  myList.getJSONObject(p0.position.toString()).get("type") as String+": "+myList.getJSONObject(p0.position.toString()).get("name") as String
        header.text =  text
        address.text = myList.getJSONObject(p0.position.toString()).get("addr") as String
        url.text = myList.getJSONObject(p0.position.toString()).get("url") as String
        phone.text = myList.getJSONObject(p0.position.toString()).get("phone") as String
        val routebutton: Button = dialogView.findViewById(id.routeBtn)
        val removebutton: Button = dialogView.findViewById(id.removeBtnattr)
        removebutton.setOnClickListener {
            alertDialog.dismiss()
        }
        val homeLayout: FrameLayout = findViewById(R.id.homeframe)
        val drawerLayout: FrameLayout = findViewById(R.id.drawer_layout)
        val listLayout: FrameLayout = findViewById(R.id.list_layout)
        val friendLayout: FrameLayout = findViewById(R.id.friend_layout)


        if (homeLayout.visibility == View.GONE) {
            routebutton.text = "Visualizza"
            routebutton.setOnClickListener {
                mMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            p0.position.latitude,
                            p0.position.longitude
                        ), 20F
                    )
                )
                switchFrame(homeLayout,listLayout,drawerLayout,friendLayout)
                alertDialog.dismiss()
            }
        }
        else{
            routebutton.setOnClickListener {
                var intent =
                    Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + address.text))
                startActivity(intent)
                alertDialog.dismiss()
            }
        }

        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        dialogBuilder.setOnDismissListener(object : DialogInterface.OnDismissListener {
            override fun onDismiss(arg0: DialogInterface) { }
        })
        dialogBuilder.setView(dialogView)

        alertDialog = dialogBuilder.create();
        alertDialog.show()
        return false
    }

    @SuppressLint("SetTextI18n")
    override fun onMapClick(p0: LatLng) {
//        var url = URL("http://192.168.1.80:3000/get")
//        val client = OkHttpClient()
//        val request = Request.Builder()
//            .url(url)
//            .build()
//
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {println("PORCODIOOO")}
//            override fun onResponse(call: Call, response: Response) = println(response.body()?.string())
//        })

        val inflater: LayoutInflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_list_view, null)
        val spinner: Spinner = dialogView.findViewById(id.planets_spinner)
        var lname : EditText = dialogView.findViewById(id.txt_lname)
        var address :  TextView = dialogView.findViewById(id.txt_address)
        var publicButton: RadioButton = dialogView.findViewById(id.rb_public)
        var privateButton: RadioButton = dialogView.findViewById(id.rb_private)

        spinner.onItemSelectedListener = SpinnerActivity()


        address.isEnabled = false

        var background = object : Runnable {
            override fun run() {
                try {
                    listAddr = geocoder.getFromLocation(p0.latitude, p0.longitude, 1)
                    return
                } catch (e: IOException) {
                    Log.e("Error", "grpc failed: " + e.message, e)
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
        address.text = listAddr?.get(0)?.getAddressLine(0)

        ArrayAdapter.createFromResource(
            this,
            R.array.planets_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
        val addbutton: Button = dialogView.findViewById(id.addBtn)
        val removebutton: Button = dialogView.findViewById(id.removeBtn)
        removebutton.setOnClickListener {
            alertDialog.dismiss()
        }
        addbutton.setOnClickListener {
            var text = ""
            var gender = "gen"
            myList.put(p0.toString(),myjson)

            if(publicButton.isChecked)
                gender = publicButton.text.toString()
            if (privateButton.isChecked)
                gender = privateButton.text.toString()

            text  = lname.text.toString()
            for(i:String in myList.keys()){
                try {
                    var x: String = myList.getJSONObject(i).get("name") as String
                    if (text == x) {
                        //esiste gia con quel nome non lo aggiuno o lo sovrascrivo?
                        alertDialog.dismiss()
                        return@setOnClickListener
                    } else if (address.text == myList.getJSONObject(i).get("addr") as String) {
                        //esiste gia con quel nome non lo aggiuno o lo sovrascrivo?
                        alertDialog.dismiss()
                        return@setOnClickListener
                    }
                }
                catch(e:java.lang.Exception){
                    println("ops")
                }
            }
            var marker = createMarker(p0)
            myjson = JSONObject()
            myjson.put("name",text)
            myjson.put("addr",address.text.toString())
            myjson.put("cont",spinner.getSelectedItem().toString())
            myjson.put("type",gender)
            myjson.put("marker",marker)


            myjson.put("url","da implementare")
            myjson.put("phone","da implementare")
//            if(txt?.get(0)?.url === null) myjson.put("url","Url non trovato")
//                else  myjson.put("url",txt?.get(0)?.url)
//            if(txt?.get(0)?.phone === null) myjson.put("phone","cellulare non trovato")
//            else  myjson.put("phone",txt?.get(0)?.phone)



            println("URLLLLL")
            println(myList)
            var id = account?.email?.replace("@gmail.com","")
            id?.let { it1 ->
                if (marker != null) {
                    writeNewPOI(it1,text,address.text.toString(),spinner.selectedItem.toString(),gender,marker,"da implementare","da implementare")


                    // create URL


//                    with(url.openConnection() as HttpURLConnection) {
//                        requestMethod = "GET"  // optional default is GET
//
//
////                        inputStream.bufferedReader().use {
////                            it.lines().forEach { line ->
////                                println(line)
////                            }
////                        }
//                    }

                    println("FATTOOOOOOOOO")
//                    val menuIntent : Intent=  Intent(this,ServerActivity::class.java)
//                    val component : ComponentName = ComponentName(this,ServerActivity::class.java)
//                    intent.component = component
//                    startActivityForResult(menuIntent,30);
//                    try {
//                        //if you are using a phone device you should connect to same local network as your laptop and disable your pubic firewall as well
//                        var socket : com.github.nkzawa.socketio.client.Socket? = IO.socket("http://192.168.1.80:3000");
//                        //create connection
//                        socket?.connect()
//
//                        // emit the event join along side with the nickname
//                        socket?.emit("add", it1);
//                    } catch (e: URISyntaxException) {
//                        e.printStackTrace();
//                    }
                }
            }
            alertDialog.dismiss()

        }

        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        dialogBuilder.setOnDismissListener(object : DialogInterface.OnDismissListener {
            override fun onDismiss(arg0: DialogInterface) {

            }
        })
        dialogBuilder.setView(dialogView)

        alertDialog = dialogBuilder.create();
        alertDialog.show()
    }


    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(this@MapsActivity,
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {}
            }
        }
    }

    /*End Map Function*/

    /*Start Override Function*/
    @SuppressLint("ResourceType")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
        else if (requestCode == 30) {
            if (resultCode == 60) {
                println("SERVER OK")
            }
            else if (resultCode == 70) {
                println("SERVER OK")
            }
        }
        else if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                val place = data?.let { Autocomplete.getPlaceFromIntent(it) };
                Log.i("OK", "Place: " + place?.getName() + ", " + place?.getId());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                val status = data?.let { Autocomplete.getStatusFromIntent(it) };
                Log.i("errpr", status?.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
        else if (requestCode == 40) {
            if (resultCode == 50) {
                account = GoogleSignIn.getLastSignedInAccount(this@MapsActivity)
                var docRef = db.collection("user")
                println("DOCREFFF")
//                if(!docRef.get().isSuccessful) docRef.add({})
                var id: String? = account?.email?.replace("@gmail.com", "")
                if (id != null) {
                    if (!db.document("user/" + id).get().isSuccessful) {
                        docRef.document(id).set({})
                    }
                }
                scheduleRepeatingTasks()
                println(id)

                val x = findViewById<NavigationView>(R.id.nav_view).getHeaderView(0)
                val google_button = x.findViewById<Button>(R.id.google_button)
                val imageView = x.findViewById<ImageView>(R.id.imageView)
                val user = x.findViewById<TextView>(R.id.user)
                val email = x.findViewById<TextView>(R.id.email)
                val close = x.findViewById<ImageView>(R.id.close)

                val autoCompleteFragment =
                    supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as? AutocompleteSupportFragment
                val layout: LinearLayout = autoCompleteFragment?.view as LinearLayout
                val menuIcon: ImageView = layout.getChildAt(0) as ImageView
                google_button.visibility = View.GONE
                imageView.visibility = View.VISIBLE
                Picasso.get().load(account?.photoUrl).into(imageView)
                Picasso.get()
                    .load(account?.photoUrl)
                    .transform(CircleTransform())
                    .resize(100, 100)
                    .into(menuIcon)
                menuIcon.setOnClickListener(View.OnClickListener() {

                    val drawerLayout: FrameLayout = findViewById(R.id.drawer_layout)
                    val listLayout: FrameLayout = findViewById(R.id.list_layout)
                    val homeLayout: FrameLayout = findViewById(R.id.homeframe)
                    val friendLayout: FrameLayout = findViewById(R.id.friend_layout)
                    switchFrame(drawerLayout, listLayout, homeLayout,friendLayout)
                })
                user.visibility = View.VISIBLE
                user.text = account?.displayName

                email.visibility = View.VISIBLE
                email.text = account?.email
                close.visibility = View.VISIBLE
                //draw all poi from DB and update localjson

                if (id != null) {
                    db.collection("user").document(id).collection("marker")
                        .addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                            if (firebaseFirestoreException != null) {
                                Log.w("TAG", "Listen failed.", firebaseFirestoreException)
                                return@addSnapshotListener
                            }

                            if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                                dataFromfirestore = querySnapshot.documents

                                Log.d("TAG", "Current data: ${querySnapshot.documents}")
                                println("CIAOOOO")
                                querySnapshot.documents.forEach { child ->
                                    myjson = JSONObject()
                                    child.data?.forEach { chi ->
                                        println(chi.key)
                                        println(chi.value)
                                        myjson.put(chi.key, chi.value)
                                    }
                                    var pos: LatLng = LatLng(
                                        myjson.getString("lat").toDouble(),
                                        myjson.getString("lon").toDouble()
                                    )
                                    println("CREATEEEEEEEE")
                                    println(pos)
                                    // refactor create marker to not call getaddress
                                    var mark = createMarker(pos)
                                    mymarker.put(pos.toString(), mark)
                                    myList.put(pos.toString(), myjson)
                                }
                            }
                            drawed = true


                        }
                }

            }

//
//                database.reference.child("user").child(it).child("marker").addValueEventListener(postListener)
//                account?.email?.let { database.reference.child("user").child(it).child("marker").addValueEventListener(postListener) }
//                account?.id?.let { database.reference.child("user").child(it).child("marker").addValueEventListener(postListener) }


            else if (resultCode == 40) {
                println("non loggato")
                var x = findViewById<NavigationView>(R.id.nav_view).getHeaderView(0)
//                var google_button = x.findViewById<SignInButton>(R.id.google_button)
                var google_button = x.findViewById<Button>(R.id.google_button)
                var close = x.findViewById<ImageView>(R.id.close)
                var user = x.findViewById<TextView>(R.id.user)
                var email = x.findViewById<TextView>(R.id.email)
                var imageView = x.findViewById<ImageView>(R.id.imageView)

                google_button.visibility = View.VISIBLE
                close.visibility = View.GONE
                imageView.visibility = View.GONE
                user.visibility = View.GONE
                email.visibility = View.GONE

            }
        }
        }


    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }


    @SuppressLint("RestrictedApi")
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.list -> {
                showPOI()
                return true
            }
            R.id.help ->{
                Toast.makeText(applicationContext, "help da implementare", Toast.LENGTH_LONG).show()
                return true
            }
            R.id.friend ->{
                showFriend()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /*End Override Function*/

    /*Start Utils Function*/

    @SuppressLint("ResourceType")
    fun setUpSearch() {
        val autoCompleteFragment =
            supportFragmentManager.findFragmentById(id.autocomplete_fragment) as? AutocompleteSupportFragment
        autoCompleteFragment?.setCountry("IT")
        autoCompleteFragment?.setPlaceFields(asList(Place.Field.ID,
            Place.Field.NAME,Place.Field.LAT_LNG,Place.Field.ADDRESS,Place.Field.PHONE_NUMBER,Place.Field.WEBSITE_URI))

        val layout : LinearLayout = autoCompleteFragment?.getView() as LinearLayout
        val menuIcon: ImageView =  layout.getChildAt(0) as ImageView

//        menuIcon?.setImageDrawable(resources.getDrawable(R.drawable.ic_menu))
        val navMenu: NavigationView = findViewById(R.id.nav_view)
        navMenu.setNavigationItemSelectedListener(this)
        autoCompleteFragment?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                var lat = place.latLng
                if (lat != null) {
                    autoCompleteFragment.setText("")
                    supportFragmentManager.popBackStack();
                    onMapClick(lat)
                }
            }
            override fun onError(status: Status) {
                Log.d("HOY", "An error occurred: ${status.statusMessage}")
            }
        })
    }

    private fun getAddress(latLng: LatLng) {
            try{
                listAddr = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            }catch(e: IOException){
                Log.e("Error", "grpc failed: " + e.message, e)
                // ... retry again your code that throws the exeception
            }

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

    fun closeDrawer(view: View) {
        val drawerLayout: FrameLayout = findViewById(R.id.drawer_layout)
        val listLayout: FrameLayout = findViewById(R.id.list_layout)
        val homeLayout: FrameLayout = findViewById(R.id.homeframe)
        val friendLayout: FrameLayout = findViewById(R.id.friend_layout)
        switchFrame(homeLayout,listLayout,drawerLayout,friendLayout)
    }

    @SuppressLint("WrongViewCast")
    fun showPOI(){
        val len = myList.length()
        var index = 0
        val txt: TextView = findViewById(R.id.nosrc)

        val drawerLayout: FrameLayout = findViewById(R.id.drawer_layout)
        val listLayout: FrameLayout = findViewById(R.id.list_layout)
        val homeLayout: FrameLayout = findViewById(R.id.homeframe)
        val friendLayout: FrameLayout = findViewById(R.id.friend_layout)
        switchFrame(listLayout,homeLayout,drawerLayout,friendLayout)


        var  lv:ListView = findViewById<ListView>(R.id.lv)
        val userList = MutableList<String>(len,{""})
        if(len == 0) txt.visibility = View.VISIBLE;
        else txt.visibility = View.INVISIBLE;
        for (i in myList.keys()){
            userList[index] = myList.getJSONObject(i).get("name") as String
            index++
        }


        var  arrayAdapter : ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, userList);


        mMap.clear()
        for(i in mymarker.keys()){
            var marker : Marker = mymarker[i] as Marker
            mMap.addMarker( MarkerOptions()
                .position(marker.position)
                .title(marker.title)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .alpha(0.7f)    )
        }

        lv.setOnItemLongClickListener { parent, view, position, id ->

            val inflater: LayoutInflater = this.layoutInflater
            val dialogView: View = inflater.inflate(R.layout.dialog_custom_eliminate, null)

            val eliminateBtn: Button = dialogView.findViewById(R.id.eliminateBtn)
            eliminateBtn.setOnClickListener {
                val selectedItem = parent.getItemAtPosition(position) as String
                for (i in myList.keys()){

                    if(selectedItem == myList.getJSONObject(i).get("name") as String) {

                        var mark = mymarker[i] as Marker
                        val removed = myList.getJSONObject(i)
                        //eliminate from DB
                        mark.remove()
                        mymarker.remove(i)
                        myList.remove(i)
                        //remove element from db
                        var AC:String
                        AC = "Annulla"
                        var text = "Rimosso "+selectedItem.toString()
                        var id = account?.email?.replace("@gmail.com","")
                        val snackbar = Snackbar.make(view, text, 2000)
                            .setAction(AC,View.OnClickListener {

                                id?.let { it1 ->
                                    myList.put(mark.position.toString(),removed)
                                    writeNewPOI(it1,removed.get("name").toString(),removed.get("addr").toString(),removed.get("cont").toString(),removed.get("type").toString(),mark,"da implementare","da implementare")
                                    Toast.makeText(this,"undo" + selectedItem.toString(),Toast.LENGTH_LONG)

                                    showPOI()
                                }
                            })
                        snackbar.setActionTextColor(Color.DKGRAY)
                        val snackbarView = snackbar.view
                        snackbarView.setBackgroundColor(Color.BLACK)
                        snackbar.show()
                        //refactor from firestore

                        dataFromfirestore.forEach { child ->
                            myjson = JSONObject()
                            child.data?.forEach { chi ->
                                if(chi.value == selectedItem){
                                    var key = chi.key
                                    if (id != null && key != null) {
                                        println("CIAOOOOO")
                                        println(child.id)
                                        println("user/"+id+"/marker/"+child.id)
                                        db.document("user/"+id+"/marker/"+child.id).delete()
                                        showPOI()
                                        alertDialog.dismiss()
                                        return@setOnClickListener
                                    }
                                }
                            }
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
            val selectedItem = parent.getItemAtPosition(position) as String
            for (i in myList.keys()){
                if(selectedItem == myList.getJSONObject(i).get("name") as String) onMarkerClick(mymarker[i] as Marker)
            }
        }
        lv.adapter = arrayAdapter;
        account
    }


    private fun switchFrame(toView: FrameLayout, toGone1: FrameLayout, toGone2: FrameLayout,toGone3: FrameLayout){
        toGone1.invalidate()
        toGone2.invalidate()
        toGone3.invalidate()
        toView.visibility = View.VISIBLE
        toGone1.visibility = View.GONE
        toGone2.visibility = View.GONE
        toGone3.visibility = View.GONE
        toView.startAnimation(mAnimation);
        mAnimation.start();
        toView.bringToFront()
    }
    private fun showFriend(){
        val len = myList.length()
        var index = 0
        val txt: TextView = findViewById(R.id.nofriend)

        val drawerLayout: FrameLayout = findViewById(R.id.drawer_layout)
        val listLayout: FrameLayout = findViewById(R.id.list_layout)
        val homeLayout: FrameLayout = findViewById(R.id.homeframe)
        val friendLayout: FrameLayout = findViewById(R.id.friend_layout)
        switchFrame(friendLayout,listLayout,homeLayout,drawerLayout)


        var  lv:ListView = findViewById<ListView>(R.id.fv)
        val friendList = MutableList<String>(len,{""})
        if(len == 0) txt.visibility = View.VISIBLE;
        else txt.visibility = View.INVISIBLE;
        /*refactor for friend list
        for (i in myList.keys()){
            friendList[index] = myList.getJSONObject(i).get("name") as String
            index++
        }
        */

        var  arrayAdapter : ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, friendList);

    }

    fun addFriend(view: View) {
        val inflater: LayoutInflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.add_friend, null)
        val emailText : EditText = dialogView.findViewById(R.id.friendEmail)
        val addBtn: Button = dialogView.findViewById(R.id.friendBtn)
        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        dialogBuilder.setOnDismissListener(object : DialogInterface.OnDismissListener {
            override fun onDismiss(arg0: DialogInterface) { }
        })
        dialogBuilder.setView(dialogView)
        alertDialog = dialogBuilder.create();
        alertDialog.show()
        addBtn.setOnClickListener {
            println("CLICCATOOOOOOOOOOO")
            println(emailText.text.toString())
            if(emailText.text.toString() !="" && emailText.text.toString() != "Inserisci Email"){

                // && emailText.text.toString() != account?.email
                var url = URL("http://192.168.1.80:3000/addFriend?"+ URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(emailText.text.toString(), "UTF-8")+"&"+ URLEncoder.encode("sender", "UTF-8") + "=" + URLEncoder.encode(account?.email.toString(), "UTF-8"))
                println("URLLLLLLLLLLLLLLLLL")
                println(url)
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(url)
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {println("something went wrong")}
                    override fun onResponse(call: Call, response: Response) = println(response.body()?.string())
                })
            }
        }

        //send call to server
    }



    /*End Utils Function*/
    /*Start Notification Function*/
    private fun scheduleRepeatingTasks() {

        /*Setting up different constraints on the work request.
         */
        val constraints = Constraints.Builder().apply {
            setRequiredNetworkType(NetworkType.CONNECTED)
            setRequiresCharging(true)
            setRequiresStorageNotLow(true)
        }.build()

        /*Build up an obejct of PeriodicWorkRequestBuilder
        */
        val repeatingWork = PeriodicWorkRequestBuilder<NotificationRequestWorker>(
            1,
            TimeUnit.DAYS
        ).setConstraints(constraints)
            .build()

        /*Enqueue the work request to an instance of Work Manager
         */
        WorkManager.getInstance(this).enqueue(repeatingWork)
    }

    /*End Notification Function*/

    /*Start Database Function*/
    public fun writeNewPOI(userId: String, name:String,addr:String,cont:String,type:String,marker:Marker,url:String,phone:String) {
        val user = UserMarker(name,addr,cont,type,marker.position.latitude.toString(),marker.position.longitude.toString(),url,phone)
        db.collection("user").document(userId).collection("marker").add(user).addOnSuccessListener {
            Log.d("TAG", "success")
        }
            .addOnFailureListener { ex : Exception ->
                Log.d("TAG", ex.toString())

            }

//        try {
//            //if you are using a phone device you should connect to same local network as your laptop and disable your pubic firewall as well
//
//
//            // emit the event join along side with the nickname
//            socket?.emit("add", userId);
//        } catch (e: URISyntaxException) {
//            e.printStackTrace();
//        }
//        var key = database.reference.child("user").child(userId).child("marker").push().key
//        if (key != null) {
//            database.reference.child("user").child(userId).child("marker").child(key).setValue(user).addOnSuccessListener {
//
//
//                }


//        }
    }


/*End Database Function*/




}



