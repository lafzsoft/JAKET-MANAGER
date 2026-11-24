import React, { useState } from 'react';
import { RotateCcw, X } from 'lucide-react';
import { JacketProvider, useJackets } from './context/JacketContext';
import { AppScreen } from './types/jacket';
import {
  AppBottomNavigationBar,
  AppNavigationSidebarAndDrawer,
} from './components/AppNavigation';
import { ExportCsvBottomSheet } from './components/modals/ExportCsvBottomSheet';
import { SkuImagePickerModal } from './components/modals/SkuImagePickerModal';
import { HomeScreen } from './screens/HomeScreen';
import { InventoryScreen } from './screens/InventoryScreen';
import { DailyProductionScreen } from './screens/DailyProductionScreen';
import { AddJacketScreen } from './screens/AddJacketScreen';
import { ReportsScreen } from './screens/ReportsScreen';
import { JacketListScreen } from './screens/JacketListScreen';

const JacketManagerShell: React.FC = () => {
  const {
    allJackets,
    filteredJackets,
    currentScreen,
    isIndonesian,
    isExportSheetOpen,
    selectedJacketForImage,
    toast,
    dismissToast,
    showToast,
    closeExportSheet,
    closeImagePicker,
    updateJacketImage,
    updateJacketImageBySku,
  } = useJackets();

  const [isDrawerOpen, setIsDrawerOpen] = useState(false);

  const renderScreen = () => {
    switch (currentScreen) {
      case AppScreen.HOME:
        return <HomeScreen onOpenDrawer={() => setIsDrawerOpen(true)} />;
      case AppScreen.INVENTORY:
        return <InventoryScreen onOpenDrawer={() => setIsDrawerOpen(true)} />;
      case AppScreen.PRODUCTION:
        return <DailyProductionScreen onOpenDrawer={() => setIsDrawerOpen(true)} />;
      case AppScreen.ADD_JACKET:
        return <AddJacketScreen />;
      case AppScreen.REPORTS:
        return <ReportsScreen onOpenDrawer={() => setIsDrawerOpen(true)} />;
      case AppScreen.JACKET_LIST:
        return <JacketListScreen />;
    }
  };

  return (
    <div className="min-h-screen bg-[#F5F5F7] text-[#1A1A1A] flex">
      <AppNavigationSidebarAndDrawer
        isOpen={isDrawerOpen}
        onClose={() => setIsDrawerOpen(false)}
      />

      <main className="flex-1 min-w-0">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 pt-4 sm:pt-6">
          {renderScreen()}
        </div>
      </main>

      {currentScreen !== AppScreen.ADD_JACKET && <AppBottomNavigationBar />}

      {/* Global Export CSV Sheet */}
      <ExportCsvBottomSheet
        isOpen={isExportSheetOpen}
        allJackets={allJackets}
        currentFilteredJackets={filteredJackets}
        isIndonesian={isIndonesian}
        onDismiss={closeExportSheet}
        onNotify={(msg) => showToast(msg)}
      />

      {/* Global SKU Image Picker Modal */}
      <SkuImagePickerModal
        jacket={selectedJacketForImage}
        isIndonesian={isIndonesian}
        onDismiss={closeImagePicker}
        onSave={(newUrl, applyAll) => {
          if (!selectedJacketForImage) return;
          if (applyAll && selectedJacketForImage.sku.trim()) {
            updateJacketImageBySku(selectedJacketForImage.sku, newUrl);
          } else {
            updateJacketImage(selectedJacketForImage.id, newUrl);
          }
        }}
      />

      {/* Global Toast / Undo Snackbar */}
      {toast && (
        <div className="fixed bottom-20 lg:bottom-6 right-4 left-4 sm:left-auto sm:min-w-[320px] z-50 flex justify-end pointer-events-none">
          <div className="pointer-events-auto bg-slate-900 text-white rounded-2xl shadow-xl border border-slate-700 px-4 py-3 flex items-center justify-between gap-3">
            <span className="text-xs font-semibold">{toast.message}</span>
            <div className="flex items-center gap-2 shrink-0">
              {toast.onUndo && toast.undoLabel && (
                <button
                  type="button"
                  onClick={() => {
                    toast.onUndo?.();
                    dismissToast();
                  }}
                  className="px-2.5 py-1 rounded-lg bg-amber-400 hover:bg-amber-300 text-slate-950 text-xs font-extrabold inline-flex items-center gap-1 cursor-pointer"
                >
                  <RotateCcw className="w-3 h-3" />
                  {toast.undoLabel}
                </button>
              )}
              <button
                type="button"
                onClick={dismissToast}
                aria-label="Dismiss notification"
                className="text-slate-400 hover:text-white cursor-pointer"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export function App() {
  return (
    <JacketProvider>
      <JacketManagerShell />
    </JacketProvider>
  );
}

export default App;
