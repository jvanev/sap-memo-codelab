# SAP Memo - An Android app for storing memos

## Build

Use the latest Android Studio version to build this app

A Google Maps API key must be added to your `local.properties`.

Example `local.properties`:
```properties
sdk.dir=...

# Google Maps API Key
GOOGLE_MAPS_API_KEY=AIzaYourGoogleMapsAPIKey
```

> **Note:** Gradle build will fail if the API key is not specified

## The application

- Stores memos with title, description, and location on the map
- Sends notifications when the device is nearby a memo (200m radius)
- Uses geofences in order to monitor the device's location over time,
  even if the app is not currently running
