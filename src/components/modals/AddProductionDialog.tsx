import React, { useEffect, useState } from 'react';
import { CheckCircle2, Factory, X } from 'lucide-react';
import {
  JacketItem,
  PRODUCTION_SHIFT_META,
  PRODUCTION_STATUS_META,
  ProductionShift,
  ProductionStatus,
} from '../../types/jacket';

interface AddProductionDialogProps {
  isOpen: boolean;
  allJackets: JacketItem[];
  isIndonesian: boolean;
  onDismiss: () => void;
  onSave: (data: {
    jacketId: string;
    jacketName: string;
    jacketSku: string;
    batchNumber: string;
    shift: ProductionShift;
    operatorName: string;
    quantityProduced: number;
    defectCount: number;
    status: ProductionStatus;
    notes: string;
    addedToStock: boolean;
  }) => void;
}

export const AddProductionDialog: React.FC<AddProductionDialogProps> = ({
  isOpen,
  allJackets,
  isIndonesian,
  onDismiss,
  onSave,
}) => {
  const [selectedJacketId, setSelectedJacketId] = useState('');
  const [batchNumber, setBatchNumber] = useState('');
  const [operatorName, setOperatorName] = useState('');
  const [shift, setShift] = useState<ProductionShift>(ProductionShift.MORNING);
  const [quantityProduced, setQuantityProduced] = useState('20');
  const [defectCount, setDefectCount] = useState('0');
  const [status, setStatus] = useState<ProductionStatus>(ProductionStatus.COMPLETED);
  const [notes, setNotes] = useState('');
  const [addedToStock, setAddedToStock] = useState(true);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen) {
      if (allJackets.length > 0 && !allJackets.some((j) => j.id === selectedJacketId)) {
        setSelectedJacketId(allJackets[0].id);
      }
      setBatchNumber(`BATCH-${Math.floor(100 + Math.random() * 900)}`);
      setErrorMsg(null);
    }
  }, [isOpen, allJackets, selectedJacketId]);

  if (!isOpen) return null;

  const selectedJacket = allJackets.find((j) => j.id === selectedJacketId) ?? allJackets[0];
  const qtyVal = Math.max(0, Number(quantityProduced) || 0);
  const defectVal = Math.max(0, Number(defectCount) || 0);
  const netAccepted = Math.max(0, qtyVal - defectVal);
  const defectRate = qtyVal > 0 ? ((defectVal / qtyVal) * 100).toFixed(1) : '0.0';

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedJacket) {
      setErrorMsg(
        isIndonesian ? 'Pilih model jaket terlebih dahulu' : 'Please select a jacket model'
      );
      return;
    }
    if (!operatorName.trim()) {
      setErrorMsg(
        isIndonesian ? 'Mohon isi nama operator/penanggung jawab' : 'Please enter operator name'
      );
      return;
    }
    if (qtyVal <= 0) {
      setErrorMsg(
        isIndonesian
          ? 'Jumlah produksi harus lebih dari 0'
          : 'Produced quantity must be greater than 0'
      );
      return;
    }

    onSave({
      jacketId: selectedJacket.id,
      jacketName: selectedJacket.name,
      jacketSku: selectedJacket.sku,
      batchNumber: batchNumber.trim() || `BATCH-${Date.now() % 1000}`,
      shift,
      operatorName: operatorName.trim(),
      quantityProduced: qtyVal,
      defectCount: defectVal,
      status,
      notes: notes.trim(),
      addedToStock,
    });

    setOperatorName('');
    setNotes('');
    setQuantityProduced('20');
    setDefectCount('0');
  };

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-slate-900/55 backdrop-blur-[1px] sm:p-4">
      <div className="bg-white w-full sm:max-w-lg rounded-t-3xl sm:rounded-2xl shadow-2xl max-h-[92vh] flex flex-col overflow-hidden">
        <div className="px-5 py-4 border-b border-slate-100 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-slate-900 text-white flex items-center justify-center">
              <Factory className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-lg font-extrabold text-slate-900">
                {isIndonesian ? 'Catat Batch Produksi Baru' : 'Log New Production Batch'}
              </h3>
              <p className="text-xs text-slate-500">
                {isIndonesian
                  ? 'Sinkronisasi QC otomatis ke stok gudang'
                  : 'Automatic QC sync to warehouse inventory'}
              </p>
            </div>
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
          {/* Select Jacket */}
          <div>
            <label className="block text-xs font-bold text-slate-700 mb-1">
              {isIndonesian ? 'Model Jaket Diproduksi *' : 'Target Jacket Model *'}
            </label>
            <select
              value={selectedJacket?.id ?? ''}
              onChange={(e) => setSelectedJacketId(e.target.value)}
              className="w-full h-10 px-3 rounded-xl border border-slate-300 text-xs font-semibold bg-white focus:outline-none focus:ring-2 focus:ring-slate-800"
            >
              {allJackets.map((j) => (
                <option key={j.id} value={j.id}>
                  {j.name} ({j.sku}) — {j.size}
                </option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">
                {isIndonesian ? 'Nomor Batch' : 'Batch Number'}
              </label>
              <input
                type="text"
                value={batchNumber}
                onChange={(e) => setBatchNumber(e.target.value.toUpperCase())}
                className="w-full h-10 px-3 rounded-xl border border-slate-300 font-mono-sku text-xs font-bold"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">
                {isIndonesian ? 'Nama Operator / PIC *' : 'Operator Name *'}
              </label>
              <input
                type="text"
                value={operatorName}
                onChange={(e) => {
                  setOperatorName(e.target.value);
                  setErrorMsg(null);
                }}
                placeholder={isIndonesian ? 'cth. Budi Santoso' : 'e.g. Budi Santoso'}
                className="w-full h-10 px-3 rounded-xl border border-slate-300 text-xs"
              />
            </div>
          </div>

          {/* Shift */}
          <div>
            <label className="block text-xs font-bold text-slate-700 mb-1.5">
              {isIndonesian ? 'Shift Kerja' : 'Work Shift'}
            </label>
            <div className="grid grid-cols-3 gap-2">
              {Object.values(ProductionShift).map((s) => {
                const active = shift === s;
                return (
                  <button
                    key={s}
                    type="button"
                    onClick={() => setShift(s)}
                    className={`py-2 px-2 rounded-xl text-xs font-bold border transition cursor-pointer ${
                      active
                        ? 'bg-slate-900 text-white border-slate-900'
                        : 'bg-slate-50 text-slate-700 border-slate-200'
                    }`}
                  >
                    {isIndonesian
                      ? PRODUCTION_SHIFT_META[s].shortId
                      : PRODUCTION_SHIFT_META[s].shortEn}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Quantities */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">
                {isIndonesian ? 'Total Diproduksi (pcs) *' : 'Quantity Produced *'}
              </label>
              <input
                type="number"
                min={1}
                value={quantityProduced}
                onChange={(e) => setQuantityProduced(e.target.value)}
                className="w-full h-10 px-3 rounded-xl border border-slate-300 text-sm font-bold tabular-nums"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">
                {isIndonesian ? 'Cacat / Reject (pcs)' : 'Defect Count (pcs)'}
              </label>
              <input
                type="number"
                min={0}
                value={defectCount}
                onChange={(e) => setDefectCount(e.target.value)}
                className="w-full h-10 px-3 rounded-xl border border-slate-300 text-sm font-bold tabular-nums"
              />
            </div>
          </div>

          {/* Live QC Preview */}
          <div className="rounded-xl bg-emerald-50 border border-emerald-200 p-3 flex items-center justify-between">
            <div>
              <p className="text-[11px] font-semibold text-emerald-800">
                {isIndonesian ? 'Lolos Quality Control (QC)' : 'Net Passed QC'}
              </p>
              <p className="text-lg font-extrabold text-emerald-700 tabular-nums">
                +{netAccepted} pcs
              </p>
            </div>
            <div className="text-right">
              <p className="text-[11px] text-slate-600">
                {isIndonesian ? 'Tingkat Cacat' : 'Defect Rate'}
              </p>
              <p className="text-sm font-extrabold text-slate-900 tabular-nums">{defectRate}%</p>
            </div>
          </div>

          {/* Status */}
          <div>
            <label className="block text-xs font-bold text-slate-700 mb-1.5">
              {isIndonesian ? 'Status Batch' : 'Batch Status'}
            </label>
            <div className="grid grid-cols-3 gap-2">
              {Object.values(ProductionStatus).map((st) => {
                const active = status === st;
                return (
                  <button
                    key={st}
                    type="button"
                    onClick={() => setStatus(st)}
                    className={`py-2 px-2 rounded-xl text-[11px] font-bold border transition cursor-pointer ${
                      active
                        ? 'bg-slate-900 text-white border-slate-900'
                        : 'bg-slate-50 text-slate-700 border-slate-200'
                    }`}
                  >
                    {isIndonesian
                      ? PRODUCTION_STATUS_META[st].labelId
                      : PRODUCTION_STATUS_META[st].labelEn}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Added to Stock Toggle */}
          <label className="flex items-center justify-between p-3 rounded-xl bg-slate-50 border border-slate-200 cursor-pointer">
            <div>
              <p className="text-xs font-bold text-slate-800">
                {isIndonesian
                  ? 'Tambahkan Otomatis ke Stok Inventaris'
                  : 'Auto-Add Passed QC to Inventory'}
              </p>
              <p className="text-[11px] text-slate-500">
                {isIndonesian
                  ? 'Langsung menambah stok jaket terpilih'
                  : 'Immediately increments linked jacket stock'}
              </p>
            </div>
            <input
              type="checkbox"
              checked={addedToStock}
              onChange={(e) => setAddedToStock(e.target.checked)}
              className="w-4 h-4 accent-slate-900 cursor-pointer"
            />
          </label>

          {/* Notes */}
          <div>
            <label className="block text-xs font-bold text-slate-700 mb-1">
              {isIndonesian ? 'Catatan Jahitan / QC' : 'Production / QC Notes'}
            </label>
            <textarea
              rows={2}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder={
                isIndonesian
                  ? 'cth. Jahitan rapi, kancing terpasang kuat...'
                  : 'e.g. Stitching verified, zipper tested...'
              }
              className="w-full p-3 rounded-xl border border-slate-300 text-xs focus:outline-none focus:ring-2 focus:ring-slate-800"
            />
          </div>

          {errorMsg && (
            <div className="rounded-xl bg-red-50 border border-red-200 p-3 text-xs font-semibold text-red-700">
              {errorMsg}
            </div>
          )}

          <button
            type="submit"
            className="w-full h-11 rounded-xl bg-slate-900 hover:bg-slate-800 text-white font-bold text-sm inline-flex items-center justify-center gap-2 cursor-pointer"
          >
            <CheckCircle2 className="w-4 h-4" />
            {isIndonesian ? 'Simpan Laporan Produksi' : 'Save Production Log'}
          </button>
        </form>
      </div>
    </div>
  );
};
