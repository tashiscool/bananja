
var RS = require('RS');

module.exports = class InputText extends PureRenderComponent {
    onChange(ev) {
        RS.set(this.props.rsKey, ev.target.value);
    }

    onBlur(ev) {
        RS.set(this.props.rsKey, ev.target.value);
    }

    componentWillMount() {
        this.props.rsKey && this.registerStoreKey(this.props.rsKey, 'value');
        this.props.errorKey && this.registerStoreKey(this.props.errorKey, 'error');
    }

    render() {
        var props = _.defaults({}, this.props, {type: this.props.type || 'text'});
        return (
            <div className="input-text">
                { this.props.icon && <span className={'input-text__icon input-text__icon--' + this.props.icon}/>}
                { this.props.addOn ? addOnWrap.call(this) : getInput.call(this) }
                { this.state.error && <label className="input-error_label">{this.state.error}</label>}
            </div>
        );

        function addOnWrap() {
            return <div className="input-add-on">
                    { getInput.call(this) }
                <span className="input-add-on__item">{this.props.addOn}</span>
                </div>;
        }

        function getInput() {
            return <input {...props} className={`input-text ${this.state.error ? 'input-error' : ''}`} value={this.state.value} onBlur={this.props.onBlur ? this.props.onBlur : this.onBlur.bind(this)} onChange={this.props.onChange? this.props.onChange : this.onChange.bind(this)} />;
        }
    }

};