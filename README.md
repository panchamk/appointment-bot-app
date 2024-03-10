# Appointment booking bot
An appointment scheduling service powered by openai chat assistant model.

## environment.
add `openai_api_key` in the application environment.

## build release artifacts with UI.
`./gradlew clean assemble`

## run service locally.
```
./gradlew buildUI
./gradlew copyUIFiles
./gradlew start
```

## api docs
```
http://localhost:8080/swagger-ui/index.html#/
```

## run UI only
```
./gradlew startUI
```
## deploy service on google app engine.
```
./gradlew appengnineRun
```