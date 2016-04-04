var RS = require('RS');
var _ = require('lodash');

var FormService = module.exports = {
    validateForm(rsKey) {
        RS.set(`${rsKey}.meta.lastValidation`, new Date().getTime());
        var validatorsList = RS.get(`${rsKey}.validators`);
        clearErrorsWithNoValidator();
        _.each(validatorsList, (validators, name) => FormService.validateField(rsKey, name));

        function clearErrorsWithNoValidator() {
            var errorsKey = `${rsKey}.errors`;
            _.difference(_.keys(RS.get(errorsKey), _.keys(validatorsList))).forEach(name => RS.set(`${errorsKey}.${name}`, undefined));
        }
    },

    isFormValid(rsKey) {
        var errors = RS.get(`${rsKey}.errors`);
        return !_.any(_.values(errors));
    },

    validateField(rsKey, fieldName) {
        var validatorsList = RS.get(`${rsKey}.validators.${fieldName}`);
        var errors = _.map(validatorsList, runValidator).filter(e => e);
        RS.set(`${rsKey}.errors.${fieldName}`, errors[0]);


        function runValidator(validationObj) {
            var testFn = validationFunctions[validationObj.test];
            testFn === undefined && console.log('no validation function found for ', validationObj);
            return testFn(validationObj, RS.get(`${rsKey}.values.${fieldName}`));
        }

    },

    isFieldValid(rsKey, fieldName) {
        var errors = RS.get(`${rsKey}.errors.${fieldName}`);
        return !_.any(_.values(errors));
    }
};

var validationFunctions = {
    required: (validationObj, value) => value ? '' : validationObj.text,
    phoneNumber: (validationObj, value) => {
        return value && numbersOnly().length === 10 ? '' : 'Please enter a valid phone number using 10 numbers';

        function numbersOnly() {
            return value.replace(/[^0-9]/g, '')
        }
    },
    custom: (validationObj, value) => {
        var fn;
        eval(`fn = ${validationObj.fn}`);
        return fn(value);
    },
    minLength: (validationObj, value) => _.size(value) >= validationObj.length ? '' : validationObj.text || text('min.length.fail').replace('{0}', validationObj.length),
    maxLength: (validationObj, value) => _.size(value) <= validationObj.length ? '' : validationObj.text || text('max.length.fail').replace('{0}', validationObj.length),
    regex: (validationObj, value) => validationObj.regex.test(value) ? '' : validationObj.text || text('regex.fail')
};


