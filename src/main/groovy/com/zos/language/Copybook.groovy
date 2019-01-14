package com.zos.language

import com.ibm.dbb.repository.*
import com.ibm.dbb.dependency.*
import com.ibm.dbb.build.*
import com.wsfg.zos.groovy.utilities.*

class Copybook {


	static main(args) {
	}
	
	public void run(args) {
	
	// receive passed arguments
	def file = args[0]
	println("* Building $file using ${this.class.getName()}.groovy script")
	
	GroovyObject tools = (GroovyObject) Tools.newInstance()
	def properties = BuildProperties.getInstance()
	def datasets
	datasets = Eval.me(properties.CopybookSrcFiles)
	tools.createDatasets(suffixList:datasets, suffixOpts:"${properties.srcOptions}")
	
	def member = CopyToPDS.createMemberName(file)
	def logFile = new File("${properties.workDir}/${member}.${properties.logFileSuffix}")
	
	// copy program to PDS
	//println("Copying ${properties.workDir}/$file to ${properties.copybookPDS}($member)")
	def rc = new CopyToPDS().file(new File("${properties.workDir}/$file")).dataset(properties.copybookPDS).member(member).execute()
	
	// update build result
	tools.updateBuildResult(file:"$file", rc:rc, maxRC:0, log:logFile)
	}
	
}

