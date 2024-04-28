def url_repo = "https://github.com/Kachma6/DesafiosEducativos.git"

pipeline {
    // agent any
    agent{ 
        label 'deploy'
    }
     tools {
        nodejs 'nodejs'
        jdk 'jdk21'
    }
    parameters{
        string defaultValue: 'main',description:'colocar el branch del deploy', name: 'BRANCH', trim: false
    }
    environment{
        workspace="/data/"
       
    }
    stages {
        stage("Limpiar"){
            steps{
                cleanWs()
            }
        }
        stage('Download proyect') {
            steps {
                // Get some code from a GitHub repository
                git credentialsId: 'git_credentials', branch : "${BRANCH}", url: "${url_repo}"
                echo "Proyecto descargado"

            }
        }
        stage('Build') {
            steps {
               sh "pwd"
               sh "npm version"
              sh "npm install"
               sh "npm run build"
               sh "pwd"
               sh "tar -rf dist.tar dist/"
               archiveArtifacts artifacts: 'dist.tar', onlyIfSuccessful:true
              sh "cp dist.tar /tmp/"
            }
        } 
        stage("Test vulnerability"){
            steps{
                sh "cd /tmp/"
                sh "chmod +x /home/grype"
                sh "pwd"
                
                sh "/home/grype /tmp/dist.tar > informe-scan.txt"
                sh "pwd"
                archiveArtifacts artifacts: 'informe-scan.txt', onlyIfSuccessful:true
            }
        }
        stage('pull') {
            steps {
               sh "pwd"
               sh "npm version"
            //    sh "npm install"
            //    sh "npm run build"
               sh "pwd"


                  sh "docker rmi 192.168.137.5:8082/v2/repository/docker/front-prueba:latest | true ; docker build -t 192.168.137.5:8082/v2/repository/docker/front-prueba:latest ."
                   sh "docker push 192.168.137.5:8082/v2/repository/docker/front-prueba:latest "
              

            }
        }
       
      
        
    }
}