var InfoBtn = require('shared/core/InfoBtn');
var Link = require('react-router').Link;
var LinkAlt = require('shared/core/Link');
var ImgRetina = require('components/core/ImgRetina');
require('components/widget/style/footer.less');

module.exports = props => (
    <footer className="footer" role="contentinfo">
        <Grid>
            <Row>
                <Col xs={12} sm={8}>
                    <ul className="footer__links">
                        <li><LinkAlt><InfoBtn infoLink="Trademark information" title="Trademark information" disableIcon>Asurion&reg;, the Asurion logo and other Asurion product names are the property of Asurion, LLC. All other trademarks, service marks, and product brands not owned by Asurion that appear here are the property of their respective owners. Asurion is not affiliated with, sponsored by, or endorsed by the respective owners of the other trademarks, service marks and/or product brands cited herein.</InfoBtn></LinkAlt></li>
                        <li><LinkTo to="/privacy-policy">Privacy Policy</LinkTo></li>
                        <li><LinkTo to="/terms-of-use">Terms of Use</LinkTo></li>
                    </ul>
                    <p className="footer__copyright">&copy; Asurion 2001-2014. All rights reserved. Visit <a href="https://www.asurion.com" target="_blank">asurion.com</a> to learn more.</p>
                </Col>
                <Col xs={12} sm={4}>
                    <div className="footer__logo"><LinkTo to="/"><ImgRetina imgSrc={ require('./logo-dealer-portal@2x.png') } imgAlt="Dealer Portal - powered by Asurion"/></LinkTo></div>
                </Col>
            </Row>
        </Grid>
    </footer>
);

