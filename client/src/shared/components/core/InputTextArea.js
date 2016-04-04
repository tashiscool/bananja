
var RS = require('RS');

module.exports = class InputTextArea extends PureRenderComponent {
    onChange(ev) {
        RS.set(this.props.rsKey, ev.target.value);
    }

    componentWillMount() {
        this.props.rsKey && this.registerStoreKey(this.props.rsKey, 'value');
        this.props.errorKey && this.registerStoreKey(this.props.errorKey, 'error');
    }

    render() {
        var props = _.defaults({}, this.props, {type: 'text'});
        return (
            <div className="input-textarea">
                <textarea {...props}
                    className={`input-textarea ${this.state.error ? 'input-error' : ''}`}
                    defaultValue={this.state.value || ""}
                    value={this.state.value}
                    onChange={this.props.onChange? this.props.onChange : this.onChange.bind(this)}
                    maxLength={this.props.maxLength || ''}/>
                { this.props.maxLength && <span className="input-textarea__maxlength">{_.size(this.state.value) + '/' + this.props.maxLength }</span> }
                { this.state.error && <label className="input-error_label">{this.state.error}</label>}
            </div>
        )
    }

};