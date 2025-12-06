package PriceObservers;

import java.util.ArrayList;
import java.util.List;

import interfaces.Observer;
import interfaces.Subject;
import models.Price;

public class PriceSubject implements Subject{
    List<Observer> observers = new ArrayList<>();
    Price price;
    
    @Override
    public void register(Observer o) {
        observers.add(o);
        System.out.println("Observer added succesfully");
    }

    @Override
    public void remove(Observer o) {
        if(observers.remove(o)){
            System.out.println("Observer removed succesfully");
        }else{
            System.out.println("Observer couldn'd removed!!");
        }
    }

    @Override
    public void notifyObservers() {
        for(int i = 0; i <observers.size(); i++){
            observers.get(i).priceUpdated(price);
        }
        System.out.println("Observers Notified!!");
    }
    public void setPrice(Price price){
        this.price = price;
    }
    
}
