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

        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(recyclerView: RecyclerView,
                                viewHolder: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                (activity as MarksActivity).presenter.setMarkDeleted(viewHolder.itemView.tag as MarkItem)
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