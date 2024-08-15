package com.example.qpuc.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qpuc.PlayerItemView
import com.example.qpuc.R
import com.example.qpuc.models.Player
import com.example.qpuc.utils.FirebaseUtils
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class PlayersFragment : Fragment(R.layout.players) {

    private val players = ArrayList<Player>()
    private val qualifiedPlayers = ArrayList<Player>()
    private lateinit var gameRef : DatabaseReference
    private lateinit var numberOfPlayersView: TextView
    private var numberOfPlayers : Int = 0
    private lateinit var playersListener: ChildEventListener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        numberOfPlayersView = view.findViewById(R.id.textview_number_of_players)

        val gameCode = requireArguments().getString("gameCode")!!
        gameRef = FirebaseUtils.getInstance().getReference("games").child(gameCode)

        val qualifiedPlayersAdapter = PlayerItemView(qualifiedPlayers)
        val qualifiedPlayersRecyclerView: RecyclerView = view.findViewById(R.id.recyclerview_qualified_players)
        qualifiedPlayersRecyclerView.layoutManager = GridLayoutManager(requireContext(), 4)
        qualifiedPlayersRecyclerView.adapter = qualifiedPlayersAdapter

        val hideQualified = requireArguments().getBoolean("hideQualified") ?: false
        if (hideQualified) {
            numberOfPlayersView.visibility = View.GONE
            qualifiedPlayersRecyclerView.visibility = View.GONE
            view.findViewById<TextView>(R.id.textview_players_remaining).visibility = View.GONE
            view.findViewById<TextView>(R.id.textview_players_qualified).visibility = View.GONE
        }


        val playersAdapter = PlayerItemView(players)
        val playersRecyclerView: RecyclerView = view.findViewById(R.id.recyclerview_players)
        playersRecyclerView.layoutManager = GridLayoutManager(requireContext(), 4)
        playersRecyclerView.adapter = playersAdapter

        val numberOfPlayersListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                numberOfPlayers = dataSnapshot.getValue(Int::class.java)!!
                update()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
        gameRef.child("numberOfPlayers").addListenerForSingleValueEvent(numberOfPlayersListener)

        playersListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val name = dataSnapshot.key!!
                val points = dataSnapshot.child("points").getValue(Int::class.java) ?: 0
                val player = Player(name, points)
                if (points >= 9) {
                    qualifiedPlayers.add(player)
                    qualifiedPlayersAdapter.notifyItemInserted(qualifiedPlayers.size - 1)
                } else {
                    players.add(player)
                    playersAdapter.notifyItemInserted(players.size - 1)
                }
                update()
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val name = dataSnapshot.key!!
                val points = dataSnapshot.child("points").getValue(Int::class.java) ?: 0
                val player = players.find { player -> player.name == name } ?: return
                player.points = points

                if (points >= 9) {
                    val index = players.indexOf(player)
                    players.remove(player)
                    playersAdapter.notifyItemRemoved(index)
                    qualifiedPlayers.add(player)
                    qualifiedPlayersAdapter.notifyItemInserted(qualifiedPlayers.size - 1)
                } else {
                    playersAdapter.notifyItemChanged(players.indexOf(player))
                }

                update()
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                val name = dataSnapshot.key!!
                val player = players.find { player -> player.name == name } ?: return
                val index = players.indexOf(player)
                players.remove(player)
                playersAdapter.notifyItemRemoved(index)
                update()
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        gameRef.child("players").addChildEventListener(playersListener)
    }

    private fun update() {
        val activity = activity ?: return
        numberOfPlayersView.text = activity.resources.getString(R.string.players_numbers, players.size + qualifiedPlayers.size, numberOfPlayers)
    }
}