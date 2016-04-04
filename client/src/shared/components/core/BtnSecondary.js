
var Btn = require('shared/core/Btn');

module.exports = class BtnSecondary extends PureRenderComponent {
    render() {
        return (
            <Btn type="secondary" {...this.props}>{this.props.children}</Btn>
        )
    }
};