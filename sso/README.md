# spid-clojure-sso-example

A Clojure bare-bones implementation of SSO with SPiD.

## Usage

Start by installing spid-sdk-clojure. It is unfortunately not on
Clojars, because the SPiD Java SDK isn't in a central nexus.

https://github.com/kodemaker/spid-sdk-clojure

Then fix the configuration by copying `config/config.edn.sample` to
`config/config.edn` - replacing `:client-id` and `:client-secret` with
your own credentials.

Then you can start the server with `lein ring server-headless`.
