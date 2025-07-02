package com.example.pcpartpicker

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MainPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val listNames: List<String>
)  : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 1 + listNames.size

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) {
            MainSearchFragment()
        }
        else {
            ComponentListFragment.newInstance(listNames[position -1])
        }
    }
}