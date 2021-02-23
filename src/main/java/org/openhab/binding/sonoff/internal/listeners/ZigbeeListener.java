package org.openhab.binding.sonoff.internal.listeners;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface ZigbeeListener {

    void sensorTriggered(String date);
}
