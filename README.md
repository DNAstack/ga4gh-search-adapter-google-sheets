# GA4GH Dataset API over Google Sheets

This project is a stateless service that responds to GA4GH Dataset API requests
by fetching data from Google Sheets via the Google Sheets API. With this service
in place, any Dataset API Client can pull data from Google Sheets worksheets.

## Setup

You need a Google API client ID and client secret. You can either create a new one
or use an existing one.

### Create a new Client ID
1. Navigate to https://console.cloud.google.com/apis/credentials
1. Make sure you are in the correct GCloud Project
1. Press the "Create Credentials" button and select "OAuth Client ID"
1. Fill in the form:
   * Application type: "Web application"
   * Name: anything you like (for example, "Dataset Adapter - Sheets")
   * Authorized JavaScript origins: all the places where your app will be deployed
   * Authorized redirect URLs: take each origin from the previous step and append `/oauth/code`
   
### Use an existing Client ID
1. Navigate to https://console.cloud.google.com/apis/credentials
1. Make sure you are in the correct GCloud Project
1. Under OAuth 2.0 client IDs, select the existing client you want to use (for
   example, "Dataset Adapter - Sheets")
1. Get the "Client ID" and "Client Secret" values from the table at the top of
   the page

## Building and Running

If you have an OpenJDK 11 installed, you can build an executable JAR of the service
directly on your machine. If you have Docker installed, you can build a Docker
image that runs the app. 

### In a Java IDE
1. Import the project as a Maven Project
1. Set up a run configuration
  * Environment Variables: see below
  * Main Class: `com.dnastack.search.sheets.SheetsAdapterApplication`

### Command Line
1. `./mvnw clean package`
1. Export environment variables (see below)
1. Execute the JAR: `java -jar target/ga4gh-search-adapter-google-sheets-0.0.1-SNAPSHOT.jar`

### Docker
1. `ci/build-docker-image ga4gh-search-adapter-google-sheets:dev ga4gh-search-adapter-google-sheets dev`
1. `docker run -ti --rm -e SHEETS_AUTH_CLIENT_SECRET=your-secret -p 8087:8087 ga4gh-search-adapter-google-sheets:dev`

## Environment Variables
All runtime configuration is passed into the app using environment variables.

The following variables, at a minimum, must be set in order for the service to work
```
SHEETS_AUTH_CLIENT_ID=
SHEETS_AUTH_CLIENT_SECRET=
```

Examples of some other variables you might want to tweak at runtime:
```
SERVER_CONTEXTPATH=/
SERVER_PORT=8087
```

In fact, all values specified in [application.yml](src/main/resources/application.yml) can be set via environment variables.
See the [Spring Boot External Configuration Guide](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html) for details.
