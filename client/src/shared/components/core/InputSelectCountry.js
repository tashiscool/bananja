
var InputSelect = require('shared/core/InputSelect');
var CountryDataService = require('shared/services/CountryDataService');
var text = require('GetText');

module.exports = class InputSelectCountry extends PureRenderComponent {
    componentWillMount() {
        this.props.optionsKey && this.registerStoreKey(this.props.optionsKey, 'options');
    }

    render() {
        return <InputSelect options={countryOptions} {...this.props} />
    }
};

var PREFIX = '---';

var COUNTRY_GROUP_LABELS = {
    NORTH_AMERICA: '',
    US_TERRITORY: PREFIX + text('address.country.divider.territory') + PREFIX,
    MILITARY: PREFIX + text('address.country.divider.military') + PREFIX,
    INTERNATIONAL: PREFIX + text('address.country.divider.international') + PREFIX
};



var countryOptions = (function() {
    return _.map(CountryDataService.countryData, (data, key) => ({
        label: COUNTRY_GROUP_LABELS[key],
        options: _.map(data, (name, code) => ({value: code, text: name}))
    }));
}());


