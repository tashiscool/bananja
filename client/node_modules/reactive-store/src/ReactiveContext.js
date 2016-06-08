var R = require('ramda');
var ReactiveContext = module.exports = function(fn) {
    var deps = [];

    var that = {
        fn: fn,
        flush: function () {
            deps.length && _.some(deps, R.prop('invalid')) && that.run(false);
        },
        addDependency: function (dep) {
            deps.indexOf(dep) === -1 && deps.push(dep);
        },
        run: function (opts) {
            deps = [];
            var prevContext = ReactiveContext.current;
            ReactiveContext.current = that;
            fn(opts);
            ReactiveContext.current = prevContext;
        }
    };
    return that;
};

ReactiveContext.flushAll = function () {
    ReactiveContext.list.forEach(c => c.flush());
};

ReactiveContext.autorun = function (fn) {
    var ctx = _.find(ReactiveContext.list, {fn: fn});
    if (ctx) {
        ctx.run(false);
    } else {
        ctx = ReactiveContext(fn);
        ctx.run(true);
        ReactiveContext.list.push(ctx);
    }
    return {
        stop: () => {
            ReactiveContext.list = _.without(ReactiveContext.list, ctx);
        }
    }
};

ReactiveContext.nonReactive = function(fn) {
    var prevContext = ReactiveContext.current;
    ReactiveContext.current = undefined;
    fn();
    ReactiveContext.current = prevContext;
};


ReactiveContext.list = [];
