# GuberStudio - Android App & Vercel Web Download Portal

Aplikasi Android Kiosk Browser yang dioptimalkan untuk Gemini AI Studio (Image Generator & Voice TTS Generator) dengan dukungan penuh pengunduhan file suara WAV (blob & data URL), gambar, video, dan dokumen langsung ke perangkat Android.

---

## 🚀 Fitur Utama Aplikasi

- **Unduh Suara WAV (TTS):** Menangani audio blob & data URL hasil generator suara TTS, menyimpannya langsung ke folder `Downloads` perangkat dengan nama dinamis dan notifikasi unduhan selesai.
- **Dukungan Unduhan Semua Format File:** Gambar (PNG, JPG, JPEG), Audio (MP3, WAV), Video (MP4), Dokumen (PDF), dan Arsip (ZIP, RAR, APK).
- **Kiosk Full-Screen Experience:** Tampilan layar penuh bersih tanpa header ganda atau tombol bawaan yang mengganggu.
- **Notifikasi & Alur Aman:** Konfirmasi penyimpanan file di perangkat sebelum melanjutkan alur eksternal.

---

## 📦 Cara Deploy ke Vercel (Web Download Portal)

Proyek ini sudah dilengkapi dengan `index.html` dan `vercel.json` yang siap di-deploy langsung ke **Vercel** sebagai halaman unduh APK untuk publik:

1. **Push proyek ke GitHub:**
   - Gunakan menu **Push to GitHub** di Google AI Studio, atau gunakan Git CLI:
     ```bash
     git remote add origin https://github.com/USERNAME/REPO_NAME.git
     git push -u origin main
     ```
2. **Buka [vercel.com](https://vercel.com) & Login dengan GitHub:**
   - Klik **"Add New..."** ➜ **"Project"**.
   - Pilih repositori GitHub proyek ini.
   - Vercel akan otomatis mengenali proyek sebagai **Static Site** (tidak perlu ubah setting build command).
   - Klik **"Deploy"**.
3. **Selesai!**
   - Vercel akan memberikan domain gratis (contoh: `https://guberstudio.vercel.app`).
   - Siapa pun yang membuka tautan tersebut dapat langsung mengklik tombol **"Unduh APK Sekarang"** untuk mengunduh `guberstudio.apk` ke HP Android mereka.

---

## 🛠️ Build APK Manual (Android Studio / Gradle)

Jika Anda ingin mengompilasi ulang file APK dari sumber:

```bash
# Debug APK
gradle assembleDebug

# Output APK berlokasi di:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 📱 Panduan Instalasi di Smartphone Android

1. Download file `guberstudio.apk` melalui halaman Vercel.
2. Jika muncul peringatan browser *"File might be harmful"*, pilih **Download anyway** / **Tetap download**.
3. Buka file yang selesai diunduh dan pilih **Install**.
4. Jika muncul prompt izin, aktifkan **"Allow from this source"** (Izinkan dari sumber ini).
5. Buka aplikasi GuberStudio.
