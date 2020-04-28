package com.example.maptry

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.nkzawa.socketio.client.IO
import java.net.URISyntaxException

private var socket : com.github.nkzawa.socketio.client.Socket? = null
class ServerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("CREATE SEREVERRRRRRRRRRRRRR")
        socket = IO.socket("http://192.168.56.1:3000");
        socket?.connect()
        try {
            //if you are using a phone device you should connect to same local network as your laptop and disable your pubic firewall as well


            // emit the event join along side with the nickname
            socket?.emit("add", "id");
            var data : Intent = Intent();

            data.data = Uri.parse("done");
            setResult(60, data);
            //---close the activity---
            finish();
        } catch (e: URISyntaxException) {
            var data : Intent = Intent();

            data.data = Uri.parse("Problem");
            setResult(70, data);
            e.printStackTrace();
        }
    }



/*End SignIn Function*/


/*Start Override Function*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data);


    }
    override fun onBackPressed() { }
/*End Override Function*/
}