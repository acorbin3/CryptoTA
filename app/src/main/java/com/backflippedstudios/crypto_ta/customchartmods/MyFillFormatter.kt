package com.backflippedstudios.crypto_ta.customchartmods

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.data.LineDataSet



/**
 * Created by C0rbin on 11/26/2017.
 */
class MyFillFormatter(boundaryDataSet: ILineDataSet) : IFillFormatter {
    private var boundaryDataSet: ILineDataSet = boundaryDataSet

    override fun getFillLinePosition(dataSet: ILineDataSet?, dataProvider: LineDataProvider?): Float {
        return 0.0F
    }

    //Define a new method which is used in the LineChartRenderer
    override fun getFillLineBoundary(): List<Entry>? {
        return if (boundaryDataSet != null) {
            (boundaryDataSet as LineDataSet).values
        } else null
    }
}