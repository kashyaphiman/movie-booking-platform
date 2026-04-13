package com.xyz.movieticket.strategy;

import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;

@Slf4j
public class BulkDiscountStrategy implements DiscountStrategy {

    @Override
    public boolean isApplicable(PricingContext context) {
        return context.getNumberOfSeats() >= 3;
    }

    @Override
    public BigDecimal calculateDiscount(PricingContext context) {
        // 50% discount on every third ticket
        int numberOfDiscountedSeats = context.getNumberOfSeats() / 3;
        BigDecimal discount = context.getBasePrice()
                .multiply(BigDecimal.valueOf(numberOfDiscountedSeats))
                .multiply(BigDecimal.valueOf(0.5));

        log.info("Applied bulk discount: {}% off on {} tickets", 50, numberOfDiscountedSeats);
        return discount;
    }

    @Override
    public String getName() {
        return "Bulk Booking Discount (50% off on 3rd ticket)";
    }
}