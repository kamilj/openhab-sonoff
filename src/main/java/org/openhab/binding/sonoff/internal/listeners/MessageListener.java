package org.openhab.binding.sonoff.internal.listeners;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sonoff.internal.helpers.CommandMessage;

@NonNullByDefault
public interface MessageListener {

    void sendMessage(CommandMessage message);

    void okMessage(Long sequence);
}
