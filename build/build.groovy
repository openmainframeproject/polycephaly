import com.zos.groovy.utilities.*
import com.ibm.dbb.build.*
import com.zos.java.utilities.*


	def GroovyObject zBuild = (GroovyObject) ZosAppBuild.newInstance()
	// parse command line arguments and load build properties
	def usage = "build.groovy [options] buildfile"
 	def build = zBuild.execute(args, usage)
	
	 // define local properties
	 //def properties = BuildProperties.getInstance()
	 
	 //def subJob = new SubmitJob()
	 //def subPDS = "${properties.jclPDS}(TESTJOB)"
	 //subJob.sub("//'$subPDS'")
	 
	 
