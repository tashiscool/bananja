var InputText = require('shared/core/InputText');
var $j = require('jquery');
var awesomplete = require('awesomplete');
require('awesomplete/awesomplete.css');
require('shared/core/style/typeahead.less');
var ReactDom = require('react-dom');

module.exports = class InputTypeahead extends PureRenderComponent {
    componentDidMount() {
        var that = this;
        this.registerStoreKey(this.props.listKey, 'list');
        this.awesomplete = new Awesomplete($j(ReactDom.findDOMNode(this)).find('input').get(0), {list: this.state.list, autoFirst: true});
        this.props.completeCallback && $j(ReactDom.findDOMNode(this)).on('awesomplete-selectcomplete', function(evt){
            RS.set(that.props.rsKey, evt.target.value);
            that.props.completeCallback();
        });
    }

    componentDidUpdate() {
        this.awesomplete.list = this.state.list;
        this.awesomplete.evaluate();
    }

    render() {
        return  <InputText {...this.props} />
    }
};
