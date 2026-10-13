package org.nrg.xnat.customformevent;

import org.nrg.framework.annotations.XnatPlugin;
import org.nrg.xnat.eventservice.services.EventService;
import org.nrg.xnat.services.customfields.CustomFieldService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@XnatPlugin(value = "CustomFormEvent", name = "Custom Form Modification Events",
        description = "Publishes an XNAT event when custom form data is changed", version = "1.0.0")
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class CustomFormEventPlugin {
    @Bean
    public CustomFormEventPublisher customFormEventPublisher(final EventService eventService,
                                                               final CustomFieldService customFieldService) {
        return new CustomFormEventPublisher(eventService, customFieldService);
    }
}

