package com.zos.groovy.utilities

import com.ibm.dbb.build.*
import com.ibm.dbb.build.report.*
import com.ibm.dbb.repository.*
import com.ibm.dbb.dependency.*
import groovy.time.*
import groovy.lang.GroovyClassLoader
import groovy.lang.Script 

/**
* @author gedgingt
* @version v4.0.0
* Date 12/24/2018
*
* SPDX-License-Identifier: Apache-2.0 
*/

class ZosResBuild {
	/**
	 *
	 * <br />
	 * Main zOS system or resident volume build routine
	 * 	Currently still work in progress
	 *
	 * @param args
	 * @deprecated
	 * @since version 1.00
	 */
	static main(args) {
		
	}
	public boolean execute(args) {

		GroovyObject tools = (GroovyObject) Tools.newInstance()
		def startTime = new Date()
		
		// parse command line arguments and load build properties
		def usage = "build.groovy [options] buildfile"
		def opts = tools.parseArgs(args, usage)
		def properties = tools.loadProperties(opts)
		tools.validateRequiredProperties(["BuildList"])
		
		properties.startTime = startTime.format("yyyyMMdd.hhmmss.mmm")
		println("** Build start at $properties.startTime")
		
		// initialize build artifacts
		tools.initializeBuildArtifacts()
		
		// create workdir (if necessary)
		new File(properties.workDir).mkdirs()
		println("** Build output will be in $properties.workDir")
		
		// create build list from input build file
		def buildList = tools.getBuildList(opts.arguments())
		
		println("************************************* properties ****************************************************************************")
		println(properties.list())
		def env = System.getenv()
			env.each{
			println it
		}
		println("*****************************************************************************************************************************")
		
		// generate build report
		//def (File jsonFile, File htmlFile) = tools.generateBuildReport()
		
		// finalize build result
		//tools.finalizeBuildResult(jsonReport:jsonFile, htmlReport:htmlFile, filesProcessed:processCounter)
		
	}
}
