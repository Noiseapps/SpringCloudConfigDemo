package com.ftq.app1;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RefreshScope
@RequestMapping("/price")
public class PromotionPriceController {

    private final StrategyFactory factory;

    PromotionPriceController(StrategyFactory factory) {
        this.factory = factory;
    }

    @GetMapping
    ResponseEntity<Price> calculatePrice(@RequestParam("amount") double price) {
        return ResponseEntity.ok(new Price(factory.get().calculatePrice(price)));
    }
}

record Price(double value) {
}

interface CalculatorStrategy {

    PromotionType supportedType();

    double calculatePrice(double price);
}

@Component
@RefreshScope
class StrategyFactory {

    private final PromotionType type;

    private final Set<CalculatorStrategy> strategies;

    StrategyFactory(@Value("${promotion.type:CONSTANT}") String type, Set<CalculatorStrategy> strategies) {
        this.type = PromotionType.valueOf(type);
        this.strategies = strategies;
    }

    CalculatorStrategy get() {
        return strategies.stream().filter((strategy) -> strategy.supportedType().equals(type)).findFirst().orElseThrow();
    }
}

@RefreshScope
@Component
class ConstantRebateStrategy implements CalculatorStrategy {

    @Value("${promotion.constant.amount:5}")
    private int amount;
    @Value("${promotion.constant.min-amount:50}")
    private int minimumAmount;

    @Override
    public PromotionType supportedType() {
        return PromotionType.CONSTANT;
    }

    @Override
    public double calculatePrice(double price) {
        if (price > minimumAmount) {
            return price - amount;
        } else {
            return price;
        }
    }
}

@RefreshScope
@Component
class PercentRebateStrategy implements CalculatorStrategy {
    @Value("${promotion.percent.amount:5}")
    private int amount;

    @Override
    public PromotionType supportedType() {
        return PromotionType.PERCENT;
    }

    @Override
    public double calculatePrice(double price) {
        return price * (100.0 - amount) / 100.0;
    }
}

enum PromotionType {
    CONSTANT, PERCENT
}