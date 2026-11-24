import React, { useMemo, useState } from 'react';
import { Plus, Search, X } from 'lucide-react';
import { useJackets } from '../context/JacketContext';
import { JacketItem, JacketSortOrder } from '../types/jacket';
import { InventorySummaryCard } from '../components/InventorySummaryCard';
import { HeroSplitSection } from '../components/HeroSplitSection';
import {
  ColorsCarouselSection,
  DemographicCategoriesSection,
  MaterialsGridSection,
  SeasonalBannerSection,
  SizesGridSection,
  StockStatusListSection,
  StylesCarouselSection,
} from '../components/CatalogSections';
import { StyleFilterBar } from '../components/StyleFilterBar';
import { DatabaseJacketCard } from '../components/JacketCards';
import { EmptyInventoryView } from '../components/EmptyInventoryView';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { AddJacketSheet } from '../components/modals/AddJacketSheet';
import { EditJacketSheet } from '../components/modals/EditJacketSheet';
import { JacketDetailModal } from '../components/modals/JacketDetailModal';
import { InventoryListSheet } from '../components/modals/InventoryListSheet';

interface HomeScreenProps {
  onOpenDrawer: () => void;
}

export const HomeScreen: React.FC<HomeScreenProps> = ({ onOpenDrawer }) => {
  const {
    allJackets,
    filteredJackets,
    activeFilter,
    searchQuery,
    selectedCategoryFilter,
    isIndonesian,
    isAddSheetOpen,
    isFilterSheetOpen,
    toggleLanguage,
    navigateToAddJacket,
    navigateToProduction,
    openAddSheet,
    closeAddSheet,
    openExportSheet,
    openImagePickerForJacket,
    openFilterByStyle,
    openFilterBySize,
    openFilterByStatus,
    openFilterByMaterial,
    openFilterByColor,
    openFilterSeasonal,
    openFilterByCategory,
    setSelectedCategoryFilter,
    closeFilterSheet,
    setSearchQuery,
    adjustQuantity,
    restock,
    deleteJacket,
    addNewJacket,
    updateJacketDetails,
    seedSampleStock,
  } = useJackets();

  const [homeSearch, setHomeSearch] = useState('');
  const [selectedStyleFilter, setSelectedStyleFilter] = useState('ALL');
  const [sortOrder, setSortOrder] = useState<JacketSortOrder>(JacketSortOrder.NAME_ASC);

  const [jacketToDelete, setJacketToDelete] = useState<JacketItem | null>(null);
  const [jacketToEdit, setJacketToEdit] = useState<JacketItem | null>(null);
  const [selectedDetailJacket, setSelectedDetailJacket] = useState<JacketItem | null>(null);

  const displayedHomeJackets = useMemo(() => {
    const q = homeSearch.trim().toLowerCase();
    const list = allJackets.filter((j) => {
      const catMatch =
        selectedCategoryFilter === 'ALL' ||
        j.genderCategory.toLowerCase() === selectedCategoryFilter.toLowerCase();
      const styleMatch =
        selectedStyleFilter === 'ALL' ||
        j.style.toLowerCase() === selectedStyleFilter.toLowerCase();
      const searchMatch =
        !q ||
        j.name.toLowerCase().includes(q) ||
        j.sku.toLowerCase().includes(q) ||
        j.locationRack.toLowerCase().includes(q) ||
        j.style.toLowerCase().includes(q);
      return catMatch && styleMatch && searchMatch;
    });

    const sorted = [...list];
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
  }, [allJackets, homeSearch, selectedCategoryFilter, selectedStyleFilter, sortOrder]);

  return (
    <div className="space-y-6 pb-24 lg:pb-10">
      <InventorySummaryCard
        jackets={allJackets}
        isIndonesian={isIndonesian}
        onToggleLanguage={toggleLanguage}
        onOpenDrawer={onOpenDrawer}
        onAddNewClick={navigateToAddJacket}
        onProductionClick={navigateToProduction}
        onExportClick={openExportSheet}
      />

      <HeroSplitSection isIndonesian={isIndonesian} onAddNewClick={navigateToAddJacket} />

      <DemographicCategoriesSection
        jackets={allJackets}
        isIndonesian={isIndonesian}
        onCategoryClick={(cat) => openFilterByCategory(cat.titleId)}
      />

      <StylesCarouselSection
        jackets={allJackets}
        isIndonesian={isIndonesian}
        onStyleClick={openFilterByStyle}
      />

      <SizesGridSection
        jackets={allJackets}
        isIndonesian={isIndonesian}
        onSizeClick={openFilterBySize}
      />

      <StockStatusListSection
        jackets={allJackets}
        isIndonesian={isIndonesian}
        onStatusClick={openFilterByStatus}
      />

      <SeasonalBannerSection
        jackets={allJackets}
        isIndonesian={isIndonesian}
        onClick={openFilterSeasonal}
      />

      <MaterialsGridSection
        jackets={allJackets}
        isIndonesian={isIndonesian}
        onMaterialClick={openFilterByMaterial}
      />

      <ColorsCarouselSection
        isIndonesian={isIndonesian}
        onColorClick={openFilterByColor}
      />

      {/* Database Inventory Section */}
      <section className="space-y-3">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <div>
            <h2 className="text-base font-bold text-slate-900">
              {isIndonesian ? 'Daftar Stok Gudang' : 'Warehouse Stock Directory'}
            </h2>
            <p className="text-xs text-slate-500">
              {isIndonesian
                ? 'Sesuaikan stok secara instan atau ketuk kartu untuk melihat rincian'
                : 'Adjust stock levels immediately or tap a card for full details'}
            </p>
          </div>
          <button
            type="button"
            onClick={openAddSheet}
            className="h-9 px-3.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-white text-xs font-bold inline-flex items-center gap-1.5 cursor-pointer"
          >
            <Plus className="w-4 h-4" />
            {isIndonesian ? 'Tambah Cepat' : 'Quick Add'}
          </button>
        </div>

        {/* Search */}
        <div className="relative">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={homeSearch}
            onChange={(e) => setHomeSearch(e.target.value)}
            placeholder={
              isIndonesian
                ? 'Cari nama jaket, SKU, atau lokasi rak...'
                : 'Search jacket name, SKU, or rack...'
            }
            className="w-full h-11 pl-10 pr-9 rounded-2xl bg-white border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-slate-800"
          />
          {homeSearch && (
            <button
              type="button"
              onClick={() => setHomeSearch('')}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 cursor-pointer"
            >
              <X className="w-4 h-4" />
            </button>
          )}
        </div>

        <StyleFilterBar
          selectedCategory={selectedCategoryFilter}
          onCategorySelected={setSelectedCategoryFilter}
          selectedStyle={selectedStyleFilter}
          onStyleSelected={setSelectedStyleFilter}
          sortOrder={sortOrder}
          onSortOrderSelected={setSortOrder}
          totalCount={displayedHomeJackets.length}
          isIndonesian={isIndonesian}
        />

        {displayedHomeJackets.length === 0 ? (
          <EmptyInventoryView
            isDatabaseEmpty={allJackets.length === 0}
            isIndonesian={isIndonesian}
            activeFilterName={
              selectedCategoryFilter !== 'ALL'
                ? selectedCategoryFilter
                : selectedStyleFilter !== 'ALL'
                ? selectedStyleFilter
                : homeSearch
            }
            onAddJacketClick={navigateToAddJacket}
            onLoadSampleStock={seedSampleStock}
            onResetFilter={() => {
              setSelectedCategoryFilter('ALL');
              setSelectedStyleFilter('ALL');
              setHomeSearch('');
            }}
          />
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            {displayedHomeJackets.map((jacket) => (
              <DatabaseJacketCard
                key={jacket.id}
                jacket={jacket}
                isIndonesian={isIndonesian}
                onAdjustQuantity={(delta) => adjustQuantity(jacket.id, delta)}
                onRestock={() => restock(jacket.id)}
                onEditImage={() => openImagePickerForJacket(jacket)}
                onEditDetails={() => setJacketToEdit(jacket)}
                onDelete={() => setJacketToDelete(jacket)}
                onClick={() => setSelectedDetailJacket(jacket)}
              />
            ))}
          </div>
        )}
      </section>

      {/* Modals */}
      <AddJacketSheet
        isOpen={isAddSheetOpen}
        isIndonesian={isIndonesian}
        existingJackets={allJackets}
        onDismiss={closeAddSheet}
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
        jacket={selectedDetailJacket}
        allJackets={allJackets}
        isIndonesian={isIndonesian}
        onDismiss={() => setSelectedDetailJacket(null)}
        onAdjustQuantity={adjustQuantity}
        onRestock={restock}
        onEditImage={openImagePickerForJacket}
        onDelete={(item) => setJacketToDelete(item)}
        onSelectVariant={(variant) => setSelectedDetailJacket(variant)}
      />

      <InventoryListSheet
        isOpen={isFilterSheetOpen}
        activeFilter={activeFilter}
        jackets={filteredJackets}
        allJacketsCount={allJackets.length}
        searchQuery={searchQuery}
        isIndonesian={isIndonesian}
        onSearchQueryChange={setSearchQuery}
        onDismiss={closeFilterSheet}
        onAdjustQuantity={adjustQuantity}
        onRestock={restock}
        onEditImage={openImagePickerForJacket}
        onDelete={(item) => setJacketToDelete(item)}
        onSelectJacket={(item) => setSelectedDetailJacket(item)}
        onAddNew={navigateToAddJacket}
        onLoadSample={seedSampleStock}
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
