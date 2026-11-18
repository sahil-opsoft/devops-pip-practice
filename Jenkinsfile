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

    environment {
        IMAGE = 'sahilopsoft/hello-maven'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'git log -1 --oneline'
                script {
                    env.IMAGE_TAG = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                }
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
                    sh 'mvn -B org.jacoco:jacoco-maven-plugin:0.8.12:prepare-agent test org.jacoco:jacoco-maven-plugin:0.8.12:report'
                }
            }
            post {
                always {
                    junit 'hello-maven/target/surefire-reports/*.xml'
                }
            }
        }
        stage('SonarQube analysis') {
            steps {
                withSonarQubeEnv('sonarqube') {
                    dir('hello-maven') {
                        sh 'mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=hello-maven -Dsonar.projectName=hello-maven'
                    }
                }
            }
        }
        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        stage('Trivy config scan') {
            steps {
                sh 'trivy config --severity HIGH,CRITICAL --exit-code 1 hello-maven'
            }
        }
        stage('Docker build') {
            steps {
                sh 'docker build -t ${IMAGE}:${IMAGE_TAG} hello-maven'
            }
        }
        stage('Trivy image scan') {
            steps {
                sh '''
                    trivy image --scanners vuln --no-progress --output trivy-image-report.txt ${IMAGE}:${IMAGE_TAG}
                    trivy image --scanners vuln --no-progress --severity HIGH,CRITICAL --exit-code 1 ${IMAGE}:${IMAGE_TAG}
                '''
            }
            post {
                always {
                    archiveArtifacts artifacts: 'trivy-image-report.txt', allowEmptyArchive: true
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
            echo "Pipeline succeeded: ${env.JOB_NAME} #${env.BUILD_NUMBER} - image ${env.IMAGE}:${env.IMAGE_TAG}"
        }
        failure {
            echo "Pipeline FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER} - check the red stage"
        }
        always {
            echo 'Pipeline finished'
        }
    }
}
