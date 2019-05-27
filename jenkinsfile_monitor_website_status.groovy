/**
 * This script curls the configured domain, 
 * and sends HTTP status to configured Slack webhook, 
 * if HTTP code does not match expected value.
 * Build Parameters:
 * WEBSITE_URL - string - website to be checked
 * SLACK_WEBHOOK_URL - string - URL of slack channel webhook
 * EXPECTED_HTTP_CODE - string - HTTP status code expected
 */

pipeline {
    agent {label 'master'}
    
    stages {
        stage('Curl') { 
            steps {
                script {
                    def http_code = sh(returnStdout: true, script: '''
                    http_status_code=$(curl -s -o /dev/null -I -w "%{http_code}" "${WEBSITE_URL}")
                    echo $http_status_code
                    ''')
                    env.http_code = http_code
                    
                    if ("${http_code}" != "${EXPECTED_HTTP_CODE}") {
                        env.message = "${WEBSITE_URL} DOWN! Site returned HTTP ${http_code}, but expected was ${EXPECTED_HTTP_CODE}"
                        error env.message
                    } else {
                        env.message = "${WEBSITE_URL} UP! Site returned expected HTTP code ${EXPECTED_HTTP_CODE}"
                    }
                    echo env.message
                }
            }
        }
    } // stages
    post {
        changed {
            script {
                sh "curl -X POST -H \'Content-type: application/json\' --data \'{\"text\":\"${env.message}\"}\' ${SLACK_WEBHOOK_URL}"
            }
        }
    }
}
