pipeline {
    agent any

    stages {
        stage('Git integration') {
            steps {
                checkout scmGit(branches: [[name: '*/master']], extensions: [], userRemoteConfigs: [[credentialsId: 'devops14-jenkins-ssh-id', url: 'https://malleshdevops2021@bitbucket.org/malleshdevops2021/createat-task.git']])
            }
        }
        stage('terraform check') {
            steps {
                sh '''terraform --version
				      if [ $? == 0 ]
					  then
					    echo "Terraform installed"
					  else
					    echo "Terraform not installed"
						exit 1
					  fi
                '''
            }

        }
        stage('Terraform format') {
            steps {
                sh '''cd $WORKSPACE/eks
                      terraform fmt
					  '''
                
            }
            
        }
        stage('Terraform init') {
            steps {
                sh '''cd $WORKSPACE/eks
                      terraform init
					  '''
                
            }
            
        }
        stage('Terraform plan') {
            steps {
                sh '''
                    cd $WORKSPACE/eks
                    terraform plan
                    
                '''
                
            }
            
        }
        stage('Terraform apply') {
            steps {
                sh '''
                    cd $WORKSPACE/eks
                    terraform apply --auto-approve
                    
                '''
                
            }
            
        }
        
    }
}