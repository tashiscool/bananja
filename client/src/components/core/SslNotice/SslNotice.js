require('./ssl-notice.less');
module.exports = props => (
    <div className={`ssl-notice ${props.className || ''}`}>
        <p className={`notice font-centi ${props.childClassName || ''}`}>
            {gt.gettext('This is a secure 128-bit SSL encrypted payment system')}
        </p>
    </div>
);