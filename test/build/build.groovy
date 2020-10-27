import com.zos.groovy.utilities.*
import com.ibm.dbb.build.*
import com.zos.java.utilities.*

	GroovyObject zBuild = (GroovyObject) ZosAppBuild.newInstance()
 	def build = zBuild.execute(args)
	
	 // define local properties
	 //def properties = BuildProperties.getInstance()
	 
	 //def subJob = new SubmitJob()
	 //def subPDS = "${properties.jclPDS}(TESTJOB)"
	 //subJob.sub("//'$subPDS'")
	 
	 
	 