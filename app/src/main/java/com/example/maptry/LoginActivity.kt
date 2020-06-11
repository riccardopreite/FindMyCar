package com.example.maptry
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import com.example.maptry.MapsActivity.Companion.firebaseAuth

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    //Login
    val RC_SIGN_IN: Int = 1
     lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var mGoogleSignInOptions: GoogleSignInOptions

    private var account: GoogleSignInAccount? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        println("IN LOGIN ACTIVITY")
        setContentView(R.layout.nav_header_navigation)
//        var google_button = findViewById<SignInButton>(R.id.google_button)
        var google_button = findViewById<Button>(R.id.google_button)
        var imageView = findViewById<ImageView>(R.id.imageView)
        var user = findViewById<TextView>(R.id.user)
        var email = findViewById<TextView>(R.id.email)
        var close = findViewById<ImageView>(R.id.close)
//      var google_button = x.findViewById<SignInButton>(R.id.google_button)
        firebaseAuth = FirebaseAuth.getInstance()


        google_button.visibility = View.VISIBLE
        imageView.visibility = View.GONE
        user.visibility = View.GONE
        email.visibility = View.GONE
        close.visibility = View.GONE
        configureGoogleSignIn()
    }
    private fun configureGoogleSignIn() {
        mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, mGoogleSignInOptions)
         account = GoogleSignIn.getLastSignedInAccount(this)
        updateUI(account);
//        findViewById<SignInButton>(R.id.google_button).setOnClickListener { signIn() }

        findViewById<Button>(R.id.google_button).setOnClickListener { signIn() }
    }


    /*Start SignIn Function*/
    fun signIn() {
        var signInIntent : Intent = mGoogleSignInClient.signInIntent;
        println("IN SIGNINNNNNN")
        println(signInIntent)
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private fun updateUI( account: GoogleSignInAccount?){
        if(account != null){
//            var google_button = findViewById<SignInButton>(R.id.google_button)
//            var imageView = findViewById<ImageView>(R.id.imageView)
//            var user = findViewById<TextView>(R.id.user)
//            var email =findViewById<TextView>(R.id.email)
//            google_button.visibility = View.GONE
//
//            imageView.visibility = View.VISIBLE
////            imageView.setImageIcon(account.photoUrl as Icon)
//
//            user.visibility = View.VISIBLE
//            user.text = account.displayName
//
//            email.visibility = View.VISIBLE
//            email.text = account.email
            var data : Intent = Intent();

            data.data = Uri.parse("done");
            setResult(50, data);
            //---close the activity---
//            finish();

        }else {
            var data : Intent = Intent();

            data.data = Uri.parse("Not logged");
            setResult(60, data);
//            var google_button = findViewById<SignInButton>(R.id.google_button)
//            var imageView = findViewById<ImageView>(R.id.imageView)
//            var user = findViewById<TextView>(R.id.user)
//            var email =findViewById<TextView>(R.id.email)
//            google_button.visibility = View.VISIBLE
//            imageView.visibility = View.GONE
//            user.visibility = View.GONE
//            email.visibility = View.GONE
        }
    }

    private fun handleSignInResult( completedTask:Task<GoogleSignInAccount>) {
        try {
            account  = completedTask.getResult(ApiException::class.java)
            account?.let { firebaseAuthWithGoogle(it) }
            // Signed in successfully, show authenticated UI.
            account?.let { updateUI(it) };
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("INFAIL", "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                this.finish()

            } else {
                Toast.makeText(this, "Google sign in failed3:(", Toast.LENGTH_LONG).show()
            }
        }
    }

/*End SignIn Function*/

    public final fun get(): GoogleSignInAccount? {
        return this.account
    }

/*Start Override Function*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            var task:Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    override fun onBackPressed() { }
/*End Override Function*/
}