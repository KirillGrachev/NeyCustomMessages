package eu.neydev.custommessages.service.strategy;

import eu.neydev.custommessages.service.strategy.context.NotificationContext;

public interface NotificationStrategy {

    void execute(NotificationContext context);
    boolean isEnabled(NotificationContext context);

}