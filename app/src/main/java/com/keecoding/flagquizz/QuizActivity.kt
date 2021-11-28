package com.keecoding.flagquizz

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.keecoding.flagquizz.databinding.ActivityQuizBinding
import java.io.IOException
import java.io.InputStream
import java.net.URL

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding
    private lateinit var btnChoices: Array<Button>
    private lateinit var quest: String
    private var isOver = false
    private var questionNow = 1
    private var score = 0
    private lateinit var name: String
    private lateinit var mediaPlayer: MediaPlayer
    private var isMute = false
    private var quizHandler = Handler(Looper.getMainLooper())
    private lateinit var sfxCorrect: MediaPlayer
    private lateinit var sfxWrong: MediaPlayer
    private var currentTime : Long = 0
    private var isRunning = true

    private val btnRes = arrayOf(
        R.drawable.btn_choice_a_style,
        R.drawable.btn_choice_b_style,
        R.drawable.btn_choice_c_style,
        R.drawable.btn_choice_d_style
    )

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mediaPlayer = MediaPlayer.create(this, R.raw.background_music)

        name = intent.getStringExtra("name").toString()
        isMute = intent.getBooleanExtra("isMute", false)
        if (!isMute) {
            mediaPlayer.seekTo(intent.getIntExtra("music", 0))
            mediaPlayer.start()
            mediaPlayer.isLooping = true
        }

        btnChoices = arrayOf(
            binding.btnChoiceA,
            binding.btnChoiceB,
            binding.btnChoiceC,
            binding.btnChoiceD
        )

        sfxCorrect = MediaPlayer.create(this, R.raw.sfx_correct)
        sfxWrong = MediaPlayer.create(this, R.raw.sfx_wrong)

        binding.btnSubmit.setBackgroundResource(R.drawable.btn_un_next)
        generateQuest()

        btnChoices.forEach { btn ->
            btn.setOnClickListener {
                pauseTimer()
                if (!isOver) {
                    if (btn.text == CountryCodes.COUNTRY_MAP[quest]) {
                        it.setBackgroundResource(R.drawable.btn_choice_correct)
                        sfxCorrect.start()
                        score++
                    } else {
                        sfxWrong.start()
                        it.setBackgroundResource(R.drawable.btn_choice_wrong)
                        btnChoices.forEach { ans ->
                            if (ans.text == CountryCodes.COUNTRY_MAP[quest]) {
                                ans.setBackgroundResource(R.drawable.btn_choice_correct)
                            }
                        }
                    }
                    binding.btnSubmit.setBackgroundResource(R.drawable.btn_play_style)
                    isOver = true
                }
            }
        }

        binding.btnSubmit.setOnClickListener {
            if (isOver) {
                if (questionNow == 10) {
                    showResult()
                    return@setOnClickListener
                } else {
                    startTimer()
                    if (isNetworkAvailable()) {
                        generateQuest()
                        isOver = false
                        questionNow++
                        binding.tvQuestionNumber.text = "Question $questionNow of 10"
                        if (questionNow == 10) binding.btnSubmit.text = "Finish"
                    } else {
                        Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show()
                    }
                }
                binding.tvQuestionNumber.contentDescription = "Question $questionNow of 10"
                binding.btnSubmit.setBackgroundResource(R.drawable.btn_un_next)
                binding.btnSubmit.isPressed = true
            }

        }
        binding.btnBackToMenu.setOnClickListener { onBackPressed() }
    }

    private fun showResult() {
        val dialogShowResult = ShowResultDialogFragment()
        dialogShowResult.setStyle(DialogFragment.STYLE_NORMAL, R.style.myDialog)
        dialogShowResult.isCancelable = false
        dialogShowResult.show(supportFragmentManager, "dialogShowFragment")
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun generateQuest() {
        var i = 0
        btnChoices.forEach {
            it.setBackgroundResource(btnRes[i])
            i++
        }
        quest = CountryCodes.COUNTRY_CODES.random()

        var choice2 = CountryCodes.COUNTRY_CODES.random()
        while (choice2 == quest) {
            choice2 = CountryCodes.COUNTRY_CODES.random()
        }
        var choice3 = CountryCodes.COUNTRY_CODES.random()
        while (choice3 == quest || choice3 == choice2) {
            choice3 = CountryCodes.COUNTRY_CODES.random()
        }
        var choice4 = CountryCodes.COUNTRY_CODES.random()
        while (choice4 == quest || choice4 == choice3 || choice4 == choice2) {
            choice4 = CountryCodes.COUNTRY_CODES.random()
        }

        val strChoices = arrayOf(choice2, choice3, choice4)

        btnChoices.random().apply {
            text = CountryCodes.COUNTRY_MAP[quest]
            contentDescription = CountryCodes.COUNTRY_MAP[quest]
        }

        val url = "https://flagcdn.com/224x168/${quest.lowercase()}.png"
        FetchImage(url).start()

        i = 0
        btnChoices.forEach {
            if (it.text != CountryCodes.COUNTRY_MAP[quest]) {
                it.text = CountryCodes.COUNTRY_MAP[strChoices[i]]
                it.contentDescription = CountryCodes.COUNTRY_MAP[strChoices[i]]
                i++
            }
        }
    }

    fun getScore() = score

    fun getName() = name

    @SuppressLint("ResourceAsColor", "SetTextI18n")
    override fun onBackPressed() {
        pauseTimer()
        val dialog = Dialog(this)
        dialog.apply {
            setContentView(R.layout.dialog_confirm_exit)
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            findViewById<Button>(R.id.btnPositive).setOnClickListener { finish() }
            findViewById<Button>(R.id.btnNegative).setOnClickListener {
                dismiss()
                startTimer()
            }
            findViewById<TextView>(R.id.tvConfirm).apply {
                setTextColor(R.color.statusBarColor)
                text = "Are You Sure Back to Menu?"
            }
            show()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)

        return (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
    }

    fun stopMusic() { mediaPlayer.reset() }

    fun finishTimer() : String {
        binding.chronometer2.stop()
        return binding.chronometer2.text.toString()
    }

    private fun pauseTimer(){
        if (isRunning){
            binding.chronometer2.stop()
            currentTime = SystemClock.elapsedRealtime() -  binding.chronometer2.base
            isRunning = false
        }
    }

    private fun startTimer(){
        if (!isRunning){
            binding.chronometer2.apply {
                base = SystemClock.elapsedRealtime() - currentTime
                start()
            }
            isRunning = true
        }
    }

    override fun onPause() {
        if (!isMute) mediaPlayer.pause()
        pauseTimer()
        super.onPause()
    }

    override fun onResume() {
        startTimer()
        if (!isMute) mediaPlayer.start()
        super.onResume()
    }

    inner class FetchImage(private var url: String) : Thread() {
        private lateinit var bitmap: Bitmap

        override fun run() {
            quizHandler.post {
                binding.imgFlag.setImageResource(R.drawable.load_img)
                pauseTimer()
            }

            val inputStream: InputStream?

            try {
                inputStream = URL(url).openStream()
                bitmap = BitmapFactory.decodeStream(inputStream)
            } catch (e: IOException) {
                FetchImage(url)
            }

            quizHandler.post {
                binding.imgFlag.setImageBitmap(bitmap)
                startTimer()
            }

        }

    }
}
