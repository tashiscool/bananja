
var Btn = require('shared/core/Btn');

module.exports = class BtnPrimary extends PureRenderComponent {
    render() {
        return (
            <Btn type="primary" {...this.props}>{this.props.children}</Btn>
        )
    }
};