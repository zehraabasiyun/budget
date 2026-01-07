pipeline {
    agent any

    tools {
        maven 'maven' 
    }

    environment {
        IMAGE_NAME = "butce-app-image"
        CONTAINER_NAME = "butce-app-container"
    }

    stages {
        stage('1. Checkout') {
            steps {
                checkout scm
                echo 'Kodlar Github\'dan çekildi (5 puan)'
            }
        }

        stage('2. Build') {
            steps {
                bat 'mvn clean package -DskipTests'
                echo 'Kodlar build edildi (5 puan)'
            }
        }

        stage('3. Unit Tests') {
            steps {
                bat 'mvn test'
                
                junit 'target/surefire-reports/*.xml'
                echo 'Birim Testleri çalıştırıldı ve raporlandı (15 puan)'
            }
        }

        stage('4. Integration Tests') {
            steps {
                bat 'mvn verify -DskipUnitTests'
                
                junit 'target/failsafe-reports/*.xml'
                echo 'Entegrasyon testleri çalıştırıldı ve raporlandı (15 puan)'
            }
        }

        stage('5. Docker System Run') {
            steps {
                script {
                    echo 'Docker temizliği yapılıyor...'
                    try {
                        bat "docker stop ${CONTAINER_NAME}"
                        bat "docker rm ${CONTAINER_NAME}"
                    } catch (Exception e) {
                        echo 'Temizlenecek eski container bulunamadı, devam ediliyor.'
                    }

                    echo 'Docker build ve run işlemi...'
                    bat "docker build -t ${IMAGE_NAME} ."
                    bat "docker run -d -p 8090:8080 --name ${CONTAINER_NAME} ${IMAGE_NAME}"
                    
                    echo 'Sistemin ayağa kalkması bekleniyor (20sn)...'
                    sleep 20
                }
                echo 'Sistem docker container\'lar üzerinde çalıştırıldı (5 puan)'
            }
        }

        stage('6. E2E Tests (Selenium)') {
            steps {
                bat 'mvn test -Dtest=SeleniumSystemTest'
                
                echo 'Çalışır durumdaki sistem üzerinden test senaryoları çalıştırıldı (55+ puan)'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'target/**/*.jar', fingerprint: true
            
            script {
                try {
                    bat "docker stop ${CONTAINER_NAME}"
                    bat "docker rm ${CONTAINER_NAME}"
                } catch (Exception e) {}
            }
        }
    }
}
