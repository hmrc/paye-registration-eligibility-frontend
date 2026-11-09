#!/usr/bin/env bash
sbt -Dfrontend -Dfeature.userResearchBanner=true -Dapplication.router=testOnlyDoNotUseInAppConf.Routes "run 9877"
