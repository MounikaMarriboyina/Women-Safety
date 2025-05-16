package com.project.womensafety.presentationLayer.user

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.project.womensafety.R
import com.project.womensafety.chatbot.ChatActivity
import com.project.womensafety.databinding.UserMainBinding
import com.project.womensafety.presentationLayer.MainActivity
import com.project.womensafety.presentationLayer.commonView.showToast
import com.project.womensafety.responsiveLayer.RetrofitC
import com.project.womensafety.responsiveLayer.models.User
import kotlinx.coroutines.*
import java.util.*

class UserMainActivity : AppCompatActivity(), OnMapReadyCallback {

   private lateinit var mMap: GoogleMap
   private lateinit var binding: UserMainBinding
   private lateinit var tts: TextToSpeech
   private lateinit var speechRecognizer: SpeechRecognizer

   private var isListening = false
   private var latLng: LatLng? = null
   private var arrayUser = ArrayList<User>()

   private val shared by lazy { getSharedPreferences("user", MODE_PRIVATE) }
   private val fused by lazy { LocationServices.getFusedLocationProviderClient(this) }

   private val locationPermissions = arrayOf(
      Manifest.permission.ACCESS_COARSE_LOCATION,
      Manifest.permission.ACCESS_FINE_LOCATION
   )

   private var command = ""

   private val requestPermissionLauncher = registerForActivityResult(
      ActivityResultContracts.RequestMultiplePermissions()
   ) { permissions ->
      if (permissions.all { it.value }) {
         runMyCurrentLocation()
      } else {
         Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
      }
   }

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      binding = UserMainBinding.inflate(layoutInflater)
      setContentView(binding.root)

      command = shared.getString("command", "") ?: ""
      showToast(command)

      initSpeechRecognizer()
      initTextToSpeech()

      binding.micintialise.setOnClickListener {
         if (!tts.isSpeaking) {
            if (isListening) stopListening() else startListening()
         }
      }

      binding.chatbot.setOnClickListener {
         startActivity(Intent(this, ChatActivity::class.java))
      }

      binding.floatingActionButton.setOnClickListener {
         startActivity(Intent(this, TheMainActivity::class.java).apply {
            putExtra("data", arrayUser)
         })
      }

      binding.logout.setOnClickListener {
         showLogoutDialog()
      }

      val mapFragment = supportFragmentManager
         .findFragmentById(R.id.map) as? SupportMapFragment
      mapFragment?.getMapAsync(this)
   }

   private fun initSpeechRecognizer() {
      speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
      speechRecognizer.setRecognitionListener(SpeechRecognitionListener())
   }

   private fun initTextToSpeech() {
      tts = TextToSpeech(this) {
         if (it == TextToSpeech.SUCCESS) {
            tts.language = Locale.getDefault()
            tts.setSpeechRate(1f)
            speak("Welcome to Women Safety App.")
         } else {
            showToast("Text-to-Speech initialization failed.")
         }
      }
   }

   private fun speak(text: String) {
      if (text.isNotEmpty()) {
         tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
      }
   }

   private fun startListening() {
      isListening = true
      speechRecognizer.startListening(getSpeechRecognizerIntent())
      speak("Listening")
   }

   private fun stopListening() {
      isListening = false
      speechRecognizer.stopListening()
      speak("Listening stopped")
   }

   private fun getSpeechRecognizerIntent(): Intent {
      return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
         putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
         putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
      }
   }

   private fun showLogoutDialog() {
      MaterialAlertDialogBuilder(this).apply {
         setTitle("Do you want to Logout?")
         setPositiveButton("Yes") { dialog, _ ->
            shared.edit().clear().apply()
            startActivity(Intent(this@UserMainActivity, MainActivity::class.java))
            finishAffinity()
            dialog.dismiss()
         }
         setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
         show()
      }
   }

   override fun onMapReady(googleMap: GoogleMap) {
      mMap = googleMap
      if (hasLocationPermission()) {
         runMyCurrentLocation()
      } else {
         requestPermissionLauncher.launch(locationPermissions)
      }

      CoroutineScope(Dispatchers.IO).launch {
         try {
            val response = RetrofitC.api.getLocation("getLocations")
            response.body()?.data?.let { users ->
               withContext(Dispatchers.Main) {
                  setNearbyUsers(users)
               }
            }
         } catch (e: Exception) {
            withContext(Dispatchers.Main) {
               showToast("Failed to fetch locations")
            }
         }
      }
   }

   @SuppressLint("MissingPermission")
   private fun runMyCurrentLocation() {
      fused.lastLocation.addOnSuccessListener { location ->
         location?.let {
            latLng = LatLng(it.latitude, it.longitude)
            mMap.addMarker(
               MarkerOptions().position(latLng!!).title("Current Location")
                  .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))
            )
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng!!, 16f))
         }
      }
   }

   private fun hasLocationPermission(): Boolean {
      return locationPermissions.all {
         ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
      }
   }

   private fun setNearbyUsers(users: List<User>) {
      latLng?.let { currentLatLng ->
         val currentLocation = Location("current").apply {
            latitude = currentLatLng.latitude
            longitude = currentLatLng.longitude
         }

         users.forEach { user ->
            user.location?.split(",")?.takeIf { it.size == 2 }?.let { (lat, lng) ->
               val userLocation = Location("user").apply {
                  latitude = lat.toDouble()
                  longitude = lng.toDouble()
               }
               val distance = currentLocation.distanceTo(userLocation) / 1000
               if (distance < 50) {
                  val markerLatLng = LatLng(lat.toDouble(), lng.toDouble())
                  mMap.addMarker(
                     MarkerOptions().position(markerLatLng)
                        .title(user.name)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin2))
                  )
                  arrayUser.add(user)
               }
            }
         }
      }
   }

   @SuppressLint("MissingPermission")
   private fun sendSMS(user: User) {
      fused.lastLocation.addOnSuccessListener { location ->
         location?.let {
            val message = "Here is my location \nhttp://maps.google.com/maps?daddr=${it.latitude},${it.longitude}"
            SmsManager.getDefault().sendTextMessage(user.mobile, null, message, null, null)
         }
      }
   }

   private inner class SpeechRecognitionListener : RecognitionListener {
      override fun onReadyForSpeech(params: Bundle?) {}
      override fun onBeginningOfSpeech() {}
      override fun onRmsChanged(rmsdB: Float) {}
      override fun onBufferReceived(buffer: ByteArray?) {}
      override fun onEndOfSpeech() {}
      override fun onError(error: Int) {}
      override fun onResults(results: Bundle?) {
         val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
         matches?.firstOrNull()?.let { result ->
            if (result.contains(command, ignoreCase = true)) {
               arrayUser.forEach { sendSMS(it) }
               speak("Sos Sent Successfully")
            }
         } ?: showToast("No speech recognized")
      }

      override fun onPartialResults(partialResults: Bundle?) {}
      override fun onEvent(eventType: Int, params: Bundle?) {}
   }

   override fun onDestroy() {
      tts.stop()
      tts.shutdown()
      speechRecognizer.destroy()
      super.onDestroy()
   }
}
