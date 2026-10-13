package org.nrg.xnat.customformevent;

import com.fasterxml.jackson.databind.JsonNode;
import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatImagescandataI;

import java.util.Map;

@XnatEventServiceEvent(name = "CustomFormScanModified")
public final class ScanCustomFormModifiedEvent extends CustomFormModifiedEvent<XnatImagescandataI> {
    private static final long serialVersionUID = 1L;
    private static final String DISPLAY_NAME = "Custom Form (Scan) - Modified";

    public ScanCustomFormModifiedEvent() {
        super();
    }

    public ScanCustomFormModifiedEvent(final XnatImagescandataI scan, final String username,
                                       final Map<String, JsonNode> changedForms) {
        super(scan, username, scan.getProject(), scan.getXSIType(),
                scan.getXnatImagescandataId() == null ? scan.getId() : scan.getXnatImagescandataId().toString(),
                changedForms);
    }

    @Override public String getDisplayName() { return DISPLAY_NAME; }
    @Override public String getDescription() { return "Custom form data on a scan was modified."; }
}
