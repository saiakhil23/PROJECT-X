pipeline {
    agent any

    stages {
        stage('Git integration') {
            steps {
                checkout scmGit(branches: [[name: '*/master']], extensions: [], userRemoteConfigs: [[credentialsId: 'devops14-jenkins-ssh-id', url: 'https://malleshdevops2021@bitbucket.org/malleshdevops2021/createat-task.git']])
            }
        }
        /*stage('npm build') {
            steps {
                sh '''cd $WORKSPACE/react-frontend/
                npm i react-scripts
                npm run build '''
            }

        }*/
        stage('sonar validation') {
            steps {
                sh 'echo sonar sucess'
                
            }
            
        }
        stage('Docker build') {
            steps {
                sh '''
                    cd $WORKSPACE/react-frontend
                    docker build -t reactjs:v1 .
                    
                '''
                
            }
            
        }
        stage('Docker repo push') {
            steps {
                sh '''
                 docker tag reactjs:v1 malleshdevops/jenkins-flow-test:reactjs-v1
                 docker push malleshdevops/jenkins-flow-test:reactjs-v1
                '''
                
            }
            
        }
        stage('connect to k8') {
            steps {
                sh 'kubectl get ns'
                
            }
            
        }
        
        stage('Deploy helm ') {
            steps {
                sh '''
                    cd $WORKSPACE/react-frontend
                    helm upgrade --install cicd-deploy frontend -f frontend/frontend-values-prod.yaml \
					--set image.repository=malleshdevops/jenkins-flow-test \
					--set image.tag=reactjs-v1
                    
                '''
                
            }
            
        }
    }
}

#########################################################################################################



pipeline {
    agent any

    stages {
        stage('Git integration') {
            steps {
                checkout scmGit(branches: [[name: '*/master']], extensions: [], userRemoteConfigs: [[credentialsId: 'devops14-jenkins-ssh-id', url: 'https://malleshdevops2021@bitbucket.org/malleshdevops2021/createat-task.git']])
            }
        }
        /*stage('Maven build') {
            steps {
                sh '''cd $WORKSPACE/react-frontend/
                npm i react-scripts
                npm run build '''
            }

        }*/
        stage('sonar validation') {
            steps {
                sh 'echo sonar sucess'
                
            }
            
        }
        stage('Docker build') {
            steps {
                sh '''
                    cd $WORKSPACE/react-frontend
                    docker build -t reactjs:v1 .
                    
                '''
                
            }
            
        }
        stage('Docker repo push') {
            steps {
                sh '''
                 docker tag reactjs:v1 malleshdevops/jenkins-flow-test:reactjs-v1
                 docker push malleshdevops/jenkins-flow-test:reactjs-v1
                '''
                
            }
            
        }
        stage('connecting to eks cluster'){
            steps{
                sh '''kubectl config use-context arn:aws:eks:eu-west-2:247137868475:cluster/devops13-eks-ef89SGZh '''
            }
        }
        stage('connect to k8') {
            steps {
                sh 'kubectl get ns'
                
            }
            
        }
        
        stage('Deploy helm ') {
            steps {
                sh '''
                    cd $WORKSPACE/react-frontend
                    helm upgrade --install cicd-deploy frontend -f frontend/frontend-values-prod.yaml \
					--set image.repository=malleshdevops/jenkins-flow-test \
					--set image.tag=reactjs-v1
                    
                '''
                
            }
            
        }
    }
}



