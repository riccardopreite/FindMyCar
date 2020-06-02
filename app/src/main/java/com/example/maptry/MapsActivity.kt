package com.example.maptry

import android.Manifest
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.maptry.R.id
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_friend_view.*
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import org.eclipse.paho.android.service.MqttAndroidClient
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.net.URLEncoder
import java.util.*
import java.util.Arrays.asList

//import khttp.get
@Suppress("DEPRECATION")
class MapsActivity  : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener,NavigationView.OnNavigationItemSelectedListener{

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var mqttAndroidClient: MqttAndroidClient
    private lateinit var locationRequest: LocationRequest


    private var locationUpdateState = false
    private var zoom = 1
    private var timer = Timer()


    private lateinit var database: FirebaseDatabase
//    public lateinit var dataFromfirebase : DataSnapshot

    private var mainHandler = Handler()


    private var mFunctions : FirebaseFunctions = FirebaseFunctions.getInstance();
    private var run = object : Runnable {
        override fun run() {
            if(drawed) {
                println("DRAWED E' TRUE")
                val drawerLayout: FrameLayout = findViewById(R.id.drawer_layout)
                val listLayout: FrameLayout = findViewById(R.id.list_layout)
                val homeLayout: FrameLayout = findViewById(R.id.homeframe)
                val splashLayout: FrameLayout = findViewById(R.id.splashFrame)
                val friendLayout: FrameLayout = findViewById(R.id.friend_layout)
                val friendRequestLayout: FrameLayout = findViewById(R.id.friendFrame)
                splashLayout.invalidate()
                splashLayout.visibility = View.GONE
                switchFrame(homeLayout,listLayout,drawerLayout,friendLayout,friendRequestLayout)
                mainHandler.removeCallbacksAndMessages(null);
            }
            else {
                println("DRAWED E' FALSE")
                mainHandler.postDelayed(this, 1500)
            }
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
        lateinit var context : Context
        lateinit var alertDialog: AlertDialog
        lateinit var mMap: GoogleMap
        var addrThread:Thread? = null
        lateinit var geocoder : Geocoder
        var listAddr:MutableList<Address>? = null
        var drawed = false
        var myjson = JSONObject() //tmp json
        val mymarker = JSONObject() //marker
        val myList = JSONObject() // POI json
        val myCar = JSONObject() // car json
        lateinit var mAnimation : Animation
        lateinit var dataFromfirebase: DataSnapshot
        public var account : GoogleSignInAccount? = null
        lateinit var dataFromfirestore :List<DocumentSnapshot>
        val db = Firebase.firestore
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1

        private const val REQUEST_CHECK_SETTINGS = 2

        private const val PLACE_PICKER_REQUEST = 3
        var friendJson = JSONObject() // friend json
        var friendTempPoi = JSONObject()
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

//    override fun onStop() {
//        super.onStop()
//        println("STOPPPPPP")
//        startService(Intent(this, NotificationService::class.java))
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        val policy: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        geocoder = Geocoder(this)
        setContentView(R.layout.activity_maps)
        //create connection


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
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type="text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, "https://maps.google.com/?q="+myList.getJSONObject(p0.position.toString()).get("lat")+","+myList.getJSONObject(p0.position.toString()).get("lon"));
            startActivity(Intent.createChooser(shareIntent,"Stai condividendo "+myList.getJSONObject(p0.position.toString()).get("name")))
            alertDialog.dismiss()
        }
        val homeLayout: FrameLayout = findViewById(R.id.homeframe)
        val drawerLayout: FrameLayout = findViewById(R.id.drawer_layout)
        val listLayout: FrameLayout = findViewById(R.id.list_layout)
        val friendLayout: FrameLayout = findViewById(R.id.friend_layout)
        val friendRequestLayout: FrameLayout = findViewById(R.id.friendFrame)

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
                switchFrame(homeLayout,listLayout,drawerLayout,friendLayout,friendRequestLayout)
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
        val inflater: LayoutInflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_list_view, null)
        val spinner: Spinner = dialogView.findViewById(id.planets_spinner)
        var lname : EditText = dialogView.findViewById(id.txt_lname)
        var address :  TextView = dialogView.findViewById(id.txt_address)
        var publicButton: RadioButton = dialogView.findViewById(id.rb_public)
        var privateButton: RadioButton = dialogView.findViewById(id.rb_private)
        var timePickerLayout = dialogView.findViewById<RelativeLayout>(R.id.timePicker)
        var timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker1)
        timePicker.hour = 3
        timePicker.minute = 0
        var radioGroup = dialogView.findViewById<RelativeLayout>(R.id.rl_gender)
        var time = 180
//        timePicker.setIs24HourView(true)
//        timePicker.visibility = View.GONE
//        spinner.onItemSelectedListener = SpinnerActivity()


        address.isEnabled = false

        var background = object : Runnable {
            override fun run() {
                try {
                    listAddr = geocoder.getFromLocation(p0.latitude, p0.longitude, 1)
                    return
                } catch (e: IOException) {
                    Log.e("Error", "grpc failed: " + e.message, e)
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

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                var type = parent?.getItemAtPosition(position) as String
                println("SELECETED")
                if(type == "Macchina"){

                    println("MACCHINA")
                    radioGroup.visibility = View.GONE

                    timePicker.setIs24HourView(true)
                    timePickerLayout.visibility = View.VISIBLE
                }
                else{

                    println("ALTRO")
                    radioGroup.visibility = View.VISIBLE
                    timePicker.setIs24HourView(true)
                    timePickerLayout.visibility = View.GONE
                }
            }

        }


        removebutton.setOnClickListener {
            alertDialog.dismiss()
        }
        addbutton.setOnClickListener {
            var text = lname.text.toString()
            if (text == "") {
                lname.background.setColorFilter(
                    resources.getColor(R.color.quantum_googred),
                    PorterDuff.Mode.SRC_ATOP
                )
            }
            else {
                myjson = JSONObject()
                var gender = "gen"


                if (publicButton.isChecked)
                    gender = publicButton.text.toString()
                if (privateButton.isChecked)
                    gender = privateButton.text.toString()

                if (spinner.selectedItem.toString() == "Macchina") { //and for live methode too it will be public server call to  add event for all friend
                    gender = privateButton.text.toString()
                    println("TIMEEEEEE")
                    time = timePicker.hour * 60 + timePicker.minute
                    myjson.put("timer", time.toString())
                    myjson.put("name", text)
                    myjson.put("address", address.text.toString())
                    myjson.put("owner", account?.email?.replace("@gmail.com", ""))
                    reminderAuto(myjson)
                    myCar.put(p0.toString(), myjson)
                    //add countdown for notification 5 minutes before end and give the intent to reset/set timer server call with timer to do this, write in DB
                    println(time)
                }
                //                spinner on item selected
                else{
                    for (i: String in myList.keys()) {
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
                        } catch (e: java.lang.Exception) {
                            println("ops")
                        }
                    }
                    var marker = createMarker(p0)

                    myjson.put("name", text)
                    myjson.put("addr", address.text.toString())
                    myjson.put("cont", spinner.selectedItem.toString())
                    myjson.put("type", gender)
                    myjson.put("marker", marker)


                    myjson.put("url", "da implementare")
                    myjson.put("phone", "da implementare")
                    //            if(txt?.get(0)?.url === null) myjson.put("url","Url non trovato")
                    //                else  myjson.put("url",txt?.get(0)?.url)
                    //            if(txt?.get(0)?.phone === null) myjson.put("phone","cellulare non trovato")
                    //            else  myjson.put("phone",txt?.get(0)?.phone)


                    println("URLLLLL")
                    myList.put(p0.toString(), myjson)
                    println(myList)
                    var id = account?.email?.replace("@gmail.com", "")
                    id?.let { it1 ->
                        if (marker != null) {
                            writeNewPOI(
                                it1,
                                text,
                                address.text.toString(),
                                spinner.selectedItem.toString(),
                                gender,
                                marker,
                                "da implementare",
                                "da implementare"
                            )
                        }
                    }
            }
                alertDialog.dismiss()
            }

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
                database = FirebaseDatabase.getInstance()
                database.setPersistenceEnabled(true)
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
                    val friendRequestLayout: FrameLayout = findViewById(R.id.friendFrame)
                    switchFrame(drawerLayout, listLayout, homeLayout,friendLayout,friendRequestLayout)
                })
                user.visibility = View.VISIBLE
                user.text = account?.displayName

                email.visibility = View.VISIBLE
                email.text = account?.email
                close.visibility = View.VISIBLE
                //draw all poi from DB and update localjson

                if (id != null) {
                    createPoiList(id)
                    createFriendList(id)
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
        try{
            alertDialog.dismiss()
        }
        catch (e:Exception){

        }
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
            id.list -> {
                showPOI()
                return true
            }
            id.help ->{
                Toast.makeText(applicationContext, "help da implementare", Toast.LENGTH_LONG).show()
                return true
            }
            id.friend ->{
                showFriend()
                return true
            }
            id.car ->{
                //to show the car?
                showCar()
                return true
            }
            id.live ->{
                //to show the live event?
                Toast.makeText(applicationContext, "live da implementare", Toast.LENGTH_LONG).show()
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

//    fun createMarker(p0: LatLng): Marker? {
//
//        var background = object : Runnable {
//            override fun run() {
//                    try {
//                        listAddr = geocoder.getFromLocation(p0.latitude, p0.longitude, 1)
//                        return
//                    } catch (e: IOException) {
//                        Log.e("Error", "grpc failed2: " + e.message, e)
//                        // ... retry again your code that throws the exeception
//                    }
//                }
//
//        }
//        addrThread = Thread(background)
//        addrThread?.start()
//        try {
//            addrThread?.join()
//        } catch (e:InterruptedException) {
//            e.printStackTrace()
//        }
//
//
//        var text = "Indirizzo:" + listAddr?.get(0)?.getAddressLine(0)+"\nGeoLocalita:" +  listAddr?.get(0)?.getLocality() + "\nAdminArea: " + listAddr?.get(0)?.getAdminArea() + "\nCountryName: " + listAddr?.get(0)?.getCountryName()+ "\nPostalCode: " + listAddr?.get(0)?.getPostalCode() + "\nFeatureName: " + listAddr?.get(0)?.getFeatureName();
//
//        var x= mMap.addMarker(
//            MarkerOptions()
//                .position(p0)
//                .title(text)
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
//                .alpha(0.7f)
//        )
//
//        mymarker.put(p0.toString(),x)
//        return x
//    }

    fun closeDrawer(view: View) {
//        val drawerLayout: FrameLayout = findViewById(R.id.drawer_layout)
//        val listLayout: FrameLayout = findViewById(R.id.list_layout)
//        val homeLayout: FrameLayout = findViewById(R.id.homeframe)
//        val friendLayout: FrameLayout = findViewById(R.id.friend_layout)
//        val friendRequestLayout: FrameLayout = findViewById(R.id.friendFrame)
//        switchFrame(homeLayout,listLayout,drawerLayout,friendLayout,friendRequestLayout)
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

    @SuppressLint("WrongViewCast")
    fun showPOI(){
        val len = myList.length()
        var index = 0
        val txt: TextView = findViewById(R.id.nosrc)

        val drawerLayout: FrameLayout = findViewById(R.id.drawer_layout)
        val listLayout: FrameLayout = findViewById(R.id.list_layout)
        val homeLayout: FrameLayout = findViewById(R.id.homeframe)
        val friendLayout: FrameLayout = findViewById(R.id.friend_layout)
        val friendRequestLayout: FrameLayout = findViewById(R.id.friendFrame)
        switchFrame(listLayout,homeLayout,drawerLayout,friendLayout,friendRequestLayout)


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
    }



    @SuppressLint("ShowToast")
    private fun showFriend(){
        val len = friendJson.length()
        var index = 0
        val txt: TextView = findViewById(R.id.nofriend)
        val inflater: LayoutInflater = this.layoutInflater
        val id = account?.email?.replace("@gmail.com","")

        val drawerLayout: FrameLayout = findViewById(R.id.drawer_layout)
        val listLayout: FrameLayout = findViewById(R.id.list_layout)
        val homeLayout: FrameLayout = findViewById(R.id.homeframe)
        val friendLayout: FrameLayout = findViewById(R.id.friend_layout)
        val friendRequestLayout: FrameLayout = findViewById(R.id.friendFrame)
        switchFrame(friendLayout,listLayout,homeLayout,drawerLayout,friendRequestLayout)


        var  lv:ListView = findViewById<ListView>(R.id.fv)
        val friendList = MutableList<String>(len,{""})
        if(len == 0) txt.visibility = View.VISIBLE;
        else txt.visibility = View.INVISIBLE;
        println("PRINT FRIEND LIST")
        println(friendJson)
        for (i in friendJson.keys()){
            friendList[index] = friendJson[i] as String
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

                for(i in friendJson.keys()){
                    if(selectedItem == friendJson[i] as String) {
                        var removed = selectedItem
                        friendJson.remove(i)
                        var key = i
                        var AC:String
                        AC = "Annulla"
                        var text = "Rimosso "+selectedItem
                        var id = account?.email?.replace("@gmail.com","")
                        val snackbar = Snackbar.make(view, text, 2000)
                            .setAction(AC,View.OnClickListener {

                                id?.let { it1 ->
                                    friendJson.put(key,removed)
                                    confirmFriend(id,removed)
                                    Toast.makeText(this,"undo" + selectedItem.toString(),Toast.LENGTH_LONG)
                                    showFriend()

                                }
                            })

                        snackbar.setActionTextColor(Color.DKGRAY)
                        val snackbarView = snackbar.view
                        snackbarView.setBackgroundColor(Color.BLACK)
                        snackbar.show()
                        if (id != null) {
                            removeFriend(id,removed)
                            showFriend()
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
            val dialogView: View = inflater.inflate(R.layout.dialog_friend_view, null)
            var txtName :TextView = dialogView.findViewById(R.id.friendNameTxt)
            var spinner :Spinner = dialogView.findViewById(R.id.planets_spinner_POI)
            val selectedItem = parent.getItemAtPosition(position) as String

            var context = this
            txtName.text = selectedItem
                println("CLICK")
            var url = URL("http://192.168.1.80:3000/getPoiFromFriend?"+ URLEncoder.encode("friend", "UTF-8") + "=" + URLEncoder.encode(selectedItem, "UTF-8"))
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

                    this@MapsActivity.runOnUiThread(Runnable {
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
                            var arrayAdapter2:ArrayAdapter<String> = ArrayAdapter<String>(context,R.layout.support_simple_spinner_dropdown_item,markerList)
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
                                        friendTempPoi.put(pos.toString(), result.getJSONObject(key))
                                        mMap.moveCamera(
                                            CameraUpdateFactory.newLatLngZoom(
                                                LatLng(
                                                    lat,
                                                    lon
                                                ), 20F
                                            )
                                        )
                                        switchFrame(
                                            homeLayout,
                                            friendLayout,
                                            listLayout,
                                            drawerLayout,
                                            friendRequestLayout
                                        )
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
    private fun showCar(){
        val len = MapsActivity.myCar.length()
        var index = 0
        var indexFull = 0
        val txt: TextView = findViewById(R.id.nocar)
        val inflater: LayoutInflater = this.layoutInflater
        val id = MapsActivity.account?.email?.replace("@gmail.com","")

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
            println(emailText.text.toString())
            if(emailText.text.toString() !="" && emailText.text.toString() != "Inserisci Email"){// && emailText.text.toString() != account?.email
                account?.email?.replace("@gmail.com","")?.let { it1 ->
                    sendFriendRequest(emailText.text.toString(),
                        it1
                    )
                }
                alertDialog.dismiss()
            }
            else{
                //popup error
            }
        }

        //send call to server
    }

    /*End Utils Function*/


    /*Start Notification Function*/
    fun scheduleRepeatingTasks() {
        /*Setting up different constraints on the work request.
         */
        createNotification(this)
//        println("iniziato schedule")
//        val constraints = Constraints.Builder().apply {
//            setRequiredNetworkType(NetworkType.CONNECTED)
//            setRequiresCharging(true)
//            setRequiresStorageNotLow(true)
//        }.build()
//
//        /*Build up an obejct of PeriodicWorkRequestBuilder
//        */
//        val repeatingWork = OneTimeWorkRequestBuilder<NotificationRequestWorker>(
////            1,
////            TimeUnit.DAYS
//        ).setConstraints(constraints)
//            .build()
//
//        /*Enqueue the work request to an instance of Work Manager
//         */
//        WorkManager.getInstance(this).enqueue(repeatingWork)
//        println("fine schedule")
    }

    /*End Notification Function*/

    /*Start Database Function*/
    fun writeNewPOI(userId: String, name:String,addr:String,cont:String,type:String,marker:Marker,url:String,phone:String) {
        val user = UserMarker(name,addr,cont,type,marker.position.latitude.toString(),marker.position.longitude.toString(),url,phone)
        db.collection("user").document(userId).collection("marker").add(user).addOnSuccessListener {
            Log.d("TAG", "success")
        }
            .addOnFailureListener { ex : Exception ->
                Log.d("TAG", ex.toString())

            }
    }

    fun createFriendList(id:String){
        var count = 0
        db.collection("user").document(id).collection("friend")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                if (firebaseFirestoreException != null) {
                    Log.w("TAG", "Listen failed.", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                    dataFromfirestore = querySnapshot.documents

                    Log.d("TAGcreatefriendlist", "Current data: ${querySnapshot.documents}")
                    friendJson = JSONObject()
                    querySnapshot.documents.forEach { child ->
                        child.data?.forEach { chi ->
                            println(chi.key)
                            println(chi.value)
                            friendJson.put(count.toString(),chi.value)
                            count++

                        }
                    }

                }
            }
    }

    fun createPoiList(id:String){
            db.collection("user").document(id).collection("marker")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    println("SONO ENTRATO IN MARKER FIREBASE")
                    if (firebaseFirestoreException != null) {
                        Log.w("TAG", "Listen failed.", firebaseFirestoreException)
//                        return@addSnapshotListener
                    }

                    if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                        dataFromfirestore = querySnapshot.documents

                        Log.d("TAGcreatePoiList", "Current data: ${querySnapshot.documents}")
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
                            // refactor create marker to not call getaddress
                            var mark = createMarker(pos)
                            mymarker.put(pos.toString(), mark)
                            myList.put(pos.toString(), myjson)
                        }
                    }
                    println("DRAWED E' MESSO A TRUE")
                    drawed = true
                }

    }

/*End Database Function*/

}



