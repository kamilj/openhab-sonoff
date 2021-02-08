package org.openhab.binding.sonoff.internal.listeners;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface RFListener {

    void sensorTriggered(String date);

    void buttonPressed(Integer button, String date);
}
