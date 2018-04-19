#!/bin/bash

echo "Applying migration OffshoreEmployer"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /offshoreEmployer                       controllers.OffshoreEmployerController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /offshoreEmployer                       controllers.OffshoreEmployerController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeOffshoreEmployer                       controllers.OffshoreEmployerController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeOffshoreEmployer                       controllers.OffshoreEmployerController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "offshoreEmployer.title = offshoreEmployer" >> ../conf/messages.en
echo "offshoreEmployer.heading = offshoreEmployer" >> ../conf/messages.en
echo "offshoreEmployer.checkYourAnswersLabel = offshoreEmployer" >> ../conf/messages.en
echo "offshoreEmployer.error.required = Please give an answer for offshoreEmployer" >> ../conf/messages.en

echo "Adding helper line into UserAnswers"
awk '/class/ {\
     print;\
     print "  def offshoreEmployer: Option[Boolean] = cacheMap.getEntry[Boolean](OffshoreEmployerId.toString)";\
     print "";\
     next }1' ../app/utils/UserAnswers.scala > tmp && mv tmp ../app/utils/UserAnswers.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def offshoreEmployer: Option[AnswerRow] = userAnswers.offshoreEmployer map {";\
     print "    x => AnswerRow(\"offshoreEmployer.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, routes.OffshoreEmployerController.onPageLoad(CheckMode).url)"; print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration OffshoreEmployer completed"
