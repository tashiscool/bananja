var Modal = require('react-bootstrap/lib/Modal?bypass');
var Form = require('shared/group/InputForm');
var Component = require('Component');

require('shared/group/style/modalWindow.less');

module.exports = class ModalWindow extends Component {
    constructor() {
        super();
        this.state = {show:true};
    }

    close() {
        this.setState({show: false});
        $j(this.props.container).remove();
    }

    doAction() {
        this.props.action.call(null, this) !== false && this.close();
    }

    doCloseAction() {
        this.props.closeBtnAction.call(null, this) !== false && this.close();
    }

    render() {
        return (
            <Modal show={this.state.show} onHide={this.close.bind(this)}>
                <Modal.Header closeButton>
                    <Modal.Title>{this.props.title}</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {this.props.bodyComponent}
                </Modal.Body>
                <Modal.Footer>
                    {getButtons.call(this)}
                </Modal.Footer>
            </Modal>
        );

        function getButtons() {
            var buttons = [];
            this.props.closeBtnText !== '' && buttons.push(cancelBtn.call(this));
            this.props.action && buttons.push(actionBtn.call(this))
            return <Form.BtnGroup type={this.props.btnGroupType || 'default'}>{buttons}</Form.BtnGroup>;

            function cancelBtn() {
                return (
                    <Form.Btn type={this.props.closeBtnType || 'secondary-outline'} size={this.props.closeBtnSize || 'lg'} key="cancelBtn" onClick={this.props.closeBtnAction ? this.doCloseAction.bind(this) : this.close.bind(this)}>{this.props.closeBtnText || 'Cancel'}</Form.Btn>
                )
            }

            function actionBtn() {
                return (
                    <Form.Btn type={this.props.actionBtnType || 'primary'} size={this.props.actionBtnSize || 'lg'} key="actionBtn" onClick={this.doAction.bind(this)}>{this.props.actionBtnText || 'Continue'}</Form.Btn>
                )
            }
        }
    }

    static open(props) {
        var container = $j('<div></div>').get(0);
        $j('body').append(container);

        return Component.renderToDom(ModalWindow, container, _.extend({}, props, {container: container}));
    }
};