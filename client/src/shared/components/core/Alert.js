var _ = require('lodash');
require('shared/core/style/alert.less');

module.exports = class Alert extends PureRenderComponent {
    render() {
        var props = _.omit(this.props, 'type', 'dismissable');
        return (
            <div className={`alert alert--${this.props.type || 'danger'}`} {...props}>
                {getDismissButton.call(this)}
                <div className="alert__content">
                    {this.props.children}
                </div>
            </div>
        )

        function getDismissButton() {
            /*close button currently controlled by bootstrap*/
            return this.props.dismissable && <button type="button" className="close" aria-hidden="true">×</button>;
        }
    }
};