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
class LinkEdit {

	static void main(args) {
	
	}
	
	public void run(args) {
		
		// receive passed arguments
		def file = args[0]
		def fileName = new File(file).getName().toString()
		println("* Building $file using ${this.class.getName()}.groovy script")
		
		//GroovyObject tools = (GroovyObject) Tools.newInstance()
		def tools = new Tools()
		// define local properties
		def properties = BuildProperties.getInstance()
		def datasets
		datasets = Eval.me(properties.LinkEditSrcFiles)
		tools.createDatasets(suffixList:datasets, suffixOpts:"${properties.srcOptions}")
		datasets = Eval.me(properties.LinkEditloadFiles)
		tools.createDatasets(suffixList:datasets, suffixOpts:"${properties.loadOptions}")

		def member = CopyToPDS.createMemberName(file)
		def logFile = new File("${properties.workDir}/${member}.log")
		
		/********************************************************************************
		 *  Building the LinkEdit step
		 ********************************************************************************/
		def lkedcntl = properties.getFileProperty("LKEDCNTL", fileName)
		def lkedMember
		if (lkedcntl != null) {
			lkedMember = CopyToPDS.createMemberName(lkedcntl)
			//println("with $fileName - copying ${properties.workDir}/${properties.'src.zOS.dir'}$lkedcntl to ${properties.linkPDS}($lkedMember)")
			new CopyToPDS().file(new File("${properties.workDir}/${properties.'src.zOS.dir'}$lkedcntl")).dataset(properties.linkPDS).member(lkedMember).execute()
		}
		
		// define the MVSExec command to link edit the program
		// create the appropriate compile parm list
		def linkOpts = properties.getFileProperty("LinkOpts", fileName)
		if (linkOpts == null) {
			linkOpts = properties.DefaultLinkEditOpts
		}
		println("** LinkEditing program $member with LinkEdit Parms = $linkOpts")
		def linkedit = new MVSExec().file(file).pgm(properties.linkEditProgram).parm(linkOpts)
		// add DD statements to the linkedit command
		linkedit.dd(new DDStatement().name("SYSLIN").dsn("${properties.objPDS}($member)").options("shr"))
		linkedit.dd(new DDStatement().dsn("${properties.linkPDS}($member)").options("shr"))
		linkedit.dd(new DDStatement().name("SYSLMOD").dsn("${properties.loadlibPDS}($member)").options("old").output(true).deployType("LOAD"))
		linkedit.dd(new DDStatement().name("SYSPRINT").options(properties.tempCreateOptions))
		linkedit.dd(new DDStatement().name("SYSUT1").options(properties.tempCreateOptions))
		linkedit.dd(new DDStatement().name("SYSLIB").dsn(properties.objectPDS).options("shr"))
		
		// add a copy command to the linkedit command to append the SYSPRINT from the temporary dataset to the HFS log file
		linkedit.copy(new CopyToHFS().ddName("SYSPRINT").file(logFile).hfsEncoding(properties.logEncoding))
		
		// execute the link edit command
		def rc = linkedit.execute()
		
		// update build result
		//tools.updateBuildResult(file:"$file", rc:rc, maxRC:0, log:logFile)
		
	}

}
