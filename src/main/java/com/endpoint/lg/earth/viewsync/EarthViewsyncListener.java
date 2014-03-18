package com.endpoint.lg.earth.viewsync;

import interactivespaces.service.comm.network.client.UdpBroadcastClientNetworkCommunicationEndpoint;
import interactivespaces.service.comm.network.client.UdpBroadcastClientNetworkCommunicationEndpointListener;

import java.net.InetSocketAddress;

import com.endpoint.lg.support.viewsync.EarthViewSyncState;
import com.google.common.eventbus.EventBus;

/**
 * A UDP listener for sniffing and deserializing Earth viewsync datagrams.
 * 
 * <p>
 * Information on the Earth viewsync format:
 * https://code.google.com/p/liquid-galaxy/wiki/GoogleEarth_ViewSync
 * 
 * @author Matt Vollrath <matt@endpoint.com>
 * @author Wojciech Ziniewicz <wojtek@endpoint.com>
 */
public class EarthViewsyncListener implements
    UdpBroadcastClientNetworkCommunicationEndpointListener {

  private EventBus eventBus;

  public EarthViewsyncListener(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  public void onUdpMessage(UdpBroadcastClientNetworkCommunicationEndpoint endpoint, byte[] message,
      InetSocketAddress remoteAddress) {

    String viewSyncData = new String(message);

    eventBus.post(new EarthViewSyncState(viewSyncData));
  }
}
