var Form = require('shared/group/InputForm');
var Alert = require('shared/core/Alert');
var LinkTo = require('components/core/LinkTo');
var UserService = require('services/UserService');

module.exports = class SignIn extends PureRenderComponent {


    componentWillMount() {
        this.registerStoreKey('signin.alertBar');

    }


    signin() {
        var obj = RS.get("signin-form.values");
        this.refs.signInForm.isValid() && UserService.authenticate(obj.phone, obj.password).then(UserService.signInComplete);

    }

    render() {

        return (
            <div>
                { _.size(this.state.alertBar) > 0  &&
                <Alert type="danger" imgSrc={ require('components/page/img/icon-info.png') } imgAlt="Info Icon"
                       onClose={ () => {RS.set('signin.alertBar', [])} }>{this.state.alertBar}</Alert> }
                    <Form ref="signInForm" rsKey="signin-form">
                        <Form.InputText name="phone" label={gt.gettext("Email Address")}
                                        customValidator={value => /^[-a-z0-9~!$%^&*_=+}{\'?]+(\.[-a-z0-9~!$%^&*_=+}{\'?]+)*@([a-z0-9_][-a-z0-9_]*(\.[-a-z0-9_]+)*\.(aero|arpa|biz|com|coop|edu|gov|info|int|mil|museum|name|net|org|pro|travel|mobi|[a-z][a-z])|([0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}))(:[0-9]{1,5})?$/i.test(value) ? '' : 'Please enter a valid email address'}/>
                        <Form.InputText name="password" label={gt.gettext("Password")}
                                        type="password"
                                        customValidator={value => /(?=.*[0-9])(?=.*[a-zA-Z])(.+){7,}/i.test(value) ? '' : 'Please enter a valid password'}
                                        helpText={gt.gettext("Note: Minimum 7 characters with at least 1 number and 1 letter.")}/>
                    </Form>
                    <Form.BtnPrimary onClick={this.signin.bind(this)}>{gt.gettext("Sign in")}</Form.BtnPrimary>
                <LinkTo to="/password/reset">{gt.gettext('Forgot password?')}</LinkTo>
            </div>
        );
    }
};
