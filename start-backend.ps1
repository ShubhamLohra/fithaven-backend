$JAVA_HOME = "C:\Users\Shubham\java-tools\jdk21"
$MAVEN_HOME = "C:\Users\Shubham\java-tools\maven"

if (Test-Path "$JAVA_HOME\bin\java.exe") {
    $env:JAVA_HOME = $JAVA_HOME
    $env:PATH = "$JAVA_HOME\bin;$MAVEN_HOME\bin;$env:PATH"
}

Write-Host "Starting FitHaven Spring Boot Backend..."
mvn compile spring-boot:run
