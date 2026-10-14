package org.nrg.xnat.customformevent;

import com.fasterxml.jackson.databind.JsonNode;
import org.nrg.xnat.eventservice.events.AbstractEventServiceEvent;
import org.nrg.xft.XFTItem;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class CustomFormModifiedEvent<T> extends AbstractEventServiceEvent<T> {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomFormModifiedEvent.class);
    private transient T eventObject;

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

    private final Map<String, JsonNode> changedForms;

    @Override
    @SuppressWarnings("unchecked")
    public final T getObject(final UserI user) {
        if (eventObject != null) {
            return eventObject;
        }
        if (xsiType == null || payloadId == null) {
            LOGGER.warn("Cannot reload custom form event payload without an XSI type and payload ID");
            return null;
        }
        try {
            final XFTItem item = ItemSearch.GetItem(xsiType, payloadId, user, false);
            if (item == null) {
                LOGGER.warn("No XNAT item found for custom form event payload {} ({})", payloadId, xsiType);
                return null;
            }
            return (T) org.nrg.xdat.base.BaseElement.GetGeneratedItem(item);
        } catch (final Exception exception) {
            LOGGER.warn("Unable to reload custom form event payload {} ({})", payloadId, xsiType, exception);
            return null;
        }
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

