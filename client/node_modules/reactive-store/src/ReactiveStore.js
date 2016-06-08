global._babelPolyfill || require('babel-polyfill');

var _ = require('lodash');
var Dict = require('./Dict');
var Notifier = require('./Notifier');
var ReactiveContext = require('./ReactiveContext');
var Dependency = require('./Dependency');

function ReactiveStore() {
    "use strict";
    var dict = Dict();
    var debug;

    window.dict = dict;

    function convertToDotNotation(key) {
        return key.replace(/\[([0-9]*)\]/g, '.$1'); // replace [] array syntax with dot notation
    }

    var that = {
        clearChildren: function(key) {
            dict.clearChildren(key);
            Notifier(dict).add(key).flush();
        },
        set: function (key, val) {
            debug && console.log('set(' + key + ', ' + val + ')');
            var notifier = Notifier(dict);
            set(key,val);
            notifier.flush();

            function set(key, val) {
                if (key === undefined) {
                    throw new Error("Can not get value of undefined key");
                }
                key = convertToDotNotation(key);

                if(JSON.stringify(val) !== JSON.stringify(dict.get(key))) {
                    _.flattenDeep(dict.set(key, val)).forEach(notifier.add);
                }
            }
        },
        get: function (key) {
            debug && console.log('get('+key+')');
            if(key === undefined) {
                throw new Error("Can not get value of undefined key");
            }
            key = convertToDotNotation(key);
            var val = dict.get(key);

            if(ReactiveContext.current) {
                var dep = Dependency();
                dep.depend();
                dict.addDependency(key, dep);
            }

            return val;
        },
        dump: dict.dump,

        load: dict.load,


        autorun: ReactiveContext.autorun,
        nonReactive: ReactiveContext.nonReactive,
        raw: dict.raw,
        wipe: dict.wipe,
        debug: {
            on: function() {
                debug = true;
            },
            off: function() {
                debug = false;
            }
        }
    };
    return that;
};

module.exports = ReactiveStore;
