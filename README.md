# XNAT Custom Form Event Plugin

Publishes XNAT Event Service events after custom form data changes on supported XNAT data items in XNAT 1.10.x. It registers distinct subscription-visible triggers for the Event Service basetypes:

- `Custom Form (Auto QC)`
- `Custom Form (CR Session)`
- `Custom Form (CT Session)`
- `Custom Form (Manual QC)`
- `Custom Form (MR Session)`
- `Custom Form (PET MR Session)`
- `Custom Form (PET Session)`
- `Custom Form (Project)`
- `Custom Form (Protocol Validation)`
- `Custom Form (ROI Collection)`
- `Custom Form (Subject)`
- `Custom Form (XSync Assessor Data)`

The XSync trigger accepts the generic `xsync:assessorData` type when provided by an installed XSync extension; the model is not part of stock XNAT.

Only changed JSON object values under UUID-shaped form IDs create events; unchanged values and ordinary custom variables do not.

## Event payload

Each event's filterable payload retains the serialized XNAT object fields and adds a `customForms` array identifying the forms changed by the save. Each entry has a `uid` and, when the form definition is available, its `name` (the Form.io title). For example:

```json
{
  "id": "XNAT_E05998",
  "xsiType": "xnat:mrSessionData",
  "customForms": [
    {
      "uid": "81902a73-23b2-4bfb-a5aa-779997fae683",
      "name": "scan-map-mapped_sessions"
    }
  ]
}
```

Use these fields in an Event Payload Filter to select events by form UUID or name:

```text
("81902a73-23b2-4bfb-a5aa-779997fae683" in @.customForms[*].uid)
```

```text
("scan-map-mapped_sessions" in @.customForms[*].name)
```

The event instance also exposes `changedForms` to Java consumers: a map from form ID to the resulting JSON data, with removed forms represented by JSON `null`. This map is separate from the filterable JSON payload. Events are triggered through XNAT's Event Service and appear as subscription triggers after the plugin is installed and XNAT is restarted.

## Build

Build the XNAT plugin archive with Java 21 or newer (required by XNAT 1.10.x artifacts):

```sh
./gradlew xnatPluginJar
```

Install the generated `build/libs/*-xpl.jar` into the XNAT plugins directory and restart XNAT. Build against the same XNAT 1.10.x version used by the target installation. The custom-form event triggers should then be available in the Event Service subscription UI.

## GitHub releases

Push a version tag such as `v1.0.0` to build and attach a versioned `*-xpl.jar` to a GitHub release. The `Release plugin` workflow can also be run manually for an existing tag from the Actions tab.
