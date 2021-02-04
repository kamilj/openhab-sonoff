package org.openhab.binding.sonoff.internal.listeners;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonoff.internal.dto.api.Device;

@NonNullByDefault
public interface ConnectionListener {

    void onError(String module, @Nullable String code, @Nullable String message);

    void ApiconnectionOpen();

    // void onApiMessage(String message);

    void lanConnectionOpen();

    void onLanMessage(ServiceInfo info);

    void webSocketConnectionOpen();

    void webSocketMessage(Device device);
}
