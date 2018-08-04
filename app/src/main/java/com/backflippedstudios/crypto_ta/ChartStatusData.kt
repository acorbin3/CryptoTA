package com.backflippedstudios.crypto_ta

class ChartStatusData(var status: Status, var type: Type){
    enum class Status{
        LOADING,
        INITIAL_LOAD,
        UPDATE_CHART,
        UPDATE_FAILED,
        UPDATE_CANDLESTICKS,
        UPDATE_OVERLAYS,
        INTERNET_OUT
    }

    enum class Type{
        MAIN_CHART,
        VOLUME_CHART,
        AROON_OSCI_CHART,
        AROON_UP_DOWN_CHART,
    }

}