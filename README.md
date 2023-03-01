**NOTICE: This project is in [Emeritus status](https://tac.openmainframeproject.org/process/lifecycle.html#emeritus-stage) and no longer maintained**

![](https://github.com/openmainframeproject/artwork/blob/master/projects/polycephaly/polycephaly-color.svg)
![GitHub](https://img.shields.io/github/license/openmainframeproject/polycephaly)

# Polycephaly 
This project can be used to build z/OS application using Jenkins and Git, with DBB, from any IDE.

## Installation
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

## Contributing
Anyone can contribute to the Polycephaly project - learn more at [CONTRIBUTING.md](Documentation/CONTRIBUTING.md)



## Governance
Polycephaly is a project hosted by the [Open Mainframe Project](https://openmainframeproject.org). This project has established it's own processes for managing day-to-day processes in the project at [GOVERNANCE.md](Documentation/GOVERNANCE.md).

## Reporting Issues
To report a problem, you can open an [issue](https://github.com/openmainframeproject/polycephaly/issues) in repository against a specific workflow. If the issue is senstive in nature or a security related issue, please do not report in the issue tracker but instead email polycephaly-private@lists.openmainframeproject.org.

## More Information
A presentation is available at [Presentation](Documentation/Polycephaly-OpenMainframeProject.pptx).
