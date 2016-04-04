
var Btn = require('shared/core/Btn');

module.exports = class BtnSecondaryOutline extends PureRenderComponent {
    render() {
        return (
            <Btn type="secondary-outline" {...this.props}>{this.props.children}</Btn>
        )
    }
};