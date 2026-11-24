import React from 'react';
import { FilterX, Package, Plus, Sparkles } from 'lucide-react';

interface EmptyInventoryViewProps {
  isDatabaseEmpty: boolean;
  isIndonesian: boolean;
  activeFilterName?: string;
  onAddJacketClick: () => void;
  onLoadSampleStock: () => void;
  onResetFilter: () => void;
}

export const EmptyInventoryView: React.FC<EmptyInventoryViewProps> = ({
  isDatabaseEmpty,
  isIndonesian,
  activeFilterName = '',
  onAddJacketClick,
  onLoadSampleStock,
  onResetFilter,
}) => {
  if (isDatabaseEmpty) {
    return (
      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm px-6 py-8 flex flex-col items-center text-center max-w-lg mx-auto my-4">
        <div className="w-44 h-44 rounded-2xl bg-slate-50 border border-slate-100 flex items-center justify-center overflow-hidden mb-5">
          <svg
            viewBox="0 0 180 180"
            className="w-36 h-36"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
            aria-hidden="true"
          >
            <rect x="24" y="20" width="132" height="140" rx="14" fill="#E2E8F0" />
            <rect x="34" y="30" width="112" height="120" rx="8" fill="#F8FAFC" />
            <line x1="42" y1="62" x2="138" y2="62" stroke="#94A3B8" strokeWidth="4" strokeLinecap="round" />
            <path d="M68 62V72C68 74 62 78 58 80" stroke="#64748B" strokeWidth="3" strokeLinecap="round" />
            <path d="M112 62V72C112 74 118 78 122 80" stroke="#64748B" strokeWidth="3" strokeLinecap="round" />
            <path d="M88 62V74" stroke="#CBD5E1" strokeWidth="3" strokeLinecap="round" />
            <rect x="52" y="122" width="76" height="18" rx="4" fill="#CBD5E1" />
            <circle cx="90" cy="131" r="2.5" fill="#475569" />
          </svg>
        </div>

        <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-amber-100 border border-amber-300 text-amber-800 text-xs font-semibold mb-3">
          <span className="w-2 h-2 rounded-full bg-amber-600" />
          {isIndonesian ? 'Inventaris Kosong' : 'Closet Waiting'}
        </span>

        <h3 className="text-xl font-bold text-slate-800 mb-2">
          {isIndonesian ? 'Lemari Jaket Masih Kosong' : 'Your Wardrobe is Waiting'}
        </h3>

        <p className="text-sm text-slate-500 leading-relaxed mb-6">
          {isIndonesian
            ? 'Belum ada jaket yang tersimpan. Tambahkan jaket pertamamu untuk mulai melacak model, ukuran, rak gudang, dan jumlah stok secara rapi!'
            : 'No jackets are currently logged in your inventory. Add your first jacket to track styles, sizes, warehouse racks, and stock quantities!'}
        </p>

        <div className="w-full space-y-2.5">
          <button
            type="button"
            onClick={onAddJacketClick}
            className="w-full h-12 rounded-xl bg-slate-800 hover:bg-slate-900 text-white font-semibold text-sm flex items-center justify-center gap-2 transition cursor-pointer"
          >
            <Plus className="w-4 h-4" />
            {isIndonesian ? 'Tambah Jaket Pertama' : 'Add First Jacket'}
          </button>

          <button
            type="button"
            onClick={onLoadSampleStock}
            className="w-full h-12 rounded-xl border border-slate-300 hover:bg-slate-50 text-slate-700 font-medium text-sm flex items-center justify-center gap-2 transition cursor-pointer"
          >
            <Sparkles className="w-4 h-4 text-slate-600" />
            {isIndonesian ? 'Muat Stok Sampel (Default)' : 'Load Sample Jackets'}
          </button>
        </div>
      </div>
    );
  }

  const explanation =
    activeFilterName && activeFilterName !== 'ALL'
      ? isIndonesian
        ? `Tidak ditemukan jaket dengan filter '${activeFilterName}'. Coba pilih model jaket lain atau reset filter.`
        : `No jackets match the '${activeFilterName}' filter. Try selecting a different style or resetting your filters.`
      : isIndonesian
      ? 'Kriteria pencarian tidak cocok dengan jaket manapun. Silakan coba kata kunci lain.'
      : 'No jackets match your current search criteria. Try a different keyword.';

  return (
    <div className="bg-white rounded-2xl border border-slate-200 shadow-sm px-6 py-8 flex flex-col items-center text-center max-w-lg mx-auto my-4">
      <div className="w-20 h-20 rounded-full bg-slate-100 flex items-center justify-center mb-4">
        <FilterX className="w-9 h-9 text-slate-500" />
      </div>

      <h3 className="text-lg font-bold text-slate-800 mb-2">
        {isIndonesian ? 'Tidak Ada Jaket yang Sesuai' : 'No Matching Jackets Found'}
      </h3>

      <p className="text-sm text-slate-500 leading-relaxed mb-5">{explanation}</p>

      <button
        type="button"
        onClick={onResetFilter}
        className="h-11 px-5 rounded-xl bg-[#2D3748] hover:bg-slate-800 text-white font-semibold text-sm inline-flex items-center gap-2 transition cursor-pointer"
      >
        <Package className="w-4 h-4" />
        {isIndonesian ? 'Tampilkan Semua Jaket' : 'Show All Jackets'}
      </button>
    </div>
  );
};
