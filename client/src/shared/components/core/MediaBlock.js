require('shared/core/style/mediaBlock.less');

module.exports = props => (
    <div className="media-block">
        <div className="media-block__img">
            { props.imgSrc && <img src={ props.imgSrc } alt={ props.imgAlt || 'Content Image'}/> }
        </div>
        <div className="media-block__bdy">
            { props.children }
        </div>
    </div>
);

