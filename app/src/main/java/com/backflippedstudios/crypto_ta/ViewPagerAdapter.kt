package com.backflippedstudios.crypto_ta

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class ViewPagerAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm) {
    private val mFragmentList: ArrayList<Fragment> = ArrayList()
    private val mFragmentTileList: ArrayList<String> = ArrayList()
    override fun getCount(): Int {
        return mFragmentList.size
    }

    override fun getItem(p0: Int): Fragment {
        return mFragmentList[p0]
    }

    fun addFragment(frag: Fragment, title: String){
        mFragmentList.add(frag)
        mFragmentTileList.add(title)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mFragmentTileList[position]
    }

}