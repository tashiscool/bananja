var AppService = require('services/AppService');
var Link = require('react-router').Link;
var _ = require('lodash');

module.exports = props => {
    props = _.defaults({to: AppService.getUrl(props.to)}, props);

    return <Link {...props}>{props.children}</Link>
};