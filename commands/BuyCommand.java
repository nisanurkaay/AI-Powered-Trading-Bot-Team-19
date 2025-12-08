package commands;

import models.Order;

public class BuyCommand implements OrderCommand {
    private OrderReceiver receiver;
    private Order order;
    private double currentPrice;

    public BuyCommand(OrderReceiver receiver, Order order, double currentPrice) {
        this.receiver = receiver;
        this.order = order;
        this.currentPrice = currentPrice;
    }

    @Override
    public void execute() {
        receiver.placeBuyOrder(order, currentPrice);
    }

    @Override
    public void undo() {
        System.out.println("Undo Buy Command not implemented");
    }
}
