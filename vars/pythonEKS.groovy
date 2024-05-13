def call(Map configMap){  
    pipeline{
        agent{
            node{
                label 'NODE-1'
            }
        }

        options {
            timeout(time: 1, unit: 'HOURS')
            disableConcurrentBuilds()
            ansiColor('xterm')
        }

        parameters {
            booleanParam(name: 'Deploy', defaultValue: false, description: 'Toggle this value')
        }
            
        stages{
            stage('Clone code from GitHub'){
                steps{
                //git branch: 'main', url: 'https://github.com/srikanthhg/checkoutservice'
                git url: 'https://github.com/srikanthhg/recommendationservice',  branch: 'main'
                }
            }
            stage('Get the version'){
                steps{
                    script {
                        def packageVersion = readFile('version.py').trim()
                        if (packageVersion) {                       
                            echo "Application version: $packageVersion"
                        } else {
                            error "Application version not found in main.go"
                        }          
                    }
                }
            }

            stage('Install Dependencies'){
                steps{
                    sh """
                    pip3.12 install -r requirements.txt
                    """
                }
            }

            stage('Unit testing') {
                steps {
                    sh """
                        echo "unit tests will run here" 
                    """
                }
            }
            stage('Sonar scan') { // sonar-scanner is the command, it will read sonar-project properties and start scanning
                steps {
                    sh """
                        "sonar-scanner"
                    """
                }
            }

        }

        post { 
            always { 
                echo 'I will always say Hello again!'
                deleteDir()
            }
            failure { 
                echo 'this runs when pipeline is failed, used generally to send some alerts'
            }
            success{
                echo 'I will say Hello when pipeline is success'
            }
        }
    }
}