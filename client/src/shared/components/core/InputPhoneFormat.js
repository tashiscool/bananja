var InputText = require('shared/core/InputText');

module.exports = class InputPhoneFormat extends PureRenderComponent {

    formatField(ev) {
        _.size(ev.target.value) > 0 ? RS.set(this.props.rsKey, this.formatPhone(ev.target.value)) : RS.set(this.props.rsKey, ev.target.value);
    }

    formatPhone(dirty) {
        var rawValue = dirty.replace(/[\D]+/g, '');
        return /[\D]+/g.test(rawValue) ? dirty : testLength(rawValue);

        function testLength(digitVal){
            return _.size(digitVal) >= 3 ? hyphenate(digitVal) : digitVal;
        }

        function hyphenate(val) {
            var npa = _.size(val.substr(0, 4)) === 4 ? val.substr(0, 3) + '-' + val.substr(3, 1) : val;
            var nxx = _.size(val.substr(4, 3)) === 3 ? val.substr(4, 2) + '-' + val.substr(6, 1) : val.substr(4, 3);
            var last3 = val.substr(7, 3);
            return npa + nxx + last3;
        }
    }

    render() {
        return (
            <InputText {...this.props} onChange={this.formatField.bind(this)} maxLength={12}/>)
    }

};

