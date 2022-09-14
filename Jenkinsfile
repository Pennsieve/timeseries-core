node("executor") {
    checkout scm
    def authorName = sh(returnStdout: true, script: 'git --no-pager show --format="%an" --no-patch')

    def commitHash = sh(returnStdout: true, script: 'git rev-parse HEAD | cut -c-7').trim()
    def imageTag = "${env.BUILD_NUMBER}-${commitHash}"

    def sbt = "sbt -Dsbt.log.noformat=true -Dversion=$imageTag"

    def pennsieveNexusCreds = usernamePassword(
        credentialsId: "pennsieve-nexus-ci-login",
        usernameVariable: "PENNSIEVE_NEXUS_USER",
        passwordVariable: "PENNSIEVE_NEXUS_PW"
    )

    stage("Build") {
        try {
            withCredentials([pennsieveNexusCreds]) {
                sh "$sbt clean +compile"
            }
        } catch (e) {
            slackSend(color: '#b20000', message: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL}) by ${authorName}")
            throw e
        }
    }
    stage("Test") {
        try {
            withCredentials([pennsieveNexusCreds]) {
                sh "$sbt +test"
                junit 'target/test-reports/*.xml'
            }
        } catch (e) {
            slackSend(color: '#b20000', message: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL}) by ${authorName}")
            throw e
        }
    }
    if (env.BRANCH_NAME == "main") {
        stage('Publish') {
            try {
                withCredentials([pennsieveNexusCreds]) {
                    sh "$sbt +publish"
                }
            } catch (e) {
                slackSend(color: '#b20000', message: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL}) by ${authorName}")
                throw e
            }
        }
    }
    slackSend(color: '#006600', message: "SUCCESSFUL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL}) by ${authorName}")
}
