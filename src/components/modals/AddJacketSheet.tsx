import React, { useEffect, useState } from 'react';
import {
  Check,
  ChevronDown,
  ChevronUp,
  Minus,
  Plus,
  Save,
  ShoppingBag,
  SlidersHorizontal,
  X,
} from 'lucide-react';
import {
  COLORS,
  EXTRA_STYLE_OPTIONS,
  MATERIALS,
  SIZES,
} from '../../data/catalogData';
import { JacketItem, StockStatusLevel, getStockStatus } from '../../types/jacket';

interface AddJacketSheetProps {
  isOpen: boolean;
  isIndonesian: boolean;
  existingJackets: JacketItem[];
  onDismiss: () => void;
  onSave: (data: {
    sku: string;
    name: string;
    style: string;
    size: string;
    material: string;
    color: string;
    quantity: number;
    locationRack: string;
    isSeasonal: boolean;
  }) => void;
}

export const AddJacketSheet: React.FC<AddJacketSheetProps> = ({
  isOpen,
  isIndonesian,
  existingJackets,
  onDismiss,
  onSave,
}) => {
  const [name, setName] = useState('');
  const [selectedStyle, setSelectedStyle] = useState(EXTRA_STYLE_OPTIONS[0]);
  const [selectedSize, setSelectedSize] = useState('M');
  const [quantity, setQuantity] = useState(10);
  const [sku, setSku] = useState('');
  const [isManualSku, setIsManualSku] = useState(false);
  const [manualSkuText, setManualSkuText] = useState('');
  const [selectedMaterial, setSelectedMaterial] = useState(MATERIALS[0].titleEn);
  const [selectedColor, setSelectedColor] = useState(COLORS[9].titleEn);
  const [locationRack, setLocationRack] = useState('Rack A-01');
  const [isSeasonal, setIsSeasonal] = useState(false);
  const [showAdditionalDetails, setShowAdditionalDetails] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const generateAutoSkuCandidate = (nm: string, st: string, sz: string) => {
    const styleCode = st.slice(0, 3).toUpperCase();
    const nameCode = nm.trim()
      ? nm.trim().replace(/\s+/g, '').slice(0, 3).toUpperCase()
      : 'NEW';
    const base = `JKT-${styleCode}-${nameCode}-${sz}`;
    if (existingJackets.some((j) => j.sku.toLowerCase() === base.toLowerCase())) {
      return `${base}-${Math.floor(10 + Math.random() * 90)}`;
    }
    return base;
  };

  useEffect(() => {
    if (isOpen && !isManualSku) {
      setSku(generateAutoSkuCandidate(name, selectedStyle, selectedSize));
    }
  }, [isOpen, name, selectedStyle, selectedSize, isManualSku]);

  useEffect(() => {
    if (isOpen) {
      setErrorMessage(null);
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const activeSku = (isManualSku ? manualSkuText : sku).trim();
  const duplicateJacket = activeSku
    ? existingJackets.find((j) => j.sku.toLowerCase() === activeSku.toLowerCase())
    : undefined;

  const previewStatus = getStockStatus(quantity, 5);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      setErrorMessage(
        isIndonesian ? 'Mohon masukkan nama jaket!' : 'Please enter jacket name!'
      );
      return;
    }
    if (isManualSku && !activeSku) {
      setErrorMessage(
        isIndonesian ? 'Mohon masukkan kode SKU manual!' : 'Please enter manual SKU code!'
      );
      return;
    }
    if (duplicateJacket) {
      setErrorMessage(
        isIndonesian
          ? `SKU '${activeSku}' sudah digunakan oleh '${duplicateJacket.name}'. Harap gunakan SKU unik!`
          : `SKU '${activeSku}' is already used by '${duplicateJacket.name}'. Please enter a unique SKU!`
      );
      return;
    }

    onSave({
      sku: activeSku || generateAutoSkuCandidate(name, selectedStyle, selectedSize),
      name: name.trim(),
      style: selectedStyle,
      size: selectedSize,
      material: selectedMaterial,
      color: selectedColor,
      quantity,
      locationRack: locationRack.trim() || 'Rack A-01',
      isSeasonal,
    });

    setName('');
    setQuantity(10);
    setManualSkuText('');
    setIsManualSku(false);
    setShowAdditionalDetails(false);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-slate-900/55 backdrop-blur-[1px] sm:p-4">
      <div className="bg-white w-full sm:max-w-lg rounded-t-3xl sm:rounded-2xl shadow-2xl max-h-[92vh] flex flex-col overflow-hidden">
        {/* Header */}
        <div className="px-5 py-4 border-b border-slate-100 flex items-center justify-between">
          <div>
            <div className="flex items-center gap-2">
              <span className="px-2 py-0.5 rounded-md bg-slate-900 text-white text-[10px] font-bold tracking-wider">
                QUICK ADD
              </span>
              <h3 className="text-lg font-extrabold text-slate-900">
                {isIndonesian ? 'Input Jaket Baru' : 'Add New Jacket'}
              </h3>
            </div>
            <p className="text-xs text-slate-500 mt-0.5">
              {isIndonesian
                ? 'Simpan langsung ke penyimpanan inventaris lokal'
                : 'Save directly to local inventory storage'}
            </p>
          </div>
          <button
            type="button"
            onClick={onDismiss}
            aria-label="Close"
            className="w-9 h-9 rounded-full hover:bg-slate-100 flex items-center justify-center text-slate-500 cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-5 overflow-y-auto space-y-4">
          {/* 1. Name */}
          <div>
            <label className="block text-xs font-bold text-slate-800 mb-1.5">
              {isIndonesian ? '1. Nama Jaket *' : '1. Jacket Name *'}
            </label>
            <div className="relative">
              <ShoppingBag className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                value={name}
                onChange={(e) => {
                  setName(e.target.value);
                  setErrorMessage(null);
                }}
                placeholder={
                  isIndonesian
                    ? 'cth. Bomber Pilot Kulit Premium'
                    : 'e.g. Premium Leather Bomber'
                }
                className="w-full h-11 pl-9 pr-3 rounded-xl border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-slate-800"
              />
            </div>
          </div>

          {/* 2. Style */}
          <div>
            <div className="flex items-center justify-between mb-1.5">
              <label className="text-xs font-bold text-slate-800">
                {isIndonesian ? '2. Model / Gaya Jaket *' : '2. Jacket Style *'}
              </label>
              <span className="text-xs font-bold text-blue-600">{selectedStyle}</span>
            </div>
            <div className="flex flex-wrap gap-2">
              {EXTRA_STYLE_OPTIONS.map((style) => {
                const active = selectedStyle === style;
                return (
                  <button
                    key={style}
                    type="button"
                    onClick={() => setSelectedStyle(style)}
                    className={`px-3 py-1.5 rounded-xl text-xs font-semibold inline-flex items-center gap-1 transition cursor-pointer ${
                      active
                        ? 'bg-slate-900 text-white'
                        : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
                    }`}
                  >
                    {active && <Check className="w-3.5 h-3.5" />}
                    {style}
                  </button>
                );
              })}
            </div>
          </div>

          {/* 3. Size */}
          <div>
            <div className="flex items-center justify-between mb-1.5">
              <label className="text-xs font-bold text-slate-800">
                {isIndonesian ? '3. Ukuran / Size *' : '3. Jacket Size *'}
              </label>
              <span className="text-xs font-bold text-blue-600">Size {selectedSize}</span>
            </div>
            <div className="grid grid-cols-7 gap-1.5">
              {SIZES.map((s) => {
                const active = selectedSize === s.title;
                return (
                  <button
                    key={s.id}
                    type="button"
                    onClick={() => setSelectedSize(s.title)}
                    className={`h-10 rounded-xl text-xs font-bold border transition cursor-pointer ${
                      active
                        ? 'bg-slate-900 text-white border-slate-900'
                        : 'bg-slate-100 text-slate-800 border-slate-200 hover:bg-slate-200'
                    }`}
                  >
                    {s.title}
                  </button>
                );
              })}
            </div>
          </div>

          {/* 4. Quantity Card */}
          <div className="rounded-2xl bg-slate-50 border border-slate-200 p-3.5 space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-slate-800">
                {isIndonesian ? '4. Jumlah Kuantitas Stok *' : '4. Stock Quantity *'}
              </span>
              <span
                className={`px-2.5 py-0.5 rounded-full text-[11px] font-bold ${
                  previewStatus === StockStatusLevel.OUT_OF_STOCK
                    ? 'bg-red-100 text-red-700'
                    : previewStatus === StockStatusLevel.LOW_STOCK
                    ? 'bg-amber-100 text-amber-700'
                    : 'bg-emerald-100 text-emerald-700'
                }`}
              >
                {previewStatus === StockStatusLevel.OUT_OF_STOCK
                  ? isIndonesian
                    ? 'Habis (0)'
                    : 'Out of Stock (0)'
                  : previewStatus === StockStatusLevel.LOW_STOCK
                  ? isIndonesian
                    ? 'Menipis (≤5)'
                    : 'Low Stock (≤5)'
                  : isIndonesian
                  ? 'Tersedia (>5)'
                  : 'In Stock (>5)'}
              </span>
            </div>

            <div className="flex items-center justify-center gap-4">
              <button
                type="button"
                onClick={() => setQuantity((q) => Math.max(0, q - 1))}
                className="w-11 h-11 rounded-full bg-slate-200 hover:bg-slate-300 flex items-center justify-center text-slate-900 cursor-pointer"
              >
                <Minus className="w-5 h-5" />
              </button>
              <input
                type="number"
                min={0}
                max={9999}
                value={quantity}
                onChange={(e) => setQuantity(Math.max(0, Number(e.target.value) || 0))}
                className="w-24 h-11 text-center text-xl font-extrabold text-slate-900 bg-white border border-slate-300 rounded-xl tabular-nums focus:outline-none focus:ring-2 focus:ring-slate-800"
              />
              <button
                type="button"
                onClick={() => setQuantity((q) => q + 1)}
                className="w-11 h-11 rounded-full bg-slate-200 hover:bg-slate-300 flex items-center justify-center text-slate-900 cursor-pointer"
              >
                <Plus className="w-5 h-5" />
              </button>
            </div>

            <div className="grid grid-cols-4 gap-2">
              {[5, 10, 25, 50].map((amt) => (
                <button
                  key={amt}
                  type="button"
                  onClick={() => setQuantity((q) => q + amt)}
                  className="h-8 rounded-lg bg-white border border-slate-200 hover:bg-slate-100 text-xs font-bold text-slate-700 cursor-pointer tabular-nums"
                >
                  +{amt}
                </button>
              ))}
            </div>
          </div>

          {/* Expandable Additional Info */}
          <div>
            <button
              type="button"
              onClick={() => setShowAdditionalDetails((v) => !v)}
              className="w-full flex items-center justify-between px-3.5 py-2.5 rounded-xl bg-slate-100 hover:bg-slate-200/70 text-xs font-semibold text-slate-700 cursor-pointer"
            >
              <span className="flex items-center gap-2">
                <SlidersHorizontal className="w-4 h-4" />
                {isIndonesian
                  ? 'Informasi Tambahan (SKU, Rak, Bahan, Warna)'
                  : 'Additional Details (SKU, Rack, Material, Color)'}
              </span>
              {showAdditionalDetails ? (
                <ChevronUp className="w-4 h-4" />
              ) : (
                <ChevronDown className="w-4 h-4" />
              )}
            </button>

            {showAdditionalDetails && (
              <div className="mt-3 p-3.5 rounded-xl border border-slate-200 space-y-3 bg-white">
                {/* SKU */}
                <div>
                  <div className="flex items-center justify-between mb-1">
                    <label className="text-xs font-bold text-slate-700">
                      {isIndonesian ? 'Kode SKU / Barcode' : 'SKU / Barcode Code'}
                    </label>
                    <button
                      type="button"
                      onClick={() => {
                        const next = !isManualSku;
                        setIsManualSku(next);
                        if (!next) {
                          setSku(generateAutoSkuCandidate(name, selectedStyle, selectedSize));
                        } else {
                          setManualSkuText(sku);
                        }
                      }}
                      className="text-xs font-bold text-blue-600 cursor-pointer"
                    >
                      {isManualSku
                        ? isIndonesian
                          ? 'Mode Otomatis'
                          : 'Switch Auto'
                        : isIndonesian
                        ? 'Ubah Manual'
                        : 'Edit Manual'}
                    </button>
                  </div>
                  <input
                    type="text"
                    value={isManualSku ? manualSkuText : sku}
                    onChange={(e) => {
                      setIsManualSku(true);
                      setManualSkuText(e.target.value.toUpperCase());
                    }}
                    className={`w-full h-10 px-3 rounded-lg border font-mono-sku text-xs font-bold ${
                      duplicateJacket ? 'border-red-500 bg-red-50' : 'border-slate-300'
                    }`}
                  />
                  {duplicateJacket && (
                    <p className="text-[11px] text-red-600 font-semibold mt-1">
                      {isIndonesian
                        ? `SKU sudah digunakan oleh '${duplicateJacket.name}'`
                        : `SKU already used by '${duplicateJacket.name}'`}
                    </p>
                  )}
                </div>

                {/* Rack */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    {isIndonesian ? 'Lokasi Rak Gudang' : 'Warehouse Rack Location'}
                  </label>
                  <input
                    type="text"
                    value={locationRack}
                    onChange={(e) => setLocationRack(e.target.value)}
                    placeholder="Rack A-01"
                    className="w-full h-10 px-3 rounded-lg border border-slate-300 text-xs"
                  />
                </div>

                {/* Material */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    {isIndonesian ? 'Bahan / Material' : 'Material'}
                  </label>
                  <div className="flex flex-wrap gap-1.5">
                    {MATERIALS.map((mat) => {
                      const active = selectedMaterial === mat.titleEn;
                      return (
                        <button
                          key={mat.id}
                          type="button"
                          onClick={() => setSelectedMaterial(mat.titleEn)}
                          className={`px-2.5 py-1 rounded-lg text-xs font-semibold cursor-pointer ${
                            active
                              ? 'bg-slate-900 text-white'
                              : 'bg-slate-100 text-slate-700'
                          }`}
                        >
                          {isIndonesian ? mat.titleId : mat.titleEn}
                        </button>
                      );
                    })}
                  </div>
                </div>

                {/* Color */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    {isIndonesian ? 'Warna / Color' : 'Color'}
                  </label>
                  <div className="flex flex-wrap gap-1.5">
                    {COLORS.map((col) => {
                      const active = selectedColor.toLowerCase() === col.titleEn.toLowerCase();
                      return (
                        <button
                          key={col.id}
                          type="button"
                          onClick={() => setSelectedColor(col.titleEn)}
                          className={`px-2.5 py-1 rounded-lg text-xs font-semibold inline-flex items-center gap-1.5 cursor-pointer border ${
                            active
                              ? 'bg-slate-900 text-white border-slate-900'
                              : 'bg-slate-50 text-slate-700 border-slate-200'
                          }`}
                        >
                          <span
                            className="w-2.5 h-2.5 rounded-full border border-slate-300"
                            style={{ backgroundColor: col.hexColor }}
                          />
                          {isIndonesian ? col.titleId : col.titleEn}
                        </button>
                      );
                    })}
                  </div>
                </div>

                {/* Seasonal */}
                <label className="flex items-center justify-between pt-1 cursor-pointer">
                  <span className="text-xs font-semibold text-slate-700">
                    {isIndonesian
                      ? 'Koleksi Musim Dingin (Seasonal)'
                      : 'Winter Seasonal Collection'}
                  </span>
                  <input
                    type="checkbox"
                    checked={isSeasonal}
                    onChange={(e) => setIsSeasonal(e.target.checked)}
                    className="w-4 h-4 accent-slate-900 rounded cursor-pointer"
                  />
                </label>
              </div>
            )}
          </div>

          {errorMessage && (
            <div className="rounded-xl bg-red-50 border border-red-200 p-3 text-xs font-semibold text-red-700">
              {errorMessage}
            </div>
          )}

          <button
            type="submit"
            className="w-full h-12 rounded-xl bg-slate-900 hover:bg-slate-800 text-white font-bold text-sm flex items-center justify-center gap-2 transition cursor-pointer"
          >
            <Save className="w-4 h-4" />
            {isIndonesian ? 'Simpan ke Inventaris' : 'Save to Inventory'}
          </button>
        </form>
      </div>
    </div>
  );
};
