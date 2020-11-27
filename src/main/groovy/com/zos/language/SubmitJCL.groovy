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
class SubmitJCL {

	static void main(args) {

	}

	public void run(args) {

		def file = args[0]
		println("* Building $file using ${this.class.getName()}.groovy script")

		//GroovyObject tools = (GroovyObject) Tools.newInstance()
		def tools = new Tools()
		// define local properties
		def properties = BuildProperties.getInstance()

		// Execute JCL from a file on HFS
		JCLExec jclExec = new JCLExec()
		jclExec.file(new File("${properties.workDir}/$file")).confDir(properties.confDir).execute()
		def maxRC = jclExec.getMaxRC()
		def jobID = jclExec.getSubmittedJobId()
		def jobName = jclExec.getSubmittedJobName()
		jclExec.saveOutput(new CopyToHFS().ddName("JESMSGLG").file(logFile).hfsEncoding(properties.logEncoding))
		jclExec.saveOutput(new CopyToHFS().ddName("JESJCL").file(logFile).hfsEncoding(properties.logEncoding))
		jclExec.saveOutput(new CopyToHFS().ddName("JESYSMSG").file(logFile).hfsEncoding(properties.logEncoding))

		println("rc = $rc")

		// update build result
		tools.updateBuildResult(file:"$file", rc:rc, maxRC:4, log:logFile)

	}

}