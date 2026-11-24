import React, { useState } from 'react';
import {
  CheckCircle2,
  Clock,
  Factory,
  Menu,
  Pencil,
  Plus,
  RefreshCw,
  Trash2,
  User,
} from 'lucide-react';
import { useJackets } from '../context/JacketContext';
import {
  DailyProductionItem,
  PRODUCTION_SHIFT_META,
  PRODUCTION_STATUS_META,
  ProductionShift,
  ProductionStatus,
  getDefectRatePercentage,
  getNetAcceptedQuantity,
} from '../types/jacket';
import { AddProductionDialog } from '../components/modals/AddProductionDialog';
import { ConfirmDialog } from '../components/ConfirmDialog';

interface DailyProductionScreenProps {
  onOpenDrawer: () => void;
}

export const DailyProductionScreen: React.FC<DailyProductionScreenProps> = ({
  onOpenDrawer,
}) => {
  const {
    allJackets,
    filteredProductions,
    dailySummary,
    productionShiftFilter,
    isIndonesian,
    isAddProductionSheetOpen,
    setProductionShiftFilter,
    openAddProductionSheet,
    closeAddProductionSheet,
    updateDailyTarget,
    addDailyProduction,
    deleteDailyProduction,
  } = useJackets();

  const [isEditingTarget, setIsEditingTarget] = useState(false);
  const [targetInput, setTargetInput] = useState(String(dailySummary.dailyTarget));
  const [batchToDelete, setBatchToDelete] = useState<DailyProductionItem | null>(null);

  const shiftFilters = [
    { id: 'ALL', label: isIndonesian ? 'Semua Shift' : 'All Shifts' },
    { id: ProductionShift.MORNING, label: isIndonesian ? 'Shift Pagi' : 'Morning' },
    { id: ProductionShift.AFTERNOON, label: isIndonesian ? 'Shift Siang' : 'Afternoon' },
    { id: ProductionShift.NIGHT, label: isIndonesian ? 'Shift Malam' : 'Night' },
  ];

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
              {isIndonesian ? 'Produksi Harian' : 'Daily Production'}
            </h1>
            <p className="text-xs text-slate-500">
              {isIndonesian
                ? 'Pantau batch jahit, kontrol kualitas (QC) & target harian'
                : 'Track sewing batches, QC inspection & daily targets'}
            </p>
          </div>
        </div>

        <button
          type="button"
          onClick={openAddProductionSheet}
          className="h-10 px-4 rounded-xl bg-slate-900 hover:bg-slate-800 text-white text-xs font-bold inline-flex items-center gap-1.5 cursor-pointer"
        >
          <Plus className="w-4 h-4" />
          {isIndonesian ? 'Catat Batch' : 'Log Batch'}
        </button>
      </div>

      {/* Daily KPI Card */}
      <div
        className="rounded-[20px] p-5 text-white shadow-md space-y-4"
        style={{
          background: 'linear-gradient(135deg, #2D3748 0%, #1E293B 55%, #0F172A 100%)',
        }}
      >
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <p className="text-xs text-slate-300 font-medium">
              {isIndonesian ? 'Pencapaian Lolos QC Hari Ini' : 'Today Passed QC Output'}
            </p>
            <div className="flex items-baseline gap-2 mt-1">
              <span className="text-3xl font-extrabold tabular-nums">
                {dailySummary.totalAcceptedToday}
              </span>
              <span className="text-sm font-semibold text-slate-300 tabular-nums">
                / {dailySummary.dailyTarget} pcs
              </span>
            </div>
          </div>

          <div className="flex items-center gap-2">
            {isEditingTarget ? (
              <div className="flex items-center gap-1.5 bg-white/10 rounded-xl p-1">
                <input
                  type="number"
                  min={1}
                  value={targetInput}
                  onChange={(e) => setTargetInput(e.target.value)}
                  className="w-16 h-8 px-2 rounded-lg bg-white text-slate-900 text-xs font-bold tabular-nums"
                />
                <button
                  type="button"
                  onClick={() => {
                    const parsed = Number(targetInput);
                    if (!Number.isNaN(parsed) && parsed > 0) {
                      updateDailyTarget(parsed);
                    }
                    setIsEditingTarget(false);
                  }}
                  className="h-8 px-2.5 rounded-lg bg-emerald-500 text-white text-xs font-bold cursor-pointer"
                >
                  OK
                </button>
              </div>
            ) : (
              <button
                type="button"
                onClick={() => {
                  setTargetInput(String(dailySummary.dailyTarget));
                  setIsEditingTarget(true);
                }}
                className="h-8 px-3 rounded-xl bg-white/15 hover:bg-white/25 text-xs font-semibold inline-flex items-center gap-1.5 cursor-pointer"
              >
                <Pencil className="w-3.5 h-3.5" />
                {isIndonesian ? 'Ubah Target' : 'Edit Target'}
              </button>
            )}
          </div>
        </div>

        {/* Progress Bar */}
        <div>
          <div className="flex justify-between text-xs font-semibold mb-1.5">
            <span className="text-slate-300">
              {isIndonesian ? 'Pemenuhan Target Harian' : 'Daily Target Fulfillment'}
            </span>
            <span className="text-emerald-300 tabular-nums">
              {dailySummary.targetFulfillmentPercentage.toFixed(0)}%
            </span>
          </div>
          <div className="w-full h-2.5 rounded-full bg-white/15 overflow-hidden">
            <div
              className="h-full rounded-full bg-emerald-400 transition-all duration-500"
              style={{ width: `${Math.min(100, dailySummary.targetFulfillmentPercentage)}%` }}
            />
          </div>
        </div>

        {/* 4 Mini KPIs */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
          <div className="rounded-xl bg-white/10 p-2.5">
            <p className="text-[11px] text-slate-300">
              {isIndonesian ? 'Total Dibuat' : 'Total Made'}
            </p>
            <p className="text-lg font-extrabold tabular-nums">
              {dailySummary.totalProducedToday}
            </p>
          </div>
          <div className="rounded-xl bg-emerald-500/15 p-2.5">
            <p className="text-[11px] text-emerald-200">
              {isIndonesian ? 'Lolos QC' : 'Passed QC'}
            </p>
            <p className="text-lg font-extrabold text-emerald-300 tabular-nums">
              {dailySummary.totalAcceptedToday}
            </p>
          </div>
          <div className="rounded-xl bg-amber-500/15 p-2.5">
            <p className="text-[11px] text-amber-200">
              {isIndonesian ? 'Reject / Cacat' : 'Defects'}
            </p>
            <p className="text-lg font-extrabold text-amber-300 tabular-nums">
              {dailySummary.totalDefectsToday} ({dailySummary.averageDefectRate.toFixed(1)}%)
            </p>
          </div>
          <div className="rounded-xl bg-white/10 p-2.5">
            <p className="text-[11px] text-slate-300">
              {isIndonesian ? 'Total Batch' : 'Total Batches'}
            </p>
            <p className="text-lg font-extrabold tabular-nums">
              {dailySummary.totalBatchesToday}
            </p>
          </div>
        </div>
      </div>

      {/* Automatic Stock Sync Banner */}
      <div className="rounded-2xl bg-emerald-50 border border-emerald-200 p-3.5 flex items-center gap-3">
        <div className="w-9 h-9 rounded-xl bg-emerald-100 text-emerald-700 flex items-center justify-center shrink-0">
          <RefreshCw className="w-4 h-4" />
        </div>
        <p className="text-xs text-emerald-900 leading-relaxed">
          {isIndonesian
            ? 'Sinkronisasi Stok Otomatis Aktif: Setiap batch yang ditandai masuk ke stok akan langsung menambah jumlah unit pada SKU jaket terkait.'
            : 'Automatic Stock Sync Active: Batches marked for inventory sync automatically increment the linked jacket SKU stock.'}
        </p>
      </div>

      {/* Shift Filter Chips */}
      <div className="flex gap-2 overflow-x-auto no-scrollbar pb-0.5">
        {shiftFilters.map((f) => {
          const active = productionShiftFilter === f.id;
          return (
            <button
              key={f.id}
              type="button"
              onClick={() => setProductionShiftFilter(f.id)}
              className={`shrink-0 px-3.5 py-2 rounded-xl text-xs font-bold transition cursor-pointer ${
                active
                  ? 'bg-slate-900 text-white'
                  : 'bg-white text-slate-700 border border-slate-200 hover:bg-slate-50'
              }`}
            >
              {f.label}
            </button>
          );
        })}
      </div>

      {/* Batches List */}
      {filteredProductions.length === 0 ? (
        <div className="bg-white rounded-2xl border border-slate-200 p-8 text-center space-y-2">
          <Factory className="w-10 h-10 text-slate-300 mx-auto" />
          <h3 className="text-base font-bold text-slate-800">
            {isIndonesian ? 'Belum Ada Catatan Batch' : 'No Production Batches Logged'}
          </h3>
          <p className="text-xs text-slate-500">
            {isIndonesian
              ? 'Klik "Catat Batch" untuk menambahkan hasil produksi baru.'
              : 'Click "Log Batch" to record a new sewing production batch.'}
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3.5">
          {filteredProductions.map((batch) => {
            const netQty = getNetAcceptedQuantity(batch);
            const defectRate = getDefectRatePercentage(batch);
            const statusClass =
              batch.status === ProductionStatus.COMPLETED
                ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                : batch.status === ProductionStatus.IN_PROGRESS
                ? 'bg-sky-50 text-sky-700 border-sky-200'
                : 'bg-amber-50 text-amber-700 border-amber-200';

            return (
              <div
                key={batch.id}
                className="bg-white rounded-2xl border border-slate-200/80 shadow-xs p-4 space-y-3"
              >
                <div className="flex items-start justify-between gap-2">
                  <div className="flex flex-wrap items-center gap-1.5">
                    <span className="px-2 py-0.5 rounded-md bg-slate-900 text-white font-mono-sku text-[11px] font-bold">
                      {batch.batchNumber}
                    </span>
                    <span className="px-2 py-0.5 rounded-md bg-slate-100 text-slate-700 text-[11px] font-semibold">
                      {isIndonesian
                        ? PRODUCTION_SHIFT_META[batch.shift].shortId
                        : PRODUCTION_SHIFT_META[batch.shift].shortEn}
                    </span>
                    <span
                      className={`px-2 py-0.5 rounded-md border text-[11px] font-bold ${statusClass}`}
                    >
                      {isIndonesian
                        ? PRODUCTION_STATUS_META[batch.status].labelId
                        : PRODUCTION_STATUS_META[batch.status].labelEn}
                    </span>
                  </div>

                  <button
                    type="button"
                    onClick={() => setBatchToDelete(batch)}
                    aria-label="Delete production batch"
                    className="w-8 h-8 rounded-lg hover:bg-red-50 text-red-500 flex items-center justify-center cursor-pointer"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>

                <div>
                  <h3 className="text-sm font-extrabold text-slate-900">{batch.jacketName}</h3>
                  <p className="font-mono-sku text-[11px] font-semibold text-slate-500">
                    {batch.jacketSku}
                  </p>
                </div>

                <div className="grid grid-cols-3 gap-2 rounded-xl bg-slate-50 border border-slate-100 p-2.5 text-center">
                  <div>
                    <p className="text-[10px] text-slate-500">
                      {isIndonesian ? 'Lolos QC' : 'Passed'}
                    </p>
                    <p className="text-sm font-extrabold text-emerald-700 tabular-nums">
                      +{netQty} pcs
                    </p>
                  </div>
                  <div>
                    <p className="text-[10px] text-slate-500">
                      {isIndonesian ? 'Cacat' : 'Defect'}
                    </p>
                    <p className="text-sm font-extrabold text-amber-700 tabular-nums">
                      -{batch.defectCount} ({defectRate.toFixed(1)}%)
                    </p>
                  </div>
                  <div>
                    <p className="text-[10px] text-slate-500">
                      {isIndonesian ? 'Stok Sync' : 'Stock Sync'}
                    </p>
                    <p className="text-xs font-bold text-slate-800 inline-flex items-center justify-center gap-1 mt-0.5">
                      {batch.addedToStock ? (
                        <>
                          <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
                          {isIndonesian ? 'Ya' : 'Synced'}
                        </>
                      ) : (
                        <>
                          <Clock className="w-3.5 h-3.5 text-slate-400" />
                          {isIndonesian ? 'Belum' : 'Pending'}
                        </>
                      )}
                    </p>
                  </div>
                </div>

                <div className="flex items-center justify-between gap-2 text-xs text-slate-600">
                  <span className="inline-flex items-center gap-1.5 font-semibold">
                    <User className="w-3.5 h-3.5 text-slate-400" />
                    {batch.operatorName}
                  </span>
                  <span className="text-[11px] text-slate-400 tabular-nums">
                    {batch.dateString}
                  </span>
                </div>

                {batch.notes && (
                  <p className="text-xs text-slate-500 bg-slate-50 rounded-lg px-3 py-2 border border-slate-100">
                    {batch.notes}
                  </p>
                )}
              </div>
            );
          })}
        </div>
      )}

      <AddProductionDialog
        isOpen={isAddProductionSheetOpen}
        allJackets={allJackets}
        isIndonesian={isIndonesian}
        onDismiss={closeAddProductionSheet}
        onSave={addDailyProduction}
      />

      <ConfirmDialog
        isOpen={batchToDelete !== null}
        title={isIndonesian ? 'Hapus Batch Produksi?' : 'Delete Production Batch?'}
        message={
          batchToDelete
            ? isIndonesian
              ? `Hapus catatan produksi '${batchToDelete.batchNumber}' (${batchToDelete.jacketName})?`
              : `Remove production log '${batchToDelete.batchNumber}' (${batchToDelete.jacketName})?`
            : ''
        }
        confirmLabel={isIndonesian ? 'Hapus' : 'Delete'}
        cancelLabel={isIndonesian ? 'Batal' : 'Cancel'}
        onConfirm={() => {
          if (batchToDelete) {
            deleteDailyProduction(batchToDelete.id);
            setBatchToDelete(null);
          }
        }}
        onCancel={() => setBatchToDelete(null)}
      />
    </div>
  );
};
