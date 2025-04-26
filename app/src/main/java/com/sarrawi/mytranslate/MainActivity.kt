package com.sarrawi.mytranslate

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.sarrawi.mytranslate.adapter.SpinnerAdapter
import com.sarrawi.mytranslate.databinding.ActivityMainBinding
import com.sarrawi.mytranslate.model.Item

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

// إخفاء اسم التطبيق من الـ Toolbar
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)

        appBarConfiguration = AppBarConfiguration.Builder(R.id.translateFragment, R.id.historyFragment,R.id.splashScreenFragment)
             // إذا كنت تستخدم درج التنقل
            .build()
        setupActionBarWithNavController(navController, appBarConfiguration)


//        val options = listOf("Translate", "History","Camera")
        val options = listOf("Translate", "History")
//        val options = listOf(
//            Pair(R.drawable.ic_clear, "Home"),
//            Pair(R.drawable.ic_clear, "Favorites"),
//            Pair(R.drawable.ic_clear, "Settings")
//        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

//        val options = listOf(
//            Item("Translate"),
//            Item("History")
//        )
//
//        val spinner = findViewById<Spinner>(R.id.spinner_fragment)
//        val adapter = SpinnerAdapter(this, options)
//        spinner.adapter = adapter


        val spinnerFragment = findViewById<Spinner>(R.id.spinner_fragment)
        spinnerFragment.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> navController.navigate(R.id.translateFragment)
                    1 -> navController.navigate(R.id.historyFragment)
                    //2 -> navController.navigate(R.id.translateCameraFragment)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        spinnerFragment.adapter = adapter
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

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}

//spinner = findViewById(R.id.swapfrag)
//
//        val fragmentNames = listOf("الواجهة الأولى", "الواجهة الثانية")
//        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, fragmentNames)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spinner.adapter = adapter
//
//        // تحميل أول Fragment بشكل افتراضي
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragment_container, FirstFragment())
//            .commit()
//
//        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>, view: View?, position: Int, id: Long
//            ) {
//                val selectedFragment = when (position) {
//                    0 -> FirstFragment()
//                    1 -> SecondFragment()
//                    else -> FirstFragment()
//                }
//
//                supportFragmentManager.beginTransaction()
//                    .replace(R.id.fragment_container, selectedFragment)
//                    .commit()
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {}
//        }
//    }