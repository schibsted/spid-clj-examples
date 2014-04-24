# spid-clojure-sso-example

A Clojure bare-bones implementation of SSO with SPiD.

## Usage

1. **Download and install the Clojure SDK**

   Start by installing spid-sdk-clojure. It is unfortunately not on
   Clojars, because the SPiD Java SDK isn't in a central nexus.

   https://github.com/kodemaker/spid-sdk-clojure

2. **Fill in the configuration**

   ```sh
   cd sso/config
   cp config.edn.sample config.edn
   vim config.edn
   ```

   Replace `:client-id` and `:client-secret` with your own credentials.

3. **Start the server**

   ```sh
   lein ring server-headless
   ```
