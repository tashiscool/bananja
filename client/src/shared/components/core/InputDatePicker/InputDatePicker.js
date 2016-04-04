var InputText = require('shared/core/InputText');
var Btn = require('shared/core/Btn');
var DatePicker = require('react-date-picker');
require('react-date-picker/base.css');
require('./InputDatePicker.less');
/*For date picker options for the dateOptions obj, see https://github.com/zippyui/react-date-picker
    NOTE: all dates need to be PARSED ( Date.parse('your-date-string') ) in order to work
*/

module.exports = class InputDatePicker extends PureRenderComponent {

    componentDidMount() {
        this.setState({
            showDropdown: false,
            currentDate: this.props.startDate ? Date.parse(this.props.startDate) : Date.now()
        });
    }

    updateField(ev) {
        this.props.validRegex ? validateField.call(this) : RS.set(this.props.rsKey, ev.target.value);

        function validateField(){
            this.props.validRegex.test(ev.target.value) ?
                RS.set(this.props.rsKey, ev.target.value) :
                clearValues.call(this);
        }

        function clearValues() {
            RS.set(this.props.rsKey, '');
            this.setState({ showDropdown: true });
        }
    }

    dateSelected(value) {
        RS.set(this.props.rsKey, value);
        this.setState({ currentDate: value });
        this.hideDatePicker();
    }

    toggleDatePicker() {
        this.setState({ showDropdown: !this.state.showDropdown });
    }

    hideDatePicker() {
        this.setState({ showDropdown: false });
    }

    render() {
        var props = _.omit(this.props, 'dateOptions', 'startDate', 'dateFormat');
        var dOpts = this.props.dateOptions || {};
        return (<div className="datepicker-wrapper">
            <InputText {...props}
                onChange={this.updateField.bind(this)}
                addOn={<Btn type="primary" onClick={ this.toggleDatePicker.bind(this) }><span className="glyphicon glyphicon-calendar"/></Btn>}/>
            { this.state.showDropdown && <div className="datepicker-popup" >
                    <DatePicker
                        date={ this.state.currentDate }
                        dateFormat={ this.props.dateFormat || 'MM/DD/YYYY' }
                        onChange={ this.dateSelected.bind(this) }
                        {...dOpts}
                        hideFooter={ true }/>
              </div> }
            </div>);
    }



};

