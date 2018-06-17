package com.backflippedstudios.crypto_ta

import com.backflippedstudios.crypto_ta.Overlay.Kind.*
import com.backflippedstudios.crypto_ta.dropdownmenus.OverlayAdapter
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import org.ta4j.core.BaseTimeSeries
import org.ta4j.core.Tick
import org.ta4j.core.TimeSeries
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator
import java.util.ArrayList
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator
import org.ta4j.core.Decimal
import org.ta4j.core.indicators.*
import org.ta4j.core.indicators.ichimoku.*
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator
import kotlin.math.absoluteValue


/**
 * Created by C0rbin on 11/8/2017.
 */
class TechnicalAnalysis {
    //TODO - use Overlay.kind to structure how we orgnize our data sets
    var candlestickData = ArrayList<CandleEntry>()
    val ticksDataArray = ArrayList<Tick>()
    var bbUpperData = ArrayList<Entry>()
    var bbMiddleData = ArrayList<Entry>()
    var bbLowerData = ArrayList<Entry>()
    var emaData = ArrayList<Entry>()
    var smaData = ArrayList<Entry>()
    var parabolicSAR_Data = ArrayList<Entry>()
    var keltnerChannelUpperData = ArrayList<Entry>()
    var keltnerChannelMiddleData = ArrayList<Entry>()
    var keltnerChannelLowerData = ArrayList<Entry>()
    var chandelierExitData = ArrayList<Entry>()
    var ichCloud_ConversionData = ArrayList<Entry>()
    var ichCloud_BaseData = ArrayList<Entry>()
    var ichCloud_LeadingAData = ArrayList<Entry>()
    var ichCloud_LeadingBData = ArrayList<Entry>()
    var ichCloud_LaggingData = ArrayList<Entry>()
    var zigZagData = ArrayList<Entry>()
    var emaRibbonData = ArrayList<ArrayList<Entry>>()
    var ts : TimeSeries? = null
    lateinit var closePrice: ClosePriceIndicator
    var loaded: Boolean = false
    var volumeBarData = ArrayList<BarEntry>()
    var aroonUpIndicatorData = ArrayList<Entry>()
    var aroonDownIndicatorData = ArrayList<Entry>()
    var aroonOscillatorData = ArrayList<Entry>()


    fun getData(kind: Overlay.Kind): ArrayList<ArrayList<Entry>>{
        var list: ArrayList<ArrayList<Entry>> = ArrayList()
        when(kind){
            Overlay.Kind.AroonUpDown -> {
                list.add(aroonDownIndicatorData)
                list.add(aroonUpIndicatorData)
                return list
            }
            Overlay.Kind.AroonOsci -> {
                list.add(aroonOscillatorData)
                return list
            }
            Overlay.Kind.Bollinger_Bands -> {
                list.add(bbMiddleData)
                list.add(bbUpperData)
                list.add(bbLowerData)
                return list
            }
            Overlay.Kind.Keltner_Channel -> {
                list.add(keltnerChannelMiddleData)
                list.add(keltnerChannelUpperData)
                list.add(keltnerChannelLowerData)
                return list
            }
            Overlay.Kind.Simple_Moving_Avg -> {
                list.add(smaData)
                return list
            }
            Overlay.Kind.Exponential_MA -> {
                list.add(emaData)
                return list
            }
            Overlay.Kind.Parabolic_SAR -> {
                list.add(parabolicSAR_Data)
                return list
            }
            Overlay.Kind.Chandelier_Exit -> {
                list.add(chandelierExitData)
                return list
            }
            Overlay.Kind.Ichimoku_Cloud -> {
                list.add(ichCloud_LeadingAData)
                list.add(ichCloud_LeadingBData)


                list.add(ichCloud_ConversionData)
                list.add(ichCloud_BaseData)
                list.add(ichCloud_LaggingData)
                return list
            }
            Overlay.Kind.ZigZag -> {
                list.add(zigZagData)
                return list
            }
            Overlay.Kind.Exponential_MA_Ribbon -> {
                return emaRibbonData
            }
        }
        return list
    }

    fun clearAll(){
        this.ticksDataArray.clear()
        candlestickData.clear()
        bbLowerData.clear()
        bbMiddleData.clear()
        bbUpperData.clear()
        emaData.clear()
        smaData.clear()
        parabolicSAR_Data.clear()
        keltnerChannelLowerData.clear()
        keltnerChannelMiddleData.clear()
        keltnerChannelUpperData.clear()
        chandelierExitData.clear()
        ichCloud_BaseData.clear()
        ichCloud_ConversionData.clear()
        ichCloud_LaggingData.clear()
        ichCloud_LeadingAData.clear()
        ichCloud_LeadingBData.clear()
        zigZagData.clear()
        emaRibbonData.clear()
        volumeBarData.clear()
        aroonUpIndicatorData.clear()
        aroonDownIndicatorData.clear()
    }




    constructor(ticksDataArray: ArrayList<Tick>){
        if(ticksDataArray.size>0) {
            this.ticksDataArray.clear()
            this.ticksDataArray.addAll(ticksDataArray)
            this.ts = BaseTimeSeries(this.ticksDataArray)
            this.closePrice = ClosePriceIndicator(this.ts)
            updateTA(this.ticksDataArray)
        }
    }

    private fun addXNumberOfEmptyBarEntries(start: Int, numOfEntries: Int, data: ArrayList<BarEntry>){
        for(i in start..(start + numOfEntries)){
            data.add(i, BarEntry(i.toFloat(),0F))
        }
    }

    private fun addXNumberOfEmptyEntries(start: Int, numOfEntries: Int, data: ArrayList<Entry>){
        for(i in start..(start + numOfEntries)){
            data.add(i, BarEntry(i.toFloat(),0F))
        }
    }

    fun updateOverlay(overlay: Overlay) {
        println("Updating " + overlay.title)
        when(overlay.kind){
            Volume_Bars ->{}
            Bollinger_Bands ->{
                bbUpperData.clear()
                bbMiddleData.clear()
                bbLowerData.clear()
                updateBollingerBandsData(
                        this.closePrice,
                        overlay.values[overlay.timeFrame].value.toInt()
                )
            }
            Keltner_Channel ->{
                keltnerChannelLowerData.clear()
                keltnerChannelMiddleData.clear()
                keltnerChannelUpperData.clear()
                updatekeltnerChannel(
                        closePrice,
                        overlay.values[overlay.timeFrame].value.toInt(),
                        overlay.values[overlay.ratio].value.toInt()
                )
            }
            Exponential_MA ->{
                this.emaData.clear()
                updateEMA(closePrice,overlay.values[overlay.timeFrame].value.toInt())

            }
            Simple_Moving_Avg ->{
                this.smaData.clear()
                updateSMA(closePrice,overlay.values[overlay.timeFrame].value.toInt())
            }
            Parabolic_SAR ->{
                this.parabolicSAR_Data.clear()
                this.ts?.let { updateParabolicSAR(
                        it,
                        overlay.values[overlay.accelerationFactor].value,
                        overlay.values[overlay.maximumAcceleration].value
                ) }
            }
            Chandelier_Exit ->{
                this.chandelierExitData.clear()
                this.ts?.let { updateChandelierExit(
                        it,
                        overlay.values[overlay.timeFrame].value.toInt(),
                        Decimal.valueOf(overlay.values[overlay.ratio].value.toInt())
                ) }
            }
            Ichimoku_Cloud ->{
                this.ichCloud_BaseData.clear()
                this.ichCloud_ConversionData.clear()
                this.ichCloud_LaggingData.clear()
                this.ichCloud_LeadingAData.clear()
                this.ichCloud_LeadingBData.clear()
                updateIchimokuCloud(
                        overlay.values[overlay.conversionPeriod].value.toInt(),
                        overlay.values[overlay.basePeriod].value.toInt(),
                        overlay.values[overlay.leadingPeriod].value.toInt(),
                        overlay.values[overlay.laggingPeriod].value.toInt()
                )
            }
            ZigZag ->{
                this.zigZagData.clear()
                updateZigZag(overlay.values[overlay.thresholdPercent].value)
            }
            Exponential_MA_Ribbon ->{
                this.emaRibbonData.clear()
                updateEMARibbon()
            }
        }
    }



    private fun updateEMARibbon() {
        for(timeframe in 20..55 step 5){
            val ema = EMAIndicator(closePrice, timeframe)
            var emaList = ArrayList<Entry>()
            for (j in 0 until closePrice.timeSeries.tickCount) {
                emaList.add(Entry(j.toFloat(),ema.getValue(j).toDouble().toFloat()))
            }
            this.emaRibbonData.add(emaList)
        }
    }


    //init last piviot
    //loop over all the values
    //Looking for % > threashold
    //When found

    private fun updateZigZag(thresholdPercent: Double) {
//        val zigZagPoints = ArrayList<Decimal>()
        var swingHigh = false
        var swingLow = false

//        println("ZigZag: $thresholdPercent")
        var observationLow = ts?.getTick(0)?.closePrice
        var obsLowIndex = 0
        var observationHigh = ts?.getTick(0)?.closePrice
        var obsHighIndex = 0

        for (i in 0..closePrice.getTimeSeries().getTickCount()-1) {
//            println("$i: ${closePrice.getValue(i)} ")
            //Add initial point as a base
            if (i == 0){
                this.zigZagData.add(Entry(i.toFloat(),closePrice.getValue(i).toDouble().toFloat()))
                continue
            }

            if (ts?.getTick(i)?.maxPrice?.isGreaterThan(observationHigh)!!) {
                observationHigh = ts?.getTick(i)?.maxPrice
                obsHighIndex = i
//                println("Updating high obs with: $observationHigh .. Percent: ${observationHigh.minus(observationLow)
//                        .dividedBy(observationLow)
//                        .multipliedBy(Decimal.HUNDRED)}")
                if (!swingLow && observationHigh?.minus(observationLow)
                        ?.dividedBy(observationLow)
                        ?.multipliedBy(Decimal.HUNDRED)
                        ?.isGreaterThanOrEqual(Decimal.valueOf(thresholdPercent))!!) {
                    observationLow?.toDouble()?.toFloat()?.let { Entry(obsLowIndex.toFloat(), it) }?.let { this.zigZagData.add(it) }
//                    println("Low ($obsLowIndex,${observationLow})")
                    swingHigh = false
                    swingLow = true
                }
                if (swingLow) observationLow = observationHigh

            } else if (ts!!.getTick(i).minPrice.isLessThan(observationLow)) {
                observationLow = ts?.getTick(i)?.minPrice
                obsLowIndex = i
//                println("Updating low obs with: $observationLow .. Percent: ${observationHigh.minus(observationLow)
//                        .dividedBy(observationLow)
//                        .multipliedBy(Decimal.HUNDRED)}")
                if (!swingHigh && observationHigh?.minus(observationLow)
                        ?.dividedBy(observationLow)
                        ?.multipliedBy(Decimal.HUNDRED)
                        ?.isGreaterThanOrEqual(Decimal.valueOf(thresholdPercent))!!) {
                    this.zigZagData.add(Entry(obsHighIndex.toFloat(),observationHigh.toDouble().toFloat()))
//                    println("High ($obsHighIndex,${observationHigh})")
                    swingHigh = true
                    swingLow = false
                }
                if (swingHigh) observationHigh = observationLow
            }
        }//Forloop

        //Add the last swing point even if it hasnt met the threashold
        if(swingLow){
            observationHigh?.toDouble()?.toFloat()?.let { Entry(obsHighIndex.toFloat(), it) }?.let { this.zigZagData.add(it) }
        }else{
            observationLow?.toDouble()?.toFloat()?.let { Entry(obsLowIndex.toFloat(), it) }?.let { this.zigZagData.add(it) }
        }
    }
    private fun updateIchimokuCloud(conversionPeriod: Int, basePeriod: Int, leadingPeriod: Int, laggingPeriod: Int) {
       if(closePrice.timeSeries.tickCount < 52){
            return
        }
        var conversionData = IchimokuTenkanSenIndicator(ts, conversionPeriod)
        var baseData = IchimokuKijunSenIndicator(ts,basePeriod)
        var leadingAData = IchimokuSenkouSpanAIndicator(ts, conversionPeriod,basePeriod)
        var leadingBData = IchimokuSenkouSpanBIndicator(ts,leadingPeriod)
        var laggingData = IchimokuChikouSpanIndicator(ts,laggingPeriod)
        for (j in 0 until closePrice.timeSeries.tickCount) {
            var leadX = j.toFloat() + laggingPeriod
//            println("X: $leadX A ${leadingAData.getValue(j)} B ${leadingBData.getValue(j)}")
            this.ichCloud_ConversionData.add(Entry(j.toFloat(), conversionData.getValue(j).toDouble().toFloat()))
            this.ichCloud_BaseData.add(Entry(j.toFloat(), baseData.getValue(j).toDouble().toFloat()))
            this.ichCloud_LeadingAData.add(Entry(leadX, leadingAData.getValue(j).toDouble().toFloat()))
            this.ichCloud_LeadingBData.add(Entry(leadX, leadingBData.getValue(j).toDouble().toFloat()))
            if(j >= leadingPeriod)
                this.ichCloud_LaggingData.add(Entry(j.toFloat()-leadingPeriod, laggingData.getValue(j).toDouble().toFloat()))
        }
        //TODO-enhancement:  might want to recalculate only when these are selected
        updateVolumeBarData()
        updateAroonOscilatorData()
        updateAroonUpDownData()
    }

    //This function will update all the TA for the set of data
    private fun updateTA(ticksDataArray: ArrayList<Tick>){
        loaded = true
        updateVolumeBarData()
        if(MainActivity.data.endTA) return // Stop TA due to UI Update

        updateAroonOscilatorData()
        if(MainActivity.data.endTA) return // Stop TA due to UI Update

        updateAroonUpDownData()
        if(MainActivity.data.endTA) return // Stop TA due to UI Update

        updateCandlestickData(ticksDataArray,addTimeSeriesData = false)
        if(MainActivity.data.endTA) return // Stop TA due to UI Update

        updateBollingerBandsData(this.closePrice,20)
        if(MainActivity.data.endTA) return // Stop TA due to UI Update

        updateEMA(this.closePrice,20)
        if(MainActivity.data.endTA) return // Stop TA due to UI Update

        updateSMA(this.closePrice,20)
        if(MainActivity.data.endTA) return // Stop TA due to UI Update

        updatekeltnerChannel(this.closePrice,14,2)
        if(MainActivity.data.endTA) return // Stop TA due to UI Update

        this.ts?.let { updateParabolicSAR(it,0.025,0.050) }
        if(MainActivity.data.endTA) return // Stop TA due to UI Update

        this.ts?.let { updateChandelierExit(it,22, Decimal.valueOf(1)) }
        if(MainActivity.data.endTA) return // Stop TA due to UI Update

        updateIchimokuCloud(9,26,52,26)
        if(MainActivity.data.endTA) return // Stop TA due to UI Update

        updateZigZag(7.0)
        if(MainActivity.data.endTA) return // Stop TA due to UI Update

        updateEMARibbon()

    }

    private fun updateBollingerBandsData(closePrice: ClosePriceIndicator, timeFrame: Int){
        val sma = SMAIndicator(closePrice, timeFrame)

        val bbmSMA = BollingerBandsMiddleIndicator(sma)
        val standardDeviation = StandardDeviationIndicator(closePrice, timeFrame)
        val bbuSMA = BollingerBandsUpperIndicator(bbmSMA, standardDeviation)
        val bblSMA = BollingerBandsLowerIndicator(bbmSMA, standardDeviation)

        //Populate data for Upper, middle and lower
        for (j in 0 until closePrice.timeSeries.tickCount) {
            this.bbUpperData.add(Entry(j.toFloat(), bbuSMA.getValue(j).toDouble().toFloat()))
            this.bbMiddleData.add(Entry(j.toFloat(), bbmSMA.getValue(j).toDouble().toFloat()))
            this.bbLowerData.add(Entry(j.toFloat(), bblSMA.getValue(j).toDouble().toFloat()))
        }
    }

    // Function used to ensure all charts that have their own data get updated
    fun updateIndividualChartData(){
        updateAroonOscilatorData()
        updateAroonUpDownData()
        updateVolumeBarData()
    }

    fun updateAroonOscilatorData(){
        this.aroonOscillatorData.clear()
        var timeFrame: Int = 0
        var laggingPeriod: Int = 0
        for(overlay in OverlayAdapter.data.list) {
            if (overlay.kind == Overlay.Kind.AroonOsci) {
                timeFrame = overlay.values[Overlay(AroonOsci).timeFrame].value.toInt()
            }
            if( overlay.kind == Overlay.Kind.Ichimoku_Cloud){
                laggingPeriod = overlay.values[Overlay(Ichimoku_Cloud).laggingPeriod].value.toInt()
            }
        }
        var aroonOscillatorIndicator = AroonOscillatorIndicator(this.ts, timeFrame)
        for (j in 0 until closePrice.timeSeries.tickCount) {
            this.aroonOscillatorData.add(Entry(j.toFloat(),aroonOscillatorIndicator.getValue(j).toDouble().toFloat()))
        }

        //Attempt to update the Incimoku Cloud due to it goes further in the future & we need to make sure
        // that the volume bars are aligned
        if(isIchCloudSelected() and !MainActivity.data.runningTA) {
            addXNumberOfEmptyEntries(ticksDataArray.indices.last,laggingPeriod,this.aroonOscillatorData)
        }

    }

    private fun isIchCloudSelected(): Boolean {
        var ichCloudSelected: Boolean = false
        for (overlay in OverlayAdapter.data.list) {
            if ((overlay.kind == Ichimoku_Cloud) and overlay.selected) {
                ichCloudSelected = true
                break
            }
        }
        return ichCloudSelected
    }

    fun updateAroonUpDownData(){
        this.aroonDownIndicatorData.clear()
        this.aroonUpIndicatorData.clear()
        var timeFrame: Int = 0
        var laggingPeriod: Int = 0
        for(overlay in OverlayAdapter.data.list) {
            if (overlay.kind == Overlay.Kind.AroonUpDown) {
                timeFrame = overlay.values[Overlay(AroonUpDown).timeFrame].value.toInt()
            }
            if( overlay.kind == Overlay.Kind.Ichimoku_Cloud){
                laggingPeriod = overlay.values[Overlay(Ichimoku_Cloud).laggingPeriod].value.toInt()
            }
        }

        var aroonUpIndicatorData = AroonUpIndicator(this.ts, timeFrame)
        var aroonDownIndicatorData = AroonDownIndicator(this.ts, timeFrame)
        for (j in 0 until closePrice.timeSeries.tickCount) {
            this.aroonUpIndicatorData.add(Entry(j.toFloat(),aroonUpIndicatorData.getValue(j).toDouble().toFloat()))
            this.aroonDownIndicatorData.add(Entry(j.toFloat(),aroonDownIndicatorData.getValue(j).toDouble().toFloat()))
        }

        // Attempt to update the Incimoku Cloud due to it goes further in the future & we need to make sure
        // that the graph is aligned
        if(isIchCloudSelected() and !MainActivity.data.runningTA) {
            addXNumberOfEmptyEntries(ticksDataArray.indices.last,laggingPeriod,this.aroonUpIndicatorData)
            addXNumberOfEmptyEntries(ticksDataArray.indices.last,laggingPeriod,this.aroonDownIndicatorData)
        }

    }

    fun updateVolumeBarData(){
        println("Updating Volume data")
        this.volumeBarData.clear()
        for (i in this.ticksDataArray.indices) {
            val volume = this.ticksDataArray[i].volume.toDouble().toFloat()
            this.volumeBarData.add(BarEntry(
                    i.toDouble().toFloat(),
                    volume))

        }
        //Attempt to update the Incimoku Cloud due to it goes further in the future & we need to make sure
        // that the volume bars are aligned
        if(isIchCloudSelected() and !MainActivity.data.runningTA) {
            for(overlay in OverlayAdapter.data.list) {
                if( overlay.kind == Overlay.Kind.Ichimoku_Cloud){
                    addXNumberOfEmptyBarEntries(ticksDataArray.indices.last,overlay.values[Overlay(Ichimoku_Cloud).laggingPeriod].value.toInt(),this.volumeBarData)
                }
            }
        }
    }
    //This function updates the candlestickdata given the list of Ticks
    fun updateCandlestickData(ticksDataArray: ArrayList<Tick>,addTimeSeriesData: Boolean){
        if(addTimeSeriesData) {
            this.ticksDataArray.addAll(ticksDataArray)
            this.ts = BaseTimeSeries(this.ticksDataArray)
        }
        for (i in ticksDataArray.indices) {
            val candleEntry = CandleEntry(
                    i.toDouble().toFloat(),
                    ticksDataArray[i].maxPrice.toDouble().toFloat(),
                    ticksDataArray[i].minPrice.toDouble().toFloat(),
                    ticksDataArray[i].openPrice.toDouble().toFloat(),
                    ticksDataArray[i].closePrice.toDouble().toFloat()
            )
            this.candlestickData.add(candleEntry)

            val volume = ticksDataArray[i].volume.toDouble().toFloat()
            this.volumeBarData.add(BarEntry(
                    i.toDouble().toFloat(),
                    volume
            ))
        }
    }

    //Exponential Moving Average
    private fun updateEMA(closePrice: ClosePriceIndicator, timeFrame: Int){
        val ema = EMAIndicator(closePrice, timeFrame)

        for (i in 0 until closePrice.timeSeries.tickCount) {
            this.emaData.add(Entry(i.toFloat(),ema.getValue(i).toDouble().toFloat()))
        }
    }

    //Simple Moving Average
    private fun updateSMA(closePrice: ClosePriceIndicator, timeFrame: Int){
        val sma = SMAIndicator(closePrice, timeFrame)

        for (i in 0 until closePrice.timeSeries.tickCount) {
            this.smaData.add(Entry(i.toFloat(),sma.getValue(i).toDouble().toFloat()))
        }
    }

    //keltnerChannel
    private fun updatekeltnerChannel(closePrice: ClosePriceIndicator, timeFrame: Int, ratio: Int){
        val km = KeltnerChannelMiddleIndicator(closePrice, timeFrame)
        val kl = KeltnerChannelLowerIndicator(km, Decimal.valueOf(ratio), timeFrame)
        val ku = KeltnerChannelUpperIndicator(km, Decimal.valueOf(ratio), timeFrame)
        for (i in 0 until closePrice.timeSeries.tickCount) {
            val kuVal = ku.getValue(i).toDouble().toFloat()
            val kmVal = km.getValue(i).toDouble().toFloat()
            val klVal = kl.getValue(i).toDouble().toFloat()
            val percKUKM = kuVal/kmVal
            val percKLKM = kmVal/klVal
//            println("$i ku:${kuVal}\t%$percKUKM\tkm:${kmVal}\tkl:${klVal}\t%$percKLKM ")
            if(percKLKM.absoluteValue <= 1.05 && percKUKM.absoluteValue <= 1.05) {
                this.keltnerChannelUpperData.add(Entry(i.toFloat(), kuVal))
                this.keltnerChannelMiddleData.add(Entry(i.toFloat(), kmVal))
                this.keltnerChannelLowerData.add(Entry(i.toFloat(), klVal))
//                println("Add ${i}")
            }


        }

    }
    private fun updateParabolicSAR(timeSeries: TimeSeries, aF: Double, maxA: Double){
        val parSARData =  ParabolicSarIndicator(timeSeries,Decimal.valueOf(aF),Decimal.valueOf(maxA))
        for (i in 0 until closePrice.timeSeries.tickCount) {
            this.parabolicSAR_Data.add(Entry(i.toFloat(),parSARData.getValue(i).toDouble().toFloat()))
//            println("SAR data: ${parSARData.getValue(i).toDouble().toFloat()}")
        }
    }

    private fun updateChandelierExit(timeSeries: TimeSeries,timeFrame: Int, ratio: Decimal) {
        var chadExitData = ChandelierExitLongIndicator(timeSeries, timeFrame,ratio)
        for (i in 0 until closePrice.timeSeries.tickCount) {
            this.chandelierExitData.add(Entry(i.toFloat(),chadExitData.getValue(i).toDouble().toFloat()))
        }
    }
    //Ichmoto cloud
    //ZigZag
    //Detrend Price Ocelator
    //Commodity Channel Index
    //Average Directional Index
}