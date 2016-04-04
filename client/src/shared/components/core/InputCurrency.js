var InputText = require('shared/core/InputText');
require('shared/core/style/inputCurrency.less');

module.exports = class InputCurrency extends PureRenderComponent {

    getFixedValue(dirty, digits = 2) {
        var clean = parseFloat(dirty);

        return isNaN(clean) ? (0).toFixed(digits) : Math.abs(clean).toFixed(digits);
    }

    formatField(ev) {
        RS.set(this.props.rsKey, this.getFixedValue(ev.target.value))
    }

    render() {
        return (
            <InputText {...this.props} onBlur={this.formatField.bind(this)} icon='dollar'/>)
    }

};

