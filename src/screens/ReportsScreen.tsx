import React from 'react';
import {
  AlertTriangle,
  CheckCircle2,
  Download,
  FileSpreadsheet,
  Menu,
  RefreshCw,
} from 'lucide-react';
import { useJackets } from '../context/JacketContext';
import { EXTRA_STYLE_OPTIONS } from '../data/catalogData';
import { StockStatusLevel, getStockStatus } from '../types/jacket';
import { InventoryDistributionCharts } from '../components/InventoryDistributionCharts';

interface ReportsScreenProps {
  onOpenDrawer: () => void;
}

export const ReportsScreen: React.FC<ReportsScreenProps> = ({ onOpenDrawer }) => {
  const {
    allJackets,
    isIndonesian,
    openExportSheet,
    restock,
    restockAllLowStock,
  } = useJackets();

  const totalUnits = allJackets.reduce((s, j) => s + j.quantity, 0);
  const totalValue = allJackets.reduce((s, j) => s + j.quantity * j.unitPrice, 0);

  const inStockCount = allJackets.filter(
    (j) => getStockStatus(j.quantity, j.minThreshold) === StockStatusLevel.IN_STOCK
  ).length;
  const lowAndOutItems = allJackets.filter(
    (j) => getStockStatus(j.quantity, j.minThreshold) !== StockStatusLevel.IN_STOCK
  );
  const healthPct =
    allJackets.length > 0 ? Math.round((inStockCount / allJackets.length) * 100) : 0;

  return (
    <div className="space-y-5 pb-24 lg:pb-10">
      {/* Header */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-2.5">
          <button
            type="button"
            onClick={onOpenDrawer}
            aria-label="Open menu"
            className="w-10 h-10 rounded-xl bg-white border border-slate-200 hover:bg-slate-50 flex items-center justify-center text-slate-700 cursor-pointer lg:hidden"
          >
            <Menu className="w-5 h-5" />
          </button>
          <div>
            <h1 className="text-xl font-extrabold text-slate-900">
              {isIndonesian ? 'Laporan & Analitik' : 'Reports & Analytics'}
            </h1>
            <p className="text-xs text-slate-500">
              {isIndonesian
                ? 'Valuasi aset, distribusi stok, dan peringatan restock'
                : 'Asset valuation, stock distribution, and restock alerts'}
            </p>
          </div>
        </div>

        <button
          type="button"
          onClick={openExportSheet}
          className="h-10 px-4 rounded-xl bg-slate-900 hover:bg-slate-800 text-white text-xs font-bold inline-flex items-center gap-1.5 cursor-pointer"
        >
          <Download className="w-4 h-4" />
          {isIndonesian ? 'Ekspor CSV' : 'Export CSV'}
        </button>
      </div>

      {/* Total Valuation Card */}
      <div
        className="rounded-[20px] p-5 text-white shadow-md flex flex-wrap items-center justify-between gap-4"
        style={{
          background: 'linear-gradient(135deg, #2D3748 0%, #1E293B 55%, #0F172A 100%)',
        }}
      >
        <div>
          <p className="text-xs font-medium text-slate-300">
            {isIndonesian ? 'Estimasi Nilai Aset Gudang' : 'Total Estimated Stock Value'}
          </p>
          <p className="text-3xl font-extrabold tracking-tight tabular-nums mt-1">
            ${totalValue.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </p>
          <p className="text-xs text-slate-300 mt-1 tabular-nums">
            {totalUnits} {isIndonesian ? 'unit lintas' : 'units across'} {allJackets.length}{' '}
            {isIndonesian ? 'varian SKU' : 'unique SKUs'}
          </p>
        </div>

        <div className="bg-white/10 rounded-2xl px-4 py-3 border border-white/10">
          <p className="text-[11px] text-slate-300">
            {isIndonesian ? 'Kesehatan Stok' : 'Stock Health'}
          </p>
          <p className="text-2xl font-extrabold text-emerald-300 tabular-nums">{healthPct}%</p>
          <p className="text-[11px] text-slate-300 tabular-nums">
            {inStockCount} / {allJackets.length} SKU {isIndonesian ? 'aman' : 'healthy'}
          </p>
        </div>
      </div>

      {/* Interactive Bar Chart */}
      <InventoryDistributionCharts allJackets={allJackets} isIndonesian={isIndonesian} />

      {/* Stock Health Progress Card */}
      <div className="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-xs space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <CheckCircle2 className="w-5 h-5 text-emerald-600" />
            <h3 className="text-base font-bold text-slate-900">
              {isIndonesian ? 'Indeks Kesiapan Stok' : 'Inventory Stock Health'}
            </h3>
          </div>
          <span className="text-sm font-extrabold text-emerald-700 tabular-nums">
            {healthPct}%
          </span>
        </div>
        <div className="w-full h-3 rounded-full bg-slate-100 overflow-hidden">
          <div
            className="h-full rounded-full bg-emerald-600 transition-all duration-500"
            style={{ width: `${healthPct}%` }}
          />
        </div>
        <p className="text-xs text-slate-500">
          {isIndonesian
            ? `${inStockCount} dari ${allJackets.length} model jaket memiliki stok di atas batas minimum aman.`
            : `${inStockCount} of ${allJackets.length} jacket SKUs are above their minimum safety threshold.`}
        </p>
      </div>

      {/* Needs Immediate Restock */}
      <div className="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-xs space-y-3">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <div className="flex items-center gap-2">
            <AlertTriangle className="w-5 h-5 text-amber-600" />
            <h3 className="text-base font-bold text-slate-900">
              {isIndonesian ? 'Perlu Restock Segera' : 'Needs Immediate Restock'}
            </h3>
          </div>
          {lowAndOutItems.length > 0 && (
            <button
              type="button"
              onClick={restockAllLowStock}
              className="h-9 px-3.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-white text-xs font-bold inline-flex items-center gap-1.5 cursor-pointer"
            >
              <RefreshCw className="w-3.5 h-3.5" />
              {isIndonesian ? 'Restock Semua (+15)' : 'Restock All (+15)'}
            </button>
          )}
        </div>

        {lowAndOutItems.length === 0 ? (
          <div className="rounded-xl bg-emerald-50 border border-emerald-200 p-4 text-xs font-semibold text-emerald-800">
            {isIndonesian
              ? 'Semua SKU dalam kondisi stok aman!'
              : 'All SKUs are currently at healthy stock levels!'}
          </div>
        ) : (
          <div className="space-y-2.5">
            {lowAndOutItems.map((item) => {
              const isOut =
                getStockStatus(item.quantity, item.minThreshold) ===
                StockStatusLevel.OUT_OF_STOCK;
              return (
                <div
                  key={item.id}
                  className="rounded-xl border border-slate-200 p-3 flex items-center justify-between gap-3"
                >
                  <div className="min-w-0">
                    <div className="flex items-center gap-2">
                      <span
                        className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
                          isOut
                            ? 'bg-red-100 text-red-700'
                            : 'bg-amber-100 text-amber-700'
                        }`}
                      >
                        {isOut
                          ? isIndonesian
                            ? 'Habis'
                            : 'Out of Stock'
                          : isIndonesian
                          ? 'Menipis'
                          : 'Low Stock'}
                      </span>
                      <span className="font-mono-sku text-[11px] font-bold text-slate-500">
                        {item.sku}
                      </span>
                    </div>
                    <p className="text-sm font-bold text-slate-900 truncate mt-0.5">
                      {item.name}
                    </p>
                    <p className="text-xs text-slate-500 tabular-nums">
                      {item.locationRack} • {item.quantity} / min {item.minThreshold} pcs
                    </p>
                  </div>

                  <button
                    type="button"
                    onClick={() => restock(item.id)}
                    className="h-9 px-3 rounded-xl bg-[#2D3748] hover:bg-slate-800 text-white text-xs font-bold inline-flex items-center gap-1 shrink-0 cursor-pointer"
                  >
                    <RefreshCw className="w-3.5 h-3.5" />
                    +15
                  </button>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Style Breakdown Progress Bars */}
      <div className="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-xs space-y-3">
        <h3 className="text-base font-bold text-slate-900">
          {isIndonesian ? 'Distribusi per Model Jaket' : 'Style Distribution Breakdown'}
        </h3>
        <div className="space-y-3">
          {EXTRA_STYLE_OPTIONS.map((styleName) => {
            const qty = allJackets
              .filter((j) => j.style.toLowerCase() === styleName.toLowerCase())
              .reduce((s, j) => s + j.quantity, 0);
            const pct = totalUnits > 0 ? Math.round((qty / totalUnits) * 100) : 0;
            return (
              <div key={styleName}>
                <div className="flex justify-between text-xs font-semibold mb-1">
                  <span className="text-slate-800">{styleName}</span>
                  <span className="text-slate-600 tabular-nums">
                    {qty} pcs ({pct}%)
                  </span>
                </div>
                <div className="w-full h-2 rounded-full bg-slate-100 overflow-hidden">
                  <div
                    className="h-full rounded-full bg-slate-800"
                    style={{ width: `${pct}%` }}
                  />
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Export CSV Card */}
      <div className="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-xs flex flex-wrap items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="w-11 h-11 rounded-xl bg-emerald-50 text-emerald-700 flex items-center justify-center">
            <FileSpreadsheet className="w-6 h-6" />
          </div>
          <div>
            <h3 className="text-sm font-bold text-slate-900">
              {isIndonesian ? 'Ekspor Cadangan Inventaris CSV' : 'Export CSV Inventory Backup'}
            </h3>
            <p className="text-xs text-slate-500">
              {isIndonesian
                ? 'Kompatibel dengan Microsoft Excel & Google Sheets'
                : 'Compatible with Microsoft Excel & Google Sheets'}
            </p>
          </div>
        </div>
        <button
          type="button"
          onClick={openExportSheet}
          className="h-10 px-4 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold inline-flex items-center gap-1.5 cursor-pointer"
        >
          <Download className="w-4 h-4" />
          {isIndonesian ? 'Buka Ekspor CSV' : 'Open CSV Export'}
        </button>
      </div>
    </div>
  );
};
