pipeline {
    agent any

    tools {
        maven 'maven-3.9'
    }

    options {
        skipDefaultCheckout()
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'git log -1 --oneline'
            }
        }
        stage('Build') {
            steps {
                dir('hello-maven') {
                    sh 'mvn -B clean package -DskipTests'
                }
            }
        }
        stage('Test') {
            steps {
                dir('hello-maven') {
                    sh 'mvn -B tset'
                }
            }
            post {
                always {
                    junit 'hello-maven/target/surefire-reports/*.xml'
                }
            }
        }
        stage('Archive') {
            steps {
                archiveArtifacts artifacts: 'hello-maven/target/*.jar', fingerprint: true
            }
        }
    }

    post {
        success {
            echo "Pipeline succeeded: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
        }
        failure {
            echo "Pipeline FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER} - check the red stage"
        }
        always {
            echo 'Pipeline finished'
        }
    }
}
