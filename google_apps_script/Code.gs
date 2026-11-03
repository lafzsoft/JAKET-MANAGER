/**
 * =========================================================================
 * GOOGLE APPS SCRIPT: SISTEM MANAJEMEN INVENTARIS GUDANG & PRODUKSI JAKET
 * File: Code.gs (Backend Server Apps Script)
 * =========================================================================
 * 
 * Fitur Komprehensif:
 * 1. Web App Server: Menyajikan Index.html dengan dukungan Recharts & Web Scanner
 * 2. Database Spreadsheet Otomatis:
 *    - Sheet 'Inventory': Katalog jaket, SKU, ukuran, warna, harga, foto, status stok
 *    - Sheet 'DailyProduction': Catatan produksi harian, operator, shift, defect, batch
 * 3. Inventaris: Tambah, edit, restock, restock massal, hapus, update foto SKU
 * 4. Produksi Harian: Catat batch produksi harian, hitung defect rate & target fulfillment
 * 5. Barcode & QR Code Lookup: Verifikasi SKU & integrasi pemindai label produk
 * 6. Analitik Gudang: Perhitungan valuasi aset, distribusi model gaya & ukuran
 * 7. Ekspor CSV: Unduh rekapan inventaris & produksi dalam format CSV
 */

var INVENTORY_SHEET = 'Inventory';
var PRODUCTION_SHEET = 'DailyProduction';

/**
 * Endpoint utama Web App doGet
 * Membaca dan me-render template dari file Index.html
 */
function doGet() {
  return HtmlService.createTemplateFromFile('Index')
    .evaluate()
    .setTitle('Sistem Inventaris Gudang & Produksi Jaket')
    .addMetaTag('viewport', 'width=device-width, initial-scale=1.0')
    .setXFrameOptionsMode(HtmlService.XFrameOptionsMode.ALLOWALL);
}

/**
 * Mengambil atau membuat sheet berdasarkan nama pada Spreadsheet aktif
 */
function getOrCreateSheet(sheetName) {
  var name = sheetName || INVENTORY_SHEET;
  var ss;
  try {
    ss = SpreadsheetApp.getActiveSpreadsheet();
  } catch (e) {
    ss = null;
  }
  
  if (!ss) {
    ss = SpreadsheetApp.create('Database Inventaris & Produksi Gudang Jaket');
  }
  
  var sheet = ss.getSheetByName(name);
  
  if (!sheet) {
    sheet = ss.insertSheet(name);
    if (name === INVENTORY_SHEET) {
      var invHeaders = [
        'ID', 'SKU', 'Nama Jaket', 'Kategori', 'Model Gaya', 'Ukuran', 
        'Jumlah Stok', 'Batas Min', 'Bahan', 'Warna', 'Lokasi Rak', 
        'Harga Unit ($)', 'URL Gambar', 'Koleksi Musiman', 'Status Stok', 'Terakhir Diperbarui'
      ];
      sheet.appendRow(invHeaders);
      sheet.getRange(1, 1, 1, invHeaders.length)
        .setBackground('#1E293B')
        .setFontColor('#FFFFFF')
        .setFontWeight('bold');
      sheet.setFrozenRows(1);
      seedInitialStock(sheet);
    } else if (name === PRODUCTION_SHEET) {
      var prodHeaders = [
        'ID', 'Tanggal', 'Tanggal Teks', 'ID Jaket', 'Nama Jaket', 'SKU Jaket',
        'Nomor Batch', 'Shift', 'Nama Operator', 'Jumlah Produksi', 'Jumlah Cacat',
        'Status Produksi', 'Catatan', 'Ditambahkan ke Stok', 'Waktu Dibuat'
      ];
      sheet.appendRow(prodHeaders);
      sheet.getRange(1, 1, 1, prodHeaders.length)
        .setBackground('#0F172A')
        .setFontColor('#38BDF8')
        .setFontWeight('bold');
      sheet.setFrozenRows(1);
      seedInitialProduction(sheet);
    }
  }
  return sheet;
}

/**
 * Mengambil seluruh data jaket dari Google Sheet
 */
function getInventoryData() {
  try {
    var sheet = getOrCreateSheet(INVENTORY_SHEET);
    var data = sheet.getDataRange().getValues();
    
    if (data.length <= 1) {
      seedInitialStock(sheet);
      data = sheet.getDataRange().getValues();
    }
    
    var items = [];
    for (var i = 1; i < data.length; i++) {
      var row = data[i];
      if (!row[0]) continue;
      
      var qty = parseInt(row[6]) || 0;
      var minThresh = parseInt(row[7]) || 5;
      
      var status = 'IN_STOCK';
      if (qty <= 0) {
        status = 'OUT_OF_STOCK';
      } else if (qty <= minThresh) {
        status = 'LOW_STOCK';
      }
      
      items.push({
        rowIndex: i + 1,
        id: String(row[0]),
        sku: String(row[1]),
        name: String(row[2]),
        genderCategory: String(row[3] || 'Jaket Pria'),
        style: String(row[4] || 'Bomber Jacket'),
        size: String(row[5] || 'L'),
        quantity: qty,
        minThreshold: minThresh,
        material: String(row[8] || 'Cotton'),
        color: String(row[9] || 'Black'),
        locationRack: String(row[10] || 'Rack A-01'),
        unitPrice: parseFloat(row[11]) || 89.99,
        imageUrl: String(row[12] || ''),
        isSeasonal: Boolean(row[13] === true || String(row[13]).toLowerCase() === 'true'),
        status: status,
        updatedAt: String(row[15] || new Date().toISOString())
      });
    }
    
    var ssUrl = '';
    try {
      ssUrl = SpreadsheetApp.getActiveSpreadsheet().getUrl();
    } catch (_) {}
    
    return {
      success: true,
      jackets: items,
      spreadsheetUrl: ssUrl
    };
  } catch (err) {
    return { success: false, error: err.toString(), jackets: [] };
  }
}

/**
 * Mencari jaket berdasarkan kode QR / Barcode / SKU
 */
function lookupJacketByCode(code) {
  if (!code) return { success: false, error: 'Kode kosong' };
  var cleanCode = String(code).trim().toLowerCase();
  var inv = getInventoryData();
  
  if (!inv.success || !inv.jackets) {
    return { success: false, error: 'Gagal membaca inventaris' };
  }
  
  var matched = null;
  for (var i = 0; i < inv.jackets.length; i++) {
    var j = inv.jackets[i];
    if (j.sku.toLowerCase() === cleanCode || 
        j.id.toLowerCase() === cleanCode || 
        cleanCode.indexOf(j.sku.toLowerCase()) !== -1) {
      matched = j;
      break;
    }
  }
  
  if (matched) {
    return { success: true, matched: true, jacket: matched };
  }
  
  // Parse style/size suggestions if structured SKU e.g. JKT-BMB-BLK-L
  var parts = String(code).trim().split('-');
  var suggestedStyle = 'Bomber Jacket';
  var suggestedSize = 'M';
  
  if (parts.length >= 3) {
    var prefix = parts[1].toUpperCase();
    if (prefix === 'BMB') suggestedStyle = 'Bomber Jacket';
    else if (prefix === 'PRK') suggestedStyle = 'Parka';
    else if (prefix === 'DNM') suggestedStyle = 'Denim Jacket';
    else if (prefix === 'TRN') suggestedStyle = 'Trench Coat';
    else if (prefix === 'PUF') suggestedStyle = 'Puffer Jacket';
    
    var lastPart = parts[parts.length - 1].toUpperCase();
    if (['XS', 'S', 'M', 'L', 'XL', 'XXL', '3XL'].indexOf(lastPart) !== -1) {
      suggestedSize = lastPart;
    }
  }
  
  return {
    success: true,
    matched: false,
    scannedCode: String(code).trim(),
    suggestions: {
      style: suggestedStyle,
      size: suggestedSize
    }
  };
}

/**
 * Menambah jaket baru ke Google Sheet
 */
function addJacket(jacketData) {
  try {
    var sheet = getOrCreateSheet(INVENTORY_SHEET);
    var newId = 'jkt_' + new Date().getTime();
    var qty = parseInt(jacketData.quantity) || 0;
    var minThresh = parseInt(jacketData.minThreshold) || 5;
    
    var status = 'IN_STOCK';
    if (qty <= 0) status = 'OUT_OF_STOCK';
    else if (qty <= minThresh) status = 'LOW_STOCK';
    
    var defaultSku = 'JKT-' + (jacketData.style ? jacketData.style.substring(0, 3).toUpperCase() : 'GEN') + '-' + (jacketData.size || 'L');
    
    var row = [
      newId,
      jacketData.sku || defaultSku,
      jacketData.name || 'Jaket Baru',
      jacketData.genderCategory || 'Jaket Pria',
      jacketData.style || 'Bomber Jacket',
      jacketData.size || 'L',
      qty,
      minThresh,
      jacketData.material || 'Cotton',
      jacketData.color || 'Black',
      jacketData.locationRack || 'Rack A-01',
      parseFloat(jacketData.unitPrice) || 89.99,
      jacketData.imageUrl || 'https://mediadirhub.com/vibe/solid-black-jacket-front-with-minimal-branding.png',
      Boolean(jacketData.isSeasonal),
      status,
      new Date().toLocaleString('id-ID')
    ];
    
    sheet.appendRow(row);
    return { success: true, id: newId, message: 'Jaket berhasil ditambahkan ke inventaris!' };
  } catch (err) {
    return { success: false, error: err.toString() };
  }
}

/**
 * Menyesuaikan jumlah stok (+1 atau -1 atau jumlah spesifik)
 */
function adjustStock(jacketId, delta) {
  try {
    var sheet = getOrCreateSheet(INVENTORY_SHEET);
    var data = sheet.getDataRange().getValues();
    
    for (var i = 1; i < data.length; i++) {
      if (String(data[i][0]) === String(jacketId)) {
        var currentQty = parseInt(data[i][6]) || 0;
        var newQty = Math.max(0, currentQty + parseInt(delta));
        var minThresh = parseInt(data[i][7]) || 5;
        
        var newStatus = 'IN_STOCK';
        if (newQty <= 0) newStatus = 'OUT_OF_STOCK';
        else if (newQty <= minThresh) newStatus = 'LOW_STOCK';
        
        sheet.getRange(i + 1, 7).setValue(newQty);
        sheet.getRange(i + 1, 15).setValue(newStatus);
        sheet.getRange(i + 1, 16).setValue(new Date().toLocaleString('id-ID'));
        
        return { success: true, newQuantity: newQty, newStatus: newStatus };
      }
    }
    return { success: false, error: 'Jaket tidak ditemukan' };
  } catch (err) {
    return { success: false, error: err.toString() };
  }
}

/**
 * Restock cepat (+15 unit)
 */
function restockJacket(jacketId, amount) {
  return adjustStock(jacketId, amount || 15);
}

/**
 * Restock semua jaket yang menipis atau habis
 */
function restockAllLowStock(amount) {
  try {
    var sheet = getOrCreateSheet(INVENTORY_SHEET);
    var data = sheet.getDataRange().getValues();
    var addAmount = parseInt(amount) || 15;
    var restockedCount = 0;
    
    for (var i = 1; i < data.length; i++) {
      var qty = parseInt(data[i][6]) || 0;
      var minThresh = parseInt(data[i][7]) || 5;
      
      if (qty <= minThresh) {
        var newQty = qty + addAmount;
        sheet.getRange(i + 1, 7).setValue(newQty);
        sheet.getRange(i + 1, 15).setValue('IN_STOCK');
        sheet.getRange(i + 1, 16).setValue(new Date().toLocaleString('id-ID'));
        restockedCount++;
      }
    }
    return { success: true, restockedCount: restockedCount };
  } catch (err) {
    return { success: false, error: err.toString() };
  }
}

/**
 * Mengubah URL foto jaket berdasarkan ID atau Kode SKU
 */
function updateJacketImage(targetIdentifier, newImageUrl, updateBySku) {
  try {
    var sheet = getOrCreateSheet(INVENTORY_SHEET);
    var data = sheet.getDataRange().getValues();
    var updatedCount = 0;
    
    for (var i = 1; i < data.length; i++) {
      var match = updateBySku 
        ? String(data[i][1]).toLowerCase() === String(targetIdentifier).toLowerCase()
        : String(data[i][0]) === String(targetIdentifier);
        
      if (match) {
        sheet.getRange(i + 1, 13).setValue(newImageUrl);
        sheet.getRange(i + 1, 16).setValue(new Date().toLocaleString('id-ID'));
        updatedCount++;
        if (!updateBySku) break;
      }
    }
    return { success: true, updatedCount: updatedCount };
  } catch (err) {
    return { success: false, error: err.toString() };
  }
}

/**
 * Menghapus jaket dari Google Sheet
 */
function deleteJacket(jacketId) {
  try {
    var sheet = getOrCreateSheet(INVENTORY_SHEET);
    var data = sheet.getDataRange().getValues();
    
    for (var i = 1; i < data.length; i++) {
      if (String(data[i][0]) === String(jacketId)) {
        sheet.deleteRow(i + 1);
        return { success: true, message: 'Jaket berhasil dihapus' };
      }
    }
    return { success: false, error: 'Jaket tidak ditemukan' };
  } catch (err) {
    return { success: false, error: err.toString() };
  }
}

/**
 * Mengambil seluruh data produksi harian
 */
function getProductionData() {
  try {
    var sheet = getOrCreateSheet(PRODUCTION_SHEET);
    var data = sheet.getDataRange().getValues();
    
    if (data.length <= 1) {
      seedInitialProduction(sheet);
      data = sheet.getDataRange().getValues();
    }
    
    var items = [];
    for (var i = 1; i < data.length; i++) {
      var row = data[i];
      if (!row[0]) continue;
      
      items.push({
        id: String(row[0]),
        date: row[1],
        dateString: String(row[2]),
        jacketId: String(row[3]),
        jacketName: String(row[4]),
        jacketSku: String(row[5]),
        batchNumber: String(row[6]),
        shift: String(row[7]),
        operatorName: String(row[8]),
        quantityProduced: parseInt(row[9]) || 0,
        defectCount: parseInt(row[10]) || 0,
        status: String(row[11]),
        notes: String(row[12] || ''),
        addedToStock: Boolean(row[13]),
        createdAt: String(row[14])
      });
    }
    
    return { success: true, productions: items };
  } catch (err) {
    return { success: false, error: err.toString(), productions: [] };
  }
}

/**
 * Menambah catatan produksi harian & opsional menambahkan hasil ke stok gudang
 */
function addDailyProduction(prod) {
  try {
    var sheet = getOrCreateSheet(PRODUCTION_SHEET);
    var now = new Date();
    var todayStr = Utilities.formatDate(now, Session.getScriptTimeZone() || 'Asia/Jakarta', 'yyyy-MM-dd');
    var prodId = 'prod_' + now.getTime();
    
    var qty = parseInt(prod.quantityProduced) || 0;
    var defects = parseInt(prod.defectCount) || 0;
    var accepted = Math.max(0, qty - defects);
    var added = Boolean(prod.addedToStock);
    
    var row = [
      prodId,
      now.getTime(),
      todayStr,
      prod.jacketId || '',
      prod.jacketName || '',
      prod.jacketSku || '',
      prod.batchNumber || ('BATCH-' + (now.getTime() % 10000)),
      prod.shift || 'Pagi',
      prod.operatorName || 'Operator 1',
      qty,
      defects,
      prod.status || 'SELESAI',
      prod.notes || '',
      added,
      now.toLocaleString('id-ID')
    ];
    
    sheet.appendRow(row);
    
    // Jika dicentang tambahkan ke stok inventaris, otomatis tingkatkan kuantitas jaket
    if (added && prod.jacketId && accepted > 0) {
      adjustStock(prod.jacketId, accepted);
    }
    
    return { success: true, id: prodId, message: 'Catatan produksi berhasil disimpan!' };
  } catch (err) {
    return { success: false, error: err.toString() };
  }
}

/**
 * Menghapus catatan produksi
 */
function deleteDailyProduction(prodId) {
  try {
    var sheet = getOrCreateSheet(PRODUCTION_SHEET);
    var data = sheet.getDataRange().getValues();
    
    for (var i = 1; i < data.length; i++) {
      if (String(data[i][0]) === String(prodId)) {
        sheet.deleteRow(i + 1);
        return { success: true, message: 'Data produksi berhasil dihapus' };
      }
    }
    return { success: false, error: 'Data produksi tidak ditemukan' };
  } catch (err) {
    return { success: false, error: err.toString() };
  }
}

/**
 * Ekspor seluruh data inventaris ke format CSV
 */
function exportInventoryCsv() {
  var sheet = getOrCreateSheet(INVENTORY_SHEET);
  var data = sheet.getDataRange().getValues();
  var csv = '';
  
  for (var r = 0; r < data.length; r++) {
    var row = data[r].map(function(cell) {
      var str = String(cell).replace(/"/g, '""');
      return '"' + str + '"';
    });
    csv += row.join(',') + '\r\n';
  }
  return csv;
}

/**
 * Mengisi data awal sampel jaket
 */
function seedInitialStock(sheet) {
  var initialItems = [
    ['jkt_01', 'JKT-BMB-BLK-L', 'Classic Flight Bomber', 'Jaket Pria', 'Bomber Jacket', 'L', 24, 5, 'Leather', 'Black', 'Rack A-01', 99.99, 'https://mediadirhub.com/vibe/black-leather-bomber-jacket-with-ribbed-cuffs-and-zipper-front.png', false, 'IN_STOCK', new Date().toLocaleString('id-ID')],
    ['jkt_02', 'JKT-TRN-BEI-M', 'Elegance Trench Coat', 'Jaket Wanita', 'Trench Coat', 'M', 18, 5, 'Cotton', 'Beige', 'Rack B-02', 129.99, 'https://mediadirhub.com/vibe/beige-cotton-trench-coat-with-double-breasted-buttons-and-belt.png', false, 'IN_STOCK', new Date().toLocaleString('id-ID')],
    ['jkt_03', 'JKT-DNM-BLU-S', 'Vintage Denim Trucker', 'Jaket Anak', 'Denim Jacket', 'S', 3, 5, 'Denim', 'Blue', 'Rack C-01', 79.99, 'https://mediadirhub.com/vibe/medium-blue-denim-jacket-with-patch-pockets-and-button-front.png', false, 'LOW_STOCK', new Date().toLocaleString('id-ID')],
    ['jkt_04', 'JKT-PFR-GRY-XL', 'Alpine Quilted Puffer', 'Jaket Unisex', 'Puffer Jacket', 'XL', 14, 5, 'Polyester', 'Gray', 'Rack D-03', 119.99, 'https://mediadirhub.com/vibe/dark-gray-quilted-puffer-jacket-with-hood-and-zip-closure.png', true, 'IN_STOCK', new Date().toLocaleString('id-ID')],
    ['jkt_05', 'JKT-PRK-OLV-XXL', 'Sub-Zero Storm Parka', 'Jaket Pria', 'Parka', 'XXL', 2, 5, 'Nylon', 'Olive', 'Rack E-01', 149.99, 'https://mediadirhub.com/vibe/olive-green-jacket-with-matte-finish-and-structured-silhouette.png', true, 'LOW_STOCK', new Date().toLocaleString('id-ID')],
    ['jkt_06', 'JKT-BMB-NVY-3XL', 'Urban Pilot Bomber Extra', 'Jaket Unisex', 'Bomber Jacket', '3XL', 0, 5, 'Nylon', 'Navy', 'Rack A-04', 109.99, 'https://mediadirhub.com/vibe/deep-navy-blue-jacket-with-clean-lines-and-no-patterns.png', false, 'OUT_OF_STOCK', new Date().toLocaleString('id-ID')],
    ['jkt_07', 'JKT-TRN-BLK-M', 'Metropolitan Dark Trench', 'Jaket Pria', 'Trench Coat', 'M', 28, 5, 'Wool', 'Black', 'Rack B-01', 139.99, 'https://mediadirhub.com/vibe/solid-black-jacket-front-with-minimal-branding.png', false, 'IN_STOCK', new Date().toLocaleString('id-ID')],
    ['jkt_08', 'JKT-DNM-BLK-L', 'Washed Black Denim', 'Jaket Wanita', 'Denim Jacket', 'L', 15, 5, 'Denim', 'Black', 'Rack C-02', 84.99, 'https://mediadirhub.com/vibe/black-leather-bomber-jacket-with-ribbed-cuffs-and-zipper-front.png', false, 'IN_STOCK', new Date().toLocaleString('id-ID')]
  ];
  
  for (var i = 0; i < initialItems.length; i++) {
    sheet.appendRow(initialItems[i]);
  }
}

/**
 * Mengisi data awal sampel catatan produksi
 */
function seedInitialProduction(sheet) {
  var now = new Date();
  var todayStr = Utilities.formatDate(now, Session.getScriptTimeZone() || 'Asia/Jakarta', 'yyyy-MM-dd');
  
  var initialBatches = [
    ['prod_01', now.getTime() - 86400000, todayStr, 'jkt_01', 'Classic Flight Bomber', 'JKT-BMB-BLK-L', 'BATCH-701', 'Pagi', 'Ahmad S.', 30, 1, 'SELESAI', 'Hasil jahit sempurna', true, now.toLocaleString('id-ID')],
    ['prod_02', now.getTime() - 43200000, todayStr, 'jkt_04', 'Alpine Quilted Puffer', 'JKT-PFR-GRY-XL', 'BATCH-702', 'Siang', 'Budi W.', 25, 2, 'SELESAI', 'Resleting lancar', true, now.toLocaleString('id-ID')],
    ['prod_03', now.getTime() - 10000000, todayStr, 'jkt_02', 'Elegance Trench Coat', 'JKT-TRN-BEI-M', 'BATCH-703', 'Malam', 'Citra M.', 20, 0, 'SELESAI', 'Kancing rapi', true, now.toLocaleString('id-ID')]
  ];
  
  for (var i = 0; i < initialBatches.length; i++) {
    sheet.appendRow(initialBatches[i]);
  }
}
