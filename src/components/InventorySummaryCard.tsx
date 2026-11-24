import React from 'react';
import { Download, Factory, Menu, Plus } from 'lucide-react';
import { JacketItem, StockStatusLevel, getStockStatus } from '../types/jacket';

interface InventorySummaryCardProps {
  jackets: JacketItem[];
  isIndonesian: boolean;
  onToggleLanguage: () => void;
  onOpenDrawer: () => void;
  onAddNewClick: () => void;
  onProductionClick: () => void;
  onExportClick: () => void;
}

export const InventorySummaryCard: React.FC<InventorySummaryCardProps> = ({
  jackets,
  isIndonesian,
  onToggleLanguage,
  onOpenDrawer,
  onAddNewClick,
  onProductionClick,
  onExportClick,
}) => {
  const totalQuantity = jackets.reduce((sum, j) => sum + j.quantity, 0);
  const totalVariants = jackets.length;
  const inStockCount = jackets.filter(
    (j) => getStockStatus(j.quantity, j.minThreshold) === StockStatusLevel.IN_STOCK
  ).length;
  const lowStockCount = jackets.filter(
    (j) => getStockStatus(j.quantity, j.minThreshold) === StockStatusLevel.LOW_STOCK
  ).length;
  const outStockCount = jackets.filter(
    (j) => getStockStatus(j.quantity, j.minThreshold) === StockStatusLevel.OUT_OF_STOCK
  ).length;

  return (
    <section
      className="rounded-[20px] shadow-md text-white p-5 relative overflow-hidden"
      style={{
        background: 'linear-gradient(135deg, #2D3748 0%, #1E293B 55%, #0F172A 100%)',
      }}
    >
      {/* Top Bar: Drawer + Brand + Language Switcher */}
      <div className="flex items-center justify-between gap-3 mb-4">
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={onOpenDrawer}
            aria-label="Open Navigation Drawer"
            className="w-10 h-10 rounded-full bg-white/15 hover:bg-white/25 flex items-center justify-center transition cursor-pointer"
          >
            <Menu className="w-5 h-5 text-white" />
          </button>
          <div>
            <p className="text-[11px] font-semibold tracking-wider text-slate-300 uppercase">
              JACKET MANAGER PRO
            </p>
            <h1 className="text-base font-bold text-white leading-tight">
              {isIndonesian ? 'Manajemen Stok Gudang' : 'Warehouse Stock Manager'}
            </h1>
          </div>
        </div>

        <button
          type="button"
          onClick={onToggleLanguage}
          aria-label="Toggle Language"
          className="px-3 py-1.5 rounded-full bg-white/15 hover:bg-white/25 text-xs font-bold tracking-wide text-white transition cursor-pointer"
        >
          {isIndonesian ? 'ID' : 'EN'}
        </button>
      </div>

      {/* Main Stock Readout + Action Buttons */}
      <div className="flex flex-wrap items-end justify-between gap-4 mb-5">
        <div>
          <p className="text-xs text-slate-300 font-medium">
            {isIndonesian ? 'Total Stok Tersedia' : 'Total Available Stock'}
          </p>
          <div className="flex items-baseline gap-2 mt-0.5">
            <span className="text-4xl font-extrabold tracking-tight tabular-nums">
              {totalQuantity}
            </span>
            <span className="text-sm font-semibold text-slate-300">
              {isIndonesian ? 'pcs' : 'units'}
            </span>
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <button
            type="button"
            onClick={onProductionClick}
            className="h-9 px-3 rounded-xl bg-emerald-500/20 hover:bg-emerald-500/30 border border-emerald-400/30 text-emerald-200 text-xs font-semibold inline-flex items-center gap-1.5 transition cursor-pointer"
          >
            <Factory className="w-3.5 h-3.5" />
            {isIndonesian ? 'Produksi' : 'Produce'}
          </button>

          <button
            type="button"
            onClick={onAddNewClick}
            className="h-9 px-3 rounded-xl bg-white text-slate-900 hover:bg-slate-100 text-xs font-bold inline-flex items-center gap-1.5 transition cursor-pointer shadow-sm"
          >
            <Plus className="w-3.5 h-3.5" />
            {isIndonesian ? 'Tambah Baru' : 'Add New'}
          </button>

          <button
            type="button"
            onClick={onExportClick}
            className="h-9 px-3 rounded-xl bg-white/15 hover:bg-white/25 text-white text-xs font-semibold inline-flex items-center gap-1.5 transition cursor-pointer"
          >
            <Download className="w-3.5 h-3.5" />
            {isIndonesian ? 'Ekspor' : 'Export'}
          </button>
        </div>
      </div>

      {/* 4 Mini Status Cards */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
        <div className="rounded-xl bg-white/10 px-3 py-2.5 border border-white/10">
          <p className="text-[11px] text-slate-300 font-medium">
            {isIndonesian ? 'Varian SKU' : 'Unique SKUs'}
          </p>
          <p className="text-lg font-extrabold tabular-nums text-white mt-0.5">{totalVariants}</p>
        </div>

        <div className="rounded-xl bg-emerald-500/15 px-3 py-2.5 border border-emerald-400/20">
          <p className="text-[11px] text-emerald-200 font-medium">
            {isIndonesian ? 'Stok Aman' : 'In Stock'}
          </p>
          <p className="text-lg font-extrabold tabular-nums text-emerald-300 mt-0.5">
            {inStockCount}
          </p>
        </div>

        <div className="rounded-xl bg-amber-500/15 px-3 py-2.5 border border-amber-400/20">
          <p className="text-[11px] text-amber-200 font-medium">
            {isIndonesian ? 'Menipis' : 'Low Stock'}
          </p>
          <p className="text-lg font-extrabold tabular-nums text-amber-300 mt-0.5">
            {lowStockCount}
          </p>
        </div>

        <div className="rounded-xl bg-red-500/15 px-3 py-2.5 border border-red-400/20">
          <p className="text-[11px] text-red-200 font-medium">
            {isIndonesian ? 'Habis Stok' : 'Out Stock'}
          </p>
          <p className="text-lg font-extrabold tabular-nums text-red-300 mt-0.5">
            {outStockCount}
          </p>
        </div>
      </div>
    </section>
  );
};
