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
class Assembler {

	static main(args) {
	}
	/**
	 *
	 * Assemble and Linkedit z/OS assembler source code 
	 * <br />
	 * The methods adds 2 numbers and return the result
	 *
	 * @param args[0] = source file input from USS directory
	 * 
	 * Properties bring used:
	 * @param properties.asmPDS 				= Output name of Assembler source PDS, generated
	 * @param properties.asmProgram				= Name of the Assembler program
	 * @param properties.appSyslibs				= List of Application SYSLIB datasets
	 * @param properties.AssemblerLoadFiles		= List of Assembler Load PDS
	 * @param properties.AssemblerSrcFiles		= List of Assembler Source PDS
	 * @param properties.DefaultAssemblerCompileOpts	= Default Assemlby Options
	 * @param properties.DefaultLinkEditOpts	= Default Linkedit Options
	 * @param properties.logEncoding			= logfile Encoding
	 * @param properties.linkEditProgram		= Name of Linkedit program
	 * @param properties.linkPDS				= PDS name of Linkedit controls by source file name, PDS generated
	 * @param properties.loadlibPDS				= Output name of Assembler load PDS, generated
	 * @param properties.loadOptions			= Allocation parms for load type PDSs
	 * @param properties.maclibPDS				= Name of maclib PDS, generated
	 * @param properties.objectPDS				= Name of the object PDS, generated
	 * @param properties.SASMMOD1				= Name of the PDS containing asmProgram
	 * @param properties.SDFHLOAD				= Name of the CICS SDFHLOAD library
	 * @param properties.SDFHMAC				= Name of the CICS SDFHMAC library
	 * @param properties.srcOptions				= Allocation parms for source type PDSs
	 * @param properties.tempCreateOptions		= Allocation parms for temporary dataset creation
	 * @param properties.workDir				= Name of the Jenkins working directory
	 * 
	 * Project properties being used
	 * @param AssemblerOpts 					= Override Assembly parms, by project properties, per Assembler source name
	 * @param LinkOpts							= Override Linkedit parms, by project properties, per Assembler source name
	 * @param LKEDCNTL							= Override Linkedit member name withing linkPDS, per Assembler source name
	 * 
	 * @return ASM, LINK, MACLIB, OBJECT		= creates source PDSs, generated by AssemblerSrcFiles using srcOptions
	 * @return LOADLIB							= creates loadlib PDSs, generated by AAssemblerLoadFiles using loadOptions
	 * @return logfile							= Output by source member from Assemble and Linkedit builds
	 * @return DBB report						= Generates a report for each source file and saved in DBB build repo
	 * @since version 1.00
	 */
	public void run(args) {
		
		// receive passed arguments
		def file = args[0]
		def fileName = new File(file).getName().toString()
		println("* Building $file using ${this.class.getName()}.groovy script")
		
		//GroovyObject tools = (GroovyObject) Tools.newInstance()
		//--
		def tools = this.class.classLoader.loadClass( 'Tools', true, false )?.newInstance()
		//--
		
		
		def properties = BuildProperties.getInstance()
		def datasets 
		datasets = Eval.me(properties.AssemblerSrcFiles)
		tools.createDatasets(suffixList:datasets, suffixOpts:"${properties.srcOptions}")
		datasets = Eval.me(properties.AssemblerLoadFiles)
		tools.createDatasets(suffixList:datasets, suffixOpts:"${properties.loadOptions}")
		
		def member = CopyToPDS.createMemberName(file)
		def logFile = new File("${properties.workDir}/${member}.log")
		
		// copy program to PDS
		//println("Copying ${properties.workDir}/$file to ${properties.asmPDS}($member)")
		new CopyToPDS().file(new File("${properties.workDir}/$file")).dataset(properties.asmPDS).member(member).execute()
		
		//resolve program dependencies and copy to PDS
		//println("Resolving dependencies for file $file and copying to ${properties.maclibPDS}")
		def resolver = tools.getDefaultDependencyResolver(file)
		def deps = resolver.resolve()
		new CopyToPDS().dependencies(deps).dataset(properties.maclibPDS).execute()
		def logicalFile = resolver.getLogicalFile()
		
		// Process Assembler routine
		def assemblerParms = properties.getFileProperty("AssemblerOpts", fileName)
		if (assemblerParms == null) {
			assemblerParms = properties.DefaultAssemblerCompileOpts
		}
		if (logicalFile.isSQL()) {
			assemblerParms = "$assemblerParms,SQL"
		}
		if (properties.errPrefix) {
			assemblerParms = "$assemblerParms,ADATA,EX(ADX(ELAXMGUX))"
		}
		println("** Running Assembler for program $member and opts = $assemblerParms")
		def assemble = new MVSExec().file(file).pgm(properties.asmProgram).parm(assemblerParms)
		assemble.dd(new DDStatement().name("SYSIN").dsn("${properties.asmPDS}($member)").options("shr"))
		assemble.dd(new DDStatement().name("SYSPUNCH").dsn("&&TEMPOBJ").options(properties.tempCreateOptions).pass(true))
		assemble.dd(new DDStatement().name("SYSPRINT").options(properties.tempCreateOptions))
		assemble.dd(new DDStatement().name("SYSUT1").options(properties.tempCreateOptions))
		assemble.dd(new DDStatement().name("SYSUT2").options(properties.tempCreateOptions))
		assemble.dd(new DDStatement().name("SYSUT3").options(properties.tempCreateOptions))
		assemble.dd(new DDStatement().name("SYSLIB").dsn(properties.maclibPDS).options("shr"))
		if (logicalFile.isCICS()) {
			// create a DD statement without a name to concatenate to the last named DD
			assemble.dd(new DDStatement().dsn(properties.SDFHMAC).options("shr"))
		}
		if (properties.appMaclibs != null) {
			// for user builds concatenate the team build copbook pds
			def maclibs = Eval.me(properties.appMaclibs)
			maclibs.each { maclib ->
				//println(" Adding $maclib to assemble.SYSLIB")
				assemble.dd(new DDStatement().dsn(maclib).options("shr"))
			}
		}

		// add a tasklib to the compile command with optional CICS, DB2, and IDz concatenations
		assemble.dd(new DDStatement().name("TASKLIB").dsn(properties.SASMMOD1).options("shr"))
		// add a copy command to the compile command to copy the SYSPRINT from the temporary dataset to an HFS log file
		assemble.copy(new CopyToHFS().ddName("SYSPRINT").file(logFile).hfsEncoding(properties.logEncoding).append(true))
	
		// define the MVSExec command to link edit the program
		def linkOpts = properties.getFileProperty("LinkOpts", fileName)
		def lkedcntl = properties.getFileProperty("LKEDCNTL", fileName)
		if (linkOpts == null) {
			linkOpts = properties.DefaultLinkEditOpts
		}
		println("** LinkEditing Assembler program $member with LinkEdit Parms = $linkOpts")
		def linkedit = new MVSExec().file(file).pgm(properties.linkEditProgram).parm(linkOpts)
								
		// add DD statements to the linkedit command
		linkedit.dd(new DDStatement().name("SYSLIN").dsn("&&TEMPOBJ").options("shr"))
		if (lkedcntl != null) {
			def lkedMember = CopyToPDS.createMemberName(lkedcntl)
			//println("with $fileName - copying ${properties.workDir}/${properties.'src.zOS.dir'}$lkedcntl to ${properties.linkPDS}($lkedMember)")
			new CopyToPDS().file(new File("${properties.workDir}/${properties.'src.zOS.dir'}$lkedcntl")).dataset(properties.linkPDS).member(lkedMember).execute()
			//println("Using linkedit datasets = ${properties.linkPDS}($lkedMember)")
			linkedit.dd(new DDStatement().dsn("${properties.linkPDS}($lkedMember)").options("shr"))
		}
		linkedit.dd(new DDStatement().name("SYSLMOD").dsn("${properties.loadlibPDS}($member)").options("old").output(true).deployType("LOAD"))
		linkedit.dd(new DDStatement().name("SYSPRINT").options(properties.tempCreateOptions))
		linkedit.dd(new DDStatement().name("SYSUT1").options(properties.tempCreateOptions))
		linkedit.dd(new DDStatement().name("SYSLIB").dsn(properties.objectPDS).options("shr"))
		if (logicalFile.isCICS()) {
			linkedit.dd(new DDStatement().dsn(properties.SDFHLOAD).options("shr"))
		}
		if (properties.appSyslibs != null) {
			// for user builds concatenate the team build copbook pds
			def syslibs = Eval.me(properties.appSyslibs)
			syslibs.each { syslib ->
				//println(" Adding $syslib to SYSLIB")
				linkedit.dd(new DDStatement().dsn(syslib).options("shr"))
			}
		}
		
		// add a copy command to the linkedit command to append the SYSPRINT from the temporary dataset to the HFS log file
		linkedit.copy(new CopyToHFS().ddName("SYSPRINT").file(logFile).hfsEncoding(properties.logEncoding).append(true))
		
		/********************************************************************************
		 *  Running individual steps
		 ********************************************************************************/
		def job = new MVSJob()
		job.start()
		
		def rc = assemble.execute()
		//println(" ran Assembly completed RC = $rc ")
		//tools.updateBuildResult(file:"$file", rc:rc, maxRC:4, log:logFile)
		if (rc <= 4) {
			rc = linkedit.execute()
			//println(" running LinkEdit completed RC = $rc ")
			//tools.updateBuildResult(file:"$file", rc:rc, maxRC:4, log:logFile)
		}
		job.stop()
	}
}
