package com.zxerrinor.githubstarwatchdog.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.omegar.mvp.MvpAppCompatActivity
import com.zxerrinor.githubstarwatchdog.R
import com.zxerrinor.githubstarwatchdog.clearDatabaseCache
import com.zxerrinor.githubstarwatchdog.ui.base.BaseActivity

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_clear_cache -> {
                clearDatabaseCache(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}