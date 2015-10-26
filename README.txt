This project is highly customized selenium test suit application. This may not suite to your own individual applications. But this will be able to use as a reference for overcome technical challenges.

In this application has a main entry point com.iuse.order.FulfillSubmittedOrders.java.

In this application, has demonstrated the use of Selenium on 
Login to a application, validating fields, traverse on tables rows, accessing iFrames and of course enter values on fields and so on.

Below are the set of information and instructions required to build and run the Automation Tool.

Contents:
==========
│   pom.xml
│   ReadME.docx
├───lib
└───src
    ├───main
    │   ├───java
    │   │   └───com
    │   │       └───selenium
    │   │           │   PortalBase.java
    │   │           │
    │   │           ├───order
    │   │           │       FulfillSubmittedOrders.java
    │   │           │
    │   │           └───util
    │   │                   XLSReader.java
    │   │
    │   └───resources
    │           config.properties
    │           log4j.properties
    │           Order.xlsx
    │
    └───test
        ├───java
        │   └───com
        │       └───selenium
        │           │   IuseTest.java
        │           │
        │           ├───order
        │           │       TestFulfillSubmittedOrders.java
        │           │
        │           ├───replacement
        │           │       IuseFulfillReplacementOrder.java
        │           │       UpdateReplacementRequest.java
        │           │
        │           └───util
        │                   TestXLSReader.java
        │
        └───resources
                config.properties
                log4j.properties


Building the Project.
====================

Pre-requisites
--------------
JDK 1.6.x or higher
Maven 2 or higher
set java_home and m2_home.

Steps:
------
1.	Checkout the code to a local folder (eg: Selenium Test)
2.	Build the executable
-	Go to the project home directory / Selenium Test/, and run 
-	mvn clean package
-	This might take a few seconds to download all the dependencies to your local M2 repo.
3.	Prepare the package
-	Create a folder (eg: C:/Selenium)
-	Copy the target \iUse_TestSuite.jar to above Created folder C:/Selenium
-	mvn dependency:copy-dependencies to download dependencies to folder “target\dependency”, then 
  Copy all *.jar files from target\dependency to C:/Selenium/lib folder
-	copy all (except log4j.properties) files from src\main\resources to C:/Selenium/resources/
-	copy log4j.properties file from src\main\resources to C:/Selenium/
Folder structure would look like below
.
│   iUse_TestSuite.jar
│   log4j.properties
│
├───lib
│       bcpkix-jdk15on-1.48.jar
│       bcprov-jdk15on-1.48.jar
│       cglib-nodep-2.1_3.jar
│       ...
│       ...
│       ...
│       xml-apis-1.4.01.jar
│       xmlbeans-2.6.0.jar
│
└───resources
        config.properties
        Order.xlsx 

4.	To Run the Suite
-	Go into 

Running the application.
========================

-	Go to target folder C:/Selenium (above created) and enter the command	
-	Change all configurations as per the site on config.properties

-		Java –jar iUse_TestSuite.jar

Result
======
Observe the automation running on Firefox Web Browser
