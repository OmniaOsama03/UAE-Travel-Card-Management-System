package com.demo.travelcardsystem.entity;



public interface Observable<T> {

    //List<T> observerCollection = new ArrayList<>(); Not needed anymore

    void notifyAllObservers();

    void registerObserver(Observer<T> observer);

}

