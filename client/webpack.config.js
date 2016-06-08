var path = require('path');
var webpack = require('webpack');
var BeepPlugin = require('webpack-beep-plugin');
var fs = require('fs');



var isProduction = process.env.NODE_ENV === 'production';

var definePlugin = new webpack.DefinePlugin({
    PRODUCTION: isProduction
});


module.exports = {
    entry: './drunkr.js',
    output: {
        filename: `${__dirname}/../public/javascripts/drunkr.js`
    },
    module: {
        loaders: [
            {
                test: /\.js$/,
                loader: 'babel-loader',
                include: [/reactive-store/,/src/,/drunkr.js/],
                query: {
                    presets: ['es2015', 'react']
                }
            },
            {test: /\.po$/, loader: 'json-loader!po-loader?format=jed1.x'},
            { test: /\.less$/,loader: 'style-loader!css-loader?sourceMap!less-loader?sourceMap'},
            {test: /\.css$/, loaders: ['style-loader', 'css-loader?sourceMap']},
            {
                test: /\.(jpe?g|png|gif|svg|pdf)$/i,
                loader: 'url-loader'
            },
            { test: /\.woff(2)?(\?v=[0-9]\.[0-9]\.[0-9])?$/, loader: "url-loader?mimetype=application/font-woff" },
            { test: /\.(ttf|eot)(\?v=[0-9]\.[0-9]\.[0-9])?$/, loader: "file-loader?name=../public/fonts/[name].[ext]" }
        ]
    },
    devtool: isProduction ? undefined : "#inline-source-map",
    resolve: {
        alias: {
            RS: __dirname + '/RS.js',
            components: __dirname + '/src/components',
            services: __dirname + '/src/services',
            shared: `${__dirname}/src/shared/components`,
            style: `${__dirname}/src/style`,
            bootstrap: 'react-bootstrap/lib',
            GetText: `${__dirname}/src/services/OldGetText`,
            messages: `${__dirname}/messages`,
            routes: `${__dirname}/src/routes`
        }
    },
    node: {
        fs: "empty"
    },
    plugins: [
        definePlugin,
        new webpack.OldWatchingPlugin(),
        new BeepPlugin(),
        new webpack.ProvidePlugin({
            $j: "jquery",
            _: "lodash",
            Col: 'react-bootstrap/lib/Col',
            Row: 'react-bootstrap/lib/Row',
            Grid: 'react-bootstrap/lib/Grid',
            AppService: __dirname + '/src/services/AppService',
            PureRenderComponent: __dirname + '/src/shared/components/PureRenderComponent',
            Component: __dirname + '/src/shared/components/Component',
            LinkTo: 'components/core/LinkTo'
        })
    ]
};


