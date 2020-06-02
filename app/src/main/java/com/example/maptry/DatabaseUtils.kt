package com.example.maptry

import android.util.Log
import com.example.maptry.MapsActivity.Companion.drawed
import com.example.maptry.MapsActivity.Companion.myList
import com.example.maptry.MapsActivity.Companion.myjson
import com.example.maptry.MapsActivity.Companion.mymarker
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import org.json.JSONObject

/*Start Database Function*/
fun writeNewPOI(userId: String, name:String, addr:String, cont:String, type:String, marker: Marker, url:String, phone:String) {
    val user = UserMarker(name,addr,cont,type,marker.position.latitude.toString(),marker.position.longitude.toString(),url,phone)
    MapsActivity.db.collection("user").document(userId).collection("marker").add(user).addOnSuccessListener {
        Log.d("TAG", "success")
    }
        .addOnFailureListener { ex : Exception ->
            Log.d("TAG", ex.toString())

        }
}

fun createFriendList(id:String){
    var count = 0
    MapsActivity.db.collection("user").document(id).collection("friend")
        .addSnapshotListener { querySnapshot, firebaseFirestoreException ->

            if (firebaseFirestoreException != null) {
                Log.w("TAG", "Listen failed.", firebaseFirestoreException)
                return@addSnapshotListener
            }

            if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                MapsActivity.dataFromfirestore = querySnapshot.documents

                Log.d("TAGcreatefriendlist", "Current data: ${querySnapshot.documents}")
                querySnapshot.documents.forEach { child ->
                    child.data?.forEach { chi ->
                        println(chi.key)
                        println(chi.value)
                        MapsActivity.friendJson.put(count.toString(),chi.value)
                        count++

                    }
                }

            }
        }
}

fun createPoiList(id:String){
    MapsActivity.db.collection("user").document(id).collection("marker")
        .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            println("SONO ENTRATO IN MARKER FIREBASE")
            if (firebaseFirestoreException != null) {
                Log.w("TAG", "Listen failed.", firebaseFirestoreException)
//                        return@addSnapshotListener
            }

            if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                MapsActivity.dataFromfirestore = querySnapshot.documents

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