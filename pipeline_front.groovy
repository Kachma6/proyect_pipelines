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
              

            }
        }
       
      
        
    }
}