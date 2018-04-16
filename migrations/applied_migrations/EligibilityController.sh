#!/bin/bash

echo "Applying migration EligibilityController"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /eligibilityController                       controllers.EligibilityControllerController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /eligibilityController                       controllers.EligibilityControllerController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeEligibilityController                       controllers.EligibilityControllerController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeEligibilityController                       controllers.EligibilityControllerController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "eligibilityController.title = eligibilityController" >> ../conf/messages.en
echo "eligibilityController.heading = eligibilityController" >> ../conf/messages.en
echo "eligibilityController.checkYourAnswersLabel = eligibilityController" >> ../conf/messages.en
echo "eligibilityController.error.required = Please give an answer for eligibilityController" >> ../conf/messages.en

echo "Adding helper line into UserAnswers"
awk '/class/ {\
     print;\
     print "  def eligibilityController: Option[Boolean] = cacheMap.getEntry[Boolean](EligibilityControllerId.toString)";\
     print "";\
     next }1' ../app/utils/UserAnswers.scala > tmp && mv tmp ../app/utils/UserAnswers.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def eligibilityController: Option[AnswerRow] = userAnswers.eligibilityController map {";\
     print "    x => AnswerRow(\"eligibilityController.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, routes.EligibilityControllerController.onPageLoad(CheckMode).url)"; print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration EligibilityController completed"
