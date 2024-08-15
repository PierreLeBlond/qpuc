package com.example.qpuc

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.qpuc.models.Player

class PlayerItemView(private val dataSet: ArrayList<Player>) :
    RecyclerView.Adapter<PlayerItemView.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Define click listener for the ViewHolder's View
        val nameView: TextView = view.findViewById(R.id.textview_name)
        val pointsView: TextView = view.findViewById(R.id.textview_points)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.player_row_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.nameView.text = dataSet[position].name
        if (dataSet[position].points >= 9) {
            viewHolder.nameView.setTextColor(viewHolder.itemView.resources.getColor(R.color.green))
        }
        viewHolder.pointsView.text = dataSet[position].points.toString()
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}

