📆 Belman Upload System

Et JavaFX-baseret applikationssystem til billed-upload og godkendelse i QA- og produktionsmiljøer.
Dette applikationssystem er designet for belman a/s, der ønsker at optimere deres upload- og godkendelsesprocesser.

Forudsætninger for åbning af program:

Java 17+ (eller Java 21)

Maven (eller IntelliJ IDEA med Maven support)

MySQL server (v8.x eller MariaDB)

SceneBuilder (valgfrit til FXML-redigering)

🔧 Opsætning

1. Klon projektet

git clone https://github.com/OnemanF/BelmanProduction.git
cd belman-upload-system

2. Åbn i IntelliJ IDEA

Gå til File > Open... > Vælg projektmappen

IntelliJ vil automatisk importere pom.xml

📊 Databaseopsætning

Databasen hedder BelmanSystems (datagrip anbefales)

Opret en mappe med filnavnet Config.Settings

I rapporten er information omkring Login oplysninger, 
som skal tilføjes til filen Config.Settings

📁 Projektstruktur

src/
├── BE/              → Dataobjekter (User, UploadEntry)
├── BLL/             → Forretningslogik (UserBLL, UploadBLL)
├── DAL/             → Databaseadgang (UserDal, UploadDal)
├── Gui/
│   ├── Controller/ → JavaFX-controllere
│   ├── Model/      → ViewModels (UserModel, UploadModel)
│   └── View/       → FXML + styling
├── Interface/       → Interface-lag (Useri, Uploadi)
├── Utility/         → Hjælpeklasser (Settings, ModelException)
└── Main.java        → Program-start

📖 Maven-afhængigheder

Følgende libraries bruges:

📅 pom.xml afhængigheder

<dependencies>
    <!-- JavaFX -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>17.0.6</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>17.0.6</version>
    </dependency>

    <!-- PDF export support -->
    <dependency>
        <groupId>org.apache.pdfbox</groupId>
        <artifactId>pdfbox</artifactId>
        <version>2.0.29</version>
    </dependency>

    <!-- JUnit 5 for testing -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-api</artifactId>
        <version>5.10.2</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-engine</artifactId>
        <version>5.10.2</version>
        <scope>test</scope>
    </dependency>

    <!-- MySQL JDBC driver -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.33</version>
    </dependency>

    <!-- Bcrypt password hashing -->
    <dependency>
        <groupId>at.favre.lib</groupId>
        <artifactId>bcrypt</artifactId>
        <version>0.9.0</version>
    </dependency>
</dependencies>

🚂 Kør projektet (CLI / Maven)

Via IntelliJ:

Højreklik Main.java > Run

Via Maven:

mvn clean javafx:run

Hvis du bruger CLI:

java --module-path "/sti/til/javafx-sdk-17/lib" --add-modules javafx.controls,javafx.fxml -jar target/Belman-1.0-SNAPSHOT.jar

💡 Tips for Udviklere

Brug ModelException til at fange fejl i model-laget

Useri og Uploadi sikrer testbarhed via interface-abstraktion

BLL indeholder kun forretningslogik

FXML-filer opdateres nemt i SceneBuilder

👤 Bidrag

Fork repo

Opret en ny feature branch

Lav dine ændringer

Commit og lav en Pull Request



