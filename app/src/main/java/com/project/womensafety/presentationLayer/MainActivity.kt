package com.project.womensafety.presentationLayer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.project.womensafety.R
import com.project.womensafety.databinding.ActivityMainBinding
import com.project.womensafety.databinding.LoginActivityBinding
import com.project.womensafety.presentationLayer.commonView.Login
import com.project.womensafety.presentationLayer.user.UserMainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
   private val bind by lazy {
      ActivityMainBinding.inflate(layoutInflater)
   }

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContentView(bind.root)
      val type = getSharedPreferences("user", MODE_PRIVATE)
         .getString("mobile", "")
      bind.imageView.apply {
         alpha = 0f
         animate().setDuration(1000).alpha(1f).withEndAction {
               finish()

            if(type!=null){
               if(type.isNotEmpty()){
                  startActivity(Intent(this@MainActivity,UserMainActivity::class.java))
               }else{
                  startActivity(Intent(this@MainActivity, Login::class.java))
               }
            }else{
               startActivity(Intent(this@MainActivity, Login::class.java))
            }
         }.start()
      }
   }
}