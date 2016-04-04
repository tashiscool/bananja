global.React = global.React || require('react');
var _ = require('lodash');
var RS = require('RS');
var ReactDom = require('react-dom');

var Component = module.exports = class Component extends React.Component {
    constructor() {
        super();
        addMountedFlag.call(this);
        this.state = {};
        this.autoruns = [];
    }

    autorun(fn) {
        this.autoruns.push(RS.autorun((firstRun) => this.mounted && fn.call(this, firstRun)));
    }

    registerStoreKey(key, name) {
        name = name ? name : key.replace(/.*\.(.*)/, '$1');
        this.autorun(() => this.setState({[name]: RS.get(key)}));
    }

    static renderToDom(ComponentClass, placeholder, props) {
        return ReactDom.render(<ComponentClass {...props} />, placeholder);
    }
};

function addMountedFlag() {
    this.componentWillUnmount = _.wrap(this.componentWillUnmount, (fn) => {
        this.mounted = false;
        fn && fn.call(this);
        this.autoruns.forEach(a => a.stop());
    });

    this.componentWillMount = _.wrap(this.componentWillMount, (fn) => {
        this.mounted = true;
        fn && fn.call(this);
    });
}
