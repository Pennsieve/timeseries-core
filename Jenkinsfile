node {
    checkout scm
    def authorName = sh(returnStdout: true, script: 'git --no-pager show --format="%an" --no-patch')
    def buildEnv
    if (env.BRANCH_NAME == "master") {
        buildEnv = "prod"
    } else {
        buildEnv = "dev"
    }
    def creds = [
        usernamePassword(
            credentialsId: "pennsieve-nexus-ci-login",
            usernameVariable: "PENNSIEVE_NEXUS_USER",
            passwordVariable: "PENNSIEVE_NEXUS_PW"
        )
    ]
    def sbt = "sbt -Dsbt.log.noformat=true -Dbuild-env=${buildEnv}"
    stage("Build") {
        try {
            withCredentials(creds) {
                sh "$sbt clean compile"
            }
        } catch (e) {
            slackSend(color: '#b20000', message: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL}) by ${authorName}")
            throw e
        }
    }
    // stage("Test") {
    //     try {
    //         withCredentials(creds) {
    //             sh "$sbt test"
    //             junit 'target/test-reports/*.xml'
    //         }
    //     } catch (e) {
    //         slackSend(color: '#b20000', message: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL}) by ${authorName}")
    //         throw e
    //     }
    // }
    if (env.BRANCH_NAME == "master") {
        stage('Deploy') {
            try {
                withCredentials(creds) {
                    sh "$sbt publish"
                }
            } catch (e) {
                slackSend(color: '#b20000', message: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL}) by ${authorName}")
                throw e
            }
        }
    }
    slackSend(color: '#006600', message: "SUCCESSFUL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL}) by ${authorName}")
}
