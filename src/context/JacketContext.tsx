import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import {
  ActiveFilter,
  AppScreen,
  ColorCategory,
  DailyProductionItem,
  DailyProductionSummary,
  JacketItem,
  MaterialCategory,
  ProductionShift,
  ProductionStatus,
  SizeCategory,
  StockStatusCategory,
  StockStatusLevel,
  StyleCategory,
  ToastMessage,
  getNetAcceptedQuantity,
  getStockStatus,
} from '../types/jacket';
import {
  COLORS,
  HERO_IMAGE_URL,
  SEASONAL_BANNER,
  STYLES,
  getInitialProductions,
  getInitialWarehouseInventory,
  getTodayDateString,
} from '../data/catalogData';

const STORAGE_KEYS = {
  JACKETS: 'jacket_manager_jackets_v1',
  PRODUCTIONS: 'jacket_manager_productions_v1',
  DAILY_TARGET: 'jacket_manager_target_v1',
  IS_INDONESIAN: 'jacket_manager_lang_v1',
};

function loadInitialJackets(): JacketItem[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEYS.JACKETS);
    if (raw !== null) {
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed)) return parsed;
    }
  } catch {
    // Fallback to initial seed
  }
  return getInitialWarehouseInventory();
}

function loadInitialProductions(): DailyProductionItem[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEYS.PRODUCTIONS);
    if (raw !== null) {
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed)) return parsed;
    }
  } catch {
    // Fallback to initial seed
  }
  return getInitialProductions();
}

function loadInitialTarget(): number {
  try {
    const raw = localStorage.getItem(STORAGE_KEYS.DAILY_TARGET);
    if (raw !== null) {
      const n = Number(raw);
      if (!Number.isNaN(n) && n > 0) return n;
    }
  } catch {
    // Default
  }
  return 60;
}

function loadInitialLanguage(): boolean {
  try {
    const raw = localStorage.getItem(STORAGE_KEYS.IS_INDONESIAN);
    if (raw !== null) return raw === 'true';
  } catch {
    // Default Indonesian
  }
  return true;
}

interface AddJacketInput {
  sku: string;
  name: string;
  style: string;
  size: string;
  material: string;
  color: string;
  quantity: number;
  locationRack: string;
  isSeasonal?: boolean;
  genderCategory?: string;
  imageUrl?: string;
  unitPrice?: number;
}

interface UpdateJacketInput {
  id: string;
  sku: string;
  name: string;
  style: string;
  size: string;
  material: string;
  color: string;
  quantity: number;
  locationRack: string;
  isSeasonal?: boolean;
  genderCategory?: string;
  unitPrice?: number;
}

interface AddProductionInput {
  jacketId: string;
  jacketName: string;
  jacketSku: string;
  batchNumber: string;
  shift: ProductionShift;
  operatorName: string;
  quantityProduced: number;
  defectCount: number;
  status: ProductionStatus;
  notes: string;
  addedToStock: boolean;
}

interface JacketContextValue {
  allJackets: JacketItem[];
  allProductions: DailyProductionItem[];
  filteredProductions: DailyProductionItem[];
  filteredJackets: JacketItem[];
  dailyTarget: number;
  dailySummary: DailyProductionSummary;
  productionShiftFilter: string;
  currentScreen: AppScreen;
  activeFilter: ActiveFilter;
  searchQuery: string;
  selectedCategoryFilter: string;
  isIndonesian: boolean;
  isAddSheetOpen: boolean;
  isFilterSheetOpen: boolean;
  isExportSheetOpen: boolean;
  isAddProductionSheetOpen: boolean;
  selectedJacketForImage: JacketItem | null;
  prefilledSkuForAdd: string | null;
  toast: ToastMessage | null;
  dismissToast: () => void;
  showToast: (message: string, undoLabel?: string, onUndo?: () => void) => void;
  toggleLanguage: () => void;
  selectScreen: (screen: AppScreen) => void;
  navigateToHome: () => void;
  navigateToInventory: () => void;
  navigateToReports: () => void;
  navigateToJacketList: () => void;
  navigateToProduction: () => void;
  navigateToAddJacket: () => void;
  navigateToAddJacketWithSku: (sku: string) => void;
  clearPrefilledSku: () => void;
  openAddSheet: () => void;
  closeAddSheet: () => void;
  openExportSheet: () => void;
  closeExportSheet: () => void;
  openAddProductionSheet: () => void;
  closeAddProductionSheet: () => void;
  setProductionShiftFilter: (shift: string) => void;
  updateDailyTarget: (target: number) => void;
  openImagePickerForJacket: (jacket: JacketItem) => void;
  closeImagePicker: () => void;
  openFilterByStyle: (style: StyleCategory) => void;
  openFilterBySize: (size: SizeCategory) => void;
  openFilterByStatus: (status: StockStatusCategory) => void;
  openFilterByMaterial: (material: MaterialCategory) => void;
  openFilterByColor: (color: ColorCategory) => void;
  openFilterSeasonal: () => void;
  openFilterByCategory: (category: string) => void;
  setSelectedCategoryFilter: (category: string) => void;
  closeFilterSheet: () => void;
  setSearchQuery: (query: string) => void;
  adjustQuantity: (id: string, delta: number) => void;
  restock: (id: string) => void;
  restockAllLowStock: () => void;
  deleteJacket: (id: string) => void;
  isSkuDuplicate: (sku: string, excludeId?: string | null) => boolean;
  findJacketBySku: (sku: string) => JacketItem | undefined;
  generateAutoSku: (style: string, name: string, size: string) => string;
  generateRandomCustomSku: (prefix?: string) => string;
  addNewJacket: (input: AddJacketInput) => void;
  updateJacketDetails: (input: UpdateJacketInput) => void;
  updateJacketImage: (id: string, imageUrl: string) => void;
  updateJacketImageBySku: (sku: string, imageUrl: string) => void;
  addDailyProduction: (input: AddProductionInput) => void;
  deleteDailyProduction: (id: string) => void;
  seedSampleStock: () => void;
}

const JacketContext = createContext<JacketContextValue | null>(null);

export const JacketProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [allJackets, setAllJackets] = useState<JacketItem[]>(loadInitialJackets);
  const [allProductions, setAllProductions] = useState<DailyProductionItem[]>(loadInitialProductions);
  const [dailyTarget, setDailyTarget] = useState<number>(loadInitialTarget);
  const [isIndonesian, setIsIndonesian] = useState<boolean>(loadInitialLanguage);

  const [productionShiftFilter, setProductionShiftFilter] = useState<string>('ALL');
  const [currentScreen, setCurrentScreen] = useState<AppScreen>(AppScreen.HOME);
  const [activeFilter, setActiveFilter] = useState<ActiveFilter>({ type: 'NONE' });
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [selectedCategoryFilter, setSelectedCategoryFilter] = useState<string>('ALL');

  const [isAddSheetOpen, setIsAddSheetOpen] = useState(false);
  const [isFilterSheetOpen, setIsFilterSheetOpen] = useState(false);
  const [isExportSheetOpen, setIsExportSheetOpen] = useState(false);
  const [isAddProductionSheetOpen, setIsAddProductionSheetOpen] = useState(false);
  const [selectedJacketForImage, setSelectedJacketForImage] = useState<JacketItem | null>(null);
  const [prefilledSkuForAdd, setPrefilledSkuForAdd] = useState<string | null>(null);
  const [toast, setToast] = useState<ToastMessage | null>(null);

  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEYS.JACKETS, JSON.stringify(allJackets));
    } catch {
      // Ignore storage quota errors
    }
  }, [allJackets]);

  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEYS.PRODUCTIONS, JSON.stringify(allProductions));
    } catch {
      // Ignore storage quota errors
    }
  }, [allProductions]);

  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEYS.DAILY_TARGET, String(dailyTarget));
    } catch {
      // Ignore
    }
  }, [dailyTarget]);

  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEYS.IS_INDONESIAN, String(isIndonesian));
    } catch {
      // Ignore
    }
  }, [isIndonesian]);

  useEffect(() => {
    if (!toast) return;
    const timer = window.setTimeout(() => {
      setToast(null);
    }, 4500);
    return () => window.clearTimeout(timer);
  }, [toast]);

  const showToast = useCallback((message: string, undoLabel?: string, onUndo?: () => void) => {
    setToast({ id: Date.now(), message, undoLabel, onUndo });
  }, []);

  const dismissToast = useCallback(() => {
    setToast(null);
  }, []);

  const filteredProductions = useMemo(() => {
    if (productionShiftFilter === 'ALL') return allProductions;
    return allProductions.filter(
      (item) => item.shift.toLowerCase() === productionShiftFilter.toLowerCase()
    );
  }, [allProductions, productionShiftFilter]);

  const dailySummary = useMemo<DailyProductionSummary>(() => {
    const todayStr = getTodayDateString();
    const todayList = allProductions.filter((item) => item.dateString === todayStr);
    const totalProduced = todayList.reduce((sum, item) => sum + item.quantityProduced, 0);
    const totalDefects = todayList.reduce((sum, item) => sum + item.defectCount, 0);
    const totalAccepted = Math.max(0, totalProduced - totalDefects);
    const batches = todayList.length;

    const fulfillmentPct =
      dailyTarget > 0 ? Math.min(100, (totalAccepted / dailyTarget) * 100) : 0;
    const avgDefectRate = totalProduced > 0 ? (totalDefects / totalProduced) * 100 : 0;

    return {
      totalProducedToday: totalProduced,
      totalAcceptedToday: totalAccepted,
      totalDefectsToday: totalDefects,
      totalBatchesToday: batches,
      dailyTarget,
      targetFulfillmentPercentage: fulfillmentPct,
      averageDefectRate: avgDefectRate,
    };
  }, [allProductions, dailyTarget]);

  const filteredJackets = useMemo(() => {
    const catList =
      selectedCategoryFilter === 'ALL'
        ? allJackets
        : allJackets.filter(
            (j) => j.genderCategory.toLowerCase() === selectedCategoryFilter.toLowerCase()
          );

    const afterFilter = catList.filter((item) => {
      switch (activeFilter.type) {
        case 'NONE':
          return true;
        case 'BY_STYLE':
          return item.style.toLowerCase() === activeFilter.style.toLowerCase();
        case 'BY_SIZE':
          return item.size.toLowerCase() === activeFilter.size.toLowerCase();
        case 'BY_STATUS':
          return getStockStatus(item.quantity, item.minThreshold) === activeFilter.status;
        case 'BY_MATERIAL':
          return item.material.toLowerCase() === activeFilter.material.toLowerCase();
        case 'BY_COLOR': {
          const matchedColorCat = COLORS.find(
            (c) =>
              c.titleEn.toLowerCase() === activeFilter.color.toLowerCase() ||
              c.titleId.toLowerCase() === activeFilter.color.toLowerCase()
          );
          if (
            item.color.toLowerCase() === activeFilter.color.toLowerCase() ||
            item.color.toLowerCase() === activeFilter.title.toLowerCase()
          ) {
            return true;
          }
          if (matchedColorCat) {
            return (
              item.color.toLowerCase() === matchedColorCat.titleId.toLowerCase() ||
              item.color.toLowerCase() === matchedColorCat.titleEn.toLowerCase()
            );
          }
          return false;
        }
        case 'BY_SEASONAL':
          return item.isSeasonal;
        case 'BY_CATEGORY':
          return item.genderCategory.toLowerCase() === activeFilter.category.toLowerCase();
      }
    });

    const q = searchQuery.trim().toLowerCase();
    if (!q) return afterFilter;
    return afterFilter.filter(
      (j) =>
        j.name.toLowerCase().includes(q) ||
        j.sku.toLowerCase().includes(q) ||
        j.locationRack.toLowerCase().includes(q) ||
        j.style.toLowerCase().includes(q)
    );
  }, [allJackets, activeFilter, searchQuery, selectedCategoryFilter]);

  const toggleLanguage = useCallback(() => {
    setIsIndonesian((prev) => !prev);
  }, []);

  const selectScreen = useCallback((screen: AppScreen) => {
    setCurrentScreen(screen);
  }, []);

  const navigateToHome = useCallback(() => setCurrentScreen(AppScreen.HOME), []);
  const navigateToInventory = useCallback(() => setCurrentScreen(AppScreen.INVENTORY), []);
  const navigateToReports = useCallback(() => setCurrentScreen(AppScreen.REPORTS), []);
  const navigateToJacketList = useCallback(() => setCurrentScreen(AppScreen.JACKET_LIST), []);
  const navigateToProduction = useCallback(() => setCurrentScreen(AppScreen.PRODUCTION), []);

  const navigateToAddJacket = useCallback(() => {
    setPrefilledSkuForAdd(null);
    setCurrentScreen(AppScreen.ADD_JACKET);
  }, []);

  const navigateToAddJacketWithSku = useCallback((sku: string) => {
    setPrefilledSkuForAdd(sku);
    setCurrentScreen(AppScreen.ADD_JACKET);
  }, []);

  const clearPrefilledSku = useCallback(() => setPrefilledSkuForAdd(null), []);

  const openAddSheet = useCallback(() => setIsAddSheetOpen(true), []);
  const closeAddSheet = useCallback(() => setIsAddSheetOpen(false), []);
  const openExportSheet = useCallback(() => setIsExportSheetOpen(true), []);
  const closeExportSheet = useCallback(() => setIsExportSheetOpen(false), []);
  const openAddProductionSheet = useCallback(() => setIsAddProductionSheetOpen(true), []);
  const closeAddProductionSheet = useCallback(() => setIsAddProductionSheetOpen(false), []);

  const updateDailyTarget = useCallback((target: number) => {
    setDailyTarget(Math.max(1, target));
  }, []);

  const openImagePickerForJacket = useCallback((jacket: JacketItem) => {
    setSelectedJacketForImage(jacket);
  }, []);

  const closeImagePicker = useCallback(() => {
    setSelectedJacketForImage(null);
  }, []);

  const openFilterByStyle = useCallback(
    (style: StyleCategory) => {
      setActiveFilter({
        type: 'BY_STYLE',
        style: style.titleEn,
        title: isIndonesian ? style.titleId : style.titleEn,
      });
      setIsFilterSheetOpen(true);
    },
    [isIndonesian]
  );

  const openFilterBySize = useCallback((size: SizeCategory) => {
    setActiveFilter({
      type: 'BY_SIZE',
      size: size.title,
      title: `Size ${size.title}`,
    });
    setIsFilterSheetOpen(true);
  }, []);

  const openFilterByStatus = useCallback(
    (status: StockStatusCategory) => {
      setActiveFilter({
        type: 'BY_STATUS',
        status: status.status,
        title: isIndonesian ? status.titleId : status.titleEn,
      });
      setIsFilterSheetOpen(true);
    },
    [isIndonesian]
  );

  const openFilterByMaterial = useCallback(
    (material: MaterialCategory) => {
      setActiveFilter({
        type: 'BY_MATERIAL',
        material: material.titleEn,
        title: isIndonesian ? material.titleId : material.titleEn,
      });
      setIsFilterSheetOpen(true);
    },
    [isIndonesian]
  );

  const openFilterByColor = useCallback(
    (color: ColorCategory) => {
      setActiveFilter({
        type: 'BY_COLOR',
        color: color.titleEn,
        title: isIndonesian ? color.titleId : color.titleEn,
      });
      setIsFilterSheetOpen(true);
    },
    [isIndonesian]
  );

  const openFilterSeasonal = useCallback(() => {
    setActiveFilter({
      type: 'BY_SEASONAL',
      title: isIndonesian ? SEASONAL_BANNER.titleId : SEASONAL_BANNER.titleEn,
    });
    setIsFilterSheetOpen(true);
  }, [isIndonesian]);

  const openFilterByCategory = useCallback((category: string) => {
    setSelectedCategoryFilter(category);
    setActiveFilter({
      type: 'BY_CATEGORY',
      category,
      title: category,
    });
    setIsFilterSheetOpen(true);
  }, []);

  const closeFilterSheet = useCallback(() => {
    setIsFilterSheetOpen(false);
    setSearchQuery('');
  }, []);

  const adjustQuantity = useCallback((id: string, delta: number) => {
    setAllJackets((prev) =>
      prev.map((j) => (j.id === id ? { ...j, quantity: Math.max(0, j.quantity + delta) } : j))
    );
  }, []);

  const restock = useCallback(
    (id: string) => {
      let restockedName = '';
      setAllJackets((prev) =>
        prev.map((j) => {
          if (j.id === id) {
            restockedName = j.name;
            return { ...j, quantity: j.quantity + 15 };
          }
          return j;
        })
      );
      if (restockedName) {
        showToast(
          isIndonesian
            ? `+15 unit ditambahkan ke ${restockedName}`
            : `+15 units restocked for ${restockedName}`
        );
      }
    },
    [isIndonesian, showToast]
  );

  const restockAllLowStock = useCallback(() => {
    let count = 0;
    setAllJackets((prev) =>
      prev.map((j) => {
        if (getStockStatus(j.quantity, j.minThreshold) !== StockStatusLevel.IN_STOCK) {
          count += 1;
          return { ...j, quantity: j.quantity + 15 };
        }
        return j;
      })
    );
    if (count > 0) {
      showToast(
        isIndonesian
          ? `${count} SKU stok menipis/habis telah diisi ulang (+15 unit)`
          : `${count} low/out-of-stock SKUs restocked (+15 units)`
      );
    }
  }, [isIndonesian, showToast]);

  const deleteJacket = useCallback(
    (id: string) => {
      setAllJackets((prev) => {
        const targetIndex = prev.findIndex((j) => j.id === id);
        if (targetIndex === -1) return prev;
        const deleted = prev[targetIndex];
        const next = prev.filter((j) => j.id !== id);
        showToast(
          isIndonesian ? `'${deleted.name}' dihapus` : `'${deleted.name}' deleted`,
          isIndonesian ? 'Urungkan' : 'Undo',
          () => {
            setAllJackets((curr) => {
              if (curr.some((item) => item.id === deleted.id)) return curr;
              const restored = [...curr];
              restored.splice(Math.min(targetIndex, restored.length), 0, deleted);
              return restored;
            });
          }
        );
        return next;
      });
    },
    [isIndonesian, showToast]
  );

  const isSkuDuplicate = useCallback(
    (sku: string, excludeId: string | null = null) => {
      const clean = sku.trim().toLowerCase();
      if (!clean) return false;
      return allJackets.some(
        (j) => j.sku.toLowerCase() === clean && (excludeId === null || j.id !== excludeId)
      );
    },
    [allJackets]
  );

  const findJacketBySku = useCallback(
    (sku: string) => {
      const clean = sku.trim().toLowerCase();
      if (!clean) return undefined;
      return allJackets.find((j) => j.sku.toLowerCase() === clean);
    },
    [allJackets]
  );

  const generateAutoSku = useCallback(
    (style: string, name: string, size: string) => {
      const styleCode = style.trim() ? style.trim().slice(0, 3).toUpperCase() : 'JKT';
      const nameWords = name
        .trim()
        .split(/\s+/)
        .filter((w) => w.length > 0);
      let nameCode = 'NEW';
      if (nameWords.length >= 2) {
        nameCode = `${nameWords[0].slice(0, 2)}${nameWords[1].slice(0, 2)}`.toUpperCase();
      } else if (nameWords.length === 1 && nameWords[0].length > 0) {
        nameCode = nameWords[0].slice(0, 3).toUpperCase();
      }
      const sizeCode = size.trim() ? size.trim().toUpperCase() : 'STD';
      const base = `JKT-${styleCode}-${nameCode}-${sizeCode}`;
      const exists = allJackets.some((j) => j.sku.toLowerCase() === base.toLowerCase());
      if (exists) {
        const rand = Math.floor(10 + Math.random() * 90);
        return `${base}-${rand}`;
      }
      return base;
    },
    [allJackets]
  );

  const generateRandomCustomSku = useCallback(
    (prefix = 'JKT') => {
      const cleanPrefix = prefix.trim().slice(0, 6).toUpperCase() || 'JKT';
      let candidate = '';
      let attempts = 0;
      do {
        const randomNum = Math.floor(1000 + Math.random() * 9000);
        candidate = `${cleanPrefix}-${randomNum}`;
        attempts += 1;
      } while (isSkuDuplicate(candidate) && attempts < 20);
      return candidate;
    },
    [isSkuDuplicate]
  );

  const addNewJacket = useCallback(
    (input: AddJacketInput) => {
      const resolvedImage =
        input.imageUrl && input.imageUrl.trim() !== ''
          ? input.imageUrl.trim()
          : STYLES.find((s) => s.titleEn.toLowerCase() === input.style.toLowerCase())?.imageUrl ??
            HERO_IMAGE_URL;

      const newJacket: JacketItem = {
        id:
          typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function'
            ? crypto.randomUUID()
            : `jkt-${Date.now()}-${Math.floor(Math.random() * 1000)}`,
        sku: input.sku.trim() || `JKT-${Date.now() % 10000}`,
        name: input.name.trim(),
        style: input.style,
        size: input.size,
        material: input.material,
        color: input.color,
        quantity: Math.max(0, input.quantity),
        minThreshold: 5,
        locationRack: input.locationRack.trim() || 'Rack A-01',
        imageUrl: resolvedImage,
        isSeasonal: Boolean(input.isSeasonal),
        unitPrice: input.unitPrice && input.unitPrice > 0 ? input.unitPrice : 89.99,
        genderCategory: input.genderCategory || 'Jaket Pria',
        createdAt: Date.now(),
      };

      setAllJackets((prev) => [newJacket, ...prev]);
      setIsAddSheetOpen(false);
      showToast(
        isIndonesian
          ? `'${newJacket.name}' disimpan ke inventaris`
          : `'${newJacket.name}' added to inventory`
      );
    },
    [isIndonesian, showToast]
  );

  const updateJacketDetails = useCallback(
    (input: UpdateJacketInput) => {
      setAllJackets((prev) =>
        prev.map((j) => {
          if (j.id !== input.id) return j;
          return {
            ...j,
            sku: input.sku.trim(),
            name: input.name.trim(),
            style: input.style,
            size: input.size,
            material: input.material,
            color: input.color,
            quantity: Math.max(0, input.quantity),
            locationRack: input.locationRack.trim() || 'Rack A-01',
            isSeasonal: input.isSeasonal ?? j.isSeasonal,
            genderCategory: input.genderCategory ?? j.genderCategory,
            unitPrice: input.unitPrice ?? j.unitPrice,
          };
        })
      );
      showToast(
        isIndonesian ? `Perubahan '${input.name}' disimpan` : `Updated '${input.name}'`
      );
    },
    [isIndonesian, showToast]
  );

  const updateJacketImage = useCallback(
    (id: string, imageUrl: string) => {
      setAllJackets((prev) =>
        prev.map((j) => (j.id === id ? { ...j, imageUrl } : j))
      );
      showToast(isIndonesian ? 'Foto SKU diperbarui' : 'SKU image updated');
    },
    [isIndonesian, showToast]
  );

  const updateJacketImageBySku = useCallback(
    (sku: string, imageUrl: string) => {
      setAllJackets((prev) =>
        prev.map((j) =>
          j.sku.toLowerCase() === sku.trim().toLowerCase() ? { ...j, imageUrl } : j
        )
      );
      showToast(
        isIndonesian ? `Foto untuk SKU ${sku} diperbarui` : `Image updated for SKU ${sku}`
      );
    },
    [isIndonesian, showToast]
  );

  const addDailyProduction = useCallback(
    (input: AddProductionInput) => {
      const now = Date.now();
      const todayStr = getTodayDateString();

      const item: DailyProductionItem = {
        id:
          typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function'
            ? crypto.randomUUID()
            : `prod-${now}`,
        date: now,
        dateString: todayStr,
        jacketId: input.jacketId,
        jacketName: input.jacketName,
        jacketSku: input.jacketSku,
        batchNumber: input.batchNumber.trim() || `BATCH-${now % 10000}`,
        shift: input.shift,
        operatorName: input.operatorName.trim() || 'Operator 1',
        quantityProduced: Math.max(0, input.quantityProduced),
        defectCount: Math.max(0, input.defectCount),
        status: input.status,
        notes: input.notes.trim(),
        addedToStock: input.addedToStock,
        createdAt: now,
      };

      setAllProductions((prev) => [item, ...prev]);

      if (item.addedToStock && item.jacketId) {
        const netToAdd = getNetAcceptedQuantity(item);
        if (netToAdd > 0) {
          setAllJackets((prev) =>
            prev.map((j) =>
              j.id === item.jacketId ? { ...j, quantity: j.quantity + netToAdd } : j
            )
          );
        }
      }

      setIsAddProductionSheetOpen(false);
      showToast(
        isIndonesian
          ? `Batch ${item.batchNumber} dicatat (${getNetAcceptedQuantity(item)} pcs lolos QC)`
          : `Batch ${item.batchNumber} logged (${getNetAcceptedQuantity(item)} pcs passed QC)`
      );
    },
    [isIndonesian, showToast]
  );

  const deleteDailyProduction = useCallback(
    (id: string) => {
      setAllProductions((prev) => {
        const idx = prev.findIndex((p) => p.id === id);
        if (idx === -1) return prev;
        const removed = prev[idx];
        showToast(
          isIndonesian ? `Batch ${removed.batchNumber} dihapus` : `Batch ${removed.batchNumber} removed`,
          isIndonesian ? 'Urungkan' : 'Undo',
          () => {
            setAllProductions((curr) => {
              if (curr.some((p) => p.id === removed.id)) return curr;
              const next = [...curr];
              next.splice(Math.min(idx, next.length), 0, removed);
              return next;
            });
          }
        );
        return prev.filter((p) => p.id !== id);
      });
    },
    [isIndonesian, showToast]
  );

  const seedSampleStock = useCallback(() => {
    setAllJackets(getInitialWarehouseInventory());
    setAllProductions(getInitialProductions());
    showToast(
      isIndonesian ? '19 sampel stok jaket dimuat' : '19 sample jackets loaded'
    );
  }, [isIndonesian, showToast]);

  const value: JacketContextValue = {
    allJackets,
    allProductions,
    filteredProductions,
    filteredJackets,
    dailyTarget,
    dailySummary,
    productionShiftFilter,
    currentScreen,
    activeFilter,
    searchQuery,
    selectedCategoryFilter,
    isIndonesian,
    isAddSheetOpen,
    isFilterSheetOpen,
    isExportSheetOpen,
    isAddProductionSheetOpen,
    selectedJacketForImage,
    prefilledSkuForAdd,
    toast,
    dismissToast,
    showToast,
    toggleLanguage,
    selectScreen,
    navigateToHome,
    navigateToInventory,
    navigateToReports,
    navigateToJacketList,
    navigateToProduction,
    navigateToAddJacket,
    navigateToAddJacketWithSku,
    clearPrefilledSku,
    openAddSheet,
    closeAddSheet,
    openExportSheet,
    closeExportSheet,
    openAddProductionSheet,
    closeAddProductionSheet,
    setProductionShiftFilter,
    updateDailyTarget,
    openImagePickerForJacket,
    closeImagePicker,
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
    restockAllLowStock,
    deleteJacket,
    isSkuDuplicate,
    findJacketBySku,
    generateAutoSku,
    generateRandomCustomSku,
    addNewJacket,
    updateJacketDetails,
    updateJacketImage,
    updateJacketImageBySku,
    addDailyProduction,
    deleteDailyProduction,
    seedSampleStock,
  };

  return <JacketContext.Provider value={value}>{children}</JacketContext.Provider>;
};

export function useJackets(): JacketContextValue {
  const ctx = useContext(JacketContext);
  if (!ctx) {
    throw new Error('useJackets must be used within a JacketProvider');
  }
  return ctx;
}
