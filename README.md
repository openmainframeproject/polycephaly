# zJenkins
Groovy code to build z/OS source code files with Jenkins and Git

Requires;
- IBM JZOS Toolkit Library, which can be downloaded from IBM Developer https://developer.ibm.com/mainframe/products/downloads/
    - After downloading and installing IBM Aqua for Eclipse, add library "IBM JZOS Toolkit Library'
    - That should resolve any missing com.ibm.jzos.** classes
- Add Groovy libraries
- Add IBM DBB libaries, to developer and run, these routines require IBM DBB APIs.  

ToDo:
- Add documentation
- Complete SDFGenUtility, only one step setup
- JCLCheck fails because the Java environment is not authorized. Need to switch to a different method


    
