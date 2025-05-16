package com.project.womensafety.presentationLayer.commonView

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.textfield.TextInputEditText
import com.project.womensafety.R
import com.project.womensafety.dataLayer.interaction.InteractionView
import com.project.womensafety.databinding.SignUpBinding
import com.project.womensafety.responsiveLayer.RetrofitC
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class SignUp : Fragment() {
   private val bind by lazy {
      SignUpBinding.inflate(layoutInflater)
   }
   private val fused by lazy {
      LocationServices.getFusedLocationProviderClient(requireActivity())
   }

   private val p by lazy {
      CommonClass(requireContext()).p
   }

   private val interactionView by lazy {
      (activity as InteractionView)
   }





   @SuppressLint("ResourceAsColor")
   override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
   ): View {
      requireActivity().window.statusBarColor= requireContext().getColor( R.color.thick)

      bind.loginBtn.setOnClickListener {
         bind.errorShower.text = ""
         val name = bind.name.text.toString().trim()
         val mobile = bind.mobile.text.toString().trim()
         val password = bind.password.text.toString().trim()
         val command = bind.sos.text.toString().trim()
         if (name.isEmpty()) {
            requestFocus(inputEditText = bind.name, "Please enter your Name")
         } else if (mobile.isEmpty()) {
            requestFocus(inputEditText = bind.mobile, "Please enter your Mobile number")
         } else if (password.isEmpty()) {
            requestFocus(inputEditText = bind.password, "Please enter your Password")
         } else if (command.isEmpty()) {
            requestFocus(inputEditText = bind.sos, "Please enter your Safe word")
         }else if (mobile.length != 10) {
            requestFocus(inputEditText = bind.mobile, "Please enter a valid mobile number")
         } else {
            showMeWhereAreYou()
         }

      }


      bind.loginPage.setOnClickListener {
         interactionView.changeView(string = "Changer")
      }

      return bind.root
   }

   private fun showMeWhereAreYou() {
      if (ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
         ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
         ) != PackageManager.PERMISSION_GRANTED
      ) {
         requireActivity().requestPermissions(
            arrayOf(
               Manifest.permission.ACCESS_FINE_LOCATION,
               Manifest.permission.ACCESS_COARSE_LOCATION
            ), 2
         )
         return
      }
      p.show()
      fused.lastLocation.addOnSuccessListener { it ->
         if(it==null){
            p.dismiss()
         }
         it?.let {

            CoroutineScope(IO).async {
               async {
                  try {
                     RetrofitC.api.users(
                        name = bind.name.text.toString(),
                        mobile = bind.mobile.text.toString(),
                        password = bind.password.text.toString(),
                        location = "${it.latitude},${it.longitude}",
                        command = bind.sos.text.toString()
                     )
                  } catch (e: Exception) {
                     withContext(Main) {
                        p.dismiss()
                        bind.errorShower.text == e.message
                     }
                     null
                  }
               }.await().let {
                  withContext(Main) {
                     it?.body()?.message?.let {
                        showToast(it)
                        if (it == "Success") {
                           interactionView.changeView(string = "Changer")
                        }
                     }
                     p.dismiss()
                  }
               }
            }.start()
         }
      }.addOnFailureListener {
         p.dismiss()
         bind.errorShower.text = it.message
      }

   }


   private fun requestFocus(inputEditText: TextInputEditText, string: String) {
      inputEditText.requestFocus()
      bind.errorShower.text = string
      requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_MODE_CHANGED)
   }
}