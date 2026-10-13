package org.nrg.xnat.customformevent;

import com.fasterxml.jackson.databind.JsonNode;
import org.nrg.framework.event.XnatEventServiceEvent;
import java.util.Map;

@XnatEventServiceEvent(name = "CustomFormManualQcModified")
public final class ManualQcCustomFormModifiedEvent extends OtherBasetypeCustomFormModifiedEvent<Object> {
    private static final long serialVersionUID = 1L;
    public ManualQcCustomFormModifiedEvent() { super(); }
    public ManualQcCustomFormModifiedEvent(final Object item, final String username, final String projectId, final String xsiType, final String payloadId, final Map<String, JsonNode> changedForms) { super(item, username, projectId, xsiType, payloadId, changedForms); }
    @Override public String getDisplayName() { return "Custom Form (Manual QC) - Modified"; }
    @Override public String getDescription() { return "Custom form data on Manual QC was modified."; }
}
