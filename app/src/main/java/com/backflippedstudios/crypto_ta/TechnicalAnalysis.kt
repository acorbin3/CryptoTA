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
import org.ta4j.core.indicators.pivotpoints.PivotLevel
import org.ta4j.core.indicators.pivotpoints.PivotPointIndicator
import org.ta4j.core.indicators.pivotpoints.StandardReversalIndicator
import org.ta4j.core.indicators.pivotpoints.TimeLevel
import org.ta4j.core.indicators.volume.*
import kotlin.math.absoluteValue


/**
 * Created by C0rbin on 11/8/2017.
 */
class TechnicalAnalysis {

    data class TAData(var data: ArrayList<*>, val kind: Overlay.Kind)

    val ticksDataArray = ArrayList<Tick>()


    var ts : TimeSeries? = null
    lateinit var closePrice: ClosePriceIndicator
    lateinit var vwapIndicator: VWAPIndicator
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

        for(overlay in OverlayAdapter.data.all.values){
            if(overlay.kindData.parentKind == kind && overlay.kindData.hasData){
                if(kind != Exponential_MA_Ribbon) {
                    list.add(getEntryData(overlay.kind))
                }else{
                    return getEntryDataList(overlay.kind)
                }
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


    fun updateNonSelectedItems(){
        try {
            for (overlay in OverlayAdapter.data.list) {
                if (overlay.selected) {
                    continue
                }
                recalculateData(overlay.kind)
            }
        }catch (e: Exception){

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

        //Check to see if we have a time series and close price
        if(this.ts == null || this.ts?.isEmpty == null || this.ts?.tickCount == 0 || this.closePrice.timeSeries.isEmpty) {
            return
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
            Hull_Moving_Average -> updateHMA(OverlayAdapter.getTimeframe(Hull_Moving_Average).toInt())
            Zero_Lag_Moving_Average -> updateZLEMA(OverlayAdapter.getTimeframe(Zero_Lag_Moving_Average).toInt())
            Volume_Weighted_Average_Price -> updateVWAP(OverlayAdapter.getTimeframe(Volume_Weighted_Average_Price).toInt())
            Moving_Volume_Weighted_Average_Price -> updateMVWAP(OverlayAdapter.getTimeframe(Moving_Volume_Weighted_Average_Price).toInt())
            Awesome_Oscillator -> updateAO(OverlayAdapter.getTimeframe(overlayKind).toInt(),OverlayAdapter.getTimeframe2(overlayKind).toInt())
            Rate_Of_Change -> updateROC(OverlayAdapter.getTimeframe(overlayKind).toInt())
            Chande_Momentum_Oscillator -> updateCMO(OverlayAdapter.getTimeframe(overlayKind).toInt())
            Coppock_Curve -> updateCC(OverlayAdapter.getTimeframe(overlayKind).toInt(),
                    OverlayAdapter.getTimeframe2(overlayKind).toInt(),
                    OverlayAdapter.getTimeframe3(overlayKind).toInt())
            Accumulation_Distribution -> updateAccumDist()
            Williams__R -> updateWR(OverlayAdapter.getTimeframe(overlayKind).toInt())
            Ulcer_Index -> updateUlcerIndex(OverlayAdapter.getTimeframe(overlayKind).toInt())
            Chaikin_Money_Flow ->updateChaikinMoneyFlow(OverlayAdapter.getTimeframe(overlayKind).toInt())
            Positive_Volume -> updatePositiveVolume()
            Negative_Volume -> updateNegativeVolume()
            On_Balance_Volume -> updateOnBalanceVolume()
            Piviot_Point -> updatePiviotPoints(TimeLevel.MONTH) //TODO - Add in some kind of selection from user
            Triple_EMA -> updateTripleEMA(OverlayAdapter.getTimeframe(overlayKind).toInt())
            Kaufman_Adaptive_MA -> updateKAMA(OverlayAdapter.getTimeframe(Kaufman_Adaptive_MA).toInt(),
                    OverlayAdapter.getTimeframeFast(Kaufman_Adaptive_MA).toInt(),
                    OverlayAdapter.getTimeframeSlow(Kaufman_Adaptive_MA).toInt())
            Exponential_MA_Ribbon -> updateEMARibbon()
        }

        //Add extra padding for seperate charts when IchCloud is on
        if(isIchCloudSelected()) {
            for(overlay in OverlayAdapter.data.all){
                if(overlayKind == overlay.value.kindData.parentKind
                        && OverlayAdapter.data.all[overlay.value.kindData.parentKind]?.separateChart!!
                        && overlay.value.kindData.hasData
                ){
                    if(overlay.value.kind == Volume_Bars){
                        if(this.getEntryData(overlay.value.kind).isNotEmpty())
                            addXNumberOfEmptyBarEntries(ticksDataArray.indices.last,
                                    OverlayAdapter.getLaggingPeriod(Overlay.Kind.Ichimoku_Cloud).toInt(),
                                    this.getBarData(Volume_Bars))
                    }else{
                        if(this.getEntryData(overlay.value.kind).isNotEmpty())
                            addXNumberOfEmptyEntries(ticksDataArray.indices.last,
                                    OverlayAdapter.getLaggingPeriod(Overlay.Kind.Ichimoku_Cloud).toInt(),
                                    this.getEntryData(overlay.value.kind))
                    }
                }
            }

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

    private fun updateEMARibbon() {
        this.getEntryDataList(Overlay.Kind.Exponential_MA_Ribbon).clear()
        for(timeframe in 20..55 step 5){
            val ema = EMAIndicator(closePrice, timeframe)
            var emaList = ArrayList<Entry>()
            for (j in 0 until closePrice.timeSeries.tickCount) {
                if(ema.timeSeries.tickCount > j)
                    emaList.add(Entry(j.toFloat(),ema.getValue(j).toDouble().toFloat()))
            }
            if(!emaList.isEmpty())
                this.getEntryDataList(Overlay.Kind.Exponential_MA_Ribbon).add(emaList)
        }
    }

    private fun updateHMA(timeFrame: Int){
        this.getEntryData(Hull_Moving_Average).clear()
        var hmaData = HMAIndicator(closePrice,timeFrame)
        for (i in 0 until closePrice.timeSeries.tickCount) {
            if(hmaData.timeSeries.tickCount > i)
                this.getEntryData(Hull_Moving_Average).add(Entry(i.toFloat(),hmaData.getValue(i).toDouble().toFloat()))
        }
    }

    private fun updateZLEMA(timeFrame: Int){
        this.getEntryData(Zero_Lag_Moving_Average).clear()
        var data = ZLEMAIndicator(closePrice,timeFrame)
        for (i in 0 until closePrice.timeSeries.tickCount) {
            if(data.timeSeries.tickCount > i)
                this.getEntryData(Zero_Lag_Moving_Average).add(Entry(i.toFloat(),data.getValue(i).toDouble().toFloat()))
        }
    }

    private fun updateVWAP(timeFrame: Int){
        this.getEntryData(Volume_Weighted_Average_Price).clear()
        var data = VWAPIndicator(this.ts,timeFrame)
        this.vwapIndicator = data
        for (i in 0 until this.ts?.tickCount!!) {
            if(data.timeSeries.tickCount > i)
                this.getEntryData(Volume_Weighted_Average_Price).add(Entry(i.toFloat(),data.getValue(i).toDouble().toFloat()))
        }
    }

    private fun updateMVWAP(timeFrame: Int){
        this.getEntryData(Moving_Volume_Weighted_Average_Price).clear()
        var data = MVWAPIndicator(this.vwapIndicator,timeFrame)
        for (i in 0 until this.ts?.tickCount!!) {
            if(data.timeSeries.tickCount > i)
                this.getEntryData(Moving_Volume_Weighted_Average_Price).add(Entry(i.toFloat(),data.getValue(i).toDouble().toFloat()))
        }
    }

    private fun updateKAMA(timeFrameRatio: Int, timeFrameFast: Int, timeFrameSlow: Int){
        this.getEntryData(Kaufman_Adaptive_MA).clear()
        var data = KAMAIndicator(this.closePrice,timeFrameRatio, timeFrameFast, timeFrameSlow)
        for (i in 0 until closePrice.timeSeries.tickCount) {
            val value = data.getValue(i).toDouble().toFloat()
            this.getEntryData(Kaufman_Adaptive_MA).add(Entry(i.toFloat(),value))
        }
    }

    private fun updateAO(timeFrame1: Int, timeFrame2: Int){
        this.getEntryData(Awesome_Oscillator).clear()
        var data = AwesomeOscillatorIndicator(this.closePrice,timeFrame1,timeFrame2)
        for (i in 0 until closePrice.timeSeries.tickCount) {
            if(data.timeSeries.tickCount > i) {
                val value = data.getValue(i).toDouble().toFloat()
                this.getEntryData(Awesome_Oscillator).add(Entry(i.toFloat(), value))
            }
        }
    }

    private fun updateROC(timeFrame: Int){
        this.getEntryData(Rate_Of_Change).clear()
        var data = ROCIndicator(this.closePrice,timeFrame)
        for (i in 0 until closePrice.timeSeries.tickCount) {
            if(data.timeSeries.tickCount > i){
                val value = data.getValue(i).toDouble().toFloat()
                this.getEntryData(Rate_Of_Change).add(Entry(i.toFloat(),value))
            }
        }
    }

    private fun updateCMO(timeFrame: Int){
        var kind = Chande_Momentum_Oscillator
        this.getEntryData(kind).clear()
        var data = CMOIndicator(this.closePrice,timeFrame)
        for (i in 0 until closePrice.timeSeries.tickCount) {
            if(data.timeSeries.tickCount > i){
                val value = data.getValue(i).toDouble().toFloat()
                this.getEntryData(kind).add(Entry(i.toFloat(),value))
            }
        }
    }

    private fun updateCC(longTimeFrame: Int, shortTimeframe: Int, wmaTimeFrame: Int){
        var kind = Coppock_Curve
        this.getEntryData(kind).clear()
        var data = CoppockCurveIndicator(this.closePrice,longTimeFrame,shortTimeframe,wmaTimeFrame)
        for (i in 0 until closePrice.timeSeries.tickCount) {
            if(data.timeSeries.tickCount > i){
                val value = data.getValue(i).toDouble().toFloat()
                this.getEntryData(kind).add(Entry(i.toFloat(),value))
            }
        }
    }
    private fun updateWR(timeFrame: Int){
        var kind = Williams__R
        this.getEntryData(kind).clear()
        var data = WilliamsRIndicator(this.ts,timeFrame)
        for (i in 0 until closePrice.timeSeries.tickCount) {
            if(data.timeSeries.tickCount > i){
                val value = data.getValue(i).toDouble().toFloat()
                this.getEntryData(kind).add(Entry(i.toFloat(),value))
            }
        }
    }

    private fun updateUlcerIndex(timeFrame: Int){
        var kind = Ulcer_Index
        this.getEntryData(kind).clear()
        var data = UlcerIndexIndicator(this.closePrice,timeFrame)
        for (i in 0 until closePrice.timeSeries.tickCount) {
            if(data.timeSeries.tickCount > i){
                val value = data.getValue(i).toDouble().toFloat()
                this.getEntryData(kind).add(Entry(i.toFloat(),value))
            }
        }
    }

    private fun updateChaikinMoneyFlow(timeFrame: Int){
        var kind = Chaikin_Money_Flow
        this.getEntryData(kind).clear()
        var data = ChaikinMoneyFlowIndicator(this.ts,timeFrame)
        for (i in 0 until closePrice.timeSeries.tickCount) {
            if(data.timeSeries.tickCount > i){
                val value = data.getValue(i).toDouble().toFloat()
                this.getEntryData(kind).add(Entry(i.toFloat(),value))
            }
        }
    }

    private fun updatePositiveVolume(){
        var kind = Positive_Volume
        this.getEntryData(kind).clear()
        var data = PVIIndicator(this.ts)
        for (i in 0 until this.ts?.tickCount!!) {
            if(data.timeSeries.tickCount > i){
                val value = data.getValue(i).toDouble().toFloat()
                this.getEntryData(kind).add(Entry(i.toFloat(),value))
            }
        }
    }

    private fun updateNegativeVolume(){
        var kind = Negative_Volume
        this.getEntryData(kind).clear()
        var data = NVIIndicator(this.ts)
        for (i in 0 until this.ts?.tickCount!!) {
            if(data.timeSeries.tickCount > i){
                val value = data.getValue(i).toDouble().toFloat()
                this.getEntryData(kind).add(Entry(i.toFloat(),value))
            }
        }
    }

    private fun updateOnBalanceVolume(){
        var kind = On_Balance_Volume
        this.getEntryData(kind).clear()
        var data = OnBalanceVolumeIndicator(this.ts)
        for (i in 0 until this.ts?.tickCount!!) {
            if(data.timeSeries.tickCount > i){
                val value = data.getValue(i).toDouble().toFloat()
                this.getEntryData(kind).add(Entry(i.toFloat(),value))
            }
        }
    }

    private fun updatePiviotPoints(timeLevel: TimeLevel){
        var kind = Piviot_Point
        this.getEntryData(kind).clear()
        this.getEntryData(D_PP_R1).clear()
        this.getEntryData(D_PP_R2).clear()
        this.getEntryData(D_PP_R3).clear()
        this.getEntryData(D_PP_S1).clear()
        this.getEntryData(D_PP_S2).clear()
        this.getEntryData(D_PP_S3).clear()
        var data = PivotPointIndicator(this.ts,timeLevel)
        var dataR1 = StandardReversalIndicator(data,PivotLevel.RESISTANCE_1)
        var dataR2 = StandardReversalIndicator(data,PivotLevel.RESISTANCE_1)
        var dataR3 = StandardReversalIndicator(data,PivotLevel.RESISTANCE_1)
        var dataS1 = StandardReversalIndicator(data,PivotLevel.SUPPORT_1)
        var dataS2 = StandardReversalIndicator(data,PivotLevel.SUPPORT_2)
        var dataS3 = StandardReversalIndicator(data,PivotLevel.SUPPORT_3)

        for (i in 0 until this.ts?.tickCount!!) {
            if(data.timeSeries.tickCount > i){
                if(i < data.timeSeries.tickCount) {
                    val value = data.getValue(i).toDouble().toFloat()
                    this.getEntryData(kind).add(Entry(i.toFloat(), value))
                }
                if(i < dataR1.timeSeries.tickCount)
                    this.getEntryData(D_PP_R1).add(Entry(i.toFloat(),dataR1.getValue(i).toDouble().toFloat()))

                if(i < dataR2.timeSeries.tickCount)
                    this.getEntryData(D_PP_R2).add(Entry(i.toFloat(),dataR2.getValue(i).toDouble().toFloat()))

                if(i < dataR3.timeSeries.tickCount)
                    this.getEntryData(D_PP_R3).add(Entry(i.toFloat(),dataR3.getValue(i).toDouble().toFloat()))

                if(i < dataS1.timeSeries.tickCount)
                    this.getEntryData(D_PP_S1).add(Entry(i.toFloat(),dataS1.getValue(i).toDouble().toFloat()))

                if(i < dataS2.timeSeries.tickCount)
                    this.getEntryData(D_PP_S2).add(Entry(i.toFloat(),dataS2.getValue(i).toDouble().toFloat()))

                if(i < dataS3.timeSeries.tickCount)
                    this.getEntryData(D_PP_S3).add(Entry(i.toFloat(),dataS3.getValue(i).toDouble().toFloat()))
            }
        }
    }



    private fun updateTripleEMA(timeFrame: Int){
        var kind = Triple_EMA
        this.getEntryData(kind).clear()
        var data = TripleEMAIndicator(this.closePrice,timeFrame)
        for (i in 0 until closePrice.timeSeries.tickCount) {
            if(data.timeSeries.tickCount > i){
                val value = data.getValue(i).toDouble().toFloat()
                this.getEntryData(kind).add(Entry(i.toFloat(),value))
            }
        }
    }

    private fun updateAccumDist(){
        var kind = Accumulation_Distribution
        this.getEntryData(kind).clear()
        var data = AccumulationDistributionIndicator(this.ts)
        for (i in 0 until this.ts?.tickCount!!) {
            if(data.timeSeries.tickCount > i){
                val value = data.getValue(i).toDouble().toFloat()
                this.getEntryData(kind).add(Entry(i.toFloat(),value))
            }
        }
    }

    //init last piviot
    //loop over all the values
    //Looking for % > threashold
    //When found

    private fun updateZigZag(thresholdPercent: Double) {
//        val zigZagPoints = ArrayList<Decimal>()
        this.getEntryData(ZigZag).clear()
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
       if(closePrice.timeSeries.tickCount in 1..51){
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
            if(j < conversionData.timeSeries.tickCount)
                this.getEntryData(Overlay.Kind.D_Ich_Cloud_Conversion).add(Entry(j.toFloat(), conversionData.getValue(j).toDouble().toFloat()))

            if(j < baseData.timeSeries.tickCount)
                this.getEntryData(Overlay.Kind.D_Ich_Cloud_Base).add(Entry(j.toFloat(), baseData.getValue(j).toDouble().toFloat()))

            if(j < leadingAData.timeSeries.tickCount)
                this.getEntryData(Overlay.Kind.D_Ich_Cloud_Lead_A).add(Entry(leadX, leadingAData.getValue(j).toDouble().toFloat()))

            if(j < leadingBData.timeSeries.tickCount)
                this.getEntryData(Overlay.Kind.D_Ich_Cloud_Lead_B).add(Entry(leadX, leadingBData.getValue(j).toDouble().toFloat()))

            if(j >= leadingPeriod && laggingData.timeSeries.tickCount > j)
                this.getEntryData(Overlay.Kind.D_Ich_Cloud_Lagging).add(Entry(j.toFloat()- leadingPeriod, laggingData.getValue(j).toDouble().toFloat()))
        }
    }

    private fun updateRSI(closePrice: ClosePriceIndicator, timeFrame: Int){
        this.getEntryData(RSI).clear()
        val rsi = RSIIndicator(closePrice, timeFrame)

        for(i in 0 until closePrice.timeSeries.tickCount){
            if(i< rsi.timeSeries.tickCount)
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
                if(i < dpo.timeSeries.tickCount)
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
            if(i < sod.timeSeries.tickCount)
                this.getEntryData(Overlay.Kind.D_Stoch_Oscill_SMA_Timeframe).add(Entry(i.toFloat(), sod.getValue(i).toDouble().toFloat()))
            if(i < sok.timeSeries.tickCount)
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
            if(i < ppo.timeSeries.tickCount)
                this.getEntryData(Overlay.Kind.D_PPO_ShortTerm).add(Entry(i.toFloat(), ppo.getValue(i).toDouble().toFloat()))
            if(i < ema.timeSeries.tickCount)
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
            if(j < bbuSMA.timeSeries.tickCount)
                this.getEntryData(D_BB_Upper).add(Entry(j.toFloat(), bbuSMA.getValue(j).toDouble().toFloat()))
            if(j < bbmSMA.timeSeries.tickCount)
                this.getEntryData(D_BB_Middle).add(Entry(j.toFloat(), bbmSMA.getValue(j).toDouble().toFloat()))
            if(j < bblSMA.timeSeries.tickCount)
                this.getEntryData(D_BB_Lower).add(Entry(j.toFloat(), bblSMA.getValue(j).toDouble().toFloat()))
        }
    }

    fun updateAroonOscilatorData(){
        this.getEntryData(AroonOsci).clear()
        var timeFrame: Int = 0
        for(overlay in OverlayAdapter.data.list) {
            if (overlay.kind == Overlay.Kind.AroonOsci) {
                timeFrame = OverlayAdapter.getTimeframe(AroonOsci).toInt()
            }
        }
        var aroonOscillatorIndicator = AroonOscillatorIndicator(this.ts, timeFrame)
        if(this.ts == null)
            return
        for (j in 0 until this.ts?.tickCount!!) {
            this.getEntryData(AroonOsci).add(Entry(j.toFloat(),aroonOscillatorIndicator.getValue(j).toDouble().toFloat()))
        }
    }

    fun isIchCloudSelected(): Boolean {
        return OverlayAdapter.data.all[Ichimoku_Cloud]?.selected!!
    }


    // Used to add or remove the lagging items to the separate charts to alight wtih Ich Cloud
    fun updateSeparateCharts(){
        var laggingPeriod: Int = 0
        for(overlay in OverlayAdapter.data.list) {
            if( overlay.kind == Overlay.Kind.Ichimoku_Cloud){
                laggingPeriod = OverlayAdapter.getLaggingPeriod(Ichimoku_Cloud).toInt()
            }
        }
        if(isIchCloudSelected()){
            for(overlay in OverlayAdapter.data.all){
                if(OverlayAdapter.data.all[overlay.value.kindData.parentKind]?.separateChart!!
                        && overlay.value.kindData.hasData
                ){
                    if(overlay.value.kind == Volume_Bars){
                        if(this.getEntryData(overlay.value.kind).isNotEmpty())
                            addXNumberOfEmptyBarEntries(this.getEntryData(overlay.value.kind).indices.last,laggingPeriod,this.getBarData(Volume_Bars))
                    }else{
                        if(this.getEntryData(overlay.value.kind).isNotEmpty())
                            addXNumberOfEmptyEntries(this.getEntryData(overlay.value.kind).indices.last,laggingPeriod,this.getEntryData(overlay.value.kind))
                    }
                }
            }
        }else{
            for(i in 0..laggingPeriod) {
                for(overlay in OverlayAdapter.data.all){
                    if(OverlayAdapter.data.all[overlay.value.kindData.parentKind]?.separateChart!!
                            && overlay.value.kindData.hasData){
                        if(this.getEntryData(overlay.value.kind).isNotEmpty())
                            this.getEntryData(overlay.value.kind).removeAt(this.getEntryData(overlay.value.kind).lastIndex)
                    }
                }
            }
        }
    }

    fun updateAroonUpDownData(){
        this.getEntryData(Overlay.Kind.D_AroonUp).clear()
        this.getEntryData(Overlay.Kind.D_AroonDown).clear()
        var timeFrame: Int = 0
        for(overlay in OverlayAdapter.data.list) {
            if (overlay.kind == Overlay.Kind.AroonUpDown) {
                timeFrame = OverlayAdapter.getTimeframe(AroonUpDown).toInt()
            }
        }

        var aroonUpIndicatorData = AroonUpIndicator(this.ts, timeFrame)
        var aroonDownIndicatorData = AroonDownIndicator(this.ts, timeFrame)
        for (j in 0 until closePrice.timeSeries.tickCount) {
            this.getEntryData(Overlay.Kind.D_AroonUp).add(Entry(j.toFloat(),aroonUpIndicatorData.getValue(j).toDouble().toFloat()))
            this.getEntryData(Overlay.Kind.D_AroonDown).add(Entry(j.toFloat(),aroonDownIndicatorData.getValue(j).toDouble().toFloat()))
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
        this.getEntryData(Exponential_MA).clear()
        val ema = EMAIndicator(closePrice, timeFrame)

        for (i in 0 until closePrice.timeSeries.tickCount) {
            getEntryData(Exponential_MA).add(Entry(i.toFloat(),ema.getValue(i).toDouble().toFloat()))
        }
    }

    //Simple Moving Average
    private fun updateSMA(closePrice: ClosePriceIndicator, timeFrame: Int){
        this.data[Simple_Moving_Avg]?.data?.clear()
        val sma = SMAIndicator(closePrice, timeFrame)

        for (i in 0 until closePrice.timeSeries.tickCount) {
            (this.data[Simple_Moving_Avg]?.data as ArrayList<Entry>).add(Entry(i.toFloat(),sma.getValue(i).toDouble().toFloat()))
        }
    }

    //keltnerChannel
    private fun updatekeltnerChannel(closePrice: ClosePriceIndicator, timeFrame: Int, ratio: Int){
        getEntryData(D_KC_Upper).clear()
        getEntryData(D_KC_Middle).clear()
        getEntryData(D_KC_Lower).clear()
        val km = KeltnerChannelMiddleIndicator(closePrice, timeFrame)
        val kl = KeltnerChannelLowerIndicator(km, Decimal.valueOf(ratio), timeFrame)
        val ku = KeltnerChannelUpperIndicator(km, Decimal.valueOf(ratio), timeFrame)
        for (i in 0 until closePrice.timeSeries.tickCount) {
            if(ku.timeSeries.tickCount > i && km.timeSeries.tickCount > i && km.timeSeries.tickCount > i) {
                val kuVal = ku.getValue(i).toDouble().toFloat()
                val kmVal = km.getValue(i).toDouble().toFloat()
                val klVal = kl.getValue(i).toDouble().toFloat()
                val percKUKM = kuVal / kmVal
                val percKLKM = kmVal / klVal
//            println("$i ku:${kuVal}\t%$percKUKM\tkm:${kmVal}\tkl:${klVal}\t%$percKLKM ")
                if (percKLKM.absoluteValue <= 1.05 && percKUKM.absoluteValue <= 1.05) {
                    this.getEntryData(D_KC_Upper).add(Entry(i.toFloat(), kuVal))
                    this.getEntryData(D_KC_Middle).add(Entry(i.toFloat(), kmVal))
                    this.getEntryData(D_KC_Lower).add(Entry(i.toFloat(), klVal))
//                println("Add ${i}")
                }
            }


        }

    }
    private fun updateParabolicSAR(aF: Double, maxA: Double){
        this.getEntryData(Parabolic_SAR).clear()
        val parSARData =  ParabolicSarIndicator(this.ts,Decimal.valueOf(aF),Decimal.valueOf(maxA))
        for (i in 0 until closePrice.timeSeries.tickCount) {
            this.getEntryData(Parabolic_SAR).add(Entry(i.toFloat(),parSARData.getValue(i).toDouble().toFloat()))
//            println("SAR data: ${parSARData.getValue(i).toDouble().toFloat()}")
        }
    }

    private fun updateChandelierExit(timeSeries: TimeSeries?,timeFrame: Int, ratio: Decimal) {
        this.getEntryData(Chandelier_Exit).clear()
        var chadExitData = ChandelierExitLongIndicator(timeSeries, timeFrame,ratio)
        for (i in 0 until closePrice.timeSeries.tickCount) {
            this.getEntryData(Chandelier_Exit).add(Entry(i.toFloat(),chadExitData.getValue(i).toDouble().toFloat()))
        }
    }
    //Detrend Price Ocelator
    //Commodity Channel Index
    //Average Directional Index
}