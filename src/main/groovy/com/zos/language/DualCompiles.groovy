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
	
/**
 * 
 * Example file = args[0]
 * src/main/zOS/com/zos/cobol/App1/k164bacd.dual
 * 
 * Rules:
 * 		For Online 
 * 			- k164baco
 * 			- remove all the # in column 7
 *		For Batch
 *			- k164bacb
 *			- remove all the % in column 7
 *		k164baco save to /com/zos/cobol/App1/k164baco.cbl
 *		k164bacb save to /com/zos/cobol/App1/k164bacb.cbl
  *
 *  	- input file
 *  		properties.DualCompileDeleteLineBuildOnline 
 *  			- remove the lines with this property in column 7 for Onlines
 *  		properties.DualCompileDeleteLineBuildBatch
 *  			- remove the lines with this property in column 7 for Batch
 *  	- output file 
 *  		First character of the program + properties.DualCompileOnlineSuffix for online
 *  		First character of the program + properties.DualCompileBatchSuffix for batch
 *  
 *  	Using these properties will allow the developer to override the options if necessary
 
 *		Run CobolCompile for both

 */
class DualCompiles {

	static main(args) {
		
	}
	public void run(args) {
		
		// receive passed arguments
		def file = args[0]
		println("* Building $file using ${this.class.getName()}.groovy script")
		//* Building src/main/zOS/com/zos/cobol/App1/k164bacd.dual using com.zos.groovy.utilities.DualCompiles.groovy script

		//* 	src/main/zOS/com/zos/cobol/App1/k164bacd.dual
		//*	to  src/main/zOS/com/zos/cobol/App1/k164baco.cbl
		//*	to	src/main/zOS/com/zos/cobol/App1/k164bacb.cbl
		
		GroovyObject tools = (GroovyObject) Tools.newInstance()
		
		// define local properties
		def properties = BuildProperties.getInstance()
		def member = CopyToPDS.createMemberName(file)
		
		def fileNameNoFileType = file[0..<file.lastIndexOf('.')]
		def fileNamePrefix = fileNameNoFileType.substring(0, fileNameNoFileType.length() -1)
		def onlineFileName = "${fileNamePrefix}${properties.DualCompileOnlineSuffix}.${properties.cobolSuffix}"
		def batchFileName = "${fileNamePrefix}${properties.DualCompileBatchSuffix}.${properties.cobolSuffix}"
		def onlineFile = new File("${properties.workDir}/$onlineFileName")
		def batchFile = new File("${properties.workDir}/$batchFileName")
		onlineFile.write "000001******* Dynamically Generated from $member for Onlines ***********${System.getProperty('line.separator')}"
		batchFile.write  "000001******* Dynamically Generated from $member for Batch *************${System.getProperty('line.separator')}"
		
		new File("${properties.workDir}/${file}").eachLine { line, nb ->
			def switchChar = line[6..6]
			
			switch (switchChar) {
				case "${properties.DualCompileDeleteLineBuildOnline}":
					line = line.replaceFirst(/${properties.DualCompileDeleteLineBuildOnline}/," ")
					//println("Onlne $nb -- $line --")
					onlineFile.append "$line${System.getProperty('line.separator')}"
					break
				case "${properties.DualCompileDeleteLineBuildBatch}":
					line = line.replaceFirst(/${properties.DualCompileDeleteLineBuildBatch}/," ")
					//println("Batch $nb -- $line --")
					batchFile.append "$line${System.getProperty('line.separator')}"
					break
				default:
					//println("Both  $nb -- $line --")	
					onlineFile.append "$line${System.getProperty('line.separator')}" 
					batchFile.append "$line${System.getProperty('line.separator')}"
					break
			}
		}
		
		// create collection if needed
		def repositoryClient = tools.getDefaultRepositoryClient()
		if (!repositoryClient.collectionExists(properties.collection))
			repositoryClient.createCollection(properties.collection)
			
		//println("** Scan the build list to collect dependency data")
		def scanner = new DependencyScanner()
		def logicalFiles = [] as List<LogicalFile>
		def buildList = ["$onlineFileName", "$batchFileName"]
		
		buildList.each { buildFile ->
			def scanFile = "$buildFile"
			//println("Scanning $scanFile in directory ${properties.workDir}")
			def logicalFile = scanner.scan(scanFile, properties.workDir)
			
			logicalFiles.add(logicalFile)
			
			if (logicalFiles.size() == 500) {
				//println("** Storing ${logicalFiles.size()} logical files in repository collection '$properties.collection'")
				repositoryClient.saveLogicalFiles(properties.collection, logicalFiles);
				//println(repositoryClient.getLastStatus())
				logicalFiles.clear()
			}
		}
	
		//println("** Storing remaining ${logicalFiles.size()} logical files in repository collection '$properties.collection'")
		repositoryClient.saveLogicalFiles(properties.collection, logicalFiles);
		//println(repositoryClient.getLastStatus())
		
		def cCompile = new CobolCompile()
		
		member = CopyToPDS.createMemberName(onlineFileName)
		def logFile = new File("${properties.workDir}/${member}.log}")
		def rc = cCompile.run([onlineFileName] as String[])
		//tools.updateBuildResult(file:"$member", rc:0, maxRC:0, log:logFile)
		
		member = CopyToPDS.createMemberName(batchFileName)
		logFile = new File("${properties.workDir}/${member}.log")
		rc = cCompile.run([batchFileName] as String[])
		//tools.updateBuildResult(file:"$member", rc:0, maxRC:0, log:logFile)
		
	}
}
