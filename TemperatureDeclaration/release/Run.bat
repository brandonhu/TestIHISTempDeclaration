@echo off
@rem set classpath
set classpath=./IHISTemperatureDeclaration.jar

@rem execute testNG
java -cp %classpath% org.testng.TestNG testng.xml

@rem pause to view the logs
pause