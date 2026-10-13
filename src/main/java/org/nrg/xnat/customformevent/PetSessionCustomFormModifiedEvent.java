package org.nrg.xnat.customformevent;

import com.fasterxml.jackson.databind.JsonNode;
import org.nrg.framework.event.XnatEventServiceEvent;
import java.util.Map;

@XnatEventServiceEvent(name = "CustomFormPetSessionModified")
public final class PetSessionCustomFormModifiedEvent extends OtherBasetypeCustomFormModifiedEvent<Object> {
    private static final long serialVersionUID = 1L;
    public PetSessionCustomFormModifiedEvent() { super(); }
    public PetSessionCustomFormModifiedEvent(final Object item, final String username, final String projectId, final String xsiType, final String payloadId, final Map<String, JsonNode> changedForms) { super(item, username, projectId, xsiType, payloadId, changedForms); }
    @Override public String getDisplayName() { return "Custom Form (PET Session) - Modified"; }
    @Override public String getDescription() { return "Custom form data on PET Session was modified."; }
}
