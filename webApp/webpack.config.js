// Generated using webpack-cli https://github.com/webpack/webpack-cli

const path = require('path');

module.exports = {
    entry: './client.js',
    output: {
        path: path.resolve(__dirname, 'dist'),
        filename: 'main.js',
    },
    module: {
        rules: [
            {
                test: /\.js$/,
                exclude: /node_modules/, 
                use: {
                    loader: 'babel-loader',
                    options: {
                      presets: ['@babel/preset-env']
                    }
                }
            },
        ],
    },
    mode: 'development', // Use 'production' for optimized builds
};
