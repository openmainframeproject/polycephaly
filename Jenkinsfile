pipeline {
    agent { node { label 'zOS' } }

    options {
        timestamps()
    }
    
    environment {	
    	binDir				= 'bin'
    	classesDir			= 'classes'	
		srcJavaZosFile		= 'src/main/java/com/jenkins/zos/file'
		srcJavaZosUtil		= 'src/main/java/com/zos/java/utilities'
		srcZosResbiuld		= 'src/main/zOS/com.zos.resbuild'
		srcGroovyZosLang	= 'src/main/groovy/com/zos/language'
		srcGrovoyZosUtil	= 'src/main/groovy/com/zos/groovy/utilities'
		srcGroovyPrgUtil	= 'src/main/groovy/com/zos/program/utilities'
		javaHome			= '/usr/lpp/java/J8.0_64/bin'
		groovyHome			= '/u/jerrye/jenkins/groovy/bin'
		groovyzHome			= '/opt/lpp/IBM/dbb/bin'
		ibmjzos				= '/usr/lpp/java/J8.0_64/lib/ext/ibmjzos.jar'
		dbbcore				= '/opt/lpp/IBM/dbb/lib/dbb.core_1.0.6.jar'
		dbbhtml				= '/opt/lpp/IBM/dbb/lib/dbb.html_1.0.6.jar'
		dbbJNI 				= '/opt/lpp/IBM/dbb/lib/libDBB_JNI64.so'
		polycephalyJar		= "${env.binDir}/polycephaly.jar"
		javaClassPath		= "${env.ibmjzos}:${env.dbbcore}:${env.dbbhtml}"
		groovyClassPath		= "${env.javaClassPath}:${env.polycephalyJar}"
		groovyLibPath		= "/opt/lpp/IBM/dbb/lib/*:${env.dbbJNI}:${env.groovyClassPath}"
		polyRuntime			= '/u/jerrye'
		
    }

    stages {
        stage('Clean workspace') {
            steps {
                cleanWs()
            }
        }
        stage ("Print env") {
            steps {
                //sh 'printenv'
                echo sh(script: 'env|sort', returnStdout: true)
            }          
      	}

	    stage ('Start') {
	      steps {
	        // send to email
	        emailext (
	            subject: "STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
	            body: """STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'>
	              Check console output at;<a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]""",
	            recipientProviders: [[$class: 'DevelopersRecipientProvider']]
	          )
	        }
	    }
    	stage("CheckOut")  {
    		steps {
    			checkout scm
    		}	 
		}
		stage('Create Directories'){
            steps {
                rc = sh(script: "mkdir ${env.binDir}", returnStatus: true)
                sh "mkdir ${env.classesDir}"
            }
        }
        stage('Build zOS utilities') {
            steps {
                sh "${env.javaHome}/javac -d ${env.classesDir} ${env.srcJavaZosFile}/*.java"
                sh "${env.javaHome}/javac -d ${env.classesDir} ${env.srcJavaZosUtil}/*.java"
                sh "${env.javaHome}/javac -cp .:${env.javaClassPath}  -d ${env.classesDir} ${env.srcZosResbiuld}/*.java"
            }
        }
        stage('Create Java Jar file') {
            steps {
                sh "${env.javaHome}/jar cvf ${env.polycephalyJar} -C ${env.classesDir} . "
            }
        }
        stage('Build Groovy zOS Utilities') {
            steps {
                sh "${env.groovyHome}/groovyc-1047 -cp .:${env.groovyClassPath}  -d ${env.classesDir} ${env.srcGrovoyZosUtil}/*.groovy"
            }
        }
        stage('Add Groovy ZOS Utilities to JAR') {
            steps {
                sh "${env.javaHome}/jar uf ${env.polycephalyJar} -C ${env.classesDir} . "
            }
        }
        stage('Build Groovy Language Utilities') {
            steps {
                sh "${env.groovyHome}/groovyc-1047 -cp .:${env.groovyClassPath}  -d ${env.classesDir} ${env.srcGroovyZosLang}/*.groovy"
            }
        }
        stage('Add Groovy Language Utilities to JAR') {
            steps {
                sh "${env.javaHome}/jar uf ${env.polycephalyJar} -C ${env.classesDir} . "
            }
        }
        stage('Build Groovy Program Utilities') {
            steps {
                sh "${env.groovyHome}/groovyc-1047 -cp .:${env.groovyClassPath}  -d ${env.classesDir} ${env.srcGroovyPrgUtil}/*.groovy"
            }
        }
        stage('Add Groovy Program Utilities to JAR') {
            steps {
                sh "${env.javaHome}/jar uf ${env.polycephalyJar} -C ${env.classesDir} . "
                sh "chmod 766 ${env.polycephalyJar}"
            }
        }
        stage("Test") {
            options {
                timeout(time: 2, unit: "MINUTES")
            }
            steps {
            	sh "export DBB_HOME=/opt/lpp/IBM/dbb"
            	sh "export export DBB_CONF=$WORKSPACE/conf"
                sh "${env.groovyzHome}/groovyz --classpath .:${env.groovyLibPath}:$WORKSPACE/${env.polycephalyJar} $WORKSPACE/build/build.groovy --collection Polycephaly --sourceDir $WORKSPACE/conf/package.txt"
            }
        }
        stage("Deploy") {
            options {
                timeout(time: 2, unit: "MINUTES")
            }
            steps {
                sh "cp -Rf ${WORKSPACE}/${env.polycephalyJar} ${env.polyRuntime}/bin/" 
                sh "cp -Rf ${WORKSPACE}/conf/*.properties ${env.polyRuntime}/conf/"
                sh "cp -Rf ${WORKSPACE}/conf/*.pw ${env.polyRuntime}/conf/"   
            }
        }
    }
	post {
	    success {
	      emailext (
          		attachLog: true, attachmentsPattern: '*.log',
          		subject: "SUCCESSFUL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
    			body: "${currentBuild.currentResult}: Job ${env.JOB_NAME} build ${env.BUILD_NUMBER}\n More info at: ${env.BUILD_URL}",
     			recipientProviders: [developers(), requestor()],
	        )
	    }
	
	    failure {
	          emailext (
	          	attachLog: true, attachmentsPattern: '*.log',
    			body: "${currentBuild.currentResult}: Job ${env.JOB_NAME} build ${env.BUILD_NUMBER}\n More info at: ${env.BUILD_URL}",
     			recipientProviders: [developers(), requestor()],
     			subject: "Jenkins Build ${currentBuild.currentResult}: Job ${env.JOB_NAME}"
	        )
	    }
    }
}
  