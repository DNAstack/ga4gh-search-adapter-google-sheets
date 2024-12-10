# GA4GH Dataset API over Google Sheets

This project is a stateless service that responds to
[GA4GH Dataset API](https://github.com/ga4gh-discovery/ga4gh-discovery-search/pull/3)
requests by fetching data from Google Sheets via the Google Sheets API. With this service
in place, any Dataset API Client can pull data from Google Sheets worksheets.



# Quick Start

### Prerequisites
- Google API Client ID and Client Secret. (See #<here> for setup instructions.)
- Java 11+


### Build

```
mvn clean package
```


### Configure
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

### Run

```
java -jar ./target/ga4gh-search-adapter-google-sheets-0.0.1-SNAPSHOT.jar
```

Once the application is running, first you have to obtain an OAuth token for use with the Google Sheets API. We're
still thinking about how to make this a seamless experience across many Dataset implementations. In the mean time, you
can enjoy our ad-hoc authentication/authorization scheme:

1. Visit http://localhost:8087/oauth/token
1. Sign in to Google
1. Accept the permissions prompt
1. Copy the entire string that appears in the browser window (about 130 characters beginning with `ya29.`). This is your Sheets auth token.
1. In your shell save the auth token: `sheets_token=<paste string from above>`

Once you have the auth token, try grabbing a sheet as a Dataset:
```
$ curl -H "Authorization: Bearer $sheets_token" http://localhost:8087/dataset/jwoOp4nBQPtOxaL2xYZ4GI-ntZnBii6Z5c2z4z-TGMlg:Sheet1
```

The dataset ID is the ID of a Google Sheet you have access to (get it from the sheet's URL)
followed by a `:` followed by the name of a worksheet within it (the default worksheet name in
Google Sheet is `Sheet1`).

## End-to-end Testing

There is a separate Maven project rooted at [e2e-tests/](e2e-tests) that exercises a running instance of the service.
You can use it to ensure the application is properly deployed and configured to interact with the Google APIs.

### Building the tests

Similar to the main application, you can build and run the E2E tests in your IDE, on the command line,
or using Docker. 

#### In a Java IDE
1. Import the project under `e2e-tests/` as a Maven Project
1. Set up a run configuration for JUnit 4.x
  * Environment Variables: see below

#### Command Line
1. Export environment variables (see below)
1. `./mvnw clean test`

#### Docker
1. `ci/build-docker-image ga4gh-search-adapter-google-sheets:dev ga4gh-search-adapter-google-sheets dev`
1. `docker run -ti --rm -e ENV1=env1 -e ENV2=env2 -p 8087:8087 ga4gh-search-adapter-google-sheets:dev`

### Test environment variables

The end-to-end tests need to be told where your app is deployed, and also need a service account
with read access to the test spreadsheet.

```
E2E_BASE_URI=http://localhost:8087
E2E_GOOGLE_CREDENTIALS_JSON_BASE64=<base64-encoded contents of the service account json credentials file>
```

## Confiugre (Advanced)

### Create a new Google API Client ID
1. Navigate to https://console.cloud.google.com/apis/credentials
1. Make sure you are in the correct GCloud Project
1. Press the "Create Credentials" button and select "OAuth Client ID"
1. Fill in the form:
   * Application type: "Web application"
   * Name: anything you like (for example, "Dataset Adapter - Sheets")
   * Authorized JavaScript origins: all the places where your app will be deployed
   * Authorized redirect URLs: take each origin from the previous step and append `/oauth/code`
   
### Use an existing Google API Client ID
1. Navigate to https://console.cloud.google.com/apis/credentials
1. Make sure you are in the correct GCloud Project
1. Under OAuth 2.0 client IDs, select the existing client you want to use (for
   example, "Dataset Adapter - Sheets")
1. Get the "Client ID" and "Client Secret" values from the table at the top of
   the page
