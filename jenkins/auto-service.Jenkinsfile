pipeline {
    agent any

    triggers {
        // Раз в ~5 минут проверять, есть ли новые коммиты в main
        pollSCM('H/5 * * * *')
    }

    options {
        disableConcurrentBuilds()
        timeout(time: 60, unit: 'MINUTES')
    }

    stages {
        stage('Deploy to dev and run smoke tests') {
            steps {
                build job: 'stand-deploy-and-test', parameters: [
                    string(name: 'STAND', value: 'dev'),
                    booleanParam(name: 'DEPLOY', value: true),
                    booleanParam(name: 'RESET_DATA', value: false),
                    booleanParam(name: 'BACKUP', value: false),
                    string(name: 'TESTS', value: 'smoke'),
                    string(name: 'BROWSER', value: 'chrome')
                ]
            }
        }
    }
}