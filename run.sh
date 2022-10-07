sbt validate
sbt -Dfrontend -Dapplication.router=testOnlyDoNotUseInAppConf.Routes "run 9877"
echo "running here http://localhost:9877/eligibility-for-paye"
