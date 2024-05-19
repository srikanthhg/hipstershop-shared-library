def call(Map configMap){    
    pipeline {
        agent any
        environment { 
            packageVersion = ''    
            nexusURL = '172.31.74.236:8081'
        }
        tools {
            mabuild 'dotnetapp'
        }
        options {
            timeout(time: 1, unit: 'HOURS')
            disableConcurrentBuilds()
            ansiColor('xterm')
        }
        parameters {
            booleanParam(name: 'Deploy', defaultValue: false, description: 'Toggle this value')
        }

        stages {
            stage('Get the version') {
                steps { 
                    script{
                        def csprojFile = readFile 'cartservice/src/cartservice.csproj'
                        def versionRegex = /<Version>(.*?)<\/Version>/
                        def match = (csprojFile =~ versionRegex)
                    
                        if (match) {
                            def version = match[0][1]
                            echo "packageVersion: ${version}"
                            // You can use the 'version' variable for further processing
                        } else {
                            error "Failed to extract version from .csproj file"
                        }              
                    }
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
                        echo "sonar-scanner"
                    """
                }
            }
            stage('Install dependencies') {
                steps {
                    sh """
                    dotnet restore cartservice.csproj \
                        -r linux-musl-x64
                    dotnet publish cartservice.csproj \
                        -p:PublishSingleFile=true \
                        -r linux-musl-x64 \
                        --self-contained true \
                        -p:PublishTrimmed=True \
                        -p:TrimMode=Full \
                        -c release \
                        -o /cartservice \
                        --no-restore
                       
                    """
                }
            }
            stage('Build') {
                steps {
                    sh """
                        ls -ltr
                        zip -q -r ${configMap.component}.jar ./build
                        ls -ltr
                    
                    """
                }
            }
        // stage('Publish Artifact') { // nexus artifact uploader plugin
        //     steps {
        //         nexusArtifactUploader(
        //             nexusVersion: 'nexus3',
        //             protocol: 'http',
        //             nexusUrl: "${nexusURL}",
        //             //nexusUrl: '172.31.74.236:8081',
        //             //nexusURL: pipelineGlobals.nexusURL(),
        //             groupId: 'com.hipstershop',
        //             //version: '1.0.0',
        //             version: "${packageVersion}",
        //             repository: "${configMap.component}",
        //             credentialsId: 'nexus-auth', // store nexus credentials
        //             artifacts: [
        //                 [artifactId: "${configMap.component}",
        //                 classifier: '',
        //                 file: "${configMap.component}.jar",
        //                 type: 'jar']
        //             ]
        //         )
        //     }
        // }
        // stage('Deploy') {
        //     when {
        //         expression {
        //             params.Deploy
        //         }
        //     }
        //     steps {
        //         script {
        //             def params = [
        //                 string(name: 'version', value:"$packageVersion"),
        //                 string(name: 'environment', value:"dev")
        //                 // booleanParam(name: 'create', value: "${params.Deploy}")
        //             ]
        //             build job: "../${configMap.component}-deploy", wait: true, parameters: params
                    
        //         }
        //     }
        // }  
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