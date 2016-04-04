var Form = require( 'shared/group/InputForm' );

module.exports = props => (
        <div className='text-center'>
            <p>{gt.gettext( 'It may be a system error on our end.' )} {getVerbiage()}</p>
            <Form.BtnPrimary
                onClick={() => App.goto('/')}>{getButtonText()}</Form.BtnPrimary>
        </div>
);

function getVerbiage() {
    return (
        RS.get( 'user.id' ) ?
            gt.gettext( 'Return to your dashboard to manage claims, registrations, training, and marketing.' ) :
            gt.gettext( 'Return to xx.com to enroll and learn more about our program.' )
    )
}
function getButtonText() {
    return (
        RS.get( 'user.id' ) ?
            gt.gettext( 'Return to dashboard' ) :
            gt.gettext( 'Return to homepage' )
    )
}
