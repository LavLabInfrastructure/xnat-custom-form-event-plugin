package org.nrg.xnat.customformevent;

import com.fasterxml.jackson.databind.JsonNode;
import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatExperimentdataI;

import java.util.Map;

@XnatEventServiceEvent(name = "CustomFormExperimentModified")
public final class ExperimentCustomFormModifiedEvent extends CustomFormModifiedEvent<XnatExperimentdataI> {
    private static final long serialVersionUID = 1L;
    private static final String DISPLAY_NAME = "Custom Form (Experiment) - Modified";

    public ExperimentCustomFormModifiedEvent() {
        super();
    }

    public ExperimentCustomFormModifiedEvent(final XnatExperimentdataI experiment, final String username,
                                             final Map<String, JsonNode> changedForms) {
        super(experiment, username, experiment.getProject(), experiment.getXSIType(),
                experiment.getId(), changedForms);
    }

    @Override public String getDisplayName() { return DISPLAY_NAME; }
    @Override public String getDescription() { return "Custom form data on an experiment was modified."; }
}
