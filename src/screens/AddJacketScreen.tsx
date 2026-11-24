import React, { useEffect, useRef, useState } from 'react';
import {
  ArrowLeft,
  Check,
  ImagePlus,
  Link as LinkIcon,
  Minus,
  Plus,
  QrCode,
  RefreshCw,
  RotateCcw,
  Upload,
} from 'lucide-react';
import { useJackets } from '../context/JacketContext';
import {
  COLORS,
  CURATED_PRESETS,
  EXTRA_STYLE_OPTIONS,
  GENDER_CATEGORIES,
  HERO_IMAGE_URL,
  MATERIALS,
  SIZES,
  STYLES,
} from '../data/catalogData';
import { StockStatusLevel, getStockStatus } from '../types/jacket';
import { NetworkImageWithFallback } from '../components/NetworkImageWithFallback';
import { QrCodeScannerModal } from '../components/modals/QrCodeScannerModal';

export const AddJacketScreen: React.FC = () => {
  const {
    allJackets,
    isIndonesian,
    prefilledSkuForAdd,
    clearPrefilledSku,
    navigateToHome,
    generateAutoSku,
    generateRandomCustomSku,
    addNewJacket,
  } = useJackets();

  const fileInputRef = useRef<HTMLInputElement | null>(null);

  const [name, setName] = useState('');
  const [selectedCategory, setSelectedCategory] = useState(GENDER_CATEGORIES[0].titleId);
  const [selectedStyle, setSelectedStyle] = useState(EXTRA_STYLE_OPTIONS[0]);
  const [selectedSize, setSelectedSize] = useState('M');
  const [quantity, setQuantity] = useState(10);
  const [isManualSku, setIsManualSku] = useState(false);
  const [sku, setSku] = useState('');
  const [manualSkuText, setManualSkuText] = useState('');
  const [selectedMaterial, setSelectedMaterial] = useState(MATERIALS[0].titleEn);
  const [selectedColor, setSelectedColor] = useState(COLORS[9].titleEn);
  const [locationRack, setLocationRack] = useState('Rack A-01');
  const [unitPrice, setUnitPrice] = useState('129.99');
  const [isSeasonal, setIsSeasonal] = useState(false);
  const [customImageUrl, setCustomImageUrl] = useState('');
  const [showUrlInput, setShowUrlInput] = useState(false);
  const [customUrlText, setCustomUrlText] = useState('');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isScannerOpen, setIsScannerOpen] = useState(false);

  // Prefill from QR scanner if navigated with unknown SKU
  useEffect(() => {
    if (prefilledSkuForAdd) {
      setIsManualSku(true);
      setManualSkuText(prefilledSkuForAdd);
      setSku(prefilledSkuForAdd);
      const parts = prefilledSkuForAdd.split('-');
      if (parts.length >= 3) {
        const code = parts[1].toUpperCase();
        if (code === 'BMB') setSelectedStyle('Bomber Jacket');
        else if (code === 'PRK') setSelectedStyle('Parka');
        else if (code === 'DNM') setSelectedStyle('Denim Jacket');
        else if (code === 'TRN') setSelectedStyle('Trench Coat');
        else if (code === 'PUF' || code === 'PFF') setSelectedStyle('Puffer Jacket');

        const last = parts[parts.length - 1].toUpperCase();
        if (['XS', 'S', 'M', 'L', 'XL', 'XXL', '3XL'].includes(last)) {
          setSelectedSize(last);
        }
      }
      clearPrefilledSku();
    }
  }, [prefilledSkuForAdd, clearPrefilledSku]);

  useEffect(() => {
    if (!isManualSku) {
      setSku(generateAutoSku(selectedStyle, name, selectedSize));
    }
  }, [name, selectedStyle, selectedSize, isManualSku, generateAutoSku]);

  const activeSku = (isManualSku ? manualSkuText : sku).trim();
  const duplicateJacket = activeSku
    ? allJackets.find((j) => j.sku.toLowerCase() === activeSku.toLowerCase())
    : undefined;

  const calculatedStatus = getStockStatus(quantity, 5);
  const defaultStyleImage =
    STYLES.find((s) => s.titleEn.toLowerCase() === selectedStyle.toLowerCase())?.imageUrl ??
    HERO_IMAGE_URL;
  const effectivePreviewImage = customImageUrl.trim() || defaultStyleImage;

  const handleReset = () => {
    setName('');
    setSelectedCategory(GENDER_CATEGORIES[0].titleId);
    setSelectedStyle(EXTRA_STYLE_OPTIONS[0]);
    setSelectedSize('M');
    setQuantity(10);
    setIsManualSku(false);
    setManualSkuText('');
    setSelectedMaterial(MATERIALS[0].titleEn);
    setSelectedColor(COLORS[9].titleEn);
    setLocationRack('Rack A-01');
    setUnitPrice('129.99');
    setIsSeasonal(false);
    setCustomImageUrl('');
    setErrorMessage(null);
  };

  const handleFilePick = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => {
      if (typeof reader.result === 'string') {
        setCustomImageUrl(reader.result);
      }
    };
    reader.readAsDataURL(file);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      setErrorMessage(
        isIndonesian ? 'Mohon isi nama jaket' : 'Please enter jacket name'
      );
      return;
    }
    if (isManualSku && !activeSku) {
      setErrorMessage(
        isIndonesian ? 'Mohon masukkan kode SKU manual' : 'Please enter manual SKU code'
      );
      return;
    }
    if (duplicateJacket) {
      setErrorMessage(
        isIndonesian
          ? `SKU '${activeSku}' sudah digunakan oleh '${duplicateJacket.name}'. Harap gunakan SKU unik!`
          : `SKU '${activeSku}' is already in use by '${duplicateJacket.name}'. Please enter a unique SKU!`
      );
      return;
    }

    const parsedPrice = Number(unitPrice);
    addNewJacket({
      sku: activeSku || `JKT-${Date.now() % 10000}`,
      name: name.trim(),
      style: selectedStyle,
      size: selectedSize,
      material: selectedMaterial,
      color: selectedColor,
      quantity,
      locationRack: locationRack.trim() || 'Rack A-01',
      isSeasonal,
      genderCategory: selectedCategory,
      imageUrl: customImageUrl,
      unitPrice: !Number.isNaN(parsedPrice) && parsedPrice > 0 ? parsedPrice : 89.99,
    });
    navigateToHome();
  };

  return (
    <div className="max-w-3xl mx-auto space-y-5 pb-24 lg:pb-10">
      {/* Top Bar */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={navigateToHome}
            aria-label="Back"
            className="w-10 h-10 rounded-xl bg-white border border-slate-200 hover:bg-slate-50 flex items-center justify-center text-slate-700 cursor-pointer"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div>
            <h1 className="text-xl font-extrabold text-slate-900">
              {isIndonesian ? 'Tambah Jaket Baru' : 'Add New Jacket'}
            </h1>
            <p className="text-xs text-slate-500">
              {isIndonesian
                ? 'Daftarkan produk jaket baru ke inventaris gudang'
                : 'Register a new jacket SKU into warehouse inventory'}
            </p>
          </div>
        </div>

        <button
          type="button"
          onClick={handleReset}
          className="h-9 px-3 rounded-xl bg-white border border-slate-200 hover:bg-slate-50 text-xs font-semibold text-slate-700 inline-flex items-center gap-1.5 cursor-pointer"
        >
          <RotateCcw className="w-3.5 h-3.5" />
          Reset
        </button>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Main Specs Card */}
        <div className="bg-white rounded-2xl border border-slate-200/80 p-5 space-y-4 shadow-xs">
          {/* Name */}
          <div>
            <label className="block text-xs font-bold text-slate-800 mb-1.5">
              {isIndonesian ? 'Nama Produk Jaket *' : 'Jacket Product Name *'}
            </label>
            <input
              type="text"
              value={name}
              onChange={(e) => {
                setName(e.target.value);
                setErrorMessage(null);
              }}
              placeholder={
                isIndonesian
                  ? 'cth. Tactical MA-1 Bomber Black'
                  : 'e.g. Tactical MA-1 Bomber Black'
              }
              className="w-full h-11 px-3.5 rounded-xl border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-slate-800"
            />
          </div>

          {/* Target Demographic Category */}
          <div>
            <label className="block text-xs font-bold text-slate-800 mb-1.5">
              {isIndonesian ? 'Kategori Demografis *' : 'Demographic Category *'}
            </label>
            <div className="flex flex-wrap gap-2">
              {GENDER_CATEGORIES.map((cat) => {
                const active = selectedCategory === cat.titleId;
                return (
                  <button
                    key={cat.id}
                    type="button"
                    onClick={() => setSelectedCategory(cat.titleId)}
                    className={`px-3 py-1.5 rounded-xl text-xs font-semibold cursor-pointer transition ${
                      active
                        ? 'bg-slate-900 text-white'
                        : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
                    }`}
                  >
                    {isIndonesian ? cat.titleId : cat.titleEn}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Style & Price */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-slate-800 mb-1.5">
                {isIndonesian ? 'Model Jaket *' : 'Jacket Style *'}
              </label>
              <select
                value={selectedStyle}
                onChange={(e) => setSelectedStyle(e.target.value)}
                className="w-full h-11 px-3 rounded-xl border border-slate-300 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-slate-800"
              >
                {EXTRA_STYLE_OPTIONS.map((st) => (
                  <option key={st} value={st}>
                    {st}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-800 mb-1.5">
                {isIndonesian ? 'Harga Satuan (USD)' : 'Unit Price (USD)'}
              </label>
              <input
                type="number"
                step="0.01"
                min="0"
                value={unitPrice}
                onChange={(e) => setUnitPrice(e.target.value)}
                className="w-full h-11 px-3.5 rounded-xl border border-slate-300 text-sm tabular-nums focus:outline-none focus:ring-2 focus:ring-slate-800"
              />
            </div>
          </div>

          {/* Size */}
          <div>
            <label className="block text-xs font-bold text-slate-800 mb-1.5">
              {isIndonesian ? 'Ukuran (Size) *' : 'Size *'}
            </label>
            <div className="grid grid-cols-4 sm:grid-cols-7 gap-2">
              {SIZES.map((s) => {
                const active = selectedSize === s.title;
                return (
                  <button
                    key={s.id}
                    type="button"
                    onClick={() => setSelectedSize(s.title)}
                    title={isIndonesian ? s.descId : s.descEn}
                    className={`h-10 rounded-xl text-xs font-bold border transition cursor-pointer ${
                      active
                        ? 'bg-slate-900 text-white border-slate-900'
                        : 'bg-slate-50 text-slate-800 border-slate-200 hover:bg-slate-100'
                    }`}
                  >
                    {s.title}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Quantity */}
          <div className="rounded-xl bg-slate-50 border border-slate-200 p-4 space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-slate-800">
                {isIndonesian ? 'Jumlah Stok Awal *' : 'Initial Stock Quantity *'}
              </span>
              <span
                className={`px-2.5 py-0.5 rounded-full text-[11px] font-bold ${
                  calculatedStatus === StockStatusLevel.OUT_OF_STOCK
                    ? 'bg-red-100 text-red-700'
                    : calculatedStatus === StockStatusLevel.LOW_STOCK
                    ? 'bg-amber-100 text-amber-700'
                    : 'bg-emerald-100 text-emerald-700'
                }`}
              >
                {calculatedStatus === StockStatusLevel.OUT_OF_STOCK
                  ? isIndonesian
                    ? 'Habis (0)'
                    : 'Out of Stock'
                  : calculatedStatus === StockStatusLevel.LOW_STOCK
                  ? isIndonesian
                    ? 'Menipis (≤5)'
                    : 'Low Stock'
                  : isIndonesian
                  ? 'Tersedia (>5)'
                  : 'In Stock'}
              </span>
            </div>

            <div className="flex items-center gap-3">
              <button
                type="button"
                onClick={() => setQuantity((q) => Math.max(0, q - 1))}
                className="w-10 h-10 rounded-full bg-slate-200 hover:bg-slate-300 flex items-center justify-center cursor-pointer"
              >
                <Minus className="w-4 h-4" />
              </button>
              <input
                type="number"
                min={0}
                value={quantity}
                onChange={(e) => setQuantity(Math.max(0, Number(e.target.value) || 0))}
                className="w-24 h-10 text-center font-extrabold text-base bg-white border border-slate-300 rounded-xl tabular-nums"
              />
              <button
                type="button"
                onClick={() => setQuantity((q) => q + 1)}
                className="w-10 h-10 rounded-full bg-slate-200 hover:bg-slate-300 flex items-center justify-center cursor-pointer"
              >
                <Plus className="w-4 h-4" />
              </button>
              <div className="flex gap-1.5 ml-auto flex-wrap">
                {[5, 10, 25, 50].map((amt) => (
                  <button
                    key={amt}
                    type="button"
                    onClick={() => setQuantity((q) => q + amt)}
                    className="px-2.5 h-8 rounded-lg bg-white border border-slate-200 hover:bg-slate-100 text-xs font-bold text-slate-700 cursor-pointer tabular-nums"
                  >
                    +{amt}
                  </button>
                ))}
              </div>
            </div>
          </div>

          {/* SKU Section */}
          <div className="rounded-xl bg-slate-50 border border-slate-200 p-4 space-y-3">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <span className="text-xs font-bold text-slate-800">
                {isIndonesian ? 'Kode SKU / Barcode' : 'SKU / Barcode Code'}
              </span>
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={() => setIsScannerOpen(true)}
                  className="h-8 px-3 rounded-lg bg-sky-600 hover:bg-sky-700 text-white text-xs font-bold inline-flex items-center gap-1.5 cursor-pointer"
                >
                  <QrCode className="w-3.5 h-3.5" />
                  {isIndonesian ? 'Scan QR' : 'Scan QR'}
                </button>
                <button
                  type="button"
                  onClick={() => {
                    const next = !isManualSku;
                    setIsManualSku(next);
                    if (next) setManualSkuText(sku);
                  }}
                  className="h-8 px-3 rounded-lg bg-white border border-slate-300 text-xs font-bold text-slate-700 cursor-pointer"
                >
                  {isManualSku
                    ? isIndonesian
                      ? 'Auto-Gen'
                      : 'Auto-Gen'
                    : isIndonesian
                    ? 'Ubah Manual'
                    : 'Manual'}
                </button>
              </div>
            </div>

            {!isManualSku ? (
              <div className="bg-white rounded-xl border border-slate-200 p-3 flex items-center justify-between">
                <div>
                  <p className="text-[11px] text-slate-500">
                    {isIndonesian ? 'SKU Otomatis:' : 'Generated SKU:'}
                  </p>
                  <p className="font-mono-sku text-sm font-extrabold text-slate-900">
                    {sku || 'JKT-...'}
                  </p>
                </div>
                <span className="text-[11px] text-slate-400">JKT-[Style]-[Name]-[Size]</span>
              </div>
            ) : (
              <div className="space-y-2">
                <input
                  type="text"
                  value={manualSkuText}
                  onChange={(e) => {
                    const cleaned = e.target.value
                      .toUpperCase()
                      .replace(/[^A-Z0-9\-_]/g, '');
                    setManualSkuText(cleaned);
                    setErrorMessage(null);
                  }}
                  placeholder="cth. WHR-BOMBER-001"
                  className={`w-full h-10 px-3 rounded-xl border font-mono-sku text-xs font-bold bg-white ${
                    duplicateJacket ? 'border-red-500' : 'border-slate-300'
                  }`}
                />
                {duplicateJacket ? (
                  <p className="text-xs font-semibold text-red-600">
                    {isIndonesian
                      ? `SKU '${manualSkuText}' sudah digunakan oleh '${duplicateJacket.name}'!`
                      : `SKU '${manualSkuText}' is already used by '${duplicateJacket.name}'!`}
                  </p>
                ) : manualSkuText ? (
                  <p className="text-xs font-semibold text-emerald-600">
                    {isIndonesian ? 'SKU unik & siap digunakan' : 'SKU is unique & ready'}
                  </p>
                ) : null}

                <div className="flex flex-wrap gap-1.5">
                  <button
                    type="button"
                    onClick={() => setManualSkuText(generateRandomCustomSku('WHR'))}
                    className="px-2.5 py-1 rounded-lg bg-white border border-slate-300 text-[11px] font-bold text-slate-700 inline-flex items-center gap-1 cursor-pointer"
                  >
                    <RefreshCw className="w-3 h-3" />
                    Random WHR
                  </button>
                  {['WHR-', 'JKT-', 'BAR-'].map((prefix) => (
                    <button
                      key={prefix}
                      type="button"
                      onClick={() =>
                        setManualSkuText(
                          prefix +
                            manualSkuText
                              .replace(/^WHR-/, '')
                              .replace(/^JKT-/, '')
                              .replace(/^BAR-/, '')
                        )
                      }
                      className="px-2.5 py-1 rounded-lg bg-white border border-slate-300 text-[11px] font-mono-sku font-bold text-slate-700 cursor-pointer"
                    >
                      {prefix}
                    </button>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* Rack Location */}
          <div>
            <label className="block text-xs font-bold text-slate-800 mb-1.5">
              {isIndonesian ? 'Lokasi Rak Gudang *' : 'Warehouse Rack Location *'}
            </label>
            <input
              type="text"
              value={locationRack}
              onChange={(e) => setLocationRack(e.target.value)}
              placeholder="Rack A-01"
              className="w-full h-11 px-3.5 rounded-xl border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-slate-800"
            />
          </div>

          {/* Material */}
          <div>
            <label className="block text-xs font-bold text-slate-800 mb-1.5">
              {isIndonesian ? 'Bahan (Material)' : 'Material'}
            </label>
            <div className="flex flex-wrap gap-2">
              {MATERIALS.map((mat) => {
                const active = selectedMaterial === mat.titleEn;
                return (
                  <button
                    key={mat.id}
                    type="button"
                    onClick={() => setSelectedMaterial(mat.titleEn)}
                    className={`px-3 py-1.5 rounded-xl text-xs font-semibold cursor-pointer ${
                      active
                        ? 'bg-slate-900 text-white'
                        : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
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
            <label className="block text-xs font-bold text-slate-800 mb-1.5">
              {isIndonesian ? 'Warna (Color)' : 'Color'}
            </label>
            <div className="flex flex-wrap gap-2">
              {COLORS.map((col) => {
                const active = selectedColor.toLowerCase() === col.titleEn.toLowerCase();
                return (
                  <button
                    key={col.id}
                    type="button"
                    onClick={() => setSelectedColor(col.titleEn)}
                    className={`px-3 py-1.5 rounded-full text-xs font-semibold inline-flex items-center gap-1.5 border cursor-pointer ${
                      active
                        ? 'bg-slate-900 text-white border-slate-900'
                        : 'bg-slate-50 text-slate-700 border-slate-200'
                    }`}
                  >
                    <span
                      className="w-3 h-3 rounded-full border border-slate-300"
                      style={{ backgroundColor: col.hexColor }}
                    />
                    {isIndonesian ? col.titleId : col.titleEn}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Seasonal Switch */}
          <label className="flex items-center justify-between pt-2 border-t border-slate-100 cursor-pointer">
            <div>
              <p className="text-xs font-bold text-slate-900">
                {isIndonesian
                  ? 'Koleksi Musiman Musim Dingin'
                  : 'Winter Seasonal Collection'}
              </p>
              <p className="text-[11px] text-slate-500">
                {isIndonesian
                  ? 'Tandai untuk ditampilkan pada banner koleksi musiman'
                  : 'Mark for seasonal promotional showcase'}
              </p>
            </div>
            <input
              type="checkbox"
              checked={isSeasonal}
              onChange={(e) => setIsSeasonal(e.target.checked)}
              className="w-4 h-4 accent-slate-900 cursor-pointer"
            />
          </label>
        </div>

        {/* Image Picker Card */}
        <div className="bg-white rounded-2xl border border-slate-200/80 p-5 space-y-3 shadow-xs">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-sm font-bold text-slate-900 flex items-center gap-1.5">
                <ImagePlus className="w-4 h-4 text-blue-600" />
                {isIndonesian ? 'Gambar / Foto SKU Produk' : 'Product SKU Image'}
              </h3>
              <p className="text-xs text-slate-500">
                {isIndonesian
                  ? 'Unggah foto perangkat, pilih preset katalog, atau URL web'
                  : 'Upload device photo, choose catalog preset, or enter web URL'}
              </p>
            </div>
            {customImageUrl && (
              <button
                type="button"
                onClick={() => setCustomImageUrl('')}
                className="text-xs font-bold text-blue-600 cursor-pointer"
              >
                Reset
              </button>
            )}
          </div>

          <input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            onChange={handleFilePick}
            className="hidden"
          />

          <div className="grid grid-cols-2 gap-2.5">
            <button
              type="button"
              onClick={() => fileInputRef.current?.click()}
              className="h-10 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold inline-flex items-center justify-center gap-1.5 cursor-pointer"
            >
              <Upload className="w-4 h-4" />
              {isIndonesian ? 'Pilih dari Perangkat' : 'Upload Photo'}
            </button>
            <button
              type="button"
              onClick={() => setShowUrlInput((v) => !v)}
              className="h-10 rounded-xl border border-slate-300 hover:bg-slate-50 text-slate-700 text-xs font-bold inline-flex items-center justify-center gap-1.5 cursor-pointer"
            >
              <LinkIcon className="w-4 h-4" />
              {isIndonesian ? 'Input URL' : 'Enter URL'}
            </button>
          </div>

          {showUrlInput && (
            <div className="flex gap-2">
              <input
                type="url"
                value={customUrlText}
                onChange={(e) => setCustomUrlText(e.target.value)}
                placeholder="https://domain.com/jacket.png"
                className="flex-1 h-10 px-3 rounded-xl border border-slate-300 text-xs"
              />
              <button
                type="button"
                onClick={() => {
                  if (customUrlText.trim()) setCustomImageUrl(customUrlText.trim());
                }}
                className="h-10 px-4 rounded-xl bg-slate-900 text-white text-xs font-bold cursor-pointer"
              >
                {isIndonesian ? 'Terapkan' : 'Apply'}
              </button>
            </div>
          )}

          <div className="flex gap-2.5 overflow-x-auto no-scrollbar pb-1">
            {CURATED_PRESETS.map((preset) => {
              const isSelected = customImageUrl === preset.imageUrl;
              return (
                <button
                  key={preset.id}
                  type="button"
                  onClick={() => setCustomImageUrl(preset.imageUrl)}
                  className={`shrink-0 w-20 rounded-xl p-1.5 border text-center cursor-pointer ${
                    isSelected
                      ? 'border-blue-600 bg-blue-50'
                      : 'border-slate-200 bg-slate-50'
                  }`}
                >
                  <div className="w-full h-14 rounded-lg overflow-hidden bg-slate-100">
                    <NetworkImageWithFallback
                      src={preset.imageUrl}
                      alt={preset.nameEn}
                    />
                  </div>
                  <p className="text-[10px] font-semibold text-slate-700 truncate mt-1">
                    {isIndonesian ? preset.nameId : preset.nameEn}
                  </p>
                </button>
              );
            })}
          </div>
        </div>

        {/* Live Preview Card */}
        <div>
          <p className="text-xs font-bold text-slate-600 mb-2">
            {isIndonesian ? 'Pratinjau Kartu Inventaris' : 'Live Inventory Preview'}
          </p>
          <div className="bg-white rounded-2xl border border-slate-200 p-3.5 flex items-center gap-3.5 shadow-xs">
            <div className="w-16 h-16 rounded-xl overflow-hidden bg-slate-100 shrink-0">
              <NetworkImageWithFallback src={effectivePreviewImage} alt="Preview" />
            </div>
            <div className="flex-1 min-w-0">
              <div className="flex items-center justify-between gap-2">
                <span className="font-mono-sku text-[11px] font-bold text-slate-500">
                  {activeSku || 'JKT-SKU'}
                </span>
                <span
                  className={`text-xs font-bold tabular-nums ${
                    calculatedStatus === StockStatusLevel.IN_STOCK
                      ? 'text-emerald-600'
                      : calculatedStatus === StockStatusLevel.LOW_STOCK
                      ? 'text-amber-600'
                      : 'text-red-600'
                  }`}
                >
                  {quantity} units
                </span>
              </div>
              <p className="text-sm font-bold text-slate-900 truncate">
                {name || (isIndonesian ? 'Contoh Nama Jaket' : 'Sample Jacket Name')}
              </p>
              <p className="text-xs text-slate-500 truncate">
                {selectedCategory} • {selectedStyle} • {selectedSize} • {selectedColor}
              </p>
            </div>
          </div>
        </div>

        {errorMessage && (
          <div className="rounded-xl bg-red-50 border border-red-200 p-3 text-xs font-semibold text-red-700">
            {errorMessage}
          </div>
        )}

        {/* Submit Actions */}
        <div className="flex gap-3 pt-1">
          <button
            type="button"
            onClick={navigateToHome}
            className="flex-1 h-12 rounded-full border border-slate-300 bg-white text-slate-800 font-semibold text-sm hover:bg-slate-50 cursor-pointer"
          >
            {isIndonesian ? 'Batal' : 'Cancel'}
          </button>
          <button
            type="submit"
            className="flex-[2] h-12 rounded-full bg-slate-900 hover:bg-slate-800 text-white font-bold text-sm inline-flex items-center justify-center gap-2 shadow-sm cursor-pointer"
          >
            <Check className="w-4 h-4" />
            {isIndonesian ? 'Simpan ke Inventaris' : 'Add to Inventory'}
          </button>
        </div>
      </form>

      <QrCodeScannerModal
        isOpen={isScannerOpen}
        allJackets={allJackets}
        isIndonesian={isIndonesian}
        onDismiss={() => setIsScannerOpen(false)}
        onJacketIdentified={(identified) => {
          setIsScannerOpen(false);
          setIsManualSku(true);
          setManualSkuText(identified.sku);
          setSku(identified.sku);
          setName(identified.name);
          setSelectedStyle(identified.style);
          setSelectedSize(identified.size);
          setSelectedMaterial(identified.material);
          setSelectedColor(identified.color);
          setLocationRack(identified.locationRack);
        }}
        onAddNewWithSku={(scannedSku) => {
          setIsScannerOpen(false);
          setIsManualSku(true);
          setManualSkuText(scannedSku);
          setSku(scannedSku);
        }}
      />
    </div>
  );
};
