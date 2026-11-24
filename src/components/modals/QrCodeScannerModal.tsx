import React, { useCallback, useEffect, useRef, useState } from 'react';
import {
  Camera,
  CheckCircle2,
  Keyboard,
  Plus,
  QrCode,
  RefreshCw,
  Shirt,
  X,
} from 'lucide-react';
import { JacketItem, StockStatusLevel, getStockStatus } from '../../types/jacket';
import { NetworkImageWithFallback } from '../NetworkImageWithFallback';

interface QrCodeScannerModalProps {
  isOpen: boolean;
  allJackets: JacketItem[];
  isIndonesian: boolean;
  onDismiss: () => void;
  onJacketIdentified: (jacket: JacketItem) => void;
  onAddNewWithSku: (sku: string) => void;
}

interface BarcodeDetectorLike {
  detect: (source: HTMLVideoElement) => Promise<Array<{ rawValue?: string }>>;
}

declare global {
  interface Window {
    BarcodeDetector?: new (opts?: { formats?: string[] }) => BarcodeDetectorLike;
  }
}

export const QrCodeScannerModal: React.FC<QrCodeScannerModalProps> = ({
  isOpen,
  allJackets,
  isIndonesian,
  onDismiss,
  onJacketIdentified,
  onAddNewWithSku,
}) => {
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const streamRef = useRef<MediaStream | null>(null);

  const [cameraStatus, setCameraStatus] = useState<'idle' | 'active' | 'denied' | 'unsupported'>(
    'idle'
  );
  const [detectedCode, setDetectedCode] = useState<string | null>(null);
  const [matchedJacket, setMatchedJacket] = useState<JacketItem | null>(null);
  const [showManualInput, setShowManualInput] = useState(true);
  const [manualInputText, setManualInputText] = useState('');

  const handleScannedCode = useCallback(
    (rawCode: string) => {
      const clean = rawCode.trim();
      if (!clean) return;
      setDetectedCode(clean);
      const found =
        allJackets.find(
          (j) =>
            j.sku.toLowerCase() === clean.toLowerCase() ||
            j.id.toLowerCase() === clean.toLowerCase() ||
            clean.toLowerCase().includes(j.sku.toLowerCase()) ||
            j.name.toLowerCase() === clean.toLowerCase()
        ) ?? null;
      setMatchedJacket(found);
    },
    [allJackets]
  );

  const stopCamera = useCallback(() => {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach((track) => track.stop());
      streamRef.current = null;
    }
  }, []);

  const startCamera = useCallback(async () => {
    if (
      typeof navigator === 'undefined' ||
      !navigator.mediaDevices ||
      !navigator.mediaDevices.getUserMedia
    ) {
      setCameraStatus('unsupported');
      return;
    }

    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'environment' },
      });
      streamRef.current = stream;
      if (videoRef.current) {
        videoRef.current.srcObject = stream;
        await videoRef.current.play().catch(() => undefined);
      }
      setCameraStatus('active');
    } catch {
      setCameraStatus('denied');
    }
  }, []);

  useEffect(() => {
    if (!isOpen) {
      stopCamera();
      setDetectedCode(null);
      setMatchedJacket(null);
      setManualInputText('');
      return;
    }

    startCamera();
    return () => stopCamera();
  }, [isOpen, startCamera, stopCamera]);

  // BarcodeDetector polling when camera is active & supported by browser
  useEffect(() => {
    if (!isOpen || cameraStatus !== 'active' || detectedCode) return;
    if (!window.BarcodeDetector || !videoRef.current) return;

    let cancelled = false;
    let timerId: number | null = null;
    const detector = new window.BarcodeDetector({
      formats: ['qr_code', 'code_128', 'code_39', 'ean_13', 'upc_a'],
    });

    const scanTick = async () => {
      if (cancelled || !videoRef.current) return;
      try {
        if (videoRef.current.readyState >= 2) {
          const results = await detector.detect(videoRef.current);
          const firstValid = results.find((r) => r.rawValue && r.rawValue.trim() !== '');
          if (firstValid?.rawValue) {
            handleScannedCode(firstValid.rawValue);
            return;
          }
        }
      } catch {
        // Ignore single frame errors
      }
      if (!cancelled) {
        timerId = window.setTimeout(scanTick, 450);
      }
    };

    timerId = window.setTimeout(scanTick, 500);
    return () => {
      cancelled = true;
      if (timerId !== null) window.clearTimeout(timerId);
    };
  }, [isOpen, cameraStatus, detectedCode, handleScannedCode]);

  if (!isOpen) return null;

  const sampleSkus = [
    ...allJackets
      .map((j) => j.sku)
      .filter(Boolean)
      .slice(0, 4),
    'JKT-NEW-WHT-XL',
  ];

  return (
    <div className="fixed inset-0 z-50 bg-slate-950/95 text-white flex flex-col">
      {/* Top Bar */}
      <div className="px-4 py-3 flex items-center justify-between border-b border-slate-800">
        <button
          type="button"
          onClick={onDismiss}
          aria-label="Close scanner"
          className="w-10 h-10 rounded-full bg-white/10 hover:bg-white/20 flex items-center justify-center cursor-pointer"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="px-4 py-1.5 rounded-full bg-slate-900 border border-slate-700 inline-flex items-center gap-2">
          <QrCode className="w-4 h-4 text-sky-400" />
          <span className="text-xs font-bold">
            {isIndonesian ? 'Scan Label Jaket' : 'Scan Jacket Label'}
          </span>
        </div>

        <button
          type="button"
          onClick={() => setShowManualInput((v) => !v)}
          aria-label="Toggle manual input"
          className={`w-10 h-10 rounded-full flex items-center justify-center cursor-pointer ${
            showManualInput ? 'bg-blue-600' : 'bg-white/10 hover:bg-white/20'
          }`}
        >
          <Keyboard className="w-5 h-5" />
        </button>
      </div>

      {/* Viewfinder Area */}
      <div className="flex-1 relative flex items-center justify-center overflow-hidden p-4">
        {cameraStatus === 'active' ? (
          <>
            <video
              ref={videoRef}
              playsInline
              muted
              className="absolute inset-0 w-full h-full object-cover opacity-80"
            />
            <div className="relative z-10 w-64 h-64 rounded-3xl border-2 border-sky-400 shadow-[0_0_0_9999px_rgba(2,6,23,0.65)] overflow-hidden">
              {!detectedCode && (
                <div className="absolute inset-x-3 h-1 rounded-full bg-gradient-to-r from-transparent via-sky-400 to-transparent animate-scan-laser" />
              )}
            </div>
            <p className="relative z-10 mt-72 px-4 py-2 rounded-xl bg-slate-900/80 border border-slate-700 text-xs text-slate-200 text-center">
              {isIndonesian
                ? 'Arahkan kotak ke kode QR atau Barcode label jaket'
                : 'Align frame with jacket label QR code or barcode'}
            </p>
          </>
        ) : (
          <div className="max-w-md w-full bg-slate-900 border border-slate-700 rounded-2xl p-6 text-center space-y-3">
            <div className="w-14 h-14 rounded-full bg-sky-500/20 text-sky-400 flex items-center justify-center mx-auto">
              <Camera className="w-7 h-7" />
            </div>
            <h4 className="text-base font-bold">
              {isIndonesian
                ? 'Kamera Tidak Tersedia / Belum Diizinkan'
                : 'Camera Unavailable or Permission Denied'}
            </h4>
            <p className="text-xs text-slate-400 leading-relaxed">
              {isIndonesian
                ? 'Gunakan panel Input SKU Manual di bawah untuk memindai atau menguji kode label secara instan.'
                : 'Use the Manual Barcode / SKU panel below to scan or test label codes instantly.'}
            </p>
            <div className="flex justify-center gap-2 pt-1">
              <button
                type="button"
                onClick={startCamera}
                className="h-9 px-4 rounded-xl bg-sky-600 hover:bg-sky-700 text-xs font-bold inline-flex items-center gap-1.5 cursor-pointer"
              >
                <RefreshCw className="w-3.5 h-3.5" />
                {isIndonesian ? 'Coba Kamera Lagi' : 'Retry Camera'}
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Bottom Panels: Result or Manual Input */}
      <div className="p-4 max-w-2xl w-full mx-auto space-y-3">
        {detectedCode && (
          <div className="bg-slate-900 border border-slate-700 rounded-2xl p-4 space-y-3">
            {matchedJacket ? (
              <>
                <div className="flex items-center justify-between">
                  <span className="inline-flex items-center gap-1.5 text-xs font-bold text-emerald-400">
                    <CheckCircle2 className="w-4 h-4" />
                    {isIndonesian ? 'Jaket Ditemukan di Gudang' : 'Jacket Found in Warehouse'}
                  </span>
                  <button
                    type="button"
                    onClick={() => {
                      setDetectedCode(null);
                      setMatchedJacket(null);
                    }}
                    className="text-xs text-sky-400 font-semibold cursor-pointer"
                  >
                    {isIndonesian ? 'Scan Ulang' : 'Scan Again'}
                  </button>
                </div>

                <div className="flex items-center gap-3 bg-slate-800 rounded-xl p-3">
                  <div className="w-14 h-14 rounded-lg overflow-hidden shrink-0 bg-slate-700">
                    <NetworkImageWithFallback
                      src={matchedJacket.imageUrl}
                      alt={matchedJacket.name}
                    />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="font-mono-sku text-[11px] font-bold text-sky-300">
                      {matchedJacket.sku}
                    </p>
                    <p className="text-sm font-bold text-white truncate">{matchedJacket.name}</p>
                    <p className="text-xs text-slate-300">
                      {matchedJacket.style} • {matchedJacket.size} • {matchedJacket.locationRack} •{' '}
                      <span
                        className={
                          getStockStatus(matchedJacket.quantity, matchedJacket.minThreshold) ===
                          StockStatusLevel.IN_STOCK
                            ? 'text-emerald-400 font-bold'
                            : 'text-amber-400 font-bold'
                        }
                      >
                        {matchedJacket.quantity} pcs
                      </span>
                    </p>
                  </div>
                </div>

                <button
                  type="button"
                  onClick={() => onJacketIdentified(matchedJacket)}
                  className="w-full h-11 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-sm cursor-pointer"
                >
                  {isIndonesian ? 'Lihat Detail Produk' : 'View Product Details'}
                </button>
              </>
            ) : (
              <>
                <div className="flex items-center justify-between">
                  <span className="text-xs font-bold text-amber-400">
                    {isIndonesian ? 'Label SKU Baru Terdeteksi' : 'Unregistered Label SKU Detected'}
                  </span>
                  <button
                    type="button"
                    onClick={() => setDetectedCode(null)}
                    className="text-xs text-sky-400 font-semibold cursor-pointer"
                  >
                    {isIndonesian ? 'Scan Ulang' : 'Scan Again'}
                  </button>
                </div>
                <p className="text-sm font-mono-sku font-bold text-white bg-slate-800 px-3 py-2 rounded-lg">
                  {detectedCode}
                </p>
                <button
                  type="button"
                  onClick={() => onAddNewWithSku(detectedCode)}
                  className="w-full h-11 rounded-xl bg-sky-600 hover:bg-sky-700 text-white font-bold text-sm inline-flex items-center justify-center gap-2 cursor-pointer"
                >
                  <Plus className="w-4 h-4" />
                  {isIndonesian
                    ? `Tambah Jaket dengan SKU '${detectedCode}'`
                    : `Add Jacket with SKU '${detectedCode}'`}
                </button>
              </>
            )}
          </div>
        )}

        {showManualInput && (
          <div className="bg-slate-900 border border-slate-700 rounded-2xl p-4 space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-slate-300">
                {isIndonesian
                  ? 'Input / Simulasi Barcode Label'
                  : 'Manual Barcode / SKU Input'}
              </span>
            </div>

            <div className="flex gap-2">
              <input
                type="text"
                value={manualInputText}
                onChange={(e) => setManualInputText(e.target.value.toUpperCase())}
                placeholder="e.g. JKT-BMB-BLK-M"
                className="flex-1 h-10 px-3 rounded-xl bg-slate-800 border border-slate-600 text-white font-mono-sku text-xs focus:outline-none focus:ring-2 focus:ring-sky-500"
              />
              <button
                type="button"
                disabled={!manualInputText.trim()}
                onClick={() => handleScannedCode(manualInputText)}
                className="h-10 px-4 rounded-xl bg-sky-600 hover:bg-sky-700 disabled:opacity-50 text-white text-xs font-bold cursor-pointer"
              >
                {isIndonesian ? 'Cek' : 'Check'}
              </button>
            </div>

            <div>
              <p className="text-[11px] text-slate-400 mb-1.5">
                {isIndonesian ? 'Label Contoh Gudang:' : 'Sample Label Codes:'}
              </p>
              <div className="flex gap-2 overflow-x-auto no-scrollbar pb-0.5">
                {sampleSkus.map((sample) => {
                  const isKnown = allJackets.some(
                    (j) => j.sku.toLowerCase() === sample.toLowerCase()
                  );
                  return (
                    <button
                      key={sample}
                      type="button"
                      onClick={() => {
                        setManualInputText(sample);
                        handleScannedCode(sample);
                      }}
                      className={`shrink-0 px-2.5 py-1.5 rounded-lg border border-slate-700 bg-slate-800 hover:bg-slate-700 text-[11px] font-mono-sku font-semibold inline-flex items-center gap-1.5 cursor-pointer ${
                        isKnown ? 'text-sky-300' : 'text-amber-300'
                      }`}
                    >
                      {isKnown ? (
                        <Shirt className="w-3 h-3" />
                      ) : (
                        <Plus className="w-3 h-3" />
                      )}
                      {isKnown ? sample : `${sample} (+)`}
                    </button>
                  );
                })}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
