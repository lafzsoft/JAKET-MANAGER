import React from 'react';
import {
  AlertTriangle,
  BarChart3,
  ClipboardList,
  Database,
  Download,
  Factory,
  Globe,
  Home,
  Package,
  PlusCircle,
  Snowflake,
  X,
} from 'lucide-react';
import { AppScreen, StockStatusLevel, getStockStatus } from '../types/jacket';
import { useJackets } from '../context/JacketContext';

interface AppNavigationDrawerProps {
  isOpen: boolean;
  onClose: () => void;
}

export const AppNavigationSidebarAndDrawer: React.FC<AppNavigationDrawerProps> = ({
  isOpen,
  onClose,
}) => {
  const {
    allJackets,
    currentScreen,
    isIndonesian,
    toggleLanguage,
    selectScreen,
    navigateToAddJacket,
    openExportSheet,
    openFilterSeasonal,
    navigateToReports,
  } = useJackets();

  const totalUnits = allJackets.reduce((s, j) => s + j.quantity, 0);
  const lowAlertCount = allJackets.filter(
    (j) => getStockStatus(j.quantity, j.minThreshold) !== StockStatusLevel.IN_STOCK
  ).length;

  const navItems = [
    {
      screen: AppScreen.HOME,
      label: isIndonesian ? 'Beranda' : 'Home',
      icon: Home,
      badge: null as string | null,
    },
    {
      screen: AppScreen.INVENTORY,
      label: isIndonesian ? 'Inventaris' : 'Inventory',
      icon: Package,
      badge: `${allJackets.length}`,
    },
    {
      screen: AppScreen.PRODUCTION,
      label: isIndonesian ? 'Produksi Harian' : 'Daily Production',
      icon: Factory,
      badge: null,
    },
    {
      screen: AppScreen.ADD_JACKET,
      label: isIndonesian ? 'Tambah Jaket Baru' : 'Add New Jacket',
      icon: PlusCircle,
      badge: null,
    },
    {
      screen: AppScreen.REPORTS,
      label: isIndonesian ? 'Laporan & Analitik' : 'Reports & Analytics',
      icon: BarChart3,
      badge: null,
    },
    {
      screen: AppScreen.JACKET_LIST,
      label: isIndonesian ? 'Daftar Status Stok' : 'Stock Status List',
      icon: ClipboardList,
      badge: lowAlertCount > 0 ? `${lowAlertCount} !` : null,
      badgeWarn: lowAlertCount > 0,
    },
  ];

  const handleSelect = (screen: AppScreen) => {
    if (screen === AppScreen.ADD_JACKET) {
      navigateToAddJacket();
    } else {
      selectScreen(screen);
    }
    onClose();
  };

  const renderDrawerContent = (isMobileOverlay: boolean) => (
    <div className="flex flex-col h-full bg-white text-slate-900">
      {/* Header */}
      <div
        className="p-5 text-white shrink-0"
        style={{
          background: 'linear-gradient(135deg, #2D3748 0%, #1E293B 60%, #0F172A 100%)',
        }}
      >
        <div className="flex items-center justify-between">
          <div>
            <p className="text-[11px] font-bold tracking-wider uppercase text-slate-300">
              JACKET MANAGER PRO
            </p>
            <h2 className="text-lg font-extrabold tracking-tight mt-0.5">
              {isIndonesian ? 'Kontrol Gudang' : 'Warehouse Control'}
            </h2>
          </div>
          {isMobileOverlay && (
            <button
              type="button"
              onClick={onClose}
              aria-label="Close menu"
              className="w-8 h-8 rounded-full bg-white/15 hover:bg-white/25 flex items-center justify-center cursor-pointer"
            >
              <X className="w-4 h-4" />
            </button>
          )}
        </div>
        <div className="mt-3 flex items-center gap-2 text-xs text-slate-300 tabular-nums">
          <span className="px-2 py-0.5 rounded-md bg-white/15 font-semibold text-white">
            {totalUnits} pcs
          </span>
          <span>•</span>
          <span>
            {allJackets.length} {isIndonesian ? 'SKU aktif' : 'active SKUs'}
          </span>
        </div>
      </div>

      {/* Main Nav Links */}
      <div className="flex-1 overflow-y-auto p-3 space-y-1">
        <p className="px-3 pt-1 pb-1.5 text-[10px] font-bold uppercase tracking-wider text-slate-400">
          {isIndonesian ? 'Navigasi Utama' : 'Main Navigation'}
        </p>

        {navItems.map((item) => {
          const Icon = item.icon;
          const active = currentScreen === item.screen;
          return (
            <button
              key={item.screen}
              type="button"
              onClick={() => handleSelect(item.screen)}
              className={`w-full flex items-center justify-between gap-3 px-3 py-2.5 rounded-xl text-sm font-semibold transition cursor-pointer ${
                active
                  ? 'bg-slate-900 text-white shadow-xs'
                  : 'text-slate-700 hover:bg-slate-100'
              }`}
            >
              <span className="flex items-center gap-3 min-w-0">
                <Icon className="w-4 h-4 shrink-0" />
                <span className="truncate">{item.label}</span>
              </span>
              {item.badge && (
                <span
                  className={`px-2 py-0.5 rounded-full text-[10px] font-extrabold tabular-nums ${
                    item.badgeWarn
                      ? 'bg-amber-500 text-white'
                      : active
                      ? 'bg-white/20 text-white'
                      : 'bg-slate-200 text-slate-700'
                  }`}
                >
                  {item.badge}
                </span>
              )}
            </button>
          );
        })}

        <div className="pt-4 mt-3 border-t border-slate-100 space-y-1">
          <p className="px-3 pb-1.5 text-[10px] font-bold uppercase tracking-wider text-slate-400">
            {isIndonesian ? 'Pintasan Cepat' : 'Quick Shortcuts'}
          </p>

          <button
            type="button"
            onClick={() => {
              openFilterSeasonal();
              onClose();
            }}
            className="w-full flex items-center gap-3 px-3 py-2 rounded-xl text-xs font-semibold text-slate-700 hover:bg-slate-100 transition cursor-pointer"
          >
            <Snowflake className="w-4 h-4 text-sky-600" />
            {isIndonesian ? 'Koleksi Musim Dingin' : 'Winter Seasonal Collection'}
          </button>

          <button
            type="button"
            onClick={() => {
              openExportSheet();
              onClose();
            }}
            className="w-full flex items-center gap-3 px-3 py-2 rounded-xl text-xs font-semibold text-slate-700 hover:bg-slate-100 transition cursor-pointer"
          >
            <Download className="w-4 h-4 text-emerald-600" />
            {isIndonesian ? 'Ekspor Data CSV' : 'Export CSV Backup'}
          </button>

          <button
            type="button"
            onClick={() => {
              navigateToReports();
              onClose();
            }}
            className="w-full flex items-center justify-between gap-3 px-3 py-2 rounded-xl text-xs font-semibold text-slate-700 hover:bg-slate-100 transition cursor-pointer"
          >
            <span className="flex items-center gap-3">
              <AlertTriangle className="w-4 h-4 text-amber-600" />
              {isIndonesian ? 'Peringatan Stok Menipis' : 'Low Stock Alerts'}
            </span>
            {lowAlertCount > 0 && (
              <span className="px-1.5 py-0.5 rounded bg-amber-100 text-amber-800 text-[10px] font-bold tabular-nums">
                {lowAlertCount}
              </span>
            )}
          </button>
        </div>
      </div>

      {/* Footer Language & Local Persistence Info */}
      <div className="p-3 border-t border-slate-100 space-y-2 shrink-0">
        <button
          type="button"
          onClick={toggleLanguage}
          className="w-full flex items-center justify-between px-3 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-xs font-bold text-slate-800 transition cursor-pointer"
        >
          <span className="flex items-center gap-2">
            <Globe className="w-4 h-4 text-slate-600" />
            {isIndonesian ? 'Bahasa: Indonesia (ID)' : 'Language: English (EN)'}
          </span>
          <span className="px-2 py-0.5 rounded bg-slate-900 text-white text-[10px]">
            {isIndonesian ? 'ID → EN' : 'EN → ID'}
          </span>
        </button>

        <div className="px-3 py-1.5 flex items-center gap-2 text-[11px] text-slate-500">
          <Database className="w-3.5 h-3.5 text-emerald-600 shrink-0" />
          <span>
            {isIndonesian
              ? 'Penyimpanan Browser Lokal Aktif'
              : 'Local Browser Persistence Active'}
          </span>
        </div>
      </div>
    </div>
  );

  return (
    <>
      {/* Persistent Desktop Sidebar */}
      <aside className="hidden lg:block w-64 shrink-0 border-r border-slate-200 bg-white sticky top-0 h-screen">
        {renderDrawerContent(false)}
      </aside>

      {/* Slide-over Drawer (Mobile / Tablet / Triggered) */}
      {isOpen && (
        <div className="fixed inset-0 z-50 flex">
          <div
            className="fixed inset-0 bg-slate-900/50 backdrop-blur-[1px]"
            onClick={onClose}
          />
          <div className="relative z-10 w-72 max-w-[85vw] h-full shadow-2xl">
            {renderDrawerContent(true)}
          </div>
        </div>
      )}
    </>
  );
};

export const AppBottomNavigationBar: React.FC = () => {
  const { currentScreen, isIndonesian, selectScreen, navigateToAddJacket } = useJackets();

  const items = [
    {
      screen: AppScreen.HOME,
      label: isIndonesian ? 'Beranda' : 'Home',
      icon: Home,
    },
    {
      screen: AppScreen.INVENTORY,
      label: isIndonesian ? 'Inventaris' : 'Inventory',
      icon: Package,
    },
    {
      screen: AppScreen.PRODUCTION,
      label: isIndonesian ? 'Produksi' : 'Produce',
      icon: Factory,
    },
    {
      screen: AppScreen.ADD_JACKET,
      label: isIndonesian ? 'Tambah' : 'Add New',
      icon: PlusCircle,
    },
    {
      screen: AppScreen.REPORTS,
      label: isIndonesian ? 'Laporan' : 'Reports',
      icon: BarChart3,
    },
  ];

  return (
    <nav
      aria-label="Primary bottom navigation"
      className="lg:hidden fixed bottom-0 inset-x-0 z-30 bg-white/95 backdrop-blur border-t border-slate-200 px-2 py-1.5"
    >
      <div className="max-w-xl mx-auto grid grid-cols-5 gap-1">
        {items.map((item) => {
          const Icon = item.icon;
          const active = currentScreen === item.screen;
          return (
            <button
              key={item.screen}
              type="button"
              onClick={() => {
                if (item.screen === AppScreen.ADD_JACKET) {
                  navigateToAddJacket();
                } else {
                  selectScreen(item.screen);
                }
              }}
              className={`flex flex-col items-center justify-center py-1.5 rounded-xl transition cursor-pointer ${
                active
                  ? 'text-slate-900 bg-slate-100 font-bold'
                  : 'text-slate-500 hover:text-slate-800 font-medium'
              }`}
            >
              <Icon className={`w-5 h-5 ${active ? 'stroke-[2.4]' : ''}`} />
              <span className="text-[10px] mt-0.5 truncate max-w-full">{item.label}</span>
            </button>
          );
        })}
      </div>
    </nav>
  );
};
