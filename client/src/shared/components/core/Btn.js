
require('shared/core/style/btn.less');

module.exports = class Btn extends PureRenderComponent {
    render() {
        var props = _.omit(this.props, 'type', 'size', 'block');

        props = _.defaults({
            onClick: this.props.disabled ? undefined : this.props.onClick,
            className: [
                'btn',
                `btn--${this.props.disabled ? 'disabled' : this.props.type || 'default'}`,
                `btn--${this.props.size || 'lg'}`,
                this.props.block ? 'btn--block' : ''
            ].join(' '),
            role: 'button'
        }, props);

        return this.props.href ? <a {...props}>{this.props.children}</a> : <button {...props}>{this.props.children}</button>;
    }
};