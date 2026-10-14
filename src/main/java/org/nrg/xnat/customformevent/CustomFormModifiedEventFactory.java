package org.nrg.xnat.customformevent;

import com.fasterxml.jackson.databind.JsonNode;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.model.XnatExperimentdataI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatProjectdataI;
import org.nrg.xdat.model.XnatSubjectdataI;
import org.nrg.xft.ItemI;
import java.util.Locale;
import java.util.Map;

final class CustomFormModifiedEventFactory {
    private CustomFormModifiedEventFactory() {
    }

    static CustomFormModifiedEvent<?> create(final Object item, final String username,
                                              final Map<String, JsonNode> changedForms) {
        final Object eventItem = item instanceof ItemI xnatItem
                ? BaseElement.GetGeneratedItem(xnatItem)
                : item;
        if (eventItem instanceof XnatProjectdataI project) {
            return new ProjectCustomFormModifiedEvent(project, username, changedForms);
        }
        if (eventItem instanceof XnatSubjectdataI subject) {
            return new SubjectCustomFormModifiedEvent(subject, username, changedForms);
        }
        if (eventItem instanceof XnatExperimentdataI experiment) {
            return createExperimentEvent(experiment, username, changedForms);
        }
        if (eventItem instanceof XnatImagescandataI scan) {
            return new ScanCustomFormModifiedEvent(scan, username, changedForms);
        }
        if (eventItem instanceof ItemI xnatItem) {
            return createTypedEvent(xnatItem, username, changedForms);
        }
        throw new IllegalArgumentException("Unsupported custom-field item type: " +
                (eventItem == null ? "null" : eventItem.getClass().getName()));
    }

    static CustomFormModifiedEvent<?> create(final Object item, final String username,
                                              final Map<String, JsonNode> changedForms,
                                              final JsonNode customForms) {
        final CustomFormModifiedEvent<?> event = create(item, username, changedForms);
        event.setCustomForms(customForms);
        return event;
    }

    private static CustomFormModifiedEvent<?> createExperimentEvent(final XnatExperimentdataI experiment,
                                                                      final String username,
                                                                      final Map<String, JsonNode> changedForms) {
        final String xsiType = experiment.getXSIType();
        if (xsiType == null) {
            return new ExperimentCustomFormModifiedEvent(experiment, username, changedForms);
        }
        final String normalized = xsiType.substring(xsiType.indexOf(':') + 1).toLowerCase(Locale.ROOT);
        final String projectId = experiment.getProject();
        final String payloadId = experiment.getId();
        return switch (normalized) {
            case "crsessiondata" -> new CrSessionCustomFormModifiedEvent(experiment, username, projectId, xsiType, payloadId, changedForms);
            case "ctsessiondata" -> new CtSessionCustomFormModifiedEvent(experiment, username, projectId, xsiType, payloadId, changedForms);
            case "mrsessiondata" -> new MrSessionCustomFormModifiedEvent(experiment, username, projectId, xsiType, payloadId, changedForms);
            case "petmrsessiondata" -> new PetMrSessionCustomFormModifiedEvent(experiment, username, projectId, xsiType, payloadId, changedForms);
            case "petsessiondata" -> new PetSessionCustomFormModifiedEvent(experiment, username, projectId, xsiType, payloadId, changedForms);
            case "qcassessmentdata" -> new AutoQcCustomFormModifiedEvent(experiment, username, projectId, xsiType, payloadId, changedForms);
            case "qcmanualassessordata" -> new ManualQcCustomFormModifiedEvent(experiment, username, projectId, xsiType, payloadId, changedForms);
            case "validationdata", "protocoldata" ->
                    new ProtocolValidationCustomFormModifiedEvent(experiment, username, projectId, xsiType, payloadId, changedForms);
            case "roicollectiondata" -> new RoiCollectionCustomFormModifiedEvent(experiment, username, projectId, xsiType, payloadId, changedForms);
            case "assessordata" -> new XsyncAssessorDataCustomFormModifiedEvent(experiment, username, projectId, xsiType, payloadId, changedForms);
            default -> new ExperimentCustomFormModifiedEvent(experiment, username, changedForms);
        };
    }

    private static String getItemProperty(final ItemI item, final String property) {
        try {
            return item.getStringProperty(property);
        } catch (final Exception exception) {
            throw new IllegalArgumentException("Unable to read " + property + " from " + item.getXSIType(), exception);
        }
    }

    private static CustomFormModifiedEvent<?> createTypedEvent(final ItemI item, final String username,
                                                                 final Map<String, JsonNode> changedForms) {
        final String xsiType = item.getXSIType();
        if (xsiType == null) {
            throw new IllegalArgumentException("Unsupported custom-field item type: " + item.getClass().getName());
        }
        final String normalized = xsiType.substring(xsiType.indexOf(':') + 1).toLowerCase(Locale.ROOT);
        final String projectId = getItemProperty(item, "project");
        final String payloadId = getItemProperty(item, "ID");
        return switch (normalized) {
            case "crsessiondata" -> new CrSessionCustomFormModifiedEvent(item, username, projectId, xsiType, payloadId, changedForms);
            case "ctsessiondata" -> new CtSessionCustomFormModifiedEvent(item, username, projectId, xsiType, payloadId, changedForms);
            case "mrsessiondata" -> new MrSessionCustomFormModifiedEvent(item, username, projectId, xsiType, payloadId, changedForms);
            case "petmrsessiondata" -> new PetMrSessionCustomFormModifiedEvent(item, username, projectId, xsiType, payloadId, changedForms);
            case "petsessiondata" -> new PetSessionCustomFormModifiedEvent(item, username, projectId, xsiType, payloadId, changedForms);
            case "qcassessmentdata" -> new AutoQcCustomFormModifiedEvent(item, username, projectId, xsiType, payloadId, changedForms);
            case "qcmanualassessordata" -> new ManualQcCustomFormModifiedEvent(item, username, projectId, xsiType, payloadId, changedForms);
            case "validationdata", "protocoldata" -> new ProtocolValidationCustomFormModifiedEvent(item, username, projectId, xsiType, payloadId, changedForms);
            case "roicollectiondata" -> new RoiCollectionCustomFormModifiedEvent(item, username, projectId, xsiType, payloadId, changedForms);
            case "assessordata" -> new XsyncAssessorDataCustomFormModifiedEvent(item, username, projectId, xsiType, payloadId, changedForms);
            default -> throw new IllegalArgumentException("Unsupported custom-field XSI type: " + xsiType);
        };
    }
}
