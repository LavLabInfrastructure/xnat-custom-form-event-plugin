package org.nrg.xnat.customformevent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.nrg.xdat.model.XnatExperimentdataI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatProjectdataI;
import org.nrg.xdat.model.XnatSubjectdataI;
import org.aspectj.lang.ProceedingJoinPoint;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.services.EventService;
import org.nrg.xnat.services.customfields.CustomFieldService;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThat;

class CustomFormEventPublisherTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @org.springframework.context.annotation.Configuration
    @org.springframework.context.annotation.EnableAspectJAutoProxy(proxyTargetClass = true)
    static class AopTestConfiguration {
        @org.springframework.context.annotation.Bean
        EventService eventService() {
            return mock(EventService.class);
        }

        @org.springframework.context.annotation.Bean
        CustomFieldService customFieldService() {
            return new TestCustomFieldService();
        }

        @org.springframework.context.annotation.Bean
        CustomFormEventPublisher publisher(EventService eventService, CustomFieldService customFieldService) {
            return new CustomFormEventPublisher(eventService, customFieldService);
        }
    }

    public static class TestCustomFieldService implements CustomFieldService {
        private JsonNode fields = new ObjectMapper().createObjectNode();

        public TestCustomFieldService() {
        }

        @Override
        public JsonNode getFields(ItemI item) {
            return fields;
        }

        @Override
        public JsonNode getFields(ItemI item, java.util.List<String> fieldNames) {
            return fields;
        }

        @Override
        public JsonNode getFieldValue(ItemI item, String fieldName) {
            return fields.get(fieldName);
        }

        @Override
        public JsonNode setFields(UserI user, ItemI item, JsonNode newFields) {
            fields = newFields.deepCopy();
            return fields;
        }

        @Override
        public JsonNode removeField(UserI user, ItemI item, String fieldName) {
            return fields;
        }
    }

    private static XnatSubjectdataI subjectItem() {
        return (XnatSubjectdataI) java.lang.reflect.Proxy.newProxyInstance(
                XnatSubjectdataI.class.getClassLoader(), new Class<?>[]{XnatSubjectdataI.class, ItemI.class},
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "getId" -> "subject";
                    case "getProject" -> "project";
                    case "getXSIType" -> "xnat:subjectData";
                    default -> null;
                });
    }

    @Test
    void springAopInterceptionRunsAfterSetFields() throws Exception {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AopTestConfiguration.class)) {
            final CustomFieldService service = context.getBean(CustomFieldService.class);
            final EventService eventService = context.getBean(EventService.class);
            assertThat(AopUtils.isAopProxy(service)).isTrue();

            final XnatSubjectdataI item = subjectItem();
            final UserI user = (UserI) java.lang.reflect.Proxy.newProxyInstance(
                    UserI.class.getClassLoader(), new Class<?>[]{UserI.class},
                    (proxy, method, arguments) -> "getUsername".equals(method.getName()) ? "user" : null);
            final JsonNode fields = mapper.readTree("""
                    {"12345678-1234-1234-1234-123456789abc":{"field":"new"}}
                    """);

            service.setFields(user, (ItemI) item, fields);

            verify(eventService).triggerEvent(org.mockito.ArgumentMatchers.any());
        }
    }

    @Test
    void publishesEventAfterCustomFieldServiceSave() throws Throwable {
        final EventService eventService = mock(EventService.class);
        final CustomFieldService customFieldService = mock(CustomFieldService.class);
        final CustomFormEventPublisher publisher = new CustomFormEventPublisher(eventService, customFieldService);
        final XnatSubjectdataI item = subjectItem();
        final JsonNode before = mapper.readTree("""
                {"12345678-1234-1234-1234-123456789abc":{"field":"old"}}
                """);
        final JsonNode after = mapper.readTree("""
                {"12345678-1234-1234-1234-123456789abc":{"field":"new"}}
                """);
        final UserI user = (UserI) java.lang.reflect.Proxy.newProxyInstance(
                UserI.class.getClassLoader(), new Class<?>[]{UserI.class},
                (proxy, method, arguments) -> "getUsername".equals(method.getName()) ? "user" : null);
        final ProceedingJoinPoint saveCall = mock(ProceedingJoinPoint.class);
        when(saveCall.getArgs()).thenReturn(new Object[]{user, item, after});
        when(customFieldService.getFields((ItemI) item)).thenReturn(before);
        when(saveCall.proceed()).thenReturn(after);

        publisher.publishAfterCustomFieldsSave(saveCall);

        verify(eventService).triggerEvent(org.mockito.ArgumentMatchers.argThat(event ->
                "Custom Form (Subject) - Modified".equals(event.getDisplayName()) &&
                        after.get("12345678-1234-1234-1234-123456789abc").equals(
                                ((CustomFormModifiedEvent<?>) event).getChangedForms()
                                        .get("12345678-1234-1234-1234-123456789abc"))));
    }

    @Test
    void doesNotPublishEventForUnchangedCustomFieldServiceSave() throws Throwable {
        final EventService eventService = mock(EventService.class);
        final CustomFieldService customFieldService = mock(CustomFieldService.class);
        final CustomFormEventPublisher publisher = new CustomFormEventPublisher(eventService, customFieldService);
        final UserI user = (UserI) java.lang.reflect.Proxy.newProxyInstance(
                UserI.class.getClassLoader(), new Class<?>[]{UserI.class},
                (proxy, method, arguments) -> "getUsername".equals(method.getName()) ? "user" : null);
        final XnatSubjectdataI item = subjectItem();
        final JsonNode fields = mapper.readTree("""
                {"12345678-1234-1234-1234-123456789abc":{"field":"same"}}
                """);
        final ProceedingJoinPoint saveCall = mock(ProceedingJoinPoint.class);
        when(saveCall.getArgs()).thenReturn(new Object[]{user, item, fields});
        when(customFieldService.getFields((ItemI) item)).thenReturn(fields);
        when(saveCall.proceed()).thenReturn(fields);

        publisher.publishAfterCustomFieldsSave(saveCall);

        verify(eventService, org.mockito.Mockito.never()).triggerEvent(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void reportsChangedFormValues() throws Exception {
        final JsonNode oldFields = mapper.readTree("{\"12345678-1234-1234-1234-123456789abc\":{\"field\":\"old\"}}");
        final JsonNode newFields = mapper.readTree("{\"12345678-1234-1234-1234-123456789abc\":{\"field\":\"new\"}}");

        final Map<String, JsonNode> changes = CustomFormEventPublisher.findChangedForms(oldFields, newFields);

        assertThat(changes).containsEntry("12345678-1234-1234-1234-123456789abc",
                mapper.readTree("{\"field\":\"new\"}"));
    }

    @Test
    void ignoresUnchangedForms() throws Exception {
        final JsonNode fields = mapper.readTree("{\"12345678-1234-1234-1234-123456789abc\":{\"field\":\"same\"}}");

        assertThat(CustomFormEventPublisher.findChangedForms(fields, fields)).isEmpty();
    }

    @Test
    void ignoresNonFormCustomValues() throws Exception {
        final JsonNode oldFields = mapper.readTree("{\"other-key\":{\"field\":\"old\"}}");
        final JsonNode newFields = mapper.readTree("{\"other-key\":{\"field\":\"new\"}}");

        assertThat(CustomFormEventPublisher.findChangedForms(oldFields, newFields)).isEmpty();
    }

    @Test
    void createsDistinctSubscriptionEventsForEachSupportedBasetype() {
        final Map<String, JsonNode> changedForms = Map.of("form", mapper.createObjectNode());
        final XnatProjectdataI project = mock(XnatProjectdataI.class);
        final XnatSubjectdataI subject = mock(XnatSubjectdataI.class);
        final XnatExperimentdataI experiment = mock(XnatExperimentdataI.class);
        final XnatImagescandataI scan = mock(XnatImagescandataI.class);
        when(project.getId()).thenReturn("project");
        when(project.getXSIType()).thenReturn("xnat:projectData");
        when(subject.getId()).thenReturn("subject");
        when(subject.getProject()).thenReturn("project");
        when(subject.getXSIType()).thenReturn("xnat:subjectData");
        when(experiment.getId()).thenReturn("experiment");
        when(experiment.getProject()).thenReturn("project");
        when(experiment.getXSIType()).thenReturn("xnat:mrSessionData");
        when(scan.getId()).thenReturn("scan");
        when(scan.getXnatImagescandataId()).thenReturn(42);
        when(scan.getProject()).thenReturn("project");
        when(scan.getXSIType()).thenReturn("xnat:mrScanData");

        final EventServiceEvent<?> projectEvent = CustomFormModifiedEventFactory.create(project, "user", changedForms);
        final EventServiceEvent<?> subjectEvent = CustomFormModifiedEventFactory.create(subject, "user", changedForms);
        final EventServiceEvent<?> experimentEvent = CustomFormModifiedEventFactory.create(experiment, "user", changedForms);
        final EventServiceEvent<?> scanEvent = CustomFormModifiedEventFactory.create(scan, "user", changedForms);

        assertThat(projectEvent.getDisplayName()).isEqualTo("Custom Form (Project) - Modified");
        assertThat(subjectEvent.getDisplayName()).isEqualTo("Custom Form (Subject) - Modified");
        assertThat(experimentEvent.getDisplayName()).isEqualTo("Custom Form (MR Session) - Modified");
        assertThat(scanEvent.getDisplayName()).isEqualTo("Custom Form (Scan) - Modified");
        assertThat(projectEvent.getPayloadId()).isEqualTo("project");
        assertThat(subjectEvent.getPayloadId()).isEqualTo("subject");
        assertThat(experimentEvent.getPayloadId()).isEqualTo("experiment");
        assertThat(scanEvent.getPayloadId()).isEqualTo("42");
    }

    @Test
    void createsDistinctEventsForEveryDropdownBasetype() {
        final Map<String, JsonNode> changedForms = Map.of("form", mapper.createObjectNode());
        final XnatExperimentdataI experiment = mock(XnatExperimentdataI.class);
        final XnatProjectdataI project = mock(XnatProjectdataI.class);
        final XnatSubjectdataI subject = mock(XnatSubjectdataI.class);
        when(experiment.getProject()).thenReturn("project");
        when(experiment.getId()).thenReturn("item");
        when(project.getId()).thenReturn("project");
        when(subject.getId()).thenReturn("subject");

        final Map<String, String> typesAndNames = Map.ofEntries(
                Map.entry("xnat:qcAssessmentData", "Custom Form (Auto QC) - Modified"),
                Map.entry("xnat:crSessionData", "Custom Form (CR Session) - Modified"),
                Map.entry("xnat:ctSessionData", "Custom Form (CT Session) - Modified"),
                Map.entry("xnat:qcManualAssessorData", "Custom Form (Manual QC) - Modified"),
                Map.entry("xnat:mrSessionData", "Custom Form (MR Session) - Modified"),
                Map.entry("xnat:petMRSessionData", "Custom Form (PET MR Session) - Modified"),
                Map.entry("xnat:petSessionData", "Custom Form (PET Session) - Modified"),
                Map.entry("xnat:projectData", "Custom Form (Project) - Modified"),
                Map.entry("val:protocolData", "Custom Form (Protocol Validation) - Modified"),
                Map.entry("icr:roiCollectionData", "Custom Form (ROI Collection) - Modified"),
                Map.entry("xnat:subjectData", "Custom Form (Subject) - Modified"),
                Map.entry("xsync:assessorData", "Custom Form (XSync Assessor Data) - Modified"));

        typesAndNames.forEach((xsiType, displayName) -> {
            final Object item;
            if (xsiType.equals("xnat:projectData")) {
                item = project;
            } else if (xsiType.equals("xnat:subjectData")) {
                item = subject;
            } else {
                when(experiment.getXSIType()).thenReturn(xsiType);
                item = experiment;
            }
            final EventServiceEvent<?> event = CustomFormModifiedEventFactory.create(item, "user", changedForms);
            assertThat(event.getDisplayName()).isEqualTo(displayName);
        });
    }

    @Test
    void reportsAddedAndRemovedForms() throws Exception {
        final JsonNode fields = mapper.readTree("{\"12345678-1234-1234-1234-123456789abc\":{\"field\":\"value\"}}");
        final Map<String, JsonNode> added = CustomFormEventPublisher.findChangedForms(null, fields);
        final Map<String, JsonNode> removed = CustomFormEventPublisher.findChangedForms(fields, null);

        assertThat(added).containsEntry("12345678-1234-1234-1234-123456789abc",
                mapper.readTree("{\"field\":\"value\"}"));
        assertThat(removed).containsKey("12345678-1234-1234-1234-123456789abc");
        assertThat(removed.get("12345678-1234-1234-1234-123456789abc").isNull()).isTrue();
    }
}
