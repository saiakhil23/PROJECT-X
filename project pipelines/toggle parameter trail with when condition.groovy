def continueotherstages = "${params.TOGGLE}"
pipeline {
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
    stage('Example-TEST1') {
      steps {
        sh "echo ${params.TOGGLE}"
      }
    }
    stage('test1') {
            when {
                expression { 
                  params.TOGGLE // Inside the when condition we need to use $ to get the value of param.TOGGLE like above stage(Example-TEST1)
                }
            }
            steps {
                    sh """
                    echo "deploy to production"
                    """
                }
    }
    stage('Example-TEST-2') {
      steps {
        sh "echo ${!params.TOGGLE}"
      }
    }
   
   stage('test2') {
            when {
                expression { 
                  !params.TOGGLE
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
  
  
