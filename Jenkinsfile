@Library('vl-jenkins')_

import no.nav.jenkins.*

def maven = new maven()
def fpgithub = new fpgithub()
def version
def GIT_COMMIT_HASH
def GIT_COMMIT_HASH_FULL
pipeline {
    agent any

    stages {

        stage('Checkout scm') { // checkout only tags.
            steps {
                script {
                    Date date = new Date()

                    checkout scm
                    GIT_COMMIT_HASH = sh(script: "git log -n 1 --pretty=format:'%h'", returnStdout: true)
                    GIT_COMMIT_HASH_FULL = sh(script: "git log -n 1 --pretty=format:'%H'", returnStdout: true)
                    changelist = "_" + date.format("YYYYMMDDHHmmss") + "_" + GIT_COMMIT_HASH
                    mRevision = maven.revision()
                    version = mRevision + changelist

                    currentBuild.displayName = version

                    echo "Building $version"
                }
            }
        }

        stage('Build branch') {
            when {
                not {
                    anyOf {
                        branch "master"
                    }
                }
            }
            steps {
                script {

                    def mRevision = maven.revision()
                    def tagName = env.BRANCH_NAME.tokenize('/')[-1] + "-" + mRevision
                    currentBuild.displayName = tagName + "-SNAPSHOT"

                    configFileProvider(
                            [configFile(fileId: 'navMavenSettings', variable: 'MAVEN_SETTINGS')]) {
                        artifactId = maven.artifactId()
                        buildEnvironment = new buildEnvironment()
                        if (maven.javaVersion() != null) {
                            buildEnvironment.overrideJDK(maven.javaVersion())
                        }

                        sh "mvn -U -B -s $MAVEN_SETTINGS -Dfile.encoding=UTF-8 -DinstallAtEnd=true -DdeployAtEnd=true -Dsha1= -Dchangelist= -Drevision=$tagName clean deploy"

                    }
                }
            }
        }


        stage('Build master') {
            when {
                branch 'master'
            }
            steps {
                script {
                    configFileProvider(
                            [configFile(fileId: 'navMavenSettings', variable: 'MAVEN_SETTINGS')]) {
                        artifactId = maven.artifactId()
                        buildEnvironment = new buildEnvironment()
                        if (maven.javaVersion() != null) {
                            buildEnvironment.overrideJDK(maven.javaVersion())
                        }

                        sh "mvn -U -B -s $MAVEN_SETTINGS -Dfile.encoding=UTF-8 -DinstallAtEnd=true -DdeployAtEnd=true -Dsha1= -Dchangelist= -Drevision=$version clean install"
                    }
                }
            }
        }

        stage('Tag master') {
            when {
                branch 'master'
            }
            steps {
                sh "git tag $version -m $version"
                sh "git push origin --tag"
            }
        }


    }

    post {
        success {
            script {
                fpgithub.updateBuildStatus("fp-felles", "success", GIT_COMMIT_HASH_FULL)
            }
        }
        failure {
            script {
                fpgithub.updateBuildStatus("fp-felles", "failure", GIT_COMMIT_HASH_FULL)
            }
        }
    }
}
