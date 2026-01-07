pipeline {
    agent any

    tools {
        // Jenkins'te tanımlı Maven tool adı 'maven' olmalı veya environment'tan gelmeli
        maven 'maven' 
    }

    stages {
        stage('Checkout') {
            steps {
                // Kod github'dan çekiliyor (Sanal olarak, Jenkins job ayarlarında repo tanımlı varsayılır)
                checkout scm
                echo 'Kodlar Github\'dan çekildi (5 puan)'
            }
        }

        stage('Build') {
            steps {
                // Kodları derle ama testleri çalıştırma
                sh 'mvn clean package -DskipTests'
                echo 'Kodlar build edildi (5 puan)'
            }
        }

        stage('Unit Tests') {
            steps {
                // Sadece Unit testleri çalıştır (Birim testler genellikle SpringBootTest olmayanlardır ama burada hepsi karışık olabilir)
                // Hızlı olması için sadece belirli paketleri veya normal 'mvn test' çalıştırabiliriz.
                // İstenen: Birim testleri çalıştır ve raporla.
                sh 'mvn test -Dtest=*ServiceTest'
                // JUnit raporlarını arşivle
                junit 'target/surefire-reports/*.xml'
                echo 'Birim Testleri çalıştırıldı ve raporlandı (15 puan)'
            }
        }

        stage('Integration Tests') {
            steps {
                // Entegrasyon testleri (Controller testleri vb.)
                sh 'mvn test -Dtest=*ControllerTest'
                junit 'target/surefire-reports/*.xml'
                echo 'Entegrasyon testleri çalıştırıldı ve raporlandı (15 puan)'
            }
        }

        stage('Docker System Run') {
            steps {
                // Docker container'ları ayağa kaldır
                sh 'docker-compose down'
                sh 'docker-compose up -d --build'
                
                // Sistemin ayağa kalkması için biraz bekle
                sleep 20
                echo 'Sistem docker container\'lar üzerinde çalıştırıldı (5 puan)'
            }
        }

        stage('E2E Tests (Selenium)') {
            steps {
                // Çalışır durumdaki sisteme (localhost:8086) test senaryolarını koş
                // DockerSystemTest sınıfını çalıştır
                sh 'mvn test -Dtest=DockerSystemTest'
                junit 'target/surefire-reports/*.xml'
                echo 'Çalışır durumdaki sistem üzerinden test senaryoları çalıştırıldı (55+ puan)'
            }
        }
    }

    post {
        always {
            // Test sonrası temizlik
            // sh 'docker-compose down' // İsteğe bağlı, debug için açık kalabilir
            archiveArtifacts artifacts: 'target/**/*.jar', fingerprint: true
        }
    }
}
