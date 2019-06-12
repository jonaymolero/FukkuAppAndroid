package net.azarquiel.fukkuapp.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.row_message.view.*
import net.azarquiel.fukkuapp.model.Message
import android.view.Gravity
import net.azarquiel.fukkuapp.R
import java.text.SimpleDateFormat


/**
 * Created by gonzaloagain on 22/05/19.
 */
class MessagesAdapter(val context: Context,
                    val layout: Int
                    ) : RecyclerView.Adapter<MessagesAdapter.ViewHolder>() {

    private var dataList: List<Message> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val viewlayout = layoutInflater.inflate(layout, parent, false)
        return ViewHolder(viewlayout, context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    internal fun setMessages(pajaros: List<Message>) {
        this.dataList = pajaros
        notifyDataSetChanged()
    }


    class ViewHolder(viewlayout: View, val context: Context) : RecyclerView.ViewHolder(viewlayout) {
        fun bind(dataItem: Message){
            itemView.tvMessage.text = dataItem.text
            itemView.tvTime.text = SimpleDateFormat("HH:mm").format(dataItem.time)

            val lp = itemView.layoutParams as ViewGroup.MarginLayoutParams

            if (FirebaseAuth.getInstance().currentUser!!.uid == dataItem.senderID) {
                itemView.LinearMessage.gravity = Gravity.RIGHT
                lp.setMargins(200,10,20,10)
                itemView.cdMessage.setCardBackgroundColor(ContextCompat.getColor(context,
                    R.color.accent_material_dark
                ))
            } else {
                itemView.LinearMessage.gravity = Gravity.LEFT
                lp.setMargins(20,10,200,10)
                itemView.cdMessage.setCardBackgroundColor(ContextCompat.getColor(context,
                    R.color.colorPrimary
                ))
            }
            //itemView.rvMessages.scrollToPosition(adapterPosition-1)
        }

    }
}