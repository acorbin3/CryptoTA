package com.backflippedstudios.crypto_ta

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
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
