package com.backflippedstudios.crypto_ta

import org.ta4j.core.BaseStrategy
import org.ta4j.core.BaseTimeSeries
import org.ta4j.core.Decimal
import org.ta4j.core.Strategy
import org.ta4j.core.Tick
import org.ta4j.core.TimeSeries
import org.ta4j.core.TimeSeriesManager
import org.ta4j.core.analysis.criteria.BuyAndHoldCriterion
import org.ta4j.core.analysis.criteria.MaximumDrawdownCriterion
import org.ta4j.core.analysis.criteria.TotalProfitCriterion
import org.ta4j.core.indicators.EMAIndicator
import org.ta4j.core.indicators.MACDIndicator
import org.ta4j.core.indicators.RSIIndicator
import org.ta4j.core.indicators.SMAIndicator
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule
import org.ta4j.core.trading.rules.OrRule
import org.ta4j.core.trading.rules.StopLossRule

/**
 * Runs preset trading strategies against a candle series using the ta4j
 * strategy engine that ships (unused until now) in the vendored library.
 * All money math is "all-in, no fees" — it answers "did the signal work",
 * not "what would my broker statement say".
 */
object Backtester {

    class Preset(
            val name: String,
            val description: String,
            val build: (TimeSeries) -> Strategy
    )

    val presets: List<Preset> = listOf(
            Preset("RSI 30/70",
                    "Buy when RSI(14) drops below 30, sell when it rises above 70") { series ->
                val rsi = RSIIndicator(ClosePriceIndicator(series), 14)
                BaseStrategy(
                        CrossedDownIndicatorRule(rsi, Decimal.valueOf(30)),
                        CrossedUpIndicatorRule(rsi, Decimal.valueOf(70)))
            },
            Preset("SMA 9/26 crossover",
                    "Buy when SMA(9) crosses above SMA(26), sell on the cross back down") { series ->
                val close = ClosePriceIndicator(series)
                val fast = SMAIndicator(close, 9)
                val slow = SMAIndicator(close, 26)
                BaseStrategy(
                        CrossedUpIndicatorRule(fast, slow),
                        CrossedDownIndicatorRule(fast, slow))
            },
            Preset("EMA 12/26 crossover",
                    "Buy when EMA(12) crosses above EMA(26), sell on the cross back down") { series ->
                val close = ClosePriceIndicator(series)
                val fast = EMAIndicator(close, 12)
                val slow = EMAIndicator(close, 26)
                BaseStrategy(
                        CrossedUpIndicatorRule(fast, slow),
                        CrossedDownIndicatorRule(fast, slow))
            },
            Preset("MACD signal cross",
                    "Buy when MACD(12,26) crosses above its 9-period signal line") { series ->
                val macd = MACDIndicator(ClosePriceIndicator(series), 12, 26)
                val signal = EMAIndicator(macd, 9)
                BaseStrategy(
                        CrossedUpIndicatorRule(macd, signal),
                        CrossedDownIndicatorRule(macd, signal))
            },
            Preset("Bollinger bounce",
                    "Buy when price crosses below the lower band, sell at the middle band") { series ->
                val close = ClosePriceIndicator(series)
                val middle = BollingerBandsMiddleIndicator(SMAIndicator(close, 20))
                val lower = BollingerBandsLowerIndicator(middle,
                        StandardDeviationIndicator(close, 20))
                BaseStrategy(
                        CrossedDownIndicatorRule(close, lower),
                        CrossedUpIndicatorRule(close, middle))
            },
            Preset("SMA cross + 5% stop loss",
                    "SMA 9/26 crossover, but also bail out on a 5% loss") { series ->
                val close = ClosePriceIndicator(series)
                val fast = SMAIndicator(close, 9)
                val slow = SMAIndicator(close, 26)
                BaseStrategy(
                        CrossedUpIndicatorRule(fast, slow),
                        OrRule(CrossedDownIndicatorRule(fast, slow),
                                StopLossRule(close, Decimal.valueOf(5))))
            }
    )

    class Result(
            val presetName: String,
            val profitPercent: Double,
            val buyHoldPercent: Double,
            val maxDrawdownPercent: Double,
            val winRatePercent: Double,
            val tradeCount: Int,
            /** (entryIndex, exitIndex) per closed trade, plus a possible open entry */
            val trades: List<Pair<Int, Int?>>
    ) {
        val beatBuyHold: Boolean get() = profitPercent > buyHoldPercent
    }

    /** Runs every preset and returns results ranked best-profit-first. */
    fun runAll(ticks: List<Tick>): List<Result> {
        val results = ArrayList<Result>()
        for (preset in presets) {
            try {
                run(ticks, preset)?.let { results.add(it) }
            } catch (e: Exception) {
                println("Backtest '${preset.name}' failed: ${e.message}")
            }
        }
        return results.sortedByDescending { it.profitPercent }
    }

    fun run(ticks: List<Tick>, preset: Preset): Result? {
        if (ticks.size < 30) return null
        val series: TimeSeries = BaseTimeSeries(ArrayList(ticks))
        val strategy = preset.build(series)
        val record = TimeSeriesManager(series).run(strategy)

        val profit = TotalProfitCriterion().calculate(series, record)
        val buyHold = BuyAndHoldCriterion().calculate(series, record)
        val drawdown = MaximumDrawdownCriterion().calculate(series, record)

        val trades = ArrayList<Pair<Int, Int?>>()
        var closedCount = 0
        var wins = 0
        for (trade in record.trades) {
            if (trade.isClosed) {
                trades.add(Pair(trade.entry.index, trade.exit.index))
                closedCount++
                if (trade.exit.price.isGreaterThan(trade.entry.price)) wins++
            }
        }
        record.currentTrade?.let {
            if (it.isOpened) trades.add(Pair(it.entry.index, null))
        }
        val winRate = if (closedCount > 0) wins.toDouble() / closedCount else 0.0

        return Result(
                preset.name,
                (profit - 1.0) * 100.0,
                (buyHold - 1.0) * 100.0,
                drawdown * 100.0,
                winRate * 100.0,
                trades.size,
                trades)
    }
}
