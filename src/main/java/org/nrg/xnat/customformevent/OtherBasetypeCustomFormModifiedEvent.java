package org.nrg.xnat.customformevent;

import com.fasterxml.jackson.databind.JsonNode;
import org.nrg.framework.event.XnatEventServiceEvent;
import java.util.Map;

abstract class OtherBasetypeCustomFormModifiedEvent<T> extends CustomFormModifiedEvent<T> {
    private static final long serialVersionUID = 1L;

    protected OtherBasetypeCustomFormModifiedEvent() {
        super();
    }

    protected OtherBasetypeCustomFormModifiedEvent(final T item, final String username, final String projectId,
                                                   final String xsiType, final String payloadId,
                                                   final Map<String, JsonNode> changedForms) {
        super(item, username, projectId, xsiType, payloadId, changedForms);
    }
}
