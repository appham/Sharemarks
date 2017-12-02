package com.appham.sharemarks.view

import android.app.Fragment
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.appham.sharemarks.R
import com.appham.sharemarks.model.Analytics
import com.appham.sharemarks.model.MarkItem
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * @author thomas
 */
class MarksFragment : Fragment() {
    companion object {
        const val TAG: String = "MarksFragment"
    }

    private lateinit var marksList: RecyclerView
    private lateinit var marksLayoutManager: LinearLayoutManager
    private val marksAdapter = MarksAdapter()
    private val marksActivity: MarksActivity by lazy { activity as MarksActivity }
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    //region lifecycle methods
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // get recycler-list of ad results
        marksList = view.findViewById<RecyclerView>(R.id.listMarks)

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        marksList.setHasFixedSize(true)

        // use a linear layout manager
        marksLayoutManager = LinearLayoutManager(activity)
        marksList.layoutManager = marksLayoutManager

        marksList.adapter = marksAdapter

        setupDeleteBySwipe()

        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = FirebaseAnalytics.getInstance(marksActivity)

        super.onViewCreated(view, savedInstanceState)
    }
    //endregion

    private fun setupDeleteBySwipe() {

        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(recyclerView: RecyclerView,
                                viewHolder: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                val item = viewHolder.itemView.tag as MarkItem
                when {
                    swipeDir == ItemTouchHelper.LEFT -> {//delete on left swipe

                        // log event
                        val bundle = Bundle()
//                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, item._id.toString())
//                        bundle.putString(Analytics.DOMAIN.get(), item.domain)
//                        bundle.putString(Analytics.REFERRER.get(), item.referrer)
//                        bundle.putString(Analytics.DELETED.get(), item.deleted.toString())
//                        bundle.putString(Analytics.ACTION.get(), Analytics.SWIPE_LEFT.get())
                        bundle.putString(Analytics.ITEM.get(), item.shortStr())
                        firebaseAnalytics.logEvent(Analytics.DELETE_MARK.get(), bundle)

                        marksActivity.presenter.setMarkDeleted(item, true)
                    }

                    item.deleted -> {//right swipe for deleted item restores it
                        marksActivity.presenter.setMarkDeleted(item, false)

                        // log event
                        val bundle = Bundle()
//                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, item._id.toString())
//                        bundle.putString(Analytics.DOMAIN.get(), item.domain)
//                        bundle.putString(Analytics.REFERRER.get(), item.referrer)
//                        bundle.putString(Analytics.DELETED.get(), item.deleted.toString())
//                        bundle.putString(Analytics.ACTION.get(), Analytics.SWIPE_RIGHT.get())
                        bundle.putString(Analytics.ITEM.get(), item.shortStr())
                        firebaseAnalytics.logEvent(Analytics.RESTORE_MARK.get(), bundle)
                    }
                    else -> { // right swipe on non-deleted item shares it
                        marksActivity.showShareChooser(item)
                        marksAdapter.notifyItemChanged(viewHolder.adapterPosition)

                        // log event
                        val bundle = Bundle()
//                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, item._id.toString())
//                        bundle.putString(Analytics.DOMAIN.get(), item.domain)
//                        bundle.putString(Analytics.REFERRER.get(), item.referrer)
//                        bundle.putString(Analytics.DELETED.get(), item.deleted.toString())
//                        bundle.putString(Analytics.ACTION.get(), Analytics.SWIPE_RIGHT.get())
                        bundle.putString(Analytics.ITEM.get(), item.shortStr())
                        firebaseAnalytics.logEvent(Analytics.SHARE_MARK.get(), bundle)
                    }
                }
            }
        }

        ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(marksList)
    }

    fun getMarksAdapter(): MarksAdapter = marksAdapter

    fun addItem(item: MarkItem) {
        marksAdapter.addItem(item)
        marksAdapter.notifyDataSetChanged()
    }

    fun removeItem(item: MarkItem) {
        marksAdapter.removeItem(item)
        marksAdapter.notifyDataSetChanged()
    }

}