# Getting started with the SPiD Clojure SDK

The following is a minimal example of using the Clojure SDK. It fetches the
`/endpoints` endpoint, which returns a description of all available endpoints.

**NB!** To run the example, you need to know your client ID and API secret.

## Usage

1. **Download and install the Clojure SDK**

   Start by installing spid-sdk-clojure. It is unfortunately not on
   Clojars, because the SPiD Java SDK isn't in a central nexus.

   https://github.com/kodemaker/spid-sdk-clojure

2. **Run the example**

   ```sh
   lein run <client-id> <secret>
   ```

   Replace pointy bracketed items with your credentials.

This will print the JSON-decoded response from the server, which shows all
available endpoints along with details on how to interact with them.
