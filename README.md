# MDD Project – Mon Projet Fullstack (Spring Boot + Angular)

## Description

Ce projet est une application web fullstack construite avec :

- **Backend** : Spring Boot 3 (API REST sécurisée avec JWT)  
- **Frontend** : Angular (application standalone avec NgRx, Signals, ReactiveForms)  
- **Base de données** : MySQL  
- **Authentification** : JSON Web Token (JWT)  

### Fonctionnalités principales

- Gestion des utilisateurs (création, connexion, mise à jour, changement de mot de passe)  
- Création et gestion des articles avec des thèmes  
- Gestion des abonnements aux thèmes  
- Authentification et autorisation via JWT  

---

## Configuration

### Fichier `application.properties`

```properties
# MySQL Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/MDD
spring.datasource.username=yourUsername
spring.datasource.password=yourPassword

# JWT Secret
jwt.secret=ThisIsYourSuperSecretKeyThatIsLongEnoughToBeSafe1234567890abcDEF!!

# JPA / Hibernate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.format_sql=true

# Tomcat server
server.port=3001
server.error.include-message=always
server.error.include-binding-errors=always
server.error.include-stacktrace=on_param

# Logger
logging.level.root=INFO
logging.level.org.springframework=INFO
logging.level.com.yourpackage=DEBUG
logging.level.com.openclassrooms=INFO
logging.level.org.springframework.boot.web.embedded.tomcat=INFO
```


### Installation
#### Backend (Spring Boot)

Prérequis :

Java 17+

Maven

MySQL en local

Démarrage :

cd backend
mvn clean install
mvn spring-boot:run


L’API sera disponible sur :
http://localhost:3001

#### Base de données (MySQL)

Créer la base de données :

CREATE DATABASE MDD;

##### Frontend (Angular)

Prérequis :

Node.js 20+

Angular CLI

Installation et lancement :

cd frontend
npm install
ng serve --open


L’application sera disponible sur :
http://localhost:4200


#### API Endpoints 
##### Auth

POST /api/auth/register → inscription

POST /api/auth/login → connexion

##### User

GET /api/users/me → infos utilisateur connecté

PUT /api/users → mise à jour profil (username/email)

PUT /api/users/password → changement de mot de passe

##### Articles

GET /api/articles → liste des articles

POST /api/articles → créer un article

GET /api/articles/{id} → détail d’un article

##### Thèmes

GET /api/themes → liste des thèmes

POST /api/themes/{id}/subscribe → s’abonner

DELETE /api/themes/{id}/unsubscribe → se désabonner
