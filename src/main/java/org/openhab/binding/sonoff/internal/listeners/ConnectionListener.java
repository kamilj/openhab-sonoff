package org.openhab.binding.sonoff.internal.listeners;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface ConnectionListener {

    void websocketConnected(Boolean connected);

    void websocketLoggedIn(Boolean loggedIn);

    void lanConnected(Boolean connected);

    void apiConnected(Boolean connected);
}
