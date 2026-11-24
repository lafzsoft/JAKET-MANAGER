import React from 'react';
import { ChevronRight, Snowflake } from 'lucide-react';
import {
  COLORS,
  GENDER_CATEGORIES,
  MATERIALS,
  SEASONAL_BANNER,
  SIZES,
  STOCK_STATUSES,
  STYLES,
} from '../data/catalogData';
import {
  ColorCategory,
  DemographicCategory,
  JacketItem,
  MaterialCategory,
  SizeCategory,
  StockStatusCategory,
  StockStatusLevel,
  StyleCategory,
  getStockStatus,
} from '../types/jacket';
import { NetworkImageWithFallback } from './NetworkImageWithFallback';

interface SectionHeaderProps {
  title: string;
  subtitle?: string;
}

const SectionHeader: React.FC<SectionHeaderProps> = ({ title, subtitle }) => (
  <div className="mb-3">
    <h2 className="text-base font-bold text-slate-900">{title}</h2>
    {subtitle && <p className="text-xs text-slate-500 mt-0.5">{subtitle}</p>}
  </div>
);

export const DemographicCategoriesSection: React.FC<{
  jackets: JacketItem[];
  isIndonesian: boolean;
  onCategoryClick: (cat: DemographicCategory) => void;
}> = ({ jackets, isIndonesian, onCategoryClick }) => (
  <section>
    <SectionHeader
      title={isIndonesian ? 'Kategori Demografis' : 'Demographic Categories'}
      subtitle={
        isIndonesian
          ? 'Pilih lini produk berdasarkan segmen pengguna'
          : 'Browse outerwear lines by target audience'
      }
    />
    <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
      {GENDER_CATEGORIES.map((cat) => {
        const count = jackets
          .filter((j) => j.genderCategory.toLowerCase() === cat.titleId.toLowerCase())
          .reduce((s, j) => s + j.quantity, 0);
        return (
          <button
            key={cat.id}
            type="button"
            onClick={() => onCategoryClick(cat)}
            className="text-left bg-white rounded-2xl border border-slate-200/80 p-3 shadow-xs hover:border-slate-300 hover:shadow-sm transition flex items-center gap-3 cursor-pointer"
          >
            <div className="w-14 h-14 rounded-xl overflow-hidden shrink-0 bg-slate-100">
              <NetworkImageWithFallback
                src={cat.imageUrl}
                alt={isIndonesian ? cat.titleId : cat.titleEn}
              />
            </div>
            <div className="min-w-0 flex-1">
              <p className="text-sm font-bold text-slate-900 truncate">
                {isIndonesian ? cat.titleId : cat.titleEn}
              </p>
              <p className="text-[11px] text-slate-500 line-clamp-2 leading-tight mt-0.5">
                {isIndonesian ? cat.descId : cat.descEn}
              </p>
              <p className="text-[11px] font-bold text-slate-700 mt-1 tabular-nums">
                {count} {isIndonesian ? 'unit' : 'units'}
              </p>
            </div>
          </button>
        );
      })}
    </div>
  </section>
);

export const StylesCarouselSection: React.FC<{
  jackets: JacketItem[];
  isIndonesian: boolean;
  onStyleClick: (style: StyleCategory) => void;
}> = ({ jackets, isIndonesian, onStyleClick }) => (
  <section>
    <SectionHeader
      title={isIndonesian ? 'Model Jaket (Styles)' : 'Jacket Styles'}
      subtitle={
        isIndonesian
          ? 'Ketuk model untuk melihat varian & stok di gudang'
          : 'Tap a style to inspect matching inventory'
      }
    />
    <div className="flex gap-3 overflow-x-auto pb-2 no-scrollbar sm:grid sm:grid-cols-2 lg:grid-cols-4 sm:overflow-visible">
      {STYLES.map((style) => {
        const count = jackets
          .filter((j) => j.style.toLowerCase() === style.titleEn.toLowerCase())
          .reduce((s, j) => s + j.quantity, 0);
        return (
          <button
            key={style.id}
            type="button"
            onClick={() => onStyleClick(style)}
            className="min-w-[175px] sm:min-w-0 text-left bg-white rounded-2xl border border-slate-200/80 overflow-hidden shadow-xs hover:border-slate-300 hover:shadow-sm transition cursor-pointer group"
          >
            <div className="h-32 w-full bg-slate-100 overflow-hidden relative">
              <NetworkImageWithFallback
                src={style.imageUrl}
                alt={isIndonesian ? style.titleId : style.titleEn}
                className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
              />
              <span className="absolute top-2 right-2 px-2 py-0.5 rounded-full bg-slate-900/80 text-white text-[10px] font-bold tabular-nums">
                {count} pcs
              </span>
            </div>
            <div className="p-3">
              <p className="text-sm font-bold text-slate-900">
                {isIndonesian ? style.titleId : style.titleEn}
              </p>
              <p className="text-xs text-slate-500 mt-0.5 line-clamp-2">
                {isIndonesian ? style.descId : style.descEn}
              </p>
            </div>
          </button>
        );
      })}
    </div>
  </section>
);

export const SizesGridSection: React.FC<{
  jackets: JacketItem[];
  isIndonesian: boolean;
  onSizeClick: (size: SizeCategory) => void;
}> = ({ jackets, isIndonesian, onSizeClick }) => (
  <section>
    <SectionHeader
      title={isIndonesian ? 'Ukuran Tersedia (Sizes)' : 'Available Sizes'}
      subtitle={
        isIndonesian
          ? 'Panduan ukuran lingkar dada & jumlah unit per size'
          : 'Chest measurement guide & stock counts per size'
      }
    />
    <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-7 gap-2.5">
      {SIZES.map((size) => {
        const count = jackets
          .filter((j) => j.size.toLowerCase() === size.title.toLowerCase())
          .reduce((s, j) => s + j.quantity, 0);
        return (
          <button
            key={size.id}
            type="button"
            onClick={() => onSizeClick(size)}
            className="text-left bg-white rounded-xl border border-slate-200/80 p-3 hover:border-slate-400 hover:shadow-xs transition cursor-pointer flex flex-col justify-between"
          >
            <div className="flex items-center justify-between gap-2 mb-1.5">
              <span className="inline-flex items-center justify-center px-2.5 py-1 rounded-lg bg-slate-900 text-white text-xs font-extrabold">
                {size.title}
              </span>
              <span className="text-xs font-bold text-slate-700 tabular-nums">{count} pcs</span>
            </div>
            <p className="text-[11px] text-slate-500 leading-snug">
              {isIndonesian ? size.descId : size.descEn}
            </p>
          </button>
        );
      })}
    </div>
  </section>
);

export const StockStatusListSection: React.FC<{
  jackets: JacketItem[];
  isIndonesian: boolean;
  onStatusClick: (status: StockStatusCategory) => void;
}> = ({ jackets, isIndonesian, onStatusClick }) => (
  <section>
    <SectionHeader
      title={isIndonesian ? 'Status Ketersediaan Stok' : 'Stock Availability Status'}
    />
    <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
      {STOCK_STATUSES.map((item) => {
        const count = jackets.filter(
          (j) => getStockStatus(j.quantity, j.minThreshold) === item.status
        ).length;
        const badgeStyle =
          item.status === StockStatusLevel.IN_STOCK
            ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
            : item.status === StockStatusLevel.LOW_STOCK
            ? 'bg-amber-50 text-amber-700 border-amber-200'
            : 'bg-red-50 text-red-700 border-red-200';
        const dotStyle =
          item.status === StockStatusLevel.IN_STOCK
            ? 'bg-emerald-600'
            : item.status === StockStatusLevel.LOW_STOCK
            ? 'bg-amber-600'
            : 'bg-red-600';

        return (
          <button
            key={item.id}
            type="button"
            onClick={() => onStatusClick(item)}
            className="text-left bg-white rounded-2xl border border-slate-200/80 p-3.5 hover:border-slate-300 shadow-xs transition flex items-center justify-between gap-3 cursor-pointer"
          >
            <div className="flex items-center gap-3 min-w-0">
              <div className={`w-10 h-10 rounded-xl border flex items-center justify-center shrink-0 ${badgeStyle}`}>
                <span className={`w-3 h-3 rounded-full ${dotStyle}`} />
              </div>
              <div className="min-w-0">
                <p className="text-sm font-bold text-slate-900 truncate">
                  {isIndonesian ? item.titleId : item.titleEn}
                </p>
                <p className="text-xs text-slate-500 truncate">
                  {isIndonesian ? item.descId : item.descEn}
                </p>
              </div>
            </div>
            <div className="flex items-center gap-1.5 shrink-0">
              <span className={`px-2.5 py-1 rounded-lg text-xs font-extrabold tabular-nums border ${badgeStyle}`}>
                {count} SKU
              </span>
              <ChevronRight className="w-4 h-4 text-slate-400" />
            </div>
          </button>
        );
      })}
    </div>
  </section>
);

export const SeasonalBannerSection: React.FC<{
  jackets: JacketItem[];
  isIndonesian: boolean;
  onClick: () => void;
}> = ({ jackets, isIndonesian, onClick }) => {
  const seasonalCount = jackets.filter((j) => j.isSeasonal).length;
  return (
    <section>
      <button
        type="button"
        onClick={onClick}
        className="w-full text-left rounded-2xl overflow-hidden relative shadow-sm border border-slate-200 group cursor-pointer"
        style={{
          background: 'linear-gradient(120deg, #1E293B 0%, #334155 100%)',
        }}
      >
        <div className="grid grid-cols-1 md:grid-cols-12 items-center">
          <div className="md:col-span-8 p-5 text-white">
            <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-sky-500/20 text-sky-200 text-[11px] font-bold mb-2 border border-sky-400/30">
              <Snowflake className="w-3.5 h-3.5" />
              {seasonalCount} {isIndonesian ? 'SKU Musim Dingin' : 'Winter SKUs'}
            </span>
            <h3 className="text-lg font-extrabold tracking-tight">
              {isIndonesian ? SEASONAL_BANNER.titleId : SEASONAL_BANNER.titleEn}
            </h3>
            <p className="text-xs text-slate-300 mt-1">
              {isIndonesian ? SEASONAL_BANNER.subtitleId : SEASONAL_BANNER.subtitleEn}
            </p>
          </div>
          <div className="md:col-span-4 h-28 md:h-36 overflow-hidden">
            <NetworkImageWithFallback
              src={SEASONAL_BANNER.imageUrl}
              alt={isIndonesian ? SEASONAL_BANNER.titleId : SEASONAL_BANNER.titleEn}
              className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
            />
          </div>
        </div>
      </button>
    </section>
  );
};

export const MaterialsGridSection: React.FC<{
  jackets: JacketItem[];
  isIndonesian: boolean;
  onMaterialClick: (material: MaterialCategory) => void;
}> = ({ jackets, isIndonesian, onMaterialClick }) => (
  <section>
    <SectionHeader title={isIndonesian ? 'Bahan & Material' : 'Materials & Fabrics'} />
    <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3">
      {MATERIALS.map((mat) => {
        const count = jackets.filter(
          (j) => j.material.toLowerCase() === mat.titleEn.toLowerCase()
        ).length;
        return (
          <button
            key={mat.id}
            type="button"
            onClick={() => onMaterialClick(mat)}
            className="bg-white rounded-2xl border border-slate-200/80 p-3 text-center hover:border-slate-300 shadow-xs transition cursor-pointer flex flex-col items-center"
          >
            <div className="w-14 h-14 rounded-full overflow-hidden border border-slate-200 mb-2 bg-slate-100">
              <NetworkImageWithFallback
                src={mat.imageUrl}
                alt={isIndonesian ? mat.titleId : mat.titleEn}
              />
            </div>
            <p className="text-xs font-bold text-slate-900">
              {isIndonesian ? mat.titleId : mat.titleEn}
            </p>
            <p className="text-[11px] text-slate-500 mt-0.5 line-clamp-1">
              {count} {isIndonesian ? 'produk' : 'items'}
            </p>
          </button>
        );
      })}
    </div>
  </section>
);

export const ColorsCarouselSection: React.FC<{
  isIndonesian: boolean;
  onColorClick: (color: ColorCategory) => void;
}> = ({ isIndonesian, onColorClick }) => (
  <section>
    <SectionHeader title={isIndonesian ? 'Pilihan Warna (Colors)' : 'Color Palette'} />
    <div className="flex gap-2.5 overflow-x-auto pb-2 no-scrollbar flex-nowrap sm:flex-wrap sm:overflow-visible">
      {COLORS.map((color) => (
        <button
          key={color.id}
          type="button"
          onClick={() => onColorClick(color)}
          className="shrink-0 bg-white rounded-xl border border-slate-200/80 px-3 py-2 hover:border-slate-400 transition flex items-center gap-2 cursor-pointer"
        >
          <span
            className="w-4 h-4 rounded-full border border-slate-300 shrink-0"
            style={{ backgroundColor: color.hexColor }}
          />
          <span className="text-xs font-semibold text-slate-800 whitespace-nowrap">
            {isIndonesian ? color.titleId : color.titleEn}
          </span>
        </button>
      ))}
    </div>
  </section>
);
