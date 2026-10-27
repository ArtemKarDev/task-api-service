pipeline {
    agent any

    options {
        disableConcurrentBuilds()
    }

    parameters {
        choice(name: 'STAND', choices: ['stage', 'dev', 'local'],
               description: 'Стенд. local: в Docker только БД, приложение запускается из IDEA')
        choice(name: 'ACTION', choices: ['status', 'up', 'restart', 'down', 'reset-data'],
               description: 'status: состояние; up: собрать и запустить; restart: перезапуск без пересборки; down: остановить (данные сохраняются); reset-data: удалить данные (только dev и local)')
        booleanParam(name: 'BACKUP', defaultValue: true,
                     description: 'reset-data: перед удалением сохранить дамп БД как артефакт сборки')
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
                    if (params.ACTION == 'reset-data' && params.STAND == 'stage') {
                        error('reset-data для stage запрещён: удаление данных разрешено только для dev и local')
                    }
                    currentBuild.displayName = "#${env.BUILD_NUMBER} ${params.STAND}:${params.ACTION}"
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
                                    sh 'docker compose --env-file "$ENV_FILE" up -d --build --wait --wait-timeout 180 $SERVICES'
                                    break
                                case 'restart':
                                    sh 'docker compose --env-file "$ENV_FILE" restart $SERVICES'
                                    break
                                case 'down':
                                    sh 'docker compose --env-file "$ENV_FILE" down'
                                    break
                                case 'reset-data':
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
                                    sh 'docker compose --env-file "$ENV_FILE" up -d --build --wait --wait-timeout 180 $SERVICES'
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