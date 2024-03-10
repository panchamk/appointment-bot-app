/** @type {import('next').NextConfig} */
const nextConfig = {
    output: 'export',
    reactStrictMode: true,
    images: {
      unoptimized: true,
    },
    async rewrites() {
      return [
        {
          source: '/v1/appointment/:path*',
          destination: 'http://localhost:8080/v1/appointment/:path*',
        },
      ]
    },
  }
  module.exports = nextConfig