# Queue Service


### Description
This is a basic spring boot application for showcasing intermittent non unique work (delayed_work records) processing.


### Basic structure / configuration
1. The main application build file is: ./build.gradle (obviously Gradle is the build tool here)
  This file contains basic information about the application as well as dependencies (mainly Spring Boot and Java).

2. The main application configuration file is: src.main.resources.application.yml
    1. Default time zone set to UTC
    2. DB configurations: url, username and password. You will need to use values that matches your local DB.
    3. Logging Level configurations: I have set the level of logging for anything under io.kungfury.coworker to WARN to reduce the "noise".

3. The coworker specific configuration file is: com.rss.queue.config.CoworkerConfiguration
    We use this class to create the spring beans that are necessary to integrate with Coworker.

4. The main application file is: com.rss.queue.QueueApplication
     1. We use this class to actually build/insert records into the delayed_work table and then starting the Coworker Manager to process those records.
     2. The NUMBER_OF_JOBS variable controls how many records we want to create in the delayed_work table. This is currently set to 50.

5. The sample job class is: com.rss.queue.job.EchoJob
  This is our sample BackgroundJavaWork which simply echo's a message and then finishes it's work.


### Running the application (clean, build, run)
1. Clone the project locally.
2. cd into the parent directory.
3. To clean the project execute command: ./gradlew clean
4. To build the project execute command: ./gradlew build
5. To run the project execute command: ./gradlew bootRun

Once you run the application, search the logs for the string "Running com.rss.queue.job.EchoJob with state" and you will notice that sometimes, this is intermittent and doesn't happen all the time, but you will notice that sometimes the same state (record) is processed more than once. Again, this is intermittent. So you might have to run the application a bunch of times before you encounter this scenario.

Important Notes:
   1. Before running the application, make sure your tables exist and your application.yml is using correct DB configurations (url, username, password).
   2. Because the call to start the Coworker Manager takes over the main thread, you will have to kill the process manually after you're done with it.
   3. Included screenshot "./LogsScreenshot.png" shows logs with this issue. Record with state 09 was processed twice.

