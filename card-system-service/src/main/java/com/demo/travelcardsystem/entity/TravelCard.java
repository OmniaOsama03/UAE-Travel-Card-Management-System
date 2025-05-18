package com.demo.travelcardsystem.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode
public class TravelCard implements Observable<TravelCard> {

    @EqualsAndHashCode.Include
    private String cardNumber;
    private double balance;
    private Journey currentJourney;

    // Move observerCollection here instead of the interface
    private final List<Observer<TravelCard>> observerCollection = new ArrayList<>();

    public synchronized void addCredit(double amount) {
        balance += amount;
    }

    public synchronized void debitAmount(double amount) {
        balance -= amount;
    }

    @Override
    public void notifyAllObservers() {
        observerCollection.forEach(observer ->
            observer.reactOnChange(this)
        );
    }

    @Override
    public void registerObserver(Observer<TravelCard> observer) {
        observerCollection.add(observer);
    }
}
