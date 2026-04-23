pipeline {
    agent any

    environment {
        // Change these two lines per repo
        DOCKER_IMAGE    = 'lordibe/candorapi'
        REPO_URL        = 'https://github.com/CandorTechnologiesHub/CandorTech-Backend.git'

        // These stay the same — they reference Jenkins credentials you set up
        DOCKER_CREDENTIALS = credentials('dockerhub-credentials')
        DOCKER_TAG         = "${BUILD_NUMBER}"
    }

    stages {

        // ---------------------------------------------------------
        // STAGE 1: Checkout
        // Jenkins pulls the latest code from GitHub
        // ---------------------------------------------------------
        stage('Checkout') {
            steps {
                git branch: 'main',
                        url: "${REPO_URL}",
                        credentialsId: 'github-credentials'
            }
        }

        // ---------------------------------------------------------
        // STAGE 2: Build and Test
        // Maven compiles the code and runs all unit tests
        // If any test fails, the pipeline stops here
        // ---------------------------------------------------------
        stage('Build & Test') {
            steps {
                sh 'mvn clean test'
            }
            post {
                always {
                    // Publish test results in Jenkins UI
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        // ---------------------------------------------------------
        // STAGE 3: Docker Build
        // Packages the compiled app into a Docker image
        // Tags it with both the build number and 'latest'
        // ---------------------------------------------------------
        stage('Docker Build') {
            steps {
                sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                sh "docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest"
            }
        }

        // ---------------------------------------------------------
        // STAGE 4: Docker Push
        // Uploads the image to Docker Hub so Kubernetes can pull it
        // ---------------------------------------------------------
        stage('Docker Push') {
            steps {
                sh "echo ${DOCKER_CREDENTIALS_PSW} | docker login -u ${DOCKER_CREDENTIALS_USR} --password-stdin"
                sh "docker push ${DOCKER_IMAGE}:${DOCKER_TAG}"
                sh "docker push ${DOCKER_IMAGE}:latest"
            }
        }

        // ---------------------------------------------------------
        // STAGE 5: Deploy via Ansible
        // Jenkins calls Ansible which SSHs into the server
        // Ansible applies K8s manifests and waits for healthy pods
        // ---------------------------------------------------------
        stage('Deploy via Ansible') {
            steps {
                sh """
                    ansible-playbook ansible/deploy.yml \
                        -i ansible/inventory.ini \
                        --extra-vars "image_tag=${DOCKER_TAG}" \
                        -v
                """
            }
        }
    }

    post {
        success {
            echo "========================================="
            echo "  PIPELINE COMPLETE — APP IS LIVE"
            echo "========================================="
        }
        failure {
            echo "========================================="
            echo "  PIPELINE FAILED — CHECK LOGS ABOVE"
            echo "========================================="
        }
        always {
            // Remove the local Docker image to free up disk space
            sh "docker rmi ${DOCKER_IMAGE}:${DOCKER_TAG} || true"
            sh "docker logout || true"
        }
    }
}