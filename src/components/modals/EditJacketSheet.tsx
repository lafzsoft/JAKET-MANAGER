import React, { useEffect, useState } from 'react';
import { Minus, Plus, Save, X } from 'lucide-react';
import {
  COLORS,
  EXTRA_STYLE_OPTIONS,
  GENDER_CATEGORIES,
  MATERIALS,
  SIZES,
} from '../../data/catalogData';
import { JacketItem } from '../../types/jacket';

interface EditJacketSheetProps {
  jacket: JacketItem | null;
  allJackets: JacketItem[];
  isIndonesian: boolean;
  onDismiss: () => void;
  onSave: (data: {
    id: string;
    sku: string;
    name: string;
    style: string;
    size: string;
    material: string;
    color: string;
    quantity: number;
    locationRack: string;
    isSeasonal: boolean;
    genderCategory: string;
  }) => void;
}

export const EditJacketSheet: React.FC<EditJacketSheetProps> = ({
  jacket,
  allJackets,
  isIndonesian,
  onDismiss,
  onSave,
}) => {
  const [name, setName] = useState('');
  const [sku, setSku] = useState('');
  const [style, setStyle] = useState(EXTRA_STYLE_OPTIONS[0]);
  const [size, setSize] = useState('M');
  const [material, setMaterial] = useState(MATERIALS[0].titleEn);
  const [color, setColor] = useState(COLORS[9].titleEn);
  const [quantity, setQuantity] = useState(0);
  const [locationRack, setLocationRack] = useState('');
  const [isSeasonal, setIsSeasonal] = useState(false);
  const [genderCategory, setGenderCategory] = useState('Jaket Pria');
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  useEffect(() => {
    if (jacket) {
      setName(jacket.name);
      setSku(jacket.sku);
      setStyle(jacket.style);
      setSize(jacket.size);
      setMaterial(jacket.material);
      setColor(jacket.color);
      setQuantity(jacket.quantity);
      setLocationRack(jacket.locationRack);
      setIsSeasonal(jacket.isSeasonal);
      setGenderCategory(jacket.genderCategory);
      setErrorMsg(null);
    }
  }, [jacket]);

  if (!jacket) return null;

  const trimmedSku = sku.trim();
  const duplicateJacket = trimmedSku
    ? allJackets.find(
        (j) => j.id !== jacket.id && j.sku.toLowerCase() === trimmedSku.toLowerCase()
      )
    : undefined;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      setErrorMsg(isIndonesian ? 'Nama produk wajib diisi' : 'Product name is required');
      return;
    }
    if (!trimmedSku) {
      setErrorMsg(isIndonesian ? 'SKU wajib diisi' : 'SKU is required');
      return;
    }
    if (duplicateJacket) {
      setErrorMsg(
        isIndonesian
          ? `SKU '${trimmedSku}' sudah digunakan oleh '${duplicateJacket.name}'`
          : `SKU '${trimmedSku}' is already used by '${duplicateJacket.name}'`
      );
      return;
    }

    onSave({
      id: jacket.id,
      sku: trimmedSku,
      name: name.trim(),
      style,
      size,
      material,
      color,
      quantity: Math.max(0, quantity),
      locationRack: locationRack.trim() || 'Rack A-01',
      isSeasonal,
      genderCategory,
    });
    onDismiss();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-slate-900/55 backdrop-blur-[1px] sm:p-4">
      <div className="bg-white w-full sm:max-w-lg rounded-t-3xl sm:rounded-2xl shadow-2xl max-h-[92vh] flex flex-col overflow-hidden">
        <div className="px-5 py-4 border-b border-slate-100 flex items-center justify-between">
          <div>
            <h3 className="text-lg font-extrabold text-slate-900">
              {isIndonesian ? 'Edit Detail Jaket' : 'Edit Jacket Details'}
            </h3>
            <p className="text-xs text-slate-500 font-mono-sku">{jacket.sku}</p>
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

        <form onSubmit={handleSubmit} className="p-5 overflow-y-auto space-y-4">
          <div>
            <label className="block text-xs font-bold text-slate-700 mb-1">
              {isIndonesian ? 'Nama Produk *' : 'Product Name *'}
            </label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full h-10 px-3 rounded-xl border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-slate-800"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">
                {isIndonesian ? 'Kode SKU *' : 'SKU Code *'}
              </label>
              <input
                type="text"
                value={sku}
                onChange={(e) => setSku(e.target.value.toUpperCase())}
                className={`w-full h-10 px-3 rounded-xl border font-mono-sku text-xs font-bold ${
                  duplicateJacket ? 'border-red-500 bg-red-50' : 'border-slate-300'
                }`}
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">
                {isIndonesian ? 'Lokasi Rak' : 'Rack Location'}
              </label>
              <input
                type="text"
                value={locationRack}
                onChange={(e) => setLocationRack(e.target.value)}
                className="w-full h-10 px-3 rounded-xl border border-slate-300 text-xs"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">
                {isIndonesian ? 'Kategori' : 'Category'}
              </label>
              <select
                value={genderCategory}
                onChange={(e) => setGenderCategory(e.target.value)}
                className="w-full h-10 px-3 rounded-xl border border-slate-300 text-xs bg-white"
              >
                {GENDER_CATEGORIES.map((c) => (
                  <option key={c.id} value={c.titleId}>
                    {isIndonesian ? c.titleId : c.titleEn}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">
                {isIndonesian ? 'Model' : 'Style'}
              </label>
              <select
                value={style}
                onChange={(e) => setStyle(e.target.value)}
                className="w-full h-10 px-3 rounded-xl border border-slate-300 text-xs bg-white"
              >
                {EXTRA_STYLE_OPTIONS.map((s) => (
                  <option key={s} value={s}>
                    {s}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 mb-1">
              {isIndonesian ? 'Ukuran' : 'Size'}
            </label>
            <div className="grid grid-cols-7 gap-1.5">
              {SIZES.map((s) => (
                <button
                  key={s.id}
                  type="button"
                  onClick={() => setSize(s.title)}
                  className={`h-9 rounded-lg text-xs font-bold border cursor-pointer ${
                    size === s.title
                      ? 'bg-slate-900 text-white border-slate-900'
                      : 'bg-slate-50 text-slate-700 border-slate-200'
                  }`}
                >
                  {s.title}
                </button>
              ))}
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">
                {isIndonesian ? 'Bahan' : 'Material'}
              </label>
              <select
                value={material}
                onChange={(e) => setMaterial(e.target.value)}
                className="w-full h-10 px-3 rounded-xl border border-slate-300 text-xs bg-white"
              >
                {MATERIALS.map((m) => (
                  <option key={m.id} value={m.titleEn}>
                    {isIndonesian ? m.titleId : m.titleEn}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">
                {isIndonesian ? 'Warna' : 'Color'}
              </label>
              <select
                value={color}
                onChange={(e) => setColor(e.target.value)}
                className="w-full h-10 px-3 rounded-xl border border-slate-300 text-xs bg-white"
              >
                {COLORS.map((c) => (
                  <option key={c.id} value={c.titleEn}>
                    {isIndonesian ? c.titleId : c.titleEn}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Quantity Stepper */}
          <div className="flex items-center justify-between p-3 rounded-xl bg-slate-50 border border-slate-200">
            <span className="text-xs font-bold text-slate-800">
              {isIndonesian ? 'Jumlah Stok' : 'Stock Quantity'}
            </span>
            <div className="flex items-center gap-2.5">
              <button
                type="button"
                onClick={() => setQuantity((q) => Math.max(0, q - 1))}
                className="w-9 h-9 rounded-full bg-slate-200 hover:bg-slate-300 flex items-center justify-center cursor-pointer"
              >
                <Minus className="w-4 h-4" />
              </button>
              <input
                type="number"
                min={0}
                value={quantity}
                onChange={(e) => setQuantity(Math.max(0, Number(e.target.value) || 0))}
                className="w-20 h-9 text-center font-extrabold text-sm bg-white border border-slate-300 rounded-lg tabular-nums"
              />
              <button
                type="button"
                onClick={() => setQuantity((q) => q + 1)}
                className="w-9 h-9 rounded-full bg-slate-200 hover:bg-slate-300 flex items-center justify-center cursor-pointer"
              >
                <Plus className="w-4 h-4" />
              </button>
            </div>
          </div>

          <label className="flex items-center justify-between px-1 cursor-pointer">
            <span className="text-xs font-semibold text-slate-700">
              {isIndonesian ? 'Koleksi Musiman (Winter)' : 'Winter Seasonal Collection'}
            </span>
            <input
              type="checkbox"
              checked={isSeasonal}
              onChange={(e) => setIsSeasonal(e.target.checked)}
              className="w-4 h-4 accent-slate-900 cursor-pointer"
            />
          </label>

          {errorMsg && (
            <div className="rounded-xl bg-red-50 border border-red-200 p-3 text-xs font-semibold text-red-700">
              {errorMsg}
            </div>
          )}

          <div className="flex gap-2.5 pt-2">
            <button
              type="button"
              onClick={onDismiss}
              className="flex-1 h-11 rounded-xl border border-slate-300 text-slate-700 font-semibold text-sm hover:bg-slate-50 cursor-pointer"
            >
              {isIndonesian ? 'Batal' : 'Cancel'}
            </button>
            <button
              type="submit"
              className="flex-1 h-11 rounded-xl bg-slate-900 hover:bg-slate-800 text-white font-bold text-sm flex items-center justify-center gap-2 cursor-pointer"
            >
              <Save className="w-4 h-4" />
              {isIndonesian ? 'Simpan Perubahan' : 'Save Changes'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
