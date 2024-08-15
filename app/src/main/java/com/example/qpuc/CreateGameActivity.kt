package com.example.qpuc

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.qpuc.databinding.ActivityCreateGameBinding
import com.example.qpuc.models.Game
import com.example.qpuc.utils.FirebaseUtils


class CreateGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateGameBinding
    private lateinit var gameCode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonCreateGame.setOnClickListener {
            val numberOfPlayers = binding.edittextNumberOfPlayers.text.toString().toInt()
            gameCode = FirebaseUtils.generateGameCode()

            val game = Game(numberOfPlayers)
            FirebaseUtils.getInstance().getReference("games").child(gameCode).setValue(game).addOnSuccessListener {
                val intent = Intent(this, LobbyActivity::class.java)
                intent.putExtra("gameCode", gameCode)
                intent.putExtra("isMaster", true)
                startActivity(intent)
            }
        }

        binding.buttonCancel.setOnClickListener {
            finish()
        }
    }
}
