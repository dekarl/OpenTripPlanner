package org.opentripplanner.standalone.config.updaters;

import org.opentripplanner.standalone.config.NodeAdapter;
import org.opentripplanner.updater.bike_rental.BikeRentalUpdater;

public class BikeRentalUpdaterConfig extends PollingGraphUpdaterConfig
    implements BikeRentalUpdater.Config {

  private final String network;
  private final String networks;
  private final String apiKey;

  public BikeRentalUpdaterConfig(NodeAdapter c) {
    super(c);
    network = c.asText("network", null);
    networks = c.asText("networks", null);
    apiKey = c.asText("apiKey", null);
  }

  public String getNetwork() { return network; }

  public String getNetworks() { return networks; }

  public String getApiKey() { return apiKey; }
}
