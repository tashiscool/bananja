var _ = require('lodash');

module.exports = class PureRenderComponent extends Component {
    shouldComponentUpdate(nextProps, nextState) {
        return !(_.isEqual(this.props, nextProps) && _.isEqual(this.state, nextState));
    }
};