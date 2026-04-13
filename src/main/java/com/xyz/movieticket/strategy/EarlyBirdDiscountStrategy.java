package com.xyz.movieticket.strategy;

import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
public class EarlyBirdDiscountStrategy implements DiscountStrategy {

    @Override
    public boolean isApplicable(PricingContext context) {
        // Apply if booking is made at least 7 days in advance
        // This would need show time from context - simplified for demo
        return false;
    }

    @Override
    public BigDecimal calculateDiscount(PricingContext context) {
        return BigDecimal.ZERO;
    }

    @Override
    public String getName() {
        return "Early Bird Discount (10% off)";
    }
}