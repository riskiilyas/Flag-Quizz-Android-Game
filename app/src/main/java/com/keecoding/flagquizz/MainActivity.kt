package com.keecoding.flagquizz

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.keecoding.flagquizz.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mediaPlayer: MediaPlayer
    private var isFromQuiz = false
    private var isMute = false
    private lateinit var sharedPreferences: SharedPreferences
    private val mainSharedPreferencesCode = "MAIN_SHARED_PREFERENCES"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences(mainSharedPreferencesCode, MODE_PRIVATE)

        if (!sharedPreferences.contains("isMuted")){
            sharedPreferences.edit().putBoolean("isMuted", isMute).apply()
        } else {
            isMute = sharedPreferences.getBoolean("isMuted", false)
        }

        binding.btnExit.setOnClickListener { finish() }
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music)

        if (!isMute){
            mediaPlayer.start()
            mediaPlayer.isLooping = true
        } else {
            binding.btnHelp.setImageResource(R.drawable.ic_mute)
        }

        binding.btnPlay.setOnClickListener {
            if (binding.appCompatEditText.text!!.isNotBlank()) {
                if (isNetworkAvailable()) {
                    val quizIntent = Intent(this, QuizActivity::class.java)
                    quizIntent.putExtra("name", binding.appCompatEditText.text.toString())
                    quizIntent.putExtra("music", mediaPlayer.currentPosition)
                    quizIntent.putExtra("isMute", isMute)
                    isFromQuiz = true
                    startActivity(quizIntent)
                } else {
                    Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
                }
            } else {
                binding.appCompatEditText.error = "Fill your name first!"
            }
        }

        binding.btnHelp.setOnClickListener {
            isMute = !isMute
            sharedPreferences.edit().putBoolean("isMuted", isMute).apply()
            updateAudio()
        }
    }

    @SuppressLint("ResourceType")
    override fun onBackPressed() {
        val dialog = Dialog(this)
        dialog.apply {
            setContentView(R.layout.dialog_confirm_exit)
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            findViewById<Button>(R.id.btnPositive).setOnClickListener { finish() }
            findViewById<Button>(R.id.btnNegative).setOnClickListener { dismiss() }
            findViewById<TextView>(R.id.tvConfirm).setTextColor(R.color.statusBarColor)
            show()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)

        return (capabilities != null && capabilities.hasCapability(NET_CAPABILITY_INTERNET))
    }

    private fun updateAudio(){
        if (isMute) {
            binding.btnHelp.setImageResource(R.drawable.ic_mute)
            mediaPlayer.pause()
            mediaPlayer.seekTo(0)
        } else {
            binding.btnHelp.setImageResource(R.drawable.ic_unmute)
            mediaPlayer.start()
        }

    }

    override fun onPause() {
        if (!isMute) mediaPlayer.pause()
        super.onPause()
    }

    override fun onResume() {
        if (!isMute){
            if (isFromQuiz) mediaPlayer.seekTo(0)
            mediaPlayer.start()
        }
        super.onResume()
    }

}