package com.appham.sharemarks.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.appham.sharemarks.R
import com.appham.sharemarks.model.MarkItem
import com.squareup.picasso.Picasso

/**
 * @author thomas
 */
class MarksAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val marks = mutableListOf<MarkItem>()

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context)
                .inflate(R.layout.mark_item, parent, false)
        return MarkHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is MarkHolder) {
            val markItem = marks[position]
            holder.txtTitle.text = position.toString() + " - " + markItem.title
            holder.txtContent.text = markItem.content
            holder.txtReferrer.text = markItem.referrer + " | " + markItem.domain
            if (markItem.imageUrl != null) {
                Picasso.with(context).load(markItem.imageUrl)
                        .resize(300, 200)
                        .onlyScaleDown()
                        .centerInside()
                        .into(holder.imgMark)
                holder.imgMark.visibility = View.VISIBLE
            } else {
                holder.imgMark.visibility = View.GONE
            }

            // add click listener to open browser
            holder.itemView.setOnClickListener {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(markItem.url)))
                //TODO: open in a webview instead
            }

            holder.itemView.tag = markItem
        }

        //TODO: maybe need different layouts for big and small images
    }

    override fun getItemCount(): Int {
        return marks.size
    }

    fun addItem(markItem: MarkItem) {
        marks.add(0, markItem)
    }

    fun removeItem(item: MarkItem) {
        marks.remove(item)
    }

    internal inner class MarkHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardItem: CardView = itemView.findViewById(R.id.cardMarkItem)
        val layItem: RelativeLayout = itemView.findViewById(R.id.layMarkItem)
        val imgMark: ImageView = itemView.findViewById(R.id.imgMark)
        val txtTitle: TextView = itemView.findViewById(R.id.txtTitle)
        val txtContent: TextView = itemView.findViewById(R.id.txtContent)

        val txtReferrer: TextView = itemView.findViewById(R.id.txtReferrer)

    }
}