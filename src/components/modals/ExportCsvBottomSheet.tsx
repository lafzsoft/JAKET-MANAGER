import React, { useMemo, useState } from 'react';
import { Check, Copy, Download, FileSpreadsheet, X } from 'lucide-react';
import { JacketItem } from '../../types/jacket';
import { downloadCsvFile, generateCsv, generateFilename } from '../../utils/csvExport';

type ExportScope = 'ALL' | 'FILTERED' | 'PRIA' | 'WANITA' | 'ANAK' | 'UNISEX';

interface ExportCsvBottomSheetProps {
  isOpen: boolean;
  allJackets: JacketItem[];
  currentFilteredJackets: JacketItem[];
  isIndonesian: boolean;
  onDismiss: () => void;
  onNotify?: (msg: string) => void;
}

export const ExportCsvBottomSheet: React.FC<ExportCsvBottomSheetProps> = ({
  isOpen,
  allJackets,
  currentFilteredJackets,
  isIndonesian,
  onDismiss,
  onNotify,
}) => {
  const [selectedScope, setSelectedScope] = useState<ExportScope>('ALL');
  const [copiedSuccess, setCopiedSuccess] = useState(false);

  const jacketsToExport = useMemo(() => {
    switch (selectedScope) {
      case 'ALL':
        return allJackets;
      case 'FILTERED':
        return currentFilteredJackets;
      case 'PRIA':
        return allJackets.filter((j) => j.genderCategory.toLowerCase().includes('pria'));
      case 'WANITA':
        return allJackets.filter((j) => j.genderCategory.toLowerCase().includes('wanita'));
      case 'ANAK':
        return allJackets.filter((j) => j.genderCategory.toLowerCase().includes('anak'));
      case 'UNISEX':
        return allJackets.filter((j) => j.genderCategory.toLowerCase().includes('unisex'));
    }
  }, [selectedScope, allJackets, currentFilteredJackets]);

  const totalUnits = useMemo(
    () => jacketsToExport.reduce((s, j) => s + j.quantity, 0),
    [jacketsToExport]
  );
  const totalValue = useMemo(
    () => jacketsToExport.reduce((s, j) => s + j.quantity * j.unitPrice, 0),
    [jacketsToExport]
  );

  const csvContent = useMemo(
    () => generateCsv(jacketsToExport, isIndonesian),
    [jacketsToExport, isIndonesian]
  );

  const previewLines = useMemo(
    () => csvContent.replace(/^\uFEFF/, '').split('\r\n').filter(Boolean).slice(0, 5),
    [csvContent]
  );

  if (!isOpen) return null;

  const scopes: Array<{ id: ExportScope; label: string }> = [
    { id: 'ALL', label: isIndonesian ? 'Semua Produk' : 'All Products' },
    { id: 'FILTERED', label: isIndonesian ? 'Filter Aktif' : 'Active Filter' },
    { id: 'PRIA', label: 'Jaket Pria' },
    { id: 'WANITA', label: 'Jaket Wanita' },
    { id: 'ANAK', label: 'Jaket Anak' },
    { id: 'UNISEX', label: 'Jaket Unisex' },
  ];

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(csvContent);
      setCopiedSuccess(true);
      onNotify?.(
        isIndonesian ? 'Data CSV disalin ke clipboard' : 'CSV content copied to clipboard'
      );
      window.setTimeout(() => setCopiedSuccess(false), 2500);
    } catch {
      onNotify?.(
        isIndonesian ? 'Gagal menyalin ke clipboard' : 'Could not copy to clipboard'
      );
    }
  };

  const handleDownload = () => {
    const filename = generateFilename(
      selectedScope === 'ALL'
        ? 'inventaris_jaket_lengkap'
        : `inventaris_jaket_${selectedScope.toLowerCase()}`
    );
    downloadCsvFile(csvContent, filename);
    onNotify?.(
      isIndonesian ? `Mengunduh ${filename}` : `Downloading ${filename}`
    );
  };

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-slate-900/55 backdrop-blur-[1px] sm:p-4">
      <div className="bg-white w-full sm:max-w-xl rounded-t-3xl sm:rounded-2xl shadow-2xl max-h-[90vh] flex flex-col overflow-hidden">
        <div className="px-5 py-4 border-b border-slate-100 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-emerald-50 text-emerald-700 flex items-center justify-center">
              <FileSpreadsheet className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-lg font-extrabold text-slate-900">
                {isIndonesian ? 'Ekspor & Cadangkan CSV' : 'Export & Backup CSV'}
              </h3>
              <p className="text-xs text-slate-500">
                RFC 4180 • UTF-8 BOM (Excel & Google Sheets)
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

        <div className="p-5 overflow-y-auto space-y-4">
          {/* Scope Selector */}
          <div>
            <p className="text-xs font-bold text-slate-700 mb-2">
              {isIndonesian ? 'Pilih Cakupan Data:' : 'Select Export Scope:'}
            </p>
            <div className="flex flex-wrap gap-2">
              {scopes.map((sc) => {
                const active = selectedScope === sc.id;
                return (
                  <button
                    key={sc.id}
                    type="button"
                    onClick={() => setSelectedScope(sc.id)}
                    className={`px-3 py-1.5 rounded-xl text-xs font-semibold transition cursor-pointer ${
                      active
                        ? 'bg-slate-900 text-white'
                        : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
                    }`}
                  >
                    {sc.label}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Summary Cards */}
          <div className="grid grid-cols-3 gap-2.5">
            <div className="rounded-xl bg-slate-50 border border-slate-200 p-3">
              <p className="text-[11px] text-slate-500">
                {isIndonesian ? 'Varian Produk' : 'Products'}
              </p>
              <p className="text-lg font-extrabold text-slate-900 tabular-nums mt-0.5">
                {jacketsToExport.length}
              </p>
            </div>
            <div className="rounded-xl bg-slate-50 border border-slate-200 p-3">
              <p className="text-[11px] text-slate-500">
                {isIndonesian ? 'Total Unit' : 'Total Units'}
              </p>
              <p className="text-lg font-extrabold text-slate-900 tabular-nums mt-0.5">
                {totalUnits}
              </p>
            </div>
            <div className="rounded-xl bg-emerald-50 border border-emerald-200 p-3">
              <p className="text-[11px] text-emerald-700">
                {isIndonesian ? 'Nilai Aset' : 'Asset Value'}
              </p>
              <p className="text-lg font-extrabold text-emerald-800 tabular-nums mt-0.5">
                ${totalValue.toFixed(0)}
              </p>
            </div>
          </div>

          {/* Live Preview */}
          <div>
            <p className="text-xs font-bold text-slate-700 mb-1.5">
              {isIndonesian ? 'Pratinjau Konten CSV:' : 'CSV Content Preview:'}
            </p>
            <pre className="rounded-xl bg-slate-900 text-emerald-300 p-3 text-[11px] font-mono-sku overflow-x-auto leading-relaxed">
              {previewLines.join('\n')}
              {jacketsToExport.length > 4 ? '\n...' : ''}
            </pre>
          </div>

          {/* Actions */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5 pt-1">
            <button
              type="button"
              disabled={jacketsToExport.length === 0}
              onClick={handleCopy}
              className="h-11 rounded-xl border border-slate-300 hover:bg-slate-50 disabled:opacity-50 text-slate-800 font-bold text-sm inline-flex items-center justify-center gap-2 cursor-pointer"
            >
              {copiedSuccess ? (
                <>
                  <Check className="w-4 h-4 text-emerald-600" />
                  {isIndonesian ? 'Tersalin!' : 'Copied!'}
                </>
              ) : (
                <>
                  <Copy className="w-4 h-4" />
                  {isIndonesian ? 'Salin ke Clipboard' : 'Copy to Clipboard'}
                </>
              )}
            </button>

            <button
              type="button"
              disabled={jacketsToExport.length === 0}
              onClick={handleDownload}
              className="h-11 rounded-xl bg-slate-900 hover:bg-slate-800 disabled:opacity-50 text-white font-bold text-sm inline-flex items-center justify-center gap-2 cursor-pointer"
            >
              <Download className="w-4 h-4" />
              {isIndonesian ? 'Unduh File .CSV' : 'Download .CSV File'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
