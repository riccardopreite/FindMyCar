package com.example.maptry

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.PendingIntent.getActivity
import android.content.ClipData
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*

import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import co.metalab.asyncawait.async
import com.example.maptry.R.id
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places;
import com.google.android.gms.location.*
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Tasks.await
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.cutsom_dialog.view.*
import kotlinx.android.synthetic.main.dialog_custom_view.*
import kotlinx.android.synthetic.main.marker_info_window.*
import org.json.JSONObject

import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.util.*
import java.util.Arrays.*

@Suppress("DEPRECATION")
class MapsActivity  : AppCompatActivity(), OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    // 2
    private lateinit var locationRequest: LocationRequest


    private val myList = JSONObject() // marker
    private val dict = JSONObject() //nome
    private val addr = JSONObject() //address
    private val content = JSONObject() //contenuto
    private val publicPrivate = JSONObject() //tipo
    private var locationUpdateState = false
    private var zoom = 1
    private lateinit var alertDialog: AlertDialog

    @SuppressLint("ResourceType")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }
    @SuppressLint("ResourceType")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return true
    }
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1

        private const val REQUEST_CHECK_SETTINGS = 2

        private const val PLACE_PICKER_REQUEST = 3
    }

    private fun setUpMap() {
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        mMap.isMyLocationEnabled = true

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
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
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

    fun setUpSearch() {

        val autoCompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as? AutocompleteSupportFragment
        autoCompleteFragment?.setCountry("IT")
        autoCompleteFragment?.setPlaceFields(asList(Place.Field.ID,
            Place.Field.NAME,Place.Field.LAT_LNG,Place.Field.ADDRESS))

        autoCompleteFragment?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                println( place)
                if(place != null){
                    var lat = place.latLng
                    if (lat != null) {
                        getSupportFragmentManager().popBackStack();
                        onMapClick(lat)
                    }
                }
            }

            override fun onError(status: Status) {
                Log.d("HOY", "An error occurred: ${status.statusMessage}")
            }

        })
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.getUiSettings().setZoomControlsEnabled(true)
        mMap.setOnMarkerClickListener(this)

        mMap.setOnMapClickListener(this)
        setUpMap()
        setUpSearch()
    }

    private fun getAddress(latLng: LatLng): MutableList<Address>? {
        val geocoder = Geocoder(this)
        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        return addresses
    }


    override fun onMarkerClick(p0: Marker): Boolean {

        val builder = AlertDialog.Builder(this)
        val x = LatLng(lastLocation.latitude,lastLocation.longitude)

        if(p0.position == x) builder.setTitle("La tua posizione")
        else builder.setTitle("cliccato su " + p0.title)
        val list = getAddress(p0.position)
        var text = "Indirizzo:" + list?.get(0)?.getAddressLine(0)+"\nCitta:" +  list?.get(0)?.getLocality() + "\nRegione: " + list?.get(0)?.getAdminArea() + "\nStato: " + list?.get(0)?.getCountryName()+ "\nCAP: " + list?.get(0)?.getPostalCode()
        if(p0.position == x) builder.setMessage("Sei Qui")
        else builder.setMessage(dict.getString(p0.position.toString())
                                +"\n"+addr.getString(p0.position.toString())
                                +"\n"+content.getString(p0.position.toString())
                                +"\n"+publicPrivate.getString(p0.position.toString()))


        builder.setPositiveButton("Close") { dialog, which ->

        }
        builder.show()
        return false
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

    override fun onMapClick(p0: LatLng) {

        val inflater: LayoutInflater = this.getLayoutInflater()
        val dialogView: View = inflater.inflate(R.layout.dialog_custom_view, null)

        val header_txt = dialogView.findViewById<TextView>(id.header)

        val spinner: Spinner = dialogView.findViewById(id.planets_spinner)
        var lname : EditText = dialogView.findViewById(id.txt_lname)
        var address :  TextView = dialogView.findViewById(id.txt_address)
        var publicButton: RadioButton = dialogView.findViewById(id.rb_public)
        var privateButton: RadioButton = dialogView.findViewById(id.rb_private)
        // Create an ArrayAdapter using the string array and a default spinner layout
        header_txt.text = "Stai aggiungendo un POI"
        spinner.onItemSelectedListener = SpinnerActivity()


            address.isEnabled = false
            var txt = getAddress(p0)
            address.setText(txt?.get(0)?.getAddressLine(0))

        ArrayAdapter.createFromResource(
            this,
            R.array.planets_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }
        val custom_button: Button = dialogView.findViewById(id.customBtn)
        custom_button.setOnClickListener {
            var text = ""
            var gender = "gen"
            createMarker(p0)
                if(publicButton.isChecked)
                    gender = publicButton.text.toString()
                if (privateButton.isChecked)
                    gender = privateButton.text.toString()



                text  = lname.text.toString()
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
//        alertDialog.window!!.getAttributes().windowAnimations = R.style.PauseDialogAnimation
        alertDialog.show()
    }


    private fun startLocationUpdates() {
        //1
        if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        //2
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }

    private fun createLocationRequest() {
        // 1
        locationRequest = LocationRequest()
        // 2
        locationRequest.interval = 10000
        // 3
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

        // 4
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        // 5
        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            // 6
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(this@MapsActivity,
                            REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
        if (requestCode == 1) {
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
    }

    // 2
    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // 3
    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }

}

