pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        timeout(time: 30, unit: 'MINUTES')
    }

    parameters {
        choice(name: 'ACTION', choices: ['status', 'deploy', 'down'],
               description: 'status: состояние; deploy: развернуть версию; down: остановить (данные сохраняются)')
        string(name: 'APP_TAG', defaultValue: '',
               description: 'Пусто: собрать образ из текущего коммита. Тег (короткий хеш коммита): развернуть уже собранный образ, например для отката')
        string(name: 'TRIGGER', defaultValue: 'manual', description: 'Источник запуска (заполняют вызывающие задания)')
    }

    environment {
        DOCKER_BUILDKIT = '1'
        IMAGE = 'task-api-service'
        REMOTE_DIR = '/opt/task-prod/service'
    }

    stages {
        stage('Prepare') {
            steps {
                script {
                    env.TAG = params.APP_TAG?.trim()
                            ? params.APP_TAG.trim()
                            : sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    currentBuild.displayName = "#${env.BUILD_NUMBER} prod:${params.ACTION}" +
                            (params.ACTION == 'deploy' ? " ${env.TAG}" : '')
                }
            }
        }

        stage('Build image') {
            when { expression { params.ACTION == 'deploy' && !params.APP_TAG?.trim() } }
            steps {
                sh 'docker build -t "$IMAGE:$TAG" .'
            }
        }

        stage('Run on prod') {
            steps {
                withCredentials([
                        sshUserPrivateKey(credentialsId: 'prod-ssh', keyFileVariable: 'SSH_KEY_RAW', usernameVariable: 'SSH_USER'),
                        file(credentialsId: 'prod-known-hosts', variable: 'KNOWN_HOSTS_RAW'),
                        file(credentialsId: 'env-prod', variable: 'ENV_FILE'),
                        string(credentialsId: 'prod-host', variable: 'PROD_HOST')
                ]) {
                    sh '''
                        set -eu
                        # Файлы, созданные в Windows, могут содержать \\r: работаем с очищенными копиями
                        umask 077
                        tr -d '\\r' < "$SSH_KEY_RAW" > .prod_key
                        tr -d '\\r' < "$KNOWN_HOSTS_RAW" > .prod_known_hosts

                        OPTS="-i .prod_key -o UserKnownHostsFile=.prod_known_hosts -o StrictHostKeyChecking=yes -o BatchMode=yes"
                        TARGET="$SSH_USER@$PROD_HOST"
                        remote() { ssh $OPTS "$TARGET" "$@"; }

                        case "$ACTION" in
                          status)
                            remote "cd $REMOTE_DIR && docker compose ps"
                            ;;
                          down)
                            remote "cd $REMOTE_DIR && docker compose down"
                            ;;
                          deploy)
                            echo "== 1. Образ $IMAGE:$TAG"
                            if remote "docker image inspect $IMAGE:$TAG > /dev/null 2>&1"; then
                              echo "Уже есть на сервере, передавать не нужно"
                            else
                              docker image inspect "$IMAGE:$TAG" > /dev/null 2>&1 \
                                || { echo "Образа $IMAGE:$TAG нет ни в Jenkins, ни на сервере"; exit 1; }
                              docker save "$IMAGE:$TAG" | gzip -1 | remote "gunzip | docker load"
                            fi

                            echo "== 2. Файлы стенда"
                            scp $OPTS compose.yaml compose.prod.yaml "$TARGET:$REMOTE_DIR/"
                            scp $OPTS -r caddy "$TARGET:$REMOTE_DIR/"

                            echo "== 3. .env (предыдущая версия: $(remote "grep '^APP_TAG=' $REMOTE_DIR/.env || echo нет"))"
                            { tr -d '\\r' < "$ENV_FILE"; echo; echo "APP_TAG=$TAG"; echo "COMPOSE_FILE=compose.yaml:compose.prod.yaml"; } \
                              | remote "umask 077 && cat > $REMOTE_DIR/.env"

                            echo "== 4. Запуск"
                            remote "cd $REMOTE_DIR && docker compose up -d --no-build --wait --wait-timeout 360"

                            echo "== 5. Проверка снаружи, как у пользователя"
                            DOMAIN=$(grep '^PUBLIC_DOMAIN=' "$ENV_FILE" | tail -n1 | cut -d= -f2- | tr -d '\\r')
                            curl -fsS -o /dev/null --retry 10 --retry-delay 6 --retry-all-errors "https://$DOMAIN/login"
                            echo "https://$DOMAIN отвечает, версия $TAG"
                            ;;
                        esac
                    '''
                }
            }
        }
    }

    post {
        always {
            sh 'rm -f .prod_key .prod_known_hosts'
        }
        failure {
            withCredentials([string(credentialsId: 'telegram-bot-token', variable: 'TG_TOKEN'),
                             string(credentialsId: 'telegram-chat-id', variable: 'TG_CHAT')]) {
                sh '''
                    curl -s -S --max-time 20 "https://api.telegram.org/bot${TG_TOKEN}/sendMessage" \
                      --data-urlencode "chat_id=${TG_CHAT}" \
                      --data-urlencode "text=❌ Prod: ${ACTION} ${TAG:-} failed
Trigger: ${TRIGGER:-manual}
${BUILD_URL}console" \
                      -o /dev/null -w "Telegram: HTTP %{http_code}\\n" || true
                '''
            }
        }
    }
}