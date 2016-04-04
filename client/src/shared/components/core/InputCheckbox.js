
var RS = require('RS');

module.exports = class InputCheckbox extends PureRenderComponent {
    onChange(ev) {
        RS.set(this.props.rsKey, ev.target.checked);
    }

    componentWillMount() {
        this.registerStoreKey(this.props.rsKey, 'checked');
        this.props.errorKey && this.registerStoreKey( this.props.errorKey, 'error' );
    }

    render() {
        var id = this.props.id || `checkbox-${_.uniqueId()}`;
        return (
            <div className={`input-checkbox ${this.state.error ? 'input-error' : ''}`}>
                <input type="checkbox" id={id} {...this.props} onChange={this.onChange.bind(this)} checked={this.state.checked} />
                <label htmlFor={id}><span className="input-checkbox__img"/><span className="input-checkbox__text">{this.props.checkboxLabel}</span></label>
                {this.state.error && <label className="input-error_label">{this.state.error}</label>}
            </div>
        )
    }

};



