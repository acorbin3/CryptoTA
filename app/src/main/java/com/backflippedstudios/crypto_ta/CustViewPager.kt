package com.backflippedstudios.crypto_ta

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

class CustViewPager(context: Context, att: AttributeSet): ViewPager(context,att){
    private var swipeEnabled: Boolean = false

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return if(swipeEnabled)
            super.onTouchEvent(ev)
        else
            false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return if(swipeEnabled)
            super.onInterceptTouchEvent(ev)
        else
            false
    }

    override fun onPageScrolled(position: Int, offset: Float, offsetPixels: Int) {

        swipeEnabled = !(position == 0 && offset.compareTo(0)== 0  && offsetPixels == 0)
        super.onPageScrolled(position, offset, offsetPixels)
    }

}