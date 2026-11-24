export enum StockStatusLevel {
  IN_STOCK = 'IN_STOCK',
  LOW_STOCK = 'LOW_STOCK',
  OUT_OF_STOCK = 'OUT_OF_STOCK',
}

export interface JacketItem {
  id: string;
  sku: string;
  name: string;
  style: string;
  size: string;
  material: string;
  color: string;
  quantity: number;
  minThreshold: number;
  locationRack: string;
  imageUrl: string;
  isSeasonal: boolean;
  unitPrice: number;
  genderCategory: string;
  createdAt?: number;
}

export function getStockStatus(quantity: number, minThreshold = 5): StockStatusLevel {
  if (quantity <= 0) return StockStatusLevel.OUT_OF_STOCK;
  if (quantity <= minThreshold) return StockStatusLevel.LOW_STOCK;
  return StockStatusLevel.IN_STOCK;
}

export interface StyleCategory {
  id: string;
  titleEn: string;
  titleId: string;
  descEn: string;
  descId: string;
  imageUrl: string;
}

export interface SizeCategory {
  id: string;
  title: string;
  descEn: string;
  descId: string;
  imageUrl: string;
}

export interface StockStatusCategory {
  id: string;
  status: StockStatusLevel;
  titleEn: string;
  titleId: string;
  descEn: string;
  descId: string;
  imageUrl: string;
}

export interface MaterialCategory {
  id: string;
  titleEn: string;
  titleId: string;
  descEn: string;
  descId: string;
  imageUrl: string;
}

export interface ColorCategory {
  id: string;
  titleEn: string;
  titleId: string;
  descEn: string;
  descId: string;
  hexColor: string;
  imageUrl: string;
}

export interface BannerData {
  titleEn: string;
  titleId: string;
  subtitleEn: string;
  subtitleId: string;
  imageUrl: string;
}

export interface DemographicCategory {
  id: string;
  titleId: string;
  titleEn: string;
  descId: string;
  descEn: string;
  tag: string;
  imageUrl: string;
}

export type ActiveFilter =
  | { type: 'NONE' }
  | { type: 'BY_STYLE'; style: string; title: string }
  | { type: 'BY_SIZE'; size: string; title: string }
  | { type: 'BY_STATUS'; status: StockStatusLevel; title: string }
  | { type: 'BY_MATERIAL'; material: string; title: string }
  | { type: 'BY_COLOR'; color: string; title: string }
  | { type: 'BY_SEASONAL'; title: string }
  | { type: 'BY_CATEGORY'; category: string; title: string };

export enum ProductionShift {
  MORNING = 'MORNING',
  AFTERNOON = 'AFTERNOON',
  NIGHT = 'NIGHT',
}

export const PRODUCTION_SHIFT_META: Record<
  ProductionShift,
  { titleEn: string; titleId: string; shortEn: string; shortId: string }
> = {
  [ProductionShift.MORNING]: {
    titleEn: 'Morning Shift (08:00 - 16:00)',
    titleId: 'Shift Pagi (08:00 - 16:00)',
    shortEn: 'Morning',
    shortId: 'Pagi',
  },
  [ProductionShift.AFTERNOON]: {
    titleEn: 'Afternoon Shift (16:00 - 24:00)',
    titleId: 'Shift Siang (16:00 - 24:00)',
    shortEn: 'Afternoon',
    shortId: 'Siang',
  },
  [ProductionShift.NIGHT]: {
    titleEn: 'Night Shift (00:00 - 08:00)',
    titleId: 'Shift Malam (00:00 - 08:00)',
    shortEn: 'Night',
    shortId: 'Malam',
  },
};

export enum ProductionStatus {
  COMPLETED = 'COMPLETED',
  IN_PROGRESS = 'IN_PROGRESS',
  QUALITY_CHECK = 'QUALITY_CHECK',
}

export const PRODUCTION_STATUS_META: Record<
  ProductionStatus,
  { labelEn: string; labelId: string }
> = {
  [ProductionStatus.COMPLETED]: { labelEn: 'Finished', labelId: 'Selesai' },
  [ProductionStatus.IN_PROGRESS]: { labelEn: 'In Progress', labelId: 'Proses' },
  [ProductionStatus.QUALITY_CHECK]: { labelEn: 'QC Review', labelId: 'Pemeriksaan QC' },
};

export interface DailyProductionItem {
  id: string;
  date: number;
  dateString: string;
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
  createdAt: number;
}

export function getNetAcceptedQuantity(item: Pick<DailyProductionItem, 'quantityProduced' | 'defectCount'>): number {
  return Math.max(0, item.quantityProduced - item.defectCount);
}

export function getDefectRatePercentage(item: Pick<DailyProductionItem, 'quantityProduced' | 'defectCount'>): number {
  if (item.quantityProduced <= 0) return 0;
  return (item.defectCount / item.quantityProduced) * 100;
}

export interface DailyProductionSummary {
  totalProducedToday: number;
  totalAcceptedToday: number;
  totalDefectsToday: number;
  totalBatchesToday: number;
  dailyTarget: number;
  targetFulfillmentPercentage: number;
  averageDefectRate: number;
}

export enum JacketSortOrder {
  NAME_ASC = 'NAME_ASC',
  QTY_ASC = 'QTY_ASC',
  QTY_DESC = 'QTY_DESC',
  STYLE = 'STYLE',
}

export enum AppScreen {
  HOME = 'HOME',
  INVENTORY = 'INVENTORY',
  ADD_JACKET = 'ADD_JACKET',
  PRODUCTION = 'PRODUCTION',
  REPORTS = 'REPORTS',
  JACKET_LIST = 'JACKET_LIST',
}

export interface PresetImageOption {
  id: string;
  nameId: string;
  nameEn: string;
  imageUrl: string;
}

export interface ToastMessage {
  id: number;
  message: string;
  undoLabel?: string;
  onUndo?: () => void;
}
