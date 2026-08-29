package com.backflippedstudios.crypto_ta

import android.app.Activity
import android.view.View
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.tabs.TabLayout

// Typed replacements for the removed kotlinx.android.synthetic accessors.
// Same names as the original synthetic properties so call sites stay unchanged.

// detail_analysis_main_layout
val View.tv_live_price: TextView get() = findViewById(R.id.tv_live_price)
val View.tv_usd_value: TextView get() = findViewById(R.id.tv_usd_value)
val View.spinner_time_period: Spinner get() = findViewById(R.id.spinner_time_period)
val View.spinner_coin_type: Spinner get() = findViewById(R.id.spinner_coin_type)
val View.tv_data_source: TextView get() = findViewById(R.id.tv_data_source)
val View.b_drawer: ImageView get() = findViewById(R.id.b_drawer)
val View.b_collapse_arrow: ImageView get() = findViewById(R.id.b_collapse_arrow)
val View.iv_share_screenshot: ImageView get() = findViewById(R.id.iv_share_screenshot)
val View.iv_feedback: ImageView get() = findViewById(R.id.iv_feedback)
val View.swipe_to_refresh_market_cap: SwipeRefreshLayout get() = findViewById(R.id.swipe_to_refresh_market_cap)
val View.indicators_recycler_view: RecyclerView get() = findViewById(R.id.indicators_recycler_view)
val View.all_charts_recycler_view: RecyclerView get() = findViewById(R.id.all_charts_recycler_view)

// market_overview_main_layout
val View.rv_market_overview: RecyclerView get() = findViewById(R.id.rv_market_overview)

// activity_swipable_tabs
val Activity.viewpager: CustViewPager get() = findViewById(R.id.viewpager)
val Activity.tablayout: TabLayout get() = findViewById(R.id.tablayout)
