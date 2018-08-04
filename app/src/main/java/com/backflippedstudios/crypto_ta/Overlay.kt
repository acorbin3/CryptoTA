package com.backflippedstudios.crypto_ta

import android.content.Context
import android.support.v4.content.ContextCompat

data class Overlay(val kind: Kind){

    enum class IndicatorType{
        Line,
        Scatter
    }
    data class IndicatorInfo(
            var label: String = "",
            var color: Int = 0,
            var colorDefault: Int = 0,
            var filled: Boolean = false,
            var filledColor: Int = 0,
            var type: IndicatorType = IndicatorType.Line
    )
    data class Values(var value: Double = -1.0, var max: Double = 0.0, var min: Double = 0.0)
    var title: String = kind.toString().replace("_"," ")
    var timeFrame: Int = 0
    var ratio: Int = 0
    var accelerationFactor: Int = 0
    var maximumAcceleration: Int = 0
    var conversionPeriod: Int = 0
    var basePeriod: Int = 0
    var leadingPeriod: Int = 0
    var laggingPeriod: Int = 0
    var thresholdPercent: Int = 0
    var separateChart: Boolean = false
    var valuesAreInts: Boolean = true
    var chartType: ChartStatusData.Type = ChartStatusData.Type.MAIN_CHART
    // The values represents each of the editable text items that an overlay can edit. A positive
    // value will mean that it can be visible and editable
    var values: Array<Values> = Array(4,{ _ -> Values()})
    var valuesScaleFactor: Int = 1
    var selected: Boolean = false
    var allIndicatorInfo: Array<IndicatorInfo> = Array(8,{_ -> IndicatorInfo() })
    var kindData: KindData
    //This is attempt to have 1 location for all the configuration
    init {
        when(this.kind){
            Overlay.Kind.Bollinger_Bands ->{
                values[0].value = 20.0
                values[0].min = 2.0
                values[0].max = 50.0
                this.timeFrame = 0
                allIndicatorInfo[0].label = "BB Middle"
                allIndicatorInfo[1].label = "BB Upper"
                allIndicatorInfo[2].label = "BB Lower"
                kindData = KindData(true,false,Kind.Bollinger_Bands, -1,-1, true)
            }
            Kind.D_BB_Timeframe ->{
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.Bollinger_Bands,0)
            }
            Overlay.Kind.D_BB_Middle -> {
                allIndicatorInfo[0].label = "BB Middle Color"
                kindData = KindData(false,true,Kind.Bollinger_Bands,-1,0)
            }
            Overlay.Kind.D_BB_Upper -> {
                allIndicatorInfo[0].label = "BB Upper Color"
                kindData = KindData(false,true,Kind.Bollinger_Bands,-1,1)
            }
            Overlay.Kind.D_BB_Lower -> {
                allIndicatorInfo[0].label = "BB Lower Color"
                kindData = KindData(false,true,Kind.Bollinger_Bands,-1,2)
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
                allIndicatorInfo[0].label = "KC Middle Color"
                allIndicatorInfo[1].label = "KC Upper Color"
                allIndicatorInfo[2].label = "KC Lower Color"
                kindData = KindData(true,false,Kind.Keltner_Channel, -1,-1, true)
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
                kindData = KindData(false,true,Kind.Keltner_Channel,-1,0)
            }
            Overlay.Kind.D_KC_Upper -> {
                allIndicatorInfo[0].label = "KC Upper Color"
                kindData = KindData(false,true,Kind.Keltner_Channel,-1,1)
            }
            Overlay.Kind.D_KC_Lower -> {
                allIndicatorInfo[0].label = "KC Lower Color"
                kindData = KindData(false,true,Kind.Keltner_Channel,-1,2)
            }
            Overlay.Kind.Simple_Moving_Avg ->{
                values[0].value = 20.0
                values[0].min = 2.0
                values[0].max = 50.0
                this.timeFrame = 0
                allIndicatorInfo[0].label = "Simple Moving Average"
                kindData = KindData(true,false,Kind.Simple_Moving_Avg, -1,-1, true)
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
                kindData = KindData(true,false,Kind.Exponential_MA,-1,-1,true)
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
                kindData = KindData(true,false,Kind.Parabolic_SAR, -1,-1, true)
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
            Overlay.Kind.Chandelier_Exit ->{
                values[0].value = 22.0
                values[0].min = 2.0
                values[0].max = 50.0
                values[1].value = 3.0
                values[1].min = 2.0
                values[1].max = 5.0
                this.timeFrame = 0
                this.ratio = 1
                allIndicatorInfo[0].label = "Chandelier Exit"
                kindData = KindData(true,false,Kind.Chandelier_Exit, -1,-1, true)
            }
            Overlay.Kind.D_CE_Timeframe -> {
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.Chandelier_Exit,0)
            }
            Overlay.Kind.D_CE_Ratio -> {
                allIndicatorInfo[0].label = "Ratio"
                kindData = KindData(false,true,Kind.Chandelier_Exit,1)
            }
            Kind.D_CE_Color ->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.Chandelier_Exit,-1,0)
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
                kindData = KindData(true,false,Kind.Ichimoku_Cloud, -1,-1, true)
            }
            Overlay.Kind.D_Ich_Cloud_Conversion -> {
                allIndicatorInfo[0].label = "Conversion"
                kindData = KindData(false,true,Kind.Ichimoku_Cloud,0,2)
            }
            Overlay.Kind.D_Ich_Cloud_Base -> {
                allIndicatorInfo[0].label = "Base"
                kindData = KindData(false,true,Kind.Ichimoku_Cloud,1,3)
            }

            Overlay.Kind.D_Ich_Cloud_Lead -> {
                allIndicatorInfo[0].label = "Leading"
                kindData = KindData(false,true,Kind.Ichimoku_Cloud,2,-1)
            }

            Overlay.Kind.D_Ich_Cloud_Lagging -> {
                allIndicatorInfo[0].label = "Lagging"
                kindData = KindData(false,true,Kind.Ichimoku_Cloud,3,4)
            }
            Overlay.Kind.D_Ich_Cloud_Lead_A -> {
                allIndicatorInfo[0].label = "Leading A"
                kindData = KindData(false,true,Kind.Ichimoku_Cloud,-1,0)
            }
            Overlay.Kind.D_Ich_Cloud_Lead_B -> {
                allIndicatorInfo[0].label = "Leading B"
                kindData = KindData(false,true,Kind.Ichimoku_Cloud,-1,1)
            }
            Overlay.Kind.ZigZag ->{
                values[0].value = 7.0
                values[0].min = 0.5
                values[0].max = 20.0
                this.thresholdPercent = 0
                allIndicatorInfo[0].label = "Zig Zag"
                kindData = KindData(true,false,Kind.ZigZag,-1,-1, true)
            }
            Overlay.Kind.D_ZigZag_Timeframe ->{
                allIndicatorInfo[0].label = "Timeframe"
                kindData = KindData(false,true,Kind.ZigZag,0)
            }
            Overlay.Kind.D_ZigZag_COLOR ->{
                allIndicatorInfo[0].label = "Line Color"
                kindData = KindData(false,true,Kind.ZigZag,-1,0)
            }
            Overlay.Kind.AroonOsci -> {
                values[0].value = 14.0
                values[0].min = 2.0
                values[0].max = 50.0
                this.timeFrame = 0
                this.separateChart = true
                this.chartType = ChartStatusData.Type.AROON_OSCI_CHART
                allIndicatorInfo[0].label = "Aroon Oscillator"
                kindData = KindData(true,false,Kind.AroonOsci, -1,-1, false)
            }
            Overlay.Kind.AroonUpDown -> {
                values[0].value = 25.0
                values[0].min = 2.0
                values[0].max = 50.0
                this.timeFrame = 0
                this.separateChart = true
                this.chartType = ChartStatusData.Type.AROON_UP_DOWN_CHART
                allIndicatorInfo[0].label = "Aroon Up"
                allIndicatorInfo[1].label = "Aroon Down"
                kindData = KindData(true,false,Kind.AroonUpDown, -1,-1, true)
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
                kindData = KindData(false,true,Kind.AroonUpDown,-1,0)
            }
            Overlay.Kind.D_AroonDown -> {
                allIndicatorInfo[0].label = "Aroon Down"
                kindData = KindData(false,true,Kind.AroonUpDown,-1,1)
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
            }
            Overlay.Kind.Volume_Bars -> {
                this.separateChart = true
                this.chartType = ChartStatusData.Type.VOLUME_CHART
                kindData = KindData(true,false,Kind.Volume_Bars)
            }
            Overlay.Kind.Notifications ->{
                kindData = KindData(true,false,Kind.Notifications,-1,-1, false)
                this.selected = true

            }
        }
    }
    fun updateColors(context: Context) {
        var sharedPref = context.getSharedPreferences("com.skydoves.colorpickerpreference", Context.MODE_PRIVATE);


        when(this.kind){
            Overlay.Kind.AroonOsci -> {
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.AroonOsci.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_cyan_500)
                allIndicatorInfo[0].filled = true
                allIndicatorInfo[0].filledColor = ContextCompat.getColor(context,R.color.md_cyan_500)
            }
            Overlay.Kind.Volume_Bars -> {}
            Overlay.Kind.AroonUpDown -> {
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.D_AroonUp.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_green_300)
                allIndicatorInfo[1].color = sharedPref.getInt(Overlay.Kind.D_AroonDown.toString() + "_COLOR",0)
                allIndicatorInfo[1].colorDefault = ContextCompat.getColor(context,R.color.md_red_300)
            }
            Overlay.Kind.Bollinger_Bands -> {
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.D_BB_Middle.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context, R.color.md_red_500)

                allIndicatorInfo[1].color = sharedPref.getInt(Overlay.Kind.D_BB_Upper.toString() + "_COLOR",0)
                allIndicatorInfo[1].colorDefault = ContextCompat.getColor(context, R.color.md_cyan_500)

                allIndicatorInfo[2].color = sharedPref.getInt(Overlay.Kind.D_BB_Lower.toString() + "_COLOR",0)
                allIndicatorInfo[2].colorDefault = ContextCompat.getColor(context, R.color.md_blue_500)
            }
            Overlay.Kind.Keltner_Channel -> {
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.D_KC_Middle.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_purple_500)
                allIndicatorInfo[1].color = sharedPref.getInt(Overlay.Kind.D_KC_Upper.toString() + "_COLOR",0)
                allIndicatorInfo[1].colorDefault = ContextCompat.getColor(context,R.color.md_yellow_700)
                allIndicatorInfo[2].color = sharedPref.getInt(Overlay.Kind.D_KC_Lower.toString() + "_COLOR",0)
                allIndicatorInfo[2].colorDefault = ContextCompat.getColor(context,R.color.md_yellow_700)
            }
            Overlay.Kind.Simple_Moving_Avg -> {
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.Simple_Moving_Avg.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_amber_700)
            }
            Overlay.Kind.Exponential_MA -> {
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.Exponential_MA.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_light_blue_300)
            }
            Overlay.Kind.Parabolic_SAR -> {
                allIndicatorInfo[0].type = IndicatorType.Scatter
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.D_P_SAR_COLOR.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_white_1000)
            }
            Overlay.Kind.Chandelier_Exit -> {
                allIndicatorInfo[0].color = sharedPref.getInt(Overlay.Kind.Chandelier_Exit.toString() + "_COLOR",0)
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context,R.color.md_purple_500)
            }
            Overlay.Kind.Ichimoku_Cloud -> {
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
            Overlay.Kind.ZigZag -> {
                allIndicatorInfo[0].colorDefault = ContextCompat.getColor(context, R.color.md_cyan_500)

            }
            Overlay.Kind.Exponential_MA_Ribbon -> {
                allIndicatorInfo[0].color = ContextCompat.getColor(context,R.color.md_yellow_200)
                allIndicatorInfo[1].color = ContextCompat.getColor(context,R.color.md_yellow_500)
                allIndicatorInfo[2].color = ContextCompat.getColor(context,R.color.md_orange_200)
                allIndicatorInfo[3].color = ContextCompat.getColor(context,R.color.md_orange_300)
                allIndicatorInfo[4].color = ContextCompat.getColor(context,R.color.md_orange_400)
                allIndicatorInfo[5].color = ContextCompat.getColor(context,R.color.md_orange_500)
                allIndicatorInfo[6].color = ContextCompat.getColor(context,R.color.md_red_400)
                allIndicatorInfo[7].color = ContextCompat.getColor(context,R.color.md_red_600)
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
            val hasChildren: Boolean = false
    )
    enum class Kind{
        Volume_Bars,

        AroonUpDown,
        D_ArronUpDown_Timeframe,
        D_AroonUp,
        D_AroonDown,

        AroonOsci,

        Bollinger_Bands,
        D_BB_Timeframe,
        D_BB_Middle,
        D_BB_Upper,
        D_BB_Lower,

        Keltner_Channel,
        D_KC_Timeframe,
        D_KC_Ratio,
        D_KC_Middle,
        D_KC_Upper,
        D_KC_Lower,

        Simple_Moving_Avg,
        D_SMA_Timeframe,
        D_SMA_COLOR,

        Exponential_MA,
        D_EMA_Timeframe,
        D_EMA_COLOR,

        Parabolic_SAR,
        D_P_SAR_ACC_FAC,
        D_P_SAR_MAX_ACC,
        D_P_SAR_COLOR,

        Chandelier_Exit,
        D_CE_Timeframe,
        D_CE_Ratio,
        D_CE_Color,

        Ichimoku_Cloud,
        D_Ich_Cloud_Conversion,
        D_Ich_Cloud_Base,
        D_Ich_Cloud_Lead,
        D_Ich_Cloud_Lagging,
        D_Ich_Cloud_Lead_A,
        D_Ich_Cloud_Lead_B,

        ZigZag,
        D_ZigZag_Timeframe,
        D_ZigZag_COLOR,
        Exponential_MA_Ribbon,

        Notifications
    }

}