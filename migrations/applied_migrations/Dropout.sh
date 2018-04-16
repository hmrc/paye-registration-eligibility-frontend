#!/bin/bash

echo "Applying migration Dropout"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /dropout                       controllers.DropoutController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "dropout.title = dropout" >> ../conf/messages.en
echo "dropout.heading = dropout" >> ../conf/messages.en

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration Dropout completed"
