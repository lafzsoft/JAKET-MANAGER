import React from 'react';
import { Plus } from 'lucide-react';
import { HERO_IMAGE_URL } from '../data/catalogData';
import { NetworkImageWithFallback } from './NetworkImageWithFallback';

interface HeroSplitSectionProps {
  isIndonesian: boolean;
  onAddNewClick: () => void;
}

export const HeroSplitSection: React.FC<HeroSplitSectionProps> = ({
  isIndonesian,
  onAddNewClick,
}) => {
  return (
    <section className="bg-white rounded-2xl border border-slate-200/80 shadow-xs overflow-hidden">
      <div className="grid grid-cols-1 md:grid-cols-12 items-stretch">
        <div className="md:col-span-5 h-44 md:h-auto min-h-[176px] relative bg-slate-100">
          <NetworkImageWithFallback
            src={HERO_IMAGE_URL}
            alt={isIndonesian ? 'Gudang Jaket Utama' : 'Jacket Warehouse Showcase'}
            className="w-full h-full object-cover"
          />
          <div className="absolute inset-0 bg-gradient-to-t from-slate-900/50 via-transparent to-transparent" />
          <span className="absolute bottom-3 left-3 px-2.5 py-1 rounded-md bg-slate-900/80 text-white text-[11px] font-semibold tracking-wide">
            {isIndonesian ? 'GUDANG PUSAT' : 'CENTRAL WAREHOUSE'}
          </span>
        </div>

        <div className="md:col-span-7 p-5 flex flex-col justify-center">
          <span className="text-xs font-bold uppercase tracking-wider text-slate-500 mb-1">
            {isIndonesian ? 'KATALOG & LOGISTIK' : 'CATALOG & LOGISTICS'}
          </span>
          <h2 className="text-xl font-extrabold text-slate-900 tracking-tight mb-2">
            {isIndonesian
              ? 'Kelola Setiap Model, Ukuran & Rak Gudang'
              : 'Organize Every Style, Size & Rack Location'}
          </h2>
          <p className="text-sm text-slate-600 leading-relaxed mb-4">
            {isIndonesian
              ? 'Pantau ketersediaan stok real-time dari Bomber, Parka, Denim hingga Trench Coat. Daftarkan produk baru atau sinkronkan hasil produksi harian langsung ke inventaris.'
              : 'Track real-time stock across Bomber, Parka, Denim, and Trench Coat lines. Register new SKUs or sync daily production batches straight into inventory.'}
          </p>
          <div>
            <button
              type="button"
              onClick={onAddNewClick}
              className="h-10 px-4 rounded-xl bg-[#2D3748] hover:bg-slate-800 text-white text-sm font-semibold inline-flex items-center gap-2 transition cursor-pointer"
            >
              <Plus className="w-4 h-4" />
              {isIndonesian ? 'Tambah Jaket Baru' : 'Add New Jacket'}
            </button>
          </div>
        </div>
      </div>
    </section>
  );
};
