

module.exports = class Link extends PureRenderComponent {
    render() {
        return <a href={this.props.href || 'javascript:void(0)'} {...this.props}>{this.props.children}</a>;
    }
};