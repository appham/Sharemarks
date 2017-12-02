package com.appham.sharemarks.view

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.RotateDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.appham.sharemarks.R
import com.appham.sharemarks.model.Analytics
import com.appham.sharemarks.model.MarkItem
import com.appham.sharemarks.presenter.MarksContract
import com.google.firebase.analytics.FirebaseAnalytics
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation

/**
 * @author thomas
 */
class MarksAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val marks = mutableListOf<MarkItem>()

    private lateinit var context: Context

    private val screenWidthPx by lazy { Resources.getSystem().displayMetrics.widthPixels }

    private val firebaseAnalytics by lazy { FirebaseAnalytics.getInstance(context) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context)
                .inflate(R.layout.mark_item, parent, false)
        return MarkHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is MarkHolder) {
            val item = marks[position]
            holder.txtTitle.text = position.toString().plus(" - ").plus(item.title)
            holder.txtContent.text = item.content
            holder.txtReferrer.text = item.referrer.plus(" | ").plus(item.domain)
            if (item.imageUrl != null) {

                // rotating placeholder image
                val placeholder = ContextCompat.getDrawable(context, R.drawable.ic_launcher_rotate) as RotateDrawable
                val animator = ObjectAnimator.ofInt(placeholder, "level", 0, 10000)
                animator.repeatCount = Animation.INFINITE
                animator.duration = 2000
                animator.interpolator = LinearInterpolator()
                animator.start()

                Picasso.with(context).load(item.imageUrl)
                        .resize(screenWidthPx / 3,
                                screenWidthPx / 4)
                        .placeholder(placeholder)
                        .error(R.mipmap.ic_launcher)
                        .onlyScaleDown()
                        .centerInside()
                        .transform(RoundedCornersTransformation(10, 10))
                        .into(holder.imgMark)

                holder.imgMark.visibility = View.VISIBLE
            } else {
                holder.imgMark.visibility = View.GONE
            }

            // add click listener to open browser
            holder.itemView.setOnClickListener {

                val uri = Uri.parse(item.url)
                val viewIntent = Intent(Intent.ACTION_VIEW, Uri.parse(item.url))
                val resInfos = context.packageManager.queryIntentActivities(viewIntent, 0)
                val viewIntents = MarksActivity.getBrowserIntents(resInfos, uri)

                if (viewIntents.isNotEmpty()) context.startActivity(viewIntents.removeAt(0))

                // log event
                val bundle = Bundle()
//                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, item._id.toString())
//                bundle.putString(Analytics.DOMAIN.get(), item.domain)
//                bundle.putString(Analytics.REFERRER.get(), item.referrer)
//                bundle.putString(Analytics.DELETED.get(), item.deleted.toString())
//                bundle.putString(Analytics.ACTION.get(), Analytics.CLICK_MARK.get())
                bundle.putString(Analytics.ITEM.get(), item.shortStr())
                firebaseAnalytics.logEvent(Analytics.OPEN_BROWSER.get(), bundle)

                //TODO: open in a webview instead
            }

            holder.itemView.setOnLongClickListener {
                (context as MarksContract.View).showShareChooser(item)

                // log event
                val bundle = Bundle()
//                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, item._id.toString())
//                bundle.putString(Analytics.DOMAIN.get(), item.domain)
//                bundle.putString(Analytics.REFERRER.get(), item.referrer)
//                bundle.putString(Analytics.DELETED.get(), item.deleted.toString())
//                bundle.putString(Analytics.ACTION.get(), Analytics.LONG_CLICK_MARK.get())
                bundle.putString(Analytics.ITEM.get(), item.shortStr())
                firebaseAnalytics.logEvent(Analytics.SHARE_MARK.get(), bundle)

                true
            }

            holder.itemView.tag = item
        }

        //TODO: maybe need different layouts for big and small images
    }

    override fun getItemCount(): Int {
        return marks.size
    }

    fun addItem(item: MarkItem) {
        marks.add(0, item)
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