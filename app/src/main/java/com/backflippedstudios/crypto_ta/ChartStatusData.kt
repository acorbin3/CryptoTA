package com.backflippedstudios.crypto_ta

class ChartStatusData(var status: Status, var type: Type, var kind: Overlay.Kind, var recalculate: Boolean = false){
    enum class Status{
        LOADING,
        INITIAL_LOAD,
        UPDATE_CHART, // This is used when needed to recalculate
        TOGGLE_CHART, //This is used for add or removing items
        UPDATE_FAILED,
        UPDATE_CANDLESTICKS,
        UPDATE_OVERLAYS,
        INTERNET_OUT,
        LOADING_COMPLETE
    }

    enum class Type{
        MAIN_CHART,
        SEPARATE_CHART,
    }

}