#!/usr/bin/make -f

.DEFAULT_GOAL := build
.PHONY: help scan clean build doc install release rebase push git

# https://www.thapaliya.com/en/writings/well-documented-makefiles/
help: ## Shows this help including list of targets
	@awk 'BEGIN {FS = ":.*##"; printf "Usage: make \033[36m<target>\033[0m\n\nTargets:\n"} /^[a-zA-Z0-9_-]+:.*?##/ { printf "  \033[36m%-10s\033[0m %s\n", $$1, $$2 }' $(MAKEFILE_LIST)

rebase: ## Updates git database and pulls all missing commits for current git branch
	@git fetch --all --prune
	@git pull --rebase

push: ## Pushes current git branch incl. git tags
	@git push --follow-tags

git: rebase push ## Executes target 'rebase' followed by target 'push'

clean: ## Runs maven clean
	@mvn -o clean

scan: ## Scans for plugin/property updates
	@mvn -U versions:display-plugin-updates versions:display-property-updates

build: ## Runs maven build (no install, no deploy)
	@mvn -U clean verify

doc: ## Runs maven build incl. site goal (no install, no deploy)
	@mvn -U clean verify site

install: ## Runs maven install (into local .m2 folder) (no deploy)
	@mvn clean install

release: ## Runs maven OSSRH deploy incl. GPG signing
	@mvn --settings oss-deploy-settings.xml \
	     clean \
	     build-helper:parse-version \
	     release:prepare -Dresume=false \
	                     -Dtag=$$\{parsedVersion.majorVersion\}.$$\{parsedVersion.minorVersion\}.$$\{parsedVersion.incrementalVersion\} \
	                     -DreleaseVersion=$$\{parsedVersion.majorVersion\}.$$\{parsedVersion.minorVersion\}.$$\{parsedVersion.incrementalVersion\} \
	                     -DpreparationGoals="clean deploy" \
	                     -Darguments="-DupdateReleaseInfo=true -Dgpg.keyname=FA327F13415EDECE3165321539C57878EC1B23C5" \
	                     -DdevelopmentVersion=$$\{parsedVersion.majorVersion\}.$$\{parsedVersion.minorVersion\}.$$\{parsedVersion.nextIncrementalVersion\}-SNAPSHOT \
	                     -DpushChanges=false \
	     release:clean

