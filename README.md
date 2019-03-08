# zJenkins
Groovy code to build z/OS source code files with Jenkins and Git

Requires;
- IBM JZOS Toolkit Library, which can be downloaded from IBM Developer https://developer.ibm.com/mainframe/products/downloads/
    - After downloading and installing IBM Aqua for Eclipse
    - Add library "IBM JZOS Toolkit Library"
        ibmjzos.jar
    
- Add IBM DBB libaries, to developer and run, these routines require IBM DBB APIs.
    dbb.core_1.0.0.jar
    groovy-2.4.12.jar
    
 - Add JUnit 4 libraries
    junit.jar
    org.hamcrest.core_1.3.0.v201303031735.jar
    
- Add Apache commons-cli-2.0.jar
- Add jre.1.8.0_191



    


See Wiki for more information, including installation, migration and customization 

ToDo:
- Add documentation
- Complete SDFGenUtility, only one step setup
- JCLCheck fails because the Java environment is not authorized. Need to switch to a different method
