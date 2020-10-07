pipeline {

	environment {
    	USE_JDK = 'true'
    	JavaHome = '/usr/lpp/java/J8.0_64'
    	zJenkinsProjectName = 'zJenkins'
    	ZosBuild = 'com.zos.groovy.utilities.ZosBuild'
    	Other_dir = '/usr/lpp/tools/lib'
    	ddb_dir = '/usr/lpp/IBM/dbb/lib'
    	dbb_groovy_dir = ' /usr/lpp/IBM/dbb/groovy-2.4.12/lib'
    	conf_dir = '${basedir}/conf'
    
  	}
	node ('zOS') { 
	
		stage ('Polycephaly - Checkout') {
	 	 checkout([$class: 'GitSCM', branches: [[name: '*/edge05/branch01']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'edge05', url: 'https://github.com/openmainframeproject/polycephaly.git']]]) 
		}
		stage ('Polycephaly - Build') {
		} 	
	
	    stage('PrintENV') {
	      steps {
	        sh 'printenv'
	        echo "conf_dir = ${conf_dir}" 
	      }
	    }
	}
}