module.exports = class ImgRetina extends PureRenderComponent {
    componentDidMount() {
        var img = new Image();
        img.src = this.props.imgSrc;
        img.onload = () => {
            this.setState({ imgWidth: shrink(img.width), imgHeight: shrink(img.height)});

            function shrink(size) {
                return size && size > 0 ? Math.round(size / 2) : 'auto';
            }
        }
    }

    render() {
        var props = this.props;
        return <span>{(this.state.imgWidth && this.state.imgHeight) && <img src={props.imgSrc} alt={props.imgAlt} {...props} width={this.state.imgWidth} height={this.state.imgHeight} {...props}/>}</span>;
    }

}