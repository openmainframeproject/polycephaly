import com.zos.groovy.utilities.ZosAppBuild
import com.ibm.dbb.build.BuildProperties
import com.zos.java.utilities.*

	GroovyObject zBuild = (GroovyObject) ZosAppBuild.newInstance()
 	def build = zBuild.execute()
	
	 // define local properties
	 def properties = BuildProperties.getInstance()
	 
	 //def subJob = new SubmitJob()
	 //def subPDS = "${properties.jclPDS}(TESTJOB)"
	 //subJob.sub("//'$subPDS'")
	 
	 
	 