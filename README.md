# zJenkins
Groovy code to build z/OS source code files with Jenkins and Git

Requires;
- IBM JZOS Toolkit Library, which can be downloaded from IBM Developer https://developer.ibm.com/mainframe/products/downloads/
    - After downloading and installing IBM Aqua for Eclipse, add library "IBM JZOS Toolkit Library'
    - That should resolve any missing com.ibm.jzos.** classes
- Add Groovy libraries
- Add IBM DBB libaries

ToDo:
- Add documentation
- Complete SDFGenUtility, only one step setup
- JCLCheck fails because the Java environment is not authorized. Need to switch to a different method

Property Files:
o	build.properties
    	Used to define and load the other properties. Zconstants.BUILDPROPS
    	Encoding		= UTF-8				 
    	DB2BindProps		= DB2Bind.properties		
    	FileProps		= FileTypeMap.properties	
    	LinkEditScannerProps	= LinkEditScanner.properties
    	SystemProps		= System.properties
    	GlobalAntProps		= GlobalAnt.properties
o	DB2Bind.properties
    	As defined by IBM, unchanged
o	GlobalAnt.properties
       Properties used in Ant and Groovy scripts
    	src.zOS.dir		= zOS source files
    	src.groovy.dir		= Groovy source files
    	src.java.dir		= Java source files
    	test.java.dir		= unit source files
    	zos.asm			= package name for Assembler source
    	zos.bms		= package name for BMS maps
    	zos.cics			= package name for the CICS source code
    	zos.cicsapi		= package name for the CICS JSON code
    	zos.cobol		= package name for Cobol code
    	zos.copybook		= package name for Copybook code
    	zos.ezt			= package name for Eztrieve code
    	zos.jcl			= package name for JCL
    	zos.link			= package name for LinkEdit control cards
    	zos.maclib		= package name for application MACLIBs
    	zos.parmlib		= package name for Parmlib members
    	zos.sql			= package name for SQL control cards
    	conf.dir			= configuration directory name
    	bin.dir			= binary files directory name
    	dist.dir			= distribution directory name
    	3rdparty.dir		= directory location for 3rd party software, like Junit
    	dbb.dir			= directory path to DBB runtime ./lib
    	dbb.groovy.dir		= directory path to DBB’s Groovy runtime ./lib
    	zJenkins.lib.dir		= directory path for zJenkins jar file
    	zJenkins.conf.dir	= directory path for zJenkins configuration files
    	appDev.bin.dir		= AppDev binary path
    	appDev.conf.dir		= AppDev configuration path
    	appDev.lib.dir		= AppDev library path
    	appDev.web.dir		= AppDev web modules path
o	LinkEditScanner.properties
    	As defined by IBM, unchanged
o	System.properties
    	scriptSrcDir		= source directory for groovy routines
    	scriptSrcPackage	= package name for the groovy routines
    	BuildList		= relative dire/filename of the source files to build
    	loadOptions		= DCB information used to create load PDS
    	srcOptions		= DCB information used to create source PDS
    	buildOrder		= Order in which groovy routines will be called
    	AssemblerSrcFiles	= Source datasets for Assembler routine
    	AssemblerLoadFiles	= Load datasets for Assembler routine
    	BMSsrcFiles		= Source datasets for BMS routine
    	BMSloadFiles		= Load datasets BMS routine
    	CobolCompileSrcFiles	= Source datasets for CobolCompiler routine
    	CobolCompilerLoadFiles	= Load datasets for CobolCompiler routine
    	CobolSrcFiles		= Source datasets for Cobol Compile only routine
    	CopybookSrcFiles	= Source datasets for Copybook routine
    	EztrieveSrcFiles		= Source datasets for Eztrieve routine
    	EztrieveLoadFiles	= Load datasets for Eztrieve routine
    	JCLcheckSrcFiles	= Source datasets for JCLcheck routine
    	LinkEditSrcFiles		= Source datasets for LinkEdit routine
    	LinkEditLoadFiles	= Load datasets for LinkEdit routine
    	MFSSrcFiles		= Source datasets for MFSGEN utility 
    	MFSLoadFiles		= Load datasets for MFSGEN utility
    	DualCompileDeleteLineBuildOnline	= character in col 7 for Online program
    	DualCompileDeleteLineBuildBatch	= character in col 7 for Batch program
    	DualCompileOnlineSuffix		= Program name suffix for Online
    	DualCompileBatchSuffix			= Program name suffix for Batch
    	DefaultAssemblerCompileOpts		= Default Assembler options
    	DefaultBMScopybookOpts		= Default BMS copybook Gen options
    	DefaultCobolCompileOpts		= Default Cobol Compiler options
    	DefaultcicsCSDOpts			= Default parms for DFHCSDUP 
    	DefaultLinkEditOpts			= Default Linkage Edit options
    	DefaultXpediterCompileOpts		= Default Xpediter compiler options
    	bmsSuffix		= fileType for BMS map source
    	cicsAPIsuffix		= fileType for CICS RESTful source
    	cicsWSsuffix		= fileType for CICS SOAP source
    	cobolSuffix		= fileType for Cobol source
    	copybookSuffix		= fileType for Cobol Copybook source
    	dbrmSuffix		= fileType for DBRM source
    	eztrieveSuffix		= fileType for Eztrieve source
    	linkSuffix		= fileType for LinkEdit control cards
    	mfsSuffix		= fileType for MFSGEN source
    	parmSuffix		= fileType for parmlib source
    	xmlPDSsuffix		= LLQ for XML datasets
    	BUILD_MFS		= Boolean to run MFSGEN routine, defaults to false
    	XPED_BUILD_PARMS	= Boolean to run Xpediter Build parms, defaults to true
    	XPED_DELDEF_PARMS	= Boolean to delete/define DDIO files, defaults to true
    	logEncoding		= output encoding for logfiles
    	logFileSuffix		= fileType for logfiles
    	ddbID			= UserID to connect to DBB server
    	ddbpwFile		= file with encoding password to connect DBB server
    	dbbRepo		= URL for DBB server
    	datasetPrefix		= default z/OS dataset HLQ
    	dfdssDataset		= DFDSS parm1
    	dfdssAllData		= DFDSS parm2
    	dfdssToEnqf		= DFDSS parm3
    	dfdssRenameU		= DFDSS parm4
    	dfdssCatalog		= DFDSS parm5
    	iebcopyCmd		= Input parm for IEBCOPY to copy non-load members
    	iebcopyModCmd	= input parm for IEBCOPY to copy load members
    	idcamsDelete		= 
o	SystemLibs.properties
    	scriptSrcDir		= folder name for groovy source
    	scriptSrcPackage	= Package name for the groovy source
    	BuildList		= file name and location of the source files to build
    	loadOptions		= allocation parms for Load type PDSs
    	buildOrder		= Build order of the source files
    	AssemblerSrcFiles	= Assembler routine source PDSs to create
    	AssmeblerLoadFiles	= Assembler routine load PDSs to create
    	BMSsrcFiles		= BMS map source PDSs to create
    	BMSloadFiles		= BMS map load PDSs to create
    	CobolCompileSrcFiles	= Cobol routine source PDSs to create
    	CobolCompileLoadFiles	= Cobol routine load PDSs to create
    	CobolSrcFiles		= Cobol compile only source PDSs to create
    	CopybookSrcFiles	= Copybook routine source PDSs to create
    	EasytrieveSrcFiles	= Easytrieve routine source PDSs to create
    	EasytrieveLoadFiles	= Easytrieve routine load PDSs to create
    	JCLcheckSrcFiles	= JCLcheck routine source PDSs to create
    	LinkEditSrcFiles		= Linkedit routine source PDSs to create
    	LinkEditLoadFiles	= Linkedit routine load PDSs to create
    	MFSSrcFiles		= IMS MFS routine source PDSs to create
    	MFSLoadFiles		= IMS MFS routine load PDSs to create
    	SDFSrcFiles		= Screen Definition Facility source PDSs to create
    	SDFLoadFiles		= Screen Definition Facility load PDSs to create
    	DualCompileDeleteLineBuildOnline
    	DualCompileDeleteLineBuildBatch
    	DualCompileOnlineSuffix
    	DualCompileBatchSuffix
        •	Dual Compile routine to split Cobol source into batch and online
    	DefaultAssemblerCompileOpts	= Default Assembler Options
    	DefaultBMScopybookGenOpts	= Default BMS map copybook Gen Options
    	DefaultCobolCompileOpts	= Default Cobol Compiler Options
    	DefaultcicsCSDOpts		= Default CICS CSD input Options
    	DefaultEasytrieveOpts		= Default Easytrieve compile Options
    	DefaultLinkEditOpts		= Default Linkedit Options
    	DefaultMFSOpts		= Default IMS MFS options
    	DefaultXpediterCompilerOpts	= Default Xpediter Compiler Options
    	bmsSuffix			= Default BMS map source suffix
    	cicsAPIsuffix			= Default CICS API conf suffix
    	cicsWSsuffix			= Default CICS WebService conf suffix
    	cobolSuffix			= Default Cobol source suffix
    	copybookSuffix			= Default Copybook source suffix
    	dbrmSuffix			= Default DBRM source suffix
    	easytrieveSuffix			= Default Easytrieve source suffix
    	linkSuffix			= Default Linkedit source suffix
    	mfsSuffix			= Default IMS MFS source suffix
    	parmSuffix			= Default Parmlib source suffix
    	xmlPDSsuffix			= IBM IDz XML source suffix
    	BUILD_MFS			= Boolean to turn off/on MFS generate
    	XPED_BUILD_FALSE		= Boolean to build Xpediter input parms
    	XPED_DELDEF_DDIO		= Boolean to build Xpediter DDIO file
    	logEncoding			= logfile encoding 
    	logFileSuffix			= Logfile suffix 
    	ddbID				= UserID to logon to DBB server
    	dbbpwFile			= Name of password for UserID to logon
    	dbbRepo			= URL of DBB server
    	datasetPrefix			= Default HLQ for PDSs to create
    	dfdssDataset			= DFDSS input cards
    	dfdssAllData			= DFDSS input cards
    	dfdssTolEnqF			= DFDSS input cards
    	dfdssRenameU			= DFDSS input cards
    	dfdssCatalog			= DFDSS input cards
    	iebcopyCmd			= IEBCOPY input parm command
    	iebcopyModCmd		= IEBCOPY copymod input parm command
    	idcamsDelete			= IDCAMS input cards
    	idcamsStartBracket		= IDCAMS input cards
    	idcamsEndBracket		= IDCAMS input cards
    	LineContinue			= IDCAMS input cards
    	idcamsSetMaxRC		= IDCAMS input cards
    	idcamsVerify			= IDCAMS input cards
    	xpedDDIOspace			= Default space allocation for DDIO file
    	xpedDDIODefineStart		= Xpediter DDIO idcams input cards
    	xpedDDIOCiSize			= Xpediter DDIO idcams input cards
    	xpedDDIOrecSize		= Xpediter DDIO idcams input cards
    	xpedDDIOVsamOpts		= Xpediter DDIO idcams input cards
    	xpedDDIOp1			= Xpediter format input card
    	xpedDDIOp2			= Xpediter format input card
    	xpedCompp1			= Xpediter input parm card
    	xpedCompp2			= Xpediter input parm card
    	xpedCompp3			= Xpediter input parm card
    	xpedCompp4			= Xpediter input parm card
    	lrecl80				= Dataset allocation using LRECL=80
    	lrecl121				= Dataset allocation using LRECL=121
    	lrec133				= Dataset allocation using LRECL=133
    	lrecl400				= Dataset allocation using LRECL=400
    	recfmFB			= Dataset allocation using RECFM(FB)
    	recfmFBA			= Dataset allocation using RECFM(FBA)
    	recfmFBM			= Dataset allocation using RECFM(FBM)
    	tempCreateOptions		= Dataset temporary allocation, LRECL(80)
    	tempCreateOptions2		= Dataset temporary allocation, no lrecl/recfm
    	tempPDSCreateOptions		= PDS temporary allocation, LRECL(80)
    	tempUssDiskOptions		= USS DFDSS copy using temporary disk
    	tempUssTapeOptions		= USS DFDSS copy using temporary tape
    	xmlOptions			= Dataset allocations for XML files
    	eztvmOptions			= Temporary allocation for EZTVM file
    	sysprintDatasetAllocation	= Temporary dataset allocation for SYSPRINT
    	sysutDDOptions			= Space allocation for SYSUTx DDs
    	adrdssuProgram		= Name of the ADRDSSU program
    	asmProgram			= Name of the Assembler program
    	cicsCSDProgram		= Name of the CICS CSD program
    	cobolCompiler			= Name of the Cobol compiler
    	easytrieveProgram		= Name of the Easytrieve compiler
    	linkEditProgram			= Name of the Linkeditor program
    	jclCheckProgram		= Name of the JCL Check program
    	ickdsfProgram			= Name of the ICKDSF program
    	idcamsProgram			= Name of the IDCAMS program
    	iebcopyProgram		= Name of the IEBCOPY program
    	MFSProgram			= IMS MFS utility program
    	ISPFbatchProgram		= Name of the ISPF batch program
    	xpediterMainProgram		= Xpediter Compiler program
    	xpediterUtilProgram		= Xpediter format utility program
    	CAIPARM			= CA JCL Check parmlib name
    	DFHCSD			= CICS CSD file
    	EDCMSGS			= 
    	EDCHKDD			= WSLOPTNS
    	MACLIB				= Name of SYS1.MACLIB
    	SYS1LINKLIB			= Name of SYS1.LINKLIB
    	SASMMOD1			= PDS name of Assembler programs
    	SDFHCOB			= PDS name of CICS Cobol copybooks
    	SDFHLOAD			= PDS name of CICS loadlib
    	SDFHMAC			= PDS name of CICS macros
    	SIGYCOMPV6			= PDS name for the Cobol v6 loadlib
    	SIGYCOMPV4			= PDS name for the Cobol v4 loadlib
    	TANDELOAD			= 
    	XPEDLOAD			= PDS for the Xpediter compiler
    	cicsDir				= Directory location for CICS scripts
    	cicsVersion			= CICS Version for CICS scripts
    	cobol2json			= CICS script for Cobol to JSON script
    	json2cobol			= CICS script for JSON to Cobol script
    	cobol2soap			= CICS script for Cobol to SOAP script
    	soap2cobol			= CICS script for SOAP to Cobol script
    	JAVA_HOME			= Default Java home

    
    
