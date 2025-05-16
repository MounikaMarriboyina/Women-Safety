package com.project.womensafety.chatbot

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.project.womensafety.R
import com.project.womensafety.databinding.ActivityChatBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private val binding by lazy { ActivityChatBinding.inflate(layoutInflater) }
    private var bitmap: Bitmap? = null
    private val responseData = arrayListOf<DataResponse>()
    private lateinit var adapter: GeminiAdapter

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                binding.selectIv.setImageURI(it)
            } ?: Log.d("Photopicker", "No media selected")
        }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupRecyclerView()

        binding.selectIv.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.askButton.setOnClickListener {
            handleUserQuery()
        }
    }

    private fun setupRecyclerView() {
        adapter = GeminiAdapter(this, responseData)
        binding.recyclerViewId.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = this@ChatActivity.adapter
        }
    }

    private fun handleUserQuery() {
        val userQuery = binding.askEditText.text.toString()
        if (userQuery.isBlank()) {
            Toast.makeText(
                this@ChatActivity,
                "Kindly provide any input to SuperBot",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        binding.askEditText.setText("")
        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = getString(R.string.apikey2)
        )

        val chasePrompt = """
            You are a women’s safety chatbot for India, designed to assist women in staying safe and accessing help. Your goal is to provide practical safety advice, emotional support, and emergency resources tailored to India. 
            - For safety queries, offer actionable tips (e.g., how to stay safe in public, deal with harassment, or find help).
            - If an image is uploaded, analyze it for potential safety risks (e.g., isolated area, suspicious situation) and suggest precautions.
            - Include these emergency helplines when relevant:
              - National Emergency Number: 112
              - Women Helpline: 181
              - Police: 100
              - National Commission for Women (NCW) 24x7 Helpline: 7827170170
              - Child Helpline (if applicable): 1098
            - If the query involves violence or distress, encourage contacting local police or One Stop Centres (Sakhi Centres).
            - If unclear, ask clarifying questions politely.
            - Keep responses concise, empathetic, and empowering, considering Indian cultural and social contexts.
       
        """.trimIndent()

        val completePrompt = "$chasePrompt\nUser Query: $userQuery"
        val imageTag =
            bitmap?.let { binding.selectIv.tag?.toString() ?: "default_tag" } ?: "no_image"

        responseData.add(DataResponse(0, userQuery, imageTag))
        adapter.notifyDataSetChanged()
        binding.recyclerViewId.scrollToPosition(responseData.size - 1)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputContent = if (bitmap != null) {
                    content {
                        image(bitmap!!)
                        text(completePrompt)
                    }
                } else {
                    content {
                        text(completePrompt)
                    }
                }

                val response = generativeModel.generateContent(inputContent)
                runOnUiThread {
                    responseData.add(DataResponse(1, response.text ?: "I'm here to assist!", ""))
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Log.e("ChatActivity", "Error generating content: ${e.localizedMessage}", e)
                runOnUiThread {
                    Toast.makeText(
                        this@ChatActivity,
                        "Failed to generate response. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
