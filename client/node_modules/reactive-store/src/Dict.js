var MetaStore = require('./MetaStore');

module.exports = () => {
    var that = {};
    var store = MetaStore();
    that.raw = store.raw;
    that.delete = store.delete;
    that.dump = store.dump;
    that.load = store.load;
    that.set = store.setValue;
    that.get = (key, dflt) => {
        var value = store.getValue(key);
        if (value === undefined) {
            return dflt;
        }
        return value;
    };
    that.wipe = store.wipe;
    that.addDependency = (key, dep) => {
        var deps = store.getMeta(key, 'deps');
        store.setMeta(key, {deps: deps ? deps.concat(dep) : [dep]});
    };

    that.getDependencies = (key) => store.getMeta(key, 'deps') || [];
    that.clearChildren = key => {
        _.keys(that.get(key)).forEach(k => store.delete(`${key}.${k}`));
    };

    return that;
};