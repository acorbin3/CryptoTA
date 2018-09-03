package com.backflippedstudios.crypto_ta

import com.backflippedstudios.crypto_ta.Overlay.Kind.*
import com.backflippedstudios.crypto_ta.dropdownmenus.OverlayAdapter
import com.facebook.R
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

    data class TAData(var data: ArrayList<*>, val kind: Overlay.Kind)

    val ticksDataArray = ArrayList<Tick>()


    var ts : TimeSeries? = null
    lateinit var closePrice: ClosePriceIndicator
    var loaded: Boolean = false
    var data : HashMap<Overlay.Kind, TAData> = HashMap()


    constructor(ticksDataArray: ArrayList<Tick>){

        initilize(ticksDataArray)
    }
    constructor(ticksDataArray: ArrayList<Tick>, candleStickData: TAData){

        initilize(ticksDataArray, candleStickData)
    }

    fun initilize(ticksDataArray: ArrayList<Tick>, candleStickData: TAData = TAData(ArrayList<Entry>(),None)) {
        for (kind in values()) {
            if (kind == Volume_Bars) {
                data[kind] = TAData(ArrayList<BarEntry>(), kind)
            } else if (kind == Exponential_MA_Ribbon) {
                data[kind] = TAData(ArrayList<ArrayList<Entry>>(), kind)
            } else if (kind == CandleStick) {
                if(candleStickData.kind == CandleStick){
                    data[kind] = candleStickData
                }else{
                    data[kind] = TAData(ArrayList<CandleEntry>(), kind)
                }

            } else {
                data[kind] = TAData(ArrayList<Entry>(), kind)
            }
        }

        if (ticksDataArray.size > 0) {
            this.ticksDataArray.clear()
            this.ticksDataArray.addAll(ticksDataArray)
            this.ts = BaseTimeSeries(this.ticksDataArray)
            this.closePrice = ClosePriceIndicator(this.ts)
            updateTASmart()
        }
    }

    fun getEntryData(kind: Overlay.Kind): ArrayList<Entry>{
        return data[kind]?.data as ArrayList<Entry>
    }
    fun getCandlestickData(kind: Overlay.Kind): ArrayList<CandleEntry>{
        return data[kind]?.data as ArrayList<CandleEntry>
    }
    fun getEntryDataList(kind: Overlay.Kind): ArrayList<ArrayList<Entry>>{
        return data[kind]?.data as ArrayList<ArrayList<Entry>>
    }
    fun getBarData(kind: Overlay.Kind): ArrayList<BarEntry>{
        return data[kind]?.data as ArrayList<BarEntry>
    }
    fun getData(kind: Overlay.Kind): ArrayList<ArrayList<Entry>>{
        var list: ArrayList<ArrayList<Entry>> = ArrayList()
        when(kind){
            Overlay.Kind.AroonUpDown -> {
                list.add(getEntryData(Overlay.Kind.D_AroonDown))
                list.add(getEntryData(Overlay.Kind.D_AroonUp))
                return list
            }
            Overlay.Kind.AroonOsci -> {
                list.add(getEntryData(AroonOsci))
                return list
            }
            Overlay.Kind.RSI ->{
                list.add(getEntryData(RSI))
                return list
            }
            Overlay.Kind.DPO ->{
                list.add(getEntryData(DPO))
                return list
            }
            Overlay.Kind.Stoch_Oscill ->{
                list.add(getEntryData(Overlay.Kind.D_Stoch_Oscill_K_Timeframe))
                list.add(getEntryData(Overlay.Kind.D_Stoch_Oscill_SMA_Timeframe))
            }
            Overlay.Kind.Bollinger_Bands -> {
                list.add(getEntryData(D_BB_Middle))
                list.add(getEntryData(D_BB_Upper))
                list.add(getEntryData(D_BB_Lower))
                return list
            }
            Overlay.Kind.Keltner_Channel -> {
                list.add(getEntryData(D_KC_Middle))
                list.add(getEntryData(D_KC_Upper))
                list.add(getEntryData(D_KC_Lower))
                return list
            }
            Overlay.Kind.Simple_Moving_Avg -> {
                list.add(this.getEntryData(Simple_Moving_Avg))
                return list
            }
            Overlay.Kind.Exponential_MA -> {
                list.add(getEntryData(Exponential_MA))
                return list
            }
            Overlay.Kind.Parabolic_SAR -> {
                list.add(getEntryData(Parabolic_SAR))
                return list
            }
            Overlay.Kind.Chandelier_Exit -> {
                list.add(getEntryData(Overlay.Kind.Chandelier_Exit))
                return list
            }
            Overlay.Kind.Ichimoku_Cloud -> {
                list.add(getEntryData(Overlay.Kind.D_Ich_Cloud_Lead_A))
                list.add(getEntryData(Overlay.Kind.D_Ich_Cloud_Lead_B))


                list.add(getEntryData(Overlay.Kind.D_Ich_Cloud_Conversion))
                list.add(getEntryData(Overlay.Kind.D_Ich_Cloud_Base))
                list.add(getEntryData(Overlay.Kind.D_Ich_Cloud_Lagging))
                return list
            }
            Overlay.Kind.ZigZag -> {
                list.add(getEntryData(ZigZag))
                return list
            }
            Overlay.Kind.Exponential_MA_Ribbon -> {
                return getEntryDataList(Overlay.Kind.Exponential_MA_Ribbon)
            }
        }
        return list
    }

    fun clearAll(){
        this.ticksDataArray.clear()
        for(item in this.data.values){
            item.data.clear()
        }
    }






    fun updateTASmart(){

        for(overlay in OverlayAdapter.data.list){
            if(!overlay.selected){
                continue
            }
            recalculateData(overlay.kind)
        }

    }

    fun recalculateData(overlayKind: Overlay.Kind) {
        //Check if the main item has data, and is not empty
        if(OverlayAdapter.data.all[overlayKind]?.kindData?.hasData!!){
            if(data[overlayKind]?.data?.isNotEmpty()!!){
                return
            }
        }else {
            //If item is a detailed item, check if item has data and is not empty
            for(item in OverlayAdapter.data.all.values){
                if(item.kindData.parentKind == overlayKind && data[item.kind]?.data?.isNotEmpty()!!){
                    return
                }
            }
        }


        when (overlayKind) {
            Volume_Bars -> updateVolumeBarData()
            AroonUpDown -> updateAroonUpDownData()
            AroonOsci -> updateAroonOscilatorData()
            RSI -> updateRSI(this.closePrice, OverlayAdapter.getTimeframe(RSI).toInt())
            DPO -> updateDPO(this.ts, OverlayAdapter.getTimeframe(DPO).toInt())
            Stoch_Oscill -> updateStochasticOscil(this.ts, OverlayAdapter.getTimeframe(Stoch_Oscill).toInt(),
                    OverlayAdapter.getTimeframeSMA(Stoch_Oscill).toInt())
            PPO -> updatePPO(OverlayAdapter.getShortTerm(Overlay.Kind.PPO).toInt(),
                    OverlayAdapter.getLongTerm(Overlay.Kind.PPO).toInt(),
                    OverlayAdapter.getPPOEMA(Overlay.Kind.PPO).toInt())
            Bollinger_Bands -> updateBollingerBandsData(this.closePrice, OverlayAdapter.getTimeframe(Bollinger_Bands).toInt())
            Keltner_Channel -> updatekeltnerChannel(this.closePrice, OverlayAdapter.getTimeframe(Keltner_Channel).toInt(),
                    OverlayAdapter.getRatio(Keltner_Channel).toInt())
            Simple_Moving_Avg -> updateSMA(this.closePrice, OverlayAdapter.getTimeframe(Simple_Moving_Avg).toInt())
            Exponential_MA -> updateEMA(this.closePrice, OverlayAdapter.getTimeframe(Exponential_MA).toInt())
            Parabolic_SAR -> updateParabolicSAR(OverlayAdapter.getAccFactor(Parabolic_SAR),
                    OverlayAdapter.getMaxAcc(Parabolic_SAR))
            Chandelier_Exit -> updateChandelierExit(this.ts, OverlayAdapter.getTimeframe(Chandelier_Exit).toInt(),
                    Decimal.valueOf(OverlayAdapter.getRatio(Parabolic_SAR)))
            Ichimoku_Cloud -> updateIchimokuCloud(OverlayAdapter.getConversionPeriod(Ichimoku_Cloud).toInt(),
                    OverlayAdapter.getBase(Ichimoku_Cloud).toInt(),
                    OverlayAdapter.getLeadingPeriod(Ichimoku_Cloud).toInt(),
                    OverlayAdapter.getLaggingPeriod(Ichimoku_Cloud).toInt())
            ZigZag -> updateZigZag(OverlayAdapter.getThresholdPercent(ZigZag))

            Exponential_MA_Ribbon -> updateEMARibbon()
        }
    }

    //This function will update all the TA for the set of data
    private fun updateTA(){
        loaded = true
        updateVolumeBarData()
        if(MainActivity.data.endTA) return // Stop TA due to UI Update

        updateAroonOscilatorData()
        if(MainActivity.data.endTA) return // Stop TA due to UI Update

        updateAroonUpDownData()
        if(MainActivity.data.endTA) return // Stop TA due to UI Update

        updateRSI(this.closePrice,
                OverlayAdapter.getTimeframe(Overlay.Kind.RSI).toInt())
        if(MainActivity.data.endTA) return // Stop TA due to UI Update

        updateDPO(this.ts,
                OverlayAdapter.getTimeframe(Overlay.Kind.DPO).toInt())
        if(MainActivity.data.endTA) return // Stop TA due to UI Update

        updateStochasticOscil(this.ts,
                OverlayAdapter.getTimeframe(Overlay.Kind.Stoch_Oscill).toInt(),
                OverlayAdapter.getTimeframeSMA(Overlay.Kind.Stoch_Oscill).toInt())

        updatePPO(OverlayAdapter.getShortTerm(Overlay.Kind.PPO).toInt(),
                OverlayAdapter.getLongTerm(Overlay.Kind.PPO).toInt(),
                OverlayAdapter.getPPOEMA(Overlay.Kind.PPO).toInt())

        if(MainActivity.data.endTA) return // Stop TA due to UI Update

        updateCandlestickData(ticksDataArray,addTimeSeriesData = false)
        if(MainActivity.data.endTA) return // Stop TA due to UI Update

        updateBollingerBandsData(this.closePrice,OverlayAdapter.getTimeframe(Overlay.Kind.Bollinger_Bands).toInt())
        if(MainActivity.data.endTA) return // Stop TA due to UI Update

        updateEMA(this.closePrice,OverlayAdapter.getTimeframe(Overlay.Kind.Exponential_MA).toInt())
        if(MainActivity.data.endTA) return // Stop TA due to UI Update

        updateSMA(this.closePrice,OverlayAdapter.getTimeframe(Overlay.Kind.Simple_Moving_Avg).toInt())
        if(MainActivity.data.endTA) return // Stop TA due to UI Update

        updatekeltnerChannel(this.closePrice,OverlayAdapter.getTimeframe(Overlay.Kind.Keltner_Channel).toInt(),
                OverlayAdapter.getRatio(Overlay.Kind.Keltner_Channel).toInt())
        if(MainActivity.data.endTA) return // Stop TA due to UI Update

        updateParabolicSAR(OverlayAdapter.getAccFactor(Overlay.Kind.Parabolic_SAR),
                OverlayAdapter.getMaxAcc(Overlay.Kind.Parabolic_SAR))
        if(MainActivity.data.endTA) return // Stop TA due to UI Update

        updateChandelierExit(this.ts,OverlayAdapter.getTimeframe(Overlay.Kind.Chandelier_Exit).toInt(),
                Decimal.valueOf(OverlayAdapter.getRatio(Overlay.Kind.Parabolic_SAR)))
        if(MainActivity.data.endTA) return // Stop TA due to UI Update

        updateIchimokuCloud(OverlayAdapter.getConversionPeriod(Overlay.Kind.Ichimoku_Cloud).toInt(),
                OverlayAdapter.getBase(Overlay.Kind.Ichimoku_Cloud).toInt(),
                OverlayAdapter.getLeadingPeriod(Overlay.Kind.Ichimoku_Cloud).toInt(),
                OverlayAdapter.getLaggingPeriod(Overlay.Kind.Ichimoku_Cloud).toInt())
        if(MainActivity.data.endTA) return // Stop TA due to UI Update

        updateZigZag(OverlayAdapter.getThresholdPercent(Overlay.Kind.ZigZag))
        if(MainActivity.data.endTA) return // Stop TA due to UI Update

        updateEMARibbon()

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
                updateBollingerBandsData(
                        this.closePrice,
                        overlay.values[overlay.timeFrame].value.toInt()
                )
            }
            RSI ->{
                updateRSI(this.closePrice, overlay.values[overlay.timeFrame].value.toInt())
            }
            DPO ->{
                getEntryData(DPO).clear()
                updateDPO(this.ts,overlay.values[overlay.timeFrame].value.toInt())
            }
            Stoch_Oscill ->{
                updateStochasticOscil(this.ts,overlay.values[overlay.timeFrame].value.toInt(),overlay.values[overlay.timeFrameSMA].value.toInt())
            }
            PPO ->{
                updatePPO(overlay.values[overlay.shortTerm].value.toInt(),
                        overlay.values[overlay.longTerm].value.toInt(),
                        overlay.values[overlay.PPO_EMA].value.toInt())
            }
            Keltner_Channel ->{
                getEntryData(D_KC_Upper).clear()
                getEntryData(D_KC_Middle).clear()
                getEntryData(D_KC_Lower).clear()
                updatekeltnerChannel(
                        closePrice,
                        overlay.values[overlay.timeFrame].value.toInt(),
                        overlay.values[overlay.ratio].value.toInt()
                )
            }
            Exponential_MA ->{
                this.getEntryData(Exponential_MA).clear()
                updateEMA(closePrice,overlay.values[overlay.timeFrame].value.toInt())

            }
            Simple_Moving_Avg ->{
                this.data[Simple_Moving_Avg]?.data?.clear()
                updateSMA(closePrice,overlay.values[overlay.timeFrame].value.toInt())
            }
            Parabolic_SAR ->{
                this.getEntryData(Parabolic_SAR).clear()
                updateParabolicSAR(
                        overlay.values[overlay.accelerationFactor].value,
                        overlay.values[overlay.maximumAcceleration].value
                )
            }
            Chandelier_Exit ->{
                this.getEntryData(Chandelier_Exit).clear()
                updateChandelierExit(
                        this.ts,
                        overlay.values[overlay.timeFrame].value.toInt(),
                        Decimal.valueOf(overlay.values[overlay.ratio].value.toInt())
                )
            }
            Ichimoku_Cloud ->{
                this.getEntryData(Overlay.Kind.D_Ich_Cloud_Base).clear()
                this.getEntryData(Overlay.Kind.D_Ich_Cloud_Conversion).clear()
                this.getEntryData(Overlay.Kind.D_Ich_Cloud_Lagging).clear()
                this.getEntryData(Overlay.Kind.D_Ich_Cloud_Lead_A).clear()
                this.getEntryData(Overlay.Kind.D_Ich_Cloud_Lead_B).clear()
                updateIchimokuCloud(
                        overlay.values[overlay.conversionPeriod].value.toInt(),
                        overlay.values[overlay.basePeriod].value.toInt(),
                        overlay.values[overlay.leadingPeriod].value.toInt(),
                        overlay.values[overlay.laggingPeriod].value.toInt()
                )
            }
            ZigZag ->{
                this.getEntryData(ZigZag).clear()
                updateZigZag(overlay.values[overlay.thresholdPercent].value)
            }
            Exponential_MA_Ribbon ->{
                this.getEntryDataList(Overlay.Kind.Exponential_MA_Ribbon).clear()
                updateEMARibbon()
            }
        }
    }



    private fun updateEMARibbon() {
        this.getEntryDataList(Overlay.Kind.Exponential_MA_Ribbon).clear()
        for(timeframe in 20..55 step 5){
            val ema = EMAIndicator(closePrice, timeframe)
            var emaList = ArrayList<Entry>()
            for (j in 0 until closePrice.timeSeries.tickCount) {
                emaList.add(Entry(j.toFloat(),ema.getValue(j).toDouble().toFloat()))
            }
            this.getEntryDataList(Overlay.Kind.Exponential_MA_Ribbon).add(emaList)
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
        this.getEntryData(ZigZag).clear()

        for (i in 0..closePrice.timeSeries.tickCount -1) {
//            println("$i: ${closePrice.getValue(i)} ")
            //Add initial point as a base
            if (i == 0){
                this.getEntryData(ZigZag).add(Entry(i.toFloat(),closePrice.getValue(i).toDouble().toFloat()))
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
                    observationLow?.toDouble()?.toFloat()?.let { Entry(obsLowIndex.toFloat(), it) }?.let { this.getEntryData(ZigZag).add(it) }
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
                    this.getEntryData(ZigZag).add(Entry(obsHighIndex.toFloat(),observationHigh.toDouble().toFloat()))
//                    println("High ($obsHighIndex,${observationHigh})")
                    swingHigh = true
                    swingLow = false
                }
                if (swingHigh) observationHigh = observationLow
            }
        }//Forloop

        //Add the last swing point even if it hasnt met the threashold
        if(swingLow){
            observationHigh?.toDouble()?.toFloat()?.let { Entry(obsHighIndex.toFloat(), it) }?.let { this.getEntryData(ZigZag).add(it) }
        }else{
            observationLow?.toDouble()?.toFloat()?.let { Entry(obsLowIndex.toFloat(), it) }?.let { this.getEntryData(ZigZag).add(it) }
        }
    }
    private fun updateIchimokuCloud(conversionPeriod: Int, basePeriod: Int, leadingPeriod: Int, laggingPeriod: Int) {
       if(closePrice.timeSeries.tickCount < 52){
            return
        }
        this.getEntryData(Overlay.Kind.D_Ich_Cloud_Conversion).clear()
        this.getEntryData(Overlay.Kind.D_Ich_Cloud_Base).clear()
        this.getEntryData(Overlay.Kind.D_Ich_Cloud_Lead_A).clear()
        this.getEntryData(Overlay.Kind.D_Ich_Cloud_Lead_B).clear()
        this.getEntryData(Overlay.Kind.D_Ich_Cloud_Lagging).clear()

        var conversionData = IchimokuTenkanSenIndicator(ts, conversionPeriod)
        var baseData = IchimokuKijunSenIndicator(ts,basePeriod)
        var leadingAData = IchimokuSenkouSpanAIndicator(ts, conversionPeriod,basePeriod)
        var leadingBData = IchimokuSenkouSpanBIndicator(ts,leadingPeriod)
        var laggingData = IchimokuChikouSpanIndicator(ts,laggingPeriod)
        for (j in 0 until closePrice.timeSeries.tickCount) {
            var leadX = j.toFloat() + laggingPeriod
//            println("X: $leadX A ${leadingAData.getValue(j)} B ${leadingBData.getValue(j)}")
            this.getEntryData(Overlay.Kind.D_Ich_Cloud_Conversion).add(Entry(j.toFloat(), conversionData.getValue(j).toDouble().toFloat()))
            this.getEntryData(Overlay.Kind.D_Ich_Cloud_Base).add(Entry(j.toFloat(), baseData.getValue(j).toDouble().toFloat()))
            this.getEntryData(Overlay.Kind.D_Ich_Cloud_Lead_A).add(Entry(leadX, leadingAData.getValue(j).toDouble().toFloat()))
            this.getEntryData(Overlay.Kind.D_Ich_Cloud_Lead_B).add(Entry(leadX, leadingBData.getValue(j).toDouble().toFloat()))
            if(j >= leadingPeriod)
                this.getEntryData(Overlay.Kind.D_Ich_Cloud_Lagging).add(Entry(j.toFloat()-leadingPeriod, laggingData.getValue(j).toDouble().toFloat()))
        }
    }

    private fun updateRSI(closePrice: ClosePriceIndicator, timeFrame: Int){
        this.getEntryData(RSI).clear()
        val rsi = RSIIndicator(closePrice, timeFrame)

        for(i in 0 until closePrice.timeSeries.tickCount){
            this.getEntryData(RSI).add(Entry(i.toFloat(),rsi.getValue(i).toDouble().toFloat()))
        }
    }
    private fun updateDPO(ts: TimeSeries?, timeFrame: Int){
        getEntryData(DPO).clear()
        val dpo = DPOIndicator(this.closePrice,timeFrame)
        val timeShift = timeFrame/2 + 1
        for(i in 0 until ts?.tickCount!!){
            if(i < timeShift){
                this.getEntryData(DPO).add(Entry(i.toFloat(), 0.0F))
            }else {
                this.getEntryData(DPO).add(Entry(i.toFloat(), dpo.getValue(i).toDouble().toFloat()))
            }
        }
    }
    private fun updateStochasticOscil(ts: TimeSeries?, timeFrame: Int, timeFrameSMA: Int){
        getEntryData(Overlay.Kind.D_Stoch_Oscill_K_Timeframe).clear()
        getEntryData(Overlay.Kind.D_Stoch_Oscill_SMA_Timeframe).clear()
        val sok = StochasticOscillatorKIndicator(ts,timeFrame)
        val sma = SMAIndicator(sok,timeFrameSMA)
        val sod = StochasticOscillatorDIndicator(sma)
        if(ts == null)
            return
        for(i in 0 until ts?.tickCount!!){
            this.getEntryData(Overlay.Kind.D_Stoch_Oscill_SMA_Timeframe).add(Entry(i.toFloat(), sod.getValue(i).toDouble().toFloat()))
            this.getEntryData(Overlay.Kind.D_Stoch_Oscill_K_Timeframe).add(Entry(i.toFloat(), sok.getValue(i).toDouble().toFloat()))
        }
    }
    private fun updatePPO(shortTerm: Int, longTerm: Int, emaTerm: Int){
        getEntryData(Overlay.Kind.D_PPO_ShortTerm).clear()
        getEntryData(Overlay.Kind.D_PPO_EMA).clear()

        val ppo = PPOIndicator(this.closePrice,shortTerm,longTerm)
        val ema = EMAIndicator(ppo,emaTerm)
        if(ts == null)
            return
        for(i in 0 until ts?.tickCount!!){
            this.getEntryData(Overlay.Kind.D_PPO_ShortTerm).add(Entry(i.toFloat(), ppo.getValue(i).toDouble().toFloat()))
            this.getEntryData(Overlay.Kind.D_PPO_EMA).add(Entry(i.toFloat(), ema.getValue(i).toDouble().toFloat()))
        }
    }

    private fun updateBollingerBandsData(closePrice: ClosePriceIndicator, timeFrame: Int){
        this.getEntryData(D_BB_Upper).clear()
        this.getEntryData(D_BB_Middle).clear()
        this.getEntryData(D_BB_Lower).clear()
        val sma = SMAIndicator(closePrice, timeFrame)

        val bbmSMA = BollingerBandsMiddleIndicator(sma)
        val standardDeviation = StandardDeviationIndicator(closePrice, timeFrame)
        val bbuSMA = BollingerBandsUpperIndicator(bbmSMA, standardDeviation)
        val bblSMA = BollingerBandsLowerIndicator(bbmSMA, standardDeviation)

        //Populate data for Upper, middle and lower
        for (j in 0 until closePrice.timeSeries.tickCount) {
            this.getEntryData(D_BB_Upper).add(Entry(j.toFloat(), bbuSMA.getValue(j).toDouble().toFloat()))
            this.getEntryData(D_BB_Middle).add(Entry(j.toFloat(), bbmSMA.getValue(j).toDouble().toFloat()))
            this.getEntryData(D_BB_Lower).add(Entry(j.toFloat(), bblSMA.getValue(j).toDouble().toFloat()))
        }
    }

    // Function used to ensure all charts that have their own data get updated
    fun updateIndividualChartData(){
        updateAroonOscilatorData()
        updateAroonUpDownData()
        updateVolumeBarData()
        updateRSI_Data()
        updateDPO_Data()
        updateStochOscil_Data()
        updatePPO_Data()
    }

    fun updateRSI_Data(){
        updateRSI(this.closePrice,
                OverlayAdapter.getTimeframe(Overlay.Kind.RSI).toInt())

        if(isIchCloudSelected() and !MainActivity.data.runningTA) {
            addXNumberOfEmptyEntries(ticksDataArray.indices.last,
                    OverlayAdapter.getLaggingPeriod(Overlay.Kind.Ichimoku_Cloud).toInt(),
                    this.getEntryData(RSI))
        }
    }

    fun updateDPO_Data(){
        updateDPO(this.ts,OverlayAdapter.getTimeframe(Overlay.Kind.DPO).toInt())
        if(isIchCloudSelected() and !MainActivity.data.runningTA) {
            addXNumberOfEmptyEntries(ticksDataArray.indices.last,
                    OverlayAdapter.getLaggingPeriod(Overlay.Kind.Ichimoku_Cloud).toInt(),
                    this.getEntryData(DPO))
        }
    }
    fun updateStochOscil_Data(){
        updateStochasticOscil(this.ts,
                OverlayAdapter.getTimeframe(Overlay.Kind.Stoch_Oscill).toInt(),
                OverlayAdapter.getTimeframeSMA(Overlay.Kind.Stoch_Oscill).toInt())
        if(isIchCloudSelected() and !MainActivity.data.runningTA) {
            addXNumberOfEmptyEntries(ticksDataArray.indices.last,
                    OverlayAdapter.getLaggingPeriod(Overlay.Kind.Ichimoku_Cloud).toInt(),
                    this.getEntryData(Overlay.Kind.D_Stoch_Oscill_SMA_Timeframe))
            addXNumberOfEmptyEntries(ticksDataArray.indices.last,
                    OverlayAdapter.getLaggingPeriod(Overlay.Kind.Ichimoku_Cloud).toInt(),
                    this.getEntryData(Overlay.Kind.D_Stoch_Oscill_K_Timeframe))
        }
    }
    fun updatePPO_Data(){
        updatePPO(OverlayAdapter.getShortTerm(Overlay.Kind.PPO).toInt(),
                OverlayAdapter.getLongTerm(Overlay.Kind.PPO).toInt(),
                OverlayAdapter.getPPOEMA(Overlay.Kind.PPO).toInt())
        if(isIchCloudSelected() and !MainActivity.data.runningTA) {
            addXNumberOfEmptyEntries(ticksDataArray.indices.last,
                    OverlayAdapter.getLaggingPeriod(Overlay.Kind.Ichimoku_Cloud).toInt(),
                    this.getEntryData(Overlay.Kind.D_PPO_ShortTerm))
            addXNumberOfEmptyEntries(ticksDataArray.indices.last,
                    OverlayAdapter.getLaggingPeriod(Overlay.Kind.Ichimoku_Cloud).toInt(),
                    this.getEntryData(Overlay.Kind.D_PPO_EMA))
        }
    }
    fun updateAroonOscilatorData(){
        this.getEntryData(AroonOsci).clear()
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
        if(this.ts == null)
            return
        for (j in 0 until this.ts?.tickCount!!) {
            this.getEntryData(AroonOsci).add(Entry(j.toFloat(),aroonOscillatorIndicator.getValue(j).toDouble().toFloat()))
        }

        //Attempt to update the Incimoku Cloud due to it goes further in the future & we need to make sure
        // that the volume bars are aligned
        if(isIchCloudSelected() and !MainActivity.data.runningTA) {
            addXNumberOfEmptyEntries(ticksDataArray.indices.last,laggingPeriod,this.getEntryData(AroonOsci))
        }

    }

    fun isIchCloudSelected(): Boolean {
        var ichCloudSelected: Boolean = false
        for (overlay in OverlayAdapter.data.list) {
            if ((overlay.kind == Ichimoku_Cloud) and overlay.selected) {
                ichCloudSelected = true
                break
            }
        }
        return ichCloudSelected
    }


    // Used to add or remove the lagging items to the separate charts to alight wtih Ich Cloud
    fun updateSeparateCharts(){
        var laggingPeriod: Int = 0
        for(overlay in OverlayAdapter.data.list) {
            if( overlay.kind == Overlay.Kind.Ichimoku_Cloud){
                laggingPeriod = overlay.values[Overlay(Ichimoku_Cloud).laggingPeriod].value.toInt()
            }
        }
        if(isIchCloudSelected()){
            if(this.getEntryData(Overlay.Kind.Volume_Bars).isNotEmpty())
                addXNumberOfEmptyBarEntries(ticksDataArray.indices.last,laggingPeriod,this.getBarData(Volume_Bars))
            if(this.getEntryData(Overlay.Kind.D_AroonUp).isNotEmpty())
                addXNumberOfEmptyEntries(ticksDataArray.indices.last,laggingPeriod,this.getEntryData(Overlay.Kind.D_AroonUp))
            if(this.getEntryData(Overlay.Kind.D_AroonDown).isNotEmpty())
                addXNumberOfEmptyEntries(ticksDataArray.indices.last,laggingPeriod,this.getEntryData(Overlay.Kind.D_AroonDown))
            if(this.getEntryData(Overlay.Kind.RSI).isNotEmpty())
                addXNumberOfEmptyEntries(ticksDataArray.indices.last,laggingPeriod,this.getEntryData(RSI))
            if(this.getEntryData(Overlay.Kind.AroonOsci).isNotEmpty())
                addXNumberOfEmptyEntries(ticksDataArray.indices.last,laggingPeriod,this.getEntryData(AroonOsci))
            if(this.getEntryData(Overlay.Kind.DPO).isNotEmpty())
                addXNumberOfEmptyEntries(ticksDataArray.indices.last,laggingPeriod,this.getEntryData(DPO))
            if(this.getEntryData(Overlay.Kind.D_Stoch_Oscill_SMA_Timeframe).isNotEmpty())
                addXNumberOfEmptyEntries(ticksDataArray.indices.last,laggingPeriod,this.getEntryData(D_Stoch_Oscill_SMA_Timeframe))
            if(this.getEntryData(Overlay.Kind.D_Stoch_Oscill_K_Timeframe).isNotEmpty())
                addXNumberOfEmptyEntries(ticksDataArray.indices.last,laggingPeriod,this.getEntryData(D_Stoch_Oscill_K_Timeframe))
        }else{
            for(i in 0..laggingPeriod) {
                if(this.getEntryData(Overlay.Kind.D_AroonUp).isNotEmpty())
                    this.getEntryData(Overlay.Kind.D_AroonUp).removeAt(this.getEntryData(Overlay.Kind.D_AroonUp).lastIndex)
                if(this.getEntryData(Overlay.Kind.D_AroonDown).isNotEmpty())
                    this.getEntryData(Overlay.Kind.D_AroonDown).removeAt(this.getEntryData(Overlay.Kind.D_AroonDown).lastIndex)
                if(this.getEntryData(Overlay.Kind.RSI).isNotEmpty())
                    this.getEntryData(RSI).removeAt(this.getEntryData(RSI).lastIndex)
                if(this.getEntryData(Overlay.Kind.DPO).isNotEmpty())
                    this.getEntryData(DPO).removeAt(this.getEntryData(DPO).lastIndex)
                if(this.getEntryData(Overlay.Kind.AroonOsci).isNotEmpty())
                    this.getEntryData(AroonOsci).removeAt(this.getEntryData(AroonOsci).lastIndex)
                if(this.getEntryData(Overlay.Kind.Volume_Bars).isNotEmpty())
                    this.getEntryData(Overlay.Kind.Volume_Bars).removeAt(this.getEntryData(Overlay.Kind.Volume_Bars).lastIndex!!)
                if(this.getEntryData(Overlay.Kind.D_Stoch_Oscill_SMA_Timeframe).isNotEmpty())
                    this.getEntryData(Overlay.Kind.D_Stoch_Oscill_SMA_Timeframe).removeAt(this.getEntryData(Overlay.Kind.D_Stoch_Oscill_SMA_Timeframe).lastIndex!!)
                if(this.getEntryData(Overlay.Kind.D_Stoch_Oscill_K_Timeframe).isNotEmpty())
                    this.getEntryData(Overlay.Kind.D_Stoch_Oscill_K_Timeframe).removeAt(this.getEntryData(Overlay.Kind.D_Stoch_Oscill_K_Timeframe).lastIndex!!)
            }
        }
    }

    fun updateAroonUpDownData(){
        this.getEntryData(Overlay.Kind.D_AroonUp).clear()
        this.getEntryData(Overlay.Kind.D_AroonDown).clear()
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
            this.getEntryData(Overlay.Kind.D_AroonUp).add(Entry(j.toFloat(),aroonUpIndicatorData.getValue(j).toDouble().toFloat()))
            this.getEntryData(Overlay.Kind.D_AroonDown).add(Entry(j.toFloat(),aroonDownIndicatorData.getValue(j).toDouble().toFloat()))
        }

        // Attempt to update the Incimoku Cloud due to it goes further in the future & we need to make sure
        // that the graph is aligned
        if(isIchCloudSelected() and !MainActivity.data.runningTA) {
            addXNumberOfEmptyEntries(ticksDataArray.indices.last,laggingPeriod,this.getEntryData(Overlay.Kind.D_AroonUp))
            addXNumberOfEmptyEntries(ticksDataArray.indices.last,laggingPeriod,this.getEntryData(Overlay.Kind.D_AroonDown))
        }

    }

    fun updateVolumeBarData(){
        println("Updating Volume data")
        this.data[Volume_Bars]?.data?.clear()
        for (i in this.ticksDataArray.indices) {
            val volume = this.ticksDataArray[i].volume.toDouble().toFloat()
            (this.data[Volume_Bars]?.data as ArrayList<Entry>).add(BarEntry(
                    i.toDouble().toFloat(),
                    volume
            ))
        }

        //Attempt to update the Incimoku Cloud due to it goes further in the future & we need to make sure
        // that the volume bars are aligned
        if(isIchCloudSelected() and !MainActivity.data.runningTA) {
            for(overlay in OverlayAdapter.data.list) {
                if( overlay.kind == Overlay.Kind.Ichimoku_Cloud){
                    addXNumberOfEmptyBarEntries(ticksDataArray.indices.last,overlay.values[Overlay(Ichimoku_Cloud).laggingPeriod].value.toInt(),this.getBarData(Volume_Bars))
                }
            }
        }
    }
    //This function updates the candlestickdata given the list of Ticks
    fun updateCandlestickData(ticksDataArray: ArrayList<Tick>,addTimeSeriesData: Boolean){
        if(addTimeSeriesData) {
            this.ticksDataArray.clear()
            this.ticksDataArray.addAll(ticksDataArray)
            this.ts = BaseTimeSeries(this.ticksDataArray)
            this.closePrice = ClosePriceIndicator(this.ts)
        }
        this.data[Volume_Bars]?.data?.clear()
        this.getCandlestickData(Overlay.Kind.CandleStick).clear()
        for (i in ticksDataArray.indices) {
            val candleEntry = CandleEntry(
                    i.toDouble().toFloat(),
                    ticksDataArray[i].maxPrice.toDouble().toFloat(),
                    ticksDataArray[i].minPrice.toDouble().toFloat(),
                    ticksDataArray[i].openPrice.toDouble().toFloat(),
                    ticksDataArray[i].closePrice.toDouble().toFloat()
            )
            this.getCandlestickData(Overlay.Kind.CandleStick).add(candleEntry)

            val volume = ticksDataArray[i].volume.toDouble().toFloat()
            (this.data[Volume_Bars]?.data as ArrayList<Entry>).add(BarEntry(
                    i.toDouble().toFloat(),
                    volume
            ))
        }
    }

    //Exponential Moving Average
    private fun updateEMA(closePrice: ClosePriceIndicator, timeFrame: Int){
        val ema = EMAIndicator(closePrice, timeFrame)

        for (i in 0 until closePrice.timeSeries.tickCount) {
            getEntryData(Exponential_MA).add(Entry(i.toFloat(),ema.getValue(i).toDouble().toFloat()))
        }
    }

    //Simple Moving Average
    private fun updateSMA(closePrice: ClosePriceIndicator, timeFrame: Int){
        val sma = SMAIndicator(closePrice, timeFrame)

        for (i in 0 until closePrice.timeSeries.tickCount) {
            (this.data[Simple_Moving_Avg]?.data as ArrayList<Entry>).add(Entry(i.toFloat(),sma.getValue(i).toDouble().toFloat()))
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
                this.getEntryData(D_KC_Upper).add(Entry(i.toFloat(), kuVal))
                this.getEntryData(D_KC_Middle).add(Entry(i.toFloat(), kmVal))
                this.getEntryData(D_KC_Lower).add(Entry(i.toFloat(), klVal))
//                println("Add ${i}")
            }


        }

    }
    private fun updateParabolicSAR(aF: Double, maxA: Double){
        val parSARData =  ParabolicSarIndicator(this.ts,Decimal.valueOf(aF),Decimal.valueOf(maxA))
        for (i in 0 until closePrice.timeSeries.tickCount) {
            this.getEntryData(Parabolic_SAR).add(Entry(i.toFloat(),parSARData.getValue(i).toDouble().toFloat()))
//            println("SAR data: ${parSARData.getValue(i).toDouble().toFloat()}")
        }
    }

    private fun updateChandelierExit(timeSeries: TimeSeries?,timeFrame: Int, ratio: Decimal) {
        var chadExitData = ChandelierExitLongIndicator(timeSeries, timeFrame,ratio)
        for (i in 0 until closePrice.timeSeries.tickCount) {
            this.getEntryData(Chandelier_Exit).add(Entry(i.toFloat(),chadExitData.getValue(i).toDouble().toFloat()))
        }
    }
    //Detrend Price Ocelator
    //Commodity Channel Index
    //Average Directional Index
}