package controllers

import controllers.actions._;format="decap"$

class $className$ControllerSpec extends ControllerSpecBase {

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyCacheMap) =
    new $className$Controller(frontendAppConfig, messagesApi, FakeAuthAction,
      dataRetrievalAction, new DataRequiredAction)

  def viewAsString() = $className;format="decap"$(frontendAppConfig)(fakeRequest, messages).toString

  "$className$ Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }
  }
}




