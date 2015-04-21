/*
 * Copyright (C) 2015 End Point Corporation
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

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
