package com.backflippedstudios.crypto_ta.customchartmods

import android.graphics.Canvas
import android.graphics.Path
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.renderer.LineChartRenderer
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.ViewPortHandler
import com.github.mikephil.charting.data.Entry


/**
 * Created by C0rbin on 11/26/2017.
 */
class MyLineLegendRenderer(chart: LineDataProvider?, animator: ChartAnimator?, viewPortHandler: ViewPortHandler?) : LineChartRenderer(chart, animator, viewPortHandler) {


    override fun drawLinearFill(c: Canvas?, dataSet: ILineDataSet, trans: Transformer?, bounds: XBounds?) {
        val filled = mGenerateFilledPathBuffer

        val startingIndex = bounds!!.min
        val endingIndex: Int = bounds.range + bounds.min
        val indexInterval = 128

        var currentStartIndex = 0
        var currentEndIndex = startingIndex
        var iterations = 0

        // Doing this iteratively in order to avoid OutOfMemory errors that can happen on large bounds sets.
        do {

            currentStartIndex = currentEndIndex
            currentEndIndex = currentStartIndex + indexInterval
            currentEndIndex = if (currentEndIndex >= endingIndex) endingIndex else currentEndIndex

            if (currentStartIndex <= currentEndIndex) {

                //START GEN FILLED PATH
                val boundaryEntry = dataSet.fillFormatter.fillLineBoundary
                val phaseY = mAnimator.phaseY
                filled.reset()

                //Add top line, add cross point, add bottom points
                //Detail top, loop over index util we find a starting good value then run till we find cross or we hit the endIndex
                //Add cross based on last endIndx or cross point
                //Detail bottom, start a cross point or end index and loop back to start index

                var currentEntry: Entry? = null
                var previousEntry: Entry? = null
                //Detail top, loop over index util we find a cross or we hit the endIndex
//                println("Mainline")
                var goodStart = false
                for (x in currentStartIndex + 1..currentEndIndex) {

                    currentEntry = dataSet.getEntryForIndex(x)
                    if(x >= boundaryEntry.size){
                        break
                    }
                    if(boundaryEntry[x].y >= currentEntry.y) {
                        if(!goodStart){
//                            println("Start looking for good points. currentStartIndex=$currentStartIndex")
                            goodStart = true
                            currentStartIndex = x
                            val entry = dataSet.getEntryForIndex(currentStartIndex)
                            filled.moveTo(entry.x, boundaryEntry[currentStartIndex].y)
                            filled.lineTo(entry.x, entry.y * phaseY)
                        }
//                        println("(${currentEntry.x},${currentEntry.y})")
                        filled.lineTo(currentEntry!!.x, currentEntry.y * phaseY)
                    }
                    else{
                        //if we found a good path, time to move on and update currentEndIndex  & currentStartIndex
                        if(goodStart){
                            currentEndIndex = x
//                            println("Stop looking for good points. Update currentEndIndex:$currentEndIndex")
                            break
                        }
//                        println("(${currentEntry.x},${currentEntry.y}) Nomatch")
                    }
                    //If we find a cross point, update current end and break out of loop
                    //NOTE: Since we are working with floats, this probably wont work due to rounding error
                    if (boundaryEntry[x].y == currentEntry.y){
//                        print("Found intersection point")
                       currentEndIndex = x
                        break
                    }

                }

                //If we didnt find a good start then we can move on to the next block of indexes
                if(!goodStart){
//                    println("Didnt find a good set, moving on to next block set")
                    iterations++
                    //Check to see if we are at the end, if so we can break
                    if (currentStartIndex == endingIndex){
                        break
                    }else
                    continue
                }

                //GET the intersection when we have a crossing in the between 2 lines
                //Since the y value on the X point where we think it crosses might not have the same y value
                // We muse use the same x with the other Y to close up the shape
                //Get intersection point of 2 lines
                //Get slope of currentEntry
                var mainlineCurrent = dataSet.getEntryForIndex(currentEndIndex)
                var mainlinePrevious = dataSet.getEntryForIndex(currentEndIndex-1)
                var slopeMainline = getSlope(mainlinePrevious, mainlineCurrent)
                var yInterceptMainline = getYIntercept(mainlineCurrent, slopeMainline)
                //get slope of boundary entry
                if(currentEndIndex> boundaryEntry.size){
                    continue
                }
                var boundaryCurrent = boundaryEntry[currentEndIndex]
                var boundaryPrevious = boundaryEntry[currentEndIndex-1]
                var slopeBoundary = getSlope(boundaryPrevious, boundaryCurrent)
                var yInterceptBoundary = getYIntercept(boundaryCurrent, slopeBoundary)

//                println("mc(${mainlineCurrent.x},${mainlineCurrent.y}) mp(${mainlinePrevious.x},${mainlinePrevious.y}) slope:$slopeMainline y-Intercept:$yInterceptMainline")
//                println("bc(${boundaryCurrent.x},${boundaryCurrent.y}) bp(${boundaryPrevious.x},${boundaryPrevious.y}) slope:$slopeBoundary y-Intercept:$yInterceptBoundary")

                //Find x intersection(mx + b) = (m1x + b1)-> mx - m1x = b1 - b -> x = b1 - b/(mx-m1)
                var xIntersection = Math.abs((yInterceptMainline - yInterceptBoundary)/(slopeMainline - slopeBoundary))
                //Find y-Intersection with one of the line equations above
                var yIntersection = (slopeMainline * xIntersection) + yInterceptMainline
//                println("Intersection: ($xIntersection,$yIntersection)")

                //If the calculated xInterseciton is inbetween x1 and x2 then we will use it. Otherwise we need to use the
                //end from currentEndIndex
                if(mainlinePrevious.x < xIntersection && mainlineCurrent.x > xIntersection) {
                    filled.lineTo(xIntersection, yIntersection * phaseY)
                }else{
                    filled.lineTo(mainlineCurrent.x, boundaryCurrent.y * phaseY)
                }

                //Bottom line:  start a cross point or end index and loop back to start index
//                println("Bounded line")
                for (x in currentEndIndex downTo currentStartIndex) {
                    previousEntry = boundaryEntry[x]
                    if(dataSet.getEntryForIndex(x).y <= previousEntry.y) {
//                        println("(${previousEntry.x},${previousEntry.y})")
                        filled.lineTo(previousEntry!!.x, previousEntry.y * phaseY)
                    }
                }

                //Since the y value on the X point where we think it crosses might not have the same y value
                // We muse use the same x with the other Y to close up the shape
//                filled.lineTo(dataSet.getEntryForIndex(currentStartIndex)!!.getX(), dataSet.getEntryForIndex(currentStartIndex).y * phaseY)

                filled.close()
                //END GEN FILLEDS PATH

                //Some issues where the left part goes to zero zero
                trans?.pathValueToPixel(filled)

                val drawable = dataSet.fillDrawable
                if (drawable != null) {

                    drawFilledPath(c, filled, drawable)
                } else {

                    drawFilledPath(c, filled, dataSet.fillColor, dataSet.fillAlpha)
                }
            }

            iterations++

        } while (currentStartIndex <= currentEndIndex)
    }

    private fun getYIntercept(mainlineCurrent: Entry, slope: Float) =
            mainlineCurrent.y - (slope * mainlineCurrent.x)

    private fun getSlope(previousPoint: Entry, mainlineCurrent: Entry) =
            (previousPoint.y - mainlineCurrent.y) / (previousPoint.x - mainlineCurrent.x)

    //Only add points if boundary line is above the main line
    private fun generateFilledPath(dataSet: ILineDataSet, startIndex: Int, endIndex: Int, outputPath: Path) {

        //Call the custom method to retrieve the dataset for other line
        val boundaryEntry = dataSet.fillFormatter.fillLineBoundary
        println("Calling custom gen Filled Path")

        val phaseY = mAnimator.phaseY
        outputPath.reset()

        val entry = dataSet.getEntryForIndex(startIndex)
        println("phaseY $phaseY")
        println("entry.y ${entry.y} boundaryEntry[0].y ${boundaryEntry[0].y}")
        outputPath.moveTo(entry.x, boundaryEntry[0].y)
        outputPath.lineTo(entry.x, entry.y * phaseY)


        // create a new path
        var currentEntry: Entry? = null
        var previousEntry: Entry? = null

        //Add top line, add cross point, add bottom points
        //Detail top, loop over index util we find a cross or we hit the endIndex
        //Add cross based on last endIndx or cross point
        //Detail bottom, start a cross point or end index and loop back to start index

        for (x in startIndex + 1..endIndex) {
            currentEntry = dataSet.getEntryForIndex(x)
            if(boundaryEntry[x].y >= currentEntry.y) {
                outputPath.lineTo(currentEntry!!.x, currentEntry.y * phaseY)
            }
            println("currentEntry!!.getX() ${currentEntry!!.x} currentEntry[$x].y ${currentEntry.y}")
        }

        // close up
        if (currentEntry != null && previousEntry != null) {
            println("closeUP currentEntry!!.getX() ${currentEntry.x} previousEntry!!.getY() ${previousEntry.y}")
            outputPath.lineTo(currentEntry.x, previousEntry.y)
        }

        //Draw the path towards the other line
        println("Draw path towards the other line")
        for (x in endIndex downTo startIndex + 1) {
            previousEntry = boundaryEntry[x]
            if(dataSet.getEntryForIndex(x).y <= previousEntry.y) {
                outputPath.lineTo(previousEntry!!.x, previousEntry.y * phaseY)
            }
            println("previousEntry!!.getX() ${previousEntry!!.x} boundaryEntry[$x].y ${previousEntry.y}")
        }

        outputPath.close()
    }
}