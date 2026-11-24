import React, { useMemo, useState } from 'react';
import {
  Download,
  LayoutGrid,
  List,
  Menu,
  Plus,
  QrCode,
  Search,
  X,
} from 'lucide-react';
import { useJackets } from '../context/JacketContext';
import {
  JacketItem,
  JacketSortOrder,
  StockStatusLevel,
  getStockStatus,
} from '../types/jacket';
import { StyleFilterBar } from '../components/StyleFilterBar';
import { DatabaseJacketCard, JacketGridCard } from '../components/JacketCards';
import { EmptyInventoryView } from '../components/EmptyInventoryView';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { AddJacketSheet } from '../components/modals/AddJacketSheet';
import { EditJacketSheet } from '../components/modals/EditJacketSheet';
import { JacketDetailModal } from '../components/modals/JacketDetailModal';
import { QrCodeScannerModal } from '../components/modals/QrCodeScannerModal';

interface InventoryScreenProps {
  onOpenDrawer: () => void;
}

type StatusTab = 'ALL' | 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK' | 'SEASONAL';

export const InventoryScreen: React.FC<InventoryScreenProps> = ({ onOpenDrawer }) => {
  const {
    allJackets,
    isIndonesian,
    navigateToAddJacket,
    navigateToAddJacketWithSku,
    openExportSheet,
    openImagePickerForJacket,
    adjustQuantity,
    restock,
    deleteJacket,
    addNewJacket,
    updateJacketDetails,
    seedSampleStock,
  } = useJackets();

  const [searchQuery, setSearchQuery] = useState('');
  const [statusTab, setStatusTab] = useState<StatusTab>('ALL');
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [selectedStyle, setSelectedStyle] = useState('ALL');
  const [sortOrder, setSortOrder] = useState<JacketSortOrder>(JacketSortOrder.NAME_ASC);
  const [isGridView, setIsGridView] = useState(true);

  const [isScannerOpen, setIsScannerOpen] = useState(false);
  const [isAddSheetOpen, setIsAddSheetOpen] = useState(false);
  const [jacketToDelete, setJacketToDelete] = useState<JacketItem | null>(null);
  const [jacketToEdit, setJacketToEdit] = useState<JacketItem | null>(null);
  const [selectedJacket, setSelectedJacket] = useState<JacketItem | null>(null);

  const displayedJackets = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    const filtered = allJackets.filter((j) => {
      const status = getStockStatus(j.quantity, j.minThreshold);
      const tabOk =
        statusTab === 'ALL' ||
        (statusTab === 'IN_STOCK' && status === StockStatusLevel.IN_STOCK) ||
        (statusTab === 'LOW_STOCK' && status === StockStatusLevel.LOW_STOCK) ||
        (statusTab === 'OUT_OF_STOCK' && status === StockStatusLevel.OUT_OF_STOCK) ||
        (statusTab === 'SEASONAL' && j.isSeasonal);

      const catOk =
        selectedCategory === 'ALL' ||
        j.genderCategory.toLowerCase() === selectedCategory.toLowerCase();
      const styleOk =
        selectedStyle === 'ALL' || j.style.toLowerCase() === selectedStyle.toLowerCase();
      const searchOk =
        !q ||
        j.name.toLowerCase().includes(q) ||
        j.sku.toLowerCase().includes(q) ||
        j.locationRack.toLowerCase().includes(q) ||
        j.style.toLowerCase().includes(q);
      return tabOk && catOk && styleOk && searchOk;
    });

    const sorted = [...filtered];
    switch (sortOrder) {
      case JacketSortOrder.NAME_ASC:
        sorted.sort((a, b) => a.name.localeCompare(b.name));
        break;
      case JacketSortOrder.QTY_ASC:
        sorted.sort((a, b) => a.quantity - b.quantity);
        break;
      case JacketSortOrder.QTY_DESC:
        sorted.sort((a, b) => b.quantity - a.quantity);
        break;
      case JacketSortOrder.STYLE:
        sorted.sort((a, b) => a.style.localeCompare(b.style));
        break;
    }
    return sorted;
  }, [allJackets, searchQuery, statusTab, selectedCategory, selectedStyle, sortOrder]);

  const totalUnits = allJackets.reduce((s, j) => s + j.quantity, 0);
  const inStockCount = allJackets.filter(
    (j) => getStockStatus(j.quantity, j.minThreshold) === StockStatusLevel.IN_STOCK
  ).length;
  const lowCount = allJackets.filter(
    (j) => getStockStatus(j.quantity, j.minThreshold) === StockStatusLevel.LOW_STOCK
  ).length;
  const outCount = allJackets.filter(
    (j) => getStockStatus(j.quantity, j.minThreshold) === StockStatusLevel.OUT_OF_STOCK
  ).length;

  const tabs: Array<{ id: StatusTab; label: string }> = [
    { id: 'ALL', label: isIndonesian ? 'Semua' : 'All' },
    { id: 'IN_STOCK', label: isIndonesian ? 'Tersedia' : 'In Stock' },
    { id: 'LOW_STOCK', label: isIndonesian ? 'Menipis' : 'Low Stock' },
    { id: 'OUT_OF_STOCK', label: isIndonesian ? 'Habis' : 'Out Stock' },
    { id: 'SEASONAL', label: isIndonesian ? 'Musiman' : 'Seasonal' },
  ];

  const quickKeywords = ['Bomber', 'Parka', 'Denim', 'Trench', 'Puffer'];

  return (
    <div className="space-y-4 pb-24 lg:pb-10">
      {/* Top Header Bar */}
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
              {isIndonesian ? 'Katalog Inventaris' : 'Inventory Catalog'}
            </h1>
            <p className="text-xs text-slate-500">
              {isIndonesian
                ? 'Kelola SKU, lokasi rak, foto, dan level stok'
                : 'Manage SKUs, rack bins, photos, and stock levels'}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2 flex-wrap">
          <button
            type="button"
            onClick={() => setIsGridView((v) => !v)}
            title={isGridView ? 'Switch to List' : 'Switch to Grid'}
            className="h-10 px-3 rounded-xl bg-white border border-slate-200 hover:bg-slate-50 text-slate-700 text-xs font-bold inline-flex items-center gap-1.5 cursor-pointer"
          >
            {isGridView ? <List className="w-4 h-4" /> : <LayoutGrid className="w-4 h-4" />}
            {isGridView ? (isIndonesian ? 'Daftar' : 'List') : 'Grid'}
          </button>

          <button
            type="button"
            onClick={() => setIsScannerOpen(true)}
            className="h-10 px-3 rounded-xl bg-white border border-slate-200 hover:bg-slate-50 text-slate-800 text-xs font-bold inline-flex items-center gap-1.5 cursor-pointer"
          >
            <QrCode className="w-4 h-4 text-sky-600" />
            {isIndonesian ? 'Scan QR' : 'Scan QR'}
          </button>

          <button
            type="button"
            onClick={openExportSheet}
            className="h-10 px-3 rounded-xl bg-white border border-slate-200 hover:bg-slate-50 text-slate-800 text-xs font-bold inline-flex items-center gap-1.5 cursor-pointer"
          >
            <Download className="w-4 h-4 text-emerald-600" />
            CSV
          </button>

          <button
            type="button"
            onClick={() => setIsAddSheetOpen(true)}
            className="h-10 px-3.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-white text-xs font-bold inline-flex items-center gap-1.5 cursor-pointer"
          >
            <Plus className="w-4 h-4" />
            {isIndonesian ? 'Tambah' : 'Add Jacket'}
          </button>
        </div>
      </div>

      {/* Summary Banner */}
      <div
        className="rounded-2xl p-4 text-white flex flex-wrap items-center justify-between gap-4"
        style={{
          background: 'linear-gradient(135deg, #2D3748 0%, #1E293B 100%)',
        }}
      >
        <div className="flex items-center gap-6">
          <div>
            <p className="text-[11px] text-slate-300">
              {isIndonesian ? 'Total Stok' : 'Total Stock'}
            </p>
            <p className="text-2xl font-extrabold tabular-nums">{totalUnits} pcs</p>
          </div>
          <div className="h-8 w-px bg-white/15" />
          <div>
            <p className="text-[11px] text-slate-300">
              {isIndonesian ? 'Total SKU' : 'Unique SKUs'}
            </p>
            <p className="text-2xl font-extrabold tabular-nums">{allJackets.length}</p>
          </div>
        </div>

        <div className="flex items-center gap-2 text-xs font-bold tabular-nums">
          <span className="px-2.5 py-1 rounded-lg bg-emerald-500/20 text-emerald-300">
            {inStockCount} {isIndonesian ? 'Aman' : 'Safe'}
          </span>
          <span className="px-2.5 py-1 rounded-lg bg-amber-500/20 text-amber-300">
            {lowCount} {isIndonesian ? 'Menipis' : 'Low'}
          </span>
          <span className="px-2.5 py-1 rounded-lg bg-red-500/20 text-red-300">
            {outCount} {isIndonesian ? 'Habis' : 'Out'}
          </span>
        </div>
      </div>

      {/* Search + Quick Search Chips */}
      <div className="space-y-2">
        <div className="relative flex items-center">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder={
              isIndonesian
                ? 'Cari SKU, nama jaket, atau lokasi rak...'
                : 'Search SKU, jacket name, or rack...'
            }
            className="w-full h-11 pl-10 pr-20 rounded-2xl bg-white border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-slate-800"
          />
          <div className="absolute right-2 flex items-center gap-1">
            {searchQuery && (
              <button
                type="button"
                onClick={() => setSearchQuery('')}
                className="w-8 h-8 rounded-full hover:bg-slate-100 flex items-center justify-center text-slate-400 cursor-pointer"
              >
                <X className="w-4 h-4" />
              </button>
            )}
            <button
              type="button"
              onClick={() => setIsScannerOpen(true)}
              aria-label="Scan barcode"
              className="w-8 h-8 rounded-xl bg-slate-100 hover:bg-slate-200 flex items-center justify-center text-slate-700 cursor-pointer"
            >
              <QrCode className="w-4 h-4" />
            </button>
          </div>
        </div>

        <div className="flex items-center gap-1.5 overflow-x-auto no-scrollbar">
          <span className="text-[11px] font-semibold text-slate-500 shrink-0 mr-1">
            {isIndonesian ? 'Cepat:' : 'Quick:'}
          </span>
          {quickKeywords.map((kw) => (
            <button
              key={kw}
              type="button"
              onClick={() => setSearchQuery(searchQuery === kw ? '' : kw)}
              className={`shrink-0 px-2.5 py-1 rounded-full text-[11px] font-semibold border transition cursor-pointer ${
                searchQuery.toLowerCase() === kw.toLowerCase()
                  ? 'bg-slate-900 text-white border-slate-900'
                  : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50'
              }`}
            >
              {kw}
            </button>
          ))}
        </div>
      </div>

      {/* Status Tabs */}
      <div className="flex gap-2 overflow-x-auto no-scrollbar pb-0.5">
        {tabs.map((tab) => {
          const active = statusTab === tab.id;
          return (
            <button
              key={tab.id}
              type="button"
              onClick={() => setStatusTab(tab.id)}
              className={`shrink-0 px-3.5 py-2 rounded-xl text-xs font-bold transition cursor-pointer ${
                active
                  ? 'bg-slate-900 text-white shadow-xs'
                  : 'bg-white text-slate-700 border border-slate-200 hover:bg-slate-50'
              }`}
            >
              {tab.label}
            </button>
          );
        })}
      </div>

      {/* Category, Style & Sort Bar */}
      <StyleFilterBar
        selectedCategory={selectedCategory}
        onCategorySelected={setSelectedCategory}
        selectedStyle={selectedStyle}
        onStyleSelected={setSelectedStyle}
        sortOrder={sortOrder}
        onSortOrderSelected={setSortOrder}
        totalCount={displayedJackets.length}
        isIndonesian={isIndonesian}
      />

      {/* Items Grid or List */}
      {displayedJackets.length === 0 ? (
        <EmptyInventoryView
          isDatabaseEmpty={allJackets.length === 0}
          isIndonesian={isIndonesian}
          activeFilterName={
            selectedCategory !== 'ALL'
              ? selectedCategory
              : selectedStyle !== 'ALL'
              ? selectedStyle
              : statusTab !== 'ALL'
              ? statusTab
              : searchQuery
          }
          onAddJacketClick={navigateToAddJacket}
          onLoadSampleStock={seedSampleStock}
          onResetFilter={() => {
            setStatusTab('ALL');
            setSelectedCategory('ALL');
            setSelectedStyle('ALL');
            setSearchQuery('');
          }}
        />
      ) : isGridView ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-3.5">
          {displayedJackets.map((jacket) => (
            <JacketGridCard
              key={jacket.id}
              jacket={jacket}
              isIndonesian={isIndonesian}
              onAdjustQuantity={(delta) => adjustQuantity(jacket.id, delta)}
              onRestock={() => restock(jacket.id)}
              onEditImage={() => openImagePickerForJacket(jacket)}
              onEditDetails={() => setJacketToEdit(jacket)}
              onDelete={() => setJacketToDelete(jacket)}
              onClick={() => setSelectedJacket(jacket)}
            />
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          {displayedJackets.map((jacket) => (
            <DatabaseJacketCard
              key={jacket.id}
              jacket={jacket}
              isIndonesian={isIndonesian}
              onAdjustQuantity={(delta) => adjustQuantity(jacket.id, delta)}
              onRestock={() => restock(jacket.id)}
              onEditImage={() => openImagePickerForJacket(jacket)}
              onEditDetails={() => setJacketToEdit(jacket)}
              onDelete={() => setJacketToDelete(jacket)}
              onClick={() => setSelectedJacket(jacket)}
            />
          ))}
        </div>
      )}

      {/* Modals */}
      <JacketDetailModal
        jacket={selectedJacket}
        allJackets={allJackets}
        isIndonesian={isIndonesian}
        onDismiss={() => setSelectedJacket(null)}
        onAdjustQuantity={adjustQuantity}
        onRestock={restock}
        onEditImage={openImagePickerForJacket}
        onDelete={(item) => setJacketToDelete(item)}
        onSelectVariant={(variant) => setSelectedJacket(variant)}
      />

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

      <QrCodeScannerModal
        isOpen={isScannerOpen}
        allJackets={allJackets}
        isIndonesian={isIndonesian}
        onDismiss={() => setIsScannerOpen(false)}
        onJacketIdentified={(identified) => {
          setIsScannerOpen(false);
          setSelectedJacket(identified);
        }}
        onAddNewWithSku={(scannedSku) => {
          setIsScannerOpen(false);
          navigateToAddJacketWithSku(scannedSku);
        }}
      />

      <ConfirmDialog
        isOpen={jacketToDelete !== null}
        title={isIndonesian ? 'Hapus Jaket?' : 'Delete Jacket?'}
        message={
          jacketToDelete
            ? isIndonesian
              ? `Apakah Anda yakin ingin menghapus '${jacketToDelete.name}' (${jacketToDelete.sku}) dari database?`
              : `Are you sure you want to delete '${jacketToDelete.name}' (${jacketToDelete.sku}) from the database?`
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
