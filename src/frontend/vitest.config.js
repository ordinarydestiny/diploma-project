import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
    plugins: [vue()],
    resolve: {
        alias: {
            '@': path.resolve(__dirname, 'src')
        }
    },
    test: {
        globals: true,
        environment: 'happy-dom',
        include: [
            'src/**/*.{test,spec}.{js,ts}',
            'tests/**/*.{test,spec}.{js,ts}'
        ],
        coverage: {
            provider: 'v8',
            reporter: ['text', 'html', 'lcov'],
            include: ['src/**/*.{js,vue}'],
            exclude: [
                'src/main.js',
                'src/App.vue',
                'src/**/*.spec.js',
                'src/**/*.test.js'
            ],
            thresholds: {
                lines: 0,
                functions: 0,
                branches: 0,
                statements: 0
            }
        }
    }
})
