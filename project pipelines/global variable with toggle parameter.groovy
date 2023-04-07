def continueotherstages = "${params.TOGGLE}" // here we are defining the global variable outside the pipeline so it is 
pipeline {                                   // returning value true even when we un-toggle instead of false 
  agent any
  parameters {
    booleanParam(name: 'TOGGLE', defaultValue: '', description: 'Toggle this value')
    gitParameter branchFilter: 'origin/(.*)', defaultValue: 'master', name: 'BRANCH', type: 'PT_BRANCH'
  }
  stages {
      
    stage('Example') {
      steps {
        git branch: "${params.BRANCH}", url: 'https://github.com/jenkinsci/git-parameter-plugin.git'
      }
    }
    stage('test1') {
            when {
                expression { 
                  continueotherstages 
                }
            }
            steps {
                    sh """
                    echo "deploy to production"
                    """
                }
    }
   
   stage('test2') {
            when {
                expression { 
                  !continueotherstages
                }
            }
            steps {
                    sh """
                    echo "deploy to production"
                    """
                }
            
   }
   stage('Deploy to Production') {
            when {
                expression { 
                   params.ENVIRONMENT == 'PROD'
                }
            }
            steps {
                    sh """
                    echo "deploy to production"
                    """
                }
   }
   
} 

}