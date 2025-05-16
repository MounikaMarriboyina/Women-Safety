package com.project.womensafety.presentationLayer.commonView

import android.app.Activity
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

fun Fragment.showToast(message:Any?)= Toast.makeText(requireContext(),"$message",Toast.LENGTH_SHORT).show()
fun Activity.showToast(message:Any?)= Toast.makeText(this,"$message",Toast.LENGTH_SHORT).show()