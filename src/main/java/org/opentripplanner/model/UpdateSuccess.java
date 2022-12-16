package org.opentripplanner.model;

import java.util.Collection;
import java.util.List;
import org.opentripplanner.framework.collection.ListUtils;

public record UpdateSuccess(List<WarningType> warnings) {
  public static UpdateSuccess noWarnings() {
    return new UpdateSuccess(List.of());
  }

  public UpdateSuccess addWarnings(Collection<WarningType> addedWarnings) {
    return new UpdateSuccess(ListUtils.combine(this.warnings, addedWarnings));
  }

  public enum WarningType {
    UNKNOWN_STOPS_REMOVED_FROM_ADDED_TRIP,
    NOT_MONITORED,
  }
}
