package org.opentripplanner.routing.algorithm.raptor.transit.mappers;

import org.opentripplanner.model.Stop;
import org.opentripplanner.routing.algorithm.raptor.transit.TransitLayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This index is temporary to help creating a fixed list of stops (by index), a reverse map
 * of indexes by stop, and to create a list of stop indexes for each trip pattern. It
 * make sure the <code>stops</code> and <code>indexByStop</code> have the same order.
 * <p>
 * Raptor uses an integer index to reference stops. This is not the stop id, but just a
 * sequence number - an index. Hence we don´t care about the order - as long as the order does
 * not change. Raptor reference stops as integers for performance reasons, it never access
 * stops, it does not need to. The returned itineraries from Raptor contain stop indexes, not
 * references to stops, so OTP must maintain the stop index.
 * <p>
 * The scope of instances of this class is limited to the mapping process, the final state is
 * stored in the {@link TransitLayer}.
 */
class StopIndexForRaptor {
    final List<Stop> stops;
    final Map<Stop, Integer> indexByStop = new HashMap<>();

    StopIndexForRaptor(Collection<Stop> stops) {
        this.stops = new ArrayList<>(stops);
        initializeIndexByStop();
    }

    /**
     * Create map between stop and index used by Raptor to stop objects in original graph
     */
    private void initializeIndexByStop() {
        for(int i = 0; i< stops.size(); ++i) {
            indexByStop.put(stops.get(i), i);
        }
    }

    /**
     * Create a list of stop indexes for a given list of stops.
     */
    int[] listStopIndexesForStops(Stop[] stops) {
        int[] stopIndex = new int[stops.length];

        for (int i = 0; i < stops.length; i++) {
            stopIndex[i] = indexByStop.get(stops[i]);
        }
        return stopIndex;
    }
}
