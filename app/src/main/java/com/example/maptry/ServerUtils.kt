package com.example.maptry

import com.example.maptry.MapsActivity.Companion.ip
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.net.URLEncoder


/*Start Server Function*/

fun resetTimerAuto(car:JSONObject){
     var url = URL("http://"+ip+":3000/reminderAuto?"+ URLEncoder.encode("owner", "UTF-8") + "=" + URLEncoder.encode(car.get("owner") as String, "UTF-8")+"&"+ URLEncoder.encode("timer", "UTF-8") + "=" + URLEncoder.encode(car.get("timer").toString(), "UTF-8")+"&"+ URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(car.get("name") as String, "UTF-8")+"&"+ URLEncoder.encode("addr", "UTF-8") + "=" + URLEncoder.encode(car.get("addr") as String, "UTF-8"))

    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: okhttp3.Call, e: IOException) {
            println("something went wrong")
        }

        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
            println(response.body()?.string())
        }
    })
}

fun reminderAuto(car:JSONObject){
    var url = URL("http://"+ip+":3000/reminderAuto?"+ URLEncoder.encode("owner", "UTF-8") + "=" + URLEncoder.encode(car.get("owner") as String, "UTF-8")+"&"+ URLEncoder.encode("timer", "UTF-8") + "=" + URLEncoder.encode(car.get("timer").toString(), "UTF-8")+"&"+ URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(car.get("name") as String, "UTF-8")+"&"+ URLEncoder.encode("addr", "UTF-8") + "=" + URLEncoder.encode(car.get("addr") as String, "UTF-8"))

    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: okhttp3.Call, e: IOException) {
            println("something went wrong")
        }

        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
            println(response.body()?.string())
        }
    })
}

fun getPoiFromFriend(friend:String):JSONObject{
    println("IN GET POI")
    var url = URL("http://"+ip+":3000/getPoiFromFriend?"+ URLEncoder.encode("friend", "UTF-8") + "=" + URLEncoder.encode(friend, "UTF-8"))
    //var url = URL("http://192.168.43.76:3000/getPoiFromFriend?"+ URLEncoder.encode("friend", "UTF-8") + "=" + URLEncoder.encode(friend, "UTF-8"))
    var result = JSONObject()
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: okhttp3.Call, e: IOException) {
            println("something went wrong")
        }

        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {

            val x:String = response.body()?.string()!!
            result = JSONObject(x)
        }
    })
    return result
}

fun confirmFriend(sender:String,receiver:String){
    var url = URL("http://"+ip+":3000/confirmFriend?"+ URLEncoder.encode("receiver", "UTF-8") + "=" + URLEncoder.encode(receiver, "UTF-8")+"&"+ URLEncoder.encode("sender", "UTF-8") + "=" + URLEncoder.encode(sender, "UTF-8"))

    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: okhttp3.Call, e: IOException) {
            println("something went wrong")
        }

        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
            println(response.body()?.string())
        }
    })
}

fun removeFriend(sender:String,receiver:String){
    var url = URL("http://"+ip+":3000/removeFriend?"+ URLEncoder.encode("receiver", "UTF-8") + "=" + URLEncoder.encode(receiver, "UTF-8")+"&"+ URLEncoder.encode("sender", "UTF-8") + "=" + URLEncoder.encode(sender, "UTF-8"))

    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: okhttp3.Call, e: IOException) {
            println("something went wrong")
        }

        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
            println(response.body()?.string())
        }
    })
}

fun sendFriendRequest(username:String,sender:String){
    var url = URL("http://"+ip+":3000/addFriend?"+ URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8")+"&"+ URLEncoder.encode("sender", "UTF-8") + "=" + URLEncoder.encode(sender, "UTF-8"))
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {println("something went wrong")}
        override fun onResponse(call: Call, response: Response) = println(response.body()?.string())
    })
}
fun startLive(live:JSONObject){
    var url = URL("http://"+ip+":3000/startLive?"+ URLEncoder.encode("owner", "UTF-8") + "=" + URLEncoder.encode(live.get("owner") as String, "UTF-8")+"&"+ URLEncoder.encode("timer", "UTF-8") + "=" + URLEncoder.encode(live.get("timer").toString(), "UTF-8")+"&"+ URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(live.get("name") as String, "UTF-8")+"&"+ URLEncoder.encode("addr", "UTF-8") + "=" + URLEncoder.encode(live.get("addr") as String, "UTF-8"))
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: okhttp3.Call, e: IOException) {
            println("something went wrong")
        }

        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
            println(response.body()?.string())
        }
    })
}

/*End Server Function*/