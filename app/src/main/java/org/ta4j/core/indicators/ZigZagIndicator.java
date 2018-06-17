package org.ta4j.core.indicators;

import org.ta4j.core.Decimal;
import org.ta4j.core.Indicator;

import java.util.ArrayList;

/**
 * Created by C0rbin on 11/29/2017.
 */
/**
 * ZigZag indicator.
 * <p>
 */
public class ZigZagIndicator extends CachedIndicator<Decimal> {

    private final Indicator<Decimal> indicator;

    private final int percentThreashold;

    public ZigZagIndicator(Indicator<Decimal> indicator, int percentThreashold) {
        super(indicator);
        this.indicator = indicator;
        this.percentThreashold = percentThreashold;
    }

    @Override
    protected Decimal calculate(int index) {
        ArrayList<Decimal> zigZagPoints = new ArrayList<Decimal>();
        boolean swingHigh = false, swingLow = false;
        Decimal obsLow = indicator.getValue(0), obsHigh = indicator.getValue(0);
        for (int i = 0; i <= indicator.getTimeSeries().getTickCount(); i++) {
            if(indicator.getValue(i).isGreaterThan(obsHigh)){
                obsHigh = indicator.getValue(i);

                if(!swingLow
                        && (obsHigh.minus(obsLow)
                                .dividedBy(obsLow)
                                .multipliedBy(Decimal.HUNDRED)
                                .isGreaterThanOrEqual(Decimal.valueOf(percentThreashold)))){
                    zigZagPoints.add(obsLow);
                    swingHigh = false;
                    swingLow = true;
                }
            }
            else if(indicator.getValue(i).isLessThan(obsLow)){
                if(!swingHigh
                        && (obsHigh.minus(obsLow)
                        .dividedBy(obsLow)
                        .multipliedBy(Decimal.HUNDRED)
                        .isGreaterThanOrEqual(Decimal.valueOf(percentThreashold)))){
                    zigZagPoints.add(obsLow);
                    swingHigh = true;
                    swingLow = false;
                }
                if(swingHigh) obsHigh = obsLow;
            }
        }
        return null;
    }
}
