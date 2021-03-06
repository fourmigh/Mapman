package org.caojun.mapman

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.preference.*
import android.text.TextUtils
import android.widget.ImageView
import org.caojun.color.ColorActivity
import org.caojun.color.ColorUtils
import org.jetbrains.anko.startActivityForResult

class SettingsActivity: PreferenceActivity() {

    companion object {
        val PREFER_NAME = "org.caojun.mapman.settings"

        val Request_Selected_Color = 1
        val Request_Unselected_Color = 2
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 指定保存文件名字
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
    }

    class SettingsFragment : PreferenceFragment(), Preference.OnPreferenceChangeListener {

        companion object {
            val Key_Selected_Color = "map_selected_color"
            val Key_Unselected_Color = "map_unselected_color"
            val Key_Baike = "lp_baike"
            val Key_Gesture = "sp_gesture"
            val Key_Map = "lp_map"
        }
        private var mSharedPreferences: SharedPreferences? = null
        private var ivSetColor: ImageView? = null

        override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
            if (preference is ListPreference) {
                update(preference, newValue as String)
            }
            return true
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            preferenceManager.sharedPreferencesName = PREFER_NAME
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.settings)

            val lpBaike = findPreference(Key_Baike) as ListPreference
            lpBaike.onPreferenceChangeListener = this
            if (TextUtils.isEmpty(lpBaike.value)) {
                lpBaike.setValueIndex(0)
            }

            val lpMap = findPreference(Key_Map) as ListPreference
            lpMap.onPreferenceChangeListener = this

            if (TextUtils.isEmpty(lpMap.value)) {
                lpMap.setValueIndex(2)
            }

            mSharedPreferences = activity.getSharedPreferences(SettingsActivity.PREFER_NAME, Context.MODE_PRIVATE)
            val url = mSharedPreferences?.getString(Key_Baike, "")?:""
            update(lpBaike, url)

            val map = mSharedPreferences?.getString(Key_Map, "cn")?:"cn"
            update(lpMap, map)

            val etpSelectedColor = findPreference("etpSelectedColor")
            etpSelectedColor.onPreferenceClickListener = object : Preference.OnPreferenceClickListener {
                override fun onPreferenceClick(preference: Preference?): Boolean {
                    val dialog = (preference as EditTextPreference).dialog
                    ivSetColor = dialog.findViewById(R.id.ivSetColor)
                    ivSetColor?.setOnClickListener {
                        val color = getColor(Key_Selected_Color, Color.BLUE)
                        startActivityForResult<ColorActivity>(Request_Selected_Color, ColorActivity.KEY_HEX to color)
                    }

                    var color = getColor(Key_Selected_Color, Color.BLUE)
                    ivSetColor?.setBackgroundColor(Color.parseColor("#$color"))
                    return false
                }
            }

            val etpUnselectedColor = findPreference("etpUnselectedColor")
            etpUnselectedColor.onPreferenceClickListener = object : Preference.OnPreferenceClickListener {
                override fun onPreferenceClick(preference: Preference?): Boolean {
                    val dialog = (preference as EditTextPreference).dialog
                    ivSetColor = dialog.findViewById(R.id.ivSetColor)
                    ivSetColor?.setOnClickListener {
                        val color = getColor(Key_Unselected_Color, Color.GRAY)
                        startActivityForResult<ColorActivity>(Request_Unselected_Color, ColorActivity.KEY_HEX to color)
                    }

                    var color = getColor(Key_Unselected_Color, Color.GRAY)
                    ivSetColor?.setBackgroundColor(Color.parseColor("#$color"))
                    return false
                }
            }
        }

        private fun getColor(key: String, defColor: Int): String {
            var color = mSharedPreferences?.getString(key, ColorUtils.toHexEncoding(defColor))?:""
            if (TextUtils.isEmpty(color)) {
                color = ColorUtils.toHexEncoding(defColor)
            }
            return color
        }

        private fun update(lp: ListPreference, value: String) {

            when (lp.key) {
                Key_Baike -> {
                    val index = lp.findIndexOfValue(value)
                    val baikes = resources.getStringArray(R.array.baike)

                    lp.summary = baikes[index]
                }
                Key_Map -> {
                    val index = lp.findIndexOfValue(value)
                    val maps = resources.getStringArray(R.array.map_first)

                    lp.summary = maps[index]
                }
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (resultCode == Activity.RESULT_OK) {
                when(requestCode) {
                    Request_Selected_Color, Request_Unselected_Color -> {
                        val color = data?.getStringExtra(ColorActivity.KEY_HEX)
                        val edit = mSharedPreferences!!.edit()
                        if (requestCode == Request_Selected_Color) {
                            edit.putString(Key_Selected_Color, color)
                        } else if (requestCode == Request_Unselected_Color) {
                            edit.putString(Key_Unselected_Color, color)
                        }
                        edit.commit()
                        ivSetColor?.setBackgroundColor(Color.parseColor("#$color"))
                        return
                    }
                }
            }
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}