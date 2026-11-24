import { JacketItem, StockStatusLevel, getStockStatus } from '../types/jacket';

export function escapeCsv(value: string): string {
  if (
    value.includes(',') ||
    value.includes('"') ||
    value.includes('\n') ||
    value.includes('\r')
  ) {
    return '"' + value.replace(/"/g, '""') + '"';
  }
  return value;
}

export function generateCsv(jackets: JacketItem[], isIndonesian = true): string {
  const lines: string[] = [];

  const headers = isIndonesian
    ? [
        'ID',
        'SKU',
        'Nama Produk',
        'Kategori Demografis',
        'Model Jaket',
        'Ukuran',
        'Bahan',
        'Warna',
        'Jumlah Stok',
        'Batas Minimum',
        'Lokasi Rak',
        'Status Stok',
        'Koleksi Musiman',
        'Harga Satuan (USD)',
        'Estimasi Nilai Total (USD)',
      ]
    : [
        'ID',
        'SKU',
        'Product Name',
        'Demographic Category',
        'Jacket Style',
        'Size',
        'Material',
        'Color',
        'Stock Quantity',
        'Min Threshold',
        'Location Rack',
        'Stock Status',
        'Seasonal Collection',
        'Unit Price (USD)',
        'Estimated Total Value (USD)',
      ];

  lines.push(headers.map(escapeCsv).join(','));

  for (const jacket of jackets) {
    const status = getStockStatus(jacket.quantity, jacket.minThreshold);
    const statusLabel =
      status === StockStatusLevel.IN_STOCK
        ? isIndonesian
          ? 'Stok Tersedia'
          : 'In Stock'
        : status === StockStatusLevel.LOW_STOCK
        ? isIndonesian
          ? 'Stok Sedikit'
          : 'Low Stock'
        : isIndonesian
        ? 'Habis Stok'
        : 'Out of Stock';

    const seasonalLabel = jacket.isSeasonal
      ? isIndonesian
        ? 'Ya'
        : 'Yes'
      : isIndonesian
      ? 'Tidak'
      : 'No';

    const totalValue = jacket.quantity * jacket.unitPrice;

    const row = [
      jacket.id,
      jacket.sku,
      jacket.name,
      jacket.genderCategory,
      jacket.style,
      jacket.size,
      jacket.material,
      jacket.color,
      String(jacket.quantity),
      String(jacket.minThreshold),
      jacket.locationRack,
      statusLabel,
      seasonalLabel,
      jacket.unitPrice.toFixed(2),
      totalValue.toFixed(2),
    ];

    lines.push(row.map(escapeCsv).join(','));
  }

  return '\uFEFF' + lines.join('\r\n') + '\r\n';
}

export function generateFilename(prefix = 'inventaris_jaket'): string {
  const now = new Date();
  const pad = (n: number) => String(n).padStart(2, '0');
  const timestamp = `${now.getFullYear()}${pad(now.getMonth() + 1)}${pad(now.getDate())}_${pad(
    now.getHours()
  )}${pad(now.getMinutes())}${pad(now.getSeconds())}`;
  return `${prefix}_${timestamp}.csv`;
}

export function downloadCsvFile(csvContent: string, filename: string): void {
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}
