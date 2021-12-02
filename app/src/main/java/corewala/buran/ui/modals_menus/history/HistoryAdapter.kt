package corewala.buran.ui.modals_menus.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.row_history.view.*
import corewala.delay
import corewala.buran.R
import corewala.buran.io.database.history.HistoryEntry

class HistoryAdapter(val history: List<HistoryEntry>, val onClick:(entry: HistoryEntry) -> Unit): RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View): RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_history, parent, false))
    }

    override fun getItemCount(): Int = history.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.history_address.text = history[position].uri.toString()
        holder.itemView.history_row.setOnClickListener {
            delay(500){
                onClick(history[holder.adapterPosition])
            }
        }
    }
}