package templates

//import static templates.tags.*

yieldUnescaped '<!DOCTYPE html>'
html {
    head {
        meta('http-equiv': '"Content-Type" content="text/html; charset=utf-8"')
    }
    body {
        h1 'OpenId test'

        div 'state: ' + state
        div 'session.state: ' + sessionState
        div 'code: ' + code
        div(style: 'white-space: nowrap;') { yield 'accessToken: ' + accessToken }

        h3 'user-info'
        ul {
            userInfo.forEach {
                key, value -> li key + ' -> ' + value
            }
        }
    }
}
