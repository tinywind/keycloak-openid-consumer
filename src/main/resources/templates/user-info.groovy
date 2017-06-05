package templates

yieldUnescaped '<!DOCTYPE html>'
html {
    head {
        meta('http-equiv': '"Content-Type" content="text/html; charset=utf-8"')
        style(type: 'text/css') {
            yield 'li {white-space: nowrap;}'
        }
    }
    body (style: 'overflow-x: hidden; width: 100%;') {
        h1 'OpenId test'

        div 'state: ' + state
        div 'session.state: ' + sessionState
        div 'code: ' + code
        div(style: 'white-space: nowrap;') { yield 'accessToken: ' + accessToken }

        h3 'token'
        ul {
            li 'accessToken : ' + token.accessToken
            li 'expiresIn : ' + token.expiresIn
            li 'refreshExpiresIn : ' + token.refreshExpiresIn
            li 'refreshToken : ' + token.refreshToken
            li 'tokenType : ' + token.tokenType
            li 'idToken : ' + token.idToken
            li 'sessionState : ' + token.sessionState
            li 'id.alg : ' + token.id.alg
            li 'id.aud : ' + token.id.aud
            li 'id.azp : ' + token.id.azp
            li 'id.email : ' + token.id.email
            li 'id.emailVerified : ' + token.id.emailVerified
            li 'id.exp : ' + token.id.exp
            li 'id.familyName : ' + token.id.familyName
            li 'id.givenName : ' + token.id.givenName
            li 'id.iat : ' + token.id.iat
            li 'id.iss : ' + token.id.iss
            li 'id.jti : ' + token.id.jti
            li 'id.name : ' + token.id.name
            li 'id.nbf : ' + token.id.nbf
            li 'id.preferredUsername : ' + token.id.preferredUsername
            li 'id.roles : ' + token.id.roles
            li 'id.sessionState : ' + token.id.sessionState
            li 'id.sub : ' + token.id.sub
            li 'id.typ : ' + token.id.typ

            if (token.id.address != null)
                token.id.address.forEach {
                    key, value -> li 'id.address.' + key + ' -> ' + value
                }
        }

        h3 'user-info'
        ul {
            userInfo.forEach {
                key, value -> li key + ' -> ' + value
            }
        }
    }
}
