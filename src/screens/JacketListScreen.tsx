import React, { useMemo, useState } from 'react';
import {
  ArrowLeft,
  Database,
  Minus,
  Pencil,
  Plus,
  RefreshCw,
  Search,
  Trash2,
  X,
} from 'lucide-react';
import { useJackets } from '../context/JacketContext';
import { JacketItem, StockStatusLevel, getStockStatus } from '../types/jacket';
import { NetworkImageWithFallback } from '../components/NetworkImageWithFallback';
import { StockStatusBadge } from '../components/JacketCards';
import { EmptyInventoryView } from '../components/EmptyInventoryView';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { AddJacketSheet } from '../components/modals/AddJacketSheet';
import { EditJacketSheet } from '../components/modals/EditJacketSheet';
import { JacketDetailModal } from '../components/modals/JacketDetailModal';

type FilterTab = 'ALL' | StockStatusLevel;

export const JacketListScreen: React.FC = () => {
  const {
    allJackets,
    isIndonesian,
    navigateToHome,
    navigateToAddJacket,
    openImagePickerForJacket,
    adjustQuantity,
    restock,
    restockAllLowStock,
    deleteJacket,
    addNewJacket,
    updateJacketDetails,
    seedSampleStock,
  } = useJackets();

  const [searchQuery, setSearchQuery] = useState('');
  const [filterTab, setFilterTab] = useState<FilterTab>('ALL');
  const [isAddSheetOpen, setIsAddSheetOpen] = useState(false);
  const [jacketToDelete, setJacketToDelete] = useState<JacketItem | null>(null);
  const [jacketToEdit, setJacketToEdit] = useState<JacketItem | null>(null);
  const [selectedDetail, setSelectedDetail] = useState<JacketItem | null>(null);

  const displayedJackets = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    return allJackets.filter((j) => {
      const status = getStockStatus(j.quantity, j.minThreshold);
      const tabOk = filterTab === 'ALL' || status === filterTab;
      const searchOk =
        !q ||
        j.name.toLowerCase().includes(q) ||
        j.sku.toLowerCase().includes(q) ||
        j.locationRack.toLowerCase().includes(q);
      return tabOk && searchOk;
    });
  }, [allJackets, searchQuery, filterTab]);

  const lowCount = allJackets.filter(
    (j) => getStockStatus(j.quantity, j.minThreshold) !== StockStatusLevel.IN_STOCK
  ).length;

  const tabs: Array<{ id: FilterTab; label: string }> = [
    { id: 'ALL', label: isIndonesian ? 'Semua' : 'All' },
    { id: StockStatusLevel.IN_STOCK, label: isIndonesian ? 'Tersedia' : 'In Stock' },
    { id: StockStatusLevel.LOW_STOCK, label: isIndonesian ? 'Menipis' : 'Low Stock' },
    { id: StockStatusLevel.OUT_OF_STOCK, label: isIndonesian ? 'Habis' : 'Out of Stock' },
  ];

  return (
    <div className="space-y-4 pb-24 lg:pb-10">
      {/* Top Bar */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-2.5">
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
              {isIndonesian ? 'Daftar Status Stok' : 'Stock Status List'}
            </h1>
            <p className="text-xs text-slate-500 inline-flex items-center gap-1.5">
              <Database className="w-3.5 h-3.5 text-emerald-600" />
              {isIndonesian ? 'Penyimpanan Lokal Aktif' : 'Live Local Storage Sync'}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {lowCount > 0 && (
            <button
              type="button"
              onClick={restockAllLowStock}
              className="h-10 px-3 rounded-xl bg-amber-500 hover:bg-amber-600 text-white text-xs font-bold inline-flex items-center gap-1.5 cursor-pointer"
            >
              <RefreshCw className="w-3.5 h-3.5" />
              {isIndonesian ? `Restock Semua (${lowCount})` : `Restock All (${lowCount})`}
            </button>
          )}
          <button
            type="button"
            onClick={() => setIsAddSheetOpen(true)}
            className="h-10 px-3.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-white text-xs font-bold inline-flex items-center gap-1.5 cursor-pointer"
          >
            <Plus className="w-4 h-4" />
            {isIndonesian ? 'Tambah' : 'Quick Add'}
          </button>
        </div>
      </div>

      {/* Search */}
      <div className="relative">
        <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
        <input
          type="text"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          placeholder={
            isIndonesian ? 'Cari SKU, nama, atau rak...' : 'Search SKU, name, or rack...'
          }
          className="w-full h-11 pl-10 pr-9 rounded-2xl bg-white border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-slate-800"
        />
        {searchQuery && (
          <button
            type="button"
            onClick={() => setSearchQuery('')}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 cursor-pointer"
          >
            <X className="w-4 h-4" />
          </button>
        )}
      </div>

      {/* Status Filter Tabs */}
      <div className="flex gap-2 overflow-x-auto no-scrollbar pb-0.5">
        {tabs.map((tab) => {
          const active = filterTab === tab.id;
          return (
            <button
              key={tab.id}
              type="button"
              onClick={() => setFilterTab(tab.id)}
              className={`shrink-0 px-3.5 py-2 rounded-xl text-xs font-bold transition cursor-pointer ${
                active
                  ? 'bg-slate-900 text-white'
                  : 'bg-white text-slate-700 border border-slate-200 hover:bg-slate-50'
              }`}
            >
              {tab.label}
            </button>
          );
        })}
      </div>

      {/* Stock Rows */}
      {displayedJackets.length === 0 ? (
        <EmptyInventoryView
          isDatabaseEmpty={allJackets.length === 0}
          isIndonesian={isIndonesian}
          activeFilterName={filterTab !== 'ALL' ? filterTab : searchQuery}
          onAddJacketClick={navigateToAddJacket}
          onLoadSampleStock={seedSampleStock}
          onResetFilter={() => {
            setFilterTab('ALL');
            setSearchQuery('');
          }}
        />
      ) : (
        <div className="space-y-3">
          {displayedJackets.map((jacket) => {
            const status = getStockStatus(jacket.quantity, jacket.minThreshold);
            const targetMax = Math.max(10, jacket.minThreshold * 2);
            const healthRatio = Math.min(100, Math.round((jacket.quantity / targetMax) * 100));
            const barColor =
              status === StockStatusLevel.IN_STOCK
                ? 'bg-emerald-600'
                : status === StockStatusLevel.LOW_STOCK
                ? 'bg-amber-500'
                : 'bg-red-600';

            return (
              <div
                key={jacket.id}
                className="bg-white rounded-2xl border border-slate-200/80 p-4 shadow-xs space-y-3"
              >
                <div className="flex items-start gap-3">
                  <div
                    className="w-16 h-16 rounded-xl overflow-hidden bg-slate-100 shrink-0 cursor-pointer"
                    onClick={() => setSelectedDetail(jacket)}
                  >
                    <NetworkImageWithFallback src={jacket.imageUrl} alt={jacket.name} />
                  </div>

                  <div
                    className="flex-1 min-w-0 cursor-pointer"
                    onClick={() => setSelectedDetail(jacket)}
                  >
                    <div className="flex items-center justify-between gap-2 flex-wrap">
                      <span className="font-mono-sku text-[11px] font-bold text-slate-500">
                        {jacket.sku}
                      </span>
                      <StockStatusBadge status={status} isIndonesian={isIndonesian} compact />
                    </div>
                    <h3 className="text-sm font-bold text-slate-900 truncate mt-0.5">
                      {jacket.name}
                    </h3>
                    <p className="text-xs text-slate-500">
                      {jacket.style} • {jacket.size} • {jacket.locationRack}
                    </p>
                  </div>

                  <div className="flex items-center gap-1 shrink-0">
                    <button
                      type="button"
                      onClick={() => setJacketToEdit(jacket)}
                      aria-label="Edit jacket"
                      className="w-8 h-8 rounded-lg hover:bg-slate-100 text-slate-600 flex items-center justify-center cursor-pointer"
                    >
                      <Pencil className="w-4 h-4" />
                    </button>
                    <button
                      type="button"
                      onClick={() => setJacketToDelete(jacket)}
                      aria-label="Delete jacket"
                      className="w-8 h-8 rounded-lg hover:bg-red-50 text-red-500 flex items-center justify-center cursor-pointer"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>

                {/* Health Bar */}
                <div>
                  <div className="flex justify-between text-[11px] font-semibold text-slate-500 mb-1">
                    <span>
                      {isIndonesian ? 'Level Kesehatan Stok' : 'Stock Health Level'}
                    </span>
                    <span className="tabular-nums">
                      {jacket.quantity} / {targetMax} pcs
                    </span>
                  </div>
                  <div className="w-full h-2 rounded-full bg-slate-100 overflow-hidden">
                    <div
                      className={`h-full rounded-full transition-all ${barColor}`}
                      style={{ width: `${healthRatio}%` }}
                    />
                  </div>
                </div>

                {/* Controls */}
                <div className="flex items-center justify-between gap-3 pt-1">
                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      onClick={() => adjustQuantity(jacket.id, -1)}
                      className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 flex items-center justify-center cursor-pointer"
                    >
                      <Minus className="w-4 h-4" />
                    </button>
                    <span className="min-w-[30px] text-center text-sm font-extrabold tabular-nums">
                      {jacket.quantity}
                    </span>
                    <button
                      type="button"
                      onClick={() => adjustQuantity(jacket.id, 1)}
                      className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 flex items-center justify-center cursor-pointer"
                    >
                      <Plus className="w-4 h-4" />
                    </button>
                  </div>

                  {status !== StockStatusLevel.IN_STOCK && (
                    <button
                      type="button"
                      onClick={() => restock(jacket.id)}
                      className="h-8 px-3 rounded-lg bg-[#2D3748] hover:bg-slate-800 text-white text-xs font-bold inline-flex items-center gap-1 cursor-pointer"
                    >
                      <RefreshCw className="w-3 h-3" />
                      +15 Restock
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      <AddJacketSheet
        isOpen={isAddSheetOpen}
        isIndonesian={isIndonesian}
        existingJackets={allJackets}
        onDismiss={() => setIsAddSheetOpen(false)}
        onSave={(data) => addNewJacket(data)}
      />

      <EditJacketSheet
        jacket={jacketToEdit}
        allJackets={allJackets}
        isIndonesian={isIndonesian}
        onDismiss={() => setJacketToEdit(null)}
        onSave={(data) => updateJacketDetails(data)}
      />

      <JacketDetailModal
        jacket={selectedDetail}
        allJackets={allJackets}
        isIndonesian={isIndonesian}
        onDismiss={() => setSelectedDetail(null)}
        onAdjustQuantity={adjustQuantity}
        onRestock={restock}
        onEditImage={openImagePickerForJacket}
        onDelete={(item) => setJacketToDelete(item)}
        onSelectVariant={(variant) => setSelectedDetail(variant)}
      />

      <ConfirmDialog
        isOpen={jacketToDelete !== null}
        title={isIndonesian ? 'Hapus Jaket?' : 'Delete Jacket?'}
        message={
          jacketToDelete
            ? isIndonesian
              ? `Apakah Anda yakin ingin menghapus '${jacketToDelete.name}' (${jacketToDelete.sku})?`
              : `Are you sure you want to delete '${jacketToDelete.name}' (${jacketToDelete.sku})?`
            : ''
        }
        confirmLabel={isIndonesian ? 'Hapus' : 'Delete'}
        cancelLabel={isIndonesian ? 'Batal' : 'Cancel'}
        onConfirm={() => {
          if (jacketToDelete) {
            deleteJacket(jacketToDelete.id);
            setJacketToDelete(null);
          }
        }}
        onCancel={() => setJacketToDelete(null)}
      />
    </div>
  );
};
