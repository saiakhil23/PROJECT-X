pipeline {
    agent any

    stages {
        stage('Git integration') {
            steps {
                checkout scmGit(branches: [[name: '*/master']], extensions: [], userRemoteConfigs: [[credentialsId: 'devops14-jenkins-ssh-id', url: 'https://malleshdevops2021@bitbucket.org/malleshdevops2021/createat-task.git']])
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
        stage('helm mariadb install') {
            steps {
                sh '''cd $WORKSPACE
                      helm upgrade --install devops-mariadb mariadb --values mariadb/createat-mariadb.yaml
					  
					  '''
                
            }
            
        }
        stage('check pods') {
            steps {
                sh '''
                    kubectl get po -n database
                    
                '''
                
            }
            
        }
        
    }
}

#############################################################################################


pipeline {
    agent any

    stages {
        stage('Git integration') {
            steps {
                checkout scmGit(branches: [[name: '*/master']], extensions: [], userRemoteConfigs: [[credentialsId: 'devops14-jenkins-ssh-id', url: 'https://malleshdevops2021@bitbucket.org/malleshdevops2021/createat-task.git']])
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
        stage('connecting to eks cluster'){
            steps{
                sh '''kubectl config use-context arn:aws:eks:eu-west-2:247137868475:cluster/devops13-eks-ef89SGZh '''
            }
        }
         
        stage('helm mariadb install') {
            steps {
                sh '''cd $WORKSPACE
                      helm upgrade --install devops-mariadb mariadb --values mariadb/createat-mariadb.yaml
					  helm status devops-mariadb
					  if [ $? == 0 ]
					  then
					    echo "mariadb installed"
					  else
					    echo "mariadb not installed"
					  fi
					  '''
                
            }
            
        }
        stage('check pods') {
            steps {
                sh '''
                    kubectl get po -n database
                    
                '''
                
            }
            
        }
        stage('helm java install') {
            steps {
                sh '''cd $WORKSPACE
                      helm upgrade --install devops-java springboot --values springboot/create-task-java.yaml \
                      --set image.repository=malleshdevops/jenkins-flow-test \
                      --set image.tag=spring-v1
					  '''
                
            }
            
        }
        
    }
}
