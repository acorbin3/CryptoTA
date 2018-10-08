package com.backflippedstudios.crypto_ta

class ChartStatusData(var status: Status, var type: Type, var kind: Overlay.Kind){
    enum class Status{
        LOADING,
        INITIAL_LOAD,
        UPDATE_CHART,
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