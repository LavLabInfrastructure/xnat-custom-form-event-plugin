package org.nrg.xnat.customformevent;

import com.fasterxml.jackson.databind.JsonNode;
import org.nrg.framework.event.EventI;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CustomFormModifiedEvent implements EventI {
    private static final long serialVersionUID = 1L;

    private final String itemId;
    private final String itemType;
    private final String username;
    private final Map<String, JsonNode> changedForms;

    public CustomFormModifiedEvent(final String itemId, final String itemType, final String username,
                                   final Map<String, JsonNode> changedForms) {
        this.itemId = itemId;
        this.itemType = itemType;
        this.username = username;
        this.changedForms = Collections.unmodifiableMap(new LinkedHashMap<>(changedForms));
    }

    public String getItemId() {
        return itemId;
    }

    public String getItemType() {
        return itemType;
    }

    public String getUsername() {
        return username;
    }

    public Map<String, JsonNode> getChangedForms() {
        return changedForms;
    }
}
