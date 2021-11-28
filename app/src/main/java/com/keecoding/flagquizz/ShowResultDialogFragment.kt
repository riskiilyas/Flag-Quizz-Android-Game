package com.keecoding.flagquizz

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.keecoding.flagquizz.databinding.FragmentShowResultBinding

class ShowResultDialogFragment : DialogFragment() {
    private lateinit var binding: FragmentShowResultBinding
    private lateinit var mediaPlayer: MediaPlayer

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShowResultBinding.inflate(inflater, container, false)
        val thisActivity = activity as QuizActivity
        val score = thisActivity.getScore()
        thisActivity.stopMusic()
        binding.tvTimerShow.text = thisActivity.finishTimer()

        if (score<=5) {
            mediaPlayer = MediaPlayer.create(thisActivity, R.raw.music_lose)
            mediaPlayer.start()
            binding.tvWinStatus.text = "It's Okay, ${thisActivity.getName()} :("
            binding.tvQuestDoneStatus.text = "You only got $score out of 10"
            binding.lottieImageLose.visibility = View.VISIBLE
            binding.lottieImageWin.visibility = View.INVISIBLE
        } else{
            mediaPlayer = MediaPlayer.create(thisActivity, R.raw.music_win)
            mediaPlayer.start()
            when {
                (score in 6..7) -> binding.tvWinStatus.text = "Nice, ${thisActivity.getName()}!"
                (score in 8..9) -> binding.tvWinStatus.text = "Excellent, ${thisActivity.getName()}!"
                else -> binding.tvWinStatus.text = "Perfect, ${thisActivity.getName()}!"
            }
            binding.tvQuestDoneStatus.text = "You got $score out of 10!"
            binding.lottieImageWin.visibility = View.VISIBLE
            binding.lottieImageLose.visibility = View.INVISIBLE
        }

        binding.btnContinueToMenu.setOnClickListener { activity?.finish() }

        return binding.root
    }
}