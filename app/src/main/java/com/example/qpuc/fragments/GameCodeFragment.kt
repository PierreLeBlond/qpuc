package com.example.qpuc.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.qpuc.R

public class GameCodeFragment : Fragment(R.layout.game_code) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gameCode = requireArguments().getString("gameCode")
        val playerName = requireArguments().getString("playerName") ?: ""
        val isMaster = requireArguments().getBoolean("isMaster")

        val gameCodeView = view.findViewById<TextView>(R.id.textview_game_code)
        gameCodeView.text = resources.getString(R.string.header_game_code, gameCode)

        val roleView = view.findViewById<TextView>(R.id.textview_role)
        val roleName = if (isMaster) R.string.header_master else R.string.header_player
        roleView.text = resources.getString(roleName)

        val playerNameView = view.findViewById<TextView>(R.id.textview_player_name)
        playerNameView.text = resources.getString(R.string.header_player_name, playerName)
    }
}