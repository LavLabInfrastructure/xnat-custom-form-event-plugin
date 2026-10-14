package org.nrg.xnat.customformevent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.nrg.framework.annotations.XnatPlugin;
import org.nrg.xnat.customforms.service.CustomVariableFormService;
import org.nrg.xnat.eventservice.services.EventService;
import org.nrg.xnat.services.customfields.CustomFieldService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(proxyTargetClass = true)
@XnatPlugin(value = "CustomFormEvent", name = "Custom Form Modification Events",
        description = "Publishes an XNAT event when custom form data is changed", version = "1.0.0")
public class CustomFormEventPlugin {
    @Bean
    public CustomFormEventPublisher customFormEventPublisher(final EventService eventService,
                                                               final CustomFieldService customFieldService,
                                                               final ObjectMapper objectMapper,
                                                               final CustomVariableFormService customVariableFormService) {
        return new CustomFormEventPublisher(eventService, customFieldService, objectMapper, customVariableFormService);
    }
}
