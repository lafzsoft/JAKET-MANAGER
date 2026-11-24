import React from 'react';
import {
  ImagePlus,
  MapPin,
  Minus,
  Pencil,
  Plus,
  RefreshCw,
  Snowflake,
  Trash2,
} from 'lucide-react';
import { JacketItem, StockStatusLevel, getStockStatus } from '../types/jacket';
import { NetworkImageWithFallback } from './NetworkImageWithFallback';

export const StockStatusBadge: React.FC<{
  status: StockStatusLevel;
  isIndonesian: boolean;
  compact?: boolean;
}> = ({ status, isIndonesian, compact = false }) => {
  const config =
    status === StockStatusLevel.IN_STOCK
      ? {
          bg: 'bg-emerald-50 border-emerald-200 text-emerald-700',
          dot: 'bg-emerald-600',
          label: isIndonesian ? 'Tersedia' : 'In Stock',
        }
      : status === StockStatusLevel.LOW_STOCK
      ? {
          bg: 'bg-amber-50 border-amber-200 text-amber-700',
          dot: 'bg-amber-600',
          label: isIndonesian ? 'Menipis' : 'Low Stock',
        }
      : {
          bg: 'bg-red-50 border-red-200 text-red-700',
          dot: 'bg-red-600',
          label: isIndonesian ? 'Habis' : 'Out Stock',
        };

  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full border font-bold ${
        compact ? 'px-2 py-0.5 text-[10px]' : 'px-2.5 py-0.5 text-[11px]'
      } ${config.bg}`}
    >
      <span className={`w-1.5 h-1.5 rounded-full ${config.dot}`} />
      {config.label}
    </span>
  );
};

interface JacketCardActionsProps {
  jacket: JacketItem;
  isIndonesian: boolean;
  onAdjustQuantity: (delta: number) => void;
  onRestock: () => void;
  onEditImage: () => void;
  onEditDetails?: () => void;
  onDelete: () => void;
  onClick: () => void;
}

export const JacketGridCard: React.FC<JacketCardActionsProps> = ({
  jacket,
  isIndonesian,
  onAdjustQuantity,
  onRestock,
  onEditImage,
  onEditDetails,
  onDelete,
  onClick,
}) => {
  const status = getStockStatus(jacket.quantity, jacket.minThreshold);

  return (
    <div className="bg-white rounded-2xl border border-slate-200/80 shadow-xs hover:shadow-sm transition overflow-hidden flex flex-col">
      <div
        className="relative h-40 bg-slate-100 cursor-pointer group"
        onClick={onClick}
      >
        <NetworkImageWithFallback
          src={jacket.imageUrl}
          alt={jacket.name}
          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
        />
        <div className="absolute top-2.5 left-2.5">
          <StockStatusBadge status={status} isIndonesian={isIndonesian} compact />
        </div>
        {jacket.isSeasonal && (
          <span
            title={isIndonesian ? 'Koleksi Musiman' : 'Seasonal Collection'}
            className="absolute top-2.5 right-2.5 w-7 h-7 rounded-full bg-slate-900/75 text-sky-300 flex items-center justify-center"
          >
            <Snowflake className="w-3.5 h-3.5" />
          </span>
        )}
        <button
          type="button"
          onClick={(e) => {
            e.stopPropagation();
            onEditImage();
          }}
          title={isIndonesian ? 'Ubah Foto SKU' : 'Change SKU Photo'}
          className="absolute bottom-2.5 right-2.5 w-8 h-8 rounded-full bg-slate-900/80 hover:bg-slate-900 text-white flex items-center justify-center transition cursor-pointer"
        >
          <ImagePlus className="w-4 h-4" />
        </button>
      </div>

      <div className="p-3.5 flex-1 flex flex-col justify-between gap-2.5">
        <div className="cursor-pointer" onClick={onClick}>
          <div className="flex items-center justify-between gap-1 mb-1">
            <span className="font-mono-sku text-[11px] font-bold text-slate-500 truncate">
              {jacket.sku}
            </span>
            <span className="text-xs font-extrabold text-slate-900 tabular-nums">
              ${jacket.unitPrice.toFixed(2)}
            </span>
          </div>
          <h3 className="text-sm font-bold text-slate-900 line-clamp-1">{jacket.name}</h3>
          <p className="text-[11px] text-slate-500 mt-0.5 truncate">
            {jacket.style} • {jacket.size} • {jacket.color}
          </p>
          <p className="text-[11px] text-slate-500 mt-0.5 inline-flex items-center gap-1">
            <MapPin className="w-3 h-3 text-slate-400" />
            {jacket.locationRack}
          </p>
        </div>

        <div className="pt-2.5 border-t border-slate-100 space-y-2">
          <div className="flex items-center justify-between gap-2">
            <div className="flex items-center gap-1.5">
              <button
                type="button"
                onClick={() => onAdjustQuantity(-1)}
                aria-label="Decrease quantity"
                className="w-7 h-7 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-800 flex items-center justify-center transition cursor-pointer"
              >
                <Minus className="w-3.5 h-3.5" />
              </button>
              <span className="min-w-[28px] text-center text-sm font-extrabold text-slate-900 tabular-nums">
                {jacket.quantity}
              </span>
              <button
                type="button"
                onClick={() => onAdjustQuantity(1)}
                aria-label="Increase quantity"
                className="w-7 h-7 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-800 flex items-center justify-center transition cursor-pointer"
              >
                <Plus className="w-3.5 h-3.5" />
              </button>
            </div>

            <div className="flex items-center gap-1">
              {onEditDetails && (
                <button
                  type="button"
                  onClick={onEditDetails}
                  title={isIndonesian ? 'Edit Detail' : 'Edit Details'}
                  className="w-7 h-7 rounded-lg hover:bg-slate-100 text-slate-600 flex items-center justify-center transition cursor-pointer"
                >
                  <Pencil className="w-3.5 h-3.5" />
                </button>
              )}
              <button
                type="button"
                onClick={onDelete}
                title={isIndonesian ? 'Hapus' : 'Delete'}
                className="w-7 h-7 rounded-lg hover:bg-red-50 text-red-500 flex items-center justify-center transition cursor-pointer"
              >
                <Trash2 className="w-3.5 h-3.5" />
              </button>
            </div>
          </div>

          {status !== StockStatusLevel.IN_STOCK && (
            <button
              type="button"
              onClick={onRestock}
              className="w-full h-8 rounded-lg bg-[#2D3748] hover:bg-slate-800 text-white text-xs font-bold inline-flex items-center justify-center gap-1.5 transition cursor-pointer"
            >
              <RefreshCw className="w-3 h-3" />
              +15 Restock
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export const DatabaseJacketCard: React.FC<JacketCardActionsProps> = ({
  jacket,
  isIndonesian,
  onAdjustQuantity,
  onRestock,
  onEditImage,
  onEditDetails,
  onDelete,
  onClick,
}) => {
  const status = getStockStatus(jacket.quantity, jacket.minThreshold);

  return (
    <div className="bg-white rounded-2xl border border-slate-200/80 shadow-xs p-3.5 hover:border-slate-300 transition">
      <div className="flex items-start gap-3">
        <div
          className="relative w-20 h-20 rounded-xl overflow-hidden bg-slate-100 shrink-0 cursor-pointer"
          onClick={onEditImage}
          title={isIndonesian ? 'Ubah Foto SKU' : 'Change SKU Photo'}
        >
          <NetworkImageWithFallback src={jacket.imageUrl} alt={jacket.name} />
          <span className="absolute bottom-1 right-1 w-6 h-6 rounded-full bg-slate-900/80 text-white flex items-center justify-center">
            <ImagePlus className="w-3 h-3" />
          </span>
        </div>

        <div className="flex-1 min-w-0 cursor-pointer" onClick={onClick}>
          <div className="flex items-center justify-between gap-2 flex-wrap">
            <span className="font-mono-sku text-[11px] font-bold text-slate-500">
              {jacket.sku}
            </span>
            <StockStatusBadge status={status} isIndonesian={isIndonesian} compact />
          </div>

          <h3 className="text-sm font-bold text-slate-900 truncate mt-0.5">{jacket.name}</h3>
          <p className="text-xs text-slate-500 truncate mt-0.5">
            {jacket.genderCategory} • {jacket.style} • {jacket.size} • {jacket.material}
          </p>
          <div className="flex items-center gap-3 mt-1 text-[11px] text-slate-500">
            <span className="inline-flex items-center gap-1">
              <MapPin className="w-3 h-3 text-slate-400" />
              {jacket.locationRack}
            </span>
            <span className="font-bold text-slate-700 tabular-nums">
              ${jacket.unitPrice.toFixed(2)}
            </span>
          </div>
        </div>

        <div className="flex flex-col gap-1 shrink-0">
          {onEditDetails && (
            <button
              type="button"
              onClick={onEditDetails}
              aria-label="Edit jacket"
              className="w-8 h-8 rounded-lg hover:bg-slate-100 text-slate-600 flex items-center justify-center transition cursor-pointer"
            >
              <Pencil className="w-4 h-4" />
            </button>
          )}
          <button
            type="button"
            onClick={onDelete}
            aria-label="Delete jacket"
            className="w-8 h-8 rounded-lg hover:bg-red-50 text-red-500 flex items-center justify-center transition cursor-pointer"
          >
            <Trash2 className="w-4 h-4" />
          </button>
        </div>
      </div>

      <div className="mt-3 pt-2.5 border-t border-slate-100 flex items-center justify-between gap-3 flex-wrap">
        <div className="flex items-center gap-2">
          <span className="text-xs font-medium text-slate-500">
            {isIndonesian ? 'Stok:' : 'Stock:'}
          </span>
          <button
            type="button"
            onClick={() => onAdjustQuantity(-1)}
            aria-label="Decrease quantity"
            className="w-7 h-7 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-800 flex items-center justify-center transition cursor-pointer"
          >
            <Minus className="w-3.5 h-3.5" />
          </button>
          <span className="min-w-[30px] text-center text-sm font-extrabold text-slate-900 tabular-nums">
            {jacket.quantity}
          </span>
          <button
            type="button"
            onClick={() => onAdjustQuantity(1)}
            aria-label="Increase quantity"
            className="w-7 h-7 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-800 flex items-center justify-center transition cursor-pointer"
          >
            <Plus className="w-3.5 h-3.5" />
          </button>
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={onEditImage}
            className="h-8 px-2.5 rounded-lg border border-slate-200 hover:bg-slate-50 text-blue-600 text-xs font-semibold inline-flex items-center gap-1 transition cursor-pointer"
          >
            <ImagePlus className="w-3.5 h-3.5" />
            {isIndonesian ? 'Foto' : 'Photo'}
          </button>

          {status !== StockStatusLevel.IN_STOCK && (
            <button
              type="button"
              onClick={onRestock}
              className="h-8 px-3 rounded-lg bg-[#2D3748] hover:bg-slate-800 text-white text-xs font-bold inline-flex items-center gap-1 transition cursor-pointer"
            >
              <RefreshCw className="w-3 h-3" />
              +15 Restock
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
