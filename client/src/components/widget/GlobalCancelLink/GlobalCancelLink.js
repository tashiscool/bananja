var Form = require('shared/group/InputForm');
var ModalWindow = require('shared/group/ModalWindow');
require('./global-cancel-link.less');

module.exports = class GlobalCancelLink extends PureRenderComponent {

    showCancelModal() {
        var settings = _.defaults({}, this.props, {
            title:         gt.gettext('Are you sure you want to cancel?'),
            bodyComponent: <p>{gt.gettext('If you leave this page, this task will be cancelled.')}</p>,
            closeBtnText:  gt.gettext('Continue'),
            actionBtnText: gt.gettext('Cancel'),
            action:        this.props.dest ? (() => App.goto(this.props.dest)) : (() => App.goto('/'))
        });

        this.modalWindow = ModalWindow.open(settings);
    }

    render() {
        return <Form.Link onClick={ this.showCancelModal.bind(this) }>{this.props.children}</Form.Link>;
    }
};
