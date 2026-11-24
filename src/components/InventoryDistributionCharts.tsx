import React, { useMemo, useState } from 'react';
import { BarChart3, X } from 'lucide-react';
import { JacketItem } from '../types/jacket';

enum ChartDistributionMode {
  BY_STYLE = 'BY_STYLE',
  BY_SIZE = 'BY_SIZE',
  BY_CATEGORY = 'BY_CATEGORY',
}

interface ChartBarData {
  label: string;
  value: number;
  color: string;
  secondaryLabel: string;
}

export const InventoryDistributionCharts: React.FC<{
  allJackets: JacketItem[];
  isIndonesian: boolean;
}> = ({ allJackets, isIndonesian }) => {
  const [selectedMode, setSelectedMode] = useState<ChartDistributionMode>(
    ChartDistributionMode.BY_STYLE
  );
  const [selectedBar, setSelectedBar] = useState<ChartBarData | null>(null);

  const totalStock = useMemo(
    () => allJackets.reduce((sum, j) => sum + j.quantity, 0),
    [allJackets]
  );

  const chartData = useMemo<ChartBarData[]>(() => {
    if (selectedMode === ChartDistributionMode.BY_STYLE) {
      const styles = [
        'Bomber Jacket',
        'Parka',
        'Denim Jacket',
        'Trench Coat',
        'Puffer Jacket',
      ];
      const colors = ['#2563EB', '#0D9488', '#6366F1', '#EA580C', '#8B5CF6'];
      return styles.map((styleName, idx) => {
        const qty = allJackets
          .filter((j) => j.style.toLowerCase() === styleName.toLowerCase())
          .reduce((s, j) => s + j.quantity, 0);
        const shortName = styleName.replace(' Jacket', '').replace(' Coat', '');
        return {
          label: shortName,
          value: qty,
          color: colors[idx % colors.length],
          secondaryLabel: styleName,
        };
      });
    }

    if (selectedMode === ChartDistributionMode.BY_SIZE) {
      const sizes = ['XS', 'S', 'M', 'L', 'XL', 'XXL', '3XL'];
      const colors = [
        '#38BDF8',
        '#0284C7',
        '#2563EB',
        '#1D4ED8',
        '#4F46E5',
        '#7C3AED',
        '#9333EA',
      ];
      return sizes.map((sizeCode, idx) => {
        const qty = allJackets
          .filter((j) => j.size.toLowerCase() === sizeCode.toLowerCase())
          .reduce((s, j) => s + j.quantity, 0);
        return {
          label: sizeCode,
          value: qty,
          color: colors[idx % colors.length],
          secondaryLabel: isIndonesian ? `Ukuran ${sizeCode}` : `Size ${sizeCode}`,
        };
      });
    }

    const categories = ['Jaket Pria', 'Jaket Wanita', 'Jaket Anak', 'Jaket Unisex'];
    const colors = ['#1E40AF', '#BE185D', '#047857', '#7C2D12'];
    return categories.map((catName, idx) => {
      const qty = allJackets
        .filter((j) => j.genderCategory.toLowerCase() === catName.toLowerCase())
        .reduce((s, j) => s + j.quantity, 0);
      const shortName =
        catName === 'Jaket Pria'
          ? isIndonesian
            ? 'Pria'
            : 'Men'
          : catName === 'Jaket Wanita'
          ? isIndonesian
            ? 'Wanita'
            : 'Women'
          : catName === 'Jaket Anak'
          ? isIndonesian
            ? 'Anak'
            : 'Kids'
          : 'Unisex';
      return {
        label: shortName,
        value: qty,
        color: colors[idx % colors.length],
        secondaryLabel: catName,
      };
    });
  }, [allJackets, selectedMode, isIndonesian]);

  const maxVal = useMemo(
    () => Math.max(1, ...chartData.map((d) => d.value)),
    [chartData]
  );

  const tabs: Array<{ mode: ChartDistributionMode; label: string }> = [
    { mode: ChartDistributionMode.BY_STYLE, label: isIndonesian ? 'Model' : 'Styles' },
    { mode: ChartDistributionMode.BY_SIZE, label: isIndonesian ? 'Ukuran' : 'Sizes' },
    { mode: ChartDistributionMode.BY_CATEGORY, label: isIndonesian ? 'Kategori' : 'Category' },
  ];

  return (
    <div className="bg-white rounded-[20px] border border-slate-200/80 shadow-xs p-5">
      <div className="flex items-center justify-between gap-3 mb-4">
        <div className="flex items-center gap-2.5">
          <div className="w-9 h-9 rounded-xl bg-blue-600/10 flex items-center justify-center text-blue-600">
            <BarChart3 className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-base font-bold text-slate-900">
              {isIndonesian ? 'Distribusi Stok Jaket' : 'Jacket Stock Distribution'}
            </h3>
            <p className="text-[11px] text-slate-500">
              {isIndonesian
                ? 'Visualisasi Diagram Batang Stok'
                : 'Bar Chart Inventory Analytics'}
            </p>
          </div>
        </div>

        <span className="px-2.5 py-1 rounded-xl bg-slate-100 text-xs font-bold text-slate-700 tabular-nums">
          {totalStock} {isIndonesian ? 'unit' : 'units'}
        </span>
      </div>

      {/* Mode Tabs */}
      <div className="grid grid-cols-3 gap-1 p-1 rounded-xl bg-slate-100 mb-4">
        {tabs.map((tab) => {
          const active = selectedMode === tab.mode;
          return (
            <button
              key={tab.mode}
              type="button"
              onClick={() => {
                setSelectedMode(tab.mode);
                setSelectedBar(null);
              }}
              className={`py-2 rounded-lg text-xs transition cursor-pointer ${
                active
                  ? 'bg-white text-slate-900 font-bold shadow-xs'
                  : 'text-slate-600 font-medium hover:text-slate-900'
              }`}
            >
              {tab.label}
            </button>
          );
        })}
      </div>

      {/* Bar Chart Area */}
      <div className="relative h-52 rounded-xl bg-slate-50 border border-slate-100 px-3 py-3 flex flex-col justify-between">
        <div className="absolute inset-x-3 top-6 bottom-8 flex flex-col justify-between pointer-events-none">
          {[0, 1, 2, 3].map((row) => (
            <div key={row} className="w-full h-px bg-slate-200/80" />
          ))}
        </div>

        <div className="relative z-10 flex-1 flex items-end justify-around gap-2 pt-4 pb-1">
          {chartData.map((bar) => {
            const pctHeight = Math.max(6, Math.round((bar.value / maxVal) * 100));
            const isSelected = selectedBar?.label === bar.label;
            return (
              <button
                key={bar.label}
                type="button"
                onClick={() => setSelectedBar(isSelected ? null : bar)}
                className="flex-1 h-full flex flex-col items-center justify-end group cursor-pointer"
              >
                <span
                  className={`text-[11px] tabular-nums mb-1 ${
                    isSelected ? 'font-extrabold text-slate-900' : 'font-semibold text-slate-500'
                  }`}
                >
                  {bar.value}
                </span>
                <div
                  className={`w-full max-w-[44px] rounded-t-md transition-all duration-500 ${
                    isSelected ? 'ring-2 ring-slate-900 ring-offset-1' : 'opacity-90 group-hover:opacity-100'
                  }`}
                  style={{
                    height: `${pctHeight}%`,
                    background: `linear-gradient(180deg, ${bar.color} 0%, ${bar.color}B3 100%)`,
                  }}
                />
              </button>
            );
          })}
        </div>

        <div className="relative z-10 flex items-center justify-around gap-2 pt-1 border-t border-slate-200/60">
          {chartData.map((bar) => {
            const isSelected = selectedBar?.label === bar.label;
            return (
              <span
                key={bar.label}
                className={`flex-1 text-center text-[11px] truncate ${
                  isSelected ? 'font-bold text-slate-900' : 'font-medium text-slate-500'
                }`}
              >
                {bar.label}
              </span>
            );
          })}
        </div>
      </div>

      {/* Selected Bar Breakdown Tooltip */}
      {selectedBar && (
        <div
          className="mt-3 rounded-xl bg-slate-50 p-3 border flex items-center justify-between gap-3"
          style={{ borderColor: `${selectedBar.color}55` }}
        >
          <div className="flex items-center gap-2.5">
            <span
              className="w-3 h-3 rounded-full shrink-0"
              style={{ backgroundColor: selectedBar.color }}
            />
            <div>
              <p className="text-xs font-bold text-slate-900">
                {selectedBar.secondaryLabel || selectedBar.label}
              </p>
              <p className="text-[11px] text-slate-600 tabular-nums">
                {isIndonesian
                  ? `${selectedBar.value} unit tersedia (${
                      totalStock > 0 ? ((selectedBar.value / totalStock) * 100).toFixed(1) : '0.0'
                    }% dari total gudang)`
                  : `${selectedBar.value} units in stock (${
                      totalStock > 0 ? ((selectedBar.value / totalStock) * 100).toFixed(1) : '0.0'
                    }% of total warehouse)`}
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={() => setSelectedBar(null)}
            aria-label="Close chart detail"
            className="text-slate-400 hover:text-slate-600 cursor-pointer"
          >
            <X className="w-4 h-4" />
          </button>
        </div>
      )}

      {/* Legend Chips */}
      <div className="flex flex-wrap gap-2 mt-3">
        {chartData.map((item) => {
          const isSelected = selectedBar?.label === item.label;
          return (
            <button
              key={item.label}
              type="button"
              onClick={() => setSelectedBar(isSelected ? null : item)}
              className={`px-2.5 py-1 rounded-lg text-[11px] font-semibold inline-flex items-center gap-1.5 transition cursor-pointer border ${
                isSelected
                  ? 'bg-slate-900 text-white border-slate-900'
                  : 'bg-slate-100 text-slate-700 border-transparent hover:bg-slate-200'
              }`}
            >
              <span
                className="w-2 h-2 rounded-full shrink-0"
                style={{ backgroundColor: item.color }}
              />
              <span className="tabular-nums">
                {item.label}: {item.value}
              </span>
            </button>
          );
        })}
      </div>
    </div>
  );
};
