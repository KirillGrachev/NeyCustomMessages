package com.ney.messages.service.strategy;

import com.ney.messages.service.strategy.context.NotificationContext;

public interface NotificationStrategy {

    void execute(NotificationContext context);
    boolean isEnabled(NotificationContext context);

}