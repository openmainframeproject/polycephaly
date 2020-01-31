/**
 * 
 */
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
class SDFGenUtility {

	static main(args) {
	}
	
	public void run(args) {
		// receive passed arguments
		def file = args[0]
		def fileName = new File(file).getName().toString()
		println("* Building $file using ${this.class.getName()}.groovy script")
		
		GroovyObject tools = (GroovyObject) Tools.newInstance()
		// define local properties
		def properties = BuildProperties.getInstance()
		def datasets
		datasets = Eval.me(properties.SDFSrcFiles)
		tools.createDatasets(suffixList:datasets, suffixOpts:"${properties.srcOptions}")
		datasets = Eval.me(properties.SDFLoadFiles)
		tools.createDatasets(suffixList:datasets, suffixOpts:"${properties.loadOptions}")
	
		def member = CopyToPDS.createMemberName(file)
		def logFile = new File("${properties.workDir}/${member}.log")
		
		// copy program to PDS
		println("Copying ${properties.workDir}/$file to  ${properties.sdfPDS}($member)")
		new CopyToPDS().file(new File("${properties.workDir}/$file")).dataset(properties.sdfPDS).member(member).execute()
		
		def sdf2 = new MVSExec().file(file).pgm(properties.ISPFbatchProgram)
		sdf2.dd(new DDStatement().name("SYSPROC").dsn("TSO.COMMON.CLIST").options("shr"))
		sdf2.dd(new DDStatement().dsn("SYS2.ISP.SISPCLIB").options("shr"))
		sdf2.dd(new DDStatement().dsn("CICS.SDF2.SDGICMD").options("shr"))
		sdf2.dd(new DDStatement().name("SYSEXEC").dsn("TSO.COMMON.REXX ").options("shr"))
		sdf2.dd(new DDStatement().dsn("SYS2.ISP.SISPEXEC").options("shr"))
		sdf2.dd(new DDStatement().name("ISPPROF").options(properties.tempPDSCreateOptions))
		sdf2.dd(new DDStatement().name("SYSPRINT").options(properties.tempCreateOptions))
		sdf2.dd(new DDStatement().name("ISPLOG").options(properties.tempCreateOptions))
		sdf2.dd(new DDStatement().name("SYSTSPR").options(properties.tempCreateOptions))
		sdf2.dd(new DDStatement().name("DGIPRINT").options(properties.tempCreateOptions))
		sdf2.dd(new DDStatement().name("SYSTSIN").options(properties.tempCreateOptions))
		sdf2.dd(new DDStatement().name("SYSTSIN").dsn("${properties.parmlibPDS}($member)").options("shr"))
		
		
		/********************************************************************************
		 *  Running individual steps
		 ********************************************************************************/
		def job = new MVSJob()
		job.start()
		
		def rc = sdf2.execute()
		//println(" ran Assembly completed RC = $rc ")
		//tools.updateBuildResult(file:"$file", rc:rc, maxRC:4, log:logFile)
		
		job.stop()
	}

}
