.PHONY: build run clean

# TODO: Update the below recipes according to Gradle build configs
# 		because the Maven configs have been completely removed from the project.

build:
	mvn clean package

run: build
	java -jar target/httpserver.jar --debug

clean:
	mvn clean
