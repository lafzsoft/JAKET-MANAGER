import React, { useMemo } from 'react';
import {
  Check,
  ImagePlus,
  Info,
  Minus,
  Package,
  Palette,
  Plus,
  RefreshCw,
  Trash2,
  X,
} from 'lucide-react';
import { JacketItem, StockStatusLevel, getStockStatus } from '../../types/jacket';
import { NetworkImageWithFallback } from '../NetworkImageWithFallback';

interface JacketDetailModalProps {
  jacket: JacketItem | null;
  allJackets: JacketItem[];
  isIndonesian: boolean;
  onDismiss: () => void;
  onAdjustQuantity: (jacketId: string, delta: number) => void;
  onRestock: (jacketId: string) => void;
  onEditImage: (jacket: JacketItem) => void;
  onDelete: (jacket: JacketItem) => void;
  onSelectVariant: (jacket: JacketItem) => void;
}

function generateDetailedDescription(jacket: JacketItem, isIndonesian: boolean): string {
  const s = jacket.style.toLowerCase();
  let styleDesc = '';
  if (s.includes('bomber')) {
    styleDesc = isIndonesian
      ? 'Jaket model bomber klasik berinsulasi dengan rib elastis pada bagian kerah, pergelangan tangan, dan pinggang. Menawarkan siluet atletis dan ketahanan tinggi.'
      : 'Classic insulated bomber silhouette featuring stretch ribbed collar, cuffs, and hem. Delivers an athletic fit and rugged wind resistance.';
  } else if (s.includes('trench')) {
    styleDesc = isIndonesian
      ? 'Mantel model trench coat elegan berdesain double-breasted dengan sabuk pinggang yang dapat disesuaikan. Melindungi dari angin dan cipratan air hujan.'
      : 'Sophisticated double-breasted trench coat tailored with an adjustable waist belt. Engineered for rain repellent performance and timeless fashion.';
  } else if (s.includes('denim')) {
    styleDesc = isIndonesian
      ? 'Jaket berbahan denim premium berkancing logam dengan saku ganda di dada. Memiliki jahitan kokoh khas pakaian kerja vintage yang tahan lama.'
      : 'Rugged cotton denim outerwear featuring branded metal hardware and twin chest pockets. Reinforced stitching built for versatile daily wear.';
  } else if (s.includes('puffer')) {
    styleDesc = isIndonesian
      ? 'Jaket puffer quilted berbobot ringan dengan insulasi termal sintetis. Sangat efektif menjaga suhu tubuh tetap hangat saat cuaca dingin ekstrem.'
      : 'Ultra-light quilted puffer construction with synthetic thermal fill. Provides exceptional warmth retention in freezing conditions.';
  } else if (s.includes('parka')) {
    styleDesc = isIndonesian
      ? 'Jaket parka panjang tahan cuaca dengan penutup kepala (hoodie) dan saku kargo multifungsi. Dirancang untuk perlindungan maksimal aktivitas luar ruangan.'
      : 'Heavy-duty weather resistant long parka jacket with drawstring hood and utility cargo pockets. Built for maximum outdoor shielding.';
  } else {
    styleDesc = isIndonesian
      ? 'Pakaian luar berkualitas tinggi dengan konstruksi jahitan presisi untuk kenyamanan sepanjang hari dan daya tahan optimal.'
      : 'Premium outerwear built with reinforced precision tailoring for maximum comfort and durability.';
  }

  const m = jacket.material.toLowerCase();
  let materialDesc = '';
  if (m === 'leather' || m === 'kulit') {
    materialDesc = isIndonesian
      ? 'Dibuat dari bahan kulit premium dengan tekstur halus dan lapisan dalam lembut.'
      : 'Crafted with supple full-grain leather and breathable interior lining.';
  } else if (m === 'cotton' || m === 'katun') {
    materialDesc = isIndonesian
      ? 'Terbuat dari katun combed alami yang sejuk, ramah kulit, dan menyerap keringat.'
      : 'Constructed from 100% breathable organic cotton twill.';
  } else if (m === 'nylon' || m === 'nilon') {
    materialDesc = isIndonesian
      ? 'Menggunakan serat nilon tahan air dengan finishing matte anti robek.'
      : 'Constructed with ripstop water-resistant nylon weave.';
  } else if (m === 'wool' || m === 'wol') {
    materialDesc = isIndonesian
      ? 'Komposisi serat wol alami yang tebal dan memberikan kehangatan istimewa.'
      : 'Heavyweight natural wool blend providing superior thermal insulation.';
  } else {
    materialDesc = isIndonesian
      ? `Material kain komposit ${jacket.material} yang kuat dan mudah dirawat.`
      : `Durable ${jacket.material} composite blend, easy to clean.`;
  }

  const fitDesc = isIndonesian
    ? `Kategori: ${jacket.genderCategory}. Ukuran: ${jacket.size}. Disimpan di ${jacket.locationRack} siap untuk pemenuhan pesanan gudang.`
    : `Category: ${jacket.genderCategory}. Size: ${jacket.size}. Stored at ${jacket.locationRack} ready for warehouse fulfillment.`;

  return `${styleDesc} ${materialDesc} ${fitDesc}`;
}

export const JacketDetailModal: React.FC<JacketDetailModalProps> = ({
  jacket,
  allJackets,
  isIndonesian,
  onDismiss,
  onAdjustQuantity,
  onRestock,
  onEditImage,
  onDelete,
  onSelectVariant,
}) => {
  const currentJacket = useMemo(() => {
    if (!jacket) return null;
    return allJackets.find((j) => j.id === jacket.id) ?? jacket;
  }, [jacket, allJackets]);

  const relatedVariants = useMemo(() => {
    if (!currentJacket) return [];
    const prefix = currentJacket.name.split(' ').slice(0, 2).join(' ').toLowerCase();
    return allJackets.filter(
      (j) =>
        j.style.toLowerCase() === currentJacket.style.toLowerCase() ||
        j.name.split(' ').slice(0, 2).join(' ').toLowerCase() === prefix
    );
  }, [allJackets, currentJacket]);

  const sizeBreakdown = useMemo(() => {
    const sizes = ['XS', 'S', 'M', 'L', 'XL', 'XXL', '3XL'];
    return sizes.map((sz) => {
      const total = relatedVariants
        .filter((j) => j.size.toLowerCase() === sz.toLowerCase())
        .reduce((sum, j) => sum + j.quantity, 0);
      return { size: sz, qty: total };
    });
  }, [relatedVariants]);

  const colorOptions = useMemo(() => {
    if (!currentJacket) return [];
    const catalogColors: Array<{ name: string; hex: string; isDark: boolean }> = [
      { name: 'Black', hex: '#18181B', isDark: true },
      { name: 'Navy', hex: '#1E3A8A', isDark: true },
      { name: 'Olive', hex: '#365314', isDark: true },
      { name: 'Beige', hex: '#E5D5C5', isDark: false },
      { name: 'Maroon', hex: '#800020', isDark: true },
      { name: 'Red', hex: '#DC2626', isDark: true },
      { name: 'Gray', hex: '#6B7280', isDark: true },
      { name: 'Caramel', hex: '#C27835', isDark: true },
      { name: 'White', hex: '#FFFFFF', isDark: false },
      { name: 'Pink', hex: '#F472B6', isDark: false },
    ];
    const sameStyle = allJackets.filter(
      (j) => j.style.toLowerCase() === currentJacket.style.toLowerCase()
    );
    return catalogColors.map((col) => {
      const matched = sameStyle.find(
        (j) => j.color.toLowerCase() === col.name.toLowerCase()
      );
      return {
        ...col,
        existingJacket:
          matched ??
          (col.name.toLowerCase() === currentJacket.color.toLowerCase()
            ? currentJacket
            : null),
      };
    });
  }, [currentJacket, allJackets]);

  if (!currentJacket) return null;

  const status = getStockStatus(currentJacket.quantity, currentJacket.minThreshold);
  const statusColor =
    status === StockStatusLevel.IN_STOCK
      ? 'text-emerald-600'
      : status === StockStatusLevel.LOW_STOCK
      ? 'text-amber-600'
      : 'text-red-600';
  const statusBarColor =
    status === StockStatusLevel.IN_STOCK
      ? 'bg-emerald-600'
      : status === StockStatusLevel.LOW_STOCK
      ? 'bg-amber-600'
      : 'bg-red-600';
  const statusBg =
    status === StockStatusLevel.IN_STOCK
      ? 'bg-emerald-50 text-emerald-700'
      : status === StockStatusLevel.LOW_STOCK
      ? 'bg-amber-50 text-amber-700'
      : 'bg-red-50 text-red-700';
  const statusLabel =
    status === StockStatusLevel.IN_STOCK
      ? isIndonesian
        ? 'Stok Tersedia (Aman)'
        : 'In Stock (Safe)'
      : status === StockStatusLevel.LOW_STOCK
      ? isIndonesian
        ? 'Stok Menipis (Perlu Restock)'
        : 'Low Stock (Restock Needed)'
      : isIndonesian
      ? 'Habis Stok (Kritis)'
      : 'Out of Stock (Critical)';

  const capacityPct = Math.min(100, Math.round((currentJacket.quantity / 50) * 100));

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-slate-900/55 backdrop-blur-[1px] sm:p-4">
      <div className="bg-white w-full sm:max-w-xl rounded-t-3xl sm:rounded-2xl shadow-2xl max-h-[92vh] flex flex-col overflow-hidden">
        {/* Top Bar */}
        <div className="px-5 py-3.5 border-b border-slate-100 flex items-center justify-between">
          <span className="px-2.5 py-1 rounded-md bg-slate-100 font-mono-sku text-xs font-bold text-slate-700">
            {currentJacket.sku}
          </span>
          <button
            type="button"
            onClick={onDismiss}
            aria-label="Close"
            className="w-8 h-8 rounded-full hover:bg-slate-100 flex items-center justify-center text-slate-500 cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-5 overflow-y-auto space-y-4">
          {/* Hero Image */}
          <div className="relative h-52 rounded-2xl overflow-hidden bg-slate-100">
            <NetworkImageWithFallback
              src={currentJacket.imageUrl}
              alt={currentJacket.name}
            />
            <span
              className={`absolute top-3 left-3 px-2.5 py-1 rounded-lg text-[11px] font-bold shadow-xs ${statusBg}`}
            >
              {statusLabel}
            </span>
            <span className="absolute top-3 right-3 px-2.5 py-1 rounded-lg bg-slate-900/85 text-white text-xs font-extrabold tabular-nums">
              ${currentJacket.unitPrice.toFixed(2)}
            </span>
            <button
              type="button"
              onClick={() => onEditImage(currentJacket)}
              className="absolute bottom-3 right-3 h-8 px-3 rounded-lg bg-slate-900/85 hover:bg-slate-900 text-white text-xs font-semibold inline-flex items-center gap-1.5 cursor-pointer"
            >
              <ImagePlus className="w-3.5 h-3.5" />
              {isIndonesian ? 'Ubah Foto' : 'Edit Photo'}
            </button>
          </div>

          {/* Title & Meta Chips */}
          <div>
            <h2 className="text-xl font-extrabold text-slate-900">{currentJacket.name}</h2>
            <div className="flex flex-wrap gap-1.5 mt-2">
              <span className="px-2 py-0.5 rounded-md bg-slate-200 text-slate-700 text-[11px] font-semibold">
                {currentJacket.genderCategory}
              </span>
              <span className="px-2 py-0.5 rounded-md bg-slate-200 text-slate-700 text-[11px] font-semibold">
                {currentJacket.style}
              </span>
              <span className="px-2 py-0.5 rounded-md bg-slate-900 text-white text-[11px] font-bold">
                {isIndonesian ? `Ukuran: ${currentJacket.size}` : `Size: ${currentJacket.size}`}
              </span>
              <span className="px-2 py-0.5 rounded-md bg-emerald-100 text-emerald-800 text-[11px] font-bold">
                {currentJacket.locationRack}
              </span>
            </div>
          </div>

          {/* Section 1: Description */}
          <div className="rounded-xl bg-slate-50 border border-slate-200 p-3.5 space-y-2.5">
            <div className="flex items-center gap-1.5 text-xs font-bold text-slate-900">
              <Info className="w-4 h-4 text-blue-600" />
              {isIndonesian
                ? 'Deskripsi & Karakteristik Produk'
                : 'Product Description & Specifications'}
            </div>
            <p className="text-xs text-slate-600 leading-relaxed">
              {generateDetailedDescription(currentJacket, isIndonesian)}
            </p>
            <div className="grid grid-cols-3 gap-2 pt-2 border-t border-slate-200/70">
              <div>
                <p className="text-[10px] text-slate-500">
                  {isIndonesian ? 'Bahan Material:' : 'Material:'}
                </p>
                <p className="text-xs font-bold text-slate-900">{currentJacket.material}</p>
              </div>
              <div>
                <p className="text-[10px] text-slate-500">
                  {isIndonesian ? 'Warna Utama:' : 'Primary Color:'}
                </p>
                <p className="text-xs font-bold text-slate-900">{currentJacket.color}</p>
              </div>
              <div>
                <p className="text-[10px] text-slate-500">
                  {isIndonesian ? 'Koleksi:' : 'Collection:'}
                </p>
                <p
                  className={`text-xs font-bold ${
                    currentJacket.isSeasonal ? 'text-blue-600' : 'text-slate-900'
                  }`}
                >
                  {currentJacket.isSeasonal
                    ? isIndonesian
                      ? 'Musiman (Winter)'
                      : 'Seasonal (Winter)'
                    : isIndonesian
                    ? 'Koleksi Reguler'
                    : 'Regular Stock'}
                </p>
              </div>
            </div>
          </div>

          {/* Section 2: Color Variants */}
          <div className="rounded-xl bg-slate-50 border border-slate-200 p-3.5 space-y-2.5">
            <div className="flex items-center justify-between">
              <span className="flex items-center gap-1.5 text-xs font-bold text-slate-900">
                <Palette className="w-4 h-4 text-violet-600" />
                {isIndonesian ? 'Varian Warna Produk' : 'Color Variants'}
              </span>
              <span className="text-[11px] font-medium text-slate-500">
                {isIndonesian
                  ? `Warna aktif: ${currentJacket.color}`
                  : `Active: ${currentJacket.color}`}
              </span>
            </div>

            <div className="flex gap-3 overflow-x-auto no-scrollbar pb-1">
              {colorOptions.map((opt) => {
                const isSelected =
                  opt.name.toLowerCase() === currentJacket.color.toLowerCase();
                return (
                  <button
                    key={opt.name}
                    type="button"
                    disabled={!opt.existingJacket}
                    onClick={() => {
                      if (opt.existingJacket) {
                        onSelectVariant(opt.existingJacket);
                      }
                    }}
                    className={`shrink-0 w-16 flex flex-col items-center text-center ${
                      opt.existingJacket ? 'cursor-pointer' : 'opacity-50 cursor-default'
                    }`}
                  >
                    <span
                      className={`w-9 h-9 rounded-full flex items-center justify-center border-2 ${
                        isSelected ? 'border-blue-600 ring-2 ring-blue-200' : 'border-slate-300'
                      }`}
                      style={{ backgroundColor: opt.hex }}
                    >
                      {isSelected && (
                        <Check
                          className={`w-4 h-4 ${
                            opt.isDark ? 'text-white' : 'text-slate-900'
                          }`}
                        />
                      )}
                    </span>
                    <span className="text-[11px] font-semibold text-slate-800 mt-1 truncate max-w-full">
                      {opt.name}
                    </span>
                    <span
                      className={`text-[10px] font-semibold tabular-nums ${
                        opt.existingJacket && opt.existingJacket.quantity > 0
                          ? 'text-emerald-600'
                          : 'text-slate-400'
                      }`}
                    >
                      {opt.existingJacket
                        ? `${opt.existingJacket.quantity} unit`
                        : isIndonesian
                        ? 'Kosong'
                        : 'N/A'}
                    </span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Section 3: Detailed Stock Breakdown */}
          <div className="rounded-xl bg-slate-50 border border-slate-200 p-3.5 space-y-3">
            <div className="flex items-center justify-between gap-2">
              <span className="flex items-center gap-1.5 text-xs font-bold text-slate-900">
                <Package className={`w-4 h-4 ${statusColor}`} />
                {isIndonesian
                  ? 'Rincian & Analisis Stok Gudang'
                  : 'Detailed Stock Breakdown & Health'}
              </span>
            </div>

            <div className="grid grid-cols-3 gap-2">
              <div className="bg-white rounded-xl p-2.5 border border-slate-100">
                <p className="text-[10px] text-slate-500">
                  {isIndonesian ? 'Stok Gudang' : 'Available Stock'}
                </p>
                <p className={`text-xl font-extrabold tabular-nums ${statusColor}`}>
                  {currentJacket.quantity}
                </p>
                <p className="text-[9px] text-slate-400">
                  {isIndonesian ? 'Unit siap kirim' : 'Ready units'}
                </p>
              </div>
              <div className="bg-white rounded-xl p-2.5 border border-slate-100">
                <p className="text-[10px] text-slate-500">
                  {isIndonesian ? 'Batas Minimum' : 'Min Threshold'}
                </p>
                <p className="text-xl font-extrabold tabular-nums text-slate-900">
                  {currentJacket.minThreshold}
                </p>
                <p className="text-[9px] text-slate-400">
                  {isIndonesian ? 'Batas peringatan' : 'Alert trigger'}
                </p>
              </div>
              <div className="bg-white rounded-xl p-2.5 border border-slate-100">
                <p className="text-[10px] text-slate-500">
                  {isIndonesian ? 'Lokasi Rak' : 'Rack Bin'}
                </p>
                <p className="text-xs font-extrabold text-slate-900 truncate mt-1">
                  {currentJacket.locationRack}
                </p>
                <p className="text-[9px] text-slate-400 mt-1">
                  {isIndonesian ? 'Zona Gudang Utama' : 'Main Zone'}
                </p>
              </div>
            </div>

            {/* Rack Capacity */}
            <div>
              <p className="text-[11px] font-medium text-slate-600 mb-1">
                {isIndonesian
                  ? `Kapasitas Penyimpanan Rak (Target 50 Unit): ${capacityPct}%`
                  : `Rack Storage Capacity (50 Unit target): ${capacityPct}%`}
              </p>
              <div className="w-full h-2 rounded-full bg-slate-200 overflow-hidden">
                <div
                  className={`h-full rounded-full transition-all ${statusBarColor}`}
                  style={{ width: `${capacityPct}%` }}
                />
              </div>
            </div>

            {/* Size Matrix */}
            <div>
              <p className="text-[11px] font-bold text-slate-800 mb-1.5">
                {isIndonesian
                  ? 'Distribusi Stok per Ukuran Model Ini:'
                  : 'Stock Distribution by Size (This Model):'}
              </p>
              <div className="grid grid-cols-7 gap-1">
                {sizeBreakdown.map((item) => {
                  const isCurrent =
                    item.size.toLowerCase() === currentJacket.size.toLowerCase();
                  return (
                    <div
                      key={item.size}
                      className={`rounded-lg py-1.5 text-center border ${
                        isCurrent
                          ? 'bg-slate-900 text-white border-slate-900'
                          : 'bg-white text-slate-700 border-slate-200'
                      }`}
                    >
                      <p className="text-[10px] font-bold">{item.size}</p>
                      <p
                        className={`text-xs font-extrabold tabular-nums ${
                          isCurrent
                            ? 'text-emerald-400'
                            : item.qty > 0
                            ? 'text-slate-900'
                            : 'text-slate-400'
                        }`}
                      >
                        {item.qty}
                      </p>
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Quick Adjuster */}
            <div className="bg-white rounded-xl p-3 border border-slate-200 flex items-center justify-between gap-2">
              <div>
                <p className="text-xs font-bold text-slate-900">
                  {isIndonesian ? 'Penyesuaian Cepat Stok' : 'Quick Stock Adjuster'}
                </p>
                <p className="text-[10px] text-slate-500">
                  {isIndonesian
                    ? 'Tambah / kurangi jumlah unit'
                    : 'Add or subtract inventory'}
                </p>
              </div>
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={() => onAdjustQuantity(currentJacket.id, -1)}
                  className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 flex items-center justify-center cursor-pointer"
                >
                  <Minus className="w-4 h-4" />
                </button>
                <span className="text-base font-extrabold text-slate-900 min-w-[28px] text-center tabular-nums">
                  {currentJacket.quantity}
                </span>
                <button
                  type="button"
                  onClick={() => onAdjustQuantity(currentJacket.id, 1)}
                  className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 flex items-center justify-center cursor-pointer"
                >
                  <Plus className="w-4 h-4" />
                </button>
                {status !== StockStatusLevel.IN_STOCK && (
                  <button
                    type="button"
                    onClick={() => onRestock(currentJacket.id)}
                    className="h-8 px-2.5 rounded-lg bg-emerald-100 hover:bg-emerald-200 text-emerald-800 text-xs font-bold inline-flex items-center gap-1 cursor-pointer"
                  >
                    <RefreshCw className="w-3 h-3" />
                    +15
                  </button>
                )}
              </div>
            </div>
          </div>

          {/* Footer Actions */}
          <div className="flex gap-2.5 pt-1">
            <button
              type="button"
              onClick={() => {
                onDelete(currentJacket);
                onDismiss();
              }}
              className="flex-1 h-11 rounded-xl border border-red-200 text-red-600 hover:bg-red-50 font-semibold text-sm inline-flex items-center justify-center gap-1.5 cursor-pointer"
            >
              <Trash2 className="w-4 h-4" />
              {isIndonesian ? 'Hapus Jaket' : 'Delete Item'}
            </button>
            <button
              type="button"
              onClick={onDismiss}
              className="flex-1 h-11 rounded-xl bg-slate-900 hover:bg-slate-800 text-white font-bold text-sm cursor-pointer"
            >
              {isIndonesian ? 'Selesai' : 'Done'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
