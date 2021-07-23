package com.zos.groovy.utilities

import com.ibm.dbb.build.*
import com.ibm.dbb.build.report.*
import com.ibm.dbb.build.html.*
import com.ibm.dbb.repository.*
import com.ibm.dbb.dependency.*
import groovy.io.FileType
import groovy.transform.Field
import groovy.time.*

/**
* @author gedgingt
* @version v4.0.0
* Date 12/24/2018
*
* SPDX-License-Identifier: Apache-2.0
*/

/**
 * This is the main build script for the Mortgage Application.
 *
 * usage: build.groovy [options] buildfile
 *
 * buildFile:  Relative path (from sourceDir) of the file to build. If file
 * is *.txt then assumed to be buildlist file containing a list of relative
 * path files to build. Build list file can be absolute or relative (from
 * sourceDir) path.
 *
 * options:
 *  -b,--buildHash <hash>         Git commit hash for the build
 *  -B,--buildDir <dir>           Name of the Project Build directory
 *  -c,--collection <name>        Name of the dependency data collection
 *  -C, --clean                   Deletes the dependency collection and build result group
 *                                from the DBB repository then terminates (skips build)
 *  -D,--projectConfDir <dir>	  Name of the Project Config directory
 *  -e,--logEncoding <encoding>   Encoding of output logs. Default is EBCDIC
 *  -E,--errPrefix <uniqueId>     Unique id used for IDz error message datasets
 *  -h,--help                     Prints this message
 *  -i,--id <id>                  DBB repository id
 *  -p,--pw <password>            DBB password
 *  -P,--pwFile <file>            Absolute path to file containing DBB password
 *  -q,--hlq <hlq>                High level qualifier for partition data sets
 *  -r,--repo <url>               DBB repository URL
 *  -s,--sourceDir <dir>          Absolute path to source directory
 *  -t,--team <hlq>               Team build hlq for user build syslib concatenations
 *  -u,--userBuild                Flag indicating running a user build
 *  -w,--workDir <dir>            Absolute path to the build output directory
 *  -,--workDir <dir>             Absolute path to the build output directory
 *  -X,--confDir <dir>            Name of the Polycephaly Config directory
 *
 * All command line options can be set in the MortgageApplication/build/build.properties file
 * that is loaded at the beginning of the build. Use the argument long form name for the property's
 * name. The properties in build.properties are used as default property values and can be
 * overridden by command line options.
 */

class Tools {

	static main(args) {
	}

	def parseArgs(String[] cliArgs, String usage) {
		def cli = new CliBuilder(usage: usage)
		cli.b(longOpt:'buildHash', args:1, argName:'hash', 'Git commit hash for the build')
		cli.B(longOpt:'buildDir', args:1, argName:'dir', 'directory path to the build.groovy startup script')
		cli.c(longOpt:'collection', args:1, argName:'name', 'Name of the dependency data collection')
		cli.C(longOpt:'clean', 'Deletes the dependency collection and build reeult group from the DBB repository then terminates (skips build)')
		cli.D(longOpt:'projectConfDir', args:1, argName:'dir', 'directory path to the configuration directory')
		cli.e(longOpt:'logEncoding', args:1, argName:'encoding', 'Encoding of output logs. Default is EBCDIC')
		cli.E(longOpt:'errPrefix', args:1, argName:'errorPrefix', 'Unique id used for IDz error message datasets')
		cli.h(longOpt:'help', 'Prints this message')
		//cli.i(longOpt:'id', args:1, argName:'id', 'DBB repository id')
		//cli.p(longOpt:'pw', args:1, argName:'password', 'DBB password')
		//cli.P(longOpt:'pwFile', args:1, argName:'file', 'Absolute or relative (from sourceDir) path to file containing DBB password')
		cli.q(longOpt:'hlq', args:1, argName:'hlq', 'High level qualifier for partition data sets')
		cli.r(longOpt:'repo', args:1, argName:'url', 'DBB repository URL')
		cli.s(longOpt:'sourceDir', args:1, argName:'dir', 'Absolute path to source directory')
		cli.t(longOpt:'team', args:1, argName:'hlq', 'Team build hlq for user build syslib concatenations')
		cli.u(longOpt:'userBuild', 'Flag indicating running a user build')
		cli.w(longOpt:'workDir', args:1, argName:'dir', 'Absolute path to the build output directory')
		cli.X(longOpt:'confDir', args:1, argName:'dir', 'directory path to the polycephaly configuration directory')
		cli.Z(longOpt:'debug', 'turn on Debugging')

		def opts = cli.parse(cliArgs)
		if (opts.h) { // if help option used, print usage and exit
		 	cli.usage()
			System.exit(0)
		}

	    return opts
	}

	def loadProperties(OptionAccessor opts) {
		// check to see if there is a ./build.properties to load
		//println("*** running in loadProperties ***")

		// check to see if there is a ./build.properties to load
		def properties = BuildProperties.getInstance()

		// set command line arguments
		if (opts.s) properties.sourceDir = opts.s
		if (opts.w) properties.workDir = opts.w
		if (opts.b) properties.buildHash = opts.b
		if (opts.q) properties.hlq = opts.q
		if (opts.c) properties.collection = opts.c
		if (opts.t) properties.team = opts.t
		if (opts.e) properties.logEncoding = opts.e
		if (opts.E) properties.errPrefix = opts.E
		if (opts.u) properties.userBuild = "true"

		// override new default properties
		if (opts.r) properties.'dbb.RepositoryClient.url' = opts.r
		//if (opts.i) properties.'dbb.RepositoryClient.userId' = opts.i
		//if (opts.p) properties.'dbb.RepositoryClient.password' = opts.p
		//if (opts.P) properties.'dbb.RepositoryClient.passwordFile' = opts.P
		if (opts.B) properties.buildDir = opts.B
		if (opts.X) properties.confDir = opts.X
		if (opts.D) properties.projectConfDir = opts.D
		if (opts.Z) properties.debug = "true"

		if (properties.debug) {
			System.getenv().each { property, value -> println "$property = $value"}
		}

		// add build hash if not specific
		//if (!opts.b && !properties.userBuild) {
		//	properties.buildHash = getCurrentGitHash() as String
		//}

		if (properties.debug) println("java.version="+System.getProperty("java.runtime.version"))
		if (properties.debug) println("java.home="+System.getProperty("java.home"))
		if (properties.debug) println("user.dir="+System.getProperty("user.dir"))

		// One may also use YAML files as an alternative to properties files (DBB 1.0.6 and later):
		//     def buildPropFile = new File("${getScriptDir()}/build.yaml")
		// def buildPropFile = new File("${getScriptDir()}/build.properties")
		//if (buildPropFile.exists())
		//	   BuildProperties.load(buildPropFile)
			
		/* 
		 * get the Polcephaly Build Directory and File location
		 * Load Polycephaly properties
		 */
		def properties.PolycephalyBuildDirectory = System.getenv("PolycephalyBuildDirectory")
		if (properties.PolycephalyBuildDirectory == null) 
			new exception('Polycephaly Build Directory missing')
			
		def properties.PolycephalyBuildFile = System.getenv("PolycephalyBuildFile")
		if (properties.PolycephalyBuildFile == null)
			new exception('Polycephaly Build File missing')
			
		def buildPolycephalyPropFile = new File("properties.PolycephalyBuildDirectory/properties.PolycephalyBuildFile")
		if (properties.debug) println("buildPolycephalyPropFile = $buildPolycephalyPropFile")
		if (buildPolycephalyPropFile.exists()) {
		   properties.load(new File("${buildPolycephalyPropFile}"))
	    }

		if (properties.debug) {
			System.getenv().each { property, value -> println "$property = $value"}
		}
		
		/*
		 * get the Project Build Directory and File location
		 * Load Project properties
		 */
		properties.ProjectBuildDirectory = System.getenv("ProjectBuildDirectory")
		if (properties.ProjectBuildDirectory == null)
			new exception('Project Build Directory missing')
			
		properties.ProjectBuildFile = System.getenv("ProjectBuildFile")
		if (properties.ProjectBuildFile == null)
			new exception('Project Build File missing')
			
		buildProjectPropFile = new File("properties.ProjectBuildDirectory/properties.ProjectBuildFile")
		if (properties.debug) println("buildProjectPropFile = $buildProjectPropFile")
		if (buildProjectPropFile.exists()) {
		   properties.load(new File("${buildProjectPropFile}"))
		}

		if (properties.debug) {
			System.getenv().each { property, value -> println "$property = $value"}
		}
		/*
		 * 
		 */
		
		exit 1
		
		if (properties.debug) println("properties.workDir = $properties.workDir")
		def workDir = new File("$properties.workDir")
		if (properties.debug) println("workDir = $workDir")

		def projectConfDir = null
		if (properties.projectConfDir == null) {
			projectConfDir = new File("$properties.workDir/conf")
		} else {
			projectConfDir = new File("$properties.projectConfDir")
		}
		if (properties.debug) println("projectConfDir = $projectConfDir")



		properties.'dbb.RepositoryClient.passwordFile' = properties.confDir + '/ADMIN.pw'
		//println("dbb.RepositoryClient.passwordFile = $properties.'dbb.RepositoryClient.passwordFile'")

		// def buildDir = new File(scriptDir).getParent() + properties.buildDir
		// properties.buildDir = buildDir
		// if (properties.debug) println("buildDir = $buildDir")

		def buildPropFile = new File("properties.confDir/properties.buildProps.trim()")
		if (properties.debug) println("buildPropFile = $buildPropFile")
		if (buildPropFile.exists()) {
		   properties.load(new File("${buildPropFile}"))
	   }

		if (properties.debug) {
			System.getenv().each { property, value -> println "$property = $value"}
		}

	   def propsFiles = Eval.me(properties.PropsFiles)

	   if (propsFiles == null) {
			 println("Script text to compile cannot be null!")
			 throw new IllegalArgumentException("Script text to compile cannot be null!")
	   } else {
		   //println("starting to process the property files ")
		   propsFiles.each { file ->
		       if (properties.debug) println("loading property file = $confDir/${file}")
		       properties.load(new File("$confDir/${file}"))
		   }
		}

		println("loading $projectConfDir/${properties.collection}.properties")
		properties.load(new File("$projectConfDir/${properties.collection}.properties"))
		properties.buildNodeName = 'PolycephalyBuildName'

		if(properties.devHLQ == null) {
			properties.devHLQ = "${properties.datasetPrefix}.${properties.ProjectName}.${properties.buildNodeName}".toString()
			if (properties.debug) println("setting ddioName = default ${properties.ddioName}")
		}
		if(properties.ddioName == null) {
			properties.ddioName = "${properties.datasetPrefix}.${properties.ProjectName}.${properties.buildNodeName}.DDIO".toString().toUpperCase()
			if (properties.debug) println("setting ddioName = default ${properties.ddioName}")
		} else {
			properties.ddioName = "${properties.ddioName}".toString().toUpperCase()
		}
		if (properties.debug) println("********************** all properties have been loaded  ***************************************")
		if (properties.debug) {
			System.getenv().each { property, value -> println "$property = $value"}
		}
		if (properties.debug) println("************************************************************************************************")

		if (properties.collection == null) {
			properties.collection = 'Polycephaly'
			println (" Running $properties.collection Collection")
		} else {
			println (" Running $properties.collection Collection")
		}

		// handle --clean option
		if (opts.C)  {
			def startTime = new Date()
			println()
			println("*****************************************************************************************************")
			println("                    Project clean started at $startTime ")
			println("*****************************************************************************************************")
			def repo = getDefaultRepositoryClient()

			println("* Deleting dependency collection ${properties.collection}")
			repo.deleteCollection(properties.collection)

			println("* Deleting build result group ${properties.collection}Build")
			repo.deleteBuildResults("${properties.collection}Build")

			def endTime = new Date()
			def duration = TimeCategory.minus(endTime, startTime)

			println()
			println("*****************************************************************************************************")
			println("                    Project clean finished at $endTime  ")
			println("*****************************************************************************************************")
			
			System.exit(0)
		}

		return properties
	}

	def validateRequiredProperties(List<String> props) {
		def properties = BuildProperties.getInstance()
		props.each { prop ->
			// handle password special case i.e. can have either password or passwordFile
			if (prop.equals("password")) {
				if (!(properties.'dbb.RepositoryClient.password' || properties.'dbb.RepositoryClient.passwordFile')) {
					 assert properties.'dbb.RepositoryClient.password' : "Missing property 'dbb.RepositoryClient.password'"
					  assert properties.'dbb.RepositoryClient.passwordFile' : "Missing property 'dbb.RepositoryClient.passwordFile'"
			   }
			}
			else {
				assert properties."$prop" : "Missing property $prop"
			}
		}
	}

	def getBuildList(List<String> args) {
		if (properties.debug) println("*** running in getBuildList ***")
	    def properties = BuildProperties.getInstance()
	    def files = []
		// Set the buildFile or buildList property
		if (args) {
			def buildFile = args[0]
		    if (buildFile.endsWith(".txt")) {
				if (buildFile.startsWith("/"))
					properties.buildListFile = buildFile
			      else
					//properties.buildListFile = "$properties.workDir/$buildFile".toString()
					properties.buildListFile = "$properties.sourceDir/$buildFile".toString()
		  	}
		    else {
				properties.buildFile = buildFile
		    }
		}

		if (properties.buildFile) {
			files = [properties.buildFile]
		}
		// else check to see if a build list file was passed in
		else if (properties.buildListFile) {
		    files = new File(properties.buildListFile) as List<String>
		}
		else {
	    	files = new File("$properties.workDir/$properties.BuildList")
		//	files = new File("$properties.sourceDir/$properties.BuildList")
		}
		if (properties.debug) println("*** properties.buildFile = ${properties.buildFile} ***")

		def tempFileList = new File("${properties.workDir}/tempFileList.txt")
        def GenericFileListFound = false
		if (properties.debug) println("*** files = $files ***")
        files.eachLine('ibm-1047') { line ->
			if (properties.debug) println("line = $line")
            if (line.contains("*")) {
				GenericFileListFound = true
				if (properties.debug) println("Generic Found = $line")
                def fileDirectory = line[0..<line.lastIndexOf('*')]
				if (properties.debug) println("fileDirectory = $fileDirectory")
                def dir = new File("${properties.'src.zOS.dir'}/$fileDirectory")
				if (properties.debug) println("dir = $dir")
                def stripNumber = "${properties.'src.zOS.dir'}".size()+1
				if (properties.debug) println("stripNumber = $stripNumber")
                dir.eachFileRecurse(FileType.FILES) {  file ->
					if (properties.debug) println("file = $file")
                    file = file.toString().stripIndent(stripNumber)
                    tempFileList.append "$file${System.getProperty('line.separator')}"
                }
            } else {
                tempFileList.append "$line${System.getProperty('line.separator')}"
				if (properties.debug) println("tempFileList = $tempFileList")
            }
        }

		/*
        *   if No Generic entries
        *       files = /usr/lpp/tools/jenkins/workspace/zTech/conf/package.txt
        *   Else
        *       files = /usr/lpp/tools/jenkins/workspace/zTech/tempFileList.txt
        *
        */
        if (GenericFileListFound) {
            files = tempFileList
        }
		if (properties.debug) println("returning files = $files")
		return files
	}

	def createDatasets(Map args) {
	    def properties = BuildProperties.getInstance()
		if (properties.debug) println("*** running in createDatasets ***")
		args.suffixList.each { LLQ ->
			def dataset = "${properties.devHLQ}.$LLQ"
			LLQ = LLQ.toLowerCase().trim()
			properties."${LLQ}PDS" = dataset.toString()
			if (properties.debug) println("creating $dataset with ${args.suffixOpts} assigned properties.${LLQ}PDS ")
			new CreatePDS().dataset("$dataset").options(args.suffixOpts).create()
		}

		if (properties.errPrefix) {
	    	new CreatePDS().dataset("${properties.devHLQ}.${properties.errPrefix}.${properties.xmlPDSsuffix}").options(properties.xmlOptions).create()
		}

	}

	def getDefaultRepositoryClient() {
		def properties = BuildProperties.getInstance()
		if (properties.debug) println("*** running in getDefaultRepositoryClient ***")
		def repositoryClient = new RepositoryClient().forceSSLTrusted(true)
		return repositoryClient
	}

	def initializeBuildArtifacts() {
	    BuildReportFactory.createDefaultReport()
	    def properties = BuildProperties.getInstance()
		if (properties.debug) println("*** running in initializeBuildArtifacts ***")
	    if (!properties.userBuild) {
	        def repo = getDefaultRepositoryClient()
	        properties.buildGroup = "${properties.collection}" as String
	        properties.buildLabel = "build.${properties.startTime}" as String
	        def buildResult = repo.createBuildResult(properties.buildGroup, properties.buildLabel)
	        buildResult.setState(buildResult.PROCESSING)
	        if (properties.buildHash)
	            buildResult.setProperty("buildHash", properties.buildHash)
	        buildResult.save()
	        if (properties.debug) println("** Build result created at ${buildResult.getUrl()}")
	    }
	}

	def getBuildResult() {
	    def properties = BuildProperties.getInstance()
		if (properties.debug) println("*** running in getBuildResult ***")
	    def buildResult = null
	    if (!properties.userBuild) {
	        def repo = getDefaultRepositoryClient()
	        buildResult = repo.getBuildResult(properties.buildGroup, properties.buildLabel)
	    }
	    return buildResult
	}

	def generateBuildReport() {
	    def properties = BuildProperties.getInstance()
		if (properties.debug) println("*** running in generateBuildReport ***")
	    def jsonOutputFile = new File("${properties.workDir}/BuildReport.json")
	    def htmlOutputFile = new File("${properties.workDir}/BuildReport.html")

		// create build report data file
		def buildReportEncoding = "UTF-8"
		def buildReport = BuildReportFactory.getBuildReport()
		buildReport.save(jsonOutputFile, buildReportEncoding)

		// create build report html file
		def htmlTemplate = null  // Use default HTML template.
		def css = null       // Use default theme.
		def renderScript = null  // Use default rendering.
		def transformer = HtmlTransformer.getInstance()
		transformer.transform(jsonOutputFile, htmlTemplate, css, renderScript, htmlOutputFile, buildReportEncoding)

		return [ jsonOutputFile, htmlOutputFile ]
	}

	def getDefaultDependencyResolver(String file) {
	    def properties = BuildProperties.getInstance()
		if (properties.debug) println("*** running in getDefaultDependencyResolver ***")
		def path = new DependencyPath().sourceDir(properties.workDir).directory("${properties.sourceDir}/${properties.copybookPackage}")
		def rule = new ResolutionRule().library("SYSLIB").path(path)
		//def resolver = new DependencyResolver().sourceDir(properties.sourceDir).file(file).rule(rule)
	    def resolver = new DependencyResolver().sourceDir(properties.workDir).file(file).rule(rule)
	    if (properties.userBuild)
	    	resolver.setScanner(new DependencyScanner())
	    else {
	        path.setCollection(properties.collection)
	        resolver.setCollection(properties.collection)
	        resolver.setRepositoryClient(getDefaultRepositoryClient())
	    }
	    return resolver
	}

	def getDefaultImpactResolver(String file) {
		def properties = BuildProperties.getInstance()
		if (properties.debug) println("*** running in getDefaultImpactResolver ***")
	   	def path = new DependencyPath().sourceDir(properties.workDir).directory("${properties.sourceDir}/${properties.copybookPackage}")
	   	def rule = new ResolutionRule().library("SYSLIB").path(path)
	   	def resolver = new ImpactResolver().repositoryClient(getDefaultRepositoryClient()).collection(properties.collection).rule(rule).file(file)
	   	return resolver
	}

	def updateBuildResult(Map args) {
	    def properties = BuildProperties.getInstance()
		if (properties.debug) println("*** running in updateBuildResult ***")
	    def error = args.rc > args.maxRC
	    def errorMsg = null
	    if (error) {
	        errorMsg = "*! The return code (${args.rc}) for ${args.file} exceeded the maximum return code allowed (${args.maxRC})"
	    	println(errorMsg)
	    	properties.error = "true"
	    }

	    if (!properties.userBuild) {
	    	def buildResult = getBuildResult()
	    	def member =  CopyToPDS.createMemberName(args.file)
			if (error) {
				buildResult.setStatus(buildResult.ERROR)
				buildResult.addProperty("error", errorMsg)
				if (args.log != null)
					buildResult.addAttachment("${member}.${properties.logFileSuffix}", new FileInputStream(args.log))
			}
			buildResult.save()
		}
	}

	def finalizeBuildResult(Map args) {
		def properties = BuildProperties.getInstance()
		if (properties.debug) println("*** running in finalizeBuildResult ***")
		if (!properties.userBuild) {
			def buildResult = getBuildResult()
			buildResult.setBuildReport(new FileInputStream(args.htmlReport))
			buildResult.setBuildReportData(new FileInputStream(args.jsonReport))
			buildResult.setProperty("filesProcessed", String.valueOf(args.filesProcessed))
			buildResult.setState(buildResult.COMPLETE)
			buildResult.save()
		}
	}
}
