# Jacket Manager

Web-based product and warehouse inventory management application for jackets, built with **React**, **TypeScript**, **Vite**, and **Tailwind CSS** (rewritten from `lafzsoft/JAKET-MANAGER`).

## Core Features

- **Home Catalog Dashboard**: Summary KPIs, bilingual Indonesian/English (`ID` / `EN`) toggle, demographic categories, jacket styles, size guide (`XS`–`3XL`), stock availability breakdown, seasonal winter banner, materials, color swatches, and searchable warehouse directory.
- **Inventory Catalog**: Grid and list views, status tabs (`All`, `In Stock`, `Low Stock`, `Out of Stock`, `Seasonal`), demographic & style filters, sorting, quick stock steppers, `+15 Restock`, SKU photo picker (upload, presets, or URL), product detail modal with color variants and size matrix, and QR/Barcode scanner modal (`getUserMedia` + `BarcodeDetector` with manual/sample SKU fallback).
- **Daily Production Tracking**: Log sewing production batches by shift (`Morning`, `Afternoon`, `Night`), track QC defects & pass rates, edit daily output targets, and automatically sync passed QC units into linked jacket stock.
- **Add Jacket Flow**: Full registration screen and quick-add modal with auto-generated or manual SKUs, duplicate SKU validation, warehouse rack assignment, and live preview.
- **Reports & Analytics**: Total warehouse valuation, interactive distribution charts (by style, size, or category), stock readiness health index, immediate restock alerts with batch `+15` restock, and RFC 4180 UTF-8 BOM CSV export (preview, clipboard copy, and `.csv` download).
- **Local Persistence**: Automatically persists inventory, production batches, daily targets, and language preference in browser `localStorage`, seeded with 19 sample jackets and 4 production batches.

## Development

```bash
npm install
npm run dev
```

Runs on `http://localhost:3000`.
