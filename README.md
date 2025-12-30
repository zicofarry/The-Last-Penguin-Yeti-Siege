# JANJI

Saya **Muhammad 'Azmi Salam** dengan NIM **2406010** mengerjakan **Tugas Masa Depan** dalam mata kuliah **Desain Pemrograman Berorientasi Objek** untuk keberkahanNya maka saya tidak melakukan kecurangan seperti yang telah dispesifikasikan. Aamiin.


## The Last Penguin: Yeti Siege

**The Last Penguin: Yeti Siege** adalah aplikasi permainan desktop berbasis Java yang mengusung mekanisme *top-down shooter*. Proyek ini dirancang menggunakan arsitektur **Model-View-Presenter (MVP)** untuk memisahkan logika permainan, manajemen data, dan antarmuka pengguna secara modular. Aplikasi ini mengintegrasikan sistem basis data hibrida (SQLite dan MySQL) untuk pengelolaan data lokal dan kompetisi global.


## 1. Persyaratan Sistem

Untuk memastikan aplikasi berjalan dengan optimal, perangkat Anda harus memenuhi persyaratan berikut:

* **Java Development Kit (JDK)**: Versi 8 atau versi yang lebih tinggi.
* **Java Runtime Environment (JRE)**: Versi 8 atau versi yang lebih tinggi.
* **MySQL Server**: Diperlukan untuk fungsionalitas papan peringkat (*leaderboard*) global dalam mode **ONLINE**.
* **Driver Konektivitas**:
* `sqlite-jdbc-3.51.1.0.jar`: Untuk manajemen basis data lokal.
* `mysql-connector-j-9.5.0.jar`: Untuk manajemen koneksi server MySQL.




## 2. Struktur Proyek

Berikut adalah representasi struktur direktori proyek:

```text
The-Last-Penguin-Yeti-Siege/
├── antarctica_mysql.sql
├── antarctica_sqlite.sql
├── data/
│   └── antarctica.db
├── lib/
│   ├── mysql-connector-j-9.5.0.jar
│   └── sqlite-jdbc-3.51.1.0.jar
├── res/
│   └── assets/
│       ├── fonts/
│       ├── images/
│       └── sounds/
└── src/
    └── com/lastpenguin/
        ├── Main.java
        ├── model/
        │   ├── GameSettings.java
        │   ├── Meteor.java
        │   ├── MySQLManager.java
        │   ├── Obstacle.java
        │   ├── Player.java
        │   ├── Projectile.java
        │   ├── SQLiteManager.java
        │   └── Yeti.java
        ├── presenter/
        │   ├── GamePresenter.java
        │   └── InputHandler.java
        └── view/
            ├── AssetLoader.java
            ├── GamePanel.java
            ├── GameWindow.java
            ├── HUD.java
            ├── MenuPanel.java
            ├── SettingsPanel.java
            └── Sound.java
```


## 3. Analisis Berkas & Penerapan OOP

Setiap komponen dalam proyek ini menerapkan prinsip dasar Pemrograman Berorientasi Objek untuk menjamin kemudahan pemeliharaan kode:

### 3.1 Paket `com.lastpenguin.model` (Logika Data)

* **Player.java**: Menerapkan **Enkapsulasi** untuk mengelola status karakter (posisi, amunisi, status *ghost*) melalui metode *getter* dan *setter*.
* **Yeti.java**: Mengatur perilaku AI musuh dan tahapan animasi pergerakan secara mandiri.
* **Projectile.java**: Mengatur perilaku proyektil, termasuk kecepatan, arah, dan sifat penetrasi (*piercing*).
* **SQLiteManager & MySQLManager**: Menyediakan **Abstraksi** untuk operasi basis data sehingga logika penyimpanan terpisah dari logika inti permainan.

### 3.2 Paket `com.lastpenguin.presenter` (Logika Bisnis)

* **GamePresenter.java**: Menerapkan **Komposisi** dengan mengelola kumpulan objek (`List<Yeti>`, `List<Projectile>`, `List<Obstacle>`) dan mengoordinasikan interaksi antar objek tersebut dalam satu *game loop*.
* **InputHandler.java**: Memisahkan logika penangkapan input (Keyboard & Mouse) dari logika pergerakan, sehingga kontrol permainan bersifat modular dan mudah diubah.

### 3.3 Paket `com.lastpenguin.view` (Antarmuka Pengguna)

* **AssetLoader.java**: Menerapkan prinsip **Singleton-like Utility** untuk memastikan aset (gambar, font) dimuat secara efisien ke dalam memori hanya satu kali, mencegah pemborosan sumber daya.
* **GamePanel.java**: Berperan sebagai kanvas utama yang menerapkan **Pemisahan Tanggung Jawab (Separation of Concerns)**; fokus hanya pada proses rendering visual dari data yang diberikan oleh Presenter.
* **HUD.java**: Mengenkapsulasi logika tampilan informasi permainan (skor, amunisi, bar kesehatan) agar tidak bercampur dengan logika rendering dunia permainan utama.
* **Sound.java**: Mengelola sistem audio secara mandiri melalui **Abstraksi** kontrol suara (play, stop, volume), sehingga perubahan pada sistem audio tidak memengaruhi logika permainan lainnya.
* **MenuPanel & SettingsPanel**: Menggunakan **Modularitas UI** dengan membagi layar aplikasi menjadi beberapa panel yang dapat digonta-ganti dengan mudah di dalam satu `GameWindow`.


## 4. Alur Kerja Aplikasi (Workflow)

Aplikasi beroperasi melalui urutan proses berikut:

1. **Siklus Awal**: `Main.java` menginisialisasi basis data SQLite dan memuat `GameSettings` terakhir.
2. **Manajemen Skor**: Sistem memuat data High Score ke menu utama. Jika mode **ONLINE** aktif, data ditarik dari MySQL melalui `MySQLManager.getGlobalLeaderboard()`.
3. **Inti Permainan**: `GamePresenter` memperbarui posisi entitas, mendeteksi tabrakan menggunakan `Rectangle.intersects()`, dan menangani penggunaan amunisi serta *skill*.
4. **Sinkronisasi Data**: Saat *Game Over*, skor secara otomatis disimpan ke SQLite lokal. Jika dalam mode Online, data juga dikirim ke server MySQL melalui *background thread* untuk mencegah UI membeku.


## 5. Fitur Bonus

* **Sistem High Score & Leaderboard**:
  1. Leaderboard Lokal: Menyimpan riwayat skor tertinggi berdasarkan tingkat kesulitan di SQLite lokal.
  2. Leaderboard Global: Menampilkan peringkat 50 besar pemain dari seluruh dunia melalui server MySQL.
* **Sistem Kemampuan Khusus (Skills)**:
  1. Skill 1 (Giant Snowball): Tembakan berukuran besar dengan kemampuan menembus musuh.
  2. Skill 2 (Meteor Strike): Serangan area (AoE) yang menghancurkan musuh dan menciptakan rintangan lingkungan baru
  3. Skill 3 (Invisible): Mode transparan yang membuat musuh kehilangan jejak pemain.
* **Kontrol Hibrida**: Mendukung penggunaan Mouse untuk membidik dan menembak atau sepenuhnya menggunakan Keyboard.
* **Mode Kesulitan**: Tersedia pilihan EASY, MEDIUM, dan HARD yang memengaruhi dinamika permainan dan pencatatan skor.


## 6. Panduan Kompilasi dan Eksekusi

### 6.1 Kompilasi

Kompilasi seluruh kode sumber ke direktori `bin` dengan menyertakan pustaka yang diperlukan:

```bash
javac -d bin -cp "lib/sqlite-jdbc-3.51.1.0.jar;lib/mysql-connector-j-9.5.0.jar" src/com/lastpenguin/Main.java src/com/lastpenguin/model/*.java src/com/lastpenguin/view/*.java src/com/lastpenguin/presenter/*.java
```

### 6.2 Eksekusi

Jalankan aplikasi dengan menyertakan direktori `bin`, folder aset `res`, dan pustaka eksternal ke dalam *classpath*:

```bash
java -cp "bin;res;lib/sqlite-jdbc-3.51.1.0.jar;lib/mysql-connector-j-9.5.0.jar" com.lastpenguin.Main
```

*(Gunakan titik dua `:` sebagai pemisah path jika Anda menggunakan sistem operasi berbasis Linux atau macOS).*
