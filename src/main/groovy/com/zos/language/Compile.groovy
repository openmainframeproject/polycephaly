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
class Compile {

	static void main(args) {
	
	}
	
	public void run(args) {
		
		
		// receive passed arguments
		def file = args[0]
		def fileName = new File(file).getName().toString()
		println("* Building $file using ${this.class.getName()}.groovy script")
		//* Building src/main/zOS/com/zos/cobol/App1/k164baco.cbl using com.zos.groovy.utilities.Compile.groovy script
		
		GroovyObject tools = (GroovyObject) Tools.newInstance()
		// define local properties
		def properties = BuildProperties.getInstance()
		def datasets
		datasets = Eval.me(properties.CobolSrcFiles)
		tools.createDatasets(suffixList:datasets, suffixOpts:"${properties.srcOptions}")

		def member = CopyToPDS.createMemberName(file)
		def logFile = new File("${properties.workDir}/${member}.log")
		
		// copy program to PDS
		//println("Copying ${properties.workDir}/$file to ${properties.cobolPDS}($member)")
		new CopyToPDS().file(new File("${properties.workDir}/$file")).dataset(properties.cobolPDS).member(member).execute()
		
		//resolve program dependencies and copy to PDS
		//println("Resolving dependencies for file $file and copying to $copybookPDS")
		def resolver = tools.getDefaultDependencyResolver(file)
		def deps = resolver.resolve()
		new CopyToPDS().dependencies(deps).dataset(properties.copybookPDS).execute()
		
		// compile the build file
		//println("Compiling build file $file")
		def logicalFile = resolver.getLogicalFile()
		
		// create the appropriate compile parm list
		
		def compileParms = properties.getFileProperty("compileOpts", fileName)
		if (compileParms == null) {
			compileParms = properties.DefaultCobolCompileOpts
		}
		if (logicalFile.isCICS()) {
			compileParms = "$compileParms,DYNAM,CICS"
		}
		if (properties.errPrefix) {
			compileParms = "$compileParms,ADATA,EX(ADX(ELAXMGUX))"
		}
		
		// define the MVSExec command to compile the program
		println("** Compiling Cobol for program $member and opts = $compileParms")
		def compile = new MVSExec().file(file).pgm(properties.cobolCompiler).parm(compileParms)
		
		// add DD statements to the MVSExec command
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
		
		// add a syslib to the MVSExec command with optional CICS concatenation
		compile.dd(new DDStatement().name("SYSLIB").dsn(properties.copybookPDS).options("shr"))
		compile.dd(new DDStatement().dsn(properties.SCEESAMP).options("shr"))
		
		
		if (properties.team) {
			// for user builds concatenate the team build copbook pds
			compile.dd(new DDStatement().dsn("${properties.team}.${properties.copybookPDS}").options("shr"))
		}
		if (properties.appCopylibs != null) {
			// for user builds concatenate the team build copbook pds
			def copylibs = Eval.me(properties.appCopylibs)
			
			copylibs.each { copylib ->
				compile.dd(new DDStatement().dsn(copylib).options("shr"))
			}
		}
		
		if (logicalFile.isCICS()) {
			// create a DD statement without a name to concatenate to the last named DD added to the MVSExec
			compile.dd(new DDStatement().dsn(properties.SDFHCOB).options("shr"))
		}
		
		// add a tasklib to the MVSExec command with optional CICS and IDz concatenation
		compile.dd(new DDStatement().name("TASKLIB").dsn(properties.SIGYCOMP).options("shr"))
		
		if (logicalFile.isCICS()) {
			// create a DD statement without a name to concatenate to the last named DD added to the MVSExec
			compile.dd(new DDStatement().dsn(properties.SDFHLOAD).options("shr"))
		}
		if (properties.SFELLOAD) {
			compile.dd(new DDStatement().dsn(properties.SFELLOAD).options("shr"))
		}
		
		// add IDz User Build Error Feedback DDs
		if (properties.errPrefix) {
			compile.dd(new DDStatement().name("SYSADATA").options("DUMMY"))
			compile.dd(new DDStatement().name("SYSXMLSD").dsn("${properties.devHLQ}.${properties.errPrefix}.${properties.xmlPDSsuffix}").options("mod keep"))
		}
		
		// add a copy command to the MVSExec command to copy the SYSPRINT from the temporary dataset to an HFS log file
		compile.copy(new CopyToHFS().ddName("SYSPRINT").file(logFile).hfsEncoding(properties.logEncoding))
		
		// execute the MVSExec compile command
		def rc = compile.execute()
		
		// update build result
		//tools.updateBuildResult(file:"$file", rc:rc, maxRC:4, log:logFile)
		
	}

}
