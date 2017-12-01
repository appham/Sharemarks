package com.appham.sharemarks.view

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.Gravity
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.widget.Toast
import com.appham.sharemarks.BuildConfig
import com.appham.sharemarks.R
import com.appham.sharemarks.model.Analytics
import com.appham.sharemarks.model.MarkItem
import com.appham.sharemarks.model.MarksDataSource
import com.appham.sharemarks.presenter.HtmlManager
import com.appham.sharemarks.presenter.MarksContract
import com.appham.sharemarks.presenter.MarksPresenter
import com.google.firebase.analytics.FirebaseAnalytics


class MarksActivity : AppCompatActivity(), MarksContract.View, NavigationView.OnNavigationItemSelectedListener {

    private var referrer: String? = null

    private lateinit var marksFragment: MarksFragment

    private lateinit var drawer: DrawerLayout

    private lateinit var navigationView: NavigationView

    internal lateinit var presenter: MarksPresenter

    private lateinit var navFilter: SubMenu

    private lateinit var navDomains: SubMenu

    private var snackbar: Snackbar? = null

    private lateinit var currentFilter: String

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    //region Activity lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marks)

        // generate a ua string for loading websites
        HtmlManager.initUserAgentStr(packageName, resources.configuration.screenLayout)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // navigation drawer
        drawer = findViewById<View>(R.id.drawerLayout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.app_name, R.string.all)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        navigationView = findViewById<NavigationView>(R.id.navView)
        navigationView.setNavigationItemSelectedListener(this)
        navFilter = navigationView.menu.addSubMenu(getString(R.string.filter))
        navFilter.add(getString(R.string.all)).setIcon(android.R.drawable.ic_menu_search)
        navFilter.add(getString(R.string.deleted)).setIcon(android.R.drawable.ic_menu_delete)
        navDomains = navigationView.menu.addSubMenu(getString(R.string.by_websites))

        // add list fragment
        marksFragment = MarksFragment()
        fragmentManager.beginTransaction().add(R.id.frameMarks, marksFragment, MarksFragment.TAG)
                .commit()
        supportFragmentManager.executePendingTransactions()

        // setup presenter
        presenter = MarksPresenter(this, MarksDataSource(this), marksFragment.getMarksAdapter().marks)

        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        // Get intent, action and MIME type and handle it in presenter
        if (savedInstanceState == null) {
            handleIntent(intent)
        }

        currentFilter = getString(R.string.all)
        updateAppTitleToFilter()
    }
    //endregion

    //region Activity listener methods
    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            super.onBackPressed()
        } else {
            drawer.openDrawer(GravityCompat.START)

            // log event
            val bundle = Bundle()
            bundle.putString(Analytics.ACTION.get(), Analytics.PRESS_BACK.get())
            firebaseAnalytics.logEvent(Analytics.OPEN_NAV_DRAWER.get(), bundle)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        currentFilter = when (item.title.toString()) {
            getString(R.string.all) -> {
                presenter.syncMarksFromDb(0)
                getString(R.string.all)
            }

            getString(R.string.deleted) -> {
                presenter.syncMarksFromDb(1)
                getString(R.string.deleted)
            }
            else -> {
                presenter.queryMarksByDomain(item.title.toString(), 0)
                item.title.toString()
            }
        }
        updateAppTitleToFilter()
        drawer.closeDrawer(Gravity.START)

        // log event
        val bundle = Bundle()
        bundle.putString(Analytics.FILTER.get(), currentFilter)
        firebaseAnalytics.logEvent(Analytics.CLICK_NAV_ITEM.get(), bundle)

        return true
    }
    //region

    //region MarksContract interface methods
    override fun addMarkItem(item: MarkItem) = runOnUiThread { marksFragment.addItem(item) }

    override fun removeMarkItem(item: MarkItem) = runOnUiThread { marksFragment.removeItem(item) }

    override fun addDrawerItems(items: List<String>) =
            runOnUiThread {
                navDomains.clear()
                items.forEach { item ->
                    navDomains.add(item).setIcon(android.R.drawable.ic_menu_search)
                }
            }

    override fun notifyDataChanged() =
            runOnUiThread { marksFragment.getMarksAdapter().notifyDataSetChanged() }

    override fun notifyItemChanged(index: Int) =
            runOnUiThread { marksFragment.getMarksAdapter().notifyItemChanged(index) }

    override fun showToast(resId: Int) = Toast.makeText(this, resId, Toast.LENGTH_LONG).show()

    override fun showSnackbar(resId: Int, action: (View) -> Unit) {
        snackbar?.dismiss()
        snackbar = Snackbar.make(marksFragment.view, resId, Snackbar.LENGTH_LONG)
                .setDuration(5000)
                .setAction(R.string.undo, action)
        snackbar?.show()
    }

    override fun isDeletedFilter(): Boolean = getString(R.string.deleted).equals(currentFilter)

    override fun updateAppTitleToFilter() {
        supportActionBar?.title = getString(R.string.app_name) + " - " + currentFilter
    }

    override fun showShareChooser(item: MarkItem) {
        try {

            // filter out sharemarks as option to share
            val shareIntent = Intent(android.content.Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            val resInfos = packageManager.queryIntentActivities(shareIntent, 0)
            if (!resInfos.isEmpty()) {
                val targetedShareIntents = getShareIntents(resInfos, item)

                val chooserIntent = Intent.createChooser(targetedShareIntents.removeAt(0),
                        getString(R.string.share_mark) + " - " + item.domain)
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                        targetedShareIntents.toTypedArray<Parcelable>())
                startActivity(chooserIntent)
            }

        } catch (ex: Exception) {
            showToast(R.string.share_mark_no_options)
        }
    }
    //endregion

    private fun handleIntent(intent: Intent?) {
        if (intent != null) {
            initReferrer()
            presenter.syncMarksFromDb(0)

            var type: String? = intent.type
            val sharedText: String? = if (intent.dataString.isNullOrBlank()) { // from share
                intent.getStringExtra(Intent.EXTRA_TEXT)
            } else if (intent.data.scheme.startsWith("http", true)) { // from browsable
                type = "text/html"
                intent.dataString
            } else {
                null
            }
            presenter.handleSharedData(intent.action, type,
                    sharedText, referrer)

            // log event
            val bundle = Bundle()
            bundle.putString(Analytics.INTENT_TYPE.get(), intent.type)
            bundle.putString(Analytics.INTENT_ACTION.get(), intent.action)
            bundle.putString(Analytics.REFERRER.get(), referrer)
            firebaseAnalytics.logEvent(Analytics.HANDLE_INTENT.get(), bundle)
        }
    }

    private fun initReferrer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (getReferrer() != null) {
                referrer = getReferrer()!!.host
            }
        }
    }

    companion object {

        /**
         * share intents without sharemarks option
         */
        internal fun getShareIntents(resInfos: MutableList<ResolveInfo>,
                                     item: MarkItem): MutableList<Intent> {
            val targetedShareIntents = mutableListOf<Intent>()
            for (resolveInfo in resInfos) {
                val component = ComponentName(resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name)
                if (!TextUtils.equals(component.packageName, BuildConfig.APPLICATION_ID)) {
                    val targetedShareIntent = Intent(Intent.ACTION_SEND)
                    targetedShareIntent.type = "text/plain"
                    targetedShareIntent.putExtra(
                            Intent.EXTRA_SUBJECT, item.title)
                    targetedShareIntent.putExtra(
                            Intent.EXTRA_TITLE, item.title)
                    targetedShareIntent.putExtra(
                            Intent.EXTRA_TEXT, item.title + " " + item.url)
                    targetedShareIntent.component = component
                    targetedShareIntents.add(targetedShareIntent)
                }
            }
            return targetedShareIntents
        }

        /**
         * browser intents without sharemarks option
         */
        internal fun getBrowserIntents(resInfos: MutableList<ResolveInfo>,
                                       uri: Uri): MutableList<Intent> {
            val targetedShareIntents = mutableListOf<Intent>()
            for (resolveInfo in resInfos) {
                val component = ComponentName(resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name)
                if (!TextUtils.equals(component.packageName, BuildConfig.APPLICATION_ID)) {
                    val targetedShareIntent = Intent(Intent.ACTION_VIEW, uri)
                    targetedShareIntent.component = component
                    targetedShareIntents.add(targetedShareIntent)
                }
            }
            return targetedShareIntents
        }
    }

}