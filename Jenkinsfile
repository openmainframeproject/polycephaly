pipeline {
    agent any

    options {
        timestamps()
    }
    
    environment {
    	basedir					= '.'
        PolycephalyProjectName 	= 'Polycephaly'
        mainCclassZosBuild    	= 'com.zos.groovy.utilities.ZosBuild'
        OtherProductsDir	  	= '/usr/lpp/tools/lib'
        DBBdir					= '/usr/lpp/IBM/dbb/lib'
        DBBGroovyDir			= '/usr/lpp/IBM/dbb/groovy-2.4.12/lib'
        confdir					= "${basedir}/conf"
        props = readProperties  file: "${confdir}/Global.properties"
    }
   
    stages {
    	stage("CheckOut")  {
    		options {
    			 timeout(time: 1, unit: "MINUTES")
    		}
    		steps {
    			checkout([$class: 'GitSCM', branches: [[name: '*/edge05/branch01']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'edge05', url: 'https://github.com/openmainframeproject/polycephaly.git']]])
    		}	 
		}
	   	stage("Initialize")  { 
	   		steps {
                echo 'zJenkins Lib Dir = "${zJenkins.lib.dir}"' 
            }
	 
		}
        stage('Java_Build') {
            steps {
                sh 'java --version'
            }
        }
        
        stage("Build") {
            options {
                timeout(time: 1, unit: "MINUTES")
            }
            steps {
                sh 'printf "\\e[31mSome code compilation here...\\e[0m\\n"'
                sh 'printenv'
            }
        }

        stage("Test") {
            options {
                timeout(time: 2, unit: "MINUTES")
            }
            steps {
                sh 'printf "\\e[31mSome tests execution here...\\e[0m\\n"'
            }
        }
    }
  	post {
		always {
	            	emailext body: 'A Test EMail', recipientProviders: [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']], subject: 'Test'
       	}
    }
}