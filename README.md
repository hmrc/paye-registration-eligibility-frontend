# paye-registration-eligibility-frontend

## Running the Application

In order to run the microservice, you must have SBT installed. You should then be able to start the application using:

```sbt "run 9877 -Dapplication.router=testOnlyDoNotUseInAppConf.Routes"```

Then go to: http://localhost:9877/eligibility-for-paye/one-director-national-insurance

To run the tests for the application, you can run: ```sbt test it/test```

or ```sbt coverage test it/test coverageReport```

### License Information

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
