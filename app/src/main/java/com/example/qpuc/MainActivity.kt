package com.example.qpuc

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.qpuc.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonCreateGame.setOnClickListener {
            val intent = Intent(this, CreateGameActivity::class.java)
            startActivity(intent)
        }

        binding.buttonJoinGame.setOnClickListener {
            val gameCode = binding.edittextGameCode.text.toString()
            val intent = Intent(this, JoinGameActivity::class.java)
            intent.putExtra("gameCode", gameCode)
            startActivity(intent)
        }
    }
}
