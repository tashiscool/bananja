var path = require('path');
var glob = require('glob');
var fs = require('fs');
var BeepPlugin = require('webpack-beep-plugin');

// webpack.config.js
module.exports = {
    entry: glob.sync(__dirname+'/spec/**/*Spec.js'),
    output: {filename: __dirname + '/tests.js'},
    module: {
        loaders: [
            {test: /\.js$/, loader: 'babel-loader', query: {
                presets: ['es2015', 'react']
            }}
        ]
    },
    devtool: "#inline-source-map",
    resolve: {
        alias: {
            src: path.normalize(__dirname + '/../src')
        }
    },
    plugins: [new BeepPlugin()]
};

