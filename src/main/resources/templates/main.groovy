package templates

yieldUnescaped '<!DOCTYPE html>'
print(authUrl, clientId, redirectUri, state)

def print(authUrl, clientId, redirectUri, state) {
    return html {
        head {
            meta('http-equiv': '"Content-Type" content="text/html; charset=utf-8"')
        }
        body {
            h1 'OpenId test'

            a(href: authUrl + '?response_type=code&client_id=' + clientId + '&login=true&redirect_uri=' + redirectUri + '&state=' + state,
                    style: 'font-weight: bold;') {
                yield '로그인'
            }
        }
    }
}