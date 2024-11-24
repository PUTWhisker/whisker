// Generated using webpack-cli https://github.com/webpack/webpack-cli

const path = require('path');

module.exports = {
    entry: './client.js', // Entry point of your application
    output: {
        path: path.resolve(__dirname, 'dist'), // Output directory
        filename: 'main.js', // Output filename
    },
    module: {
        rules: [
            {
                test: /\.js$/, // Matches JavaScript files
                exclude: /node_modules/, // Excludes node_modules from transpiling
                use: {
                    loader: 'babel-loader', // Use Babel loader
                },
            },
        ],
    },
    mode: 'development', // Use 'production' for optimized builds
};
