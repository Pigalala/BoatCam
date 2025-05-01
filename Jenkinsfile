def VERSION

pipeline {
    agent any
    environment {
        GIT_HASH = GIT_COMMIT.take(8)
    }
    stages {
        stage('Version') {
            steps {
                script {
                    if (env.TAG_NAME) {
                        VERSION = "${TAG_NAME}"
                    } else if (env.BRANCH_NAME == 'master') {
                        VERSION = "${BUILD_NUMBER}+${GIT_HASH}"
                    } else {
                        VERSION = "${BRANCH_NAME}-${BUILD_NUMBER}+${GIT_HASH}"
                    }
                }
            }
        }
        stage('Build') {
            steps {
                script {
                    sh 'chmod +x gradlew'
                    sh "./gradlew -Pversion=${VERSION} clean build"
                    archiveArtifacts artifacts: "build/libs/*.jar", fingerprint: true
                }
            }
        }
    }
}
