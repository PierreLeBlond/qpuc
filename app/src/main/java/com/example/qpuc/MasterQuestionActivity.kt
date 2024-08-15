package com.example.qpuc

import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.example.qpuc.databinding.ActivityMasterQuestionBinding
import com.example.qpuc.fragments.GameCodeFragment
import com.example.qpuc.fragments.PlayersFragment
import com.example.qpuc.utils.FirebaseUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import kotlin.math.ceil

class MasterQuestionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMasterQuestionBinding
    private lateinit var gameRef: DatabaseReference
    private lateinit var gameCode: String
    private var timer: CountDownTimer? = null
    private var remainingTime: Long = 20000
    private var wrongMediaPlayer: MediaPlayer? = null
    private var questionMediaPlayer: MediaPlayer? = null
    private var questionPoints: Int = 1
    private var answeringPlayerName: String? = null
    private var state = HostStateMachine().createStateMachine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMasterQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gameCode = intent.getStringExtra("gameCode")!!
        gameRef = FirebaseUtils.getInstance().getReference("games").child(gameCode)

        if (savedInstanceState == null) {
            val headerBundle = bundleOf("gameCode" to gameCode, "isMaster" to true)
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<GameCodeFragment>(R.id.fragment_game_code, args = headerBundle)
            }

            val playersBundle = bundleOf("gameCode" to gameCode, "hideQualified" to true)
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<PlayersFragment>(R.id.fragment_players, args = playersBundle)
            }
        }

        gameRef.child("question").child("points").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                questionPoints = snapshot.getValue(Int::class.java) ?: return
                showQuestionPoints()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        gameRef.child("question").child("buzzingPlayerName").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (state.state != State.Top) {
                    return
                }
                val buzzingPlayerName = snapshot.getValue(String::class.java) ?: return
                answeringPlayerName = buzzingPlayerName
                binding.textviewPlayerAnswering.text = answeringPlayerName
                onOtherBuzz()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        binding.buttonTop.setOnClickListener {
            onTop()
        }

        binding.buttonGoodAnswer.setOnClickListener {
            onGoodAnswer()
        }

        binding.buttonWrongAnswer.setOnClickListener {
            onWrongAnswer()
        }

        update()
    }

    private fun onTop() {
        state.transition(Event.OnTop)
        gameRef.child("question").child("state").setValue("top")
        timer?.cancel()
        timer = object : CountDownTimer(remainingTime, 100) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                val remainingSeconds = ceil(millisUntilFinished.toDouble() / 1000.0).toInt()
                binding.textviewCountdown.text = "$remainingSeconds"
            }

            override fun onFinish() {
                endQuestion()
                finish()
            }
        }
        timer?.start()
        questionMediaPlayer?.start()
        update()
    }

    private fun onOtherBuzz() {
        state.transition(Event.OnOtherBuzz)
        val updates: MutableMap<String, Any> = hashMapOf(
            "question/state" to "buzz",
            "question/answeringPlayerName" to answeringPlayerName!!,
        )
        gameRef.updateChildren(updates)
        timer?.cancel()
        questionMediaPlayer?.pause()
        binding.textviewPlayerCountdown.setTextColor(resources.getColor(R.color.green))
        timer = object : CountDownTimer(4000, 100) {
            override fun onTick(millisUntilFinished: Long) {
                // TODO : Reduce global timer too
                val remainingSeconds = ceil(millisUntilFinished.toDouble() / 1000.0).toInt()
                binding.textviewPlayerCountdown.text = "$remainingSeconds"
            }

            override fun onFinish() {
                binding.textviewPlayerCountdown.text = "0"
                binding.textviewPlayerCountdown.setTextColor(resources.getColor(R.color.red))
            }
        }
        timer?.start()
        update()
    }

    private fun onGoodAnswer() {
        val name = answeringPlayerName ?: return
        val goodMediaPlayer = MediaPlayer.create(this, R.raw.good)
        timer?.cancel()
        endQuestion()
        goodMediaPlayer.setOnCompletionListener {
            gameRef.child("players").child(name).child("points")
                .runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        var points =
                            currentData.getValue(Int::class.java) ?: return Transaction.success(
                                currentData
                            )
                        points += questionPoints
                        currentData.value = points
                        return Transaction.success(currentData)
                    }

                    override fun onComplete(
                        error: DatabaseError?,
                        committed: Boolean,
                        currentData: DataSnapshot?
                    ) {
                        goodMediaPlayer.stop()
                        goodMediaPlayer.release()
                        finish()
                    }
                })
        }
        goodMediaPlayer?.start()
    }

    private fun onWrongAnswer() {
        onTop()
        wrongMediaPlayer?.start()
    }

    private fun update() {
        binding.buttonTop.visibility = if (state.state == State.Wait) android.view.View.VISIBLE else android.view.View.GONE
        binding.textviewCountdown.visibility = if (state.state == State.Top) android.view.View.VISIBLE else android.view.View.GONE
        binding.layoutPlayerAnswering.visibility = if (state.state == State.OtherBuzz) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun showQuestionPoints() {
        binding.textviewQuestionPoints.text = getResources().getQuantityString(R.plurals.question_points, questionPoints, questionPoints)
    }

    private fun endQuestion() {
        gameRef.child("question").setValue(null)
    }

    override fun onPause() {
        timer?.cancel()
        wrongMediaPlayer?.stop()
        wrongMediaPlayer?.release()
        wrongMediaPlayer = null
        questionMediaPlayer?.stop()
        questionMediaPlayer?.release()
        questionMediaPlayer = null
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        wrongMediaPlayer = MediaPlayer.create(this, R.raw.wrong)
        questionMediaPlayer = MediaPlayer.create(this, R.raw.question)
        questionMediaPlayer?.isLooping = true
    }
}