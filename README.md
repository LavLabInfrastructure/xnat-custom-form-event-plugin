# XNAT Custom Form Event Plugin

Publishes XNAT Event Service events after custom form data changes on supported XNAT data items in XNAT 1.10.x. It registers distinct subscription-visible triggers for the Event Service basetypes:

- `Custom Form (Auto QC) - Modified`
- `Custom Form (CR Session) - Modified`
- `Custom Form (CT Session) - Modified`
- `Custom Form (Manual QC) - Modified`
- `Custom Form (MR Session) - Modified`
- `Custom Form (PET MR Session) - Modified`
- `Custom Form (PET Session) - Modified`
- `Custom Form (Project) - Modified`
- `Custom Form (Protocol Validation) - Modified`
- `Custom Form (ROI Collection) - Modified`
- `Custom Form (Subject) - Modified`
- `Custom Form (XSync Assessor Data) - Modified`

The XSync trigger accepts the generic `xsync:assessorData` type when provided by an installed XSync extension; the model is not part of stock XNAT.

Only changed JSON object values under UUID-shaped form IDs create events; unchanged values and ordinary custom variables do not.

## Event payload

Each event carries the XNAT object, submitting user, project context, and `changedForms`: a map from form ID to the resulting JSON data. Removed forms are represented by a JSON `null` value. Events are triggered through XNAT's Event Service and appear as subscription triggers after the plugin is installed and XNAT is restarted.

## Build

Build the XNAT plugin archive with Java 21 or newer (required by XNAT 1.10.x artifacts):

```sh
./gradlew xnatPluginJar
```

Install the generated `build/libs/*-xpl.jar` into the XNAT plugins directory and restart XNAT. Build against the same XNAT 1.10.x version used by the target installation. The custom-form event triggers should then be available in the Event Service subscription UI.

## GitHub releases

Push a version tag such as `v1.0.0` to build and attach a versioned `*-xpl.jar` to a GitHub release. The `Release plugin` workflow can also be run manually for an existing tag from the Actions tab.
