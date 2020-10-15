/**
 * 
 */
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
 * SPDX-License-Identifier: Apache-2.0 
 */
class CicsWsBuild {

	static main(args) {
	}

	/**
	* @param args[0] = file = src/main/zOS/com/zos/cicsws/masterDiversion.cicsws
	*
	* property files used
	* 		properties.workDir
	* 		properties.Encoding
	* 		properties.cicsBuildFile
	*
	* uses hardcoded buildOrder
	* 		buildOrder = ["C2W","W2C"]
	*
	* 	Runs two private methods
	* 		buildWSConf
	* 			creates input files and calls executeJsonBuild
	* 			adds missing configuration params
	* 				properties.workDir
	* 				properties.ServiceName
	*  			properties.C2W_PDSLIB
	*  			properties.W2C_PDSLIB
	*  			properties.C2W_REQMEM
	*  			properties.W2C_REQMEM
	*  			properties.C2W_RESPMEM
	*  			properties.W2C_RESPMEM
	*  			properties.copybookPDS
	*  			properties.URIprefix
	*  			properties.URI
	*  			properties.logFileSuffix
	*
	* 		executeWSBuild
	*
	* 	output
	* 	ServiceName.C2J/J2C.properties.in	files
	*/
	
	public void run(args) {
		
		def file = args[0]
		println("* Building $file using ${this.class.getName()}.groovy script")
		
		// define local properties
		def properties = BuildProperties.getInstance()
		
		//println("cicsBuildFile = $file")
		// cicsBuildFile = src/main/zOS/com/zos/cicsws/helloworld.cicsws
		
		properties.load(new File("${properties.workDir}/$file"),properties.Encoding)
		properties.cicsBuildFile = file
		//println("Properties = $properties")

		def buildOrder = ["C2W","W2C"]
		def processCounter = 0
		
		println("** Invoking build scripts according to build order: ${buildOrder[1..-1].join(', ')}")
		buildOrder.each { processType ->
			this.buildWSConf([processType] as String[])
			processCounter++
		}
		this.executeWSBuild(file)
	}
	
	private void buildWSConf(args) {
		
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
			propsWrite.write("WSDL=${properties.workDir}/${properties.ServiceName}.wsdl \n")
			
			/*
			 * check for Optional parms
			 */
			ArrayList<String> cmds = new ArrayList<String>();
				cmds.add("ADDITIONAL-PROPERTIES");
				cmds.add("BINDING");
				cmds.add("CCSID");
				cmds.add("CHAR-MULTIPLIER");
				cmds.add("CHAR-OCCURS");
				cmds.add("CHAR-USAGE");
				cmds.add("CHAR-VARYING");
				cmds.add("CONTID");
				cmds.add("DATA-SCREENING");
				cmds.add("DATA-TRUNCATION");
				cmds.add("DATETIME");
				cmds.add("DEFAULT-CHAR-MAXLENGTH");
				cmds.add("DEFAULT-FRACTION-DIGITS");
				cmds.add("HTTPPROXY");
				cmds.add("HTTPPROXY-USERNAME");
				cmds.add("HTTPPROXY-PASSWORD");
				cmds.add("INLINE-MAXOCCURS-LIMIT");
				cmds.add("LANG");
				cmds.add("MAPPING-LEVEL");
				cmds.add("MINIMUM-RUNTIME-LEVEL");
				cmds.add("NAME-TRUNCATION");
				cmds.add("OPERATIONS");
				cmds.add("PDSCP");
				cmds.add("PGMINT");
				cmds.add("PGMNAME");
				cmds.add("REQUEST-CHANNEL");
				cmds.add("REQUEST-NAMESPACE");
				cmds.add("RESPONSE-CHANNEL");
				cmds.add("RESPONSE-NAMESPACE");
				cmds.add("SYNCONRETURN");
				cmds.add("SSL-KEYSTORE");
				cmds.add("SSL-KEYPWD");
				cmds.add("SSL-TRUSTSTORE");
				cmds.add("SSL-TRUSTPWD");
				cmds.add("TRANSACTION");
				cmds.add("URI");
				cmds.add("USERID");
				cmds.add("WSADDR-EPR-ANY");
				cmds.add("WIDE-COMP3");
				cmds.add("WSDL-SERVICE");
				cmds.add("WSRR-NAMESPACE");
				cmds.add("WSRR-PASSWORD");
				cmds.add("WSRR-SERVER");
				cmds.add("WSRR-USERID");
				cmds.add("WSRR-VERSION");
				cmds.add("XML-ONLY");
			
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
	
	private void executeWSBuild(args) {
		
		def file = args[0]
		
		GroovyObject tools = (GroovyObject) Tools.newInstance()
		def properties = BuildProperties.getInstance()
		def runConfig = null
		
		def runShell = new RunShell()
		/**
		 * Format &PATHPREF/usr/lpp/cicsts/&USSDIR/lib/wsdl/DFHSC2LS &JAVADIR &USSDIR &TMPDIR./&TMPFILE. &SERVICE &PATHPREF $3.in'
		 */

		try {
			def rc = 0
			def parm1 = properties.JAVA_HOME
			def parm2 = properties.cicsVersion
			
			def logFile = "${properties.workDir}/${properties.ServiceName}.log"
			
			def parm3 = "${properties.workDir}/${properties.ServiceName}C2W.properties"
			//println("executing ${properties.cicsDir}/${properties.cobol2soap} $parm1 $parm2 $parm3")
			rc = runShell.executeShell("sh ${properties.cicsDir}/${properties.cobol2soap} $parm1 $parm2 $parm3")
			
			println(" file: $file, rc:$rc, maxRC:0, log:$logFile")
			// update build result
			tools.updateBuildResult(file:"$file", rc:rc, maxRC:4, log:logFile)
			
			parm3 = "${properties.workDir}/${properties.ServiceName}W2C.properties"
			//println("executing ${properties.cicsDir}/${properties.soap2cobol} $parm1 $parm2 $parm3")
			rc = runShell.executeShell("sh ${properties.cicsDir}/${properties.soap2cobol} $parm1 $parm2 $parm3")
			
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
