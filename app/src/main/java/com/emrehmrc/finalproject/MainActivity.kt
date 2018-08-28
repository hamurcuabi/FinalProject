package com.emrehmrc.finalproject

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ArrayAdapter
import com.emrehmrc.finalproject.class_model.Users
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_profil_setting.*


class MainActivity : AppCompatActivity() {

    lateinit var mAuthState: FirebaseAuth.AuthStateListener
    private lateinit var mInterstitialAd: InterstitialAd
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
        initMAuthState()
        getPending()
        fillListBilgi()

        imgProfilGo.setOnClickListener {
            var intent = Intent(this@MainActivity, ProfilSettingActivity::class.java)
            startActivity(intent)
        }
        imgForum.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                var intent = Intent(this@MainActivity, ChatRoomsActivity::class.java)
                startActivity(intent)
            }

        })
        imgExit.setOnClickListener {
            if (mInterstitialAd.isLoaded) {

                mInterstitialAd.show()
            } else {
                signOut()
            }


        }
        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        // ca-app-pub-6791508794346575~5154653448
        //Admob Denemesi
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713")
        var adRequest = AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build()
        adView.loadAd(adRequest)
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"
        var adRequestFullBanner = AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build()
        mInterstitialAd.loadAd(adRequestFullBanner)
        mInterstitialAd.adListener = object : AdListener() {


            override fun onAdClosed() {
                signOut()
                mInterstitialAd.loadAd(adRequestFullBanner)
            }

        }


    }

    private fun fillListBilgi() {
        val rules = arrayOf("\"NoticeMe\" bir bildirim sistemidir.", "İlgi " + "duyduğunuz " +
                "odalara " +
                "tıklayarak giriş yapabilirsiniz.", "Giriş yapılan her odadan bildirim alacaksınız" +
                ".", "Odaların içindeki sessiz butonuna tıklayarak bildirimleri kapatabilirsiniz" +
                ".", "Kullanıcıların hiçbir bilgisi SATILMAYACAKTIR!")

        val veriAdaptoru = ArrayAdapter<String>(this, R.layout.bilgilendirme,
                R.id.txtBilgilendirme, rules)
        lstBilgi.setAdapter(veriAdaptoru)

    }

    private fun getPending() {
        var pendinIntent = intent
        if (pendinIntent.hasExtra("chatRoomId")) {


            var intent = Intent(this, ChatRoomInActivity::class.java)
            intent.putExtra("chatRoomId", pendinIntent.getStringExtra("chatRoomId"))
            startActivity(intent)
        }
    }

    private fun getUserInfos() {
        var user = FirebaseAuth.getInstance().currentUser
        var referans = FirebaseDatabase.getInstance().reference
        var query = referans.child("users")
                .orderByKey()//orderbyChild olabilir yine
                .equalTo(user?.uid)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {

                for (singleSnapshot in p0!!.children) {
                    var cameUser = singleSnapshot.getValue(Users::class.java)
                    tvUserName.text = cameUser?.name

                }

            }

        })
        tvUserMail.text = user?.email

    }

    private fun initMAuthState() {

        mAuthState = object : FirebaseAuth.AuthStateListener {
            override fun onAuthStateChanged(p0: FirebaseAuth) {

                var user = p0.currentUser
                if (user != null) {//Kullanıcı Girişi Var mı ?


                } else {

                    backToLogin()

                }

            }
        }

    }


    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

    override fun onResume() {

        var user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            backToLogin()
        } else {
            getUserInfos()
        }
        super.onResume()
    }

    private fun backToLogin() {

        var intent = Intent(this@MainActivity, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }


    override fun onStart() {
        FirebaseAuth.getInstance().addAuthStateListener(mAuthState)
        super.onStart()

    }

    override fun onStop() {
        FirebaseAuth.getInstance().removeAuthStateListener(mAuthState)
        super.onStop()

    }
}
