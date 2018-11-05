package com.backflippedstudios.crypto_ta

import android.content.Context
import android.support.v4.content.ContextCompat
import org.ta4j.core.indicators.pivotpoints.TimeLevel

data class Overlay(val context: Context, val kind: Kind){

    enum class IndicatorType{
        Line,
        Piviot_Line,
        Scatter,
        Bar
    }
    data class IndicatorInfo(
            var label: String = "",
            var selectedLegendLabel: String = "",
            var color: Int = 0,
            var colorDefault: Int = 0,
            var filled: Boolean = false,
            var filledColor: Int = 0,
            var type: IndicatorType = IndicatorType.Line
    )
    data class Values(var value: Double = -1.0, var max: Double = 0.0, var min: Double = 0.0)
    var title: String = kind.toString().replace("_"," ")
    var timeFrame: Int = 0
    var timeFrame2: Int = 0
    var timeFrame3: Int = 0
    var timeFrameFast: Int = 0
    var timeFrameSlow: Int = 0
    var timeFrameSMA: Int = 0
    var shortTerm: Int = 0
    var longTerm: Int = 0
    var PPO_EMA: Int = 0
    var ratio: Int = 0
    var k: Int = 0
    var accelerationFactor: Int = 0
    var maximumAcceleration: Int = 0
    var conversionPeriod: Int = 0
    var basePeriod: Int = 0
    var leadingPeriod: Int = 0
    var laggingPeriod: Int = 0
    var thresholdPercent: Int = 0
    var separateChart: Boolean = false
    var valuesAreInts: Boolean = true
    var timeLevel: TimeLevel = TimeLevel.TICKBASED
    // The values represents each of the editable text items that an overlay can edit. A positive
    // value will mean that it can be visible and editable
    var values: Array<Values> = Array(4,{ _ -> Values()})
    var valuesScaleFactor: Int = 1
    var selected: Boolean = false
    var allIndicatorInfo: Array<IndicatorInfo> = Array(8,{_ -> IndicatorInfo() })
    var kindData: KindData
    //This is attempt to have 1 location for all the configuration
    var sharedPref = context.getSharedPreferences("com.skydoves.colorpickerpreference", Context.MODE_PRIVATE)

    init {
        when(this.kind){
            Kind.CandleStick -> kindData = KindData(
                    false,
                    false,
                    Kind.CandleStick,
                    -1,
                    -1,
                    false,
                    true
            )
            Overlay.Kind.Bollinger_Bands ->{
                values[0].value = 20.0
                values[0].min = 2.0
                values[0].max = 50.0
                this.timeFrame = 0
                allIndicatorInfo[0].label = "Bollinger Bands - Middle"
                allIndicatorInfo[1].label = "Bollinger Bands - Upper"
                allIndicatorInfo[2].label = "Bollinger Bands - Lower"
                allIndicatorInfo[0].selectedLegendLabel = "BB - M"
                allIndicatorInfo[1].selectedLegendLabel = "BB - U"
                allIndicatorInfo[2].selectedLegendLabel = "BB - L"
                kindData = KindData(true,false,Kind.Bollinger_Bands, -1,-1, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.D_BB_Middle.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context, R.color.md_red_500)

                allIndicatorInfo[1].color = sharedPref.getInt(Overlay.Kind.D_BB_Upper.toString() + "_COLOR",0)
                allIndicatorInfo[1].colorDefault = ContextCompat.getColor(context, R.color.md_cyan_500)

                allIndicatorInfo[2].color = sharedPref.getInt(Overlay.Kind.D_BB_Lower.toString() + "_COLOR",0)
                allIndicatorInfo[2].colorDefault = ContextCompat.getColor(context, R.color.md_blue_500)
            }
            Kind.D_BB_Timeframe ->{
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.Bollinger_Bands,0)
            }
            Overlay.Kind.D_BB_Middle -> {
                allIndicatorInfo[0].label = "BB Middle Color"
                kindData = KindData(false,true,Kind.Bollinger_Bands,-1,0,false,true)
            }
            Overlay.Kind.D_BB_Upper -> {
                allIndicatorInfo[0].label = "BB Upper Color"
                kindData = KindData(false,true,Kind.Bollinger_Bands,-1,1,false,true)
            }
            Overlay.Kind.D_BB_Lower -> {
                allIndicatorInfo[0].label = "BB Lower Color"
                kindData = KindData(false,true,Kind.Bollinger_Bands,-1,2,false,true)
            }

            Overlay.Kind.Bollinger_Band_Width ->{
                allIndicatorInfo[0].label = "Bollinger Bands Width"
                allIndicatorInfo[0].selectedLegendLabel = "BB - Width"
                separateChart = true
                kindData = KindData(true,false,Kind.Bollinger_Band_Width, -1,-1, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.D_BBW_Color.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context, R.color.md_cyan_700)
            }
            Kind.D_BBW_Color ->{
                allIndicatorInfo[0].label = "BB Width Color"
                kindData = KindData(false,true,Kind.Bollinger_Band_Width,0)
            }

            Overlay.Kind.Bollinger_Band_Percent_B ->{
                values[0].value = 20.0
                values[0].min = 2.0
                values[0].max = 50.0
                values[1].value = 2.0
                values[1].min = 1.0
                values[1].max = 10.0
                separateChart = true
                this.timeFrame = 0
                this.k = 1
                allIndicatorInfo[0].label = "Bollinger Bands % B"
                allIndicatorInfo[0].selectedLegendLabel = "BB %B"
                kindData = KindData(true,false,Kind.Bollinger_Band_Percent_B, -1,-1, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.D_BBPB_Color.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context, R.color.md_amber_500)
            }
            Kind.D_BBPB_TimeFrame ->{
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.Bollinger_Band_Percent_B,0)
            }
            Kind.D_BBPB_K ->{
                allIndicatorInfo[0].label = "K"
                kindData = KindData(false,true,Kind.Bollinger_Band_Percent_B,1)
            }
            Overlay.Kind.D_BBPB_Color -> {
                allIndicatorInfo[0].label = "BB %B Color"
                kindData = KindData(false,true,Kind.Bollinger_Band_Percent_B,-1,0)
            }

            Overlay.Kind.Keltner_Channel ->{
                values[0].value = 12.0
                values[0].min = 2.0
                values[0].max = 50.0
                values[1].value = 2.0
                values[1].min = 1.0
                values[1].max = 10.0
                this.timeFrame = 0
                this.ratio = 1
                allIndicatorInfo[0].label = "Keltner Channel Middle"
                allIndicatorInfo[1].label = "Keltner Channel Upper"
                allIndicatorInfo[2].label = "Keltner Channel Lower"
                allIndicatorInfo[0].selectedLegendLabel = "KC - M"
                allIndicatorInfo[1].selectedLegendLabel = "KC - U"
                allIndicatorInfo[2].selectedLegendLabel = "KC - L"
                kindData = KindData(true,false,Kind.Keltner_Channel, -1,-1, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.D_KC_Middle.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_purple_500)
                allIndicatorInfo[1].color = sharedPref.getInt(Overlay.Kind.D_KC_Upper.toString() + "_COLOR",0)
                allIndicatorInfo[1].colorDefault = ContextCompat.getColor(context,R.color.md_yellow_700)
                allIndicatorInfo[2].color = sharedPref.getInt(Overlay.Kind.D_KC_Lower.toString() + "_COLOR",0)
                allIndicatorInfo[2].colorDefault = ContextCompat.getColor(context,R.color.md_yellow_700)
            }
            Overlay.Kind.D_KC_Timeframe -> {
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.Keltner_Channel,0)
            }
            Overlay.Kind.D_KC_Ratio -> {
                allIndicatorInfo[0].label = "Ratio"
                kindData = KindData(false,true,Kind.Keltner_Channel,1)
            }
            Overlay.Kind.D_KC_Middle -> {
                allIndicatorInfo[0].label = "KC Middle Color"
                kindData = KindData(false,true,Kind.Keltner_Channel,-1,0,false, true)
            }
            Overlay.Kind.D_KC_Upper -> {
                allIndicatorInfo[0].label = "KC Upper Color"
                kindData = KindData(false,true,Kind.Keltner_Channel,-1,1,false, true)
            }
            Overlay.Kind.D_KC_Lower -> {
                allIndicatorInfo[0].label = "KC Lower Color"
                kindData = KindData(false,true,Kind.Keltner_Channel,-1,2,false, true)
            }
            Overlay.Kind.Simple_Moving_Avg ->{
                values[0].value = 20.0
                values[0].min = 2.0
                values[0].max = 50.0
                this.timeFrame = 0
                allIndicatorInfo[0].label = "Simple Moving Average"
                allIndicatorInfo[0].selectedLegendLabel = "SMA"
                kindData = KindData(true,false,Kind.Simple_Moving_Avg, -1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.Simple_Moving_Avg.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_amber_700)
            }
            Overlay.Kind.D_SMA_Timeframe -> {
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.Simple_Moving_Avg,0)
            }
            Overlay.Kind.D_SMA_COLOR ->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.Simple_Moving_Avg,-1,0)
            }
            Overlay.Kind.Exponential_MA ->{
                values[0].value = 20.0
                values[0].min = 2.0
                values[0].max = 50.0
                this.timeFrame = 0
                allIndicatorInfo[0].label = "Exp Moving Average"
                allIndicatorInfo[0].selectedLegendLabel = "EMA"
                kindData = KindData(true,false,Kind.Exponential_MA,-1,0,true,true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.Exponential_MA.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_light_blue_300)
            }
            Overlay.Kind.D_EMA_Timeframe -> {
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.Exponential_MA,0)
            }
            Overlay.Kind.D_EMA_COLOR ->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.Exponential_MA,-1,0)
            }

            Overlay.Kind.Parabolic_SAR ->{
                this.valuesAreInts = false
                values[0].value = .025
                values[0].min = .005
                values[0].max = 0.5
                values[1].value = .050
                values[1].min = .005
                values[1].max = 0.5
                this.valuesScaleFactor = 1000
                this.accelerationFactor = 0
                this.maximumAcceleration = 1
                allIndicatorInfo[0].label = "Parabolic SAR"
                allIndicatorInfo[0].selectedLegendLabel = "P SAR"
                kindData = KindData(true,false,Kind.Parabolic_SAR, -1,0, true, true)
                allIndicatorInfo[0].type = IndicatorType.Scatter
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.D_P_SAR_COLOR.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_white_1000)
            }
            Overlay.Kind.D_P_SAR_ACC_FAC -> {
                this.valuesAreInts = false
                allIndicatorInfo[0].label = "Acceleration Factor"
                kindData = KindData(false,true,Kind.Parabolic_SAR,0)
            }
            Overlay.Kind.D_P_SAR_MAX_ACC -> {
                this.valuesAreInts = false
                allIndicatorInfo[0].label = "Max Accelerator"
                kindData = KindData(false,true,Kind.Parabolic_SAR,1)
            }
            Overlay.Kind.D_P_SAR_COLOR ->{
                allIndicatorInfo[0].label = "Dot Color"
                kindData = KindData(false,true,Kind.Parabolic_SAR,-1,0)
            }
            Overlay.Kind.Chandelier_Exit_Long ->{
                values[0].value = 22.0
                values[0].min = 2.0
                values[0].max = 50.0
                values[1].value = 3.0
                values[1].min = 2.0
                values[1].max = 5.0
                this.timeFrame = 0
                this.ratio = 1
                allIndicatorInfo[0].label = "Chandelier Exit Long"
                allIndicatorInfo[0].selectedLegendLabel = "CEL"
                kindData = KindData(true,false,Kind.Chandelier_Exit_Long, -1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.Chandelier_Exit_Long.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_purple_500)
            }
            Overlay.Kind.D_CEL_Timeframe -> {
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.Chandelier_Exit_Long,0)
            }
            Overlay.Kind.D_CEL_Ratio -> {
                allIndicatorInfo[0].label = "Ratio"
                kindData = KindData(false,true,Kind.Chandelier_Exit_Long,1)
            }
            Kind.D_CEL_Color ->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.Chandelier_Exit_Long,-1,0)
            }

            Overlay.Kind.Chandelier_Exit_Short ->{
                values[0].value = 22.0
                values[0].min = 2.0
                values[0].max = 50.0
                values[1].value = 3.0
                values[1].min = 2.0
                values[1].max = 5.0
                this.timeFrame = 0
                this.ratio = 1
                allIndicatorInfo[0].label = "Chandelier Exit Short"
                allIndicatorInfo[0].selectedLegendLabel = "CES"
                kindData = KindData(true,false,Kind.Chandelier_Exit_Short, -1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.Chandelier_Exit_Short.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_purple_200)
            }
            Overlay.Kind.D_CES_Timeframe -> {
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.Chandelier_Exit_Short,0)
            }
            Overlay.Kind.D_CES_Ratio -> {
                allIndicatorInfo[0].label = "Ratio"
                kindData = KindData(false,true,Kind.Chandelier_Exit_Short,1)
            }
            Kind.D_CES_Color ->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.Chandelier_Exit_Short,-1,0)
            }

            Overlay.Kind.Double_EMA ->{
                values[0].value = 9.0
                values[0].min = 2.0
                values[0].max = 30.0
                this.timeFrame = 0
                this.ratio = 1
                allIndicatorInfo[0].label = this.kind.toString().replace("_", " ")
                allIndicatorInfo[0].selectedLegendLabel = "DEMA"
                kindData = KindData(true,false,this.kind, -1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(this.kind.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_green_700)
            }
            Overlay.Kind.D_DEMA_Timeframe-> {
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.Double_EMA,0)
            }
            Kind.D_DEMA_Color ->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.Double_EMA,-1,0)
            }

            Overlay.Kind.Ichimoku_Cloud ->{
                values[0].value = 9.0 //ConversionPeriod
                values[0].min = 2.0
                values[0].max = 50.0
                values[1].value = 26.0 //BasePeriod
                values[1].min = 2.0
                values[1].max = 50.0
                values[2].value = 52.0//LeadingPeriod
                values[2].min = 2.0
                values[2].max = 70.0
                values[3].value = 26.0 //LaggingPeriod
                values[3].min = 2.0
                values[3].max = 50.0
                this.conversionPeriod = 0
                this.basePeriod = 1
                this.leadingPeriod = 2
                this.laggingPeriod = 3
                allIndicatorInfo[0].label = "Ich Cloud Leading A"
                allIndicatorInfo[1].label = "Ich Cloud Leading B"
                allIndicatorInfo[2].label = "Ich Cloud Conversion"
                allIndicatorInfo[3].label = "Ich Cloud Base"
                allIndicatorInfo[4].label = "Ich Cloud Lagging"
                allIndicatorInfo[0].selectedLegendLabel = "Ich Cloud LeadA"
                allIndicatorInfo[1].selectedLegendLabel = "Ich Cloud LeadB"
                allIndicatorInfo[2].selectedLegendLabel = "Ich Cloud Conv"
                allIndicatorInfo[3].selectedLegendLabel = "Ich Cloud Base"
                allIndicatorInfo[4].selectedLegendLabel = "Ich Cloud Lagg"
                kindData = KindData(true,false,Kind.Ichimoku_Cloud, -1,-1, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.D_Ich_Cloud_Lead_A.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_light_green_300)
                allIndicatorInfo[0].filled = true
                allIndicatorInfo[0].filledColor = ContextCompat.getColor(context,R.color.md_red_200)

                allIndicatorInfo[1].color = sharedPref.getInt(Overlay.Kind.D_Ich_Cloud_Lead_B.toString() + "_COLOR",0)
                allIndicatorInfo[1].colorDefault = ContextCompat.getColor(context,R.color.md_red_300)
                allIndicatorInfo[1].filled = true
                allIndicatorInfo[1].filledColor = ContextCompat.getColor(context,R.color.md_green_200)

                allIndicatorInfo[2].color = sharedPref.getInt(Overlay.Kind.D_Ich_Cloud_Conversion.toString() + "_COLOR",0)
                allIndicatorInfo[2].colorDefault = ContextCompat.getColor(context,R.color.md_blue_500)

                allIndicatorInfo[3].color = sharedPref.getInt(Overlay.Kind.D_Ich_Cloud_Base.toString() + "_COLOR",0)
                allIndicatorInfo[3].colorDefault = ContextCompat.getColor(context,R.color.md_purple_400)

                allIndicatorInfo[4].color = sharedPref.getInt(Overlay.Kind.D_Ich_Cloud_Lagging.toString() + "_COLOR",0)
                allIndicatorInfo[4].colorDefault = ContextCompat.getColor(context,R.color.md_white_1000)
            }
            Overlay.Kind.D_Ich_Cloud_Lead_A -> {
                allIndicatorInfo[0].label = "Leading A"
                kindData = KindData(false,true,Kind.Ichimoku_Cloud,-1,0, true, true)
            }
            Overlay.Kind.D_Ich_Cloud_Lead_B -> {
                allIndicatorInfo[0].label = "Leading B"
                kindData = KindData(false,true,Kind.Ichimoku_Cloud,-1,1, true, true)
            }
            Overlay.Kind.D_Ich_Cloud_Conversion -> {
                allIndicatorInfo[0].label = "Conversion"
                kindData = KindData(false,true,Kind.Ichimoku_Cloud,0,2, true, true)
            }
            Overlay.Kind.D_Ich_Cloud_Base -> {
                allIndicatorInfo[0].label = "Base"
                kindData = KindData(false,true,Kind.Ichimoku_Cloud,1,3, true, true)
            }
            Overlay.Kind.D_Ich_Cloud_Lagging -> {
                allIndicatorInfo[0].label = "Lagging"
                kindData = KindData(false,true,Kind.Ichimoku_Cloud,3,4, true, true)
            }
            Overlay.Kind.D_Ich_Cloud_Lead -> {
                allIndicatorInfo[0].label = "Leading"
                kindData = KindData(false,true,Kind.Ichimoku_Cloud,2,-1)
            }



            Overlay.Kind.ZigZag ->{
                values[0].value = 7.0
                values[0].min = 0.5
                values[0].max = 20.0
                this.thresholdPercent = 0
                allIndicatorInfo[0].label = "Zig Zag"
                allIndicatorInfo[0].selectedLegendLabel= "Zig Zag"
                kindData = KindData(true,false,Kind.ZigZag,-1,0, true, true)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context, R.color.md_cyan_500)
            }
            Overlay.Kind.D_ZigZag_Timeframe ->{
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.ZigZag,0)
            }
            Overlay.Kind.D_ZigZag_COLOR ->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.ZigZag,-1,0)
            }
            Overlay.Kind.Hull_Moving_Average ->{
                values[0].value = 7.0
                values[0].min = 0.5
                values[0].max = 20.0
                this.timeFrame = 0
                allIndicatorInfo[0].label = "Hull Moving Average"
                allIndicatorInfo[0].selectedLegendLabel= "HMA"
                kindData = KindData(true,false,Kind.Hull_Moving_Average,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.Hull_Moving_Average.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context, R.color.md_purple_300)
            }
            Overlay.Kind.D_HMA_TimeFrame ->{
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.Hull_Moving_Average,0)
            }

            Overlay.Kind.D_HMA_COLOR ->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.Hull_Moving_Average,-1,0)
            }

            Overlay.Kind.Zero_Lag_Moving_Average ->{
            values[0].value = 7.0
            values[0].min = 0.5
            values[0].max = 20.0
            this.timeFrame = 0
            allIndicatorInfo[0].label = "Zero Lag Expo. Moving Average"
            allIndicatorInfo[0].selectedLegendLabel= "ZLEMA"
            kindData = KindData(true,false,Kind.Zero_Lag_Moving_Average,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.Zero_Lag_Moving_Average.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context, R.color.md_cyan_500)
        }
            Overlay.Kind.D_ZLEMA_TimeFrame ->{
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.Zero_Lag_Moving_Average,0)
            }

            Overlay.Kind.D_ZLEMA_COLOR ->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.Zero_Lag_Moving_Average,-1,0)
            }

            Overlay.Kind.Volume_Weighted_Average_Price ->{
                values[0].value = 7.0
                values[0].min = 0.5
                values[0].max = 20.0
                this.timeFrame = 0
                allIndicatorInfo[0].label = "Volume Weighted Average Price"
                allIndicatorInfo[0].selectedLegendLabel= "VWAP"
                kindData = KindData(true,false,Kind.Volume_Weighted_Average_Price,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.Volume_Weighted_Average_Price.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_blue_600)
            }
            Overlay.Kind.D_VWAP_TimeFrame ->{
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.Volume_Weighted_Average_Price,0)
            }

            Overlay.Kind.D_VWAP_COLOR ->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.Volume_Weighted_Average_Price,-1,0)
            }

            Overlay.Kind.Moving_Volume_Weighted_Average_Price ->{
                values[0].value = 7.0
                values[0].min = 0.5
                values[0].max = 20.0
                this.timeFrame = 0
                allIndicatorInfo[0].label = "Moving Vol Weighted Avg Price"
                allIndicatorInfo[0].selectedLegendLabel= "MVWAP"
                kindData = KindData(true,false,Kind.Moving_Volume_Weighted_Average_Price,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.Moving_Volume_Weighted_Average_Price.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_red_300)
            }
            Overlay.Kind.D_MVWAP_TimeFrame ->{
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.Moving_Volume_Weighted_Average_Price,0)
            }

            Overlay.Kind.D_MVWAP_COLOR ->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.Moving_Volume_Weighted_Average_Price,-1,0)
            }

            Overlay.Kind.Awesome_Oscillator ->{
                values[0].value = 5.0
                values[0].min = 0.5
                values[0].max = 40.0
                values[1].value = 34.0
                values[1].min = 0.5
                values[1].max = 40.0
                this.timeFrame = 0
                this.timeFrame2 = 1
                this.separateChart = true
                allIndicatorInfo[0].label = "Awesome Oscillator"
                allIndicatorInfo[0].selectedLegendLabel= "Awesome Oscillator"
                kindData = KindData(true,false,Kind.Awesome_Oscillator,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.Awesome_Oscillator.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_red_300)
            }
            Overlay.Kind.D_AO_Timeframe1 ->{
                allIndicatorInfo[0].label = "Timeframe1"
                kindData = KindData(false,true,Kind.Awesome_Oscillator,0)
            }
            Overlay.Kind.D_AO_Timeframe2 ->{
                allIndicatorInfo[0].label = "Timeframe2"
                kindData = KindData(false,true,Kind.Awesome_Oscillator,1)
            }

            Overlay.Kind.D_AO_COLOR ->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.Awesome_Oscillator,-1,0)
            }


            Overlay.Kind.Rate_Of_Change ->{
                values[0].value = 5.0
                values[0].min = 0.5
                values[0].max = 40.0

                this.timeFrame = 0
                this.separateChart = true
                allIndicatorInfo[0].label = "Rate of Change"
                allIndicatorInfo[0].selectedLegendLabel= "Rate of Change"
                kindData = KindData(true,false,Kind.Rate_Of_Change,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.Rate_Of_Change.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_blue_700)
            }
            Overlay.Kind.D_ROC_Timeframe ->{
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.Rate_Of_Change,0)
            }
            Overlay.Kind.D_ROC_COLOR ->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.Rate_Of_Change,-1,0)
            }

            Overlay.Kind.Chande_Momentum_Oscillator ->{
                values[0].value = 9.0
                values[0].min = 0.5
                values[0].max = 35.0

                this.timeFrame = 0
                this.separateChart = true
                allIndicatorInfo[0].label = "Chande Momentum Oscillator"
                allIndicatorInfo[0].selectedLegendLabel= "Chande Momentum Oscillator"
                kindData = KindData(true,false,Kind.Chande_Momentum_Oscillator,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.Chande_Momentum_Oscillator.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_blue_300)
            }
            Overlay.Kind.D_CMO_Timeframe ->{
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.Chande_Momentum_Oscillator,0)
            }
            Overlay.Kind.D_CMO_COLOR ->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.Chande_Momentum_Oscillator,-1,0)
            }

            Overlay.Kind.Coppock_Curve ->{
                values[0].value = 14.0
                values[0].min = 0.5
                values[0].max = 35.0
                values[1].value = 11.0
                values[1].min = 0.5
                values[1].max = 35.0
                values[2].value = 10.0
                values[2].min = 0.5
                values[2].max = 35.0
                this.timeFrame = 0
                this.timeFrame2 = 0
                this.timeFrame3 = 0
                this.separateChart = true
                allIndicatorInfo[0].label = "Coppock Curve"
                allIndicatorInfo[0].selectedLegendLabel= "Coppock Curve"
                kindData = KindData(true,false,Kind.Coppock_Curve,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.Coppock_Curve.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_pink_600)
            }
            Overlay.Kind.D_CC_Long_Timeframe ->{
                allIndicatorInfo[0].label = "Long Timeframe"
                kindData = KindData(false,true,Kind.Coppock_Curve,0)
            }
            Overlay.Kind.D_CC_Short_Timeframe ->{
                allIndicatorInfo[0].label = "Short Timeframe"
                kindData = KindData(false,true,Kind.Coppock_Curve,1)
            }
            Overlay.Kind.D_CC_WMA_Timeframe ->{
                allIndicatorInfo[0].label = "WMA Timeframe"
                kindData = KindData(false,true,Kind.Coppock_Curve,2)
            }
            Overlay.Kind.D_CC_COLOR ->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.Coppock_Curve,-1,0)
            }

            Overlay.Kind.Williams__R ->{
                values[0].value = 14.0
                values[0].min = 0.5
                values[0].max = 35.0
                this.timeFrame = 0
                this.separateChart = true
                allIndicatorInfo[0].label = "Williams % R"
                allIndicatorInfo[0].selectedLegendLabel= "Williams % R"
                kindData = KindData(true,false,Kind.Williams__R,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.Williams__R.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_light_blue_700)
            }
            Overlay.Kind.D_WR_Timeframe->{
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.Williams__R,0)
            }
            Overlay.Kind.D_WR_COLOR ->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.Williams__R,-1,0)
            }

            Overlay.Kind.Ulcer_Index ->{
                values[0].value = 9.0
                values[0].min = 0.5
                values[0].max = 35.0
                this.timeFrame = 0
                this.separateChart = true
                allIndicatorInfo[0].label = "Ulcer Index"
                allIndicatorInfo[0].selectedLegendLabel= "Ulcer Index"
                kindData = KindData(true,false,Kind.Ulcer_Index,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.Ulcer_Index.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_purple_300)
            }
            Overlay.Kind.D_UI_Timeframe->{
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.Ulcer_Index,0)
            }
            Overlay.Kind.D_UI_COLOR ->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.Ulcer_Index,-1,0)
            }

            Overlay.Kind.Chaikin_Money_Flow ->{
                values[0].value = 20.0
                values[0].min = 0.5
                values[0].max = 35.0
                this.timeFrame = 0
                this.separateChart = true
                allIndicatorInfo[0].label = "Chaikin Money Flow"
                allIndicatorInfo[0].selectedLegendLabel= "Chaikin Money Flow"
                kindData = KindData(true,false,Kind.Chaikin_Money_Flow,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.Chaikin_Money_Flow.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_green_300)
            }
            Overlay.Kind.D_CMF_Timeframe->{
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.Chaikin_Money_Flow,0)
            }
            Overlay.Kind.D_CMF_COLOR ->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.Chaikin_Money_Flow,-1,0)
            }

            Overlay.Kind.Positive_Volume ->{
                this.separateChart = true
                allIndicatorInfo[0].label = this.kind.toString().replace("_", " ")
                allIndicatorInfo[0].selectedLegendLabel= this.kind.toString().replace("_", " ")
                kindData = KindData(true,false,Kind.Positive_Volume,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.Positive_Volume.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_cyan_700)
            }
            Overlay.Kind.D_PV_COLOR ->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.Positive_Volume,-1,0)
            }

            Overlay.Kind.Negative_Volume ->{
                this.separateChart = true
                allIndicatorInfo[0].label = this.kind.toString().replace("_", " ")
                allIndicatorInfo[0].selectedLegendLabel= this.kind.toString().replace("_", " ")
                kindData = KindData(true,false,this.kind,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(this.kind.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_red_700)
            }
            Overlay.Kind.D_NV_COLOR ->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.Negative_Volume,-1,0)
            }

            Overlay.Kind.On_Balance_Volume ->{
                this.separateChart = true
                allIndicatorInfo[0].label = this.kind.toString().replace("_", " ")
                allIndicatorInfo[0].selectedLegendLabel= this.kind.toString().replace("_", " ")
                kindData = KindData(true,false,this.kind,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(this.kind.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_amber_500)
            }
            Overlay.Kind.D_OBV_COLOR ->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.On_Balance_Volume,-1,0)
            }

            Overlay.Kind.Pivot_Point ->{
                allIndicatorInfo[0].label = this.kind.toString().replace("_", " ")
                allIndicatorInfo[0].selectedLegendLabel= this.kind.toString().replace("_", " ")
                kindData = KindData(true,false,this.kind,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(this.kind.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_yellow_500)
                allIndicatorInfo[0].type = IndicatorType.Piviot_Line

                allIndicatorInfo[1].label = "R1"
                allIndicatorInfo[1].selectedLegendLabel = "R1"
                allIndicatorInfo[1].color = sharedPref.getInt(Overlay.Kind.D_PP_R1.toString() + "_COLOR",0)
                allIndicatorInfo[1].colorDefault = ContextCompat.getColor(context,R.color.md_light_green_500)
                allIndicatorInfo[1].type = IndicatorType.Piviot_Line

                allIndicatorInfo[2].label = "R2"
                allIndicatorInfo[2].selectedLegendLabel = "R2"
                allIndicatorInfo[2].color = sharedPref.getInt(Overlay.Kind.D_PP_R2.toString() + "_COLOR",0)
                allIndicatorInfo[2].colorDefault = ContextCompat.getColor(context,R.color.md_light_green_500)
                allIndicatorInfo[2].type = IndicatorType.Piviot_Line

                allIndicatorInfo[3].label = "R3"
                allIndicatorInfo[3].selectedLegendLabel = "R3"
                allIndicatorInfo[3].color = sharedPref.getInt(Overlay.Kind.D_PP_R3.toString() + "_COLOR",0)
                allIndicatorInfo[3].colorDefault = ContextCompat.getColor(context,R.color.md_light_green_500)
                allIndicatorInfo[3].type = IndicatorType.Piviot_Line

                allIndicatorInfo[4].label = "S1"
                allIndicatorInfo[4].selectedLegendLabel = "S1"
                allIndicatorInfo[4].color = sharedPref.getInt(Overlay.Kind.D_PP_S1.toString() + "_COLOR",0)
                allIndicatorInfo[4].colorDefault = ContextCompat.getColor(context,R.color.md_red_600)
                allIndicatorInfo[4].type = IndicatorType.Piviot_Line

                allIndicatorInfo[5].label = "S2"
                allIndicatorInfo[5].selectedLegendLabel = "S2"
                allIndicatorInfo[5].color = sharedPref.getInt(Overlay.Kind.D_PP_S2.toString() + "_COLOR",0)
                allIndicatorInfo[5].colorDefault = ContextCompat.getColor(context,R.color.md_red_600)
                allIndicatorInfo[5].type = IndicatorType.Piviot_Line

                allIndicatorInfo[6].label = "S3"
                allIndicatorInfo[6].selectedLegendLabel = "S3"
                allIndicatorInfo[6].color = sharedPref.getInt(Overlay.Kind.D_PP_S3.toString() + "_COLOR",0)
                allIndicatorInfo[6].colorDefault = ContextCompat.getColor(context,R.color.md_red_600)
                allIndicatorInfo[6].type = IndicatorType.Piviot_Line
            }
            Overlay.Kind.D_PP_R1 ->{
                allIndicatorInfo[0].label = "R1"
                kindData = KindData(false,true,Kind.Pivot_Point,-1,1,false,true)
            }
            Overlay.Kind.D_PP_R2 ->{
                allIndicatorInfo[0].label = "R2"
                kindData = KindData(false,true,Kind.Pivot_Point,-1,2,false,true)
            }
            Overlay.Kind.D_PP_R3 ->{
                allIndicatorInfo[0].label = "R3"
                kindData = KindData(false,true,Kind.Pivot_Point,-1,3,false,true)
            }
            Overlay.Kind.D_PP_S1 ->{
                allIndicatorInfo[0].label = "S1"
                kindData = KindData(false,true,Kind.Pivot_Point,-1,4,false,true)
            }
            Overlay.Kind.D_PP_S2 ->{
                allIndicatorInfo[0].label = "S2"
                kindData = KindData(false,true,Kind.Pivot_Point,-1,5,false,true)
            }
            Overlay.Kind.D_PP_S3 ->{
                allIndicatorInfo[0].label = "S3"
                kindData = KindData(false,true,Kind.Pivot_Point,-1,6,false,true)
            }

            Overlay.Kind.DeMark_Pivot_Point ->{
                allIndicatorInfo[0].label = this.kind.toString().replace("_", " ")
                allIndicatorInfo[0].selectedLegendLabel= this.kind.toString().replace("_", " ")
                kindData = KindData(true,false,this.kind,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(this.kind.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_yellow_600)
                allIndicatorInfo[0].type = IndicatorType.Piviot_Line

                allIndicatorInfo[1].label = "Resistance"
                allIndicatorInfo[1].selectedLegendLabel = "Resistance"
                allIndicatorInfo[1].color = sharedPref.getInt(Overlay.Kind.D_DMPP_Resistance.toString() + "_COLOR",0)
                allIndicatorInfo[1].colorDefault = ContextCompat.getColor(context,R.color.md_red_700)
                allIndicatorInfo[1].type = IndicatorType.Piviot_Line

                allIndicatorInfo[2].label = "Support"
                allIndicatorInfo[2].selectedLegendLabel = "Support"
                allIndicatorInfo[2].color = sharedPref.getInt(Overlay.Kind.D_DMPP_Support.toString() + "_COLOR",0)
                allIndicatorInfo[2].colorDefault = ContextCompat.getColor(context,R.color.md_red_700)
                allIndicatorInfo[2].type = IndicatorType.Piviot_Line

            }
            Overlay.Kind.D_DMPP_Resistance ->{
                allIndicatorInfo[0].label = "Resistance"
                kindData = KindData(false,true,Kind.DeMark_Pivot_Point,-1,1,false,true)
            }
            Overlay.Kind.D_DMPP_Support ->{
                allIndicatorInfo[0].label = "Support"
                kindData = KindData(false,true,Kind.DeMark_Pivot_Point,-1,2,false,true)
            }

            Overlay.Kind.Fibonacci_Reversal_Resistance ->{
                allIndicatorInfo[0].label = this.kind.toString().replace("_", " ")
                allIndicatorInfo[0].selectedLegendLabel= this.kind.toString().replace("_", " ")
                kindData = KindData(true,false,this.kind,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(this.kind.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_blue_500)
                allIndicatorInfo[0].type = IndicatorType.Piviot_Line

                allIndicatorInfo[1].label = "Factor 1"
                allIndicatorInfo[1].selectedLegendLabel = "Factor 1"
                allIndicatorInfo[1].color = sharedPref.getInt(Overlay.Kind.D_FRR_1.toString() + "_COLOR",0)
                allIndicatorInfo[1].colorDefault = ContextCompat.getColor(context,R.color.md_blue_500)
                allIndicatorInfo[1].type = IndicatorType.Piviot_Line

                allIndicatorInfo[2].label = "Factor 2"
                allIndicatorInfo[2].selectedLegendLabel = "Factor 2"
                allIndicatorInfo[2].color = sharedPref.getInt(Overlay.Kind.D_FRR_2.toString() + "_COLOR",0)
                allIndicatorInfo[2].colorDefault = ContextCompat.getColor(context,R.color.md_blue_500)
                allIndicatorInfo[2].type = IndicatorType.Piviot_Line

                allIndicatorInfo[3].label = "Factor 3"
                allIndicatorInfo[3].selectedLegendLabel = "Factor 3"
                allIndicatorInfo[3].color = sharedPref.getInt(Overlay.Kind.D_FRR_3.toString() + "_COLOR",0)
                allIndicatorInfo[3].colorDefault = ContextCompat.getColor(context,R.color.md_blue_500)
                allIndicatorInfo[3].type = IndicatorType.Piviot_Line

                allIndicatorInfo[4].label = "Factor 4"
                allIndicatorInfo[4].selectedLegendLabel = "Factor 4"
                allIndicatorInfo[4].color = sharedPref.getInt(Overlay.Kind.D_FRR_4.toString() + "_COLOR",0)
                allIndicatorInfo[4].colorDefault = ContextCompat.getColor(context,R.color.md_blue_500)
                allIndicatorInfo[4].type = IndicatorType.Piviot_Line

            }
            Overlay.Kind.D_FRR_1 ->{
                allIndicatorInfo[0].label = "Factor 1"
                kindData = KindData(false,true,Kind.Fibonacci_Reversal_Resistance,-1,1,false,true)
            }
            Overlay.Kind.D_FRR_2 ->{
                allIndicatorInfo[0].label = "Factor 2"
                kindData = KindData(false,true,Kind.Fibonacci_Reversal_Resistance,-1,2,false,true)
            }
            Overlay.Kind.D_FRR_3 ->{
                allIndicatorInfo[0].label = "Factor 3"
                kindData = KindData(false,true,Kind.Fibonacci_Reversal_Resistance,-1,3,false,true)
            }
            Overlay.Kind.D_FRR_4 ->{
                allIndicatorInfo[0].label = "Factor 4"
                kindData = KindData(false,true,Kind.Fibonacci_Reversal_Resistance,-1,4,false,true)
            }

            Overlay.Kind.Fibonacci_Reversal_Support ->{
                allIndicatorInfo[0].label = this.kind.toString().replace("_", " ")
                allIndicatorInfo[0].selectedLegendLabel= this.kind.toString().replace("_", " ")
                kindData = KindData(true,false,this.kind,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(this.kind.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_blue_500)
                allIndicatorInfo[0].type = IndicatorType.Piviot_Line

                allIndicatorInfo[1].label = "Factor 1"
                allIndicatorInfo[1].selectedLegendLabel = "Factor 1"
                allIndicatorInfo[1].color = sharedPref.getInt(Overlay.Kind.D_FRS_1.toString() + "_COLOR",0)
                allIndicatorInfo[1].colorDefault = ContextCompat.getColor(context,R.color.md_blue_500)
                allIndicatorInfo[1].type = IndicatorType.Piviot_Line

                allIndicatorInfo[2].label = "Factor 2"
                allIndicatorInfo[2].selectedLegendLabel = "Factor 2"
                allIndicatorInfo[2].color = sharedPref.getInt(Overlay.Kind.D_FRS_2.toString() + "_COLOR",0)
                allIndicatorInfo[2].colorDefault = ContextCompat.getColor(context,R.color.md_blue_500)
                allIndicatorInfo[2].type = IndicatorType.Piviot_Line

                allIndicatorInfo[3].label = "Factor 3"
                allIndicatorInfo[3].selectedLegendLabel = "Factor 3"
                allIndicatorInfo[3].color = sharedPref.getInt(Overlay.Kind.D_FRS_3.toString() + "_COLOR",0)
                allIndicatorInfo[3].colorDefault = ContextCompat.getColor(context,R.color.md_blue_500)
                allIndicatorInfo[3].type = IndicatorType.Piviot_Line

                allIndicatorInfo[4].label = "Factor 4"
                allIndicatorInfo[4].selectedLegendLabel = "Factor 4"
                allIndicatorInfo[4].color = sharedPref.getInt(Overlay.Kind.D_FRS_4.toString() + "_COLOR",0)
                allIndicatorInfo[4].colorDefault = ContextCompat.getColor(context,R.color.md_blue_500)
                allIndicatorInfo[4].type = IndicatorType.Piviot_Line

            }
            Overlay.Kind.D_FRS_1 ->{
                allIndicatorInfo[0].label = "Factor 1"
                kindData = KindData(false,true,Kind.Fibonacci_Reversal_Support,-1,1,false,true)
            }
            Overlay.Kind.D_FRS_2 ->{
                allIndicatorInfo[0].label = "Factor 2"
                kindData = KindData(false,true,Kind.Fibonacci_Reversal_Support,-1,2,false,true)
            }
            Overlay.Kind.D_FRS_3 ->{
                allIndicatorInfo[0].label = "Factor 3"
                kindData = KindData(false,true,Kind.Fibonacci_Reversal_Support,-1,3,false,true)
            }
            Overlay.Kind.D_FRS_4 ->{
                allIndicatorInfo[0].label = "Factor 4"
                kindData = KindData(false,true,Kind.Fibonacci_Reversal_Support,-1,4,false,true)
            }

            Overlay.Kind.Trailing_Stop_Loss ->{
                values[0].value = 3.0
                values[0].min = 0.5
                values[0].max = 35.0
                values[1].value = 21.0
                values[1].min = 0.5
                values[1].max = 45.0
                this.timeFrame = 0
                this.timeFrame2 = 1
                allIndicatorInfo[0].label = "Trailing Stop Loss"
                allIndicatorInfo[0].selectedLegendLabel= "TSL"
                kindData = KindData(true,false,Kind.Trailing_Stop_Loss,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.Trailing_Stop_Loss.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_green_700)
            }
            Overlay.Kind.D_TSL_Distance->{
                allIndicatorInfo[0].label = "Distance"
                kindData = KindData(false,true,Kind.Trailing_Stop_Loss,0)
            }
            Overlay.Kind.D_TSL_Initial_Limit->{
                allIndicatorInfo[0].label = "Initial Limit"
                kindData = KindData(false,true,Kind.Trailing_Stop_Loss,1)
            }
            Overlay.Kind.D_TSL_Color ->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.Trailing_Stop_Loss,-1,0)
            }

            Overlay.Kind.MACD ->{
                values[0].value = 12.0
                values[0].min = 0.5
                values[0].max = 35.0
                values[1].value = 26.0
                values[1].min = 0.5
                values[1].max = 45.0
                this.timeFrame = 0
                this.timeFrame2 = 1
                this.separateChart = true
                allIndicatorInfo[0].label = "MACD"
                allIndicatorInfo[0].selectedLegendLabel= "MACD"
                allIndicatorInfo[1].label = "EMA(9)"
                allIndicatorInfo[1].selectedLegendLabel= "EMA(9)"
                allIndicatorInfo[2].label = "Historgram"
                allIndicatorInfo[2].selectedLegendLabel= "Historgram"
                kindData = KindData(true,false,Kind.MACD,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.MACD.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_orange_500)
                allIndicatorInfo[1].color = sharedPref.getInt(Overlay.Kind.D_MACD_Timeframe_Long.toString() + "_COLOR",0)
                allIndicatorInfo[1].colorDefault = ContextCompat.getColor(context,R.color.md_blue_600)
                allIndicatorInfo[2].color = sharedPref.getInt(Overlay.Kind.D_MACD_Timeframe_Short.toString() + "_COLOR",0)
                allIndicatorInfo[2].colorDefault = ContextCompat.getColor(context,R.color.md_red_600)
                allIndicatorInfo[2].type = IndicatorType.Bar
            }
            Overlay.Kind.D_MACD_Timeframe_Long->{
                allIndicatorInfo[0].label = "Timeframe Long"
                kindData = KindData(false,true,Kind.MACD,1,1,false,true) //Keep ema data
            }
            Overlay.Kind.D_MACD_Timeframe_Short->{
                allIndicatorInfo[0].label = "Timeframe Short"
                kindData = KindData(false,true,Kind.MACD,0,2,false,true) // Keep histrogram data
            }
            Overlay.Kind.D_MACD_Color ->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.MACD,-1,0)
            }

            Overlay.Kind.RAVI ->{
                values[0].value = 7.0
                values[0].min = 0.5
                values[0].max = 35.0
                values[1].value = 65.0
                values[1].min = 30.0
                values[1].max = 100.0
                this.timeFrame = 0
                this.timeFrame2 = 1
                this.separateChart = true
                allIndicatorInfo[0].label = "Range Action Verif Index"
                allIndicatorInfo[0].selectedLegendLabel= "RAVI"
                kindData = KindData(true,false,Kind.RAVI,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.RAVI.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_cyan_400)
            }
            Overlay.Kind.D_RAVI_Short->{
                allIndicatorInfo[0].label = "Timeframe Short"
                kindData = KindData(false,true,Kind.RAVI,0)
            }
            Overlay.Kind.D_RAVI_Long->{
                allIndicatorInfo[0].label = "Timeframe Long"
                kindData = KindData(false,true,Kind.RAVI,1)
            }
            Overlay.Kind.D_RAVI_Color ->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.RAVI,-1,0)
            }

            Overlay.Kind.Triple_EMA ->{
                values[0].value = 9.0
                values[0].min = 0.5
                values[0].max = 35.0
                this.timeFrame = 0
                allIndicatorInfo[0].label = "Triple EMA"
                allIndicatorInfo[0].selectedLegendLabel= "Triple EMA"
                kindData = KindData(true,false,Kind.Triple_EMA,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.Triple_EMA.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_green_700)
            }
            Overlay.Kind.D_TEMA_Timeframe->{
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.Triple_EMA,0)
            }
            Overlay.Kind.D_TEMA_COLOR ->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.Triple_EMA,-1,0)
            }

            Overlay.Kind.Periodical_Growth_Rate ->{
                values[0].value = 10.0
                values[0].min = 0.5
                values[0].max = 35.0
                this.timeFrame = 0
                separateChart = true
                allIndicatorInfo[0].label = this.kind.toString().replace("_", " ")
                allIndicatorInfo[0].selectedLegendLabel= "PGR"
                kindData = KindData(true,false,this.kind,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(this.kind.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_blue_300)
            }
            Overlay.Kind.D_PGR_Timeframe->{
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.Periodical_Growth_Rate,0)
            }
            Overlay.Kind.D_PGR_Color->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.Periodical_Growth_Rate,-1,0)
            }

            Overlay.Kind.Simple_Linear_Regression ->{
                values[0].value = 10.0
                values[0].min = 0.5
                values[0].max = 35.0
                this.timeFrame = 0
                separateChart = true
                allIndicatorInfo[0].label = this.kind.toString().replace("_", " ")
                allIndicatorInfo[0].selectedLegendLabel= "SLR"
                kindData = KindData(true,false,this.kind,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(this.kind.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_purple_400)
            }
            Overlay.Kind.D_SLR_Timeframe->{
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.Simple_Linear_Regression,0)
            }
            Overlay.Kind.D_SLR_Color->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.Simple_Linear_Regression,-1,0)
            }

            Overlay.Kind.Standard_Deviation ->{
                values[0].value = 10.0
                values[0].min = 0.5
                values[0].max = 35.0
                this.timeFrame = 0
                separateChart = true
                allIndicatorInfo[0].label = this.kind.toString().replace("_", " ")
                allIndicatorInfo[0].selectedLegendLabel= "SD"
                kindData = KindData(true,false,this.kind,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(this.kind.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_amber_500)
            }
            Overlay.Kind.D_SD_Timeframe->{
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.Standard_Deviation,0)
            }
            Overlay.Kind.D_SD_Color->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.Standard_Deviation,-1,0)
            }

            Overlay.Kind.Standard_Error ->{
                values[0].value = 10.0
                values[0].min = 0.5
                values[0].max = 35.0
                this.timeFrame = 0
                separateChart = true
                allIndicatorInfo[0].label = this.kind.toString().replace("_", " ")
                allIndicatorInfo[0].selectedLegendLabel= "SE"
                kindData = KindData(true,false,this.kind,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(this.kind.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_green_700)
            }
            Overlay.Kind.D_SE_Timefame->{
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.Standard_Error,0)
            }
            Overlay.Kind.D_SE_Color->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.Standard_Error,-1,0)
            }

            Overlay.Kind.Variance_Indicator ->{
                values[0].value = 10.0
                values[0].min = 0.5
                values[0].max = 35.0
                this.timeFrame = 0
                separateChart = true
                allIndicatorInfo[0].label = this.kind.toString().replace("_", " ")
                allIndicatorInfo[0].selectedLegendLabel= "VI"
                kindData = KindData(true,false,this.kind,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(this.kind.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_yellow_300)
            }
            Overlay.Kind.D_VI_Timeframe->{
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.Variance_Indicator,0)
            }
            Overlay.Kind.D_VI_Color->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.Variance_Indicator,-1,0)
            }

            Overlay.Kind.Mass_Index ->{
                values[0].value = 10.0
                values[0].min = 0.5
                values[0].max = 35.0
                values[1].value = 9.0
                values[1].min = 0.5
                values[1].max = 35.0
                this.timeFrame = 0
                this.timeFrame2 = 0
                separateChart = true
                allIndicatorInfo[0].label = this.kind.toString().replace("_", " ")
                allIndicatorInfo[0].selectedLegendLabel= "Mass Index"
                kindData = KindData(true,false,this.kind,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(this.kind.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_blue_300)
            }
            Overlay.Kind.D_MI_Timeframe->{
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.Mass_Index,0)
            }
            Overlay.Kind.D_MI_EMA_Timeframe->{
                allIndicatorInfo[0].label = "EMA Timeframe"
                kindData = KindData(false,true,Kind.Mass_Index,1)
            }
            Overlay.Kind.D_MI_Color->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.Mass_Index,-1,0)
            }

            Overlay.Kind.Random_Walking_Index ->{
                values[0].value = 9.0
                values[0].min = 0.5
                values[0].max = 35.0
                values[1].value = 9.0
                values[1].min = 0.5
                values[1].max = 35.0
                this.timeFrame = 0
                this.timeFrame2 = 1
                separateChart = true
                allIndicatorInfo[0].label = "Random Walking High"
                allIndicatorInfo[0].selectedLegendLabel= "Random Walking High"
                allIndicatorInfo[1].label = "Random Walking Low"
                allIndicatorInfo[1].selectedLegendLabel= "Random Walking Low"
                kindData = KindData(true,false,this.kind,-1,0, true, false)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.D_RW_Timeframe_High.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_green_700)
                allIndicatorInfo[1].color = sharedPref.getInt(Overlay.Kind.D_RW_Timeframe_Low.toString() + "_COLOR",0)
                allIndicatorInfo[1].colorDefault = ContextCompat.getColor(context,R.color.md_red_400)
            }
            Overlay.Kind.D_RW_Timeframe_High->{
                allIndicatorInfo[0].label = "Timeframe High"
                kindData = KindData(false,true,Kind.Random_Walking_Index,0,0,false,true)
            }
            Overlay.Kind.D_RW_Timeframe_Low->{
                allIndicatorInfo[0].label = "Timeframe Low"
                kindData = KindData(false,true,Kind.Random_Walking_Index,1,1,false,true)
            }

            Overlay.Kind.Fisher_Transform ->{
                values[0].value = 9.0
                values[0].min = 0.5
                values[0].max = 35.0
                this.timeFrame = 0
                separateChart = true
                allIndicatorInfo[0].label = this.kind.toString().replace("_", " ")
                allIndicatorInfo[0].selectedLegendLabel= "FT"
                kindData = KindData(true,false,this.kind,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(this.kind.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_orange_500)
            }
            Overlay.Kind.D_FT_Timeframe->{
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.Fisher_Transform,0)
            }
            Overlay.Kind.D_FT_Color->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.Fisher_Transform,-1,0)
            }

            Overlay.Kind.CCI ->{
                values[0].value = 20.0
                values[0].min = 0.5
                values[0].max = 35.0
                this.timeFrame = 0
                separateChart = true
                allIndicatorInfo[0].label = this.kind.toString().replace("_", " ")
                allIndicatorInfo[0].selectedLegendLabel= "CCI"
                kindData = KindData(true,false,Kind.CCI,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.CCI.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_brown_100)
            }
            Overlay.Kind.D_CCI_Timeframe->{
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.CCI,0)
            }
            Overlay.Kind.D_CCI_Color->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.CCI,-1,0)
            }
            Overlay.Kind.Average_Directional_Index ->{
                values[0].value = 14.0
                values[0].min = 0.5
                values[0].max = 35.0
                this.timeFrame = 0
                separateChart = true
                allIndicatorInfo[0].label = this.kind.toString().replace("_", " ")
                allIndicatorInfo[0].selectedLegendLabel= "ADX"
                allIndicatorInfo[1].label = "Minus Directional Indicator"
                allIndicatorInfo[1].selectedLegendLabel= "-DI"
                allIndicatorInfo[2].label = "Plus Direcitonal Indicator"
                allIndicatorInfo[2].selectedLegendLabel= "+DI"
                kindData = KindData(true,false,Kind.Average_Directional_Index,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.Average_Directional_Index.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_blue_grey_50)
                allIndicatorInfo[1].color = sharedPref.getInt(Overlay.Kind.D_ADX_Minus.toString() + "_COLOR",0)
                allIndicatorInfo[1].colorDefault = ContextCompat.getColor(context,R.color.md_red_400)
                allIndicatorInfo[2].color = sharedPref.getInt(Overlay.Kind.D_ADX_Plus.toString() + "_COLOR",0)
                allIndicatorInfo[2].colorDefault = ContextCompat.getColor(context,R.color.md_green_700)
            }
            Overlay.Kind.D_ADX_Timeframe->{
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.Average_Directional_Index,0)
            }
            Overlay.Kind.D_ADX_COLOR ->{
                allIndicatorInfo[0].label = "ADX Color"
                kindData = KindData(false,true,Kind.Average_Directional_Index,-1,0)
            }
            Overlay.Kind.D_ADX_Minus ->{
                allIndicatorInfo[0].label = "-DI Color"
                kindData = KindData(false,true,Kind.Average_Directional_Index,-1,1,false,true)
            }
            Overlay.Kind.D_ADX_Plus->{
                allIndicatorInfo[0].label = "+DI Color"
                kindData = KindData(false,true,Kind.Average_Directional_Index,-1,2,false,true)
            }

            Overlay.Kind.Kaufman_Adaptive_MA ->{
                values[0].value = 10.0
                values[0].min = 0.5
                values[0].max = 20.0
                values[1].value = 2.0
                values[1].min = 0.5
                values[1].max = 20.0
                values[2].value = 30.0
                values[2].min = 0.5
                values[2].max = 50.0
                this.timeFrame = 0
                this.timeFrameFast = 1
                this.timeFrameSlow = 2
                allIndicatorInfo[0].label = "Kaufman Adaptive MA"
                allIndicatorInfo[0].selectedLegendLabel= "KAMA"
                kindData = KindData(true,false,Kind.Kaufman_Adaptive_MA,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.Kaufman_Adaptive_MA.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context, R.color.md_pink_300)
            }
            Overlay.Kind.D_KAMA_Timeframe_Ratio ->{
                allIndicatorInfo[0].label = "Timeframe ratio"
                kindData = KindData(false,true,Kind.Kaufman_Adaptive_MA,0)
            }
            Overlay.Kind.D_KAMA_Timeframe_Fast->{
                allIndicatorInfo[0].label = "Timeframe fast"
                kindData = KindData(false,true,Kind.Kaufman_Adaptive_MA,1)
            }
            Overlay.Kind.D_KAMA_Timeframe_Slow->{
                allIndicatorInfo[0].label = "Timeframe Slow"
                kindData = KindData(false,true,Kind.Kaufman_Adaptive_MA,2)
            }

            Overlay.Kind.D_KAMA_COLOR ->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.Kaufman_Adaptive_MA,-1,0)
            }

            Overlay.Kind.AroonOsci -> {
                values[0].value = 14.0
                values[0].min = 2.0
                values[0].max = 50.0
                this.timeFrame = 0
                this.separateChart = true
                allIndicatorInfo[0].label = "Aroon Oscillator"
                allIndicatorInfo[0].selectedLegendLabel= "Aroon Oscillator"
                kindData = KindData(true,false,Kind.AroonOsci, -1,0, false, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.AroonOsci.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_cyan_500)
                allIndicatorInfo[0].filled = true
                allIndicatorInfo[0].filledColor = ContextCompat.getColor(context,R.color.md_cyan_500)
            }
            Overlay.Kind.AroonUpDown -> {
                values[0].value = 25.0
                values[0].min = 2.0
                values[0].max = 50.0
                this.timeFrame = 0
                this.separateChart = true
                allIndicatorInfo[0].label = "Aroon Up"
                allIndicatorInfo[1].label = "Aroon Down"
                allIndicatorInfo[0].selectedLegendLabel= "Aroon Up"
                allIndicatorInfo[1].selectedLegendLabel= "Aroon Down"
                kindData = KindData(true,false,Kind.AroonUpDown, -1,-1, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.D_AroonUp.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_green_300)
                allIndicatorInfo[1].color = sharedPref.getInt(Overlay.Kind.D_AroonDown.toString() + "_COLOR",0)
                allIndicatorInfo[1].colorDefault = ContextCompat.getColor(context,R.color.md_red_300)
            }
            Kind.D_ArronUpDown_Timeframe ->{
                values[0].value = 25.0
                values[0].min = 2.0
                values[0].max = 50.0
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.AroonUpDown,0)
            }
            Overlay.Kind.D_AroonUp -> {
                allIndicatorInfo[0].label = "Aroon Up"
                kindData = KindData(false,true,Kind.AroonUpDown,-1,0, false,true)
            }
            Overlay.Kind.D_AroonDown -> {
                allIndicatorInfo[0].label = "Aroon Down"
                kindData = KindData(false,true,Kind.AroonUpDown,-1,1, false,true)
            }

        //Detrend Price Ocelator
        //Commodity Channel Index
        //Average Directional Index

            Overlay.Kind.Exponential_MA_Ribbon ->{
                allIndicatorInfo[0].label = "EMA 1"
                allIndicatorInfo[1].label = "EMA 2"
                allIndicatorInfo[2].label = "EMA 3"
                allIndicatorInfo[3].label = "EMA 4"
                allIndicatorInfo[4].label = "EMA 5"
                allIndicatorInfo[5].label = "EMA 6"
                allIndicatorInfo[6].label = "EMA 7"
                allIndicatorInfo[7].label = "EMA 8"
                kindData = KindData(true,false,Kind.Exponential_MA_Ribbon)
                kindData.hasData = true
                allIndicatorInfo[0].color = ContextCompat.getColor(context,R.color.md_yellow_200)
                allIndicatorInfo[1].color = ContextCompat.getColor(context,R.color.md_yellow_500)
                allIndicatorInfo[2].color = ContextCompat.getColor(context,R.color.md_orange_200)
                allIndicatorInfo[3].color = ContextCompat.getColor(context,R.color.md_orange_300)
                allIndicatorInfo[4].color = ContextCompat.getColor(context,R.color.md_orange_400)
                allIndicatorInfo[5].color = ContextCompat.getColor(context,R.color.md_orange_500)
                allIndicatorInfo[6].color = ContextCompat.getColor(context,R.color.md_red_400)
                allIndicatorInfo[7].color = ContextCompat.getColor(context,R.color.md_red_600)
            }
            Overlay.Kind.Volume_Bars -> {
                this.separateChart = true
                allIndicatorInfo[0].label = "Volume"
                allIndicatorInfo[0].selectedLegendLabel= "Volume"
                kindData = KindData(true,false,Kind.Volume_Bars,-1,0,false,true)
            }
            Overlay.Kind.Notifications ->{
                kindData = KindData(true,false,Kind.Notifications,-1,-1, false)
                this.selected = true

            }
            Overlay.Kind.RSI ->{
                values[0].value = 14.0
                values[0].min = 2.0
                values[0].max = 50.0
                this.timeFrame = 0
                this.separateChart = true
                allIndicatorInfo[0].label = "RSI"
                allIndicatorInfo[0].selectedLegendLabel= "RSI"
                kindData = KindData(true,false,Kind.RSI,-1,0, true,true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.D_RSI_Timeframe.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_purple_600)
            }
            Overlay.Kind.D_RSI_Timeframe ->{
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.RSI,0,0, false)
            }
            Overlay.Kind.DPO ->{
                values[0].value = 20.0
                values[0].min = 2.0
                values[0].max = 50.0
                this.timeFrame = 0
                this.separateChart = true
                allIndicatorInfo[0].label = "Detrend Price Osci"
                allIndicatorInfo[0].selectedLegendLabel= "DPO"
                kindData = KindData(true,false,Kind.DPO,-1,0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.D_DPO_Timeframe.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_blue_700)
                allIndicatorInfo[0].filled = true
                allIndicatorInfo[0].filledColor = ContextCompat.getColor(context,R.color.md_cyan_500)
            }
            Overlay.Kind.D_DPO_Timeframe ->{
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.DPO,0,0, false)
            }

            Overlay.Kind.Stoch_Oscill -> {
                values[0].value = 14.0
                values[0].min = 2.0
                values[0].max = 50.0
                values[1].value = 3.0
                values[1].min = 1.0
                values[1].max = 20.0
                this.timeFrame = 0
                this.timeFrameSMA = 1
                this.separateChart = true
                allIndicatorInfo[0].label = "Stochastic Osci"
                allIndicatorInfo[0].selectedLegendLabel = "Stoch Osci K"
                allIndicatorInfo[1].selectedLegendLabel = "Stoch Osci D"
                kindData = KindData(true, false, Kind.Stoch_Oscill, -1, -1, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.D_Stoch_Oscill_K_Timeframe.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_blue_600)
                allIndicatorInfo[1].color = sharedPref.getInt(Overlay.Kind.D_Stoch_Oscill_SMA_Timeframe.toString() + "_COLOR",0)
                allIndicatorInfo[1].colorDefault = ContextCompat.getColor(context,R.color.md_red_400)
            }

            Overlay.Kind.D_Stoch_Oscill_K_Timeframe ->{
                allIndicatorInfo[0].label = "Stoc Osci Timeframe K"
                kindData = KindData(false,true,Kind.Stoch_Oscill,0,0, false,true)
            }

            Overlay.Kind.D_Stoch_Oscill_SMA_Timeframe ->{
                allIndicatorInfo[0].label = "Stoc Osci Timeframe SMA"
                kindData = KindData(false,true,Kind.Stoch_Oscill,1,1, false, true)
            }

            Overlay.Kind.Accumulation_Distribution -> {
                this.separateChart = true
                allIndicatorInfo[0].label = "Accumulation Distribution"
                allIndicatorInfo[0].selectedLegendLabel = "Accum/Distro"
                kindData = KindData(true, false, Kind.Accumulation_Distribution, -1, 0, true, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.Accumulation_Distribution.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_pink_300)
            }
            Overlay.Kind.D_AD_Color ->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.Accumulation_Distribution,-1,0)
            }

            Overlay.Kind.PPO -> {
                values[0].value = 12.0
                values[0].min = 2.0
                values[0].max = 52.0
                values[1].value = 26.0
                values[1].min = 2.0
                values[1].max = 52.0
                values[2].value = 9.0
                values[2].min = 2.0
                values[2].max = 24.0
                this.shortTerm = 0
                this.longTerm = 1
                this.PPO_EMA = 2
                this.separateChart = true
                allIndicatorInfo[0].label = "Percentage Price Oscillator"
                allIndicatorInfo[0].selectedLegendLabel = "PPO Short Term"
                allIndicatorInfo[1].selectedLegendLabel = "PPO Long Term"
                allIndicatorInfo[1].selectedLegendLabel = "EMA"
                kindData = KindData(true, false, Kind.PPO, -1, -1, true)
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.D_PPO_ShortTerm.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_green_400)
                allIndicatorInfo[1].color = sharedPref.getInt(Overlay.Kind.D_PPO_EMA.toString() + "_COLOR",0)
                allIndicatorInfo[1].colorDefault = ContextCompat.getColor(context,R.color.md_yellow_700)
            }
            Overlay.Kind.D_PPO_ShortTerm ->{
                allIndicatorInfo[0].label = "PPO Short Term"
                kindData = KindData(false,true,Kind.PPO,0,0, false,true)
            }
            Overlay.Kind.D_PPO_LongTerm ->{
                allIndicatorInfo[0].label = "PPO Long Term"
                kindData = KindData(false,true,Kind.PPO,1,-1, false,false)
            }
            Overlay.Kind.D_PPO_EMA ->{
                allIndicatorInfo[0].label = "EMA"
                kindData = KindData(false,true,Kind.PPO,2,1, false,true)
            }

            Kind.None ->{
                kindData = KindData(false,false,Kind.None)
            }

        }
        //When shared prefs are not avaliable(color == 0) then use the default color.
        for( item in allIndicatorInfo){
            if(item.color == 0){
                item.color = item.colorDefault
            }
        }
    }

    data class KindData(
            var visible: Boolean,
            val detailed: Boolean,
            val parentKind:Kind,
            val valueIndex: Int = -1,
            val colorIndex: Int = -1,
            val hasChildren: Boolean = false,
            var hasData: Boolean = false

    )
    enum class Kind{
        Volume_Bars,

        CandleStick,

        Average_Directional_Index,
        D_ADX_Timeframe,
        D_ADX_COLOR,
        D_ADX_Minus,
        D_ADX_Plus,

        Accumulation_Distribution,
        D_AD_Color,

        Awesome_Oscillator,
        D_AO_Timeframe1,
        D_AO_Timeframe2,
        D_AO_COLOR,

        AroonUpDown,
        D_ArronUpDown_Timeframe,
        D_AroonUp,
        D_AroonDown,

        AroonOsci,

        Chaikin_Money_Flow,
        D_CMF_Timeframe,
        D_CMF_COLOR,

        Chande_Momentum_Oscillator,
        D_CMO_Timeframe,
        D_CMO_COLOR,

        CCI,
        D_CCI_Timeframe,
        D_CCI_Color,

        Coppock_Curve,
        D_CC_Long_Timeframe,
        D_CC_Short_Timeframe,
        D_CC_WMA_Timeframe,
        D_CC_COLOR,

        DPO,
        D_DPO_Timeframe,

        Fisher_Transform,
        D_FT_Timeframe,
        D_FT_Color,

        Mass_Index,
        D_MI_Timeframe,
        D_MI_EMA_Timeframe,
        D_MI_Color,

        Random_Walking_Index,
        D_RW_Timeframe_High,
        D_RW_Timeframe_Low,

        PPO,
        D_PPO_ShortTerm,
        D_PPO_LongTerm,
        D_PPO_EMA,

        Stoch_Oscill,
        D_Stoch_Oscill_K_Timeframe,
        D_Stoch_Oscill_SMA_Timeframe,

        Rate_Of_Change,
        D_ROC_Timeframe,
        D_ROC_COLOR,

        RSI,
        D_RSI_Timeframe,

        Williams__R,
        D_WR_Timeframe,
        D_WR_COLOR,

        Ulcer_Index,
        D_UI_Timeframe,
        D_UI_COLOR,

        Positive_Volume,
        D_PV_COLOR,

        Periodical_Growth_Rate,
        D_PGR_Timeframe,
        D_PGR_Color,

        MACD,
        D_MACD_Timeframe_Short,
        D_MACD_Timeframe_Long,
        D_MACD_Color,

        Negative_Volume,
        D_NV_COLOR,

        On_Balance_Volume,
        D_OBV_COLOR,

        RAVI,
        D_RAVI_Short,
        D_RAVI_Long,
        D_RAVI_Color,

        Trailing_Stop_Loss,
        D_TSL_Distance,
        D_TSL_Initial_Limit,
        D_TSL_Color,

        Simple_Linear_Regression,
        D_SLR_Timeframe,
        D_SLR_Color,

        Standard_Deviation,
        D_SD_Timeframe,
        D_SD_Color,

        Standard_Error,
        D_SE_Timefame,
        D_SE_Color,

        Variance_Indicator,
        D_VI_Timeframe,
        D_VI_Color,

        Pivot_Point,
        D_PP_R1,
        D_PP_R2,
        D_PP_R3,
        D_PP_S1,
        D_PP_S2,
        D_PP_S3,

        DeMark_Pivot_Point,
        D_DMPP_Support,
        D_DMPP_Resistance,

        Fibonacci_Reversal_Support,
        D_FRS_1,
        D_FRS_2,
        D_FRS_3,
        D_FRS_4,

        Fibonacci_Reversal_Resistance,
        D_FRR_1,
        D_FRR_2,
        D_FRR_3,
        D_FRR_4,

        Bollinger_Bands,
        D_BB_Timeframe,
        D_BB_Middle,
        D_BB_Upper,
        D_BB_Lower,

        Bollinger_Band_Width,
        D_BBW_Color,

        Bollinger_Band_Percent_B,
        D_BBPB_TimeFrame,
        D_BBPB_K,
        D_BBPB_Color,

        Chandelier_Exit_Long,
        D_CEL_Timeframe,
        D_CEL_Ratio,
        D_CEL_Color,

        Chandelier_Exit_Short,
        D_CES_Timeframe,
        D_CES_Ratio,
        D_CES_Color,

        Exponential_MA,
        D_EMA_Timeframe,
        D_EMA_COLOR,

        Exponential_MA_Ribbon,

        Hull_Moving_Average,
        D_HMA_TimeFrame,
        D_HMA_COLOR,

        Ichimoku_Cloud,
        D_Ich_Cloud_Conversion,
        D_Ich_Cloud_Base,
        D_Ich_Cloud_Lead,
        D_Ich_Cloud_Lagging,
        D_Ich_Cloud_Lead_A,
        D_Ich_Cloud_Lead_B,

        Kaufman_Adaptive_MA,
        D_KAMA_Timeframe_Ratio,
        D_KAMA_Timeframe_Fast,
        D_KAMA_Timeframe_Slow,
        D_KAMA_COLOR,

        Keltner_Channel,
        D_KC_Timeframe,
        D_KC_Ratio,
        D_KC_Middle,
        D_KC_Upper,
        D_KC_Lower,

        //NOTE - VWAP needs to be before MVWAP
        Volume_Weighted_Average_Price,
        D_VWAP_TimeFrame,
        D_VWAP_COLOR,

        Moving_Volume_Weighted_Average_Price,
        D_MVWAP_TimeFrame,
        D_MVWAP_COLOR,

        Parabolic_SAR,
        D_P_SAR_ACC_FAC,
        D_P_SAR_MAX_ACC,
        D_P_SAR_COLOR,

        Simple_Moving_Avg,
        D_SMA_Timeframe,
        D_SMA_COLOR,


        ZigZag,
        D_ZigZag_Timeframe,
        D_ZigZag_COLOR,

        Zero_Lag_Moving_Average,
        D_ZLEMA_TimeFrame,
        D_ZLEMA_COLOR,

        Double_EMA,
        D_DEMA_Timeframe,
        D_DEMA_Color,

        Triple_EMA,
        D_TEMA_Timeframe,
        D_TEMA_COLOR,

        Notifications,

        None
    }

}