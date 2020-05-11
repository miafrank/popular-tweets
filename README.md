# Setup
### Project setup in Intellij 
1. From the main menu, select File | Open.
   1. Alternatively, click Open or Import on the welcome screen.
1. In the dialog that opens, select the pom.xml file of the project you want to open.
Click **OK**.
1. In the dialog that opens, click Open as Project.

IntelliJ IDEA opens and syncs the Maven project in the IDE. 
If you need to adjust importing options when you open the project, refer to the 
[Maven](https://www.jetbrains.com/help/idea/maven1.importing.html) settings.

### Additional Setup
1. [Confluent Platform](https://www.confluent.io/download/?_ga=2.250927620.387832776.1589147004-1583349907.1588185083) 5.2 or later
1. [Confluent CLI](https://docs.confluent.io/current/cli/installing.html#cli-install)
1. Java 1.8 or 1.11 to run Confluent Platform
    1. MacOS Java 8 installation: 
        ```bash
        brew tap adoptopenjdk/openjdk
        brew cask install adoptopenjdk8
        ```
1. [Maven](https://maven.apache.org/) to compile the client Java code (If using Intellij -- Maven comes bundled in the IDE)

### Twitter Developer Account
Apply for a [Twitter Developer Account](https://developer.twitter.com/en/docs/basics/developer-portal/faq) to receive 
access tokens to use Twitter API. When the tokens are received, keys and tokens can be generated by creating an App in the Twitter
Developer dashboard.

To crate an app:  
    1. `Apps` -> `Create an app` -> Fill out App details form. 
    2. Copy key and tokens into `twitter.properties` file. 