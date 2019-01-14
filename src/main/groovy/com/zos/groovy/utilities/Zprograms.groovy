package com.zos.groovy.utilities

import com.ibm.dbb.repository.*
import com.ibm.dbb.dependency.*
import com.ibm.dbb.build.*

/**
 * @author gedgingt
 *
 */
class Zprograms {

	static main(args) {
	}

	/**
	 * @param args[0] String - SYSUT1 input dataset
	 * @param args[1] String - SYSUT2 output dataset Doesn't need to be preallocated
	 * @param args[2] String - parmlib PDS member name
	 * @param args[3] String - Optional parmlibPDS, defaults to (properties.parmlibPDS)
	 * @return int - return code from the execution of the IEBCOPY command
	 */
	public int iebcopy(args) {

		// receive passed arguments
		def sysut1 = args[0];
		def sysut2 = args[1];
		def member  = args[2];
		def parmlibPDS 
		
		GroovyObject tools = (GroovyObject) Tools.newInstance()
		def properties = BuildProperties.getInstance() 

		if (args[3] != null) {
			parmlibPDS = args[3]
			new CreatePDS().dataset(parmlibPDS).options(properties.srcOptions).create()
		} else {
			parmlibPDS = properties.parmlibPDS
		}

		def logFile = new File("${properties.workDir}/${properties.iebcopyProgram}.log")
		new CreatePDS().dataset("$sysut2").options(properties.srcOptions).create()
		def iebcopy = new MVSExec().file(member).pgm(properties.iebcopyProgram)
		iebcopy.dd(new DDStatement().name("SYSUT1").dsn(sysut1).options("shr"))
		iebcopy.dd(new DDStatement().name("SYSUT2").dsn(sysut2).options("old"))
		iebcopy.dd(new DDStatement().name("SYSIN").dsn("$parmlibPDS($member)").options("shr"))
		iebcopy.dd(new DDStatement().name("SYSPUNCH").dsn("&&TEMPOBJ").options(properties.tempCreateOptions).pass(true))
		iebcopy.dd(new DDStatement().name("SYSPRINT").options(properties.tempCreateOptions))

		// add a copy command to the compile command to copy the SYSPRINT from the temporary dataset to an HFS log file
		iebcopy.copy(new CopyToHFS().ddName("SYSPRINT").file(logFile).hfsEncoding(properties.logEncoding).append(true))
		// execute a simple MVSJob to handle passed temporary DDs between MVSExec commands
		def rc = new MVSJob().executable(iebcopy).maxRC(0).execute()
		tools.updateBuildResult(file:"$member", rc:rc, maxRC:0, log:logFile)
		return rc
	}
	
	/**
	 * @param args[0] String - parmlib PDS member name
	 * @param args[1] String - Optional parmlibPDS, defaults to (properties.parmlibPDS)
	 * @return int - return code from the execution of the ICKDSF command
	 */
	public int ickdsf(args) {
		
		// receive passed arguments
		String member  = args[0]
		def parmlibPDS
		
		GroovyObject tools = (GroovyObject) Tools.newInstance()
		// define local properties
		def properties = BuildProperties.getInstance()
		
		if (args[1] != null) {
			parmlibPDS = args[1]
			new CreatePDS().dataset(parmlibPDS).options(properties.srcOptions).create()
		} else {
			parmlibPDS = properties.parmlibPDS
		}

		def logFile = new File("${properties.workDir}/${properties.ickdsfProgram}.log")
		def ickdsf = new MVSExec().file(member).pgm(properties.ickdsfProgram)
		ickdsf.dd(new DDStatement().name("SYSIN").dsn("$parmlibPDS($member)").options("shr"))
		ickdsf.dd(new DDStatement().name("SYSPRINT").options(properties.tempCreateOptions))
		if (args[1] != null) {
			properties.SYS1LINKLIB = "${args[1]}.${properties.SYS1LINKLIB}" 
		}
		if (args[2] != null) {
			def linkVolOpts = "shr unit(sysda) vol=ser=${args[2]}" 
			ickdsf.dd(new DDStatement().name("TASKLIB").dsn(properties.SYS1LINKLIB).options(linkVolOpts))
		} else {
			ickdsf.dd(new DDStatement().name("TASKLIB").dsn(properties.SYS1LINKLIB).options("shr"))
		}
		ickdsf.copy(new CopyToHFS().ddName("SYSPRINT").file(logFile).hfsEncoding(properties.logEncoding).append(true))
		def rc = new MVSJob().executable(ickdsf).maxRC(0).execute()
		tools.updateBuildResult(file:"$member", rc:rc, maxRC:0, log:logFile)
		return rc
	}
	
	/**
	 * @param args[0] String - parmlib PDS member name
	 * @param args[1] String - Optional parmlibPDS, defaults to (properties.parmlibPDS)
	 * @return int - return code from the execution of the ADRDSSU command
	 */
	public int adrdssu(args) {
		
		String member  = args[0];
		GroovyObject tools = (GroovyObject) Tools.newInstance()
		def properties = BuildProperties.getInstance()
		
		if (args[1] != null) {
			parmlibPDS = args[1]
			new CreatePDS().dataset(parmlibPDS).options(properties.srcOptions).create()
		} else {
			parmlibPDS = properties.parmlibPDS
		}

		def logFile = new File("${properties.workDir}/${properties.adrdssuProgram}.log")
		def adrdssu = new MVSExec().file(member).pgm(properties.adrdssuProgram)
		adrdssu.dd(new DDStatement().name("SYSIN").dsn("${properties.parmlibPDS}($member)").options("shr"))
		adrdssu.dd(new DDStatement().name("SYSPRINT").options(properties.tempCreateOptions))
		adrdssu.copy(new CopyToHFS().ddName("SYSPRINT").file(logFile).hfsEncoding(properties.logEncoding).append(true))
		def rc = new MVSJob().executable(iebcopy).maxRC(0).execute()
		tools.updateBuildResult(file:"$member", rc:rc, maxRC:0, log:logFile)
		return rc
	}
	
	
	/**
	 * @param args[0] String - PDS member name
	 * @param args[1] String - Optional parmlibPDS, defaults to (properties.parmlibPDS)
	 * @return int - return code from the execution of the IDCAMS command
	 */
	public int idcams(args) {
		
		String member  = args[0]	// FileName to be used as input parm to IDCAMS
		def parmlibPDS
		 
		GroovyObject tools = (GroovyObject) Tools.newInstance()
		def properties = BuildProperties.getInstance()
		
		if (args[1] != null) {
			parmlibPDS = args[1]
			new CreatePDS().dataset(parmlibPDS).options(properties.srcOptions).create()
		} else {
			parmlibPDS = properties.parmlibPDS
		}

		def logFile = new File("${properties.workDir}/${properties.idcamsProgram}.log")
		def idcams = new MVSExec().file(member).pgm(properties.idcamsProgram)
		idcams.dd(new DDStatement().name("SYSPRINT").options(properties.tempCreateOptions))
		idcams.dd(new DDStatement().name("SYSIN").dsn("$parmlibPDS($member)").options("shr"))
		idcams.copy(new CopyToHFS().ddName("SYSPRINT").file(logFile).hfsEncoding(properties.logEncoding).append(true))
		def rc = new MVSJob().executable(idcams).maxRC(0).execute()
		// update build result
		tools.updateBuildResult(file:"$member", rc:rc, maxRC:0, log:logFile)
		return rc
	}
	
	/**
	 * @param args[0] String - PDS member name
	 * @param args[1] String - Optional parmlibPDS, defaults to (properties.parmlibPDS)
	 * @param args[2] String - Optional output dataset name for SYSPRINT DD, must be Full qualified z/OS, but doesn't have to be preallocated
	 * @return int - return code from the execution of the DFHCSDUP command 
	 */
	public int dfhcsdup(args) {
		
		String member  = args[0]	// FileName to be used as input parm to IDCAMS
		def parmlibPDS
		def sysprint
		def outputDataset = false
		 
		GroovyObject tools = (GroovyObject) Tools.newInstance()
		def properties = BuildProperties.getInstance()
		
		if (args[1]) {
			parmlibPDS = args[1]
			new CreatePDS().dataset(parmlibPDS).options(properties.srcOptions).create()
		} else {
			parmlibPDS = properties.parmlibPDS
		}	
		
		if (args[2]) {
			sysprint = args[2]
			outputDataset = true
		}

		def logFile = new File("${properties.workDir}/${properties.cicsCSDProgram}.log")
		def dfhcsdup = new MVSExec().file(member).pgm(properties.cicsCSDProgram).parm(properties.DefaultcicsCSDOpts)
		dfhcsdup.dd(new DDStatement().name("SYSIN").dsn("$parmlibPDS($member)").options("shr"))
		dfhcsdup.dd(new DDStatement().name("DFHCSD").dsn(properties.DFHCSD).options("shr"))
		if (outputDataset) {
			if (new File(sysprint).exists()) {
				println("Using allocated z/OS dataset = $sysprint")
				dfhcsdup.dd(new DDStatement().name("SYSPRINT").dsn(sysprint).options("old"))
			} else {
				println("Using allocated z/OS dataset = $sysprint")
				dfhcsdup.dd(new DDStatement().name("SYSPRINT").dsn(sysprint).options("${properties.sysprintDataSetAllocation}"))
			}
		} else {
			dfhcsdup.dd(new DDStatement().name("SYSPRINT").options(properties.tempCreateOptions))
			dfhcsdup.copy(new CopyToHFS().ddName("SYSPRINT").file(logFile).hfsEncoding(properties.logEncoding).append(true))
		}
		
		dfhcsdup.dd(new DDStatement().name("TASKLIB").dsn(properties.SDFHLOAD).options("shr"))
		def rc = new MVSJob().executable(dfhcsdup).maxRC(0).execute()
		// update build result
		if (!outputDataset) {
			tools.updateBuildResult(file:"$member", rc:rc, maxRC:0, log:logFile)
		}
		return rc
	}
	
}
