package com.backflippedstudios.crypto_ta

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.backflippedstudios.crypto_ta.data.DataSource
import com.backflippedstudios.crypto_ta.frags.DetailedAnalysisFrag
import com.backflippedstudios.crypto_ta.frags.MarketCapFrag
import com.backflippedstudios.crypto_ta.frags.MarketOverviewFrag
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.QuerySnapshot
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.activity_swipable_tabs.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity(){
    lateinit var tab1Frag: DetailedAnalysisFrag
    lateinit var tab2Frag: MarketCapFrag
    object data{
        val dataSource = DataSource()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("Loading Main activity")
        setContentView(R.layout.activity_swipable_tabs)
        AndroidThreeTen.init(this);
        DetailedAnalysisFrag.data.mFirebaseAnalytics = FirebaseAnalytics.getInstance(applicationContext)
        val settings: FirebaseFirestoreSettings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build()
        val fsdb = FirebaseFirestore.getInstance()
        fsdb.firestoreSettings = settings


        //Init viewPager
        val adapter = ViewPagerAdapter(supportFragmentManager)

        if(DataSource.data.coins.isEmpty()) {
            loadCoins()
        }

        tab1Frag = DetailedAnalysisFrag()
        tab2Frag = MarketCapFrag()
        val tab3Frag = MarketOverviewFrag()



        adapter.addFragment(tab1Frag,tab1Frag.title)
        adapter.addFragment(tab2Frag,tab2Frag.title)
        adapter.addFragment(tab3Frag,tab3Frag.title)
        //To ensure that the first page stays in memory
        val viewPager: CustViewPager = this.viewpager as CustViewPager
        viewPager.offscreenPageLimit = 5
        viewPager.adapter = adapter


        tablayout.setupWithViewPager(viewPager)


    }

    private fun loadCoins() = runBlocking{
        GlobalScope.launch {
            val resultFirestoreItemCount: Task<QuerySnapshot>?
//            DetailedAnalysisFrag.data.mFirebaseAnalytics = FirebaseAnalytics.getInstance(applicationContext)
            println("$$$$$$$\$Getting FirestoreCount")
            resultFirestoreItemCount = data.dataSource.getFirestoreItemCount(context = applicationContext)
            resultFirestoreItemCount.addOnSuccessListener { it ->

                println("$$$$$$$\$Getting DAO count")
                val daoCount = data.dataSource.getDAOItemCount(applicationContext)
                it.forEach {
                    println("DAO count:$daoCount FirestoreCount: ${it.data["count"].toString()}")
                    if (it.data["count"].toString().toInt() == daoCount) {
                        data.dataSource.loadFromDAO(applicationContext)
                        tab1Frag.processInit(true)
                        tab2Frag.processGraphs()
                        println("Finished initial loading ")
                    } else {
                        data.dataSource.clearDAO(applicationContext)
                        val result = data.dataSource.intCoins3(applicationContext)
                        result.addOnSuccessListener {
                            tab1Frag.processInit(true)
                            tab2Frag.processGraphs()
                            println("Finished initial loading ")
                        }
                    }
                    return@addOnSuccessListener
                }
            }
            resultFirestoreItemCount.addOnCompleteListener {  }
            resultFirestoreItemCount.addOnFailureListener {
                println("Failed to get Count")
            }
        }
    }
}