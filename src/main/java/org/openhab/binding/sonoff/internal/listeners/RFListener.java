package org.openhab.binding.sonoff.internal.listeners;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface RFListener {

    void rfTriggered(Integer chl, String date);

    void rfCode(Integer chl, String rfVal);
}
