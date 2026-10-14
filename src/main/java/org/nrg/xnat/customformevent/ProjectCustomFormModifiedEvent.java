package org.nrg.xnat.customformevent;

import java.util.Map;

import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatProjectdataI;

import com.fasterxml.jackson.databind.JsonNode;

@XnatEventServiceEvent(name = "CustomFormProjectModified")
public final class ProjectCustomFormModifiedEvent extends CustomFormModifiedEvent<XnatProjectdataI> {

    private static final long serialVersionUID = 1L;
    private static final String DISPLAY_NAME = "Custom Form (Project)";

    public ProjectCustomFormModifiedEvent() {
        super();
    }

    public ProjectCustomFormModifiedEvent(final XnatProjectdataI project, final String username,
            final Map<String, JsonNode> changedForms) {
        super(project, username, project.getId(), project.getXSIType(), project.getId(), changedForms);
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public String getDescription() {
        return "Custom form data on a project was modified.";
    }
}
