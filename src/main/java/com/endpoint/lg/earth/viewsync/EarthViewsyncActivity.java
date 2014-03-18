package com.endpoint.lg.earth.viewsync;

import java.util.Map;

import com.endpoint.lg.support.viewsync.EarthViewSyncState;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import interactivespaces.activity.binary.NativeActivityRunnerFactory;
import interactivespaces.activity.binary.NativeApplicationRunner;
import interactivespaces.activity.impl.ros.BaseRoutableRosActivity;
import interactivespaces.service.comm.network.client.UdpBroadcastClientNetworkCommunicationEndpoint;
import interactivespaces.service.comm.network.client.UdpClientNetworkCommunicationEndpointService;

/**
 * An activity for sniffing Earth's ViewSync datagrams and publishing the
 * current location to a route.
 * 
 * <p>
 * Because the Interactive Spaces UDP listener binds exclusively to a port,
 * socat is used to forward all ViewSync traffic to a configurable local port
 * for eavesdropping.
 * 
 * @author Matt Vollrath <matt@endpoint.com>
 * @author Wojciech Ziniewicz <wojtek@endpoint.com>
 */
public class EarthViewsyncActivity extends BaseRoutableRosActivity {

  /**
   * Configuration key for Earth viewsync port.
   */
  private static final String CONFIG_VIEWSYNC_PORT = "lg.earth.viewSync.port";

  /**
   * Configuration key for viewsync listener port
   */
  private static final String CONFIG_VIEWSYNC_LISTENER_PORT = "lg.earth.viewSync.listenerPort";

  /**
   * Path to the socat binary.
   */
  private static final String CONFIG_NATIVE_SOCAT_PATH = "lg.native.socat.path";

  /**
   * Publishes view changes from the UDP listener.
   * 
   * @param view
   *          the new Earth view state
   */
  @Subscribe
  public void onViewsyncUpdate(EarthViewSyncState view) {
    sendOutputJsonBuilder("view", view.getJsonBuilder());
  }

  @Override
  public void onActivitySetup() {
    EventBus eventBus = new EventBus();
    eventBus.register(this);

    int viewSyncPort = getConfiguration().getRequiredPropertyInteger(CONFIG_VIEWSYNC_PORT);

    int viewSyncListenerPort =
        getConfiguration().getRequiredPropertyInteger(CONFIG_VIEWSYNC_LISTENER_PORT);

    UdpClientNetworkCommunicationEndpointService udpCommService =
        getSpaceEnvironment().getServiceRegistry().getService(
            UdpClientNetworkCommunicationEndpointService.NAME);

    UdpBroadcastClientNetworkCommunicationEndpoint udpBcastClient =
        udpCommService.newBroadcastClient(viewSyncListenerPort, getLog());

    udpBcastClient.addListener(new EarthViewsyncListener(eventBus));

    addManagedResource(udpBcastClient);

    NativeActivityRunnerFactory runnerFactory = getController().getNativeActivityRunnerFactory();
    NativeApplicationRunner socatRunner = runnerFactory.newPlatformNativeActivityRunner(getLog());

    Map<String, Object> socatConfig = Maps.newHashMap();

    String socatPath = getConfiguration().getRequiredPropertyString(CONFIG_NATIVE_SOCAT_PATH);

    String socatFlags =
        String.format("UDP4-RECV:%d,reuseaddr UDP4-DATAGRAM:127.0.0.1:%d", viewSyncPort,
            viewSyncListenerPort);

    socatConfig.put(NativeApplicationRunner.ACTIVITYNAME, socatPath);
    socatConfig.put(NativeApplicationRunner.FLAGS, socatFlags);

    socatRunner.configure(socatConfig);
    addManagedResource(socatRunner);
  }
}
