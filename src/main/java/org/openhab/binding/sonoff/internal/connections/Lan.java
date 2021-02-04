package org.openhab.binding.sonoff.internal.connections;

import java.io.IOException;
import java.net.InetAddress;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

import org.openhab.binding.sonoff.internal.listeners.ConnectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Lan {
    private final Logger logger = LoggerFactory.getLogger(Lan.class);
    // private final Map<String, ServiceListener> listeners = new HashMap<>();
    private JmDNS mdns;
    private final String ipaddress;
    private final ConnectionListener listener;
    private ServiceListener serviceListener;

    public Lan(String ipaddress, ConnectionListener listener) {
        this.ipaddress = ipaddress;
        this.listener = listener;
    }

    public void start() {
        try {
            mdns = JmDNS.create(InetAddress.getByName(ipaddress));
            serviceListener = new MDnsListener();
            mdns.addServiceListener("_ewelink._tcp.local.", serviceListener);
            listener.lanConnectionOpen();
        } catch (IOException e) {
            listener.onError("lan", e.getCause().toString(), e.getMessage());
        }
    }

    public void removeListener(ServiceListener listener) {
        mdns.removeServiceListener("_ewelink._tcp.local.", listener);
    }

    public void stop() {
        logger.debug("Sonoff - Stopping LAN connection");
        if (mdns != null) {
            mdns.removeServiceListener("_ewelink._tcp.local.", serviceListener);
            try {
                mdns.close();
            } catch (IOException e) {
                listener.onError("lan", e.getCause().toString(), e.getMessage());
            }
        }
    }

    public class MDnsListener implements ServiceListener {
        @Override
        public void serviceAdded(ServiceEvent event) {
            listener.onLanMessage(event.getInfo());
        }

        @Override
        public void serviceRemoved(ServiceEvent event) {
        }

        @Override
        public void serviceResolved(ServiceEvent event) {
            listener.onLanMessage(event.getInfo());
        }
    }
}
