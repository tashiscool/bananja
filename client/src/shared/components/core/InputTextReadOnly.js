
var RS = require('RS');

module.exports = class InputTextReadOnly extends PureRenderComponent {

    componentWillMount() {
        this.props.rsKey && this.registerStoreKey(this.props.rsKey, 'value');
        RS.set(this.props.errorKey, undefined);
    }

    render() {
        return (
            <div className="input-text--read-only">
                <p className="input-text--read-only_content">{this.props.displayValue ? this.props.displayValue : this.state.value}</p>
            </div>
        );
    }
};