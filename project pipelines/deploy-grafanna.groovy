pipeline {
    agent any

    stages {
        stage('Git integration') {
            steps {
                checkout scmGit(branches: [[name: '*/main']], extensions: [], userRemoteConfigs: [[credentialsId: 'devops14-jenkins-ssh-id', url: 'git@github.com:malleshdevops/prometheus.git']])
            }
        }
        stage('helm check') {
            steps {
                sh '''helm version
				      if [ $? == 0 ]
					  then
					    echo "Helm installed"
					  else
					    echo "Helm not installed"
						exit 1
					  fi
                '''
            }

        }
        stage('helm prometheus install') {
            steps {
                sh '''cd $WORKSPACE
                      helm dependency build charts/kube-prometheus-stack
                      helm upgrade --install devops-prometheus charts/kube-prometheus-stack
					  '''
                
            }
            
        }
        stage('check pods') {
            steps {
                sh '''
                    kubectl get po
                    
                '''
                
            }
            
        }
        
    }
}

########################################################################################


pipeline {
    agent any

    stages {
        stage('clean workspace'){
            steps{
                cleanWs()
            }
        }
        stage('Git integration') {
            steps {
                checkout scmGit(branches: [[name: '*/main']], extensions: [], userRemoteConfigs: [[credentialsId: 'devops14-jenkins-ssh-id', url: 'git@github.com:malleshdevops/prometheus.git']])
            }
        }
        stage('helm check') {
            steps {
                sh '''helm version
				      if [ $? == 0 ]
					  then
					    echo "Helm installed"
					  else
					    echo "Helm not installed"
						exit 1
					  fi
                '''
            }

        }
        
        stage('helm prometheus install') {
            steps {
                sh '''cd $WORKSPACE
                      helm dependency build charts/kube-prometheus-stack
                      helm upgrade --install devops-prometheus charts/kube-prometheus-stack
					  '''
                
            }
            
        }
        stage('check pods') {
            steps {
                sh '''
                    kubectl get po
                    
                '''
                
            }
            
        }
        
    }
}

