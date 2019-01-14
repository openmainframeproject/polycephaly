/**
 * 
 */
package com.zos.groovy.utilities

import com.ibm.jzos.AccessMethodServices;
import com.ibm.jzos.CatalogSearch;
import com.ibm.jzos.ZFile;
import com.ibm.dbb.repository.*
import com.ibm.dbb.dependency.*
import com.ibm.dbb.build.*

/**
 * @author gedgingt
 *
 */
class Zutilities {

	static main(args) {

	}

	/**
	 * @param args[0] dataset to search the catalog for
	 * @param args[1] optional to write results to outfile
	 * @param args[2] optional write outfile into directory. Default parmlib source
	 * @param args[3] optional outfile fileType. Default properties.parmSuffix 
	 * 
	 * @param outFile properties.catalogSearchFile will contain the absolute path/file name
	 */
	public void catalogSearch(args) {
		// receive passed arguments
		def hlq = args[0].toString().toUpperCase()
		def catLookupPrintOnly = true
		def outFile
		def prefix 
		def fileSuffix
		def batchFile
		
		GroovyObject tools = (GroovyObject) Tools.newInstance()
		def properties = BuildProperties.getInstance()
		
		if (args[1] != null) {
			outFile = args[1]
			catLookupPrintOnly = false
		}
		 
		if (args[2] != null) {
			prefix = args[2]
		} else {
			prefix = "${properties.workDir}/${properties.'src.zOS.dir'}/${properties.'wsfg.parmlib'}"
		}
		
		if (args[3] != null) {
			fileSuffix = args[3]
		} else {
			fileSuffix = properties.parmSuffix
		}
		
		if (!catLookupPrintOnly) {
			def prefixDir = new File(prefix)
			if (!prefixDir.exists()) {
				prefixDir.mkdirs()
			}
			def filePrm = "$prefix/$outFile.$fileSuffix"
			//println("writing datasets out to $filePrm")
			properties.catalogSearchFile = filePrm.toString()
			batchFile = new File(filePrm)
			batchFile.write("*** Generated File for Catalog Search for $hlq **** ${System.getProperty('line.separator')}")
		}
		
		//println("running catalogSearch for $hlq")
		CatalogSearch catSearch = new CatalogSearch(hlq, 64000);
		catSearch.search();
		while (catSearch.hasNext()) {
			CatalogSearch.Entry entry = (CatalogSearch.Entry)catSearch.next();
			if (entry.isDatasetEntry()) {
				if (catLookupPrintOnly) {
					println("  ${entry.getName()}")
				} else {
					batchFile.append(" ${entry.getName()} ${System.getProperty('line.separator')}")
				}
			}
		}
	}
	/**
	 * @param args
	 */
	public void copyUnix(args) {
		
		// receive passed arguments
		String dumpfile  = args[0];
		String restfile  = args[1];
		String tempUnit  = args[2];

		println("* Building ${sysut2} using ${this.class.getName()}.groovy script")
		GroovyObject tools = (GroovyObject) Tools.newInstance()

		// define local properties
		def properties = BuildProperties.getInstance()
		//println("CopyToPDS = $file")
		def member = CopyToPDS.createMemberName(dumpfile)
		def logFile = new File("${properties.workDir}/${properties.adrdssuProgram}.log")
		
		tools.createDatasets(suffixList:["PARMLIB"], suffixOpts:"${properties.srcOptions}")
		new CreatePDS().dataset("${properties.parmlibPDS}").options(properties.srcOptions).create()

		/***
		 * ADRDSSU dump
		 */
			// copy program to PDS
			//println("Copying ${properties.workDir}/$file to $asmPDS($member)")
			new CopyToPDS().file(new File("${properties.workDir}/${properties.'src.zOS.dir'}/${properties.'wsfg.parmlib'}/$dumpfile.${properties.parmSuffix}")).dataset(properties.parmlibPDS).member(member).execute()
	
			// define the MVSExec command to compile the BMS map
			def dssdump = new MVSExec().file(file).pgm(properties.adrdssuProgram)
	
			// add DD statements to the compile command
			dssdump.dd(new DDStatement().name("SYSIN").dsn("${properties.parmlibPDS}($member)").options("shr"))
			dssdump.dd(new DDStatement().name("SYSPRINT").options(properties.tempCreateOptions))
			
			if (tempUnit != null) {
				dssdump.dd(new DDStatement().name("DD1").dsn("&&TEMP").options(properties.tempUssDiskOptions).pass(true))
			} else {
				dssdump.dd(new DDStatement().name("DD1").dsn("&&TEMP").options(properties.tempTapeDiskOptions).pass(true))
			}
	
			// add a copy command to the compile command to copy the SYSPRINT from the temporary dataset to an HFS log file
			dssdump.copy(new CopyToHFS().ddName("SYSPRINT").file(logFile).hfsEncoding(properties.logEncoding).append(true))
		
		/***
		 * Start ADRDSSU restore
		 */
			// copy program to PDS
			//println("Copying ${properties.workDir}/$file to $asmPDS($member)")
			new CopyToPDS().file(new File("${properties.workDir}/${properties.'src.zOS.dir'}/${properties.'wsfg.parmlib'}/$restfile.${properties.parmSuffix}")).dataset(properties.parmlibPDS).member(member).execute()
			
			// define the MVSExec command to compile the BMS map
			def dssrest = new MVSExec().file(restfile).pgm(properties.adrdssuProgram)
	
			// add DD statements to the compile command
			dssrest.dd(new DDStatement().name("DD1").dsn("&&TEMP").options("shr"))
			dssrest.dd(new DDStatement().name("SYSIN").dsn("${properties.parmlibPDS}($member)").options("shr"))
			dssrest.dd(new DDStatement().name("SYSPRINT").options(properties.tempCreateOptions))
			
			// add a copy command to the compile command to copy the SYSPRINT from the temporary dataset to an HFS log file
			dssrest.copy(new CopyToHFS().ddName("SYSPRINT").file(logFile).hfsEncoding(properties.logEncoding).append(true))
			

		// execute a simple MVSJob to handle passed temporary DDs between MVSExec commands
		def rc = new MVSJob().executable(dssdump).executable(dssrest).maxRC(0).execute()
		// update build result
		tools.updateBuildResult(file:"$restfile", rc:rc, maxRC:0, log:logFile)
	}
}
