package templates

//import static templates.tags.*

yieldUnescaped '<!DOCTYPE html>'
html {
    head {
        meta('http-equiv': '"Content-Type" content="text/html; charset=utf-8"')
    }
    body {
        h1 'OpenId test'

        a(href: KEYCLOAK_AUTH_URL + '?response_type=code&client_id=' + clientId + '&login=true&redirect_uri=' + redirectUri + '&state=' + state,
                style: 'font-weight: bold;') {
            yield '로그인'
        }
    }
}