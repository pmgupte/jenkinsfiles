/**
 * This script curls the Splunk AppInspect tool page, 
 * and checks if latest version is same as last known. 
 * If it differs, sends alert on Slack channel.
 * Build Parameters:
 * LAST_KNOWN_VERSION - string - last known version of AppInspect tool
 * SLACK_WEBHOOK_URL - string - URL of slack channel webhook
 */

pipeline {
    agent {label 'master'}
    
    stages {
        stage('Check AppInspect Version') { 
            steps {
                script {
                    def found_version = sh(returnStdout: true, script: 'curl -Ls http://dev.splunk.com/view/appinspect/SP-CAAAFBY | grep -o "v[[:digit:]]\\+\\.[[:digit:]]\\+\\.[[:digit:]]\\+" | head -1')
                    if ("${found_version}" != "${LAST_KNOWN_VERSION}") {
                        env.message = "Splunk AppInspect ${found_version} released! Last known version was ${LAST_KNOWN_VERSION}."
                        error env.message
                    } else {
                        env.message = "Splunk AppInspect ${LAST_KNOWN_VERSION} is the latest version seen on Splunk website."
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
