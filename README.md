# XNAT Custom Form Event Plugin

Publishes a typed XNAT event after custom form field data is saved or removed on a project, subject, experiment, or scan in XNAT 1.10.x. The event is emitted only when a object under a UUID-like form key actually changes; unchanged updates, non-form custom variables, and legacy custom variables do not generate it.

## Event contract

The Reactor-backed XNAT event bus publishes `org.nrg.xnat.customformevent.CustomFormModifiedEvent` with:

- `itemId`: ID of the XNAT item whose custom fields changed
- `itemType`: XSI type for that item
- `username`: user who made the change
- `changedForms`: map from custom-field/form ID to its resulting JSON data; removed entries have a JSON `null` value

A plugin listener can implement `Consumer<Event<CustomFormModifiedEvent>>` and use XNAT's `@EventListener` annotation to subscribe through its event wiring service. The event fires after a successful save, so listeners see persisted changes.

## Build

Build the XNAT plugin archive with Java 21 or newer (required by XNAT 1.10.x artifacts):

```sh
./gradlew xnatPluginJar
```

Install the generated `build/libs/*-xpl.jar` into the XNAT plugins directory and restart XNAT. Build against the same XNAT 1.10.x version used by the target installation.

## GitHub releases

Push a version tag such as `v1.0.0` to build and attach a versioned `*-xpl.jar` to a GitHub release. The `Release plugin` workflow can also be run manually for an existing tag from the Actions tab.
