# spid-clojure-paylinks-example

A Clojure bare-bones implementation of Paylinks with SPiD.

## Usage

1. **Fill in the configuration**

   ```sh
   cd paylinks/config
   cp config.edn.sample config.edn
   vim config.edn
   ```

   Replace `:client-id` and `:client-secret` with your own credentials.

2. **Start the server**

   ```sh
   lein ring server-headless
   ```
