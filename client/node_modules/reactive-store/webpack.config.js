var path = require('path');
var BeepPlugin = require('webpack-beep-plugin');


// webpack.config.js
module.exports = {
    entry: './src/reactive-store-for-browser.js',
    output: {
        filename: 'dist/reactive-store-for-browser.js'
    },
    module: {
        loaders: [
            {
                test: /\.js$/,
                loader: 'babel-loader',
                exclude: [/node_modules/, /reactive-store/],
                query: {
                    presets: ['es2015', 'react']
                }
            }
        ]
    },
    devtool: "#inline-source-map",
    plugins: [new BeepPlugin()]
};

