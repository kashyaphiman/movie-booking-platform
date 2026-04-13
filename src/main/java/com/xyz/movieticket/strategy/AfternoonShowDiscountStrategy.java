package com.xyz.movieticket.strategy;

import com.xyz.movieticket.model.enums.ShowType;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;

@Slf4j
public class AfternoonShowDiscountStrategy implements DiscountStrategy {

    @Override
    public boolean isApplicable(PricingContext context) {
        return context.getShowType() == ShowType.AFTERNOON;
    }

    @Override
    public BigDecimal calculateDiscount(PricingContext context) {
        // 20% discount on afternoon shows
        BigDecimal discount = context.getSubtotal().multiply(BigDecimal.valueOf(0.20));
        log.info("Applied afternoon show discount: 20% off");
        return discount;
    }

    @Override
    public String getName() {
        return "Afternoon Show Discount (20% off)";
    }
}