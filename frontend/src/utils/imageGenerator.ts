export const generateDynamicPoster = (title: string, genre: string = 'Movie') => {
  // Create a deterministic hash from the title to pick colors
  const hash = title.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0);
  
  // Vibrant color palettes for modern aesthetic
  const palettes = [
    ['#FF416C', '#FF4B2B'], // Red/Orange
    ['#4776E6', '#8E54E9'], // Blue/Purple
    ['#00B4DB', '#0083B0'], // Cyan/Blue
    ['#f12711', '#f5af19'], // Fire
    ['#834d9b', '#d04ed6'], // Pink/Purple
    ['#11998e', '#38ef7d'], // Green
    ['#fc4a1a', '#f7b733'], // Orange/Yellow
    ['#654ea3', '#eaafc8'], // Muted Purple/Pink
  ];

  const [color1, color2] = palettes[hash % palettes.length];

  // SVG structure for a beautiful, modern placeholder
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 1200" width="100%" height="100%">
      <defs>
        <linearGradient id="grad${hash}" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" style="stop-color:${color1};stop-opacity:1" />
          <stop offset="100%" style="stop-color:${color2};stop-opacity:1" />
        </linearGradient>
        <filter id="noise">
          <feTurbulence type="fractalNoise" baseFrequency="0.65" numOctaves="3" stitchTiles="stitch"/>
          <feColorMatrix type="matrix" values="1 0 0 0 0, 0 1 0 0 0, 0 0 1 0 0, 0 0 0 0.15 0" />
        </filter>
      </defs>
      
      <!-- Background -->
      <rect width="100%" height="100%" fill="url(#grad${hash})" />
      
      <!-- Subtle Noise Texture for premium feel -->
      <rect width="100%" height="100%" filter="url(#noise)" opacity="0.4" />
      
      <!-- Decorative Elements -->
      <circle cx="400" cy="600" r="300" fill="white" opacity="0.05" />
      <circle cx="400" cy="600" r="450" fill="none" stroke="white" stroke-width="2" opacity="0.05" />
      
      <!-- Content -->
      <g transform="translate(400, 600)" text-anchor="middle" font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif">
        <text y="-40" fill="white" font-size="64" font-weight="800" letter-spacing="-1" opacity="0.95">${title}</text>
        <rect x="-60" y="20" width="120" height="4" fill="white" opacity="0.5" rx="2" />
        <text y="70" fill="white" font-size="28" font-weight="600" letter-spacing="4" text-transform="uppercase" opacity="0.8">${genre}</text>
      </g>
    </svg>
  `;

  // Encode as base64 data URI
  return `data:image/svg+xml;base64,${btoa(svg)}`;
};
