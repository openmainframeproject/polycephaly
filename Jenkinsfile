pipeline {
    agent { node { label 'zOS' } }

    options {
        timestamps()
    }
    
    environment {
		polydir 	= '/opt/lpp/polycephaly/bin'
		ddbDir  	= '/opt/lpp/IBM/dbb/lib'
		srcJavaDir 	= 'src/main/java'
		MY_FILE = fileExists '/tmp/myfile'
    }

    stages {
        stage('conditional if exists'){
            when { expression { MY_FILE == 'true' } }
            steps {
                echo "file exists"
            }
        }
        stage('conditional if not exists'){
            when { expression { MY_FILE == 'false' } }
            steps {
                echo "file does not exist"
            }
        }
    	stage('Checkout') {
            cleanDir(env.WORKSPACE)
            checkoutComponents(env.COMPONENTS)
            configuration = getConfiguration('BuildConfiguration.json')
        }
        
	    stage ('Start') {
	      steps {
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
		
        stage('Java_Build') {
            steps {
                echo 'Hello, JDK'
                sh '/usr/lpp/java/J8.0_64/bin/java -version'
                sh '/usr/lpp/java/J8.0_64/bin/javac -verbose -classpath ${dbbDir}/*.jar -sourcepath ${srcJavaDir} -d classes ${srcJavaDir}/**/*.jar'
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
        stage("Deploy") {
            options {
                timeout(time: 2, unit: "MINUTES")
            }
            steps {
                sh 'printf "\\e[31m Deploy package...\\e[0m\\n"'
            }
        }
    }
	post {
	    success {

	
	      emailext (
	          subject: "SUCCESSFUL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
	          body: """<p>SUCCESSFUL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
	            <p>Check console output at &QUOT;<a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>&QUOT;</p>""",
	          recipientProviders: [[$class: 'DevelopersRecipientProvider']]
	        )
	    }
	
	    failure {

	      emailext (
	          subject: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
	          body: """<p>FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
	            <p>Check console output at &QUOT;<a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>&QUOT;</p>""",
	          recipientProviders: [[$class: 'DevelopersRecipientProvider']]
	        )
	    }
    }
}
  