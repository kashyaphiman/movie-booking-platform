package com.xyz.movieticket.service;

import com.xyz.movieticket.dto.response.PriceBreakdown;
import com.xyz.movieticket.model.User;
import com.xyz.movieticket.model.enums.ShowType;
import com.xyz.movieticket.strategy.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PricingService {

    private final List<DiscountStrategy> discountStrategies;

    public PricingService() {
        this.discountStrategies = new ArrayList<>();
        this.discountStrategies.add(new BulkDiscountStrategy());      // 50% off on 3rd ticket
        this.discountStrategies.add(new AfternoonShowDiscountStrategy()); // 20% off afternoon shows
        this.discountStrategies.add(new EarlyBirdDiscountStrategy());
    }

    public PriceBreakdown calculatePrice(BigDecimal basePrice, int numberOfSeats, ShowType showType, User user) {
        BigDecimal subtotal = basePrice.multiply(BigDecimal.valueOf(numberOfSeats));
        BigDecimal totalDiscount = BigDecimal.ZERO;
        List<PriceBreakdown.AppliedDiscount> appliedDiscounts = new ArrayList<>();

        PricingContext context = PricingContext.builder()
                .basePrice(basePrice)
                .numberOfSeats(numberOfSeats)
                .showType(showType)
                .user(user)
                .subtotal(subtotal)
                .build();

        for (DiscountStrategy strategy : discountStrategies) {
            if (strategy.isApplicable(context)) {
                BigDecimal discount = strategy.calculateDiscount(context);
                totalDiscount = totalDiscount.add(discount);
                appliedDiscounts.add(PriceBreakdown.AppliedDiscount.builder()
                        .discountName(strategy.getName())
                        .discountAmount(discount)
                        .build());
            }
        }

        BigDecimal finalAmount = subtotal.subtract(totalDiscount);

        return PriceBreakdown.builder()
                .subtotal(subtotal)
                .totalDiscount(totalDiscount)
                .finalAmount(finalAmount)
                .appliedDiscounts(appliedDiscounts)
                .build();
    }
}