var OverlayTrigger = require('react-bootstrap/lib/OverlayTrigger');
var Popover = require('react-bootstrap/lib/Popover');
var _ = require('lodash');
require('shared/core/style/infoBtn.less');
require('shared/core/style/popover.less');

module.exports = props => {
    var id = props.id || (props.title && props.title.replace(/[^a-zA-Z0-9]/g, '_')) || _.uniqueId();
    var popover = <Popover id={id} title={props.title}>{props.children}</Popover>;
    return (
        <span className="info-btn">
            <OverlayTrigger rootClose trigger="click" placement={props.placement || 'top'} overlay={popover}>
                <span>
                { props.infoLink && <span className="info-btn__trigger-text">{ props.infoLink }</span> }
                { props.disableIcon && props.infoLink ? undefined : <span className="info-btn__trigger"></span> }
                </span>
            </OverlayTrigger>
        </span>
    );
};
