import js from '@eslint/js'
import pluginVue from 'eslint-plugin-vue'
import jsdoc from 'eslint-plugin-jsdoc'
import globals from 'globals'

export default [
    js.configs.recommended,
    ...pluginVue.configs['flat/recommended'],
    {
        languageOptions: {
            globals: {
                ...globals.browser,
                ...globals.es2021
            }
        },
        plugins: {
            jsdoc
        },
        rules: {
            ...jsdoc.configs.recommended.rules,
            'jsdoc/require-jsdoc': ['warn', {
                require: {
                    FunctionDeclaration: true,
                    MethodDefinition: true,
                    ClassDeclaration: true,
                    ArrowFunctionExpression: true,
                    FunctionExpression: true
                },
                contexts: [
                    'VariableDeclaration',
                    'ExportDefaultDeclaration',
                    'ExportNamedDeclaration'
                ],
                publicOnly: false
            }],
            'jsdoc/require-description': ['warn', {
                contexts: ['any']
            }],
            'jsdoc/require-param': 'warn',
            'jsdoc/require-param-type': 'warn',
            'jsdoc/require-returns': 'warn',
            'jsdoc/require-returns-type': 'warn',
            'jsdoc/check-tag-names': 'off',
            'jsdoc/require-description-complete-sentence': 'off',
            'jsdoc/no-undefined-types': 'off',
            'jsdoc/multiline-blocks': 'off',
            'jsdoc/tag-lines': 'off',
            'no-console': ['warn', {
                allow: ['warn', 'error']
            }],
            'no-unused-vars': 'warn',
            'vue/multi-word-component-names': 'off',
            'vue/require-default-prop': 'off'
        }
    },
    {
        ignores: [
            'dist/**',
            'node_modules/**',
            '*.config.js'
        ]
    }
]
