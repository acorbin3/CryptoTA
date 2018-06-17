package com.backflippedstudios.crypto_ta.recyclerviews

import android.content.Context
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ItemDecoration
import android.view.View

class RecyclerViewMargin(context: Context) : ItemDecoration() {

    override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
        outRect?.right = 0
        outRect?.bottom = 0
        outRect?.left = 0
        outRect?.top = 0
    }
}