package org.nrg.xnat.customformevent;

import java.util.Map;

import org.nrg.framework.event.XnatEventServiceEvent;

import com.fasterxml.jackson.databind.JsonNode;

@XnatEventServiceEvent(name = "CustomFormXsyncAssessorDataModified")
public final class XsyncAssessorDataCustomFormModifiedEvent extends OtherBasetypeCustomFormModifiedEvent<Object> {

    private static final long serialVersionUID = 1L;

    public XsyncAssessorDataCustomFormModifiedEvent() {
        super();
    }

    public XsyncAssessorDataCustomFormModifiedEvent(final Object item, final String username, final String projectId, final String xsiType, final String payloadId, final Map<String, JsonNode> changedForms) {
        super(item, username, projectId, xsiType, payloadId, changedForms);
    }

    @Override
    public String getDisplayName() {
        return "Custom Form (XSync Assessor Data)";
    }

    @Override
    public String getDescription() {
        return "Custom form data on XSync Assessor Data was modified.";
    }
}
