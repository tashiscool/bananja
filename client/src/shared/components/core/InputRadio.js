
var RS = require('RS');

module.exports = class InputRadio extends PureRenderComponent {
    onChange( ev ) {
        RS.set( this.props.rsKey, ev.target.value );
    }

    componentWillMount() {
        this.props.errorKey && this.registerStoreKey( this.props.errorKey, 'error' );
        this.autorun(() => {
            this.props.rsKey && this.setState({checked: RS.get(this.props.rsKey) === this.props.value});
        });
    }

    render() {
        var id = this.props.id || `radio-${_.uniqueId()}`;
        return (
            <div className={`input-radio ${this.state.error ? 'input-error' : ''}`}>
                <input {...this.props} type="radio" id={id} onChange={this.onChange.bind(this)} value={this.props.value} checked={this.state.checked} />
                <label htmlFor={id}><span className="input-radio__img"/><span className="input-radio__text">{this.props.radioLabel}</span></label>
                {this.state.error && <label className="input-error_label">{this.state.error}</label>}
            </div>
        )
    }

};