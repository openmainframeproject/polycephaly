/**
 * 
 */
package com.zos.language

import com.ibm.dbb.repository.*
import com.ibm.dbb.dependency.*
import com.ibm.dbb.build.*
import com.wsfg.zos.groovy.utilities.*

/**
 * @author gedgingt
 *
 */
class Easytrieve {

	static main(args) {
	}
	public void run(args) {
		
		// receive passed arguments
		def file = args[0]
		println("* Building $file using ${this.class.getName()}.groovy script")
		
		GroovyObject tools = (GroovyObject) Tools.newInstance()
		// define local properties
		def properties = BuildProperties.getInstance()
		def datasets
		datasets = Eval.me(properties.EasytrieveSrcFiles)
		tools.createDatasets(suffixList:datasets, suffixOpts:"${properties.srcOptions}")
		datasets = Eval.me(properties.EasytrieveloadFiles)
		tools.createDatasets(suffixList:datasets, suffixOpts:"${properties.loadOptions}")

		//println("CopyToPDS = $file")
		def member = CopyToPDS.createMemberName(file)
		def logFile = new File("${properties.workDir}/${member}.log")
		
		// copy program to PDS
		//println("Copying ${properties.workDir}/$file to $asmPDS($member)")
		new CopyToPDS().file(new File("${properties.workDir}/$file")).dataset(properties.eztPDS).member(member).execute()
		
		/**---------------------------
		 *	Run Extrieve Compile
		 */
		def easytrieveParms = properties.getFileProperty("easytrieveOpts", file)
		if (easytrieveParms == null) {
			easytrieveParms = properties.DefaultEasytrieveOpts
		}
		// define the MVSExec command to compile the BMS map
		println("** Running easytrieve for program $member and opts = $easytrieveParms")
		def easytrieve = new MVSExec().file(file).pgm(properties.easytrieveProgram).parm(easytrieveParms)
		easytrieve.dd(new DDStatement().name("SYSIN").dsn("${properties.eztPDS}($member)").options("shr"))
		easytrieve.dd(new DDStatement().name("SYSLIN").dsn("&&TEMPOBJ").options("${properties.tempCreateOptions2} ${properties.lrecl400} ${properties.recfmFB}").pass(true))
		easytrieve.dd(new DDStatement().name("SYSPRINT").options("${properties.tempCreateOptions2} ${properties.lrecl133} ${properties.recfmFBA}"))
		easytrieve.dd(new DDStatement().name("SYSUT1").options(properties.tempCreateOptions))
		easytrieve.dd(new DDStatement().name("SYSUT2").options(properties.tempCreateOptions))
		easytrieve.dd(new DDStatement().name("EZTVFM").options(properties.eztvmOptions))
		// add a copy command to the compile command to copy the SYSPRINT from the temporary dataset to an HFS log file
		easytrieve.copy(new CopyToHFS().ddName("SYSPRINT").file(logFile).hfsEncoding(properties.logEncoding).append(true))
		
		// add a syslib to the compile command with optional CICS concatenation
		easytrieve.dd(new DDStatement().name("SYSLIB").dsn(properties.copybookPDS).options("shr"))
		if (properties.appCopylibs != null) {
			// for user builds concatenate the team build copbook pds
			def copylibs = Eval.me(properties.appCopylibs)
			copylibs.each { copylib ->
				//println(" Adding $copylib to compile.SYSLIB")
				easytrieve.dd(new DDStatement().dsn(copylib).options("shr"))
			}
		}
		/**---------------------------
		 *  Run the LinkEdit Step
		 */
		def lkedcntl = properties.getFileProperty("LKEDCNTL", file)
		def lkedMember
		if (lkedcntl != null) {
			lkedMember = CopyToPDS.createMemberName(lkedcntl)
			//println("with $fileName - copying ${properties.workDir}/${properties.'src.zOS.dir'}$lkedcntl to ${properties.linkPDS}($lkedMember)")
			new CopyToPDS().file(new File("${properties.workDir}/${properties.'src.zOS.dir'}$lkedcntl")).dataset(properties.linkPDS).member(lkedMember).execute()
		}
		
		def linkOpts = properties.getFileProperty("LinkOpts", file)
		if (linkOpts == null) {
			linkOpts = properties.DefaultLinkEditOpts
		}
		def linkedit = new MVSExec().file(file).pgm(properties.linkEditProgram).parm(linkOpts)
		linkedit.dd(new DDStatement().name("SYSLMOD").dsn("${properties.loadlibPDS}($member)").options("old").output(true).deployType("MAPLOAD"))
		linkedit.dd(new DDStatement().name("SYSPRINT").options(properties.tempCreateOptions))
		linkedit.dd(new DDStatement().name("SYSUT1").options(properties.tempCreateOptions))
		linkedit.dd(new DDStatement().name("SYSLIB").dsn(properties.objectPDS).options("shr"))
		if (properties.appSyslibs != null) {
			// for user builds concatenate the team build copbook pds
			def syslibs = Eval.me(properties.appSyslibs)
			syslibs.each { syslib ->
				linkedit.dd(new DDStatement().dsn(syslib).options("shr"))
			}
		}
		
		// add a copy command to the linkedit command to append the SYSPRINT from the temporary dataset to the HFS log file
		linkedit.copy(new CopyToHFS().ddName("SYSPRINT").file(logFile).hfsEncoding(properties.logEncoding).append(true))
		
		// execute a simple MVSJob to handle passed temporary DDs between MVSExec commands
		def rc = new MVSJob().executable(easytrieve).executable(linkedit).maxRC(0).execute()
		
		
		// update build result
		tools.updateBuildResult(file:"$file", rc:rc, maxRC:0, log:logFile)
	}
}
