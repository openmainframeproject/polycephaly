pipeline {
    agent { node { label 'zOS' } }

    options {
        timestamps()
    }
    
    environment {		
		binDir				= fileExists 'bin'
		classesDir			= fileExists 'classes'
		srcJavaZosFile		= 'src/main/java/com/jenkins/zos/file'
		javaHome			= '/usr/lpp/java/J8.0_64/bin/'
		groovyHome			= '/u/jerrye/jenkins/groovy/bin/'
		ibmjzos				= '/usr/lpp/java/J8.0_64/lib/ext/ibmjzos.jar'
		dbbcore				= '/opt/lpp/IBM/dbb/lib/dbb.core_1.0.6.jar'
		polycephalyJar		= 'bin/polycephaly.jar'
		javaClassPath		= "$CLASSPATH:${env.ibmjzos}:${env.dbbcore}"
		groovyClassPath		= "$CLASSPATH:${env.javaClassPath}:${env.polycephalyJar}"
		

    }

    stages {
        stage('if directory bin exists'){
            when { expression { binDir == 'false' } }
            steps {
                echo "directory dist does not exist"
                sh 'mkdir bin'
            }
        }
        stage('if directory classes exists'){
            when { expression { classesDir == 'false' } }
            steps {
                echo "directory classes does not exist"
                sh 'mkdir classes'
            }
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
		stage('Test using variables') {
            steps {
                echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}"
                sh 'env' 
            }
        }
        stage('Build zOS File utilities') {
            steps {
                sh '/usr/lpp/java/J8.0_64/bin/javac -d classes src/main/java/com/jenkins/zos/file/*.java'
            }
        }
        stage('Build zOS Java utilities') {
            steps {
                sh '/usr/lpp/java/J8.0_64/bin/javac -d classes src/main/java/com/zos/java/utilities/*.java'
            }
        }
        stage('Build zOS resbuild utlities') {
            steps {
                sh '/usr/lpp/java/J8.0_64/bin/javac -cp .:/usr/lpp/java/J8.0_64/lib/ext/ibmjzos.jar:/opt/lpp/IBM/dbb/lib/dbb.core_1.0.6.jar  -d classes src/main/zOS/com.zos.resbuild/*.java' 
            }
        }
        stage('Create Java Jar file') {
            steps {
                sh '/usr/lpp/java/J8.0_64/bin/jar cvf bin/polycephaly.jar -C classes . '
            }
        }
        stage('Build CICS Groovy Utilities') {
            steps {
                sh '/u/jerrye/jenkins/groovy/bin/groovyc-1047 -cp .:/usr/lpp/java/J8.0_64/lib/ext/ibmjzos.jar:/opt/lpp/IBM/dbb/lib/dbb.core_1.0.6.jar:./bin/polycephaly.jar -d classes src/main/groovy/com/zos/cics/groovy/utilities/*.groovy' 
            }
        }
        stage('Add CICS Groovy Utilities to JAR') {
            steps {
                sh '/usr/lpp/java/J8.0_64/bin/jar uf bin/polycephaly.jar -C classes . '
            }
        }
        stage('Build zOS Languages') {
            steps {
                sh '/u/jerrye/jenkins/groovy/bin/groovyc-1047 -cp .:/usr/lpp/java/J8.0_64/lib/ext/ibmjzos.jar:/opt/lpp/IBM/dbb/lib/dbb.core_1.0.6.jar:./bin/polycephaly.jar  -d classes src/main/groovy/com/zos/language/*.groovy' 
            }
        }
        stage('Add Languages to JAR') {
            steps {
                sh '/usr/lpp/java/J8.0_64/bin/jar uf bin/polycephaly.jar -C classes . '
            }
        }
        stage('Build zOS Groovy Utilities') {
            steps {
                sh '/u/jerrye/jenkins/groovy/bin/groovyc-1047 -cp .:/usr/lpp/java/J8.0_64/lib/ext/ibmjzos.jar:/opt/lpp/IBM/dbb/lib/dbb.core_1.0.6.jar:./bin/polycephaly.jar -d classes src/main/groovy/com/zos/groovy/utilities/*.groovy' 
            }
        }
        stage('Add z/OS Groovy Utilities to JAR') {
            steps {
                sh '/usr/lpp/java/J8.0_64/bin/jar uf bin/polycephaly.jar -C classes . '
            }
        }

        stage("Test") {
            options {
                timeout(time: 2, unit: "MINUTES")
            }
            steps {
                sh 'printf "\\Some tests execution here...\\e[0m\\n"'
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
  