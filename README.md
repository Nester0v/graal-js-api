```

Project starts by entrypoint -> GraalJsApiApplication.java

*******************
*Project structure*
*******************

graaljs-rest-api/
├── src
│   └── main
│       └── java
│           └── com
│               └── interview_task
│                   └── graal_js_api
│                       ├── GraalJsRestApiApplication.java
│                       ├── controller
│                       │   └── ScriptController.java
│                       ├── model
│                       │   └── Script.java
│                       └── service
│                           ├── ScriptService.java
│                           └── ScriptServiceImpl.java
│       └── resources
│           └── application.properties
├── build.gradle

***************************
*CURL commands for POSTman*
***************************

Execute a script (non-blocking):
curl -X POST -d 'console.log("Hello, World!");' http://localhost:8080/scripts

Execute a script (blocking):
curl -X POST -d 'console.log("Hello, World!");' "http://localhost:8080/scripts?blocking=true"

List scripts:
curl -X GET http://localhost:8080/scripts

Get script details:
curl -X GET http://localhost:8080/scripts/{id}

Stop a script:
curl -X POST http://localhost:8080/scripts/{id}/stop

Remove a script:
curl -X DELETE http://localhost:8080/scripts/{id}

Documentation is embedded into the code
