#!/bin/bash

echo "Applying migration AtLeastOneDirectorHasNino"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /atLeastOneDirectorHasNino                       controllers.AtLeastOneDirectorHasNinoController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /atLeastOneDirectorHasNino                       controllers.AtLeastOneDirectorHasNinoController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeAtLeastOneDirectorHasNino                       controllers.AtLeastOneDirectorHasNinoController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeAtLeastOneDirectorHasNino                       controllers.AtLeastOneDirectorHasNinoController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "atLeastOneDirectorHasNino.title = atLeastOneDirectorHasNino" >> ../conf/messages.en
echo "atLeastOneDirectorHasNino.heading = atLeastOneDirectorHasNino" >> ../conf/messages.en
echo "atLeastOneDirectorHasNino.checkYourAnswersLabel = atLeastOneDirectorHasNino" >> ../conf/messages.en
echo "atLeastOneDirectorHasNino.error.required = Please give an answer for atLeastOneDirectorHasNino" >> ../conf/messages.en

echo "Adding helper line into UserAnswers"
awk '/class/ {\
     print;\
     print "  def atLeastOneDirectorHasNino: Option[Boolean] = cacheMap.getEntry[Boolean](AtLeastOneDirectorHasNinoId.toString)";\
     print "";\
     next }1' ../app/utils/UserAnswers.scala > tmp && mv tmp ../app/utils/UserAnswers.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def atLeastOneDirectorHasNino: Option[AnswerRow] = userAnswers.atLeastOneDirectorHasNino map {";\
     print "    x => AnswerRow(\"atLeastOneDirectorHasNino.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, routes.AtLeastOneDirectorHasNinoController.onPageLoad(CheckMode).url)"; print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration AtLeastOneDirectorHasNino completed"
