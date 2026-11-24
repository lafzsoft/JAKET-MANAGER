import React from 'react';
import { Search, X } from 'lucide-react';
import { ActiveFilter, JacketItem } from '../../types/jacket';
import { DatabaseJacketCard } from '../JacketCards';
import { EmptyInventoryView } from '../EmptyInventoryView';

interface InventoryListSheetProps {
  isOpen: boolean;
  activeFilter: ActiveFilter;
  jackets: JacketItem[];
  allJacketsCount: number;
  searchQuery: string;
  isIndonesian: boolean;
  onSearchQueryChange: (q: string) => void;
  onDismiss: () => void;
  onAdjustQuantity: (id: string, delta: number) => void;
  onRestock: (id: string) => void;
  onEditImage: (jacket: JacketItem) => void;
  onDelete: (jacket: JacketItem) => void;
  onSelectJacket: (jacket: JacketItem) => void;
  onAddNew: () => void;
  onLoadSample: () => void;
}

export const InventoryListSheet: React.FC<InventoryListSheetProps> = ({
  isOpen,
  activeFilter,
  jackets,
  allJacketsCount,
  searchQuery,
  isIndonesian,
  onSearchQueryChange,
  onDismiss,
  onAdjustQuantity,
  onRestock,
  onEditImage,
  onDelete,
  onSelectJacket,
  onAddNew,
  onLoadSample,
}) => {
  if (!isOpen) return null;

  const filterTitle =
    activeFilter.type === 'NONE'
      ? isIndonesian
        ? 'Semua Inventaris'
        : 'All Inventory'
      : activeFilter.title;

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-slate-900/55 backdrop-blur-[1px] sm:p-4">
      <div className="bg-[#F5F5F7] w-full sm:max-w-2xl rounded-t-3xl sm:rounded-2xl shadow-2xl max-h-[88vh] flex flex-col overflow-hidden">
        <div className="bg-white px-5 py-4 border-b border-slate-200 flex items-center justify-between">
          <div>
            <h3 className="text-lg font-extrabold text-slate-900">{filterTitle}</h3>
            <p className="text-xs text-slate-500 tabular-nums">
              {jackets.length} {isIndonesian ? 'produk ditemukan' : 'products matched'}
            </p>
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

        <div className="bg-white px-5 py-3 border-b border-slate-100">
          <div className="relative">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => onSearchQueryChange(e.target.value)}
              placeholder={
                isIndonesian
                  ? 'Cari nama jaket, SKU, atau rak...'
                  : 'Search jacket name, SKU, or rack...'
              }
              className="w-full h-10 pl-9 pr-8 rounded-xl bg-slate-100 text-sm focus:outline-none focus:ring-2 focus:ring-slate-800"
            />
            {searchQuery && (
              <button
                type="button"
                onClick={() => onSearchQueryChange('')}
                className="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 cursor-pointer"
              >
                <X className="w-4 h-4" />
              </button>
            )}
          </div>
        </div>

        <div className="p-4 overflow-y-auto space-y-3 flex-1">
          {jackets.length === 0 ? (
            <EmptyInventoryView
              isDatabaseEmpty={allJacketsCount === 0}
              isIndonesian={isIndonesian}
              activeFilterName={filterTitle}
              onAddJacketClick={() => {
                onDismiss();
                onAddNew();
              }}
              onLoadSampleStock={onLoadSample}
              onResetFilter={() => {
                onSearchQueryChange('');
                onDismiss();
              }}
            />
          ) : (
            jackets.map((jacket) => (
              <DatabaseJacketCard
                key={jacket.id}
                jacket={jacket}
                isIndonesian={isIndonesian}
                onAdjustQuantity={(delta) => onAdjustQuantity(jacket.id, delta)}
                onRestock={() => onRestock(jacket.id)}
                onEditImage={() => onEditImage(jacket)}
                onDelete={() => onDelete(jacket)}
                onClick={() => onSelectJacket(jacket)}
              />
            ))
          )}
        </div>
      </div>
    </div>
  );
};
