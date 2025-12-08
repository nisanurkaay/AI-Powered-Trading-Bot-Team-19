package commands;

import models.Order;

public class SellCommand implements OrderCommand {
    private OrderReceiver receiver;
    private Order order;
    private double currentPrice;

    public SellCommand(OrderReceiver receiver, Order order, double currentPrice) {
        this.receiver = receiver;
        this.order = order;
        this.currentPrice = currentPrice;
    }

    @Override
    public void execute() {
        receiver.placeSellOrder(order, currentPrice);
    }

    @Override
    public void undo() {
        System.out.println("Undo Sell Command not implemented ");
    }
}
