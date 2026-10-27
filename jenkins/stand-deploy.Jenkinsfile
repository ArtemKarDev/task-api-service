pipeline {
    agent any

    options {
        disableConcurrentBuilds()
    }

    parameters {
        choice(name: 'STAND', choices: ['stage', 'dev', 'local'],
               description: 'Стенд. local: в Docker только БД, приложение запускается из IDEA')
        choice(name: 'ACTION', choices: ['status', 'up', 'restart', 'down'],
               description: 'status: состояние; up: собрать и запустить; restart: перезапуск без пересборки; down: остановить (данные сохраняются)')
        booleanParam(name: 'RESET_DATA', defaultValue: false,
                     description: 'Только с ACTION=up: сначала удалить ВСЕ данные стенда (том БД). Разрешено для dev и local, для stage запрещено')
        booleanParam(name: 'BACKUP', defaultValue: false,
                     description: 'Только с RESET_DATA: перед удалением сохранить дамп БД как артефакт сборки')
    }

    environment {
        // Нужно для Dockerfile с кэшем Maven (RUN --mount=type=cache)
        DOCKER_BUILDKIT = '1'
        COMPOSE_DOCKER_CLI_BUILD = '1'
    }

    stages {
        stage('Check parameters') {
            steps {
                script {
                    if (params.RESET_DATA && params.STAND == 'stage') {
                        error('RESET_DATA для stage запрещён: удаление данных разрешено только для dev и local')
                    }
                    if (params.RESET_DATA && params.ACTION != 'up') {
                        error('RESET_DATA работает только вместе с ACTION=up')
                    }
                    if (params.BACKUP && !params.RESET_DATA) {
                        echo 'BACKUP игнорируется: дамп делается только вместе с RESET_DATA'
                    }
                    currentBuild.displayName = "#${env.BUILD_NUMBER} ${params.STAND}:${params.ACTION}" + (params.RESET_DATA ? ' +reset' : '')
                }
            }
        }

        stage('Run action') {
            steps {
                withCredentials([file(credentialsId: "env-${params.STAND}", variable: 'ENV_FILE')]) {
                    // local: поднимаем только БД, приложение запускается из IDEA
                    withEnv(["SERVICES=${params.STAND == 'local' ? 'postgres' : ''}"]) {
                        script {
                            switch (params.ACTION) {
                                case 'status':
                                    sh 'docker compose --env-file "$ENV_FILE" ps'
                                    break
                                case 'up':
                                    if (params.RESET_DATA) {
                                        input message: "Удалить ВСЕ данные стенда ${params.STAND}?", ok: 'Удалить'
                                        if (params.BACKUP) {
                                            sh '''
                                                if [ -n "$(docker compose --env-file "$ENV_FILE" ps -q postgres)" ]; then
                                                    docker compose --env-file "$ENV_FILE" exec -T postgres sh -c 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB"' > "backup-${STAND}-${BUILD_NUMBER}.sql"
                                                    test -s "backup-${STAND}-${BUILD_NUMBER}.sql"
                                                else
                                                    echo "БД не запущена, дамп пропущен"
                                                fi
                                            '''
                                            archiveArtifacts artifacts: "backup-${params.STAND}-${env.BUILD_NUMBER}.sql", allowEmptyArchive: true
                                        }
                                        sh 'docker compose --env-file "$ENV_FILE" down -v'
                                    }
                                    sh 'docker compose --env-file "$ENV_FILE" up -d --build --wait --wait-timeout 180 $SERVICES'
                                    break
                                case 'restart':
                                    sh 'docker compose --env-file "$ENV_FILE" restart $SERVICES'
                                    break
                                case 'down':
                                    sh 'docker compose --env-file "$ENV_FILE" down'
                                    break
                            }
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            withCredentials([file(credentialsId: "env-${params.STAND}", variable: 'ENV_FILE')]) {
                sh '''
                    docker compose --env-file "$ENV_FILE" ps || true
                    docker compose --env-file "$ENV_FILE" logs --no-color --tail 300 > "stand-logs-${STAND}.txt" 2>&1 || true
                '''
            }
            archiveArtifacts artifacts: "stand-logs-${params.STAND}.txt", allowEmptyArchive: true
        }
    }
}