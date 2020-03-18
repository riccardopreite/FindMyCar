package com.example.maptry

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.app.ActivityCompat
import com.example.maptry.R.id
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places;
import com.google.android.gms.location.*
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONObject

import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso
import java.util.*
import java.util.Arrays.*

@Suppress("DEPRECATION")
class MapsActivity  : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener,NavigationView.OnNavigationItemSelectedListener{

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient



    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback

    private lateinit var locationRequest: LocationRequest

    private lateinit var mAnimation : Animation
    private val myList = JSONObject() // marker
    private val dict = JSONObject() //nome
    private val addr = JSONObject() //address
    private val content = JSONObject() //contenuto
    private val publicPrivate = JSONObject() //tipo
    private var locationUpdateState = false
    private var zoom = 1
    private var timer = Timer()
    public var account : GoogleSignInAccount? = null
    private lateinit var alertDialog: AlertDialog


    companion object {
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
                    var mark: Marker = myList.get(
                        LatLng(
                            lastLocation.latitude,
                            lastLocation.longitude
                        ).toString()
                    ) as Marker
                    mark.remove()
                    myList.remove(LatLng(lastLocation.latitude, lastLocation.longitude).toString())
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

        setContentView(R.layout.activity_maps)
        val drawerLayout: FrameLayout = findViewById(R.id.drawer_layout)
        val listLayout: FrameLayout = findViewById(R.id.list_layout)
        val homeLayout: FrameLayout = findViewById(R.id.homeframe)
        //add control if is logged
        homeLayout.invalidate()
        listLayout.invalidate()

        drawerLayout.visibility = View.VISIBLE;
        listLayout.visibility = View.GONE;
        homeLayout.visibility = View.GONE;

        val menuIntent : Intent=  Intent(this,LoginActivity::class.java)
        startActivityForResult(menuIntent,40);
        mAnimation = AnimationUtils.loadAnimation(this, R.anim.enlarge);
        mAnimation.backgroundColor = Color.TRANSPARENT;

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
                    var mark: Marker = myList.get(
                        LatLng(
                            lastLocation.latitude,
                            lastLocation.longitude
                        ).toString()
                    ) as Marker
                    mark.remove()
                    myList.remove(LatLng(lastLocation.latitude, lastLocation.longitude).toString())
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

        val inflater: LayoutInflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_custom_view, null)
        var address :  TextView = dialogView.findViewById(id.txt_addressattr)
        var privacy: TextView = dialogView.findViewById(id.genderattr)
        var header : TextView = dialogView.findViewById(id.headerattr)
        var cont: TextView = dialogView.findViewById(id.contentattr)

        header.text = dict.getString(p0.position.toString())
        address.text = addr.getString(p0.position.toString())
        cont.text = content.getString(p0.position.toString())
        privacy.text = publicPrivate.getString(p0.position.toString())
        val routebutton: Button = dialogView.findViewById(id.routeBtn)
        val removebutton: Button = dialogView.findViewById(id.removeBtnattr)
        removebutton.setOnClickListener {

            alertDialog.dismiss()
        }
        routebutton.setOnClickListener {
            var intent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q="+address.text))
            startActivity(intent)
            alertDialog.dismiss()
        }

        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        dialogBuilder.setOnDismissListener(object : DialogInterface.OnDismissListener {
            override fun onDismiss(arg0: DialogInterface) { }
        })
        dialogBuilder.setView(dialogView)

        alertDialog = dialogBuilder.create();
        alertDialog.show()

//        val builder = AlertDialog.Builder(this)
//        builder.setTitle("cliccato su " + p0.title)
//        builder.setMessage(dict.getString(p0.position.toString())
//                +"\n"+addr.getString(p0.position.toString())
//                +"\n"+content.getString(p0.position.toString())
//                +"\n"+publicPrivate.getString(p0.position.toString()))
//        if(zoom == 0) {
//            if(p0.position == LatLng(lastLocation.latitude,lastLocation.longitude)) {
//                builder.setTitle("La tua posizione")
//                builder.setMessage("Sei Qui")
//            }
//        }
//        builder.setPositiveButton("Close") { dialog, which -> }
//        builder.show()
        return false
    }

    @SuppressLint("SetTextI18n")
    override fun onMapClick(p0: LatLng) {

        val inflater: LayoutInflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_custom_input, null)
        val spinner: Spinner = dialogView.findViewById(id.planets_spinner)
        var lname : EditText = dialogView.findViewById(id.txt_lname)
        var address :  TextView = dialogView.findViewById(id.txt_address)
        var publicButton: RadioButton = dialogView.findViewById(id.rb_public)
        var privateButton: RadioButton = dialogView.findViewById(id.rb_private)

        spinner.onItemSelectedListener = SpinnerActivity()


        address.isEnabled = false
        var txt = getAddress(p0)
        address.text = txt?.get(0)?.getAddressLine(0)

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

            if(publicButton.isChecked)
                gender = publicButton.text.toString()
            if (privateButton.isChecked)
                gender = privateButton.text.toString()

            text  = lname.text.toString()
            for (i in dict.keys()){
                if(text == dict[i] as String) {
                    //esiste gia con quel nome non lo aggiuno o lo sovrascrivo?
                    alertDialog.dismiss()
                    return@setOnClickListener
                }
            }
            createMarker(p0)
            dict.put(p0.toString(),text)
            addr.put(p0.toString(),address.text.toString())
            content.put(p0.toString(),spinner.getSelectedItem().toString())
            publicPrivate.put(p0.toString(),gender)
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
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
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
                var x = findViewById<NavigationView>(R.id.nav_view).getHeaderView(0)
//                var google_button = x.findViewById<SignInButton>(R.id.google_button)
                var google_button = x.findViewById<Button>(R.id.google_button)
                var imageView = x.findViewById<ImageView>(R.id.imageView)
                var user = x.findViewById<TextView>(R.id.user)
                var email = x.findViewById<TextView>(R.id.email)
                var close = x.findViewById<ImageView>(R.id.close)

                val drawerLayout: FrameLayout = findViewById(R.id.drawer_layout)
                val listLayout: FrameLayout = findViewById(R.id.list_layout)
                val homeLayout: FrameLayout = findViewById(R.id.homeframe)

                //add control if is logged
                drawerLayout.invalidate()
                listLayout.invalidate()

                homeLayout.visibility = View.VISIBLE;
                listLayout.visibility = View.GONE;
                drawerLayout.visibility = View.GONE;

                google_button.visibility = View.GONE

                imageView.visibility = View.VISIBLE

                Picasso.get().load(account?.photoUrl).into(imageView);
                user.visibility = View.VISIBLE
                user.text = account?.displayName

                email.visibility = View.VISIBLE
                email.text = account?.email
                close.visibility = View.VISIBLE
            }
            else if(resultCode == 40){
                println("non loggato")
                var x = findViewById<NavigationView>(R.id.nav_view).getHeaderView(0)
//                var google_button = x.findViewById<SignInButton>(R.id.google_button)
                var google_button = x.findViewById<Button>(R.id.google_button)
                var close = x.findViewById<ImageView>(R.id.close)
                var user = x.findViewById<TextView>(R.id.user)
                var email =x.findViewById<TextView>(R.id.email)
                var imageView =x.findViewById<ImageView>(R.id.imageView)

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
                Toast.makeText(applicationContext, "List da implementare", Toast.LENGTH_LONG).show()
                true
            }
            R.id.help ->{
                Toast.makeText(applicationContext, "help da implementare", Toast.LENGTH_LONG).show()
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
            Place.Field.NAME,Place.Field.LAT_LNG,Place.Field.ADDRESS))

        val layout : LinearLayout = autoCompleteFragment?.getView() as LinearLayout

        val menuIcon: ImageView =  layout.getChildAt(0) as ImageView
        menuIcon?.setImageDrawable(resources.getDrawable(R.drawable.ic_menu))
        val navMenu: NavigationView = findViewById(R.id.nav_view)
        navMenu.setNavigationItemSelectedListener(this)
        menuIcon.setOnClickListener(View.OnClickListener() {

            val drawerLayout: FrameLayout = findViewById(R.id.drawer_layout)
            val listLayout: FrameLayout = findViewById(R.id.list_layout)
            val homeLayout: FrameLayout = findViewById(R.id.homeframe)
            //add control if is logged
            homeLayout.invalidate()
            listLayout.invalidate()

            drawerLayout.visibility = View.VISIBLE;
            listLayout.visibility = View.GONE;
            homeLayout.visibility = View.GONE;
            drawerLayout.startAnimation(mAnimation);
            mAnimation.start();
        })
        autoCompleteFragment?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                println( place)
                var lat = place.latLng
                if (lat != null) {
                    autoCompleteFragment.setText("")
                    getSupportFragmentManager().popBackStack();
                    onMapClick(lat)
                }
            }
            override fun onError(status: Status) {
                Log.d("HOY", "An error occurred: ${status.statusMessage}")
            }
        })
    }

    private fun getAddress(latLng: LatLng): MutableList<Address>? {
        val geocoder = Geocoder(this)
        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        return addresses
    }

    fun createMarker(p0: LatLng){
        val list = getAddress(p0)
        var text = "Indirizzo:" + list?.get(0)?.getAddressLine(0)+"\nGeoLocalita:" +  list?.get(0)?.getLocality() + "\nAdminArea: " + list?.get(0)?.getAdminArea() + "\nCountryName: " + list?.get(0)?.getCountryName()+ "\nPostalCode: " + list?.get(0)?.getPostalCode() + "\nFeatureName: " + list?.get(0)?.getFeatureName();
        var x= mMap.addMarker(
            MarkerOptions()
                .position(p0)
                .title(text)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .alpha(0.7f)
        )

        myList.put(p0.toString(),x)
    }

    fun closeDrawer(view: View) {
        val drawerLayout: FrameLayout = findViewById(R.id.drawer_layout)
        val listLayout: FrameLayout = findViewById(R.id.list_layout)
        val homeLayout: FrameLayout = findViewById(R.id.homeframe)
        //add control if is logged
        drawerLayout.invalidate()
        listLayout.invalidate()

        homeLayout.visibility = View.VISIBLE;
        listLayout.visibility = View.GONE;
        drawerLayout.visibility = View.GONE;
        homeLayout.startAnimation(mAnimation);
        mAnimation.start();
        homeLayout.bringToFront()
    }

    @SuppressLint("WrongViewCast")
    fun showPOI(){
        val len = dict.length()
        var index = 0
        val txt: TextView = findViewById(R.id.nosrc)

        val drawerLayout: FrameLayout = findViewById(R.id.drawer_layout)
        val listLayout: FrameLayout = findViewById(R.id.list_layout)
        val homeLayout: FrameLayout = findViewById(R.id.homeframe)
        //add control if is logged
        homeLayout.invalidate()
        listLayout.invalidate()
        listLayout.visibility = View.VISIBLE;
        homeLayout.visibility = View.GONE;
        drawerLayout.visibility = View.GONE;
        listLayout.startAnimation(mAnimation);
        mAnimation.start();
        listLayout.bringToFront()

        var  lv:ListView = findViewById<ListView>(R.id.lv)
        val userList = MutableList<String>(len,{""})
        if(dict.length() == 0) txt.visibility = View.VISIBLE;
        else txt.visibility = View.INVISIBLE;
            for (i in dict.keys()) {
                userList[index] = dict[i] as String
                index++
            }


        var  arrayAdapter : ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, userList);


        lv.setOnItemLongClickListener { parent, view, position, id ->

            val inflater: LayoutInflater = this.layoutInflater
            val dialogView: View = inflater.inflate(R.layout.dialog_custom_eliminate, null)

            val eliminateBtn: Button = dialogView.findViewById(R.id.eliminateBtn)
            eliminateBtn.setOnClickListener {
                val selectedItem = parent.getItemAtPosition(position) as String
                println(dict)
                for (i in dict.keys()){
                    if(selectedItem == dict[i] as String) {
                        var mark = myList[i] as Marker
                        mark.remove()
                        myList.remove(i)
                        dict.remove(i)
                        addr.remove(i)
                        publicPrivate.remove(i)
                        content.remove(i)
                        showPOI()
                    }
                }
                alertDialog.dismiss()
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
            println(dict)
            for (i in dict.keys()){
                if(selectedItem == dict[i] as String) onMarkerClick(myList[i] as Marker)
            }
        }
        lv.adapter = arrayAdapter;
        account
        }

    /*End Utils Function*/
}



