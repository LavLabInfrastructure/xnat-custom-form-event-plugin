package org.nrg.xnat.customformevent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.eventservice.services.EventService;
import org.nrg.xnat.services.customfields.CustomFieldService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Aspect
public class CustomFormEventPublisher {
    private final EventService eventService;
    private final CustomFieldService customFieldService;

    public CustomFormEventPublisher(final EventService eventService,
                                    final CustomFieldService customFieldService) {
        this.eventService = eventService;
        this.customFieldService = customFieldService;
    }

    @Around("execution(* org.nrg.xft.utils.SaveItemHelper.save(org.nrg.xft.ItemI, org.nrg.xft.security.UserI, boolean, boolean, org.nrg.xft.event.EventMetaI))")
    public Object publishAfterCustomFieldsSave(final ProceedingJoinPoint joinPoint) throws Throwable {
        final Object[] arguments = joinPoint.getArgs();
        final ItemI item = (ItemI) arguments[0];
        final UserI user = (UserI) arguments[1];
        final JsonNode oldFields = getCustomFields(item.getCurrentDBVersion());
        final Object result = joinPoint.proceed();
        final JsonNode newFields = getCustomFields(item);
        final Map<String, JsonNode> changedForms = findChangedForms(oldFields, newFields);

        if (!changedForms.isEmpty()) {
            final Object eventItem = BaseElement.GetGeneratedItem(item);
            eventService.triggerEvent(CustomFormModifiedEventFactory.create(
                    eventItem == null ? item : eventItem, user.getUsername(), changedForms));
        }
        return result;
    }

    private JsonNode getCustomFields(final ItemI item) throws Exception {
        if (item == null) {
            return null;
        }
        final ItemI generatedItem = BaseElement.GetGeneratedItem(item);
        if (generatedItem != null) {
            try {
                return customFieldService.getFields(generatedItem);
            } catch (final UnsupportedOperationException ignored) {
                return null;
            }
        }
        if (item instanceof XFTItem xftItem) {
            try {
                return customFieldService.getFields(xftItem);
            } catch (final UnsupportedOperationException ignored) {
                return null;
            }
        }
        return null;
    }

    static Map<String, JsonNode> findChangedForms(final JsonNode oldFields, final JsonNode newFields) {
        final Map<String, JsonNode> changedForms = new LinkedHashMap<>();
        final Set<String> fieldNames = new TreeSet<>();
        if (oldFields != null && oldFields.isObject()) {
            oldFields.fieldNames().forEachRemaining(fieldNames::add);
        }
        if (newFields != null && newFields.isObject()) {
            newFields.fieldNames().forEachRemaining(fieldNames::add);
        }

        for (final String fieldName : fieldNames) {
            final JsonNode oldValue = oldFields == null ? null : oldFields.get(fieldName);
            final JsonNode newValue = newFields == null ? null : newFields.get(fieldName);
            if (isFormData(fieldName, oldValue) || isFormData(fieldName, newValue)) {
                if (oldValue == null || newValue == null || !oldValue.equals(newValue)) {
                    changedForms.put(fieldName, newValue == null ? NullNode.getInstance() : newValue);
                }
            }
        }
        return changedForms;
    }

    private static boolean isFormData(final String fieldName, final JsonNode value) {
        return value != null && value.isObject() && isUuid(fieldName);
    }

    private static boolean isUuid(final String value) {
        return value != null && value.matches("(?i)[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }
}
