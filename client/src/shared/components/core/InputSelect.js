
var _ = require('lodash');
var RS = require('RS');

module.exports = class InputSelect extends PureRenderComponent {
    onChange(ev) {
        this.props.onChange && this.props.onChange(ev);
        this.props.rsKey && RS.set(this.props.rsKey, ev.target.value);
    }

    componentWillMount() {
        this.props.optionsKey && this.registerStoreKey(this.props.optionsKey, 'options');
        this.props.rsKey && this.registerStoreKey(this.props.rsKey, 'value');
        this.props.errorKey && this.registerStoreKey(this.props.errorKey, 'error');
    }

    componentDidMount() {
        var options = this.props.optionsKey ? this.state.options : this.props.options;
        this.props.rsKey && _.size(RS.get(this.props.rsKey)) === 0 && options[0].value && RS.set(this.props.rsKey, options[0].value);
    }

    render() {
        var options = this.props.optionsKey ? this.state.options : this.props.options;
        return (
            <div>
                <div className={`input-select ${this.state.error ? 'input-error' : ''}`}>
                    <select {...this.props} onChange={this.onChange.bind(this)} value={this.state.value}>
                        {options.map(item => item.options ? optGroup(item) : option(item))}
                    </select>
                </div>
                {this.state.error && <label className="input-error_label">{this.state.error}</label>}
            </div>
        );

        function optGroup(group) {
            return (
                <optgroup key={group.key || _.uniqueId()} label={group.label}>
                    {group.options.map(option)}
                </optgroup>
            )
        }

        function option(opt) {
            return (
                <option key={opt.key || opt.value || _.uniqueId()} value={opt.value}>{opt.text}</option>
            )
        }
    }
};
