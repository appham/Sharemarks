package com.appham.sharemarks.view

import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.appham.sharemarks.R
import com.appham.sharemarks.model.MarkItem

/**
 * @author thomas
 */
class MarksFragment : Fragment() {
    companion object {
        const val TAG: String = "MarksFragment"
    }

    private var marksList: RecyclerView? = null
    private var marksLayoutManager: LinearLayoutManager? = null
    private val marksAdapter = MarksAdapter()
    private val marksActivity: MarksActivity by lazy { activity as MarksActivity }

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
        marksList!!.setHasFixedSize(true)

        // use a linear layout manager
        marksLayoutManager = LinearLayoutManager(activity)
        marksList!!.layoutManager = marksLayoutManager

        marksList!!.adapter = marksAdapter

        setupDeleteBySwipe()

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
                if (swipeDir == ItemTouchHelper.LEFT) { //delete on left swipe
                    marksActivity.presenter.setMarkDeleted(item, true)
                } else if (item.deleted) { //right swipe for deleted item restores it
                    marksActivity.presenter.setMarkDeleted(item, false)
                } else { // right swipe on non-deleted item shares it
                    try {
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.type = "text/plain"
                        intent.putExtra(Intent.EXTRA_SUBJECT, item.title)
                        intent.putExtra(Intent.EXTRA_TEXT, item.content + " " + item.url)
                        startActivity(Intent.createChooser(intent, getString(R.string.share_mark)))
                    } catch (ex: android.content.ActivityNotFoundException) {
                        marksActivity.showToast(R.string.share_mark_no_options)
                    }

                    marksAdapter.notifyItemChanged(viewHolder.adapterPosition)
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