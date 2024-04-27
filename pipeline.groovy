def url_repo = "https://github.com/Kachma6/DesafiosEducativosBackend.git"

pipeline {
    // agent any
    agent{ 
        label 'slave_two'
    }
    tools {
        jdk "jdk21"
        maven "maven-369"
        // jdk 'java22'
        // maven 'maven-3696'
    }
    parameters{
        string defaultValue: 'dev',description:'colocar el branch del deploy', name: 'BRANCH', trim: false
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
        stage("Build proyect"){
            steps{
                echo "iniciando build"
                sh "pwd"
                sh "chmod -R +rx /opt/jdk21"
                sh "mvn -v"
                sh "mvn clean package -Dmaven.test.skip=true -U"
                sh "mv target/*.jar target/app.jar"
                stash includes: 'target/app.jar', name: 'backartifact'
                archiveArtifacts artifacts: 'target/app.jar', onlyIfSuccessful:true
                sh "cp target/app.jar /tmp/"
            }
            
        }
        stage("Test vulnerability"){
            steps{
                sh "cd /tmp/"
                sh "chmod +x /grype"
                sh "/grype /tmp/app.jar > informe-scan.txt"
                sh "pwd"
                archiveArtifacts artifacts: 'informe-scan.txt', onlyIfSuccessful:true
            }
        }
        // stage("sonarqube analysis"){
        //     steps{
                
        //          script {
                     
        //         withSonarQubeEnv('Sonar_CI')    { 
        //         def scannerHome = tool 'Sonar_CI'
        //         sh "cp ${scannerHome}/conf/sonar-scanner.properties sonar-project.properties"
        //          writeFile encoding: "UTF-8", file: 'sonar-project.properties', text: """
        //         sonar.projectKey=DesafiosEducativos
        //         sonar.projectName=DesafiosEducativos
        //         sonar.projectVersion=Desafiov1
        //         sonar.sourceEncoding=UTF-8
        //         sonar.sources=src/main/
        //         sonar.java.binaries=target/classes
        //         sonar.language=java
        //         sonar.scm.provider=git
        //           """
        //               sh "${scannerHome}/bin/sonar-scanner -Dproject.settings=sonar-project.properties"
        
        //             }
        //          }
        //     }
            
        // }
         stage("Push artefactory"){
            agent any
            steps{
                script{
                    unstach 'backartifact'
                   
                    sh "sshpass -d admin123 scp  /home/workspace/APP-DEV/buil_app/target/DesafiosEducativosBackend-0.0.1-SNAPSHOT.jar userver@192.168.137.5/home/userver/"
                    // sh "hostname"
                    // echo "probando" > nuevo.text
                }
            }
        }
    }
}