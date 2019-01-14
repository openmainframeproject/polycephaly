package com.zos.language

import com.ibm.dbb.repository.*
import com.ibm.dbb.dependency.*
import com.ibm.dbb.build.*
import com.wsfg.zos.groovy.utilities.*

class JCLcheck {

	static void main(args) {
	
	}
	
	public void run(args) {
		
		def file = args[0]
		println("* Building $file using ${this.class.getName()}.groovy script")
		
		GroovyObject tools = (GroovyObject) Tools.newInstance()
		// define local properties
		def properties = BuildProperties.getInstance()
		def datasets
		datasets = Eval.me(properties.JCLcheckSrcFiles)
		tools.createDatasets(suffixList:datasets, suffixOpts:"${properties.srcOptions}")

		def member = CopyToPDS.createMemberName(file)
		//def logFile = new File("${properties.workDir}/${member}.log")
		
		// copy program to PDS
		//println("Copying ${properties.workDir}/$file to ${properties.jclPDS}($member)")
		new CopyToPDS().file(new File("${properties.workDir}/$file")).dataset(properties.jclPDS).member(member).execute()
		
		// define the MVSExec command to compile the program
		//def jclCheck = new MVSExec().file(file).pgm(properties.jclCheckProgram).parm("'O(EDCHKDD)'")
		
		// add DD statements to the MVSExec command
		//  --- Need to switch to ISPFexec, TSOExec or REXX script, due to Java environment is not Authorized
		//  
		//jclCheck.dd(new DDStatement().name("SYSIN").dsn("${properties.jclPDS}($member)").options("shr").report(true))
		//jclCheck.dd(new DDStatement().name("SYSPRINT").options(properties.tempCreateOptions))
		//jclCheck.dd(new DDStatement().name("SYSTSPR").options(properties.tempCreateOptions))
		//jclCheck.dd(new DDStatement().name("SYSTERM").options(properties.tempCreateOptions))
		//jclCheck.dd(new DDStatement().name("SYSABOUT").options(properties.tempCreateOptions))
		//jclCheck.dd(new DDStatement().name("SYSOUT").options(properties.tempCreateOptions))
		//jclCheck.dd(new DDStatement().name("SYSTSPRT").options(properties.tempCreateOptions))
		//jclCheck.dd(new DDStatement().name("EDCPRINT").options(properties.tempCreateOptions))
		//jclCheck.dd(new DDStatement().name("WSREXXDD ").dsn(properties.CAIPARM).options("shr"))
		//jclCheck.dd(new DDStatement().name("EDCMSGS").dsn("$properties.CAIPARM(${properties.EDCMSGS})").options("shr"))
		//jclCheck.dd(new DDStatement().name("EDCHKDD").dsn("$properties.CAIPARM(${properties.EDCHKDD})").options("shr"))
		
		//println("file = $jclCheck.file + pgm = $jclCheck.pgm with parms = $jclCheck.parm")
		//def ddStatements = jclCheck.ddStatements
		//def processCounter = 0
		//ddStatements.each { ddStatement ->
		//	println ("$ddStatement.name DD DSN=$ddStatement.dsn, $ddStatement.options")
		//	processCounter++
		//}
		
		
		// add a copy command to the MVSExec command to copy the SYSPRINT from the temporary dataset to an HFS log file
		//jclCheck.copy(new CopyToHFS().ddName("EDCPRINT").file(logFile).hfsEncoding(properties.logEncoding))
		
		//println("rc = $rc")  
		
		// update build result
		//tools.updateBuildResult(file:"$file", rc:rc, maxRC:4, log:logFile)
		
	}

}
