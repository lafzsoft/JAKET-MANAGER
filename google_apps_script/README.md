# Panduan Pemasangan Google Apps Script - Sistem Inventaris Gudang Jaket

Kode ini membawa **SELURUH FITUR APLIKASI** ke dalam Google Apps Script (GAS) dan terhubung otomatis dengan **Google Sheets**.

---

## 🌟 Seluruh Fitur yang Telah Dimasukkan:
1. **Database Google Sheets Otomatis**: Otomatis membuat sheet `Inventory` lengkap dengan 16 kolom data dan data awal sampel.
2. **Kartu Ringkasan Stok & Varian Unik (Top Summary Card)**:
   - Total unit stok tersedia
   - Jumlah varian SKU unik terdaftar
   - Status aman vs perlu restock
3. **Katalog Grid Vertikal Interaktif (Lazy Grid)**:
   - Gambar produk dengan fallback otomatis
   - Badge status warna (Tersedia / Menipis / Habis)
   - Penyesuaian stok langsung (+ / -) di kartu
   - Switcher tampilan Grid dan Tabel (List)
4. **Search Bar di Bagian Atas**:
   - Pencarian instan berdasarkan **Nama Jaket** atau **Model Gaya** (Bomber, Parka, Denim, Trench, Puffer)
   - Tombol shortcut pencarian cepat
   - Indikator pencarian aktif dengan tombol reset
5. **Filter & Pengurutan Lengkap**:
   - Filter Status (Semua, Tersedia, Menipis, Habis)
   - Urutkan berdasarkan Stok (Banyak/Sedikit), Nama (A-Z), atau Gaya
6. **Modal Detail Produk Lengkap (Expanded View)**:
   - Foto besar & harga unit
   - Deskripsi kontekstual otomatis sesuai bahan & model
   - Pemilih varian warna aktif
   - Rincian stok & lokasi rak
   - Matriks distribusi ukuran lengkap dari **XS, S, M, L, XL, XXL, hingga 3XL**
   - Quick stepper & tombol Restock +15
7. **Form Tambah Jaket Baru**:
   - Input lengkap termasuk ukuran XXL & 3XL
8. **Pengubah Foto SKU**:
   - Ganti URL gambar produk langsung dari aplikasi web
9. **Ekspor CSV**:
   - Download backup data inventaris ke format file `.csv`
10. **Dukungan Dua Bahasa**:
    - Toggle Bahasa Indonesia dan English

---

## 🚀 Cara Pemasangan di Google Sheets & Google Apps Script

### Opsi Tercepat (Hanya 1 File Code.gs - Bebas Error 100%)
Kode di `Code.gs` telah dilengkapi **fitur Anti-Error**! Anda cukup menyalin isi `Code.gs`:

1. Buka [Google Sheets](https://sheets.new) di browser Anda.
2. Di menu atas, klik **Extensions (Ekstensi)** > **Apps Script**.
3. Hapus semua teks yang ada di editor, lalu **Paste** seluruh isi dari file `Code.gs`.
4. Klik tombol **Save (Simpan)** 💾.
5. Klik **Deploy (Terapkan)** di pojok kanan atas > **New deployment (Penerapan baru)**.
6. Klik ikon gerigi (Select type) > Pilih **Web app (Aplikasi web)**.
7. Konfigurasi:
   - **Description**: `Inventaris Gudang Jaket v1.0`
   - **Execute as**: `Me (Saya)`
   - **Who has access**: `Anyone (Siapa saja)`
8. Klik **Deploy (Terapkan)** dan berikan izin akses (Authorize access).
9. Buka URL Web App yang diberikan. Aplikasi langsung berjalan dengan semua fitur!

---

### Solusi untuk Error Sebelumnya:
> *"Exception: File HTML dengan nama Index tidak ditemukan. (baris 13, file 'code')"*

Error tersebut sebelumnya terjadi karena di editor Apps Script hanya ada file `Code.gs`, sementara script mencari file `Index.html`.
Pada kode terbaru ini:
- **Solusi 1**: Script kami otomatis mendeteksi jika file `Index` belum dibuat, dan langsung memuat tampilan HTML lengkap tanpa error!
- **Solusi 2**: Jika Anda ingin memisahkan file, di editor Apps Script klik tanda **+** di samping Files > pilih **HTML** > beri nama **Index** (tanpa ekstensi .html karena Google Script otomatis menambahkannya), lalu paste isi dari `Index.html`.
