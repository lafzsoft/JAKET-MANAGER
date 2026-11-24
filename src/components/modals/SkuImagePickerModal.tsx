import React, { useEffect, useRef, useState } from 'react';
import { Check, ImagePlus, Link as LinkIcon, Save, Upload, X } from 'lucide-react';
import { CURATED_PRESETS } from '../../data/catalogData';
import { JacketItem } from '../../types/jacket';
import { NetworkImageWithFallback } from '../NetworkImageWithFallback';

interface SkuImagePickerModalProps {
  jacket: JacketItem | null;
  isIndonesian: boolean;
  onDismiss: () => void;
  onSave: (newImageUrl: string, updateAllWithSameSku: boolean) => void;
}

export const SkuImagePickerModal: React.FC<SkuImagePickerModalProps> = ({
  jacket,
  isIndonesian,
  onDismiss,
  onSave,
}) => {
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const [selectedImageUrl, setSelectedImageUrl] = useState('');
  const [customUrlInput, setCustomUrlInput] = useState('');
  const [showUrlInput, setShowUrlInput] = useState(false);
  const [applyToAllWithSameSku, setApplyToAllWithSameSku] = useState(true);

  useEffect(() => {
    if (jacket) {
      setSelectedImageUrl(jacket.imageUrl);
      setCustomUrlInput('');
      setShowUrlInput(false);
      setApplyToAllWithSameSku(true);
    }
  }, [jacket]);

  if (!jacket) return null;

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => {
      if (typeof reader.result === 'string') {
        setSelectedImageUrl(reader.result);
      }
    };
    reader.readAsDataURL(file);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-slate-900/55 backdrop-blur-[1px] sm:p-4">
      <div className="bg-white w-full sm:max-w-lg rounded-t-3xl sm:rounded-2xl shadow-2xl max-h-[90vh] flex flex-col overflow-hidden">
        <div className="px-5 py-4 border-b border-slate-100 flex items-center justify-between">
          <div>
            <h3 className="text-lg font-extrabold text-slate-900">
              {isIndonesian ? 'Pilih Foto SKU Produk' : 'Select Product SKU Photo'}
            </h3>
            <p className="text-xs text-slate-500">
              {jacket.name} • <span className="font-mono-sku font-bold">{jacket.sku}</span>
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

        <div className="p-5 overflow-y-auto space-y-4">
          {/* Preview */}
          <div className="h-44 rounded-2xl overflow-hidden bg-slate-100 border border-slate-200">
            <NetworkImageWithFallback src={selectedImageUrl} alt={jacket.name} />
          </div>

          {/* Upload & URL Actions */}
          <input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            onChange={handleFileChange}
            className="hidden"
          />

          <div className="grid grid-cols-2 gap-2.5">
            <button
              type="button"
              onClick={() => fileInputRef.current?.click()}
              className="h-10 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold inline-flex items-center justify-center gap-1.5 cursor-pointer"
            >
              <Upload className="w-4 h-4" />
              {isIndonesian ? 'Unggah dari Perangkat' : 'Upload from Device'}
            </button>

            <button
              type="button"
              onClick={() => setShowUrlInput((v) => !v)}
              className="h-10 rounded-xl border border-slate-300 hover:bg-slate-50 text-slate-700 text-xs font-bold inline-flex items-center justify-center gap-1.5 cursor-pointer"
            >
              <LinkIcon className="w-4 h-4" />
              {isIndonesian ? 'Masukkan URL Gambar' : 'Enter Image URL'}
            </button>
          </div>

          {showUrlInput && (
            <div className="flex gap-2">
              <input
                type="url"
                value={customUrlInput}
                onChange={(e) => setCustomUrlInput(e.target.value)}
                placeholder="https://example.com/jacket.png"
                className="flex-1 h-10 px-3 rounded-xl border border-slate-300 text-xs focus:outline-none focus:ring-2 focus:ring-slate-800"
              />
              <button
                type="button"
                onClick={() => {
                  if (customUrlInput.trim()) {
                    setSelectedImageUrl(customUrlInput.trim());
                  }
                }}
                className="h-10 px-4 rounded-xl bg-slate-900 text-white text-xs font-bold cursor-pointer"
              >
                {isIndonesian ? 'Terapkan' : 'Apply'}
              </button>
            </div>
          )}

          {/* Curated Presets */}
          <div>
            <p className="text-xs font-bold text-slate-700 mb-2 flex items-center gap-1.5">
              <ImagePlus className="w-4 h-4 text-slate-500" />
              {isIndonesian ? 'Preset Katalog Pilihan' : 'Curated Catalog Presets'}
            </p>
            <div className="grid grid-cols-3 gap-2">
              {CURATED_PRESETS.map((preset) => {
                const isSelected = selectedImageUrl === preset.imageUrl;
                return (
                  <button
                    key={preset.id}
                    type="button"
                    onClick={() => setSelectedImageUrl(preset.imageUrl)}
                    className={`rounded-xl p-1.5 border text-center transition cursor-pointer ${
                      isSelected
                        ? 'border-blue-600 bg-blue-50 ring-2 ring-blue-200'
                        : 'border-slate-200 bg-slate-50 hover:border-slate-300'
                    }`}
                  >
                    <div className="h-16 rounded-lg overflow-hidden bg-slate-100 relative">
                      <NetworkImageWithFallback
                        src={preset.imageUrl}
                        alt={isIndonesian ? preset.nameId : preset.nameEn}
                      />
                      {isSelected && (
                        <span className="absolute top-1 right-1 w-5 h-5 rounded-full bg-blue-600 text-white flex items-center justify-center">
                          <Check className="w-3 h-3" />
                        </span>
                      )}
                    </div>
                    <p className="text-[11px] font-semibold text-slate-800 truncate mt-1">
                      {isIndonesian ? preset.nameId : preset.nameEn}
                    </p>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Apply to same SKU checkbox */}
          <label className="flex items-center gap-2.5 p-3 rounded-xl bg-slate-50 border border-slate-200 cursor-pointer">
            <input
              type="checkbox"
              checked={applyToAllWithSameSku}
              onChange={(e) => setApplyToAllWithSameSku(e.target.checked)}
              className="w-4 h-4 accent-slate-900 cursor-pointer"
            />
            <span className="text-xs font-semibold text-slate-700">
              {isIndonesian
                ? `Terapkan ke semua jaket dengan SKU ${jacket.sku}`
                : `Apply to all jackets with SKU ${jacket.sku}`}
            </span>
          </label>

          <div className="flex gap-2.5 pt-1">
            <button
              type="button"
              onClick={onDismiss}
              className="flex-1 h-11 rounded-xl border border-slate-300 text-slate-700 font-semibold text-sm cursor-pointer"
            >
              {isIndonesian ? 'Batal' : 'Cancel'}
            </button>
            <button
              type="button"
              onClick={() => {
                onSave(selectedImageUrl, applyToAllWithSameSku);
                onDismiss();
              }}
              className="flex-1 h-11 rounded-xl bg-slate-900 hover:bg-slate-800 text-white font-bold text-sm inline-flex items-center justify-center gap-2 cursor-pointer"
            >
              <Save className="w-4 h-4" />
              {isIndonesian ? 'Simpan Foto' : 'Save Photo'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
