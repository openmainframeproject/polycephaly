pipeline {
agent { node { label 'zOS01' } }

  environment {
    USE_JDK = 'true'
    JavaHome ='/usr/lpp/java/J8.0_64'
  }
  stages {
    stage('Build') {
      steps {
        sh 'printenv'
      }
    }
  }
}