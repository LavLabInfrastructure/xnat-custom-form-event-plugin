package org.nrg.xnat.customformevent;

import com.fasterxml.jackson.databind.JsonNode;
import org.nrg.xnat.eventservice.events.AbstractEventServiceEvent;
import org.nrg.xft.security.UserI;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class CustomFormModifiedEvent<T> extends AbstractEventServiceEvent<T> {
    private static final long serialVersionUID = 1L;

    protected CustomFormModifiedEvent() {
        super();
        eventObject = null;
        changedForms = Collections.emptyMap();
    }

    protected CustomFormModifiedEvent(final T item, final String user, final String projectId,
                                     final String xsiType, final String payloadId,
                                     final Map<String, JsonNode> changedForms) {
        super(item, user, Status.MODIFIED, projectId, xsiType);
        this.eventObject = item;
        this.payloadId = payloadId;
        this.changedForms = Collections.unmodifiableMap(new LinkedHashMap<>(changedForms));
    }

    private final T eventObject;
    private final Map<String, JsonNode> changedForms;

    @Override
    public final T getObject(final UserI user) {
        return eventObject;
    }

    @Override
    public final Boolean isPayloadXsiType() {
        return Boolean.TRUE;
    }

    @Override
    public final List<String> getStatiStates() {
        return Collections.singletonList(Status.MODIFIED.name());
    }

    public final Map<String, JsonNode> getChangedForms() {
        return changedForms;
    }

    private enum Status {
        MODIFIED
    }
}

