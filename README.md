# Famly - Pemantauan Aktivitas Keluarga & Kontrol Orang Tua

**Famly** adalah aplikasi Android untuk pemantauan keluarga yang membantu orang tua memantau notifikasi, lokasi, dan aktivitas perangkat anak secara lokal, lalu meneruskan ringkasannya ke Bot Telegram orang tua. Dirancang untuk pemakaian berbasis persetujuan antara orang tua dan anak dalam satu keluarga.

> Catatan nama: proyek ini sebelumnya bernama **NotifVault** (`applicationId = com.aistudio.notifvault.wqvzkr`). Nama tampilan sekarang adalah **Famly**.

---

## 🌟 Fitur Utama

- **🛡️ Perekam & Monitoring Notifikasi**
  - Merekam secara lokal seluruh riwayat notifikasi (pesan chat, pengingat, aplikasi sosial, perbankan, dll.) pada perangkat anak.
  - Pengelompokan kategori otomatis berbasis aturan (Pesan & Chat, Media Sosial, Keuangan & Bank, Belanja, Email & Kerja, Hiburan, Sistem, Lainnya).
  - Deduplikasi cerdas agar pesan yang belum dibaca tidak dikirim ulang ke Telegram.

- **📍 Pelacakan Lokasi via Bot Telegram**
  - Orang tua dapat meminta koordinat GPS anak secara real-time melalui Bot Telegram pribadi.
  - Mengembalikan pin lokasi native Telegram dan tautan langsung ke Google Maps.

- **🤖 Integrasi Bot Telegram**
  - Meneruskan notifikasi dan update lokasi ke akun Telegram orang tua.
  - Command tersedia: `/lokasi`, `/ping`, `/scan` (Wi-Fi & Bluetooth), `/screenshot`, `/app`, `/start`, `/help`.
  - Opsi privasi `Exclude Sensitive` untuk mengabaikan notifikasi OTP/kode rahasia (perlu diaktifkan manual di layar Telegram).

- **🔢 Penyamaran Ikon (Stealth Calculator)**
  - Mengubah ikon launcher menjadi Kalkulator fungsional.
  - Dasbor Famly dibuka dengan memasukkan PIN 4-digit lalu menekan tombol `=` pada kalkulator.

- **🔐 Keamanan & Enkripsi Lokal**
  - Perlindungan PIN 4-digit (hash SHA-256) untuk membuka vault.
  - Isi notifikasi sensitif (perbankan/OTP) dienkripsi dengan AES-256 GCM via AndroidKeyStore.
  - Token Bot Telegram dan Chat ID disimpan di `EncryptedSharedPreferences` (AndroidX Security, AES-256 GCM) sehingga tidak tersisa sebagai teks biasa di perangkat.
  - Semua data tersimpan secara lokal di perangkat anak.

- **📞 Pencatatan Panggilan & Aktivitas**
  - Mencatat event panggilan masuk/keluar/terlewat dan aplikasi yang sedang dibuka anak (via Accessibility Service).

---

## 🤖 Command Bot Telegram

| Perintah | Fungsi |
| --- | --- |
| `/lokasi` | Minta koordinat GPS anak + tautan Maps |
| `/ping` | Cek status layanan & GPS perangkat anak |
| `/scan` | Pindai Wi-Fi & perangkat Bluetooth di sekitar |
| `/screenshot` | Ambil tangkapan layar perangkat anak |
| `/app` | Lihat aplikasi yang sedang dibuka anak |
| `/start`, `/help` | Tampilkan daftar perintah |

Hanya `Chat ID` yang dikonfigurasi yang diperbolehkan mengirim command (command dari chat lain diabaikan).

---

## 🛠️ Persyaratan Sistem

- **OS Minimum**: Android 7.0 (API Level 24) atau lebih baru (target & compile SDK 36).
- **Izin yang diminta**:
  - `INTERNET` — penerusan data ke Bot Telegram.
  - `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` / `ACCESS_BACKGROUND_LOCATION` — pelacakan GPS.
  - `BIND_NOTIFICATION_LISTENER_SERVICE` — membaca notifikasi.
  - `READ_PHONE_STATE`, `READ_CALL_LOG`, `PROCESS_OUTGOING_CALLS` — pencatatan panggilan.
  - `ACCESSIBILITY_SERVICE` (dinamis) — deteksi aplikasi aktif & screenshot.
  - `MEDIA_PROJECTION` (dinamis) — fallback screenshot layar.
  - `QUERY_ALL_PACKAGES` — filter aplikasi.
  - `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`, `RECEIVE_BOOT_COMPLETED` — menjaga layanan latar belakang tetap aktif.

> Beberapa izin bersifat opsional dan hanya digunakan jika fitur terkait diaktifkan di dalam aplikasi.

---

## 🏗️ Cara Build (GitHub Actions)

Repo ini menyertakan workflow `.github/workflows/build-apk.yml` yang:

1. Checkout, siapkan Java 17 + Gradle 9.3.1.
2. Membuat debug keystore sementara.
3. Build release APK: `gradle :app:assembleRelease`.
4. Mengunggah APK sebagai artifact dan menerbitkannya ke GitHub Releases.

Cara memicu:
- Push ke branch `main`, atau
- Jalankan manual lewat tab **Actions → Build Debug APK and Publish Release → Run workflow**.

Hasil APK release dapat diunduh dari halaman GitHub Releases (aset `notify-release-build-*.apk`) atau dari artifact `app-release-apk` pada setiap run.

---

## 🔒 Pernyataan Sanggahan Hukum (Legal Disclaimer)

> **DISCLAIMER PENGEMBANG**:
> Aplikasi **Famly** dirancang khusus sebagai sarana pemantauan keselamatan anak berbasis persetujuan keluarga yang sah. Pengembang sepenuhnya dibebaskan dari segala bentuk tuntutan hukum, tanggung jawab pidana maupun perdata, serta kerugian yang timbul akibat penyalahgunaan, pemantauan tanpa izin, atau pelanggaran privasi oleh pengguna. Pastikan penggunaan aplikasi ini mematuhi hukum yang berlaku di yurisdiksi kamu.

---

## 📜 Lisensi

Dikembangkan untuk perlindungan dan keselamatan keluarga.
