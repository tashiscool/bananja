var _ = require('lodash');
var Jed = require('jed');

var i18n;

var that = module.exports = {
    init(po) {
        i18n = new Jed(po);
        return that;
    }
};

Object.keys(Jed.prototype).forEach(k => {
    that[k] = (...args) => escape(i18n[k].apply(i18n, args));
});


// wrap gettext to allow components to be mixed in
that.gettext = (text, ...args) => {
    text = i18n.gettext.call(i18n, text);
    var parts = text.split(/\${[0-9]}/);
    parts = parts.reduce((memo, it, idx) => {
        memo.push(escape(it));
        args[idx] && memo.push(<xx key={`xx-${idx}`}>{args[idx]}</xx>);
        return memo;
    }, []);
    return parts.length === 1 ? parts[0] : parts;
};

function escape(text) {
    return containsHtml(text) ? <dangerousText dangerouslySetInnerHTML={{__html:text}} /> : text;
}


function containsHtml(text) {
    return /\<.*\>/.test(text) || /\&.*\;/.test(text);
}