pipeline {
    agent { node { label 'zOS01' } }

    options {
        timestamps()
    }
    
    environment {
		polydir = '/opt/lpp/polycephaly/bin'
    }
   
    stages {
    	stage("Environment-before Checkout")  {
            steps {
                sh 'printenv'
            // send to email
	  			emailext (
	      			subject: "STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
	      			body: """<p>STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
	        		<p>Check console output at &QUOT;<a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>&QUOT;</p>""",
	      			recipientProviders: [[$class: 'DevelopersRecipientProvider']]
	    		)
            }	 
		}
    	stage("CheckOut")  {
    		options {
    			 timeout(time: 1, unit: "MINUTES")
    		}
    		steps {
    			checkout([$class: 'GitSCM', branches: [[name: '*/edge05/branch01']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'edge05', url: 'https://github.com/openmainframeproject/polycephaly.git']]])
    		}	 
		}
		stage("Environment-after Checkout")  {
            steps {
                sh 'printenv'
            }	 
		}

        stage('Java_Build') {
            steps {
                sh 'java -version'
            }
        }
        
        stage("Build") {
            options {
                timeout(time: 1, unit: "MINUTES")
            }
            steps {
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
       	}
    }
}