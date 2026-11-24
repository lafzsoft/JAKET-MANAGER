import React from 'react';
import { ArrowUpDown } from 'lucide-react';
import { EXTRA_STYLE_OPTIONS, GENDER_CATEGORIES } from '../data/catalogData';
import { JacketSortOrder } from '../types/jacket';

interface StyleFilterBarProps {
  selectedCategory: string;
  onCategorySelected: (category: string) => void;
  selectedStyle: string;
  onStyleSelected: (style: string) => void;
  sortOrder: JacketSortOrder;
  onSortOrderSelected: (order: JacketSortOrder) => void;
  totalCount: number;
  isIndonesian: boolean;
}

export const StyleFilterBar: React.FC<StyleFilterBarProps> = ({
  selectedCategory,
  onCategorySelected,
  selectedStyle,
  onStyleSelected,
  sortOrder,
  onSortOrderSelected,
  totalCount,
  isIndonesian,
}) => {
  const sortLabels: Record<JacketSortOrder, string> = {
    [JacketSortOrder.NAME_ASC]: isIndonesian ? 'Nama (A-Z)' : 'Name (A-Z)',
    [JacketSortOrder.QTY_ASC]: isIndonesian ? 'Stok Terendah' : 'Lowest Stock',
    [JacketSortOrder.QTY_DESC]: isIndonesian ? 'Stok Tertinggi' : 'Highest Stock',
    [JacketSortOrder.STYLE]: isIndonesian ? 'Model Jaket' : 'By Style',
  };

  return (
    <div className="bg-white rounded-2xl border border-slate-200/80 p-3.5 shadow-xs space-y-3">
      {/* Row 1: Demographic Categories */}
      <div className="flex items-center gap-2 overflow-x-auto no-scrollbar pb-0.5">
        <button
          type="button"
          onClick={() => onCategorySelected('ALL')}
          className={`shrink-0 px-3 py-1.5 rounded-lg text-xs font-semibold transition cursor-pointer ${
            selectedCategory === 'ALL'
              ? 'bg-slate-900 text-white'
              : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
          }`}
        >
          {isIndonesian ? 'Semua Kategori' : 'All Categories'}
        </button>
        {GENDER_CATEGORIES.map((cat) => {
          const active = selectedCategory.toLowerCase() === cat.titleId.toLowerCase();
          return (
            <button
              key={cat.id}
              type="button"
              onClick={() => onCategorySelected(cat.titleId)}
              className={`shrink-0 px-3 py-1.5 rounded-lg text-xs font-semibold transition cursor-pointer ${
                active
                  ? 'bg-slate-900 text-white'
                  : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
              }`}
            >
              {isIndonesian ? cat.titleId : cat.titleEn}
            </button>
          );
        })}
      </div>

      {/* Row 2: Styles + Sort & Count */}
      <div className="flex flex-wrap items-center justify-between gap-2 pt-2 border-t border-slate-100">
        <div className="flex items-center gap-1.5 overflow-x-auto no-scrollbar max-w-full">
          <button
            type="button"
            onClick={() => onStyleSelected('ALL')}
            className={`shrink-0 px-2.5 py-1 rounded-md text-[11px] font-semibold transition cursor-pointer ${
              selectedStyle === 'ALL'
                ? 'bg-[#2D3748] text-white'
                : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
            }`}
          >
            {isIndonesian ? 'Semua Model' : 'All Styles'}
          </button>
          {EXTRA_STYLE_OPTIONS.map((style) => {
            const active = selectedStyle.toLowerCase() === style.toLowerCase();
            return (
              <button
                key={style}
                type="button"
                onClick={() => onStyleSelected(style)}
                className={`shrink-0 px-2.5 py-1 rounded-md text-[11px] font-semibold transition cursor-pointer ${
                  active
                    ? 'bg-[#2D3748] text-white'
                    : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                }`}
              >
                {style.replace(' Jacket', '').replace(' Coat', '')}
              </button>
            );
          })}
        </div>

        <div className="flex items-center gap-2 ml-auto">
          <span className="text-[11px] font-bold text-slate-500 tabular-nums">
            {totalCount} {isIndonesian ? 'item' : 'items'}
          </span>
          <div className="relative inline-flex items-center">
            <ArrowUpDown className="w-3.5 h-3.5 text-slate-500 absolute left-2.5 pointer-events-none" />
            <select
              value={sortOrder}
              onChange={(e) => onSortOrderSelected(e.target.value as JacketSortOrder)}
              aria-label="Sort inventory"
              className="pl-7 pr-3 py-1 rounded-lg bg-slate-100 border border-slate-200 text-xs font-semibold text-slate-700 focus:outline-none focus:ring-2 focus:ring-slate-400 cursor-pointer"
            >
              {(Object.keys(sortLabels) as JacketSortOrder[]).map((key) => (
                <option key={key} value={key}>
                  {sortLabels[key]}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>
    </div>
  );
};
