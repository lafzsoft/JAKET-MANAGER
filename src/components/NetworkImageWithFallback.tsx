import React, { useEffect, useState } from 'react';
import { Shirt } from 'lucide-react';

interface NetworkImageWithFallbackProps {
  src?: string;
  alt: string;
  className?: string;
  iconClassName?: string;
}

export const NetworkImageWithFallback: React.FC<NetworkImageWithFallbackProps> = ({
  src,
  alt,
  className = 'w-full h-full object-cover',
  iconClassName = 'w-8 h-8 text-slate-400',
}) => {
  const [status, setStatus] = useState<'loading' | 'loaded' | 'error'>(
    src && src.trim() !== '' ? 'loading' : 'error'
  );

  useEffect(() => {
    if (src && src.trim() !== '') {
      setStatus('loading');
    } else {
      setStatus('error');
    }
  }, [src]);

  if (!src || src.trim() === '' || status === 'error') {
    return (
      <div
        className={`flex flex-col items-center justify-center bg-slate-100 text-slate-400 select-none ${className}`}
        role="img"
        aria-label={alt}
      >
        <Shirt className={iconClassName} />
      </div>
    );
  }

  return (
    <div className="relative w-full h-full overflow-hidden bg-slate-100">
      {status === 'loading' && (
        <div className="absolute inset-0 flex items-center justify-center bg-slate-100 animate-pulse">
          <Shirt className="w-6 h-6 text-slate-300" />
        </div>
      )}
      <img
        src={src}
        alt={alt}
        referrerPolicy="no-referrer"
        onLoad={() => setStatus('loaded')}
        onError={() => setStatus('error')}
        className={`${className} transition-opacity duration-300 ${
          status === 'loaded' ? 'opacity-100' : 'opacity-0'
        }`}
      />
    </div>
  );
};
