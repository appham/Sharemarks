package com.appham.sharemarks.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import com.appham.sharemarks.R
import com.appham.sharemarks.model.MarkItem
import com.appham.sharemarks.model.MarksDataSource
import com.appham.sharemarks.presenter.HtmlManager
import com.appham.sharemarks.presenter.MarksContract
import com.appham.sharemarks.presenter.MarksPresenter

class MarksActivity : AppCompatActivity(), MarksContract.View, NavigationView.OnNavigationItemSelectedListener {

    private var referrer: String? = null

    private lateinit var marksFragment: MarksFragment

    private lateinit var drawer: DrawerLayout

    private lateinit var navigationView: NavigationView

    internal lateinit var presenter: MarksPresenter

    private lateinit var navFilter: SubMenu

    private lateinit var navDomains: SubMenu

    //region Activity lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marks)

        // generate a ua string for loading websites
        HtmlManager.initUserAgentStr(this)

        // navigation drawer
        drawer = findViewById<View>(R.id.drawerLayout) as DrawerLayout

        navigationView = findViewById<NavigationView>(R.id.navView)
        navigationView.setNavigationItemSelectedListener(this)
        navFilter = navigationView.menu.addSubMenu("FILTER:")
        navFilter.add(getString(R.string.all)).setIcon(android.R.drawable.ic_menu_search)
        navFilter.add(getString(R.string.deleted)).setIcon(android.R.drawable.ic_menu_delete)
        navDomains = navigationView.menu.addSubMenu("BY DOMAINS:")

        // add list fragment
        marksFragment = MarksFragment()
        fragmentManager.beginTransaction().add(R.id.frameMarks, marksFragment, MarksFragment.TAG)
                .commit()
        supportFragmentManager.executePendingTransactions()

        // set Referrer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (getReferrer() != null) {
                referrer = getReferrer()!!.host
            }
        }

        // setup presenter
        presenter = MarksPresenter(this, MarksDataSource(this), marksFragment.getMarksAdapter().marks)

        // Get intent, action and MIME type and handle it in presenter
        if (savedInstanceState == null) {
            presenter.handleSharedData(intent.action, intent.type,
                    intent.getStringExtra(Intent.EXTRA_TEXT), referrer)
        }

    }
    //endregion

    //region Activity listener methods
    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            super.onBackPressed()
        } else {
            drawer.openDrawer(GravityCompat.START)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.title.toString()) {
            getString(R.string.all) -> presenter.syncMarksFromDb(0)
            getString(R.string.deleted) -> presenter.syncMarksFromDb(1)
            else -> {
                presenter.queryMarksByDomain(item.title.toString())
            }
        }
        return true
    }
    //region

    //region MarksContract interface methods
    override fun addMarkItem(item: MarkItem) {
        runOnUiThread { marksFragment.addItem(item) }
    }

    override fun removeMarkItem(item: MarkItem) {
        runOnUiThread { marksFragment.removeItem(item) }
    }

    override fun addDrawerItem(item: String) {
        runOnUiThread { navDomains.add(item).setIcon(android.R.drawable.ic_menu_zoom) }
    }

    override fun notifyDataChanged() {
        runOnUiThread { marksFragment.getMarksAdapter().notifyDataSetChanged() }
    }
    //endregion


}