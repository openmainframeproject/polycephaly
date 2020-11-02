package com.zos.cics.groovy.utilities

import com.ibm.dbb.repository.*
import com.ibm.dbb.dependency.*
import com.ibm.dbb.build.*
import com.zos.groovy.utilities.*
import com.zos.java.utilities.*

/**
 * @author gedgingt
 * @version v4.0.0
 * Date 12/24/2018
 *
 * SPDX-License-Identifier: Apache-2.0 
 */
class CicsApiBuild {
	
	static main(args) {
	}
	
	/**
	 * @param args[0] = file = src/main/zOS/com/zos/cicsapi/masterDiversion.cicsapi
	 * 
	 * property files used
	 * 		properties.workDir
	 * 		properties.Encoding
	 * 		properties.cicsBuildFile
	 * 
	 * uses hardcoded buildOrder
	 * 		buildOrder = ["C2J","J2C"]
	 * 
	 * 	Runs two private methods
	 * 		buildJsonConf	
	 * 			creates input files and calls executeJsonBuild
	 * 			adds missing configuration params
	 * 				properties.workDir
	 * 				properties.ServiceName
	 *  			properties.C2J_PDSLIB
	 *  			properties.J2C_PDSLIB	
	 *  			properties.C2J_REQMEM
	 *  			properties.J2C_REQMEM
	 *  			properties.C2J_RESPMEM
	 *  			properties.J2C_RESPMEM
	 *  			properties.copybookPDS
	 *  			properties.URIprefix
	 *  			properties.URI
	 *  			properties.logFileSuffix
	 *  	
	 * 		executeJsonBuild
	 * 			execute the JSON build script and generates WSBIND and copybook files
	 * 			Properties used
	 * 				properties.JAVA_HOME
	 * 				properties.cicsVersion
	 * 				properties.workDir
	 * 				properties.ServiceName
	 * 				properties.cicsDir
	 * 				properties.cobol2json
	 * 				properties.json2cobol
	 * 
	 * 	output 
	 * 	ServiceName.C2J/J2C.properties.in	files
	 */
	public void run(args) {
		
		def file = args[0]
		// cicsBuildFile = src/main/zOS/com/zos/cicsapi/masterDiversion.cicsapi
		println("* Building $file using ${this.class.getName()}.groovy script")
		// define local properties
		def properties = BuildProperties.getInstance()
		properties.load(new File("${properties.workDir}/$file"),properties.Encoding)
		properties.cicsBuildFile = file

		def buildOrder = ["C2J","J2C"]
		def processCounter = 0
		
		println("** Invoking build scripts according to build order: ${buildOrder[1..-1].join(', ')}")
		buildOrder.each { processType ->
			this.buildJsonConf([processType] as String[])
			processCounter++ 
		}
		this.executeJsonBuild(file)
	}
	
	/**
	 * @param args[0] = file = src/main/zOS/com/zos/cicsapi/masterDiversion.cicsapi
	 * 		ToDo: Move configuration params into property file
	 * 		creates input files and calls executeJsonBuild
	 * 		adds missing configuration params
	 * 			BUNDLE
	 * 			CCSID
	 * 			CHAR-USAGE
	 * 			CHAR-VARYING
	 * 			CONTID
	 * 			DATA-TRUNCATION	
	 * 			DATETIME
	 * 			HTTPPROXY	
	 * 			HTTPPROXY-USERNAME
	 * 			HTTPPROXY-PASSWORD
	 * 			JSON-SCHEMA
	 * 			JSONTRANSFRM
	 * 			LANG
	 * 			MAPPING-LEVEL
	 * 			MINIMUM-RUNTIME-LEVEL
	 * 			PDSCP	
	 * 			PGMINT
	 * 			PGMNAME
	 * 			REQUEST-CHANNEL
	 * 			RESPONSE-CHANNEL
	 * 			STRUCTURE
	 * 			SYNCONRETURN
	 * 			TRANSACTION
	 * 			USERID
	 * 		properties used
	 * 			properties.workDir
	 * 			properties.ServiceName
	 *  		properties.C2J_PDSLIB
	 *  		properties.J2C_PDSLIB	
	 *  		properties.C2J_REQMEM
	 *  		properties.J2C_REQMEM
	 *  		properties.C2J_RESPMEM
	 *  		properties.J2C_RESPMEM
	 *  		properties.copybookPDS
	 *  		properties.URIprefix
	 *  		properties.URI
	 *  		properties.logFileSuffix
	 */
	private void buildJsonConf(args) {
		
		def processType = args[0]
		def properties = BuildProperties.getInstance()
		def propsWrite = null;
		def tempString = null;
		def line = null;
	
		println("* Running $processType using buildJsonConf for $processType")
		def propsInFile = "${properties.workDir}/${properties.ServiceName}${processType}.properties.in"
		//println ("propsInFile = $propsInFile")
		
		try {
	
			propsWrite = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(propsInFile)))
			
			def pdslibAttribute = properties.getProperty("${processType}_PDSLIB")
			def reqmemAttribute = properties.getProperty("${processType}_REQMEM")
			def respmemAttribute = properties.getProperty("${processType}_RESPMEM")
			
			propsWrite.write("/**  Generate File **/ \n")
			propsWrite.write("PDSLIB=${properties.copybookPDS} \n");
			propsWrite.write("REQMEM=${reqmemAttribute} \n");
			propsWrite.write("RESPMEM=${respmemAttribute} \n");
			
			if (properties.URIprefix != null) {
				propsWrite.write("URI=${properties.URIprefix}/${properties.URI} \n");
			} else {
				propsWrite.write("URI=${properties.URI} \n");
			}
			
			propsWrite.write("LOGFILE=${properties.workDir}/${properties.ServiceName}.${properties.logFileSuffix} \n")
			propsWrite.write("WSBIND=${properties.workDir}/${properties.ServiceName}.wsbind \n")
			
			tempString = "JSON-SCHEMA-REQUEST=${properties.workDir}/${properties.ServiceName}-REQ.json"
			if (tempString.length() < 72) {
				propsWrite.write("JSON-SCHEMA-REQUEST=${properties.workDir}/${properties.ServiceName}-REQ.json \n");
			} else {
				line = tempString.substring(0,71) + "*";
				propsWrite.write("$line \n");
				line = tempString.substring(72);
				propsWrite.write("$line \n");
			}
			
			tempString = "JSON-SCHEMA-RESPONSE=${properties.workDir}/${properties.ServiceName}-RESP.json"
			if (tempString.length() < 72) {
				propsWrite.write("JSON-SCHEMA-RESPONSE=${properties.workDir}/${properties.ServiceName}-RESP.json \n");
			} else {
				line = tempString.substring(0,71) + "*";
				propsWrite.write("$line \n");
				
				line = tempString.substring(72);
				propsWrite.write("$line \n");
			}
			
			/*
			 * check for Optional parms
			 */
			ArrayList<String> cmds = new ArrayList<String>();
				cmds.add("BUNDLE");
				cmds.add("CCSID");
				cmds.add("CHAR-USAGE");
				cmds.add("CHAR-VARYING");
				cmds.add("CONTID");
				cmds.add("DATA-TRUNCATION");
				cmds.add("DATETIME");
				cmds.add("HTTPPROXY");
				cmds.add("HTTPPROXY-USERNAME");
				cmds.add("HTTPPROXY-PASSWORD");
				cmds.add("JSON-SCHEMA");
				cmds.add("JSONTRANSFRM");
				cmds.add("LANG");
				cmds.add("MAPPING-LEVEL");
				cmds.add("MINIMUM-RUNTIME-LEVEL");
				cmds.add("PDSCP");
				cmds.add("PGMINT");
				cmds.add("PGMNAME");
				cmds.add("REQUEST-CHANNEL");
				cmds.add("RESPONSE-CHANNEL");
				cmds.add("STRUCTURE");
				cmds.add("SYNCONRETURN");
				cmds.add("TRANSACTION");
				cmds.add("USERID");
			
			for(String s:cmds){  
				// System.out.println(s);  
				if(properties.getProperty(s) != null) {
					propsWrite.write(s + "=" + properties.getProperty(s) + "\n");
				}
			}
		}
		catch(Exception ec) {
			System.out.println(ec.toString());
		}
		finally {
			propsWrite.close();
		}
	}
	
	/**
	 * @param args[0] = file = src/main/zOS/com/zos/cicsapi/masterDiversion.cicsapi
	 * 
	 * 		execute CICS Cobol to JSON script
	 * 			Format &PATHPREF/usr/lpp/cicsts/&USSDIR/lib/wsdl/DFHSC2LS &JAVADIR &USSDIR &TMPDIR./&TMPFILE. &SERVICE &PATHPREF $3.in'
	 * 		Properties used
	 * 			properties.JAVA_HOME
	 * 			properties.cicsVersion
	 * 			properties.workDir
	 * 			properties.ServiceName
	 * 			properties.cicsDir
	 * 			properties.cobol2json
	 * 			properties.json2cobol
	 * 
	 * 		Uses RunShell.executeShell method to execute the script on z/OS in EBCDIC
	 */
	private void executeJsonBuild(args) {
		
		def file = args[0]
		GroovyObject tools = (GroovyObject) Tools.newInstance()
		def properties = BuildProperties.getInstance()
		def runConfig = null
		
		def runShell = new RunShell()

		try {
			def rc = 0
			def parm1 = properties.JAVA_HOME
			def parm2 = properties.cicsVersion
			
			def logFile = "${properties.workDir}/${properties.ServiceName}.log"
			
			def parm3 = "${properties.workDir}/${properties.ServiceName}C2J.properties"
			//println("executing ${properties.cicsDir}/${properties.cobol2json} $parm1 $parm2 $parm3")
			rc = runShell.executeShell("sh ${properties.cicsDir}/${properties.cobol2json} $parm1 $parm2 $parm3")
			
			println(" file: $file, rc:$rc, maxRC:0, log:$logFile")
			// update build result
			tools.updateBuildResult(file:"$file", rc:rc, maxRC:4, log:logFile)
			
			parm3 = "${properties.workDir}/${properties.ServiceName}J2C.properties"
			//println("executing ${properties.cicsDir}/${properties.json2cobol} $parm1 $parm2 $parm3")
			rc = runShell.executeShell("sh ${properties.cicsDir}/${properties.json2cobol} $parm1 $parm2 $parm3")
			
			// update build result
			//tools.updateBuildResult(file:"$file", rc:rc, maxRC:4, log:logFile)
			
		}
		catch(Exception ec) {
			System.out.println(ec.toString());
			System.exit(1)
		}
		finally {
			
		}
		
	}
	
}
