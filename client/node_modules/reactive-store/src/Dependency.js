var ReactiveContext = require('./ReactiveContext');

module.exports = function Dependency() {
    var that = {
        changed: function () {
            that.invalid = true;
            ReactiveContext.flushAll();
        },
        depend: function () {
            ReactiveContext.current && ReactiveContext.current.addDependency(that);
        }
    };
    return that;
}
