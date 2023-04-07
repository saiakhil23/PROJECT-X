def skipRemainingStages = false 
def skipbluestage = false
def failedstage
def bluefail
def kempfail
def cyberfail

pipeline {
    //using this script will connect to lab-ansible server
    agent {
        node{
       
            label 'master'
            }
        
    }
    options { buildDiscarder(logRotator(numToKeepStr: '10')) }
    environment{
        jenkinsuser = ""
    }
    parameters {
        string(name: 'customer_id', defaultValue: '', description: 'Enter customer_id here', trim: true)
        
    }

    stages {
        stage('Validate customer_id and files'){
            
            steps {
                echo "checking parameters"
                script {
                    currentBuild.displayName += " ${params.customer_id}"
                    currentBuild.description = "PlayName: ${customer_id}"
                    failedstage=env.STAGE_NAME
                    if (!params.customer_id){
                        echo "Please specify customer_id to execute the playbooks"
                        currentBuild.result = "FAILED"
                        skipRemainingStages = true
                        skipbluestage = true
                        catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                        sh "exit 1"
						}
						}
					else{
					  try{
							sh'''
							
							echo 'updating csv files'
							cp -r /tmp/jenkins/*.* $WORKSPACE
	                        cd $WORKSPACE
	                        echo $customer_id
	                        csvfilecount=`sudo ls | grep $customer_id |wc -l`
				            if [ $csvfilecount == 0 ]
				            then
				                echo "please enter valid customer id"
				                sh "exit 1"
				            else
			                    file=`sudo ls | grep $customer_id`
                                cd ${WORKSPACE}
                                bash update.sh
				            fi
				            '''
                        }
                            catch (Exception err) {
                            
                                skipRemainingStages = true
                                skipbluestage = true
                            
							}
			                if(skipRemainingStages){
			                    catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
			                    sh 'exit 1'
			                    }  
			                }
                   }

                }
            }
        
        }
        stage('config scripts') {
            when {
                anyOf {
                expression {
                    !skipRemainingStages
                }
              }
                
            }
            steps {
            script {
                failedstage=env.STAGE_NAME
            try{
			echo 'getting started'
	 		sh '''
	            cd ${WORKSPACE}
                bash config.sh
                echo "config sucessfully Executed " '''
            }
            catch (Exception err) {
                            
                            skipbluestage = true
						}
			if(skipbluestage ){
			 catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
			    sh 'exit 1'
			     
			 }  
			}
			
            }
        }
        
    }
       
        
        
        stage('blue scripts') {
            when {
                anyOf {
                expression {
                     !skipRemainingStages
                     !skipbluestage
                }
                }
            }
            steps {
			script {
                bluestage=env.STAGE_NAME
            try{
			echo 'getting started'
	 		sh '''
	            cd ${WORKSPACE}
                bash blue.sh
                echo "install blue sucessfully Executed  " '''
            }
            catch (Exception err) {
                            
                            bluefail = true
						}
			if(bluefail){
			 catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
			    sh 'exit 1'
			     
			 }  
			}
            }
        }        
    }
    stage('cyber script') {
            when {
                expression {
                    !skipRemainingStages
                }
            }
            environment {
                TEST_COMMON_CREDS = credentials('aws-ses-cred')
            }
            steps {
            script {
                cyberstage=env.STAGE_NAME
			try{
			echo 'getting started'
            echo $jenkinsuser
	 		sh '''
	            cd ${WORKSPACE}
                echo "$TEST_COMMON_CREDS_USR"
                echo "$TEST_COMMON_CREDS_PSW"
                echo "jenkins user after beore $jenkinsuser"
                jenkinsuser=test1
                echo "jenkins user after assigining $jenkinsuser"
                touch ${WORKSPACE}/$jenkinsuser.txt
                bash test.sh
                
                if [ $? != 0 ]
                then
                    echo "build failed"
                    
                    exit 1
                fi
                
                '''
            
            }catch (Exception err) {
                            
                            cyberfail = true
                            sh ''' echo $jenkinsuser
                            rm ${WORKSPACE}/$jenkinsuser.txt '''
						}
			if(cyberfail){
			catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                  sh 'exit 1'
			}
            }
            
        }
        
    }
	}
    stage('kemp script') {
     // using this script will connect to the kemp server
     //this script executing from the kemp server so it will take standard Source file that allredy there in the path
            when {
                expression {
                    !skipRemainingStages
                }
            }
            steps {
            script {
			    
                kempstage=env.STAGE_NAME
			try{
			
			echo 'getting started'
			
	 		sh '''
	            	echo "${WORKSPACE}"
					bash kemp.sh
					if [[ $? != 0 ]]
					then
						echo "build failed"
						exit -1
					fi
            echo "sucessfully Executed kemp script"  '''
			}
			catch (Exception err) {
                            
                            kempfail = true
						}
			if(kempfail){
			catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                  sh 'exit 1'
				  }
				  }    
            }
        }
    }
       
}
    post {
       
           success {
               script{
                if(bluefail){
				   failedstage=bluestage
				   }
				else if(cyberfail){
				   failedstage=cyberstage
				   }
				else if(kempfail){
				   failedstage=kempstage
				   }
            mail to:'malleshdevops2021@outlook.com' ,
			subject:"Status Success: ${currentBuild.fullDisplayName}", 
			body: " Hi, \n your jenkins job status is success,\n \n here is your pipeline details. \n job Name: ${env.JOB_NAME} \n customer_id= $customer_id \n failed stage : ${failedstage} \n for output login to the ${env.BUILD_URL},\n \n regards, \n Cloud Team"
        }
               }
            failure {
           script {              
                if(bluefail){
				   failedstage=failedstage+":::"+bluestage
				   }
				else if(cyberfail){
				   failedstage=failedstage+":::"+cyberstage
				   }
				else if(kempfail){
				   failedstage=failedstage+":::"+kempstage
				   }
				
				   
               mail to:'malleshdevops2021@outlook.com' ,
			    subject:"Status Success: ${currentBuild.fullDisplayName}-${failedstage}", 
			    body: " Hi, \n your jenkins job status is success,\n \n here is your pipeline details. ${failedstage}"

               // To get a list of just the stage names:
               //echo "Failed stage names: " + failedStages.displayName
           }
		                                                                                                                                          
       }
	   
	   aborted {
            mail to:"malleshdevops2021@outlook.com", 
			subject:"Status Aborted: ${currentBuild.fullDisplayName}-${env.STAGE_NAME}", 
			body: " Hi, \n your jenkins job status is aborted, \n \n here is your pipeline details. \n job Name: ${env.JOB_NAME} \n customer_id= $customer_id \n stage Name:${env.STAGE_NAME} \n for output login to the ${env.BUILD_URL},\n \n regards, \n Cloud Team"
        } 
   }

}