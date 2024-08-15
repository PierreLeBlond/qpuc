package com.example.qpuc

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.qpuc.databinding.ActivityJoinGameBinding
import com.example.qpuc.models.Player
import com.example.qpuc.utils.FirebaseUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class JoinGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJoinGameBinding
    private lateinit var gameRef: DatabaseReference

    private var numberOfPlayers: Int = 0
    private lateinit var players: List<Player>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gameCode = intent.getStringExtra("gameCode")!!
        gameRef = FirebaseUtils.getInstance().getReference("games").child(gameCode)
        gameRef.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        showNoGameError()
                        return
                    }

                    numberOfPlayers = snapshot.child("numberOfPlayers").getValue(Int::class.java) ?: 0
                    players = snapshot.child("players").children.map { s -> s.getValue(Player::class.java)!! }

                    showForm()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            }
        )

        binding.buttonJoinGameConfirm.setOnClickListener {
            val playerName = binding.edittextPlayerName.text.toString()

            val playerAlreadyExists = players.any { p -> p.name == playerName }
            val placesLeft = numberOfPlayers - players.size

            if (!playerAlreadyExists && placesLeft <= 0) {
                binding.textNoPlaceError.visibility = android.view.View.VISIBLE
                return@setOnClickListener
            }

            if (playerAlreadyExists) {
                val intent = Intent(this, LobbyActivity::class.java)
                intent.putExtra("gameCode", gameCode)
                intent.putExtra("playerName", playerName)
                startActivity(intent)
                return@setOnClickListener
            }

            gameRef.child("players").child(playerName).setValue(Player(playerName)).addOnCompleteListener {
                if (it.isSuccessful) {
                    val intent = Intent(this, LobbyActivity::class.java)
                    intent.putExtra("gameCode", gameCode)
                    intent.putExtra("playerName", playerName)
                    startActivity(intent)
                }
            }
        }

        binding.buttonCancel.setOnClickListener {
            finish()
        }
    }

    private fun clear() {
        binding.layoutNoGameError.visibility = android.view.View.GONE
        binding.layoutLoading.visibility = android.view.View.GONE
        binding.layoutForm.visibility = android.view.View.GONE
    }

    private fun showNoGameError() {
        clear()
        binding.layoutNoGameError.visibility = android.view.View.VISIBLE
    }

    private fun showForm() {
        clear()
        binding.layoutForm.visibility = android.view.View.VISIBLE
    }
}
