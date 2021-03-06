package org.caojun.mapman

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.caojun.color.ColorUtils
import org.caojun.contacts.Contact
import org.caojun.contacts.ContactsView
import org.caojun.dialog.WebViewDialog
import org.caojun.imageview.ImageShow
import org.caojun.svgmap.PathItem
import org.caojun.svgmap.SvgMapView
import org.caojun.utils.ActivityUtils
import org.caojun.utils.RandomUtils
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {

    val Key_Map_Name = "Key_Map_Name"
    private var lastId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        btnSearch.setOnClickListener {
            if (TextUtils.isEmpty(btnSearch.text)) {
                return@setOnClickListener
            }
            doBaike(btnSearch.text.toString())
        }

        val list = ArrayList<Contact>()

        svgMapView.setGestureSupport(getGesture())
        svgMapView.setMapListener(object : SvgMapView.MapListener {

            override fun onLongClick(item: PathItem?, index: Int) {
                setSearchText(item)
                lastId = if (item == null) {
                    ""
                } else {
                    item.id
                }
                svgMapView.doAnimateCenter(index)
            }

            override fun onClick(item: PathItem?, index: Int) {

                setSearchText(item)
                if (item == null || TextUtils.isEmpty(item.id) || !svgMapView.hasMap(item.id)) {
                    lastId = ""
                    return
                }
                if (lastId != item.id) {
                    lastId = item.id
                    return
                }

                val intent = Intent(this@MainActivity, MainActivity::class.java)
                intent.putExtra(Key_Map_Name, item.id)
                startActivity(intent)
            }

            override fun onShow(item: PathItem, index: Int, size: Int) {
                setSearchText(item)

                val contact = Contact(index, item.title, item.getName(),null, item.getFlag())

                list.add(contact)

                if (index == size - 1) {
                    contactView.init(list, object : ContactsView.OnSelectChangedListener {
                        override fun onSelectChanged(index: Int, isChecked: Boolean) {
                            svgMapView.setSelected(index, isChecked, true)
                            lastId = if (isChecked) {
                                val item = svgMapView.doAnimateCenter(index)
                                setSearchText(item)
                                item?.id?:""
                            } else {
                                setSearchText(null)
                                svgMapView.doAnimateCenter(-1)
                                ""
                            }
                            drawer_layout.closeDrawer(GravityCompat.START)
                        }
                    })

                    setSearchText(null)
                }
            }
        })

        val mapName = intent.getStringExtra(Key_Map_Name)
        val colorSelected = getMapColor(SettingsActivity.SettingsFragment.Key_Selected_Color, Color.BLUE)
        val colorUnselected = getMapColor(SettingsActivity.SettingsFragment.Key_Unselected_Color, Color.GRAY)
        if (TextUtils.isEmpty(mapName)) {
            svgMapView.setMap(getMap(), colorSelected, colorUnselected)
        } else {
            svgMapView.setMap(mapName, colorSelected, colorUnselected)
        }

        fabFlag.setOnClickListener { view ->
            doFab(0)
        }

        fabEmblem.setOnClickListener { view ->
            doFab(1)
        }

        fabLocation.setOnClickListener { view ->
            doFab(2)
        }

        fabGone()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity<SettingsActivity>()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

//    override fun onNavigationItemSelected(item: MenuItem): Boolean {
//        // Handle navigation view item clicks here.
//        when (item.itemId) {
//            R.id.nav_camera -> {
//                // Handle the camera action
//            }
//            R.id.nav_gallery -> {
//
//            }
//            R.id.nav_slideshow -> {
//
//            }
//            R.id.nav_manage -> {
//
//            }
//            R.id.nav_share -> {
//
//            }
//            R.id.nav_send -> {
//
//            }
//        }
//
//        drawer_layout.closeDrawer(GravityCompat.START)
//        return true
//    }

    private fun fabGone() {
        fabFlag.visibility = View.GONE
        fabEmblem.visibility = View.GONE
        fabLocation.visibility = View.GONE
    }

    private fun doFab(type: Int) {
        val item = btnSearch.tag as PathItem
        when (item) {
            null -> return
            else -> {
                var resId: Int? = null
                when (type) {
                    0 -> {
                        //Flag
                        resId = ActivityUtils.getStringResId(this@MainActivity, "flag_${item.id}")
                    }
                    1 -> {
                        //Emblem
                        resId = ActivityUtils.getStringResId(this@MainActivity, "emblem_${item.id}")
                    }
                    2 -> {
                        //Location
                        resId = ActivityUtils.getStringResId(this@MainActivity, "location_${item.id}")
                    }
                }
                if (resId == null) {
                    return
                }
//                WebViewDialog.show(this, getString(resId))
//                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(resId))))
                ImageShow.show(this@MainActivity, getString(resId))
//                ImageShow.show(this@MainActivity, "https://bbs.qn.img-space.com/201803/1/dfd2eb58639be0c55be4802b99b50fb6.jpg")
            }
        }
    }

    private fun getMapColor(key: String, defColor: Int): Int {
        val mSharedPreferences = getSharedPreferences(SettingsActivity.PREFER_NAME, Context.MODE_PRIVATE)
        var color = mSharedPreferences.getString(key, ColorUtils.toHexEncoding(defColor))
        if (TextUtils.isEmpty(color)) {
            color = ColorUtils.toHexEncoding(defColor)
        }
        return Color.parseColor("#$color")
    }

    private fun getGesture(): Boolean {
        val mSharedPreferences = getSharedPreferences(SettingsActivity.PREFER_NAME, Context.MODE_PRIVATE)
        return mSharedPreferences.getBoolean(SettingsActivity.SettingsFragment.Key_Gesture, false)
    }

    private fun setSearchText(item: PathItem?) {
        runOnUiThread {
            if (item == null) {
                btnSearch.text = null
                btnSearch.tag = null
                fabGone()
            } else {
                btnSearch.text = item.getName()
//                Glide.with(this).load(getString(R.string.flag_cn)).into(ivFlag)
                if (ActivityUtils.getStringResId(this@MainActivity, "flag_${item.id}") == null) {
                    fabFlag.visibility = View.GONE
                } else {
                    fabFlag.visibility = View.VISIBLE
                }
                if (ActivityUtils.getStringResId(this@MainActivity, "emblem_${item.id}") == null) {
                    fabEmblem.visibility = View.GONE
                } else {
                    fabEmblem.visibility = View.VISIBLE
                }
                if (ActivityUtils.getStringResId(this@MainActivity, "location_${item.id}") == null) {
                    fabLocation.visibility = View.GONE
                } else {
                    fabLocation.visibility = View.VISIBLE
                }
                btnSearch.tag = item
            }

        }
    }

    private fun doBaike(text: String) {
        val mSharedPreferences = getSharedPreferences(SettingsActivity.PREFER_NAME, Context.MODE_PRIVATE)
        var url = mSharedPreferences.getString(SettingsActivity.SettingsFragment.Key_Baike, "")
        if (TextUtils.isEmpty(url)) {
            val urls = resources.getStringArray(R.array.baike_url)
            val index = RandomUtils.getRandom(1, urls.size - 1)
            url = urls[index]
        }
        WebViewDialog.show(this, url + text)
    }

    private fun getMap(): String {
        val mSharedPreferences = getSharedPreferences(SettingsActivity.PREFER_NAME, Context.MODE_PRIVATE)
        return mSharedPreferences.getString(SettingsActivity.SettingsFragment.Key_Map, "cn")
    }
}
