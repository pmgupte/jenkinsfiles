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
                    def http_code = sh(returnStatus: true, script: '''
                    curl --version
                    http_status_code=$(curl -s -o /dev/null -I -w "%{http_code}" "${WEBSITE_URL}")
                    exit $http_status_code
                    ''')
                    env.http_code = http_code
                    
                    if ("${http_code}" != "${EXPECTED_HTTP_CODE}") {
                        error "${WEBSITE_URL} returned ${http_code}"
                    } else {
                        echo "${WEBSITE_URL} returned expected HTTP code ${EXPECTED_HTTP_CODE}"
                    }
                }
            }
        }
    } // stages
    post {
        changed {
            script {
                def message="${WEBSITE_URL} HTTP code is ${env.http_code}. Expected code is ${EXPECTED_HTTP_CODE}."
                echo $message
                sh "curl -X POST -H \'Content-type: application/json\' --data \'{\"text\":\"${message}\"}\' ${SLACK_WEBHOOK_URL}"
            }
        }
    }
}
