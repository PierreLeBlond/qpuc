package com.example.qpuc

import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.example.qpuc.databinding.ActivityPlayerQuestionBinding
import com.example.qpuc.utils.FirebaseUtils
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlin.math.ceil

class PlayerQuestionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerQuestionBinding
    private lateinit var gameRef: DatabaseReference
    private lateinit var gameCode: String
    private lateinit var playerName: String
    private var questionPoints: Int = 1
    private var answeringPlayerName: String? = null
    private var state = HostStateMachine().createStateMachine()
    private var timer: CountDownTimer? = null
    private var buzzMediaPlayer: MediaPlayer? = null
    private var wrongMediaPlayer: MediaPlayer? = null
    private var countdown: Int = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlayerQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playerName = intent.getStringExtra("playerName")!!

        gameCode = intent.getStringExtra("gameCode")!!
        gameRef = FirebaseUtils.getInstance().getReference("games").child(gameCode)

        gameRef.child("question").child("points").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                questionPoints = snapshot.getValue(Int::class.java) ?: return
                showQuestionPoints()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        gameRef.child("question").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    finish()
                }
                answeringPlayerName = snapshot.child("answeringPlayerName").getValue(String::class.java)
                val gameState = snapshot.child("state").getValue(String::class.java)
                when (gameState) {
                    "top" -> onTop()
                    "buzz" -> {
                        if (answeringPlayerName != playerName) {
                            onOtherBuzz()
                        } else {
                            onBuzz()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        binding.buttonBuzz.setOnClickListener {
            gameRef.child("question").child("buzzingPlayerName").setValue(playerName)
        }

        update()
    }

    private fun onTop() {
        state.transition(Event.OnTop)
        timer?.cancel()
        update()
    }

    private fun onBuzz() {
        state.transition(Event.OnBuzz)
        buzzMediaPlayer?.start()
        update()
        startCountdown()
    }

    private fun onOtherBuzz() {
        state.transition(Event.OnOtherBuzz)
        update()
    }

    private fun update() {
        binding.buttonBuzz.isEnabled = state.state == State.Top
        val iconId = when (state.state) {
            State.Wait -> R.drawable.pause
            State.Top -> R.drawable.zap
            State.OtherBuzz -> R.drawable.pause
            State.Buzz -> null
            State.Out -> R.drawable.x
        }
        val button = binding.buttonBuzz as MaterialButton
        button.icon = if (iconId != null) getDrawable(iconId) else null
        val color = when (state.state) {
            State.Wait -> R.color.grey
            State.Top -> R.color.green
            State.OtherBuzz -> R.color.grey
            State.Buzz -> if (countdown > 0) R.color.green else R.color.red
            State.Out -> R.color.red

        }
        binding.buttonBuzz.setBackgroundColor(resources.getColor(color))
    }

    private fun showQuestionPoints() {
        binding.textviewQuestionPoints.text = getResources().getQuantityString(R.plurals.question_points, questionPoints, questionPoints)
    }

    private fun startCountdown() {
        timer?.cancel()
        timer = object : CountDownTimer(4000, 500) {
            override fun onTick(millisUntilFinished: Long) {
                countdown = ceil(millisUntilFinished.toDouble() / 1000.0).toInt()
                binding.buttonBuzz.text = "$countdown"
                update()
            }

            override fun onFinish() {
                countdown = 0
                wrongMediaPlayer?.start()
                update()
            }
        }
        timer?.start()
    }

    override fun onPause() {
        timer?.cancel()
        buzzMediaPlayer?.stop()
        buzzMediaPlayer?.release()
        buzzMediaPlayer = null;
        wrongMediaPlayer?.stop()
        wrongMediaPlayer?.release()
        wrongMediaPlayer = null;
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        buzzMediaPlayer = MediaPlayer.create(this, R.raw.buzzer)
        wrongMediaPlayer = MediaPlayer.create(this, R.raw.wrong)
    }
}