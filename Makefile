.PHONY: docs test help
.DEFAULT_GOAL := help

SHELL := /bin/bash

export ROOTDIR:=$(shell pwd)
export CURRENT_USER:=$(shell id -u ${USER}):$(shell id -g ${USER})

export CURRENT_VERSION:=$(shell mvn --batch-mode --no-transfer-progress help:evaluate     -Dexpression=project.version -DforceStdout -q )




define PRINT_HELP_PYSCRIPT
import re, sys
print("You can run the following targets (with make <target>): \r\n")
for line in sys.stdin:
	match = re.match(r'^([a-zA-Z_-]+):.*?## (.*)$$', line)
	if match:
		target, help = match.groups()
		print("%-20s %s" % (target, help))
endef
export PRINT_HELP_PYSCRIPT

help:
	@python3 -c "$$PRINT_HELP_PYSCRIPT" < $(MAKEFILE_LIST)

version: ## version
	@echo $(CURRENT_VERSION)

release: ## release new version to nexus (git stuff included). https://maven.apache.org/maven-release/maven-release-plugin/index.html
	mvn --no-transfer-progress clean formatter:format release:prepare release:perform -Drevision=$(CURRENT_VERSION)

clean: ## clean previous builds
	mvn --no-transfer-progress clean
	rm -rf target

package: ## package component
	mvn --no-transfer-progress clean formatter:format package -Drevision=$(CURRENT_VERSION)

deploy: ## deploy the component to nexus
	mvn --no-transfer-progress clean formatter:format deploy -Drevision=$(CURRENT_VERSION)

format: ## format
	mvn --no-transfer-progress formatter:format

lint: ## lint-> format and validate
	mvn --no-transfer-progress formatter:validate

test: ## tests
	mvn --no-transfer-progress clean jacoco:prepare-agent test jacoco:report

docker-dev-up: ## start the environment
	mkdir -p docker/dev/deployments
	docker-compose -f docker/dev/docker-compose.yml up -d

docker-dev-down: ## stop the environment
	docker-compose -f docker/dev/docker-compose.yml down

docker-dev-stop: ## stop the environment
	docker-compose -f docker/dev/docker-compose.yml stop

stop: docker-dev-down
