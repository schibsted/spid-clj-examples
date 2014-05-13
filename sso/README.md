# spid-clojure-sso-example

A Clojure bare-bones implementation of SSO with SPiD.

## Usage

1. **Fill in the configuration**

   ```sh
   cd sso/config
   cp config.edn.sample config.edn
   vim config.edn
   ```

   Replace `:client-id` and `:client-secret` with your own credentials.

2. **Start the server**

   ```sh
   lein ring server-headless
   ```
