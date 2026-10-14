package org.nrg.xnat.customformevent;

import java.util.Map;

import org.nrg.framework.event.XnatEventServiceEvent;

import com.fasterxml.jackson.databind.JsonNode;

@XnatEventServiceEvent(name = "CustomFormPetMrSessionModified")
public final class PetMrSessionCustomFormModifiedEvent extends OtherBasetypeCustomFormModifiedEvent<Object> {

    private static final long serialVersionUID = 1L;

    public PetMrSessionCustomFormModifiedEvent() {
        super();
    }

    public PetMrSessionCustomFormModifiedEvent(final Object item, final String username, final String projectId, final String xsiType, final String payloadId, final Map<String, JsonNode> changedForms) {
        super(item, username, projectId, xsiType, payloadId, changedForms);
    }

    @Override
    public String getDisplayName() {
        return "Custom Form (PET MR Session)";
    }

    @Override
    public String getDescription() {
        return "Custom form data on PET MR Session was modified.";
    }
}
