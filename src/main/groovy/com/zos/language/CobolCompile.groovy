package com.zos.language

import com.ibm.dbb.repository.*
import com.ibm.dbb.dependency.*
import com.ibm.dbb.build.*
import com.zos.groovy.utilities.*
/**
* @author gedgingt
* @version v4.0.0
* Date 12/24/2018
*
* SPDX-License-Identifier: Apache-2.0
*/
class CobolCompile {

	static void main(args) {

	}

	public void run(args) {

		def file = args[0]
		def fileName = new File(file).getName().toString()
		//* Building src/main/zOS/com/zos/cobol/App1/k164baco.cbl using com.zos.groovy.utilities.CobolCompile.groovy script
		 println("* Building $file using ${this.class.getName()}.groovy script")

		def AddXpediter = false
		def compile
		def compileParms
		def compilerLibrary

		//GroovyObject zProgs = (GroovyObject) Zprograms.newInstance()
		//GroovyObject tools = (GroovyObject) Tools.newInstance()
		def tools = new Tools()
		def zProgs = new Zprograms()
		def properties = BuildProperties.getInstance()
		def datasets
		datasets = Eval.me(properties.CobolCompilesrcFiles)
		tools.createDatasets(suffixList:datasets, suffixOpts:"${properties.srcOptions}")
		datasets = Eval.me(properties.CobolCompileloadFiles)
		tools.createDatasets(suffixList:datasets, suffixOpts:"${properties.loadOptions}")

		def member = CopyToPDS.createMemberName(file)
		def logFile = new File("${properties.workDir}/${member}.log")
		def xpedParms
		def xpedCheck = properties.getFileProperty("Xpediter", fileName)
		if (properties.debug) println("Xpediter check = $xpedCheck for file = $fileName")
		if (xpedCheck != null) {
			xpedParms = properties.DefaultXpediterCompileOpts
			AddXpediter = true
			if (properties.XPED_BUILD_PARMS.toBoolean()) {
				buildXpediterInputParms()
				properties.XPED_BUILD_PARMS = 'false'
			}
		}

		if (properties.debug) println("Copying ${properties.workDir}/$file to ${properties.cobolPDS}($member)")
		new CopyToPDS().file(new File("${properties.workDir}/$file")).dataset(properties.cobolPDS).member(member).execute()

		//resolve program dependencies and copy to PDS
		if (properties.debug) println("Resolving dependencies for file $file and copying to ${properties.copybookPDS}")
		def resolver = tools.getDefaultDependencyResolver(file)
		def deps = resolver.resolve()
		new CopyToPDS().dependencies(deps).dataset(properties.copybookPDS).execute()
		def logicalFile = resolver.getLogicalFile()

		// determine Compiler Options and Library version

		def compileV4Parms = properties.getFileProperty("Cobolv4Opts", fileName)
		def compileV6Parms = properties.getFileProperty("Cobolv6Opts", fileName)
		if (compileV4Parms != null) {
			compileParms = compileV4Parms
			compilerLibrary = properties.SIGYCOMPV4
			if (properties.debug) println("running with Cobol v4.2 for program $member and opts = $compileParms")
		} else {
			if (compileV6Parms != null) {
				compileParms = compileV6Parms
				compilerLibrary = properties.SIGYCOMPV6
				if (properties.debug) println("running with Cobol v6 for program $member and opts = $compileParms")
			} else {
				compileParms = properties.DefaultCobolCompileOpts
				compilerLibrary = properties.SIGYCOMPV6
				if (properties.debug) println("running with Cobol v6 for program $member and default opts = $compileParms")
			}
		}
		if (logicalFile.isCICS()) {
			if (properties.debug) println("Adding CICS to Compile")
			compileParms = "$compileParms,CICS"
		}
		if (logicalFile.isSQL()) {
			if (properties.debug) println("Adding SQL to Compile")
			compileParms = "$compileParms,SQL"
		}
		if (properties.errPrefix) {
			if (properties.debug) println("Adding errPrefix with ADATA,EX(ADX(ELAXMGUX)")
			compileParms = "$compileParms,ADATA,EX(ADX(ELAXMGUX))"
		}


		if (AddXpediter) {
			/********************************************************************************
			 *  Run Xpediter Utility to intialize the DDIO file, only once
			 ********************************************************************************/
			if (properties.debug) println("properties.XPED_DELDEF_DDIO = ${properties.XPED_DELDEF_DDIO} for fileName = $fileName")
			if (properties.XPED_DELDEF_DDIO.toBoolean()) {
				zProgs.idcams(["${properties.ddiofile}"])
				def xpedutil = new MVSExec().file(file).pgm(properties.xpediterUtilProgram)
				xpedutil.dd(new DDStatement().name("ABNLDFIL").dsn("${properties.ddioName}").options("shr"))
				xpedutil.dd(new DDStatement().name("ABNLREPT").options("${properties.tempCreateOptions2} ${properties.lrecl133} ${properties.recfmFBA}"))
				xpedutil.dd(new DDStatement().name("ABNLTERM").options("${properties.tempCreateOptions2} ${properties.lrecl133} ${properties.recfmFBA}"))
				xpedutil.dd(new DDStatement().name("ABNLPARM").dsn("${properties.parmlibPDS}(${properties.initddio})").options("shr"))
				xpedutil.dd(new DDStatement().name("TASKLIB").dsn(properties.XPEDLOAD).options("shr"))
				xpedutil.copy(new CopyToHFS().ddName("ABNLREPT").file(logFile).hfsEncoding(properties.logEncoding).append(true))
				def rc = xpedutil.execute()
				tools.updateBuildResult(file:"$file", rc:rc, maxRC:4, log:logFile)
				if (properties.debug) properties.XPED_DELDEF_DDIO.toBoolean().FALSE
				properties.XPED_DELDEF_DDIO = 'false'
				if (properties.debug) println("finished Xpediter format should be FALSE ${properties.XPED_DELDEF_DDIO} for fileName = $fileName")
			}
		}

		/********************************************************************************
		 *  Building the Compile step
		 ********************************************************************************/
		// define the MVSExec command to compile the program
		if (AddXpediter) {
			if (properties.debug) println("** Running Xpediter Compiler for Cobol program $member and options = $compileParms **")
			compile = new MVSExec().file(file).pgm(properties.xpediterMainProgram).parm(xpedParms)
		} else {
			if (properties.debug) println("** ** Running Cobol Compiler for Cobol program $member and options = $compileParms **")
			compile = new MVSExec().file(file).pgm(properties.cobolCompiler).parm(compileParms)
		}
		compile.dd(new DDStatement().name("SYSIN").dsn("${properties.cobolPDS}($member)").options("shr").report(true))
		compile.dd(new DDStatement().name("SYSLIN").dsn("${properties.objectPDS}($member)").options("old").output(true))
		compile.dd(new DDStatement().name("SYSPRINT").options(properties.tempCreateOptions))
		compile.dd(new DDStatement().name("SYSUT1").options(properties.tempCreateOptions))
		compile.dd(new DDStatement().name("SYSUT2").options(properties.tempCreateOptions))
		compile.dd(new DDStatement().name("SYSUT3").options(properties.tempCreateOptions))
		compile.dd(new DDStatement().name("SYSUT4").options(properties.tempCreateOptions))
		compile.dd(new DDStatement().name("SYSUT5").options(properties.tempCreateOptions))
		compile.dd(new DDStatement().name("SYSUT6").options(properties.tempCreateOptions))
		compile.dd(new DDStatement().name("SYSUT7").options(properties.tempCreateOptions))
		compile.dd(new DDStatement().name("SYSUT8").options(properties.tempCreateOptions))
		compile.dd(new DDStatement().name("SYSUT9").options(properties.tempCreateOptions))
		compile.dd(new DDStatement().name("SYSUT10").options(properties.tempCreateOptions))
		compile.dd(new DDStatement().name("SYSUT11").options(properties.tempCreateOptions))
		compile.dd(new DDStatement().name("SYSUT12").options(properties.tempCreateOptions))
		compile.dd(new DDStatement().name("SYSUT13").options(properties.tempCreateOptions))
		compile.dd(new DDStatement().name("SYSUT14").options(properties.tempCreateOptions))
		compile.dd(new DDStatement().name("SYSUT15").options(properties.tempCreateOptions))
		compile.dd(new DDStatement().name("SYSUT16").options(properties.tempCreateOptions))
		compile.dd(new DDStatement().name("SYSUT17").options(properties.tempCreateOptions))
		compile.dd(new DDStatement().name("SYSMDECK").options(properties.tempCreateOptions))

		if(AddXpediter) {
			compile.dd(new DDStatement().name("CWPERRM").options(properties.tempCreateOptions))
			compile.dd(new DDStatement().name("CWPDDIO").dsn("${properties.ddioName}").options("shr"))
			compile.dd(new DDStatement().name("CWPPRMO").dsn("${properties.parmlibPDS}(${properties.xpedOpts})").options("shr"))
		}

		// add a syslib to the compile command with optional CICS concatenation
		compile.dd(new DDStatement().name("SYSLIB").dsn(properties.copybookPDS).options("shr"))

		if (properties.team) {
			   // for user builds concatenate the team build copbook pds
			   compile.dd(new DDStatement().dsn("${properties.team}.COPYBOOK").options("shr"))
		}
		if (properties.appCopylibs != null) {
			// for user builds concatenate the team build copbook pds
			def copylibs = Eval.me(properties.appCopylibs)
			copylibs.each { copylib ->
				if (properties.debug) println(" Adding $copylib to compile.SYSLIB")
				compile.dd(new DDStatement().dsn(copylib).options("shr"))
			}
		}
		if (logicalFile.isCICS()) {
			// create a DD statement without a name to concatenate to the last named DD
			compile.dd(new DDStatement().dsn(properties.SDFHCOB).options("shr"))
		}

		// add a tasklib to the compile command with optional CICS, DB2, and IDz concatenations
		compile.dd(new DDStatement().name("TASKLIB").dsn(compilerLibrary).options("shr"))

		if (AddXpediter) {
			compile.dd(new DDStatement().dsn(properties.XPEDLOAD).options("shr"))
		}
		if (logicalFile.isCICS()) {
			compile.dd(new DDStatement().dsn(properties.SDFHLOAD).options("shr"))
		}
		if (logicalFile.isSQL()) {
			compile.dd(new DDStatement().dsn(properties.SDSNLOAD).options("shr"))
		}
		if (properties.SFELLOAD) {
			compile.dd(new DDStatement().dsn(properties.SFELLOAD).options("shr"))
		}

		// add optional DBRMLIB if build file contains DB2 code
		if (logicalFile.isSQL()) {
			compile.dd(new DDStatement().name("DBRMLIB").dsn("${properties.dbrmPDS}($member)").options("shr").output(true).deployType("DBRM"))
		}

		// add IDz User Build Error Feedback DDs
		if (properties.errPrefix) {
			compile.dd(new DDStatement().name("SYSADATA").options("DUMMY"))
			compile.dd(new DDStatement().name("SYSXMLSD").dsn("${properties.devHLQ}.${properties.errPrefix}.${properties.xmlPDSsuffix}").options("mod keep"))
		}

		// add a copy command to the compile command to copy the SYSPRINT from the temporary dataset to an HFS log file
		compile.copy(new CopyToHFS().ddName("SYSPRINT").file(logFile).hfsEncoding(properties.logEncoding))

		if(AddXpediter) {
			compile.copy(new CopyToHFS().ddName("CWPERRM").file(logFile).hfsEncoding(properties.logEncoding))
		}

		/********************************************************************************
		 *  Building the LinkEdit step
		 ********************************************************************************/
		def lkedcntl = properties.getFileProperty("LKEDCNTL", fileName)
		def lkedMember
		if (lkedcntl != null) {
			lkedMember = CopyToPDS.createMemberName(lkedcntl)
			if (properties.debug) println("with $fileName - copying ${properties.workDir}/${properties.'src.zOS.dir'}$lkedcntl to ${properties.linkPDS}($lkedMember)")
			new CopyToPDS().file(new File("${properties.workDir}/${properties.'src.zOS.dir'}$lkedcntl")).dataset(properties.linkPDS).member(lkedMember).execute()
		}

		// define the MVSExec command to link edit the program
		// create the appropriate compile parm list
		def linkOpts = properties.getFileProperty("LinkOpts", fileName)
		if (linkOpts == null) {
			linkOpts = properties.DefaultLinkEditOpts
		}
		if (properties.debug) println("** LinkEditing Cobol program $member with LinkEdit Parms = $linkOpts")
		def linkedit = new MVSExec().file(file).pgm(properties.linkEditProgram).parm(linkOpts)

		// add DD statements to the linkedit command
		linkedit.dd(new DDStatement().name("SYSLIN").dsn("${properties.objectPDS}($member)").options("shr"))
		if (lkedcntl != null) {
			if (properties.debug) println("Using linkedit datasets = ${properties.linkPDS}($lkedMember)")
			linkedit.dd(new DDStatement().dsn("${properties.linkPDS}($lkedMember)").options("shr"))
		}
		if (logicalFile.isCICS()) {
			if (properties.debug) println("Adding CICS to Compile")
			linkedit.dd(new DDStatement().name("SYSLMOD").dsn("${properties.onlinePDS}($member)").options("old").output(true).deployType("LOAD"))
		} else {
			linkedit.dd(new DDStatement().name("SYSLMOD").dsn("${properties.loadlibPDS}($member)").options("old").output(true).deployType("LOAD"))
		}

		linkedit.dd(new DDStatement().name("SYSPRINT").options(properties.tempCreateOptions))
		linkedit.dd(new DDStatement().name("SYSUT1").options(properties.tempCreateOptions))
		linkedit.dd(new DDStatement().name("SYSLIB").dsn(properties.objectPDS).options("shr"))

		if (properties.appSyslibs != null) {
			// for user builds concatenate the team build copbook pds
			def syslibs = Eval.me(properties.appSyslibs)
			syslibs.each { syslib ->
				//println(" Adding $syslib to linkedit.SYSLIB")
				linkedit.dd(new DDStatement().dsn(syslib).options("shr"))
			}
		}

		// add DD statements to the linkedit command
		if (logicalFile.isCICS()) {
			linkedit.dd(new DDStatement().dsn(properties.SDFHLOAD).options("shr"))
		}

		// add a copy command to the linkedit command to append the SYSPRINT from the temporary dataset to the HFS log file
		linkedit.copy(new CopyToHFS().ddName("SYSPRINT").file(logFile).hfsEncoding(properties.logEncoding).append(true))

		/********************************************************************************
		 *  Running individual steps
		 ********************************************************************************/
		def job = new MVSJob()
		job.start()

		def rc = compile.execute()
		println(" ran Cobol Compile completed RC = $rc ")
		tools.updateBuildResult(file:"$file", rc:rc, maxRC:4, log:logFile)
		if (rc <= 4) {
			//println(" running LinkEdit ")
			rc = linkedit.execute()
			println(" running LinkEdit completed RC = $rc ")
			tools.updateBuildResult(file:"$file", rc:rc, maxRC:4, log:logFile)
			// Scan the load module to determine LINK dependencies. Impact resolver can use these to determine that
			// this file gets rebuilt if a LINK dependency changes.
			if (rc == 0 && !properties.userBuild) {
				println("* Scanning $loadPDS($member) for load module dependencies.")
				def scanner = new LinkEditScanner()
				def scannerLogicalFile = scanner.scan(file, loadPDS)

				// overwrite original logicalDependencies with load module dependencies
				logicalFile.setLogicalDependencies(scannerLogicalFile.getLogicalDependencies())

				// create the outputs collection if needed.
				// NOTE: The outputs collection should be separate from properties.collection otherwise these dependencies will
				//       be overwritten when the source is changed and scanned by source code scanner.
				// NOTE: The outputs collection must be included in ImpactResolver in Tools.groovy to include these outputs
				//       during impact analysis.
				def outputs_collection = "${properties.collection}_outputs"
				def repositoryClient = tools.getDefaultRepositoryClient()
				if (!repositoryClient.collectionExists(outputs_collection)) {
					repositoryClient.createCollection(outputs_collection)
				}

				// Store logical file and indirect dependencies to the outputs collection
				repositoryClient.saveLogicalFile( outputs_collection, logicalFile );
			}
		}
		job.stop()

		// run DB2 Bind PACKAGE if bind is turned on (see MortgageApplication/build/bind.properties)
		if (logicalFile.isSQL() && properties.RUN_DB2_BIND.toBoolean()) {
			def scriptName = "$properties.workDir/build/BindPackage.groovy"
			run(new File(scriptName), [file] as String[])
		}
		if (rc > 4) {
			properties.error = "ERROR"
		}
	}

	private void buildXpediterInputParms(args) {

		GroovyObject tools = (GroovyObject) Tools.newInstance()
		// define local properties
		def properties = BuildProperties.getInstance()

		/********************************************************************************
		 *  Step #1 -  Run IDCAMS to define DDIO file
		 ********************************************************************************/
		/**
		* SYSIN for Step 2,
		* 		- create DDIO file, parmlibPDS(initddio)
		*
		*   ddioFileName default = = 'properties.datasetPrefix'.'properies.ProjectName'.'Zconstants.BUILDNAME).trim()'.DDIO
		*/

		def propsWrite = null;
		def prefix = "${properties.workDir}/${properties.'src.zOS.dir'}/${properties.'zos.parmlib'}"
		def prefixDir = new File(prefix)
		if (!prefixDir.exists()) {
			prefixDir.mkdirs()
		}

		/**
		 * DELETE 'ddioName'
		 * SET MAXCC = 0
		 * DEFINE CLUSTER (NAME('ddioName') +
		 *  'xpedDDIOspace' +
		 * 	CONTROLINTERVALSIZE(26624) +
		 * 	RECORDSIZE(26617 26617) +
		 * 	SHAREOPTIONS(4 4) SPEED UNIQUE NUMBERED)
		 */
		def filePrm = "$prefix/ddioFile.${properties.parmSuffix}"
		def batchFile = new File(filePrm)
		batchFile.write("  ${properties.idcamsDelete} ${properties.ddioName}            ${System.getProperty('line.separator')}")
		batchFile.append(" ${properties.idcamsSetMaxCC}=${properties.idcamsMaxRC}       ${System.getProperty('line.separator')}")
		batchFile.append(" ${properties.xpedDDIODefineStart}  ${properties.LineContinue}${System.getProperty('line.separator')}")
		batchFile.append("    '${properties.ddioName}'  ${properties.LineContinue}      ${System.getProperty('line.separator')}")
		batchFile.append(" ${properties.idcamsEndBracket}   ${properties.LineContinue}  ${System.getProperty('line.separator')}")
		batchFile.append("    ${properties.xpedDDIOspace} ${properties.LineContinue}    ${System.getProperty('line.separator')}")
		batchFile.append(" ${properties.xpedDDIOrecSize}     ${properties.LineContinue} ${System.getProperty('line.separator')}")
		batchFile.append(" ${properties.xpedDDIOCiSize}      ${properties.LineContinue} ${System.getProperty('line.separator')}")
		batchFile.append(" ${properties.xpedDDIOVsamOpts}${properties.idcamsEndBracket} ${System.getProperty('line.separator')}")
		def fileName = CopyToPDS.createMemberName(filePrm)
		new CopyToPDS().file(new File("$filePrm")).dataset(properties.parmlibPDS).member(fileName).execute()
		properties.ddiofile = fileName

		/**
		 * FORMAT TYPE=SOURCE,RC=2,GC=2,EXTENTS=460,AD=DUPS,BLK=26617
		 * DIRX
		 */
		filePrm = "$prefix/initddio.${properties.parmSuffix}"
		batchFile = new File(filePrm)
		batchFile.write("${properties.xpedDDIOp1}                    ${System.getProperty('line.separator')}")
		batchFile.append("${properties.xpedDDIOp2}                   ${System.getProperty('line.separator')}")
		fileName = CopyToPDS.createMemberName(filePrm)
		new CopyToPDS().file(new File("$filePrm")).dataset(properties.parmlibPDS).member(fileName).execute()
		properties.initddio = fileName

		/*
		* Xpediter Compiler Options
		* 	COBOL(OUTPUT(PRINT,DDIO))
		*   PROCESSOR(OUTPUT(NOPRINT,NODDIO),TEXT(NONE))
		*   LANGUAGE(COBOLZ/OS)
		*   LANGUAGE(OUTPUT(PRINT,DDIO))
		*/
		filePrm = "$prefix/xpedcomp.${properties.parmSuffix}"
		batchFile = new File(filePrm)
		batchFile.write("${properties.xpedCompp1}                    ${System.getProperty('line.separator')}")
		batchFile.append("${properties.xpedCompp2}                   ${System.getProperty('line.separator')}")
		batchFile.append("${properties.xpedCompp3}                   ${System.getProperty('line.separator')}")
		batchFile.append("${properties.xpedCompp4}                   ${System.getProperty('line.separator')}")
		fileName = CopyToPDS.createMemberName(filePrm)
		new CopyToPDS().file(new File("$filePrm")).dataset(properties.parmlibPDS).member(fileName).execute()
		properties.xpedOpts = fileName

	}

}
