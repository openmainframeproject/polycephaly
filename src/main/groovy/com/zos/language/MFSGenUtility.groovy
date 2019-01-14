package com.zos.language

import com.ibm.dbb.repository.*
import com.ibm.dbb.dependency.*
import com.ibm.dbb.build.*
import com.wsfg.zos.groovy.utilities.*

class MFSGenUtility {

	static void main(args) {
	
	}
	
	public void run(args) {
		

		// receive passed arguments
		def file = args[0]
		println("* Building $file using ${this.class.getName()}.groovy script")
		
		GroovyObject tools = (GroovyObject) Tools.newInstance()
		// define local properties
		def properties = BuildProperties.getInstance()
		def datasets
		datasets = Eval.me(properties.MFSSrcFiles)
		tools.createDatasets(suffixList:datasets, suffixOpts:"${properties.srcOptions}")
		datasets = Eval.me(properties.MFSLoadFiles)
		tools.createDatasets(suffixList:datasets, suffixOpts:"${properties.loadOptions}")

		def member = CopyToPDS.createMemberName(file)
		def logFile = new File("${properties.workDir}/${member}.log")
		
		// copy program to PDS
		println("Copying ${properties.workDir}/$file to  ${properties.mfsPDS}($member)")
		new CopyToPDS().file(new File("${properties.workDir}/$file")).dataset(properties.mfsPDS).member(member).execute()
		
		// Generate the MFS program
		//println("Generating MFS program $file")
		
		// Process Assembler routine
		def mfsParms = properties.getFileProperty("MFSOpts", fileName)
		if (mfsParms == null) {
			mfsParms = properties.DefaultMFSOpts
		}
		
		// define the MVSExec command for MFS Language Utility - Phase 1
		def mfsPhase1 = new MVSExec().file(file).pgm(properties.MFSProgram).parm(mfsParms)
		
		// add DD statements to the mfsPhase1 command
		mfsPhase1.dd(new DDStatement().name("SYSIN").dsn("${properties.mfsPDS}($member)").options("shr").report(true))
		mfsPhase1.dd(new DDStatement().name("REFIN").dsn(properties.REFERAL).options("shr"))
		mfsPhase1.dd(new DDStatement().name("REFOUT").dsn("&&TEMPPDS").options(properties.tempPDSCreateOptions))
		mfsPhase1.dd(new DDStatement().name("REFRD").dsn(properties.REFERAL).options("shr"))
		mfsPhase1.dd(new DDStatement().name("SYSTEXT").options(properties.tempPDSCreateOptions))
		mfsPhase1.dd(new DDStatement().name("SYSPRINT").options(properties.tempPDSCreateOptions))
		mfsPhase1.dd(new DDStatement().name("SEQBLKS").dsn("&&SEQBLK").options(properties.tempPDSCreateOptions).pass(true))
		mfsPhase1.dd(new DDStatement().name("SYSLIB").dsn(properties.SDFSMAC).options("shr"))
		mfsPhase1.dd(new DDStatement().name("TASKLIB").dsn(properties.SDFSRESL).options("shr"))
		
		// add a copy command to the compile command to copy the SYSPRINT from the temporary dataset to an HFS log file
		mfsPhase1.copy(new CopyToHFS().ddName("SYSPRINT").file(logFile).hfsEncoding(properties.logEncoding))
		
		// define the MVSExec command for MFS Language Utility - Phase 2
		def mfsPhase2 = new MVSExec().file(file).pgm("DFSUNUB0").parm("TEST")
		
		// add DD statements to the mfsPhase2 command
		mfsPhase2.dd(new DDStatement().name("UTPRINT").options(properties.tempPDSCreateOptions))
		mfsPhase2.dd(new DDStatement().name("FORMAT").dsn(properties.tformatPDS).options("shr").output(true))
		mfsPhase2.dd(new DDStatement().name("TASKLIB").dsn(properties.SDFSRESL).options("shr"))
		
		// add a copy command to the compile command to copy the SYSPRINT from the temporary dataset to an HFS log file
		mfsPhase2.copy(new CopyToHFS().ddName("UTPRINT").file(logFile).hfsEncoding(properties.logEncoding).append(true))
		
		// execute a simple MVSJob to handle passed temporary DDs between MVSExec commands
		def rc = new MVSJob().executable(mfsPhase1).executable(mfsPhase2).maxRC(8).execute()
		
		// update build result
		tools.updateBuildResult(file:"$file", rc:rc, maxRC:8, log:logFile)

	}

}