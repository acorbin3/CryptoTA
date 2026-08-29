package com.backflippedstudios.crypto_ta

import org.junit.Assert.assertTrue
import org.junit.Test
import org.ta4j.core.BaseTick
import org.ta4j.core.Decimal
import org.ta4j.core.Tick
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

class BacktesterTest {

    // A long sine-wave price series: every crossover strategy should trade
    // repeatedly and every trade should have distinct entry/exit prices.
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
    fun smaCrossoverTradesOnSineWave() {
        val ticks = sineTicks(350)
        val preset = Backtester.presets.first { it.name.startsWith("SMA 9/26") }
        val result = Backtester.run(ticks, preset)!!
        println("SMA trades=${result.tradeCount} profit=${result.profitPercent} " +
                "buyHold=${result.buyHoldPercent} winRate=${result.winRatePercent}")
        println("trade indexes=${result.trades}")
        assertTrue("expected multiple trades on a sine wave, got ${result.tradeCount}",
                result.tradeCount >= 3)
        assertTrue("expected nonzero profit", Math.abs(result.profitPercent) > 0.01)
    }

    @Test
    fun allPresetsProduceResults() {
        val results = Backtester.runAll(sineTicks(350))
        for (r in results) {
            println("${r.presetName}: trades=${r.tradeCount} profit=${"%.2f".format(r.profitPercent)}%")
        }
        assertTrue(results.isNotEmpty())
    }
}
