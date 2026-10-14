package org.nrg.xnat.customformevent;

import java.util.Map;

import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatSubjectdataI;

import com.fasterxml.jackson.databind.JsonNode;

@XnatEventServiceEvent(name = "CustomFormSubjectModified")
public final class SubjectCustomFormModifiedEvent extends CustomFormModifiedEvent<XnatSubjectdataI> {

    private static final long serialVersionUID = 1L;
    private static final String DISPLAY_NAME = "Custom Form (Subject)";

    public SubjectCustomFormModifiedEvent() {
        super();
    }

    public SubjectCustomFormModifiedEvent(final XnatSubjectdataI subject, final String username,
            final Map<String, JsonNode> changedForms) {
        super(subject, username, subject.getProject(), subject.getXSIType(), subject.getId(), changedForms);
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public String getDescription() {
        return "Custom form data on a subject was modified.";
    }
}
