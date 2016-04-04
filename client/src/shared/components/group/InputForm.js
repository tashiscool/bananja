
var RS = require('RS');
var FormService = require('shared/services/FormService');
var _ = require('lodash');
require('shared/group/style/inputForm.less');

var InputForm = module.exports = class InputForm extends PureRenderComponent {
    componentWillMount() {
        setupFormUpdateValidation.call(this);
    }

    isValid() {
        FormService.validateForm(this.props.rsKey);
        return FormService.isFormValid(this.props.rsKey);
    }

    isValidField( fieldName ) {
        FormService.validateField(this.props.rsKey, fieldName);
        return FormService.isFieldValid(this.props.rsKey, fieldName);
    }

    clear() {
        _.each(RS.get(`${this.props.rsKey}.values`), (v, k) => RS.set(`${this.props.rsKey}.values.${k}`, undefined));
        this.clearErrors();
    }

    getValue(name) {
        return RS.get(`${this.props.rsKey}.values.${name}`);
    }

    getValues() {
        return RS.get(`${this.props.rsKey}.values`);
    }

    setValue(name, value) {
        return RS.set(`${this.props.rsKey}.values.${name}`, value);
    }

    setValues(values) {
        return RS.set(`${this.props.rsKey}.values`, values);
    }

    clearErrors() {
        _.each(RS.get(`${this.props.rsKey}.errors`), (v, k) => RS.set(`${this.props.rsKey}.errors.${k}`, undefined));
    }

    setError(name, value) {
        return RS.set(`${this.props.rsKey}.errors.${name}`, value);
    }

    getError(name) {
        return RS.get(`${this.props.rsKey}.errors.${name}`);
    }

    hasError(name) {
        return !!RS.get(`${this.props.rsKey}.errors.${name}`);
    }

    setMeta(name, value) {
        return RS.set(`${this.props.rsKey}.meta.${name}`, value);
    }

    getMeta(name) {
        return RS.get(`${this.props.rsKey}.meta.${name}`);
    }

    render() {
        return (
            <form onSubmit={onSubmit.bind(this)} className="input-form" noValidate>
                {gatherFormChildren.call(this, this)}
            </form>
        );

        function gatherFormChildren(component) {
            return React.Children.map(component.props.children, child => {
                return child ? processChild.call(this, child) : child;

                function processChild(child) {
                    if ( !child.props ) {
                        return child
                    }
                    if (React.Children.count(child.props.children)) {
                        return React.cloneElement(child.props.name ? setupChild.call(this, child) : child, [], gatherFormChildren.call(this, child));
                    } else {
                        return child.props.name ? setupChild.call(this, child) : child;
                    }
                }
            });
        }

        function setupChild(child) {
            var props = {};
            this.props.rsKey && !child.props.rsKey && (props.rsKey = `${this.props.rsKey}.values.${child.props.name}`);
            this.props.rsKey && !child.props.errorKey && (props.errorKey = `${this.props.rsKey}.errors.${child.props.name}`);

            this.props.rsKey && setupValidation.call(this);
            return createChildElement.call(this);

            function createChildElement() {
                return <InputForm.InputGroup formKey={this.props.rsKey}
                                             childName={child.props.name}>{React.cloneElement(child, props)}</InputForm.InputGroup>
            }

            function setupValidation() {
                var validationKey = `${this.props.rsKey}.validators.${child.props.name}`;
                var validators = [];
                child.props.required && validators.push({test: 'required', text: child.props.required});
                child.props.validPhoneNumber && validators.push({test: 'phoneNumber'});
                child.props.customValidator && validators.push({
                    test: 'custom',
                    fn: child.props.customValidator.toString()
                });
                child.props.minLength && validators.push({test: 'minLength', length: child.props.minLength, text: child.props.minLengthText});
                child.props.maxLength && validators.push({test: 'maxLength', length: child.props.maxLength, text: child.props.maxLengthText});
                child.props.validRegex && validators.push({test: 'regex', regex: child.props.validRegex, text: child.props.validRegexText});
                RS.set(validationKey, validators);
            }
        }

        function onSubmit(ev) {
            ev.preventDefault();
            this.props.onSubmit && this.props.onSubmit(ev);
        }
    }
};

function setupFormUpdateValidation() {
    this.autorun(() => {
        var lastValidation;
        RS.nonReactive(() => lastValidation = RS.get(`${this.props.rsKey}.meta.lastValidation`));

        RS.get(`${this.props.rsKey}.values`) &&
        lastValidation &&
        new Date().getTime() - lastValidation > 100 &&
        FormService.validateForm(this.props.rsKey);
    });
}


_.extend(InputForm, {
    Link: require('shared/core/Link'),
    Btn: require('shared/core/Btn'),
    BtnGroup: require('shared/group/BtnGroup'),
    BtnPrimary: require('shared/core/BtnPrimary'),
    BtnSecondary: require('shared/core/BtnSecondary'),
    BtnSecondaryOutline: require('shared/core/BtnSecondaryOutline'),
    InfoBtn: require('shared/core/InfoBtn'),
    InputGroup: require('shared/group/InputGroup'),
    InputSelect: require('shared/core/InputSelect'),
    InputSelectCountry: require('shared/core/InputSelectCountry'),
    InputText: require('shared/core/InputText'),
    InputTextArea: require('shared/core/InputTextArea'),
    InputTextReadOnly: require('shared/core/InputTextReadOnly'),
    InputCheckbox: require('shared/core/InputCheckbox'),
    InputRadio: require('shared/core/InputRadio'),
    InputTypeahead: require('shared/core/InputTypeahead'),
    InputCurrency: require('shared/core/InputCurrency'),
    InputPhoneFormat: require('shared/core/InputPhoneFormat'),
    InputDatePicker: require('shared/core/InputDatePicker')
});
