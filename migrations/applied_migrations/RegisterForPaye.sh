#!/bin/bash

echo "Applying migration RegisterForPaye"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /registerForPaye                       controllers.RegisterForPayeController.onPageLoad" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "registerForPaye.title = registerForPaye" >> ../conf/messages.en
echo "registerForPaye.heading = registerForPaye" >> ../conf/messages.en

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration RegisterForPaye completed"
