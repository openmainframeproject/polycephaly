pipeline {
    agent { node { label 'zOS' } }

    options {
        timestamps()
    }
    
    environment {	
    
    	binDir				= 'bin'
    	classesDir			= 'classes'	
		binDirExists		= fileExists "${env.binDir}"
		classesDirExists	= fileExists "${env.classesDir}"
		srcJavaZosFile		= 'src/main/java/com/jenkins/zos/file'
		srcJavaZosUtil		= 'src/main/java/com/zos/java/utilities'
		srcZosResbiuld		= 'src/main/zOS/com.zos.resbuild'
		srcGroovyZosLang	= 'src/main/groovy/com/zos/language'
		srcGrovoyZosUtil	= 'src/main/groovy/com/zos/groovy/utilities'
		srcGroovyCICSutil	= 'src/main/groovy/com/zos/cics/groovy/utilities'
		javaHome			= '/usr/lpp/java/J8.0_64/bin'
		groovyHome			= '/u/jerrye/jenkins/groovy/bin'
		ibmjzos				= '/usr/lpp/java/J8.0_64/lib/ext/ibmjzos.jar'
		dbbcore				= '/opt/lpp/IBM/dbb/lib/dbb.core_1.0.6.jar'
		polycephalyJar		= '/polycephaly.jar'
		javaClassPath		= "${env.ibmjzos}:${env.dbbcore}"
		groovyClassPath		= "${env.javaClassPath}:${env.polycephalyJar}"
		

    }

    stages {
        stage('if directory bin exists'){
            when { expression { binDirExists == 'false' } }
            steps {
                echo "directory dist does not exist"
                sh "mkdir ${env.binDir}"
            }
        }
        stage('if directory classes exists'){
            when { expression { classesDirExists == 'false' } }
            steps {
                echo "directory classes does not exist"
                sh 'mkdir ${env.classesDir}'
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
                sh "${env.javaHome}/java -version"
            }
        }
        stage('Build zOS File utilities') {
            steps {
                sh "${env.javaHome}/javac -d ${env.classesDir} ${env.srcJavaZosFile}/*.java"
            }
        }
        stage('Build zOS Java utilities') {
            steps {
                sh "${env.javaHome}/javac -d ${env.classesDir} ${env.srcJavaZosUtil}/*.java"
            }
        }
        stage('Build zOS resbuild utlities') {
            steps {
                sh "${env.javaHome}/javac -cp .:${env.javaClassPath}  -d ${env.classesDir} ${env.srcZosResbiuld}/*.java"
            }
        }
        stage('Create Java Jar file') {
            steps {
                sh "${env.javaHome}/jar cvf ${env.binDir}/${env.polycephalyJar}-C ${env.classesDir} . "
            }
        }
        stage('Build CICS Groovy Utilities') {
            steps {
                sh "${env.groovyHome}/groovyc-1047 -cp .:${env.groovyClassPath}  -d ${env.classesDir} ${env.srcZosCICSutil}/*.groovy"
            }
        }
        stage('Add CICS Groovy Utilities to JAR') {
            steps {
                sh "${env.javaHome}/jar uf ${env.binDir}/${env.polycephalyJar} -C ${env.classesDir} . "
            }
        }
        stage('Build zOS Languages') {
            steps {
                sh "${env.groovyHome}/groovyc-1047 -cp .:${env.groovyClassPath}  -d ${env.classesDir} ${env.srcGroovyZosLang}/*.groovy"
            }
        }
        stage('Add Languages to JAR') {
            steps {
                sh "${env.javaHome}/jar uf ${env.binDir}/${env.polycephalyJar} -C ${env.classesDir} . "
            }
        }
        stage('Build zOS Groovy Utilities') {
            steps {
                sh "${env.groovyHome}/groovyc-1047 -cp .:${env.groovyClassPath}  -d classes ${env.srcGrovoyZosUtil}/*.groovy"
            }
        }
        stage('Add z/OS Groovy Utilities to JAR') {
            steps {
                sh "${env.javaHome}/jar uf ${env.binDir}/${env.polycephalyJar} -C ${env.classesDir} . "
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
  