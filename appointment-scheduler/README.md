# Appointment scheduler
An appointment scheduling service powered by openai chat assistant model.

## Build service
`./gradlew build`
## Running tests.
ocs tests depends on consul, vault and redis for running functional tests, and runs in isolated environment.

run tests for changes only - `./gradlew tests`

run all tests - `./gradlew tests --rerun-tasks`

## How to run locally
Start the app:
    1. Start the app using gradle `./gradlew start`. it will start the application with profile `dev`
    2. Hit `http://localhost:8081/actuator/health` and verify status !

## build with UI.
`./gradlew clean assemble`
