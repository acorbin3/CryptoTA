package com.backflippedstudios.crypto_ta

import org.junit.Test
import org.ta4j.core.BaseStrategy
import org.ta4j.core.BaseTick
import org.ta4j.core.BaseTimeSeries
import org.ta4j.core.Decimal
import org.ta4j.core.Tick
import org.ta4j.core.TimeSeriesManager
import org.ta4j.core.indicators.SMAIndicator
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

class EngineProbeTest {

    private fun sineTicks(n: Int): ArrayList<Tick> {
        val ticks = ArrayList<Tick>()
        var time = ZonedDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))
        for (i in 0 until n) {
            val price = 100.0 + 20.0 * Math.sin(i / 12.0)
            ticks.add(BaseTick(time,
                    Decimal.valueOf(price),
                    Decimal.valueOf(price + 1),
                    Decimal.valueOf(price - 1),
                    Decimal.valueOf(price),
                    Decimal.valueOf(1000.0)))
            time = time.plusMinutes(1)
        }
        return ticks
    }

    @Test
    fun probeRules() {
        val series = BaseTimeSeries(sineTicks(200))
        val close = ClosePriceIndicator(series)
        val fast = SMAIndicator(close, 9)
        val slow = SMAIndicator(close, 26)

        println("series begin=${series.beginIndex} end=${series.endIndex} tickCount=${series.tickCount}")
        for (i in intArrayOf(0, 10, 30, 50, 70, 90)) {
            println("i=$i close=${close.getValue(i)} fast=${fast.getValue(i)} slow=${slow.getValue(i)}")
        }

        val up = CrossedUpIndicatorRule(fast, slow)
        val down = CrossedDownIndicatorRule(fast, slow)
        val upHits = (0..series.endIndex).filter { up.isSatisfied(it, null) }
        val downHits = (0..series.endIndex).filter { down.isSatisfied(it, null) }
        println("crossUp at $upHits")
        println("crossDown at $downHits")

        val record = TimeSeriesManager(series).run(BaseStrategy(up, down))
        println("closed trades=${record.trades.size} currentTrade=${record.currentTrade}")
        for (t in record.trades) {
            println("trade entry=${t.entry?.index}@${t.entry?.price} exit=${t.exit?.index}@${t.exit?.price}")
        }
    }
}
