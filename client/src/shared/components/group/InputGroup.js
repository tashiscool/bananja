var Alert = require('shared/core/Alert');
var text = require('GetText');

module.exports = class InputGroup extends PureRenderComponent {

    componentWillMount() {
        this.registerStoreKey(`${this.props.formKey}.warnings.${this.props.childName}`, 'warning');
    }

    componentWillUnmount() {
        RS.clearChildren(`${this.props.formKey}.validators.${this.props.childName}`, undefined);
    }

    render() {
        var child = React.Children.toArray(this.props.children)[0];
        var label = this.props.label || child.props.label;
        var labelMessage = this.props.labelMessage || child.props.labelMessage;
        var helpText = this.props.helpText || child.props.helpText;
        var optional = this.props.optional || child.props.optional;
        var infoBtn = this.props.infoBtn || child.props.infoBtn;
        var [id, children] = addIdToChild.call(this);

        return (
            <div className={`input-form__group ${getOptionalClass()}` }>
                { label && <label className="input-form__group-label" htmlFor={id}>{ label }
                    { labelMessage && <span className="input-form__group-label-message"> { labelMessage }</span>}
                    { optional && <span className="input-form__group-label-optional"> { text('address.field.label.optional') }</span>}
                    </label> }
                    {infoBtn && infoBtn}
                { children }
                { helpText && <p className="input-form__group-helper">{helpText}</p> }
                { this.state.warning && <Alert>{/\<.*\>/.test(this.state.warning) ? warningTest(this.state.warning) : this.state.warning}</Alert> }
            </div>
        );

        function getOptionalClass() {
            return child.props.optionClass ? child.props.optionClass : '';
        }

        function addIdToChild() {
            return React.Children.count(this.props.children) === 1 ? doAddIdToChild.call(this) : [undefined, this.props.children];

            function doAddIdToChild() {
                var child = React.Children.only(this.props.children);
                var id = child.props.id || `generated-id-${_.uniqueId()}`;
                return [id, React.cloneElement(child, {id: id})];
            }
        }

        function warningTest(message) {
            return <dangerousText dangerouslySetInnerHTML={{__html: message}} />
        }
    }
};