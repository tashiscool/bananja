var AppService = module.exports = {
    getUrl: pathUrl => {
        var url = `/${App.context}/${App.language}`;
        return pathUrl.startsWith(url) ? pathUrl : buildUrl();

        function buildUrl() {
            pathUrl === '/' || (url += pathUrl);
            return url;
        }
    },
    getQueryStringAsMap:() => {      //Todo is this the right place to put this kind of thing?
        return _.chain(window.location.search ? window.location.search.slice(1).split('&') : '')
            .map(function(item) {
                var p = item.split('=');

                return [p[0], decodeURI(p[1])];
            })
            .object()
            .omit(_.isEmpty)
            .toJSON();

    },

    showHeaderSignInLink(){
        RS.set('app.hideSignInLink', false);
    },

    hideHeaderSignInLink(){
        RS.set('app.hideSignInLink', true);
    },

    getCCRPhone: () => { return '844-853-8954' },

    isPathActive: pathUrl => App.history.isActive(AppService.getUrl(pathUrl))
};