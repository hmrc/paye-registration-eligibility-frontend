#!/bin/bash

echo "Applying migration TaxedAwardScheme"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /taxedAwardScheme                       controllers.TaxedAwardSchemeController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /taxedAwardScheme                       controllers.TaxedAwardSchemeController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeTaxedAwardScheme                       controllers.TaxedAwardSchemeController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeTaxedAwardScheme                       controllers.TaxedAwardSchemeController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "taxedAwardScheme.title = taxedAwardScheme" >> ../conf/messages.en
echo "taxedAwardScheme.heading = taxedAwardScheme" >> ../conf/messages.en
echo "taxedAwardScheme.checkYourAnswersLabel = taxedAwardScheme" >> ../conf/messages.en
echo "taxedAwardScheme.error.required = Please give an answer for taxedAwardScheme" >> ../conf/messages.en

echo "Adding helper line into UserAnswers"
awk '/class/ {\
     print;\
     print "  def taxedAwardScheme: Option[Boolean] = cacheMap.getEntry[Boolean](TaxedAwardSchemeId.toString)";\
     print "";\
     next }1' ../app/utils/UserAnswers.scala > tmp && mv tmp ../app/utils/UserAnswers.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def taxedAwardScheme: Option[AnswerRow] = userAnswers.taxedAwardScheme map {";\
     print "    x => AnswerRow(\"taxedAwardScheme.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, routes.TaxedAwardSchemeController.onPageLoad(CheckMode).url)"; print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration TaxedAwardScheme completed"
