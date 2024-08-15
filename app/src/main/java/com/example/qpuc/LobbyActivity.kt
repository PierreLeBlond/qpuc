package com.example.qpuc

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.example.qpuc.databinding.ActivityLobbyBinding
import com.example.qpuc.fragments.GameCodeFragment
import com.example.qpuc.fragments.PlayersFragment
import com.example.qpuc.models.Question
import com.example.qpuc.utils.FirebaseUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class LobbyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLobbyBinding
    private lateinit var gameRef: DatabaseReference
    private lateinit var questionListener: ValueEventListener
    private lateinit var gameCode: String
    private var playerName: String? = null
    private var numberOfQualified: Int = 0
    private var isQualified: Boolean = false
    private var lastQuestionPoints: Int = 3
    private var introMediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gameCode = intent.getStringExtra("gameCode")!!
        playerName = intent.getStringExtra("playerName")
        val isMaster = intent.getBooleanExtra("isMaster", false)

        if (savedInstanceState == null) {
            val bundle = bundleOf("gameCode" to gameCode, "isMaster" to isMaster, "playerName" to playerName)
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<GameCodeFragment>(R.id.fragment_game_code, args = bundle)
            }
            val playersBundle = bundleOf("gameCode" to gameCode)
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<PlayersFragment>(R.id.fragment_players, args = playersBundle)
            }
        }

        binding = ActivityLobbyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gameRef = FirebaseUtils.getInstance().getReference("games").child(gameCode)

        val questionIntent = Intent(this, PlayerQuestionActivity::class.java)
        questionListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val hasQuestion = snapshot.exists()
                if (!hasQuestion) {
                    return
                }
                questionIntent.putExtra("gameCode", gameCode)
                questionIntent.putExtra("playerName", playerName)
                startActivity(questionIntent)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        }

        gameRef.child("players").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val count = snapshot.children.count { c -> (c.child("points").getValue(Int::class.java) ?: 0) >= 9 }
                    if (count != numberOfQualified) {
                        numberOfQualified = count
                        lastQuestionPoints = 3
                    }
                    if (playerName != null) {
                        isQualified = (snapshot.child(playerName!!).child("points").getValue(Int::class.java) ?: 0) >= 9
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            }
        )

        if (isMaster) {
            setupForMaster()
        }
    }

    private fun setupForMaster() {
        introMediaPlayer = MediaPlayer.create(this, R.raw.theme)
        introMediaPlayer?.start()

        binding.layoutNewQuestion.visibility = android.view.View.VISIBLE

        binding.buttonNewQuestionConfirm.setOnClickListener {
            val points = binding.edittextQuestionPoints.text.toString().toInt()
            val question = Question(points)
            gameRef.child("question").setValue(question)

            val intent = Intent(this, MasterQuestionActivity::class.java)
            intent.putExtra("gameCode", gameCode)
            startActivity(intent)

            lastQuestionPoints = points
            computeNextQuestion()
        }
    }

    override fun onResume() {
        super.onResume()
        if (playerName != null && !isQualified) {
            gameRef.child("question").addValueEventListener(questionListener)
        }
    }

    override fun onPause() {
        if (playerName != null) {
            gameRef.child("question").removeEventListener(questionListener)
        }
        introMediaPlayer?.stop()
        introMediaPlayer?.release()
        introMediaPlayer = null;
        super.onPause()
    }

    private fun computeNextQuestion() {
        if (numberOfQualified >= 3) {
            binding.layoutNewQuestion.visibility = android.view.View.GONE
            binding.textviewEndGame.visibility = android.view.View.VISIBLE
            return
        }
        val nextQuestionPoints = (lastQuestionPoints - numberOfQualified).mod((3 - numberOfQualified)) + 1 + numberOfQualified
        binding.edittextQuestionPoints.setText(nextQuestionPoints.toString())
    }
}