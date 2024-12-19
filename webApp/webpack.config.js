const path = require('path');

module.exports = {
    entry: {
        './client': './src/client.js',
        './record': './src/record.js',
        './login-signup': './src/login-signup.js',
        './history': './src/history.js',
    },
    output: {
        path: path.resolve(__dirname, 'dist'),
        filename: '[name].js',
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
    mode: 'development',
};
