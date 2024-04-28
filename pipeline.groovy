def url_repo = "https://github.com/Kachma6/DesafiosEducativosBackend.git"

pipeline {
    // agent any
    agent{ 
        label 'deploy'
    }
    tools {
        jdk "jdk21"
        maven "maven-369"
        // jdk 'java22'
        // maven 'maven-3696'
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
        stage("Build proyect"){
            steps{
                echo "iniciando build"
                sh "pwd"
                sh "chmod -R +rx /home/jdk21"
                 sh "chmod -R +rx /home/maven-369"
                sh "mvn -v"
                sh "mvn clean package -Dmaven.test.skip=true -U"
                sh "pwd"
                sh "mv target/*.jar target/app.jar"
                stash includes: 'target/app.jar', name: 'backartifact'
                stash includes: 'Dockerfile', name: 'docker'
                archiveArtifacts artifacts: 'target/app.jar', onlyIfSuccessful:true
                sh "cp target/app.jar /tmp/"
            }
            
        }
        stage("Test vulnerability"){
            steps{
                sh "cd /tmp/"
                sh "chmod +x /home/grype"
                sh "pwd"
                
                sh "/home/grype /tmp/app.jar > informe-scan.txt"
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
        //         agent {
        //     docker {
        //       image 'docker:latest'
        //       args '-v /var/run/docker.sock:/var/run/docker.sock'
        //       }
        //   }

            
            steps{
                // script{
                //     sh "pwd"
                    
                
                //     sh "pwd"
                //     echo "$PATH"
                //     sh "docker --version"
                //     sh "docker images"
                //     sh "pwd"
                //     sh "cd /home/workspace/APP-DEV/buil_app"
                //      sh "pwd"
                //    // script {
                //     // Construir la imagen Docker
                //   //  sh "docker build -t prueba:1.0 ."
                   
                //    sh "docker rmi 192.168.137.5:8082/v2/repository/docker/back-prueba:latest | true ; docker build -t 192.168.137.5:8082/v2/repository/docker/back-prueba:latest ."
                //    sh "docker push 192.168.137.5:8082/v2/repository/docker/back-prueba:latest "
                // }

                  script{
                    unstash 'backartifact'
                    sh "rm /home/publish/app.jar | true"
                    sh "cp target/app.jar /home/publish/"
                    sh "cp Dockerfile /home/publish"
                    sh "docker rmi 192.168.137.5:8082/v2/repository/docker/back-prueba:latest | true; cd /home/publish/ ; docker build -t 192.168.137.5:8082/v2/repository/docker/back-prueba:latest ."
                    sh "docker push 192.168.137.5:8082/v2/repository/docker/back-prueba:latest "
                }
                 //   script{
                //     unstash 'backartifact'
                //     sh "rm /home/publish/app.jar | true"
                //     sh "target/app.jar /home/publish/"
                //     sh "docker rmi 192.168.137.5:8082/v2/repository/docker/back-prueba:latest | true; cd /home/publish/ ; docker build -t 192.168.137.5:8082/v2/repository/docker/back-prueba:latest ."
                //     sh "docker push 192.168.137.5:8082/v2/repository/docker/back-prueba:latest "
                // }
                
            }
        }
    }
}